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

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.apache.hadoop.io;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Metadata;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.util.Progressable;

/**
 * A {@link SequenceFile} {@link Writer} that doesn't create checksum files
 */
@SuppressWarnings("rawtypes")
public class RawSequenceFileWriter extends SequenceFile.Writer {

  public static RawSequenceFileWriter createWriter(FileSystem fs,
      Configuration conf, Path name, Class keyClass, Class valClass,
      CompressionType compressionType) throws IOException {

    return new RawSequenceFileWriter(fs, conf, name, keyClass, valClass,
        fs.getConf().getInt("io.file.buffer.size", 65536),
        fs.getDefaultReplication(), fs.getDefaultBlockSize(), null,
        new Metadata());
  }

  public RawSequenceFileWriter(FileSystem fs, Configuration conf, Path name,
      Class keyClass, Class valClass, int bufferSize, short replication,
      long blockSize, Progressable progress, Metadata metadata)
      throws IOException {

    Path parent = name.getParent();
    if (parent != null && !fs.mkdirs(parent)) {
      throw new IOException("Mkdirs failed to create " + parent);
    }

    FSDataOutputStream out = ((LocalFileSystem) fs).getRawFileSystem().create(
        name, true, bufferSize, replication, blockSize, progress);

    init(name, conf, out, keyClass, valClass, false, null, metadata);

    initializeFileHeader();
    writeFileHeader();
    finalizeFileHeader();
  }

}
