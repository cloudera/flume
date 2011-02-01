/**
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.flume.agent.durability;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.cloudera.flume.core.EventSource;
import com.cloudera.flume.handlers.hdfs.SeqfileEventSource;
import com.cloudera.util.Clock;
import com.cloudera.util.FileUtil;

/**
 * This tests the write ahead log source against some trouble conditions --
 * empty file, truncated file.
 */
public class TestNaiveFileWALSource {

  // has 5 good entries.
  final static String WAL_OK = "src/data/hadoop_logs_5.hdfs";

  // this file has been prematurely truncated and is thus corrupt.
  final static String WAL_CORRUPT = "src/data/hadoop_logs_5.hdfs.aa";

  @Before
  public void setUp() {
    System.out.println("====================================================");
    Logger LOG = Logger.getLogger(NaiveFileWALManager.class.getName());
    LOG.setLevel(Level.DEBUG);
  }

  /**
   * Seqfile should fail on open when reading an empty file
   */
  @Test
  public void testSeqfileErrorOnOpen() throws IOException, InterruptedException {
    System.out.println("Exception on open empty file with seqfile");
    File tmpdir = FileUtil.mktempdir();
    tmpdir.deleteOnExit();

    // create empty file.
    File corrupt = new File(tmpdir,
        "walempty.00000000.20091104-101213997-0800.seq");
    System.out.println("corrupt file is named: " + corrupt.getAbsolutePath());
    corrupt.createNewFile();
    corrupt.deleteOnExit();

    // check now, and any age is too old.
    File commit = new File(tmpdir, "committed");
    commit.deleteOnExit();
    EventSource src = new SeqfileEventSource(corrupt.getAbsolutePath());

    try {
      src.open();
    } catch (IOException e) {
      return;
    }

    Assert.fail("should have failed with io exception");

  }

  /**
   * WAL should succeed on open even if its internal opens fail. It will block
   * on next() while continuing to try get a valid source of events.
   * 
   * This test demonstrates this by starting the WALSource, calling next in a
   * separate thread, and waits a little. Nothing should have happened.
   */
  @Test
  public void testSurviveErrorOnOpen() throws IOException, InterruptedException {
    System.out.println("Survive error on open with WALSource");
    File basedir = FileUtil.mktempdir();
    basedir.deleteOnExit();

    // create empty file.
    File logDir = new File(basedir, NaiveFileWALManager.LOGGEDDIR);
    logDir.mkdirs();
    File corrupt = new File(logDir,
        "walempty.00000000.20091104-101213997-0800.seq");
    System.out.println("corrupt file is named: " + corrupt.getAbsolutePath());
    corrupt.createNewFile();
    corrupt.deleteOnExit();

    NaiveFileWALManager walman = new NaiveFileWALManager(basedir);
    final WALSource src = new WALSource(walman);
    // open would normally fail but because this wrapped, this is ok.
    src.open();
    src.recover();
    final AtomicBoolean okstate = new AtomicBoolean(true);

    Thread t = new Thread() {
      public void run() {
        try {

          // this should block and never make progress.
          src.next();
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          // this should never execute.
          okstate.set(false);
        }
      }
    };
    t.start();

    // TODO (jon) remove sleeps
    // yeah, I know you don't like sleeps.. getting into the DirWatcher is more
    // trouble than I want right now.
    Clock.sleep(3000);

    src.close();

    Assert.assertTrue(okstate.get()); // no unexepcted exns or fall throughs.
  }

  /**
   * In this situation WAL is open and has a file that starts off being ok. It
   * should then encounter a file with zero size and fails to open). It should
   * continue if there are more data or block if there is not.
   */
  @Test
  public void testSurviveEmptyFile() throws IOException, InterruptedException {

    System.out.println("Survive empty file with walsource");
    File basedir = FileUtil.mktempdir();
    basedir.deleteOnExit();

    // create a few empty files and writing them to the wal/logged dir
    File logdir = new File(basedir, NaiveFileWALManager.LOGGEDDIR);
    logdir.mkdirs();
    File emptyfile = new File(logdir,
        "walempty.0000000.20091104-101213997-0800.seq");
    System.out.println("zero file is named: " + emptyfile.getAbsolutePath());
    emptyfile.createNewFile();
    emptyfile.deleteOnExit();

    // copy an ok file that has exactly 5 entries into the wal/logged dir
    File orig = new File(WAL_OK);
    File ok = new File(logdir, "ok.0000000.20091104-101213997-0800.seq");
    FileUtil.dumbfilecopy(orig, ok);

    // check now, and any age is too old.
    NaiveFileWALManager walman = new NaiveFileWALManager(basedir);
    final WALSource src = new WALSource(walman);

    // inject data using recovery mode
    src.open();
    src.recover();

    final AtomicInteger count = new AtomicInteger();
    final AtomicBoolean okstate = new AtomicBoolean(true);

    Thread t = new Thread("poller") {
      public void run() {
        try {
          for (int i = 0; i < 10; i++) {
            // this eventually blocks and never make progress.
            // It will always read the good entries and skip over the bad file.
            src.next();
            count.getAndIncrement();
          }
        } catch (Exception e) {
          System.out.println("about to fail because of " + e);
          e.printStackTrace();
          okstate.set(false);
        }
      }
    };
    t.start();

    // TODO (jon) remove sleeps
    // yeah, I know you don't like sleeps.. getting into the DirWatcher is more
    // trouble than I want right now.
    Clock.sleep(3000);

    src.close();

    Assert.assertTrue(okstate.get()); // no unexpected exceptions
    Assert.assertEquals(5, count.get());

    // After this call okstate will be false becuase IOExcpetion is thrown on
    // close.

  }

  /**
   * In this situation WAL is open and has a file that starts off being ok. It
   * should then encounter a file with zero size and fails to open). It should
   * continue if there are more data or block if there is not.
   */
  @Test
  public void testSurviveTwoEmptyFiles() throws IOException,
      InterruptedException {

    System.out.println("Survive two empty files with walsource");
    File basedir = FileUtil.mktempdir();
    basedir.deleteOnExit();

    // create a few empty files.
    File logdir = new File(basedir, NaiveFileWALManager.LOGGEDDIR);
    logdir.mkdirs();
    File emptyfile = new File(logdir,
        "walempty.0000000.20091104-101213997-0800.seq");
    System.out.println("zero file is named: " + emptyfile.getAbsolutePath());
    emptyfile.createNewFile();
    emptyfile.deleteOnExit();

    File emptyfile2 = new File(logdir,
        "walempty2.0000000.20091104-101213997-0800.seq");
    System.out.println("zero file is named: " + emptyfile2.getAbsolutePath());
    emptyfile2.createNewFile();
    emptyfile2.deleteOnExit();

    // copy an ok file that has exactly 5 entries
    File orig = new File(WAL_OK);
    File ok = new File(logdir, "ok.0000000.20091104-101213997-0800.seq");
    FileUtil.dumbfilecopy(orig, ok);

    // check now, and any age is too old.
    // final WriteAheadLogSource src = new WriteAheadLogSource(tmpdir
    // .getAbsolutePath(), 0, 0);
    NaiveFileWALManager walman = new NaiveFileWALManager(basedir);
    final WALSource src = new WALSource(walman);
    src.open();
    src.recover();

    final AtomicInteger count = new AtomicInteger();
    final AtomicBoolean okstate = new AtomicBoolean(true);

    Thread t = new Thread("poller") {
      public void run() {
        try {
          for (int i = 0; i < 10; i++) {
            // this eventually blocks and never make progress.
            // It will always read the good entries and skip over the bad file.
            src.next();
            count.getAndIncrement();
          }
        } catch (Exception e) {
          e.printStackTrace();
          okstate.set(false);
        }
      }
    };
    t.start();

    // TODO (jon) remove sleeps
    // yeah, I know you don't like sleeps.. getting into the DirWatcher is more
    // trouble than I want right now.
    Clock.sleep(3000);

    src.close();

    Assert.assertTrue(okstate.get()); // no unexpected exceptions
    Assert.assertEquals(5, count.get());

    // After this call okstate will be false becuase IOExcpetion is thrown on
    // close.

  }

  /**
   * In this stuation we intially open a file that starts of ok. However, at
   * some point in runs into an unexpected end of file (due to a program /
   * machine/ write failure).
   * 
   * We want this to send all entries it can, fire some event with the bad wal
   * file, and the continue on with other ok files.
   */
  @Test
  public void testSurviveCorruptFile() throws IOException, InterruptedException {

    System.out.println("Survive zero file with walsource");
    File basedir = FileUtil.mktempdir();
    basedir.deleteOnExit();
    File logdir = new File(basedir, NaiveFileWALManager.LOGGEDDIR);
    logdir.mkdirs();

    // create empty file.
    File corrupt = new File(logdir,
        "walcorrupt.0000000.20091104-101213997-0800.seq");
    System.out.println("corrupt file is named: " + corrupt.getAbsolutePath());
    FileUtil.dumbfilecopy(new File(WAL_CORRUPT), corrupt);
    corrupt.deleteOnExit();

    // check now, and any age is too old.
    NaiveFileWALManager walman = new NaiveFileWALManager(basedir);
    final WALSource src = new WALSource(walman);

    src.open();
    src.recover();

    final AtomicInteger count = new AtomicInteger();
    final AtomicBoolean okstate = new AtomicBoolean(true);

    Thread t = new Thread() {
      public void run() {
        try {
          for (int i = 0; true; i++) {
            // this eventually blocks and never make progress.
            // It will always read the good entries and skip over the bad file.
            src.next();
            count.getAndIncrement();
          }
        } catch (Exception e) {
          e.printStackTrace();
          okstate.set(false);
        }
      }
    };
    t.start();

    // TODO (jon) remove sleeps
    // yeah, I know you don't like sleeps.. getting into the DirWatcher is more
    // trouble than I want right now.
    Clock.sleep(3000);

    src.close();
    System.out.println("Outputted " + count.get() + " events");

    Assert.assertTrue(okstate.get()); // no unexpected exceptions
    Assert.assertEquals(3, count.get());
  }

}
