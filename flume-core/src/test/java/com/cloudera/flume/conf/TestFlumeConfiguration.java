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
package com.cloudera.flume.conf;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestFlumeConfiguration {
  /**
   * This class exposes a constructor so that we can instantiate new
   * FlumeConfiguration objects.
   */
  public class TestableConfiguration extends FlumeConfiguration {
    public TestableConfiguration() {
      super(true);
    }
  }
  
  @Test
  public void testPluginClassesWithWhiteSpaceAtBeginning() {
	  FlumeConfiguration cfg = new TestableConfiguration();
	  cfg.set(FlumeConfiguration.PLUGIN_CLASSES, " \n a.b.c,d.e.f");
	  
	  assertEquals("White spaces at the beginning must be stripped", "a.b.c,d.e.f", cfg.getPluginClasses());
  }
  
  @Test
  public void testPluginClassesWithWhiteSpaceAtEnd() {
	  FlumeConfiguration cfg = new TestableConfiguration();
	  cfg.set(FlumeConfiguration.PLUGIN_CLASSES, "a.b.c,d.e.f \n ");
	  
	  assertEquals("White spaces at the end must be stripped", "a.b.c,d.e.f", cfg.getPluginClasses());
  }
  
  @Test
  public void testPluginClassesWithWhiteSpaceInTheMiddle() {
	  FlumeConfiguration cfg = new TestableConfiguration();
	  cfg.set(FlumeConfiguration.PLUGIN_CLASSES, "a.b.c,\nd.e.f,   g.h.i");
	  
	  assertEquals("White spaces in the middle must be stripped", "a.b.c,d.e.f,g.h.i", cfg.getPluginClasses());
  }
  
  @Test
  public void testPluginClassesWithWhiteSpace() {
	  FlumeConfiguration cfg = new TestableConfiguration();
	  cfg.set(FlumeConfiguration.PLUGIN_CLASSES, " \n a.b.c,\nd.e.f,   g.h.i \n ");
	  
	  assertEquals("White spaces must be stripped", "a.b.c,d.e.f,g.h.i", cfg.getPluginClasses());
  }

  @Test
  public void testParseGossipServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA,hostB,hostC");
    cfg.setInt(FlumeConfiguration.MASTER_GOSSIP_PORT, 57890);

    String gossipServers = cfg.getMasterGossipServers();

    assertEquals("hostA:57890,hostB:57890,hostC:57890", gossipServers);

    assertEquals(57890, cfg.getMasterGossipPort());

    // test with spaces
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA , hostB ,     hostC");

    assertEquals("hostA:57890,hostB:57890,hostC:57890", gossipServers);

    assertEquals(57890, cfg.getMasterGossipPort());
  }

  @Test
  public void testOverrideGossipServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_GOSSIP_SERVERS,
        "hostA:57891,hostB:57892,hostC:57893");
    cfg.setInt(FlumeConfiguration.MASTER_SERVER_ID, 1);

    assertEquals(57892, cfg.getMasterGossipPort());

    // try with spaces in list
    cfg.set(FlumeConfiguration.MASTER_GOSSIP_SERVERS,
        "hostA:57891 ,  hostB:57892 , hostC:57893");
    cfg.setInt(FlumeConfiguration.MASTER_SERVER_ID, 1);

    assertEquals(57892, cfg.getMasterGossipPort());
  }

  @Test
  public void testParseZKServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA,hostB,hostC");
    cfg.setInt(FlumeConfiguration.MASTER_ZK_CLIENT_PORT, 4181);
    cfg.setInt(FlumeConfiguration.MASTER_ZK_SERVER_QUORUM_PORT, 4182);
    cfg.setInt(FlumeConfiguration.MASTER_ZK_SERVER_ELECTION_PORT, 4183);

    String gossipServers = cfg.getMasterZKServers();

    assertEquals("hostA:4181:4182:4183,hostB:4181:4182:4183,hostC:4181:4182:4183",
        gossipServers);

    assertEquals(4181, cfg.getMasterZKClientPort());
    assertEquals(4182, cfg.getMasterZKServerQuorumPort());
    assertEquals(4183, cfg.getMasterZKServerElectionPort());

    // try with arbitrary spaces
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "   hostA , hostB ,    hostC");
    assertEquals("hostA:4181:4182:4183,hostB:4181:4182:4183,hostC:4181:4182:4183",
        gossipServers);

  }

  @Test
  public void testOverrideZKServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_ZK_SERVERS,
        "hostA:1234:2345:3456,hostB:1235:2346:3457,hostC:1236:2347:3458");
    cfg.setInt(FlumeConfiguration.MASTER_SERVER_ID, 1);

    assertEquals(1235, cfg.getMasterZKClientPort());
    assertEquals(2346, cfg.getMasterZKServerQuorumPort());
    assertEquals(3457, cfg.getMasterZKServerElectionPort());

    // try with spaces
    cfg.set(FlumeConfiguration.MASTER_ZK_SERVERS,
        "  hostA: 1234:2345:3456   ,hostB: 1235:   2346 : 3457 ,  hostC:1236:2347:3458");
    assertEquals(1235, cfg.getMasterZKClientPort());
    assertEquals(2346, cfg.getMasterZKServerQuorumPort());
    assertEquals(3457, cfg.getMasterZKServerElectionPort());

    // overriding settings
    cfg.setInt(FlumeConfiguration.MASTER_ZK_CLIENT_PORT, 9999);
    cfg.setInt(FlumeConfiguration.MASTER_ZK_SERVER_QUORUM_PORT, 9998);
    cfg.setInt(FlumeConfiguration.MASTER_ZK_SERVER_ELECTION_PORT, 9997);

    assertEquals(9999, cfg.getMasterZKClientPort());
    assertEquals(9998, cfg.getMasterZKServerQuorumPort());
    assertEquals(9997, cfg.getMasterZKServerElectionPort());

  }

  @Test
  public void testParseHeartbeatServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA,hostB,hostC");
    cfg.setInt(FlumeConfiguration.MASTER_HEARTBEAT_PORT, 65432);

    String heartbeatServers = cfg.getMasterHeartbeatServers();

    assertEquals("hostA:65432,hostB:65432,hostC:65432", heartbeatServers);

    assertEquals(65432, cfg.getMasterHeartbeatPort());
  }

  @Test
  public void testParseSingleHeartbeatServer() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA.foo.com");
    cfg.setInt(FlumeConfiguration.MASTER_HEARTBEAT_PORT, 65432);

    String heartbeatServers = cfg.getMasterHeartbeatServers();

    assertEquals("hostA.foo.com:65432", heartbeatServers);

    assertEquals(65432, cfg.getMasterHeartbeatPort());
  }

  @Test
  public void testOverrideHeartbeatServers() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_HEARTBEAT_SERVERS,
        "hostA:57891,hostB:57895,hostC:57893");
    cfg.setInt(FlumeConfiguration.MASTER_SERVER_ID, 1);

    assertEquals(57895, cfg.getMasterHeartbeatPort());

    // try with spaces
    cfg.set(FlumeConfiguration.MASTER_HEARTBEAT_SERVERS,
        "hostA : 57891 ,    hostB: 57895,   hostC:57893");
    assertEquals(57895, cfg.getMasterHeartbeatPort());
  }

  @Test
  public void testMasterIsDistributed() {
    FlumeConfiguration cfg = new TestableConfiguration();
    assertEquals(false, cfg.getMasterIsDistributed());
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "hostA,hostB");
    assertEquals(true, cfg.getMasterIsDistributed());
  }

  /**
   * Master servers with ':'s should be fixed up to use default ports instead of
   */
  @Test
  public void testInvalidMasterServersFixup() {
    FlumeConfiguration cfg = new TestableConfiguration();
    cfg.set(FlumeConfiguration.MASTER_SERVERS, "foo:12345,bar:1345");
    String zksvrs = cfg.getMasterZKServers();
    assertNotSame("foo:12345:2181:3181,bar:1345:2181:3181", zksvrs);
    assertEquals("foo:3181:3182:3183,bar:3181:3182:3183", zksvrs);
  }

  @Test
  public void testNewFlumeConfDir() {
    String curConfDirProp = System.getProperty("flume.conf.dir");
    try {
      System.setProperty("flume.conf.dir", "/a/path/that/cannot/exist");
      FlumeConfiguration cfg = FlumeConfiguration.get();
      assertNull(cfg.get(FlumeConfiguration.NODE_STATUS_PORT));
    } finally {
      // Undo this on the way out.
      if (null == curConfDirProp) {
        System.clearProperty("flume.conf.dir");
      } else {
        System.setProperty("flume.conf.dir", curConfDirProp);
      }
    }
  }
}
