/********************************************************************************/
/*										*/
/*		BasisConditionParameter.java					*/
/*										*/
/*	Conditions based on a parameter value					*/
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


public class BasisConditionParameter extends BasisCondition implements UpodDeviceHandler
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodDevice	for_device;
private Object		for_state;
private UpodParameter	cond_param;
private Boolean 	is_on;
private boolean 	is_trigger;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisConditionParameter(UpodDevice device,UpodParameter p,Object s)
{
   this(device,p,s,false);
}


public BasisConditionParameter(UpodDevice device,UpodParameter p,Object s,boolean trig)
{
   super(device.getUniverse());
   for_state = s;
   for_device = device;
   cond_param = p;
   for_device.addDeviceHandler(this);
   is_on = null;
   is_trigger = trig;
   setLabel(cond_param.getLabel() + " = " + for_state);
}


public BasisConditionParameter(UpodProgram pgm,Element xml)
{
   super(pgm.getUniverse());

   for_state = IvyXml.getAttrString(xml,"STATE");
   for_device = pgm.createDevice(IvyXml.getChild(xml,"DEVICE"));
   String pnm = IvyXml.getAttrString(xml,"PARAMETER");
   cond_param = for_device.findParameter(pnm);
   if (cond_param != null) for_device.addDeviceHandler(this);
   is_trigger = IvyXml.getAttrBool(xml,"TRIGGER");
   is_on = null;
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   if (is_trigger) {
      return for_device + "->" + for_state;
    }
   return for_device.getName() + "=" + for_state;
}



@Override public String getDescription()
{
   return getName();
}

@Override public void getSensors(Collection<UpodDevice> rslt)
{
   rslt.add(for_device);
}



@Override public void setTime(UpodWorld world)
{
   if (!for_device.isEnabled()) return;
}



@Override public void addImpliedProperties(UpodPropertySet ups)
{
   ups.put(cond_param.getName(),for_state);
}



@Override public boolean isTrigger()
{
   return is_trigger;
}



/********************************************************************************/
/*										*/
/*	Handle state changes							*/
/*										*/
/********************************************************************************/

@Override public void stateChanged(UpodWorld w,UpodDevice s)
{
   if (!s.isEnabled()) {
      if (is_on == null) return;
      if (is_on == Boolean.TRUE && !is_trigger) fireOff(w);
      is_on = null;
    }
   Object cvl = s.getValueInWorld(cond_param,w);
   boolean rslt = for_state.equals(cvl);
   if (is_on != null && rslt == is_on && w.isCurrent()) return;
   is_on = rslt;

   BasisLogger.logI("CONDITION: " + getName() + " " + is_on);
   if (rslt) {
      if (is_trigger) fireTrigger(w,getResultProperties());
      else fireOn(w,getResultProperties());
    }
   else if (!is_trigger) fireOff(w);
}

protected UpodDevice getDevice()                { return for_device; }

protected Object getState()                     { return for_state; }

protected UpodParameter getParameter()          { return cond_param; }


private BasisPropertySet getResultProperties() {
   BasisPropertySet ps = new BasisPropertySet();
   ps.put(cond_param.getName(),for_state);
   return ps;
}




@Override public boolean isConsistentWith(BasisCondition bc)
{
   if (!(bc instanceof BasisConditionParameter)) return true;
   BasisConditionParameter sbc = (BasisConditionParameter) bc;
   if (sbc.for_device != for_device) return true;
   if (!sbc.for_state.equals(for_state)) return false;
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
   xw.field("PARAMETER",cond_param.getName());
   xw.field("STATE",for_state);
   xw.field("TYPE","PRIM");
   outputLocalXml(xw);
   for_device.outputXml(xw);
   outputTrailer(xw);
}

protected void outputLocalXml(IvyXmlWriter xw)          { }


@Override public String toString() {
   String r = for_device.getName() + "==" + for_state;
   if (is_trigger) r += "(T)";
   return r;
}


}	// end of class BasisConditionParameter




/* end of BasisConditionParameter.java */

