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
package com.cloudera.flume.handlers.debug;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.EventSink;
import com.google.common.base.Preconditions;

/**
 * This acts as /dev/null for performance tests.
 */
public class NullSink extends EventSink.Base {

  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String... argv) {
        Preconditions.checkArgument(argv.length == 0, "usage: null");
        return new NullSink();
      }
    };
  }
}
