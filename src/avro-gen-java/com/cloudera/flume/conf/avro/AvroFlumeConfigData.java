/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.cloudera.flume.conf.avro;

@SuppressWarnings("all")
public class AvroFlumeConfigData extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"AvroFlumeConfigData\",\"namespace\":\"com.cloudera.flume.conf.avro\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"sourceConfig\",\"type\":\"string\"},{\"name\":\"sinkConfig\",\"type\":\"string\"},{\"name\":\"sourceVersion\",\"type\":\"long\"},{\"name\":\"sinkVersion\",\"type\":\"long\"},{\"name\":\"flowID\",\"type\":\"string\"}]}");
  public long timestamp;
  public java.lang.CharSequence sourceConfig;
  public java.lang.CharSequence sinkConfig;
  public long sourceVersion;
  public long sinkVersion;
  public java.lang.CharSequence flowID;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return timestamp;
    case 1: return sourceConfig;
    case 2: return sinkConfig;
    case 3: return sourceVersion;
    case 4: return sinkVersion;
    case 5: return flowID;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: timestamp = (java.lang.Long)value$; break;
    case 1: sourceConfig = (java.lang.CharSequence)value$; break;
    case 2: sinkConfig = (java.lang.CharSequence)value$; break;
    case 3: sourceVersion = (java.lang.Long)value$; break;
    case 4: sinkVersion = (java.lang.Long)value$; break;
    case 5: flowID = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}
