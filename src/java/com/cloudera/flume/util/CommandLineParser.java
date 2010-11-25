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

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class for taking an array of strings and parsing out {@link Option}s from them. The contents of each
 * string should be in key=value format. Anything that is not in said format will result in an
 * {@link IllegalArgumentException} being thrown.
 * <p/>
 * Below are some examples of valid command lines and how they would be parsed
 * <ul>
 * <li>host=10.234.32.10 -> Option(host, "10.234.32.10")</li>
 * <li>port=1224 ->  Option(port, "1224")</li>
 * <li>args=foo,bar,baz -> Option(args, "foo,bar,baz")</li>
 * <li>enabled=false ->  Option(enabled, "false")</li>
 * <ul>
 * <p/>
 * The actual type conversion of the parsed out options is done through the different methods on
 * the {@link com.cloudera.flume.util.Option} itself, or through method on the {@link CommandLineParser}.
 * See {@link #getOptionValue(String)}, {@link #getOptionValue(String, int)}, etc. for usage.
 *
 * @see com.cloudera.flume.util.Option
 */
public class CommandLineParser {

  private final Map<String, Option> options;

  /**
   * Creates and parses out the args specified into a map of {@link Option}s.
   *
   * @param args Strings to parse
   * @throws IllegalArgumentException thrown if any of the strings are not in the valid format
   */
  public CommandLineParser(String... args) throws IllegalArgumentException {
    Preconditions.checkArgument(args != null);
    this.options = parse(args);
  }

  /**
   * Returns the number of {@link Option}s this parser contains
   *
   * @return number of options
   */
  public int getNumberOfOptions() {
    return options != null ? options.size() : 0;
  }

  /**
   * Parses each of the specified args as key=value pairs. This method will create an
   * {@link Option} for each and add it to the map that this method returns.
   *
   * @param args array of strings that contain key=value pairs
   * @return map of {@link Option}s where the key is the {@link Option#name} and the option is
   *         the value
   * @throws IllegalArgumentException thrown if any of the strings are not in the valid format, i.e.
   *                                  key=value
   */
  Map<String, Option> parse(String... args) throws IllegalArgumentException {
    Map<String, Option> options = new HashMap<String, Option>();

    for (String argument : args) {
      int indexOfEqualSign = argument.indexOf('=');

      if (indexOfEqualSign > -1) {
        String name = argument.substring(0, indexOfEqualSign);
        String value = null;

        if (indexOfEqualSign + 1 < argument.length()) {
          value = argument.substring(indexOfEqualSign + 1, argument.length());
        }

        options.put(name, new Option(name, value));
      } else {
        throw new IllegalArgumentException(String.format("'%s' is not in the correct format. " +
            "Please ensure that the parameter is in key=value format.", argument));
      }
    }

    return options;
  }

  /**
   * Returns the {@link Option} for the specified name or null if one is not found
   *
   * @param name {@link Option#name} of option to get
   * @return option or null if not found
   */
  public Option getOption(String name) {
    return options.get(name);
  }

  /**
   * Returns the {@link Option#stringValue()} for the option with the specified name or null
   * if one is not found.
   *
   * @param name {@link Option#name} of option to get
   * @return string value of option or null if not found
   */
  public String getOptionValue(String name) {
    String value = null;
    if (options.containsKey(name)) {
      value = options.get(name).stringValue();
    }

    return value;
  }

  /**
   * Returns the {@link Option#stringValue()} for the option with the specified name or defaultValue
   * if one is not found.
   *
   * @param name         {@link Option#name} of option to get
   * @param defaultValue value to return if option is not found
   * @return string value of option or defaultValue if not found
   */
  public String getOptionValue(String name, String defaultValue) {
    String value = defaultValue;
    if (options.containsKey(name)) {
      value = options.get(name).stringValue();

      if (value == null) {
        value = defaultValue;
      }
    }

    return value;
  }

  /**
   * Returns the {@link Option#stringValues()} for the option with the specified name or null
   * if one is not found.
   *
   * @param name {@link Option#name} of option to get
   * @return array of strings for the option or null if not found
   */
  public String[] getOptionValues(String name) {
    String[] values = null;

    if (options.containsKey(name)) {
      values = options.get(name).stringValues();
    }

    return values;
  }

  /**
   * Returns the {@link Option#stringValues()} for the option with the specified name or a single
   * element array containing the defaultValue  if one is not found.
   *
   * @param name         {@link Option#name} of option to get
   * @param defaultValue value to use if option is not found
   * @return array of strings for the option or single element array with defaultValue
   */
  public String[] getOptionValues(String name, String defaultValue) {
    String[] values = {defaultValue};

    if (options.containsKey(name)) {
      values = options.get(name).stringValues();

      if (values == null) {
        values = new String[]{defaultValue};
      }
    }

    return values;
  }

  /**
   * Returns the {@link Option#booleanValue()} for the option with the specified name or defaultValue
   * if one is not found.
   *
   * @param name         {@link Option#name} of option to get
   * @param defaultValue value to return if option is not found
   * @return boolean value of option or defaultValue if not found
   */
  public boolean getOptionValue(String name, boolean defaultValue) {
    Boolean value = defaultValue;
    if (options.containsKey(name)) {
      value = options.get(name).booleanValue();

      if (value == null) {
        value = defaultValue;
      }
    }

    return value;
  }

  /**
   * Returns the {@link Option#integerValue()} for the option with the specified name or defaultValue
   * if one is not found.
   *
   * @param name         {@link Option#name} of option to get
   * @param defaultValue value to return if option is not found
   * @return integer value of option or defaultValue if not found
   */
  public int getOptionValue(String name, int defaultValue) {
    Integer value = defaultValue;
    if (options.containsKey(name)) {
      value = options.get(name).integerValue();

      // null may be allowed
      if (value == null) {
        value = defaultValue;
      }
    }

    return value;
  }
}
