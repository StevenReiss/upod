/********************************************************************************/
/*                                                                              */
/*              SmartHabDevice.java                                             */
/*                                                                              */
/*      General openHAB device                                                  */
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



package edu.brown.cs.upod.smarthab;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.awt.Color;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.swing.SwingColorSet;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

class SmartHabDevice extends BasisDevice implements SmartHabConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

interface MapObject {
   String openHabToSmartHab(String v);
   String smartHabToOpenHab(Object v);
   void outputXml(IvyXmlWriter xw);
}


private String  device_name;
private MapObject map_object;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SmartHabDevice(UpodUniverse uu,String name)
{
   super(uu);
   device_name = name;
   map_object = null;
}

 
SmartHabDevice(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   
   device_name = IvyXml.getAttrString(xml,"OPENNAME");
   if (device_name == null) {
      String nm = IvyXml.getAttrString(xml,"NAME");
      int idx = nm.indexOf(NSEP);
      if (idx >= 0) nm = nm.substring(idx+NSEP.length());
      device_name = nm;
    }
   
   map_object = null;
   Element mapxml = IvyXml.getChild(xml,"MAPOBJECT");
   if (mapxml != null) {
      try {
         Class<?> c = Class.forName(IvyXml.getAttrString(mapxml,"CLASS"));
         Constructor<?> cc = c.getConstructor(new Class[] { Element.class });
         Object o = cc.newInstance(mapxml);
         map_object = (MapObject) o;
       }
      catch (Throwable t) {
         System.err.println("Problem creating map object: " + t);
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

void addTemperatureMap()
{
   map_object = new TempMap(null);
}



void addValueMap(Map<String,String> map)
{
   map_object = new ValueMap(map);
}


void addColorMap()
{
   map_object = new ColorMap(null);
}


/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public String getDescription()
{
   return device_name.replace("_"," ");
}


@Override public String getLabel()
{
   return getDescription();
}



@Override public String getName()
{
   return for_universe.getName() + NSEP + device_name;
}




/********************************************************************************/
/*                                                                              */
/*      Setting methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public void setValueInWorld(UpodParameter p,Object val,UpodWorld w)
{
   if (w == null) w = getCurrentWorld();
   
   super.setValueInWorld(p,val,w);
   
   if (p.isTarget() && w.isCurrent()) {
      Object xvalo = getValueInWorld(p,w);
      if (xvalo == null) return;
      String xval = xvalo.toString();
      if (xval.equals("Uninitialized")) return;
      if (map_object != null) xval = map_object.smartHabToOpenHab(xvalo);
      if (xval == null) return;
      SmartHabUniverse shu = (SmartHabUniverse) w.getUniverse();
      shu.sendCommand(device_name + "=" + xval);
    }
}




String convertOpenHabToSmartHab(String val)
{
   if (map_object != null) return map_object.openHabToSmartHab(val);
   return val;
}


/********************************************************************************/
/*                                                                              */
/*      OutputMethods                                                           */
/*                                                                              */
/********************************************************************************/

@Override public void outputLocalXml(IvyXmlWriter xw)
{
   xw.field("OPENNAME",device_name);
   if (map_object != null) {
      xw.begin("MAPOBJECT");
      xw.field("CLASS",map_object.getClass().getName());
      map_object.outputXml(xw);
      xw.end("MAPOBJECT");
    }
}



/********************************************************************************/
/*                                                                              */
/*      Command methods                                                         */
/*                                                                              */
/********************************************************************************/

void doCommand(UpodWorld w,String cmd)
{
   if (w.isCurrent()) {
      SmartHabUniverse shu = (SmartHabUniverse) w.getUniverse();
      shu.sendCommand(device_name + "=" + cmd);  
    }
}



/********************************************************************************/
/*                                                                              */
/*      Mapping for temperature objects                                         */
/*                                                                              */
/********************************************************************************/

private static class TempMap implements MapObject {
   
   TempMap(Element xml) { }
   
   @Override public String openHabToSmartHab(String v) {
      if (v == null) return null;
      try {
         double v1 = Double.parseDouble(v);
         v1 = 9.0*v1/5.0 + 32.0;
         return Double.toString(v1);
       }
      catch (NumberFormatException e) {
         return v;
       }
    }
   
   @Override public String smartHabToOpenHab(Object vo) {
      if (vo == null) return null;
      String v = vo.toString();
      if (v == "Uninitialized") return null;
      try {
         double v1 = Double.parseDouble(v);
         v1 = (v1-32.0)/9.0*5.0;
         return Double.toString(v1);
       }
      catch (NumberFormatException e) {
         return v;
       }
    }
   
   @Override public void outputXml(IvyXmlWriter xw) { }
   
}       // end of inner class TempMap



private static class ValueMap implements MapObject {
   
   private Map<String,String> input_map;
   private Map<String,String> output_map;
   
   ValueMap(Map<String,String> map) {
      input_map = new HashMap<String,String>();
      output_map = new HashMap<String,String>();
      for (Map.Entry<String,String> ent : map.entrySet()) {
         String k = ent.getKey();
         String v = ent.getValue();
         addValue(k,v);
       }
    }
   
   @SuppressWarnings("unused") 
   ValueMap(Element xml) {
      for (Element melt : IvyXml.children(xml,"MAP")) {
         String cmd = IvyXml.getTextElement(melt,"KEY");
         String val = IvyXml.getTextElement(melt,"VALUE");
         addValue(cmd,val);
       }
    }
   
   @Override public String openHabToSmartHab(String v) {
      if (v == null) return null;
      String v1 = input_map.get(v);
      if (v1 != null) return v1;
      return v;
    }
   
   @Override public String smartHabToOpenHab(Object vo) {
      if (vo == null) return null;
      String v = vo.toString();
      String v1 = output_map.get(v);
      if (v1 != null) return v1;
      if (v == "Uninitialized") return null;
      return v;
    }
   
   @Override public void outputXml(IvyXmlWriter xw) {
      for (Map.Entry<String,String> ent : input_map.entrySet()) {
         xw.begin("MAP");
         xw.field("KEY",ent.getKey());
         xw.field("VALUE",ent.getValue());
         xw.end("MAP");
       }
    }
   
   private void addValue(String cmd,String val) {
      input_map.put(cmd,val);
      output_map.put(val.toUpperCase(),cmd);
    }
   
}       // end of inner class ValueMap



private static class ColorMap implements MapObject {

   ColorMap(Element xml) { }
   
   @Override public String openHabToSmartHab(String v) {
      float [] vals = new float[5];
      StringTokenizer tok = new StringTokenizer(v,",");
      int idx = 0;
      while (tok.hasMoreTokens()) {
         try {
            vals[idx++] = Float.parseFloat(tok.nextToken())/100.0f;
            if (idx > 4) idx = 4;
          }
         catch (NumberFormatException e) { 
            return "#000";
          }
       }
      
      Color c = null;
      if (idx >= 4) c = new Color(vals[0],vals[1],vals[2],vals[3]);
      else c = new Color(vals[0],vals[1],vals[2]);
      
      String r = Integer.toHexString(c.getRed());
      if (r.length() == 1) r = "0" + r;
      String g = Integer.toHexString(c.getGreen());
      if (g.length() == 1) g = "0" + g;
      String b = Integer.toHexString(c.getBlue());
      if (b.length() == 1) b = "0" + b;
      return "#" + r + g + b;
   }
   
   @Override public String smartHabToOpenHab(Object v) {
      Color c = null;
      if (v instanceof Color) c = (Color) v;
      else {
        c = SwingColorSet.getColorByName(v.toString());
       }
      StringBuffer buf = new StringBuffer();
      float [] comps = c.getColorComponents(null);
      for (int i = 0; i < 3; ++i) {
         if (i > 0) buf.append(",");
         buf.append(comps[i]*100.0);
       }
      return buf.toString();
    }
   
   @Override public void outputXml(IvyXmlWriter xw) { }  
   
}       // end of inner class ColorMap




}       // end of class SmartHabDevice




/* end of SmartHabDevice.java */

