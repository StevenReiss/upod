/********************************************************************************/
/*										*/
/*		BasisConditionTimed.java					*/
/*										*/
/*	Condition that indicates another condition being on for given time	*/
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

import org.w3c.dom.Element;

import java.util.*;


class BasisConditionDuration extends BasisCondition implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private BasisCondition	base_condition;
private long		min_time;
private long		max_time;
private boolean 	is_trigger;
private Map<UpodWorld,StateRepr> active_states;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

BasisConditionDuration(UpodCondition c,long start,long end,boolean trigger)
{
   super(c.getUniverse());

   initialize();

   base_condition = (BasisCondition) c;
   min_time = start;
   max_time = end;
   is_trigger = trigger;

   setup();
}



BasisConditionDuration(UpodProgram pgm,Element xml)
{
   super(pgm,xml);

   initialize();

   min_time = IvyXml.getAttrLong(xml,"MINTIME",0);
   max_time = IvyXml.getAttrLong(xml,"MAXTIME",0);
   is_trigger = IvyXml.getAttrBool(xml,"TRIGGER");

   base_condition = (BasisCondition) pgm.createCondition(IvyXml.getChild(xml,"CONDITION"));
   
   setup();
}



private void initialize()
{
   base_condition = null;
   min_time = 0;
   max_time = 0;
   active_states = new HashMap<UpodWorld,StateRepr>();
}


private void setup()
{
   base_condition.addConditionHandler(new CondChanged());
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   return base_condition.getName() + "@" + min_time + "-" + max_time;
}

@Override public String getDescription()
{
   return getName();
}


@Override public void getSensors(Collection<UpodDevice> rslt)
{
   base_condition.getSensors(rslt);
}


@Override public boolean isTrigger()		{ return is_trigger; }




/********************************************************************************/
/*										*/
/*	Status setting methods							*/
/*										*/
/********************************************************************************/

@Override public void setTime(UpodWorld w)
{
   StateRepr sr = getState(w);
   sr.checkAgain(0);
}



private void setError(UpodWorld w,Throwable c)
{
   StateRepr sr = getState(w);
   sr.setError(c);
}


private void setOn(UpodWorld w,UpodPropertySet ps)
{
   StateRepr sr = getState(w);
   sr.setOn(ps);
}


private void setOff(UpodWorld w)
{
   StateRepr sr = getState(w);
   sr.setOff();
}


private synchronized StateRepr getState(UpodWorld w)
{
   StateRepr sr = active_states.get(w);
   if (sr == null) {
      if (is_trigger) sr = new StateReprTrigger(w);
      else  sr = new StateReprTimed(w);
      active_states.put(w,sr);
    }

   return sr;
}



/********************************************************************************/
/*										*/
/*	Conflict detection							*/
/*										*/
/********************************************************************************/

protected boolean isConsistentWith(BasisCondition uc)
{
   if (uc instanceof BasisConditionDuration) {
      BasisConditionDuration bct = (BasisConditionDuration) uc;
      if (bct.base_condition == bct.base_condition) {
	 if (max_time <= bct.min_time || min_time >= bct.max_time) return false;
	 return true;
       }
    }

   return base_condition.isConsistentWith(uc);
}



@Override public void addImpliedProperties(UpodPropertySet ups)
{
   // add properties for start and end time and day of week
}



protected boolean checkOverlapConditions(BasisCondition bc)
{
   if (bc instanceof BasisConditionDuration) {
      BasisConditionDuration bct = (BasisConditionDuration) bc;
      if (bct.base_condition == base_condition) {
	 if (max_time <= bct.min_time || min_time >= bct.max_time) return false;
	 return true;
       }
    }

   return bc.isConsistentWith(base_condition);
}

/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   outputHeader(xw);
   xw.field("TYPE",(is_trigger ? "TRIGGER" : "TIMED"));
   xw.field("MINTIME",min_time);
   xw.field("MAXTIME",max_time);
   base_condition.outputXml(xw);
   outputTrailer(xw);
}



/********************************************************************************/
/*										*/
/*	Handle changes to the condition 					*/
/*										*/
/********************************************************************************/

private class CondChanged implements UpodConditionHandler {

   @Override public void conditionError(UpodWorld w,UpodCondition c,Throwable t) {
      setError(w,t);
    }

   @Override public void conditionOn(UpodWorld w,UpodCondition c,
	 UpodPropertySet ps) {
       setOn(w,ps);
    }

   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
      setOff(w);
    }

   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,UpodPropertySet ps) {
      setOn(w,ps);
    }

}	// end of inner class CondChanged



/********************************************************************************/
/*										*/
/*	Generic state representation for a world				*/
/*										*/
/********************************************************************************/

private abstract class StateRepr {

   protected UpodWorld for_world;
   protected Throwable error_cause;
   protected TimerTask timer_task;
   protected UpodPropertySet on_params;

   StateRepr(UpodWorld w) {
      for_world = w;
      error_cause = null;
      timer_task = null;
      on_params = null;
    }

   abstract void updateStatus();

   void setError(Throwable t) {
      if (t == null) t = new Error("Unknown error");
      error_cause = t;
      on_params = null;
      updateStatus();
    }

   void setOn(UpodPropertySet ps) {
      on_params = ps;
      checkAgain(0);
      updateStatus();
    }

   void setOff() {
      on_params = null;
      checkAgain(0);
      updateStatus();
    }

   protected void checkAgain(long when) {
      if (for_world.isCurrent()) recheck(when);
      else updateStatus();
    }

   protected void recheck(long when) {
      if (timer_task != null) timer_task.cancel();
      timer_task = null;
      if (for_world.isCurrent()) {
         if (when <= 0) return;
         Timer t = BasisWorld.getWorldTimer();
         timer_task = new TimeChanged(for_world);
         t.schedule(timer_task,when);
       }
    }

}	// end of inner class StateRepr




/********************************************************************************/
/*										*/
/*	State Representation for timed condition				*/
/*										*/
/********************************************************************************/

private class StateReprTimed extends StateRepr {

   private long start_time;

   StateReprTimed(UpodWorld w) {
      super(w);
      start_time = 0;
    }

   void updateStatus() {
      if (error_cause != null) {
         start_time = 0;
         recheck(0);
         fireError(for_world,error_cause);
         return;
       }
   
      if (on_params == null) {
         start_time = 0;
         recheck(0);
         fireOff(for_world);
         return;
       }
   
      if (start_time == 0) start_time = for_world.getTime();
      long now = for_world.getTime();
      if (now - start_time < min_time) {
         fireOff(for_world);
         recheck(min_time - (now-start_time));
       }
      else if (max_time > 0 &&	now-start_time > max_time) {
         fireOff(for_world);
       }
      else {
         fireOn(for_world,on_params);
       }
    }

}	// end of inner class StateReprTimed




/********************************************************************************/
/*										*/
/*	State Representation for timed condition				*/
/*										*/
/********************************************************************************/

private class StateReprTrigger extends StateRepr {

   private long last_time;
   private UpodPropertySet save_params;

   StateReprTrigger(UpodWorld w) {
      super(w);
      last_time = 0;
      save_params = null;
    }

   void updateStatus() {
      if (error_cause != null) {
	 last_time = 0;
	 save_params = null;
	 checkAgain(0);
	 fireError(for_world,error_cause);
	 return;
       }

      if (on_params == null && last_time == 0) {
	 checkAgain(0);
	 fireOff(for_world);
	 return;
       }
      if (on_params != null) {
	 last_time = for_world.getTime();
	 save_params = on_params;
	 fireOn(for_world,on_params);
	 return;
       }

      long now = for_world.getTime();
      if (now - last_time < max_time) {
	 fireOn(for_world,save_params);
	 checkAgain(max_time - (now-last_time));
       }
      else {
	 save_params = null;
	 last_time = 0;
	 fireOff(for_world);
       }
    }


}


/********************************************************************************/
/*										*/
/*	Timer task to cause recheck						*/
/*										*/
/********************************************************************************/

private class TimeChanged extends TimerTask {

   private UpodWorld for_world;

   TimeChanged(UpodWorld w) {
      for_world = w;
    }

   @Override public void run() {
      StateRepr sr = active_states.get(for_world);
      if (sr != null) sr.updateStatus();
    }

}



}	// end of class BasisConditionTimed




/* end of BasisConditionTimed.java */

