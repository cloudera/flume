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
package com.cloudera.flume.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestCommandLineParser {

  @Test
  public void testParseValidInput() {
    CommandLineParser parser = new CommandLineParser("foo=bar", "name=dave", "boolean=true");


    assertEquals(3, parser.getNumberOfOptions());
    assertEquals("bar", parser.getOptionValue("foo"));
    assertEquals("dave", parser.getOptionValue("name"));
    assertEquals("true", parser.getOptionValue("boolean"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseInvalidInput() {
    // this should bomb because boolean is missing the equal sign
    new CommandLineParser("foo=bar", "name=dave", "boolean");
  }

  @Test
  public void testParseMissingValueAndDefaults() {
    CommandLineParser parser = new CommandLineParser("string=", "number=", "boolean=");

    assertEquals(3, parser.getNumberOfOptions());
    assertEquals(null, parser.getOptionValue("string"));
    assertEquals(null, parser.getOptionValue("number"));
    assertEquals(null, parser.getOptionValue("boolean"));
    // test the default
    assertEquals("default", parser.getOptionValue("string", "default"));
    assertEquals(123, parser.getOptionValue("number", 123));
    assertEquals(true, parser.getOptionValue("boolean", true));
  }

  @Test
  public void testDataTypeConversion() {
    CommandLineParser parser = new CommandLineParser("number=123", "string=dave", "boolean=true");

    assertEquals(3, parser.getNumberOfOptions());

    Option opt = parser.getOption("number");
    assertNotNull(opt);
    assertEquals(new Integer(123), opt.integerValue());

    opt = parser.getOption("string");
    assertNotNull(opt);
    assertEquals("dave", opt.stringValue());

    opt = parser.getOption("boolean");
    assertNotNull(opt);
    assertEquals(Boolean.TRUE, opt.booleanValue());
  }

  @Test
  public void testMultipleValues() {
    CommandLineParser parser = new CommandLineParser("bindings=x,y,z");

    // ensure parsed a single option
    assertEquals(1, parser.getNumberOfOptions());
    assertNotNull(parser.getOptionValues("bindings"));

    String[] values = parser.getOptionValues("bindings");
    assertEquals(3, values.length);
    assertEquals("x", values[0]);
    assertEquals("y", values[1]);
    assertEquals("z", values[2]);
  }
}

