/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.cloudera.flume.reporter.server;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

public class FlumeReport implements TBase<FlumeReport, FlumeReport._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("FlumeReport");

  private static final TField STRING_METRICS_FIELD_DESC = new TField("stringMetrics", TType.MAP, (short)3);
  private static final TField LONG_METRICS_FIELD_DESC = new TField("longMetrics", TType.MAP, (short)4);
  private static final TField DOUBLE_METRICS_FIELD_DESC = new TField("doubleMetrics", TType.MAP, (short)5);

  public Map<String,String> stringMetrics;
  public Map<String,Long> longMetrics;
  public Map<String,Double> doubleMetrics;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    STRING_METRICS((short)3, "stringMetrics"),
    LONG_METRICS((short)4, "longMetrics"),
    DOUBLE_METRICS((short)5, "doubleMetrics");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 3: // STRING_METRICS
          return STRING_METRICS;
        case 4: // LONG_METRICS
          return LONG_METRICS;
        case 5: // DOUBLE_METRICS
          return DOUBLE_METRICS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.STRING_METRICS, new FieldMetaData("stringMetrics", TFieldRequirementType.DEFAULT, 
        new MapMetaData(TType.MAP, 
            new FieldValueMetaData(TType.STRING), 
            new FieldValueMetaData(TType.STRING))));
    tmpMap.put(_Fields.LONG_METRICS, new FieldMetaData("longMetrics", TFieldRequirementType.DEFAULT, 
        new MapMetaData(TType.MAP, 
            new FieldValueMetaData(TType.STRING), 
            new FieldValueMetaData(TType.I64))));
    tmpMap.put(_Fields.DOUBLE_METRICS, new FieldMetaData("doubleMetrics", TFieldRequirementType.DEFAULT, 
        new MapMetaData(TType.MAP, 
            new FieldValueMetaData(TType.STRING), 
            new FieldValueMetaData(TType.DOUBLE))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(FlumeReport.class, metaDataMap);
  }

  public FlumeReport() {
  }

  public FlumeReport(
    Map<String,String> stringMetrics,
    Map<String,Long> longMetrics,
    Map<String,Double> doubleMetrics)
  {
    this();
    this.stringMetrics = stringMetrics;
    this.longMetrics = longMetrics;
    this.doubleMetrics = doubleMetrics;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public FlumeReport(FlumeReport other) {
    if (other.isSetStringMetrics()) {
      Map<String,String> __this__stringMetrics = new HashMap<String,String>();
      for (Map.Entry<String, String> other_element : other.stringMetrics.entrySet()) {

        String other_element_key = other_element.getKey();
        String other_element_value = other_element.getValue();

        String __this__stringMetrics_copy_key = other_element_key;

        String __this__stringMetrics_copy_value = other_element_value;

        __this__stringMetrics.put(__this__stringMetrics_copy_key, __this__stringMetrics_copy_value);
      }
      this.stringMetrics = __this__stringMetrics;
    }
    if (other.isSetLongMetrics()) {
      Map<String,Long> __this__longMetrics = new HashMap<String,Long>();
      for (Map.Entry<String, Long> other_element : other.longMetrics.entrySet()) {

        String other_element_key = other_element.getKey();
        Long other_element_value = other_element.getValue();

        String __this__longMetrics_copy_key = other_element_key;

        Long __this__longMetrics_copy_value = other_element_value;

        __this__longMetrics.put(__this__longMetrics_copy_key, __this__longMetrics_copy_value);
      }
      this.longMetrics = __this__longMetrics;
    }
    if (other.isSetDoubleMetrics()) {
      Map<String,Double> __this__doubleMetrics = new HashMap<String,Double>();
      for (Map.Entry<String, Double> other_element : other.doubleMetrics.entrySet()) {

        String other_element_key = other_element.getKey();
        Double other_element_value = other_element.getValue();

        String __this__doubleMetrics_copy_key = other_element_key;

        Double __this__doubleMetrics_copy_value = other_element_value;

        __this__doubleMetrics.put(__this__doubleMetrics_copy_key, __this__doubleMetrics_copy_value);
      }
      this.doubleMetrics = __this__doubleMetrics;
    }
  }

  public FlumeReport deepCopy() {
    return new FlumeReport(this);
  }

  @Deprecated
  public FlumeReport clone() {
    return new FlumeReport(this);
  }

  @Override
  public void clear() {
    this.stringMetrics = null;
    this.longMetrics = null;
    this.doubleMetrics = null;
  }

  public int getStringMetricsSize() {
    return (this.stringMetrics == null) ? 0 : this.stringMetrics.size();
  }

  public void putToStringMetrics(String key, String val) {
    if (this.stringMetrics == null) {
      this.stringMetrics = new HashMap<String,String>();
    }
    this.stringMetrics.put(key, val);
  }

  public Map<String,String> getStringMetrics() {
    return this.stringMetrics;
  }

  public FlumeReport setStringMetrics(Map<String,String> stringMetrics) {
    this.stringMetrics = stringMetrics;
    return this;
  }

  public void unsetStringMetrics() {
    this.stringMetrics = null;
  }

  /** Returns true if field stringMetrics is set (has been asigned a value) and false otherwise */
  public boolean isSetStringMetrics() {
    return this.stringMetrics != null;
  }

  public void setStringMetricsIsSet(boolean value) {
    if (!value) {
      this.stringMetrics = null;
    }
  }

  public int getLongMetricsSize() {
    return (this.longMetrics == null) ? 0 : this.longMetrics.size();
  }

  public void putToLongMetrics(String key, long val) {
    if (this.longMetrics == null) {
      this.longMetrics = new HashMap<String,Long>();
    }
    this.longMetrics.put(key, val);
  }

  public Map<String,Long> getLongMetrics() {
    return this.longMetrics;
  }

  public FlumeReport setLongMetrics(Map<String,Long> longMetrics) {
    this.longMetrics = longMetrics;
    return this;
  }

  public void unsetLongMetrics() {
    this.longMetrics = null;
  }

  /** Returns true if field longMetrics is set (has been asigned a value) and false otherwise */
  public boolean isSetLongMetrics() {
    return this.longMetrics != null;
  }

  public void setLongMetricsIsSet(boolean value) {
    if (!value) {
      this.longMetrics = null;
    }
  }

  public int getDoubleMetricsSize() {
    return (this.doubleMetrics == null) ? 0 : this.doubleMetrics.size();
  }

  public void putToDoubleMetrics(String key, double val) {
    if (this.doubleMetrics == null) {
      this.doubleMetrics = new HashMap<String,Double>();
    }
    this.doubleMetrics.put(key, val);
  }

  public Map<String,Double> getDoubleMetrics() {
    return this.doubleMetrics;
  }

  public FlumeReport setDoubleMetrics(Map<String,Double> doubleMetrics) {
    this.doubleMetrics = doubleMetrics;
    return this;
  }

  public void unsetDoubleMetrics() {
    this.doubleMetrics = null;
  }

  /** Returns true if field doubleMetrics is set (has been asigned a value) and false otherwise */
  public boolean isSetDoubleMetrics() {
    return this.doubleMetrics != null;
  }

  public void setDoubleMetricsIsSet(boolean value) {
    if (!value) {
      this.doubleMetrics = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case STRING_METRICS:
      if (value == null) {
        unsetStringMetrics();
      } else {
        setStringMetrics((Map<String,String>)value);
      }
      break;

    case LONG_METRICS:
      if (value == null) {
        unsetLongMetrics();
      } else {
        setLongMetrics((Map<String,Long>)value);
      }
      break;

    case DOUBLE_METRICS:
      if (value == null) {
        unsetDoubleMetrics();
      } else {
        setDoubleMetrics((Map<String,Double>)value);
      }
      break;

    }
  }

  public void setFieldValue(int fieldID, Object value) {
    setFieldValue(_Fields.findByThriftIdOrThrow(fieldID), value);
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case STRING_METRICS:
      return getStringMetrics();

    case LONG_METRICS:
      return getLongMetrics();

    case DOUBLE_METRICS:
      return getDoubleMetrics();

    }
    throw new IllegalStateException();
  }

  public Object getFieldValue(int fieldId) {
    return getFieldValue(_Fields.findByThriftIdOrThrow(fieldId));
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    switch (field) {
    case STRING_METRICS:
      return isSetStringMetrics();
    case LONG_METRICS:
      return isSetLongMetrics();
    case DOUBLE_METRICS:
      return isSetDoubleMetrics();
    }
    throw new IllegalStateException();
  }

  public boolean isSet(int fieldID) {
    return isSet(_Fields.findByThriftIdOrThrow(fieldID));
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof FlumeReport)
      return this.equals((FlumeReport)that);
    return false;
  }

  public boolean equals(FlumeReport that) {
    if (that == null)
      return false;

    boolean this_present_stringMetrics = true && this.isSetStringMetrics();
    boolean that_present_stringMetrics = true && that.isSetStringMetrics();
    if (this_present_stringMetrics || that_present_stringMetrics) {
      if (!(this_present_stringMetrics && that_present_stringMetrics))
        return false;
      if (!this.stringMetrics.equals(that.stringMetrics))
        return false;
    }

    boolean this_present_longMetrics = true && this.isSetLongMetrics();
    boolean that_present_longMetrics = true && that.isSetLongMetrics();
    if (this_present_longMetrics || that_present_longMetrics) {
      if (!(this_present_longMetrics && that_present_longMetrics))
        return false;
      if (!this.longMetrics.equals(that.longMetrics))
        return false;
    }

    boolean this_present_doubleMetrics = true && this.isSetDoubleMetrics();
    boolean that_present_doubleMetrics = true && that.isSetDoubleMetrics();
    if (this_present_doubleMetrics || that_present_doubleMetrics) {
      if (!(this_present_doubleMetrics && that_present_doubleMetrics))
        return false;
      if (!this.doubleMetrics.equals(that.doubleMetrics))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(FlumeReport other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    FlumeReport typedOther = (FlumeReport)other;

    lastComparison = Boolean.valueOf(isSetStringMetrics()).compareTo(typedOther.isSetStringMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStringMetrics()) {      lastComparison = TBaseHelper.compareTo(this.stringMetrics, typedOther.stringMetrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLongMetrics()).compareTo(typedOther.isSetLongMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLongMetrics()) {      lastComparison = TBaseHelper.compareTo(this.longMetrics, typedOther.longMetrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDoubleMetrics()).compareTo(typedOther.isSetDoubleMetrics());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDoubleMetrics()) {      lastComparison = TBaseHelper.compareTo(this.doubleMetrics, typedOther.doubleMetrics);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 3: // STRING_METRICS
          if (field.type == TType.MAP) {
            {
              TMap _map0 = iprot.readMapBegin();
              this.stringMetrics = new HashMap<String,String>(2*_map0.size);
              for (int _i1 = 0; _i1 < _map0.size; ++_i1)
              {
                String _key2;
                String _val3;
                _key2 = iprot.readString();
                _val3 = iprot.readString();
                this.stringMetrics.put(_key2, _val3);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: // LONG_METRICS
          if (field.type == TType.MAP) {
            {
              TMap _map4 = iprot.readMapBegin();
              this.longMetrics = new HashMap<String,Long>(2*_map4.size);
              for (int _i5 = 0; _i5 < _map4.size; ++_i5)
              {
                String _key6;
                long _val7;
                _key6 = iprot.readString();
                _val7 = iprot.readI64();
                this.longMetrics.put(_key6, _val7);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 5: // DOUBLE_METRICS
          if (field.type == TType.MAP) {
            {
              TMap _map8 = iprot.readMapBegin();
              this.doubleMetrics = new HashMap<String,Double>(2*_map8.size);
              for (int _i9 = 0; _i9 < _map8.size; ++_i9)
              {
                String _key10;
                double _val11;
                _key10 = iprot.readString();
                _val11 = iprot.readDouble();
                this.doubleMetrics.put(_key10, _val11);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.stringMetrics != null) {
      oprot.writeFieldBegin(STRING_METRICS_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRING, TType.STRING, this.stringMetrics.size()));
        for (Map.Entry<String, String> _iter12 : this.stringMetrics.entrySet())
        {
          oprot.writeString(_iter12.getKey());
          oprot.writeString(_iter12.getValue());
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.longMetrics != null) {
      oprot.writeFieldBegin(LONG_METRICS_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRING, TType.I64, this.longMetrics.size()));
        for (Map.Entry<String, Long> _iter13 : this.longMetrics.entrySet())
        {
          oprot.writeString(_iter13.getKey());
          oprot.writeI64(_iter13.getValue());
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.doubleMetrics != null) {
      oprot.writeFieldBegin(DOUBLE_METRICS_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRING, TType.DOUBLE, this.doubleMetrics.size()));
        for (Map.Entry<String, Double> _iter14 : this.doubleMetrics.entrySet())
        {
          oprot.writeString(_iter14.getKey());
          oprot.writeDouble(_iter14.getValue());
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("FlumeReport(");
    boolean first = true;

    sb.append("stringMetrics:");
    if (this.stringMetrics == null) {
      sb.append("null");
    } else {
      sb.append(this.stringMetrics);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("longMetrics:");
    if (this.longMetrics == null) {
      sb.append("null");
    } else {
      sb.append(this.longMetrics);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("doubleMetrics:");
    if (this.doubleMetrics == null) {
      sb.append("null");
    } else {
      sb.append(this.doubleMetrics);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}

