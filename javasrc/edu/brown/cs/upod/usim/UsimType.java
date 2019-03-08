/********************************************************************************/
/*                                                                              */
/*              UsimType.java                                                   */
/*                                                                              */
/*      Parameter or property type information                                  */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.upod.usim;

import java.util.*;
import java.text.*;


public abstract class UsimType implements UsimConstants
{




/********************************************************************************/
/*                                                                              */
/*      Type creation methods                                                   */
/*                                                                              */
/********************************************************************************/

public synchronized static UsimType getBoolean()
{
   UsimType ut = known_types.get(BOOLEAN_NAME);
   if (ut == null) {
      ut = new TypeBoolean();
    }
   return ut;
}



public synchronized static UsimType getRealRange(String name,double low,double high)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeReal(name,low,high);
    }
   return ut;
}


public synchronized static UsimType getIntRange(String name,int low,int high)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeInteger(name,low,high);
    }
   return ut;
}


public synchronized static UsimType getSetType(String name,Class<Enum<?>> type)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeSet(name,type);
    }
   return ut;
}


public synchronized static UsimType getSetType(String name,Object [] values)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeSet(name,values);
    }
   return ut;
}


public synchronized static UsimType getStringType(String name)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeString(name);
    }
   return ut;
}


public synchronized static UsimType getTimeType(String name)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeTime(name,PropertyType.TIME);
    }
   return ut;
}



public synchronized static UsimType getDateType(String name)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeTime(name,PropertyType.DATE);
    }
   return ut;
}


public synchronized static UsimType getDateTimeType(String name)
{
   UsimType ut = known_types.get(name);
   if (ut == null) {
      ut = new TypeTime(name,PropertyType.DATETIME);
    }
   return ut;
}



/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          type_name;

private static Map<String,UsimType>     known_types;

private static final String     BOOLEAN_NAME = "Boolean";

static {
   known_types = new HashMap<String,UsimType>();
   
   getBoolean();
}

private static final DateFormat [] formats = new DateFormat [] {
   DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG),
   DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT),
   DateFormat.getDateInstance(DateFormat.LONG),
   DateFormat.getDateInstance(DateFormat.SHORT),
   DateFormat.getTimeInstance(DateFormat.LONG),
   DateFormat.getTimeInstance(DateFormat.SHORT),
   new SimpleDateFormat("MM/dd/yyyy hh:mma"),
   new SimpleDateFormat("MM/dd/yyyy HH:mm"),
   new SimpleDateFormat("MM/dd/yyyy"),
   new SimpleDateFormat("h:mma"),
   new SimpleDateFormat("H:mm"),
};


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected UsimType(String name) 
{
   type_name = name;
   
   synchronized (UsimType.class) {
      if (name != null) known_types.put(name,this);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getName()                         { return type_name; }

public abstract PropertyType getPropertyType();

public abstract Object normalize(Object val);

public double getMinValue()                     { return 0; }
public double getMaxValue()                     { return 0; }
public List<Object> getValues()                 { return null; }



/********************************************************************************/
/*                                                                              */
/*      Boolean type implementation                                             */
/*                                                                              */
/********************************************************************************/

private static class TypeBoolean extends UsimType {
   
   TypeBoolean() {
      super("Boolean");
    }
   
   @Override public Object normalize(Object o) {
      if (o != null && o instanceof Boolean) return o;
      boolean bvl = false;
      if (o == null) ;
      else if (o instanceof Number) {
         Number n = (Number) o;
         if (n.doubleValue() != 0) bvl = true;
       }
      else if (o instanceof String) {
         String s = (String) o;
         if (s.trim().equals("")) bvl = false;
         if (s.startsWith("t") || s.startsWith("T") || s.startsWith("1") ||
               s.startsWith("y") || s.startsWith("Y"))
            bvl = true;
       }
      else bvl = true;
      return Boolean.valueOf(bvl);
    }
   
   @Override public PropertyType getPropertyType() {
      return PropertyType.BOOLEAN;
    }
   
   @Override public List<Object> getValues() {
      List<Object> rslt = new ArrayList<Object>();
      rslt.add(Boolean.FALSE);
      rslt.add(Boolean.TRUE);
      return rslt;
    }
   
}       // end of inner class BooleanType




/********************************************************************************/
/*                                                                              */
/*      Integer type implementation                                             */
/*                                                                              */
/********************************************************************************/

private static class TypeInteger extends UsimType {
   
   private int min_value;
   private int max_value;
   
   TypeInteger(String name,int min,int max) {
      super(name);
      min_value = min;
      max_value = max;
    }
 
   @Override public PropertyType getPropertyType() {
      return PropertyType.INTEGER;
    }
   
   @Override public double getMinValue()                { return min_value; }
   @Override public double getMaxValue()                { return max_value; }
   
   @Override public Object normalize(Object value) {
      if (value == null) return null;
      int ivl = 0;
      if (value instanceof Number) {
         Number n = (Number) value;
         ivl = n.intValue();
       }
      else {
         String s = value.toString();
         try {
            ivl = Integer.parseInt(s);
          }
         catch (NumberFormatException e) { }
       }
      if (ivl < min_value) ivl = min_value;
      if (ivl > max_value) ivl = max_value;
      return Integer.valueOf(ivl);
    }
   
}       // end of inner class TypeInteger



/********************************************************************************/
/*                                                                              */
/*      Real Type Implementation                                                */
/*                                                                              */
/********************************************************************************/

private static class TypeReal extends UsimType {
   
   private double min_value;
   private double max_value;
   
   TypeReal(String name,double min,double max) {
      super(name);
      min_value = min;
      max_value = max;
    }
   
   @Override public PropertyType getPropertyType() {
      return PropertyType.REAL;
    }
   
   @Override public double getMinValue()                { return min_value; }
   @Override public double getMaxValue()                { return max_value; }
   
   @Override public Object normalize(Object value) {
      if (value == null) return null;
      double ivl = 0;
      if (value instanceof Number) {
         Number n = (Number) value;
         ivl = n.doubleValue();
       }
      else {
         String s = value.toString();
         try {
            ivl = Double.parseDouble(s);
          }
         catch (NumberFormatException e) { }
       }
      if (ivl < min_value) ivl = min_value;
      if (ivl > max_value) ivl = max_value;
      return Double.valueOf(ivl);
    }
   
}       // end of inner class TypeReal




/********************************************************************************/
/*                                                                              */
/*      Set value type                                                          */
/*                                                                              */
/********************************************************************************/

private static class TypeSet extends UsimType {
   
   private List<Object> value_set;
   
   TypeSet(String nm,Class<Enum<?>> cls) {
      super(nm);
      value_set = new ArrayList<Object>();
      for (Enum<?> x : cls.getEnumConstants()) {
         value_set.add(x);
       }
    }
   
   TypeSet(String nm,Object... objs) {
      super(nm);
      value_set = new ArrayList<Object>();
      for (Object o : objs) value_set.add(o);
    }
   
   @Override public PropertyType getPropertyType() {
      return PropertyType.SET;
    }
   
   @Override public List<Object> getValues() {
      return new ArrayList<Object>(value_set);
    }
   
   @Override public Object normalize(Object o) {
      if (o == null) return null;
      String s = o.toString();
      for (Object v : value_set) {
         if (v == o || v.toString().equals(s)) return v;
       }
      return null;
    }
   
}       // end of inner class TypeSet




/********************************************************************************/
/*                                                                              */
/*      String type                                                             */
/*                                                                              */
/********************************************************************************/

private static class TypeString extends UsimType {
   
   TypeString(String name) {
      super(name);
    }
   
   @Override public PropertyType getPropertyType() {
      return PropertyType.STRING;
    }
   
   @Override public Object normalize(Object o) {
      if (o == null) return null;
      return o.toString();
    }
 
}       // end of inner class TypeString



/********************************************************************************/
/*                                                                              */
/*      Time Types                                                              */
/*                                                                              */
/********************************************************************************/

private static class TypeTime extends UsimType {

   private PropertyType property_type;
   
   TypeTime(String name,PropertyType typ) {
      super(name);
      property_type = typ;
    }
   
   @Override public PropertyType getPropertyType()    { return property_type; }
   
   @Override public Object normalize(Object o) {
      Calendar c = getCalendarObject(o);
      if (c == null) return null;
      switch (property_type) {
         case TIME :
            c.set(0,0,0);
            break;
         case DATE :
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            break;
       }
      return c;
    }
   
   private Calendar getCalendarObject(Object o) {
      if (o == null) return null;
      if (o instanceof Calendar) return ((Calendar) o);
      Date d = getDateObject(o);
      if (d == null) return null;
      Calendar c = Calendar.getInstance();
      c.setTime(d);
      return c;
    }
   
   private Date getDateObject(Object o) {
      if (o == null) return null;
      if (o instanceof Date) return ((Date) o);
      if (o instanceof Number) {
         Number n = (Number) o;
         long tm = n.longValue();
         return new Date(tm);
       }
      String svl = o.toString();
      if (svl.equals("*") || svl.equals("NOW")) return new Date();
      for (DateFormat df : formats) {
         try {
            Date d = df.parse(svl);
            return d;
          }
         catch (ParseException e) { }
       }
      return null;
    }
   
}       // end of inner class TypeTime



}       // end of interface UsimType




/* end of UsimType.java */

