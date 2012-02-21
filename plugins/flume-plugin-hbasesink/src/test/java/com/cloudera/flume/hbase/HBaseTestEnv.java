// This Cloudera, Inc. source code, including without limit any
// human-readable computer programming code and associated documentation
// (together "Source Code"), contains valuable confidential, proprietary
// and trade secret information of Cloudera and is protected by the laws
// of the U.S. and other countries. Any use of the Source Code, including
// without limit any disclosure or reproduction, without the prior
// written authorization of Cloudera is strictly prohibited.
//
// Copyright (c) 2010 Cloudera, Inc.  All rights reserved.
package com.cloudera.flume.hbase;

import java.io.File;

import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.conf.Configuration;

import com.cloudera.util.FileUtil;

public class HBaseTestEnv extends HBaseTestingUtility {
  private File hbaseTestDir;
  public static Configuration conf;

  public HBaseTestEnv() {
    super();
    conf = getConfiguration();
  }

  public String getName() {
    // TODO replace with actual test name
    return "HBaseTestEnv";
  }

  public void setUp() throws Exception {
    hbaseTestDir = FileUtil.mktempdir();
    startMiniZKCluster();
    startMiniHBaseCluster(1,1);
  }

  public void tearDown() throws Exception {
    shutdownMiniCluster();
    FileUtil.rmr(hbaseTestDir);
  }
}
