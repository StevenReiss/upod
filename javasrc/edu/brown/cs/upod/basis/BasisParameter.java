/********************************************************************************/
/*										*/
/*		BasisParameter.java						*/
/*										*/
/*	Basic parameter description class					*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.swing.SwingColorSet;
import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.Element;
import org.json.*;

import java.text.*;
import java.util.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;


public abstract class BasisParameter implements BasisConstants, UpodParameter
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String parameter_name;
private String parameter_label;
private String parameter_description;
private boolean is_sensor;
private boolean is_target;
private Boolean is_continuous;

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
   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
};




/********************************************************************************/
/*										*/
/*	Creation Methods							*/
/*										*/
/********************************************************************************/

public static BasisParameter createParameter(Element xml)
{
   String cnm = IvyXml.getAttrString(xml,"CLASS");
   try {
      Class<?> cls = Class.forName(cnm);
      Constructor<?> cnst = cls.getConstructor(Element.class);
      Object o = cnst.newInstance(xml);
      return (BasisParameter) o;
    }
   catch (Exception e) {
      BasisLogger.logE("Problem creating parameter",e);
    }

   return null;
}




public static BasisParameter createStringParameter(String name)
{
   return new StringParameter(name);
}

public static BasisParameter createBooleanParameter(String name)
{
   return new BooleanParameter(name);
}


public static BasisParameter createIntParameter(String name,int from,int to)
{
   return new IntParameter(name,from,to);
}


public static BasisParameter createIntParameter(String name)
{
   return new IntParameter(name);
}

public static BasisParameter createRealParameter(String name,double from,double to)
{
   return new RealParameter(name,from,to);
}

public static BasisParameter
createRealParameter(String name) 
{
   return new RealParameter(name);
}


public static BasisParameter createTimeParameter(String name,ParameterType typ)
{
   return new TimeParameter(name,typ);
}



public static BasisParameter createPictureParameter(String name)
{
   return new PictureParameter(name);
}

public static BasisParameter createEnumParameter(String name,Enum<?> e)
{
   return new SetParameter(name,e);
}


public static BasisParameter createEnumParameter(String name,Iterable<String> vals)
{
   return new SetParameter(name,vals);
}

public static BasisParameter createEnumParameter(String name,String [] vals)
{
   return new SetParameter(name,vals);
}
      


public static BasisParameter createJSONParameter(String name)
{
   return new JSONParameter(name);
}

public static BasisParameter createMapParameter(String name)
{
   return new MapParameter(name);
}


public static BasisParameter createColorParameter(String name)
{
   return new ColorParameter(name);
}


public static BasisParameter createLocationParameter(String name)
{
   return new LocationParameter(name);
}


/********************************************************************************/
/*                                                                              */
/*      Basic conversion routines                                               */
/*                                                                              */
/********************************************************************************/

public static Calendar createCalendar(Object o)
{
   if (o == null) return null;
   if (o instanceof Calendar) return ((Calendar) o);
   Date d = createDate(o);
   if (d == null) return null;
   Calendar c = Calendar.getInstance();
   c.setTime(d);
   return c;
}



public static Date createDate(Object o) {
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



public static Point2D createLocation(Object value) {
   if (value == null) return null;
   if (value instanceof Point2D) {
      return (Point2D) value;
    }
   else {
      String cnts = value.toString();
      StringTokenizer tok = new StringTokenizer(cnts," ,;");
      if (tok.countTokens() != 2) return null;
      String lat = tok.nextToken();
      String lng = tok.nextToken();
      try {
         return new Point2D.Double(Double.parseDouble(lat),Double.parseDouble(lng));
       }
      catch (NumberFormatException e) {
         return null;
       }
    }
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisParameter(String name)
{
   parameter_name = name;
   parameter_description = name;
   parameter_label = null;
   is_sensor = false;
   is_target = false;
   is_continuous = null;
}

protected BasisParameter(Element xml)
{
   parameter_name = IvyXml.getAttrString(xml,"NAME");
   parameter_description = IvyXml.getAttrString(xml,"DESC");
   parameter_label = IvyXml.getAttrString(xml,"LABEL");
   is_sensor = IvyXml.getAttrBool(xml,"SENSOR");
   is_target = IvyXml.getAttrBool(xml,"TARGET");
}




/********************************************************************************/
/*										*/
/*	Default Access Methods							*/
/*										*/
/********************************************************************************/

@Override public String getName()		{ return parameter_name; }

@Override public String getDescription()	{ return parameter_description; }

public void setDescription(String d)		{ parameter_description = d; }

@Override public String getLabel()
{
   if (parameter_label != null) return parameter_label;
   return getName();
}

public void setLabel(String l)			{ parameter_label = l; }

@Override public boolean isSensor()		{ return is_sensor; }

public void setIsSensor(boolean fg)		{ is_sensor = fg; }

@Override public boolean isTarget()		{ return is_target; }

public void setIsTarget(boolean fg)		{ is_target = fg; }

public void setIsContinuous(boolean fg) 	{ is_continuous = fg; }

@Override public boolean isContinuous()
{
   if (is_continuous != null) return is_continuous;

   if (isSensor()) return false;
   if (isTarget()) return true;
   return false;
}



/********************************************************************************/
/*										*/
/*	Value methods								*/
/*										*/
/********************************************************************************/

@Override public double getMinValue()			{ return 0; }
@Override public double getMaxValue()			{ return 0; }
@Override public List<Object> getValues()		{ return null; }

@Override public Object normalize(Object v)		{ return v; }

@Override public String unnormalize(Object v)
{
   v = normalize(v);
   if (v == null) return null;
   
   return externalString(v);
}


protected String externalString(Object v)
{
   return v.toString();
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw,Object val)
{
   xw.begin("PARAMETER");
   xw.field("TYPE",getParameterType());
   xw.field("NAME",getName());
   xw.field("LABEL",getLabel());
   xw.field("DESC",getDescription());
   xw.field("CLASS",getClass().getName());
   xw.field("ISSENSOR",isSensor());
   xw.field("ISTARGET",isTarget());

   outputLocalXml(xw);

   List<Object> vals = getValues();
   if (vals != null) {
      for (Object v : vals) {
	 xw.textElement("VALUE",v);
       }
    }

   if (val != null) {
      xw.textElement("CURRENT",unnormalize(val));
    }

   xw.end("PARAMETER");
}


protected void outputLocalXml(IvyXmlWriter xw)		{ }


@Override public String toString()
{
   return getName();
}





/********************************************************************************/
/*										*/
/*	String parameters							*/
/*										*/
/********************************************************************************/

private static class StringParameter extends BasisParameter {

   StringParameter(String name) {
      super(name);
    }

   public StringParameter(Element xml) {
      super(xml);
    }

   @Override public Object normalize(Object o) {
      if (o == null) return null;
      return o.toString();
    }

   @Override public ParameterType getParameterType() {
      return ParameterType.STRING;
    }

}	// end of inner class StringParameter




/********************************************************************************/
/*										*/
/*	Boolean parameters							*/
/*										*/
/********************************************************************************/

private static class BooleanParameter extends BasisParameter {

   BooleanParameter(String name) {
      super(name);
    }

   public BooleanParameter(Element xml) {
      super(xml);
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

   @Override public ParameterType getParameterType() {
      return ParameterType.BOOLEAN;
    }

   @Override public List<Object> getValues() {
      List<Object> rslt = new ArrayList<Object>();
      rslt.add(Boolean.FALSE);
      rslt.add(Boolean.TRUE);
      return rslt;
    }

}	// end of inner class BooleanParameter



/********************************************************************************/
/*										*/
/*	Numeric Parameters							*/
/*										*/
/********************************************************************************/

private static class IntParameter extends BasisParameter {

   private int min_value;
   private int max_value;

   IntParameter(String name) {
      super(name);
      min_value = Integer.MIN_VALUE;
      max_value = Integer.MAX_VALUE;
    }
   
   IntParameter(String name,int min,int max) {
      super(name);
      min_value = min;
      max_value = max;
    }

   public IntParameter(Element xml) {
      super(xml);
      min_value = IvyXml.getAttrInt(xml,"MIN");
      max_value = IvyXml.getAttrInt(xml,"MAX");
    }

   @Override public ParameterType getParameterType() {
      return ParameterType.INTEGER;
    }

   @Override public double getMinValue()		{ return min_value; }
   @Override public double getMaxValue()		{ return max_value; }

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

   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("MIN",min_value);
      xw.field("MAX",max_value);
    }

}	// end of inner class IntParameter



/********************************************************************************/
/*										*/
/*	Real Parameter								*/
/*										*/
/********************************************************************************/

private static class RealParameter extends BasisParameter {

   private double min_value;
   private double max_value;

   RealParameter(String name,double min,double max) {
      super(name);
      min_value = min;
      max_value = max;
    }
   
   RealParameter(String name) {
      super(name);
      min_value = Double.NEGATIVE_INFINITY;
      max_value = Double.POSITIVE_INFINITY;
    }

   public RealParameter(Element xml) {
      super(xml);
      min_value = IvyXml.getAttrDouble(xml,"MIN");
      max_value = IvyXml.getAttrDouble(xml,"MAX");
    }

   @Override public ParameterType getParameterType() {
      return ParameterType.REAL;
    }

   @Override public double getMinValue()		{ return min_value; }
   @Override public double getMaxValue()		{ return max_value; }

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

   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("MIN",min_value);
      xw.field("MAX",max_value);
    }

}	// end of inner class RealParameter




/********************************************************************************/
/*										*/
/*	Time-based parameters							*/
/*										*/
/********************************************************************************/

private static class TimeParameter extends BasisParameter {

   private ParameterType parameter_type;

   TimeParameter(String name,ParameterType typ) {
      super(name);
      parameter_type = typ;
    }

   public TimeParameter(Element xml) {
      super(xml);
      parameter_type = IvyXml.getAttrEnum(xml,"TYPE",ParameterType.DATE);
    }

   @Override public ParameterType getParameterType()	{ return parameter_type; }

   @Override public Object normalize(Object o) {
      Calendar c = createCalendar(o);
      if (c == null) return null;
      switch (parameter_type) {
         case TIME :
            c.set(0,0,0);
            break;
         case DATE :
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            break;
         default :
             break;
       }
      return c;
    }
   
   @Override protected String externalString(Object o)
   {
      if (o == null) return null;
      if (!(o instanceof Calendar)) o = normalize(o);
      Calendar c = (Calendar) o;
      SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
      Date d = c.getTime();
      String rslt = sdf.format(d);
      return rslt;
    }

}	// end of inner class TimeParameter





/********************************************************************************/
/*										*/
/*	SetParameter -- set of strings						*/
/*										*/
/********************************************************************************/

private static class SetParameter extends BasisParameter {

   private Set<String> value_set;

   SetParameter(String nm,Enum<?> e) {
      super(nm);
      value_set = new LinkedHashSet<String>();
      for (Enum<?> x : e.getClass().getEnumConstants()) {
         value_set.add(x.toString().intern());
       }
    }

   SetParameter(String nm,Iterable<String> vals) {
      super(nm);
      value_set = new LinkedHashSet<String>();
      for (String s : vals) value_set.add(s.intern());
    }
   
   
   SetParameter(String nm,String [] vals) {
      super(nm);
      value_set = new LinkedHashSet<String>();
      for (String s : vals) value_set.add(s.intern());
   }

   public SetParameter(Element xml) {
      super(xml);
      value_set = new LinkedHashSet<String>();
      for (Element ve : IvyXml.children(xml,"VALUE")) {
         String nm = IvyXml.getText(ve);
         value_set.add(nm);
       }
    }

   @Override public ParameterType getParameterType() {
      return ParameterType.SET;
    }

   @Override public List<Object> getValues() {
      return new ArrayList<Object>(value_set);
    }

   @Override public Object normalize(Object o) {
      if (o == null) return null;
      String s = o.toString();
      for (String v : value_set) {
         if (v.equals(s)) return v;
       }
      for (String v : value_set) {
         if (v.equalsIgnoreCase(s)) return v;
       }
      return null;
    }

}	// end of inner class SetParameter




/********************************************************************************/
/*										*/
/*	Picture parameter							*/
/*										*/
/********************************************************************************/

private static class PictureParameter extends BasisParameter {

   PictureParameter(String name) {
      super(name);
    }

   public PictureParameter(Element xml) {
      super(xml);
    }

   @Override public ParameterType getParameterType() {
      return ParameterType.PICTURE;
    }

   @Override public Object normalize(Object v) {
      if (v == null) return null;
      if (v instanceof byte []) return v;
      if (v instanceof Element) {
	 Element e = (Element) v;
	 if (e.getNodeName().equals("svg")) {
	    fixupSVG(e);
	    return IvyXml.convertXmlToString(e);
	  }
       }
      else if (v instanceof String) {
	 String vs = ((String) v);
	 if (!vs.startsWith("<svg")) {
	    try {
	       byte [] val = null;
	       val = Base64.getDecoder().decode(vs);
//	       val = javax.xml.bind.DatatypeConverter.parseBase64Binary(vs);
	       if (val[0] == 137 && val[1] == 80 && val[2] == 78 && val[3] == 71) return val;
	       return null;
	     }
	    catch (IllegalArgumentException e) { }
	  }
	 Element e = IvyXml.convertStringToXml(vs);
	 if (IvyXml.isElement(e,"svg")) {
	    if (fixupSVG(e)) vs = IvyXml.convertXmlToString(e);
	    return vs;
	  }
       }
      return null;
    }

   private boolean fixupSVG(Element xml) {
      boolean chng = false;
      chng |= setIfNull(xml,"stroke-linecap","inherit");
      chng |= setIfNull(xml,"stroke-linejoin","inherit");
      chng |= setIfNull(xml,"stroke-dasharray","inherit");
   
      for (Element ce : IvyXml.children(xml)) {
         chng |= fixupSVG(ce);
       }
   
      chng |= fixPath(xml);
   
      return chng;
    }


   private boolean setIfNull(Element xml,String attr,String val) {
      String ov = IvyXml.getAttrString(xml,attr);
      if (ov == null) return false;
      if (!ov.equals("null")) return false;
      xml.setAttribute(attr,val);
      return true;
    }

   private boolean fixPath(Element xml) {
      if (!IvyXml.isElement(xml,"path")) return false;
      String d = IvyXml.getAttrString(xml,"d");
      if (d == null || d.length() < 1000) return false;
      StringBuffer buf = new StringBuffer();
      int dchars = -1;
      for (int i = 0; i < d.length(); ++i) {
         char c = d.charAt(i);
         if (Character.isDigit(c)) {
            if (dchars >= 0 && dchars++ >= 3) continue;
          }
         else if (c == '.') dchars = 0;
         else dchars = -1;
         buf.append(c);
       }
      xml.setAttribute("d",buf.toString());
      return true;
    }

}	// end of inner class PictureParameter



/********************************************************************************/
/*                                                                              */
/*      Parameter with a JSON value                                             */
/*                                                                              */
/********************************************************************************/

private static class JSONParameter extends BasisParameter {
   
   JSONParameter(String name) {
      super(name);
    }
   
   public JSONParameter(Element xml) {
      super(xml);
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.JSON;
    }
   
   @Override public Object normalize(Object o) {
      if (o == null) return null;
      if (o instanceof JSONObject) return o;
      if (o instanceof String) {
         String txt = (String) o;
         txt = txt.trim();
         try {
            if (txt.startsWith("<")) return XML.toJSONObject(txt);
            return new JSONObject(txt);
          }
         catch (JSONException e) { 
            return null;
          }
       }
      if (o instanceof Map) {
         return new JSONObject((Map<?,?>) o);
       }
      if (o instanceof Element) {
         Element elt = (Element) o;
         String s = IvyXml.convertXmlToString(elt);
         try {
            return XML.toJSONObject(s);
          }
         catch (JSONException e) {
            return null;
          }
       }
      
      return null;
    }
   
}       // end of inner class JSONParameter



/********************************************************************************/
/*                                                                              */
/*      Map parameter                                                           */
/*                                                                              */
/********************************************************************************/

private static class MapParameter extends BasisParameter {

   MapParameter(String name) {
      super(name);
    }
   
   public MapParameter(Element xml) {
      super(xml);
    }
   
   @Override public Object normalize(Object o) {
      if (o == null) return null;
      if (o instanceof Map) return o;
      else if (o instanceof JSONObject) {
         JSONObject jo = (JSONObject) o;
         Map<String,Object> rslt = new HashMap<String,Object>();
         for (String s : JSONObject.getNames(jo)) {
            rslt.put(s,jo.opt(s));
          }
         return rslt;
       }
      else if (o instanceof String) {
         try {
            JSONObject jo = new JSONObject((String) o);
            return normalize(jo);
          }
         catch (JSONException e) { }
       }
      return null;
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.MAP;
    }
   
}       // end of inner class MapParameter



/********************************************************************************/
/*                                                                              */
/*      Color parameter                                                         */
/*                                                                              */
/********************************************************************************/

private static class ColorParameter extends BasisParameter {
   
   ColorParameter(String name) {
      super(name);
    }
   
   public ColorParameter(Element xml) {
      super(xml);
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.COLOR;
    }
   
   @Override public Object normalize(Object value) {
      if (value == null) return null;
      if (value instanceof java.awt.Color) {
         return value;
       }
      else if (value instanceof Number) {
         Number n = (Number) value;
         int ivl = n.intValue();
         return new Color(ivl);
       }
      else {
         String s = value.toString();
         return SwingColorSet.getColorByName(s);
       }
    }
   
   @Override protected String externalString(Object o) {
      if (o == null) return "#000000";
      if (!(o instanceof Color)) o = normalize(o);
      Color c = (Color) o;
      String rslt = SwingColorSet.getColorName(c);
      return rslt;
    }
   
}       // end of inner class ColorParameter


/********************************************************************************/
/*                                                                              */
/*      Location parameter                                                      */
/*                                                                              */
/********************************************************************************/

private static class LocationParameter extends BasisParameter {

   LocationParameter(String name) {
      super(name);
    }
   
   public LocationParameter(Element xml) {
      super(xml);
    }
   
   @Override public ParameterType getParameterType() {
      return ParameterType.LOCATION;
    }
   
   @Override public Object normalize(Object value) {
      return createLocation(value);
    }
   
}       // end of inner class LocationParameter



}	// end of class BasisParameter



/* end of BasisParameter.java */

