/********************************************************************************/
/*										*/
/*		BasisConditionCalendar.java					*/
/*										*/
/*	Time/Calendar based condition (event for a particular time period	*/
/*	where the time period can have repeats, days, etc.      		*/
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


public class BasisConditionTime extends BasisCondition implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private BasisCalendarEvent	calendar_event;
private String			condition_name;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

BasisConditionTime(UpodUniverse uu,String name,BasisCalendarEvent evt)
{
   super(uu);
   condition_name = name;
   calendar_event = evt;
   setupTimer();
   setCurrent();
}


public BasisConditionTime(UpodProgram pgm,Element xml)
{
   super(pgm,xml);

   condition_name = IvyXml.getAttrString(xml,"NAME");
   Element cevt = IvyXml.getChild(xml,"CALEVENT");
   if (cevt == null) cevt = xml;
   calendar_event = new BasisCalendarEvent(cevt);
   setupTimer();
   setCurrent();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   if (condition_name == null || condition_name.equals("") ||
	 condition_name.equals("null")) {
      condition_name = BasisWorld.getNewUID();
    }

   return condition_name;
}

@Override public String getLabel()
{
   String s = super.getLabel();
   if (s == null) s = condition_name;
   if (s == null) s = calendar_event.getDescription();
   return s;
}

@Override public String getDescription()
{
   if (condition_name == null) return calendar_event.getDescription();
   return condition_name + " @ " + calendar_event.getDescription();
}


@Override public void getSensors(Collection<UpodDevice> rslt)   { }




/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@Override public void setTime(UpodWorld w)
{
   if (calendar_event.isActive(w.getTime())) {
      BasisLogger.logI("CONDITION " + getLabel() + " ACTIVE");
      fireOn(w,null);
    }
   else {
      fireOff(w);
      BasisLogger.logI("CONDITION " + getLabel() + " INACTIVE");
    }
}



private void setupTimer()
{
   long delay = T_DAY;			// check at least every day
   long now = System.currentTimeMillis();
   Calendar c0 = Calendar.getInstance();
   c0.setTimeInMillis(now);
   Calendar c1 = Calendar.getInstance();
   c1.setTimeInMillis(now + delay);
   List<Calendar> slots = calendar_event.getSlots(c0,c1);
   if (slots != null && slots.size() > 0) {
      Calendar s0 = slots.get(0);
      long t0 = s0.getTimeInMillis();
      long t1 = 0;
      if (slots.size() > 1) {
	 Calendar s1 = slots.get(1);
	 t1 = s1.getTimeInMillis();
       }
      if (t0 == 0 || t0 <= now) {
	 setCurrent();
	 delay = t1-now;
       }
      else delay = t0-now;
    }
   BasisWorld.getWorldTimer().schedule(new CondChecker(),delay);
}



private void setCurrent()
{
   UpodWorld cw = BasisFactory.getFactory().getCurrentWorld(getUniverse());
   setTime(cw);
}



/********************************************************************************/
/*										*/
/*	Conflict detection							*/
/*										*/
/********************************************************************************/

protected boolean isConsistentWith(BasisCondition uc)
{
   if (!(uc instanceof BasisConditionTime)) return true;
   BasisConditionTime bcc = (BasisConditionTime) uc;

   if (calendar_event.canOverlap(bcc.calendar_event)) return true;

   return false;
}



@Override public void addImpliedProperties(UpodPropertySet ups)
{
   calendar_event.addImpliedProperties(ups);
}



/********************************************************************************/
/*										*/
/*	Timer-based condition checker						*/
/*										*/
/********************************************************************************/

private class CondChecker extends TimerTask {

   @Override public void run() {
      setCurrent();
      setupTimer();
    }

}	// end of inner class CondChecker




/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   outputHeader(xw);
   xw.field("TYPE","TIME");
   calendar_event.outputXml(xw);
   outputTrailer(xw);
}




}	// end of class BasisConditionCalendar




/* end of BasisConditionCalendar.java */

