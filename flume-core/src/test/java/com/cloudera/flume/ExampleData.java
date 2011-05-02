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
package com.cloudera.flume;

/**
 * This contains names for data files commited in the test directory for use
 * with unit testing
 */
public interface ExampleData {

  final static String APACHE_REGEXES = "data/apachelog.regex";
  final static String APACHE_SIMPLE_REGEXES = "data/apachelog.simple.regex";

  final static String HADOOP_REGEXES = "data/hadooplogs.regex";
  final static String HADOOP_REGEXES_11 = "data/hadooplogs.more.regex";
  final static String HADOOP_GREP = "data/hadooplogs.grep";

  final static String SYSLOG_LOG = "data/syslog.0";
  final static String TEST = "data/scribe_stats";

}
