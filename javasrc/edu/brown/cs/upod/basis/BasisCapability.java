/********************************************************************************/
/*										*/
/*		BasisCapability.java						*/
/*										*/
/*	Generic capability for an object					*/
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

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.*;



public class BasisCapability implements BasisConstants, UpodCapability
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		capability_name;
private String		capability_label;
private String		capability_description;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisCapability(String name)
{
   capability_name = name;
   capability_label = null;
   capability_description = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()		{ return capability_name; }

public String getLabel()
{
   if (capability_label != null) return capability_label;
   return getName();
}


public String getDescription()
{
   if (capability_description != null) return capability_description;
   return getName();
}


protected void setLabel(String lbl)
{
   capability_label = lbl;
}


protected void setDescription(String desc)
{
   capability_description = desc;
}



/********************************************************************************/
/*										*/
/*	Add capability to a device						*/
/*										*/
/********************************************************************************/

@Override public void addToDevice(UpodDevice d)         { }



/********************************************************************************/
/*										*/
/*	Helper methods								*/
/*										*/
/********************************************************************************/

protected void addParameter(UpodDevice d,BasisParameter p)
{
   BasisDevice bd = (BasisDevice) d;
   bd.addParameter(p);
}


protected void addSensor(UpodDevice d,BasisParameter p)
{
   BasisDevice bd = (BasisDevice) d;
   p.setIsSensor(true);
   bd.addParameter(p);
   bd.addConditions(p);
}


protected void addTarget(UpodDevice d,BasisParameter p)
{
   BasisDevice bd = (BasisDevice) d;
   p.setIsTarget(true);
   bd.addParameter(p);
}



/********************************************************************************/
/*										*/
/*	Parameter-based transition						*/
/*										*/
/********************************************************************************/

protected UpodTransition addTransition(UpodDevice d,String name,BasisParameter bp,Object v)
{
   ParameterSetTransition pst = new ParameterSetTransition(name,bp,v);
   BasisDevice bd = (BasisDevice) d;
   return bd.addTransition(pst);
}


protected UpodTransition addValueTransition(UpodDevice d,String name,BasisParameter bp)
{
   ValueTransition vst = new ValueTransition(d,name,bp);
   BasisDevice bd = (BasisDevice) d;
   return bd.addTransition(vst);
}



private static class ParameterSetTransition extends BasisTransition {

   private String transition_name;
   private BasisParameter for_parameter;
   private Object set_value;

   ParameterSetTransition(String name,BasisParameter bp,Object v) {
      transition_name = name;
      for_parameter = bp;
      set_value = v;
    }

   @Override public String getName() {
      return transition_name.replaceAll(" ",NSEP);
    }
   
   @Override public String getLabel() {
      String pfx = transition_name;
      if (pfx.startsWith("Set")) pfx = "Set";
      else if (pfx.startsWith("Turn")) pfx = "Turn";
      return pfx +  " " + set_value;
    }

   @Override public String getDescription() {
      return "Set " + for_parameter.getName() + " = " + set_value;
    }

   @Override public Type getTransitionType() {
      return Type.STATE_CHANGE;
    }

   @Override public void perform(UpodWorld w,UpodDevice e,UpodPropertySet params)
   throws UpodActionException {
      if (e == null) throw new UpodActionException("No entity to act on");
      if (w == null) throw new UpodActionException("No world to act in");
      e.setValueInWorld(for_parameter,set_value,w);
    }

}	// end of inner class ParameterSetTransition




private static class ValueTransition extends BasisTransition {

   private String transition_name;
   private BasisDevice for_device;
   private BasisParameter for_parameter;

   ValueTransition(UpodDevice d,String name,BasisParameter bp) {
      transition_name = name;
      for_device = (BasisDevice) d;
      for_parameter = bp;
      addParameter(for_parameter,null);
    }

   @Override public String getName() {
      return for_device.getName() + NSEP + transition_name.replaceAll(" ",NSEP);
    }

   @Override public String getDescription() {
      return transition_name + " " + for_device.getLabel() + " value";
    }

   @Override public String getLabel() {
      return transition_name + " " + for_device.getLabel();
    }

   @Override public Type getTransitionType() {
      return Type.STATE_CHANGE;
    }


   @Override public void perform(UpodWorld w,UpodDevice e,UpodPropertySet params)
   throws UpodActionException {
      if (e == null) throw new UpodActionException("No entity to act on");
      if (w == null) throw new UpodActionException("No world to act in");
      e.setValueInWorld(for_parameter,params.get(for_parameter.getName()),w);
}

}	// end of inner class ParameterSetTransition












/********************************************************************************/
/*										*/
/*	Start capability for device						*/
/*										*/
/********************************************************************************/

@Override public void startCapability(UpodDevice d)
{
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("CAPABILITY");
   xw.field("CLASS",getClass().getName());
   xw.field("NAME",capability_name);
   xw.field("LABEL",getLabel());
   outputLocalXml(xw);
   if (capability_description != null) {
      xw.textElement("DESCRIPTION",capability_description);
    }
   xw.end("CAPABILITY");
}


protected void outputLocalXml(IvyXmlWriter xw) { }



}	// end of class BasisCapability




/* end of BasisCapability.java */

