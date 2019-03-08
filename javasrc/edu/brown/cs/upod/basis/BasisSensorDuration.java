/********************************************************************************/
/*										*/
/*		BasisSensorTimed.java						*/
/*										*/
/*	Sensor to be used as a trigger or timed condition			*/
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


public class BasisSensorDuration extends BasisDevice implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String		sensor_label;
private String		sensor_name;
private String		base_id;
private UpodParameter	state_parameter;

private UpodDevice	base_sensor;
private UpodParameter	base_parameter;
private Object		base_state;
private UpodCondition	base_condition;
private String		condition_name;
private long		min_time;
private long		max_time;
private long		trigger_time;

private Map<UpodWorld,StateRepr> active_states;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisSensorDuration(String id,UpodDevice base,UpodParameter param,Object state,long start,long end)
{
   this(id,base,param,state,null,start,end);
}


public BasisSensorDuration(String id,UpodCondition c,long start,long end)
{
   this(id,null,null,null,c,start,end);
}



public BasisSensorDuration(String id,UpodDevice base,UpodParameter param,Object state,
      UpodCondition c,long start,long end)
{
   super(findInitialUniverse(base,c));

   base_sensor = base;
   base_state = (state == null ? Boolean.TRUE : state);
   sensor_label = id;
   min_time = start;
   max_time = end;
   base_parameter = param;
   base_condition = c;
   condition_name = null;

   if (base_parameter == null && base_sensor != null) {
      base_parameter = base_sensor.findParameter(base_sensor.getUID());
    }

   setup();
}



public BasisSensorDuration(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   min_time = IvyXml.getAttrLong(xml,"MIN",0);
   max_time = IvyXml.getAttrLong(xml,"MAX",-1);

   sensor_label = IvyXml.getAttrString(xml,"LABEL");
   sensor_name = IvyXml.getAttrString(xml,"NAME");

   Element eb = IvyXml.getChild(xml,"BASE");
   base_state = IvyXml.getAttrString(eb,"SET");
   Element e = IvyXml.getChild(eb,"DEVICE");
   base_id = IvyXml.getAttrString(e,"ID");
   base_sensor = null;
   if (base_id != null) {
      base_sensor = uu.findDevice(base_id);
      String pnm = IvyXml.getAttrString(eb,"PARAM");
      if (pnm == null) pnm = base_sensor.getUID();
      base_parameter = base_sensor.findParameter(pnm);
    }
   else {
      condition_name = IvyXml.getAttrString(xml,"COND");
      if (condition_name != null) {
	 base_condition = uu.findBasicCondition(condition_name);
	 if (base_condition != null) condition_name = null;
       }
    }

   uu.addUniverseListener(new UniverseChanged());

   setup();
}



private static UpodUniverse findInitialUniverse(UpodDevice base,UpodCondition c)
{
   if (base != null) return base.getUniverse();
   if (c != null) return c.getUniverse();
   return null;
}





private void setup()
{
   BasisParameter bp = BasisParameter.createBooleanParameter(getUID());
   bp.setIsSensor(true);
   bp.setLabel(getLabel());
   state_parameter = addParameter(bp);

   if (min_time < 0) min_time = 0;
   if (min_time > 0 && max_time < min_time) max_time = 0;
   if (min_time == 0 && max_time <= 0) max_time = 100;
   trigger_time = 0;

   String nm1 = sensor_label.replace(" ",WSEP);

   sensor_name = getUniverse().getName() + NSEP + nm1;

   active_states = new HashMap<UpodWorld,StateRepr>();

   addConditions(state_parameter);
   UpodCondition uc = getCondition(state_parameter,Boolean.TRUE);
   uc.setLabel(sensor_label);
   UpodCondition ucf = getCondition(state_parameter,Boolean.FALSE);
   ucf.setLabel("Not " + sensor_label);
}



@Override protected void localStartDevice()
{
   if (condition_name != null && base_condition == null) {
      base_condition = getUniverse().findBasicCondition(condition_name);
      if (base_condition != null) condition_name = null;
    }

   if (base_sensor != null) {
      base_sensor.addDeviceHandler(new SensorChanged());
    }
   else if (base_condition != null) {
      base_condition.addConditionHandler(new SensorChanged());
    }

   handleStateChanged(getCurrentWorld());
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()		{ return sensor_name; }

@Override public String getLabel()		{ return sensor_label; }

@Override public String getDescription()
{
   String s1 = null;
   if (base_sensor != null) {
      s1 = "Sensor " + base_sensor.getLabel() + "=" + base_state;
    }
   else if (base_condition != null) {
      s1 = base_condition.getLabel();
    }
   else s1 = "Sensor";

   if (min_time == 0 && max_time > 0) {
      s1 += " ON for at most " + getTimeDescription(max_time);
    }
   else if (min_time > 0 && max_time < min_time) {
      s1 += " ON for at least " + getTimeDescription(min_time);
    }
   else if (min_time > 0 && max_time > 0) {
      s1 += " ON for between " + getTimeDescription(min_time) +
      " AND " + getTimeDescription(max_time);
    }

   return s1;
}


private String getTimeDescription(long t)
{
   double t0 = t/1000;
   String what = "seconds";
   if (t0 > 60*60) {
      t0 = t0 / 60 / 60;
      what = "hours";
    }
   else if (t0 > 60) {
      t0 = t0 / 60;
      what = "minutes";
    }

   return Double.toString(t0) + " " + what;
}


@Override protected UpodCondition createParameterCondition(UpodParameter p,Object v,boolean trig)
{
   return new DurationCondition(p,v,trig);
}



/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw)
{
   xw.field("MIN",min_time);
   xw.field("MAX",max_time);
   if (base_sensor != null) {
      xw.begin("BASE");
      xw.field("SET",base_state);
      xw.field("PARAM",base_parameter.getName());
      base_sensor.outputXml(xw);
      xw.end("BASE");
    }
   else if (base_condition != null) {
      xw.field("COND",base_condition.getName());
    }
   else if (condition_name != null) {
      xw.field("COND",condition_name);
    }

   super.outputLocalXml(xw);
}




/********************************************************************************/
/*										*/
/*	Handle stat changes in underlying sensor				*/
/*										*/
/********************************************************************************/

private void handleStateChanged(UpodWorld w)
{
    StateRepr sr = active_states.get(w);
    if (sr == null) {
       sr = new StateRepr(w);
       active_states.put(w,sr);
     }

    sr.handleChange();
}



private class SensorChanged implements UpodDeviceHandler, UpodConditionHandler {

   @Override public void stateChanged(UpodWorld w,UpodDevice s) {
      handleStateChanged(w);
    }

   @Override public void conditionOn(UpodWorld w,UpodCondition c,UpodPropertySet p) {
      handleStateChanged(w);
    }

   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,UpodPropertySet p) {
      if (w.isCurrent()) {
	 trigger_time = w.getTime();
	 handleStateChanged(w);
       }
    }

   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
      handleStateChanged(w);
    }

   @Override public void conditionError(UpodWorld w,UpodCondition c,Throwable cause) {
    }

}	// end of inner class SensorChanged



private class UniverseChanged implements UpodUniverse.Listener {

   @Override public void conditionAdded(UpodUniverse u,UpodCondition c) {
      if (condition_name != null && c.getName().equals(condition_name)) {
	 base_condition = c;
	 condition_name = null;
       }
    }

   @Override public void conditionRemoved(UpodUniverse u,UpodCondition c) {
      if (base_condition == c) {
	 // remove this sensor as well
       }
    }

   @Override public void deviceAdded(UpodUniverse u,UpodDevice s) {
      if (for_universe == u && base_sensor == null && base_id != null &&
	    s.getUID().equals(base_id)) {
	 base_sensor = s;
	 base_sensor.addDeviceHandler(new SensorChanged());
       }
    }
   @Override public void deviceRemoved(UpodUniverse u,UpodDevice s) {
      if (for_universe == u && base_sensor == s) {
	 // remove this sensor as well
       }
    }

}



/********************************************************************************/
/*										*/
/*	Handle State Modifications						*/
/*										*/
/********************************************************************************/

private class StateRepr {

   private UpodWorld for_world;
   private TimerTask timer_task;
   private long start_time;

   StateRepr(UpodWorld w) {
      for_world = w;
      timer_task = null;
      start_time = 0;
    }

   void handleChange() {
      recheck(0);
      updateStatus();
    }

   protected void recheck(long when) {
      if (for_world.isCurrent()) {
	 if (timer_task != null) timer_task.cancel();
	 timer_task = null;
	 System.err.println("RECHECK DURATION " + getLabel() + when);
	 if (when <= 0) return;
	 Timer t = BasisWorld.getWorldTimer();
	 timer_task = new TimeChanged(for_world);
	 t.schedule(timer_task,when);
       }
    }

   private void updateStatus() {
      if (!for_world.isCurrent()) return;
   
      boolean fg = false;
      if (base_sensor != null) {
         Object ov = base_sensor.getValueInWorld(base_parameter,for_world);
         fg = base_state.equals(ov);
       }
      else if (base_condition != null && !base_condition.isTrigger()) {
         UpodPropertySet ups = base_condition.getCurrentStatus(for_world);
         fg = ups != null;
       }
      else if (base_condition != null && base_condition.isTrigger()) {
         if (trigger_time > 0) fg = true;
         if (start_time > 0 && trigger_time > start_time) start_time = trigger_time;
       }
   
      if (!fg) {
         start_time = 0;
         recheck(0);
         setValueInWorld(state_parameter,Boolean.FALSE,for_world);
         return;
       }
   
      if (start_time == 0) start_time = for_world.getTime();
      long now = for_world.getTime();
      if (now - start_time < min_time) {
         setValueInWorld(state_parameter,Boolean.FALSE,for_world);
         recheck(min_time - (now-start_time));
       }
      else if (max_time > 0 && now-start_time > max_time) {
         setValueInWorld(state_parameter,Boolean.FALSE,for_world);
         trigger_time = 0;
       }
      else {
         setValueInWorld(state_parameter,Boolean.TRUE,for_world);
         if (max_time > 0) {
            recheck(max_time - (now-start_time));
          }
       }
    }

}	// end of inner class StateRepr




/********************************************************************************/
/*										*/
/*	Timer Task to auto update						*/
/*										*/
/********************************************************************************/

private class TimeChanged extends TimerTask {

   private UpodWorld for_world;

   TimeChanged(UpodWorld w) {
      for_world = w;
    }

   @Override public void run() {
      BasisLogger.logD("DURATION CHECK at " + new Date().toString());
      StateRepr sr = active_states.get(for_world);
      if (sr != null) sr.updateStatus();
    }

}	// end of inner class TimeChanged



/********************************************************************************/
/*										*/
/*	Parameter condition for a duration sensor				*/
/*										*/
/********************************************************************************/

private class DurationCondition extends BasisConditionParameter {

   DurationCondition(UpodParameter p,Object v,boolean trig) {
      super(BasisSensorDuration.this,p,v,trig);
    }

   @Override public boolean isConsistentWith(BasisCondition bc) {
      if (!super.isConsistentWith(bc)) return false;
      if (bc instanceof BasisConditionParameter) {
	 BasisConditionParameter sbc = (BasisConditionParameter) bc;
	 if (sbc.getDevice() == base_sensor && sbc.getParameter() == base_parameter) {
	    if (getState() == Boolean.TRUE) {
	       if (sbc.getState() != base_state) return false;
	     }
	  }
       }

      return true;
    }

}	// end of inner class DurationCondition



}	// end of class BasisSensorTimed




/* end of BasisSensorTimed.java */
