/********************************************************************************/
/*										*/
/*		BasisConditionRange.java					*/
/*										*/
/*	Check the range of a value-base parameter				*/
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

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import java.util.*;


public class BasisConditionRange extends BasisCondition implements UpodDeviceHandler
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodDevice	for_device;
private UpodParameter	cond_param;
private Number		low_value;
private Number		high_value;
private Boolean 	is_on;
private boolean 	is_trigger;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisConditionRange(UpodDevice device,UpodParameter p,Number low,Number high,boolean trigger)
{
   super(device.getUniverse());
   low_value = low;
   high_value = high;
   for_device = device;
   cond_param = p;
   for_device.addDeviceHandler(this);
   is_on = null;
   is_trigger = trigger;
}


public BasisConditionRange(UpodProgram pgm,Element xml)
{
   super(pgm.getUniverse());

   low_value = IvyXml.getAttrDouble(xml,"LOW");
   high_value = IvyXml.getAttrDouble(xml,"HIGH");
   for_device = pgm.createDevice(IvyXml.getChild(xml,"DEVICE"));
   is_trigger = IvyXml.getAttrBool(xml,"TRIGGER");
   String pnm = IvyXml.getAttrString(xml,"PARAMETER");
   cond_param = for_device.findParameter(pnm);
   if (cond_param != null) for_device.addDeviceHandler(this);
   is_on = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   StringBuffer buf = new StringBuffer();

   buf.append(for_device.getName());
   if (low_value != null && high_value != null) {
      buf.append("BETWEEN " + low_value + " AND " + high_value);
    }
   else if (low_value != null) buf.append(" ABOVE " + low_value);
   else if (high_value != null) buf.append(" BELOW " + high_value);

   return buf.toString();
}

@Override public String getDescription()
{
   return getName();
}


@Override public void getSensors(Collection<UpodDevice> rslt)
{
   if (for_device != null) rslt.add(for_device);
}



@Override public void setTime(UpodWorld world)
{
   if (!for_device.isEnabled()) return;
}


@Override public boolean isTrigger()			{ return is_trigger; }



@Override public void addImpliedProperties(UpodPropertySet ps)
{ }



/********************************************************************************/
/*										*/
/*	Handle state changes							*/
/*										*/
/********************************************************************************/

@Override public void stateChanged(UpodWorld w,UpodDevice s)
{
   if (!s.isEnabled()) {
      if (is_on == null) return;
      if (is_on == Boolean.TRUE) fireOff(w);
      is_on = null;
    }

   Object cvl = s.getValueInWorld(cond_param,w);
   boolean rslt = false;
   if (cvl != null && cvl instanceof Number) {
      Number nvl = (Number) cvl;
      double vl = nvl.doubleValue();
      if (low_value == null || vl >= low_value.doubleValue()) {
	 if (high_value == null || vl <= high_value.doubleValue()) {
	    rslt = true;
	  }
       }
    }

   // don't trigger on initial setting
   if (is_on == null && is_trigger) is_on = rslt;

   if (is_on != null && rslt == is_on && w.isCurrent()) return;
   is_on = rslt;

   BasisLogger.logI("CONDITION: " + getName() + " " + is_on);

   if (is_trigger) {
      fireTrigger(w,getResultProperties(cvl));
    }
   else if (rslt) {
      fireOn(w,getResultProperties(cvl));
    }
   else {
      fireOff(w);
    }
}



private BasisPropertySet getResultProperties(Object val) {
   BasisPropertySet ps = new BasisPropertySet();
   ps.put(cond_param.getName(),val.toString());
   return ps;
}




@Override public boolean isConsistentWith(BasisCondition bc)
{
   if (!(bc instanceof BasisConditionRange)) return true;
   BasisConditionRange sbc = (BasisConditionRange) bc;
   if (low_value != null && sbc.high_value != null &&
	 low_value.doubleValue() < sbc.high_value.doubleValue()) return false;
   if (high_value != null && sbc.low_value != null &&
	 high_value.doubleValue() > sbc.low_value.doubleValue()) return false;
   return true;
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   outputHeader(xw);
   xw.field("TYPE","RANGE");
   xw.field("PARAMETER",cond_param.getName());
   if (low_value != null) xw.field("LOW",low_value);
   if (high_value != null) xw.field("HIGH",high_value);
   xw.field("MIN",cond_param.getMinValue());
   xw.field("MAX",cond_param.getMaxValue());

   for_device.outputXml(xw);
   outputTrailer(xw);
}


@Override public String toString() {
   return getName();
}



}	// end of class BasisConditionRange




/* end of BasisConditionRange.java */

