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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class for holding parsed out command line key/value pairs. Provides method to return
 * the value as different data types.
 */
public class Option {
  private static final Logger LOG = LoggerFactory.getLogger(Option.class);

  private final String name;
  private final String value;

  /**
   * Creates a new option with the specified name and value
   * @param name name of the option
   * @param value string value of the option
   * @throws IllegalArgumentException thrown if name is null
   */
  public Option(String name, String value) throws IllegalArgumentException {
    Preconditions.checkNotNull(name, "Name cannot be null");
    // value may be null
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the {@link #name} of this option
   * @return name of this option
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the string {@link #value} of this option. Since the raw value is stored as a string also
   * there is no conversion in this method.
   * @return string value for this option
   */
  public String stringValue() {
    return value;
  }

  /**
   * Parses and returns an array of string, or null, for this option. Multi-valued options values
   * are comma-delimited. For example, if {@link #value} is "1,2,3,5", this method will return
   * a 4 element array containing the values {"1", "2", "3", "4"}
   * @return array of string values
   */
  public String[] stringValues() {
    return value != null ? value.split(",") : null;
  }

  /**
   * Returns the Integer representation of {@link #value} if value is not-null and a non-empty
   * string.
   * @return Integer representation of the value or null if value is null or empty
   * @throws NumberFormatException thrown if the value cannot be parsed as an integer
   */
  public Integer integerValue() throws NumberFormatException{
    Integer intValue = null;

    if (value != null && value.trim().length() > 0) {
      try {
        intValue = Integer.parseInt(value);
      } catch (NumberFormatException e) {
        LOG.error(String.format("Option named '%s' value of '%s'  is not a valid integer", name, value), e);
        throw e;
      }
    }
    return intValue;

  }

  /**
   * Returns the boolean value of the {@link #value} field if it is not null. Will return
   * true if value is either "yes" or "true" (case-insensitive), false otherwise.
   * @return true or false, null if {@link #value} is null
   */
  public Boolean booleanValue() {
    if (value == null) {
      return null;
    } else {
      return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }
  }
}
