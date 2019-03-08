/********************************************************************************/
/*										*/
/*		SmartHabCapability.java 					*/
/*										*/
/*	Capabilities for different openHAB device types 			*/
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



package edu.brown.cs.upod.smarthab;

import java.util.*;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.basis.BasisCapability;
import edu.brown.cs.upod.basis.BasisDevice;
import edu.brown.cs.upod.basis.BasisParameter;
import edu.brown.cs.upod.basis.BasisTransition;
import edu.brown.cs.upod.basis.BasisUniverse;
import edu.brown.cs.upod.upod.UpodActionException;
import edu.brown.cs.upod.upod.UpodDevice;
import edu.brown.cs.upod.upod.UpodPropertySet;
import edu.brown.cs.upod.upod.UpodTransition;
import edu.brown.cs.upod.upod.UpodWorld;
import edu.brown.cs.upod.upod.UpodParameter.ParameterType;

abstract public class SmartHabCapability extends BasisCapability implements SmartHabConstants
{



/********************************************************************************/
/*										*/
/*	Static methods								*/
/*										*/
/********************************************************************************/

static void addCapabilities(BasisUniverse uu)
{
   uu.addCapability(new ColorItem());
   uu.addCapability(new ContactItem());
   uu.addCapability(new DateTimeItem());
   uu.addCapability(new DimmerItem());
   uu.addCapability(new LocationItem());
   uu.addCapability(new NumberItem());
   // uu.addCapability(new NumberRangeItem());
   uu.addCapability(new RollershutterItem());
   uu.addCapability(new StringItem());
   uu.addCapability(new SwitchItem());
   uu.addCapability(new TemperatureItem());
   // uu.addCapability(new TemperatureSetItem());
   // uu.addCapability(new ValuesItem());
   // uu.addCapability(new ValueSwitchItem());
}



static BasisCapability findTemperatureSetCapability(BasisUniverse uu,
      double min,double max)
{
   String id = "TempeartureSetItem:" + min + "," + max;
   BasisCapability cap = uu.findCapability(id);
   if (cap == null) {
      cap = new TemperatureSetItem(id,min,max);
      uu.addCapability(cap);
    }

   return cap;
}


static BasisCapability findNumberRangeCapability(BasisUniverse uu,
      double min,double max)
{
   String id = "NumberRangeItem:" + min + "," + max;
   BasisCapability cap = uu.findCapability(id);
   if (cap == null) {
      cap = new NumberRangeItem(id,min,max);
      uu.addCapability(cap);
    }

   return cap;
}


static BasisCapability findValuesCapability(BasisUniverse uu,Map<String,String> values)
{
   String id = "Values:";
   for (Map.Entry<String,String> ent : values.entrySet()) {
      id += ent.getKey() + ">" + ent.getValue() +";";
    }
   BasisCapability cap = uu.findCapability(id);
   if (cap == null) {
      cap = new ValuesItem(id,values);
      uu.addCapability(cap);
    }

   return cap;
}



static BasisCapability findValueSwitchCapability(BasisUniverse uu,Map<String,String> values)
{
   String id = "Values:";
   for (Map.Entry<String,String> ent : values.entrySet()) {
      id += ent.getKey() + ">" + ent.getValue() +";";
    }
   BasisCapability cap = uu.findCapability(id);
   if (cap == null) {
      cap = new ValueSwitchItem(id,values);
      uu.addCapability(cap);
    }

   return cap;
}





/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected SmartHabCapability(String name)
{
   super(name);
}


/********************************************************************************/
/*										*/
/*	Capabilities for SmartHab device types					*/
/*										*/
/********************************************************************************/

private static class ContactItem extends SmartHabCapability {

   private static String [] CONTACT_STATE = { "OPEN", "CLOSED" };

   ContactItem() {
      super("ContactItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter sp = BasisParameter.createEnumParameter("state",CONTACT_STATE);
      sp.setLabel(d.getLabel());
      sp.setDescription(d.getLabel() + " state");
      addSensor(d,sp);
    }

}	// end of inner class ContactItem




private static class DimmerItem extends SmartHabCapability {

   DimmerItem() {
      super("DimmerItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("switch",0,100);
      addTarget(d,bp);
      addValueTransition(d,"Set",bp);
      BasisParameter sp = BasisParameter.createIntParameter("state",0,100);
      sp.setLabel(d.getLabel());
      sp.setDescription(d.getLabel() + " setting");
      addSensor(d,sp);
    }

}	// end of inner class DimmerItem



private static class SwitchItem extends SmartHabCapability {

   private static String [] SWITCH_STATE = { "OFF", "ON" };

   SwitchItem() {
      super("SwitchItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createEnumParameter("switch",SWITCH_STATE);
      addTarget(d,bp);
      addTransition(d,"TurnOff",bp,"OFF");
      addTransition(d,"TurnOn",bp,"ON");
      BasisParameter sp = BasisParameter.createEnumParameter("state",SWITCH_STATE);
      sp.setLabel(d.getLabel());
      sp.setDescription(d.getLabel() + " state");
      addSensor(d,sp);
    }

}	// end of inner class SwitchItem


private static class RollershutterItem extends SmartHabCapability {

   RollershutterItem() {
      super("RollershutterItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createIntParameter("state",0,100);
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " state");
      addSensor(d,bp);
      addCommandTransition(d,"UP","UP",bp,Integer.valueOf(0));
      addCommandTransition(d,"DOWN","DOWN",bp,Integer.valueOf(100));
      addCommandTransition(d,"STOP","STOP",bp,Integer.valueOf(50));
    }

}	// end of inner class RollershutterItem



private static class NumberItem extends SmartHabCapability {

   NumberItem() {
      super("NumberItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createRealParameter("state");
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " value");
      addSensor(d,bp);
    }

}	// end of inner class NumberItem



private static class NumberRangeItem extends SmartHabCapability {

   private double min_value;
   private double max_value;

   NumberRangeItem(String name,double min,double max) {
      super(name);
      min_value = min;
      max_value = max;
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createRealParameter("state",min_value,max_value);
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " value");
      addSensor(d,bp);
    }

   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("MIN",min_value);
      xw.field("MAX",max_value);
    }

}	// end of inner class NumberRangeItem



private static class ValuesItem extends SmartHabCapability {

   private Map<String,String> value_map;

   ValuesItem(String name,Map<String,String> values) {
      super(name);
      value_map = new HashMap<String,String>(values);
    }

   @Override public void addToDevice(UpodDevice d) {
      SmartHabDevice shd = (SmartHabDevice) d;
      Map<Integer,String> rslt = new TreeMap<Integer,String>();
      for (Map.Entry<String,String> ent : value_map.entrySet()) {
	 String k = ent.getKey();
	 try {
	    int kv = Integer.parseInt(k);
	    rslt.put(kv,ent.getValue());
	  }
	 catch (NumberFormatException e) { }
       }
      BasisParameter bp = BasisParameter.createEnumParameter("state",rslt.values());
      bp.setDescription(d.getLabel() + " value");
      addSensor(d,bp);
      shd.addValueMap(value_map);
    }

   @Override public void outputXml(IvyXmlWriter xw) {
      for (Map.Entry<String,String> ent : value_map.entrySet()) {
	 xw.begin("MAP");
	 xw.field("KEY",ent.getKey());
	 xw.field("VALUE",ent.getValue());
	 xw.end("MAP");
       }
    }

}	// end of inner clas ValuesItem


private static class ValueSwitchItem extends SmartHabCapability {

   private Map<String,String> value_map;

   ValueSwitchItem(String name,Map<String,String> values) {
      super(name);
      value_map = new HashMap<String,String>(values);
    }

   @Override public void addToDevice(UpodDevice d) {
      SmartHabDevice shd = (SmartHabDevice) d;
      Map<Integer,String> rslt = new TreeMap<Integer,String>();
      for (Map.Entry<String,String> ent : value_map.entrySet()) {
	 String k = ent.getKey();
	 try {
	    int kv = Integer.parseInt(k);
	    rslt.put(kv,ent.getValue());
	  }
	 catch (NumberFormatException e) { }
       }
      BasisParameter bp = BasisParameter.createEnumParameter("state",rslt.values());
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " value");
      addTarget(d,bp);
      for (String s : rslt.values()) {
	 addTransition(d,"Set" + s,bp,s);
       }
      shd.addValueMap(value_map);
    }

   @Override public void outputXml(IvyXmlWriter xw) {
      for (Map.Entry<String,String> ent : value_map.entrySet()) {
	 xw.begin("MAP");
	 xw.field("KEY",ent.getKey());
	 xw.field("VALUE",ent.getValue());
	 xw.end("MAP");
       }
    }

}	// end of inner calss ValueSwitchItem



private static class TemperatureItem extends SmartHabCapability {

   TemperatureItem() {
      super("TemperatureItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      SmartHabDevice shd = (SmartHabDevice) d;
      BasisParameter bp = BasisParameter.createRealParameter("state",-100,100);
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " state");
      addSensor(d,bp);
      shd.addTemperatureMap();
    }

}	// end of inner class TemperatureItem


public static class TemperatureSetItem extends SmartHabCapability {

   private double min_value;
   private double max_value;

   TemperatureSetItem(String name,double xmin,double xmax) {
      super(name);
      min_value = xmin;
      max_value = xmax;
    }

   public TemperatureSetItem(Element xml) {
      super(IvyXml.getAttrString(xml,"NAME"));
      min_value = IvyXml.getAttrDouble(xml,"MIN");
      max_value = IvyXml.getAttrDouble(xml,"MAX");
    }

   @Override public void addToDevice(UpodDevice d) {
      SmartHabDevice shd = (SmartHabDevice) d;
      BasisParameter bp = BasisParameter.createRealParameter("setting",min_value,max_value);
      addTarget(d,bp);
      addValueTransition(d,"Set",bp);
      shd.addTemperatureMap();
    }

   @Override public void outputLocalXml(IvyXmlWriter xw) {
      xw.field("MIN",min_value);
      xw.field("MAX",max_value);
    }

}	// end of inner class TemperatureItem



private static class DateTimeItem extends SmartHabCapability {

   DateTimeItem() {
      super("DateTimeItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createTimeParameter("state",ParameterType.DATETIME);
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " time");
      addSensor(d,bp);
    }

}	// end of inner class DateTimeItem




private static class StringItem extends SmartHabCapability {

   StringItem() {
      super("StringItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createStringParameter("state");
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " value");
      addSensor(d,bp);
    }

}	// end of inner class StringItem



private static class LocationItem extends SmartHabCapability {

   LocationItem() {
      super("LocationItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      BasisParameter bp = BasisParameter.createLocationParameter("state");
      bp.setLabel(d.getLabel());
      bp.setDescription(d.getLabel() + " location");
      addSensor(d,bp);
    }

}	// end of inner class LocationItem



private static class ColorItem extends SmartHabCapability {

   ColorItem() {
      super("ColorItem");
    }

   @Override public void addToDevice(UpodDevice d) {
      SmartHabDevice shd = (SmartHabDevice) d;
      BasisParameter bp = BasisParameter.createColorParameter("state");
      addTarget(d,bp);
      addValueTransition(d,"Set",bp);
      shd.addColorMap();
    }

}	// end of inner class ColorItem




/********************************************************************************/
/*										*/
/*	Transition for action							*/
/*										*/
/********************************************************************************/

static private UpodTransition addCommandTransition(UpodDevice d,String name,String cmd,
      BasisParameter bp,Object rslt)
{
   String fullname = d.getLabel() + " " + name;
   CommandTransition ct = new CommandTransition(fullname,cmd,bp,rslt);
   BasisDevice bd = (BasisDevice) d;
   return bd.addTransition(ct);
}




private static class CommandTransition extends BasisTransition {

   private String transition_name;
   private String transition_label;
   private String command_name;
   private BasisParameter for_parameter;
   private Object result_value;

   CommandTransition(String name,String cmd,BasisParameter p,Object rslt) {
      transition_label = name;
      transition_name = name.replaceAll(" ",NSEP);
      command_name = name;
      for_parameter = p;
      result_value = rslt;
    }

   @Override public String getName()		{ return transition_name; }
   @Override public String getLabel()		{ return transition_label; }
   @Override public String getDescription()	{ return transition_label; }
   @Override public Type getTransitionType()	{ return Type.TEMPORARY_CHANGE; }

   @Override public void perform(UpodWorld w,UpodDevice d,UpodPropertySet params)
	throws UpodActionException {
      if (d == null) throw new UpodActionException("No entity to act on");
      if (w == null) throw new UpodActionException("No world to act in");
      SmartHabDevice shd = (SmartHabDevice) d;
      if (w.isCurrent()) {
	 shd.doCommand(w,command_name);
       }
      else {
	 d.setValueInWorld(for_parameter,result_value,w);
       }
    }


}	// end of inner class CommandTransition

}	// end of class SmartHabCapability




/* end of SmartHabCapability.java */

