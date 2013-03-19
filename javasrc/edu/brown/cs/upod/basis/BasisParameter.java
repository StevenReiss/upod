/********************************************************************************/
/*                                                                              */
/*              BasisParameter.java                                             */
/*                                                                              */
/*      Basic parameter description class                                       */
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



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import java.util.Date;




public abstract class BasisParameter implements BasisConstants, UpodParameter
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String parameter_name;



/********************************************************************************/
/*                                                                              */
/*      Creation Methods                                                        */
/*                                                                              */
/********************************************************************************/

public UpodParameter createParameter(String name,ParameterType typ)
{
   switch (typ) {
      case BOOLEAN :
         return new BooleanParameter(name);
    }
   
   return createParameter(name,typ,null);
}

public UpodParameter createIntParameter(String name,int from,int to)
{
   return new IntParameter(name,from,to);
}

public UpodParameter createRealParameter(String name,double from,double to)
{
   return new RealParameter(name,from,to);
}


public UpodParameter createTimeParameter(String name,ParameterType typ,long from,long to)
{
   switch (typ) {
      case TIME :
      case DATE :
      case DATETIME :
         return new TimeParameter(name,typ,from,to);
      case TIME_RANGE :
      case DATE_RANGE :
      case DATETIME_RANGE :
         return new TimeRangeParameter(name,typ,from,to);
    }
   
   return null;
}

public UpodParameter createEnumParameter(String name,Enum<?> e)
{
   return new EnumParameter(name,e);
}



public UpodParameter createParameter(String name,ParameterType typ,String pattern)
{
   switch (typ) {
      case STRING :
         return new StringParameter(name,pattern);
    }
   
   return null;
}


protected BasisParameter(String name)
{ 
   parameter_name = name;
}



/********************************************************************************/
/*                                                                              */
/*      Default methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public double getMinValue()                   { return 0; }
@Override public double getMaxValue()                   { return 0; }

@Override public String getName()                       { return parameter_name; }



/********************************************************************************/
/*                                                                              */
/*      String parameters                                                       */
/*                                                                              */
/********************************************************************************/

private static class StringParameter extends BasisParameter {
   
   private String string_pattern;
   
   StringParameter(String name,String pat) {
      super(name);
      string_pattern = pat;
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.STRING;
    }
   
   @Override public boolean isValid(Object value) {
      if (value == null) return false;
      String sv = value.toString();
      if (string_pattern == null) return true;
      return sv.matches(string_pattern);
    }
   
}       // end of inner class StringParameter



/********************************************************************************/
/*                                                                              */
/*      Boolean parameters                                                      */
/*                                                                              */
/********************************************************************************/

private static class BooleanParameter extends BasisParameter {
   
   BooleanParameter(String name) {
      super(name);
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.BOOLEAN;
    }
   
   @Override public boolean isValid(Object value) {
      if (value == null) return false;
      if (value instanceof Boolean) return true;
      return false;
    }
   
}       // end of inner class BooleanParameter



/********************************************************************************/
/*                                                                              */
/*      Numeric Parameters                                                      */
/*                                                                              */
/********************************************************************************/

private static class IntParameter extends BasisParameter {

   private int min_value;
   private int max_value;
   
   IntParameter(String name,int min,int max) {
      super(name);
      min_value = min;
      max_value = max;
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.INTEGER;
    }
   
   @Override public double getMinValue()                { return min_value; }
   @Override public double getMaxValue()                { return max_value; }
   
   @Override public boolean isValid(Object value) {
      if (value == null) return false;
      if (value instanceof Integer) {
         int v = ((Integer) value);
         if (v >= min_value && v <= max_value) return true;
       }
      return false;
    }
   
}       // end of inner class IntParameter



/********************************************************************************/
/*                                                                              */
/*      Real Parameter                                                          */
/*                                                                              */
/********************************************************************************/

private static class RealParameter extends BasisParameter {
   
   private double min_value;
   private double max_value;
   
   RealParameter(String name,double min,double max) {
      super(name);
      min_value = min;
      max_value = max;
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.REAL;
    }
   
   @Override public double getMinValue()                { return min_value; }
   @Override public double getMaxValue()                { return max_value; }
   
   @Override public boolean isValid(Object value) {
      if (value == null) return false;
      if (value instanceof Number) {
         double v = ((Number) value).doubleValue();
         if (v >= min_value && v <= max_value) return true;
       }
      return false;
    }
   
}       // end of inner class RealParameter


/********************************************************************************/
/*                                                                              */
/*      Time-based parameters                                                   */
/*                                                                              */
/********************************************************************************/

private static class TimeParameter extends BasisParameter {
   
   private long start_time;
   private long end_time;
   private ParameterType parameter_type;
   
   TimeParameter(String name,ParameterType typ,long from,long to) {
      super(name);
      start_time = from;
      end_time = to;
      parameter_type = typ;
    }
   
   @Override public ParameterType getParameterType()    { return parameter_type; }
   
   @Override public double getMinValue()                { return start_time; }
   @Override public double getMaxValue()                { return end_time; }
   
   @Override public boolean isValid(Object o) {
      if (o instanceof Date) {
         Date d = (Date) o;
         long v = d.getTime();
         if (v >= start_time && v <= end_time) return true;
       }
      return false;
    }
   
}       // end of inner class TimeParameter



/********************************************************************************/
/*                                                                              */
/*      TimeRange parameter                                                     */
/*                                                                              */
/********************************************************************************/



private static class TimeRangeParameter extends BasisParameter {

   private long start_time;
   private long end_time;
   private ParameterType parameter_type;
   
   TimeRangeParameter(String name,ParameterType typ,long from,long to) {
      super(name);
      start_time = from;
      end_time = to;
      parameter_type = typ;
    }
   
   @Override public ParameterType getParameterType()    { return parameter_type; }
   
   @Override public double getMinValue()                { return start_time; }
   @Override public double getMaxValue()                { return end_time; }
   
   @Override public boolean isValid(Object o) {
      return false;
    }
   
}       // end of inner class TimeRangeParameter



/********************************************************************************/
/*                                                                              */
/*      Enum Parameter                                                          */
/*                                                                              */
/********************************************************************************/

private static class EnumParameter extends BasisParameter {
   
   private Enum<?> enum_base;
   
   EnumParameter(String name,Enum<?> e) {
      super(name);
      enum_base = e; 
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.ENUM;
    }
   
   @Override public boolean isValid(Object o) {
      if (o.getClass() == enum_base.getClass()) return true;
      return false;
    }
   
}

}       // end of class BasisParameter




/* end of BasisParameter.java */

