/********************************************************************************/
/*										*/
/*		BasisSensorLatch.java						*/
/*										*/
/*	Implementation of a latch-type sensor device				*/
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


public class BasisSensorLatch extends BasisDevice
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
private UpodCondition   base_condition;
private String          condition_name;
private Calendar	reset_time;
private long		reset_after;
private long		off_after;

private Map<UpodWorld,StateRepr> active_states;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public BasisSensorLatch(String id,UpodDevice base,UpodParameter param,Object state,Calendar reset)
{
   this(id,base,param,state,0,0,reset);
}



public BasisSensorLatch(String id,UpodDevice base,UpodParameter param,Object state,long after,long offafter)
{
   this(id,base,param,state,after,offafter,null);
}


public BasisSensorLatch(String id,UpodCondition cond,Calendar reset)
{
   this(id,cond,0,0,reset);
}



public BasisSensorLatch(String id,UpodCondition cond,long after,long offafter)
{
   this(id,cond,after,offafter,null);
}



private BasisSensorLatch(String id,UpodDevice base,UpodParameter param,Object state,long after,long offafter,Calendar time)
{
   super(base.getUniverse());
   
   base_sensor = base;
   base_state = state;
   base_condition = null;
   condition_name = null;
   sensor_label = id;
   reset_time = time;
   reset_after = after;
   off_after = offafter;
   
   base_parameter = param;
   
   if (base_parameter == null) {
      base_parameter = base_sensor.findParameter(base_sensor.getUID());
    }
   
   setup();
}



private BasisSensorLatch(String id,UpodCondition cond,long after,long offafter,Calendar time)
{
   super(cond.getUniverse());
   
   base_sensor = null;
   base_state = null;
   base_condition = cond;
   condition_name = null;
   sensor_label = id;
   reset_time = time;
   reset_after = after;
   off_after = offafter;
   base_parameter = null;
   
   setup();
}




public BasisSensorLatch(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   sensor_label = IvyXml.getAttrString(xml,"LABEL");
   sensor_name = IvyXml.getAttrString(xml,"NAME");

   reset_time = null;
   long reset = IvyXml.getAttrLong(xml,"RESET");
   if (reset > 0) {
      reset_time = Calendar.getInstance();
      reset_time.setTimeInMillis(reset);
    }
   reset_after = IvyXml.getAttrLong(xml,"AFTER");
   off_after = IvyXml.getAttrLong(xml,"OFFAFTER");

   Element eb = IvyXml.getChild(xml,"BASE");
   if (eb != null) {
      base_state = IvyXml.getAttrString(eb,"SET");
      Element e = IvyXml.getChild(eb,"DEVICE");
      base_id = IvyXml.getAttrString(e,"ID");
      base_sensor = uu.findDevice(base_id);
      String pnm = IvyXml.getAttrString(eb,"PARAM");
      if (pnm == null) pnm = base_sensor.getUID();
      base_parameter = base_sensor.findParameter(pnm);
    }
   else {
      condition_name = IvyXml.getAttrString(xml,"COND");
      base_condition = uu.findBasicCondition(condition_name);
      if (base_condition != null) condition_name = null;
    }

   active_states = new HashMap<UpodWorld,StateRepr>();

   uu.addUniverseListener(new UniverseChanged());

   setup();
}




private void setup()
{
   BasisParameter bp = BasisParameter.createBooleanParameter(getUID());
   bp.setIsSensor(true);
   bp.setLabel(getLabel());
   state_parameter = addParameter(bp);

   String nm1 = sensor_label.replace(" ",WSEP);

   sensor_name = getUniverse().getName() + NSEP + nm1;

   active_states = new HashMap<UpodWorld,StateRepr>();

   addConditions(state_parameter);
   UpodCondition uc = getCondition(state_parameter,Boolean.TRUE);
   uc.setLabel(sensor_label);
   UpodCondition ucf = getCondition(state_parameter,Boolean.FALSE);
   ucf.setLabel("Not " + sensor_label);

   Reseter rst = new Reseter();
   addTransition(rst);
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
      s1 = "Latch " + base_sensor.getLabel() + "=" + base_state;
    }
   else if (base_condition != null) {
      s1 = "Latch " + base_condition.getLabel();
    }
   else if (condition_name != null) {
      s1 = "Latch " + condition_name;
    }
   else s1 = "Latch";

   return s1;
}


@Override protected UpodCondition createParameterCondition(UpodParameter p,Object v,boolean trig)
{
   return new LatchCondition(p,v,trig);
}


/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw)
{
   if (reset_time != null) xw.field("RESET",reset_time.getTimeInMillis());
   xw.field("AFTER",reset_after);
   xw.field("OFFAFTER",off_after);
   
   if (base_condition != null) {
      xw.field("COND",base_condition.getName());
    }
   else if (condition_name != null) {
      xw.field("COND",condition_name);
    }

   if (base_sensor != null) {
      xw.begin("BASE");
      xw.field("SET",base_state);
      xw.field("PARAM",base_parameter.getName());
      base_sensor.outputXml(xw);
      xw.end("BASE");
    }

   super.outputLocalXml(xw);
}




/********************************************************************************/
/*										*/
/*	Handle state changes in underlying sensor				*/
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
   
   @Override public void conditionOn(UpodWorld w,UpodCondition c,UpodPropertySet ps) {
      handleStateChanged(w);
    }
   
   @Override public void conditionOff(UpodWorld w,UpodCondition c) {
      handleStateChanged(w);
    }
   
   @Override public void conditionTrigger(UpodWorld w,UpodCondition c,UpodPropertySet ps) {
      handleStateChanged(w);
    }
   
   @Override public void conditionError(UpodWorld w,UpodCondition c,Throwable t) {
    }
   
}	// end of inner class SensorChanged




/********************************************************************************/
/*										*/
/*	Handle resets								*/
/*										*/
/********************************************************************************/

private void resetLatch(UpodWorld w)
{
   StateRepr sr = active_states.get(w);
   if (sr != null) sr.handleReset();
}




@Override public void apply(UpodTransition t,UpodPropertySet ps,UpodWorld w)
{
   if (t instanceof Reseter) resetLatch(w);
}




private class UniverseChanged implements UpodUniverse.Listener {

   @Override public void conditionAdded(UpodUniverse u,UpodCondition c) { 
      if (condition_name != null && c.getName().equals(condition_name)) {
         base_condition = c;
         condition_name = null;
       }
    }
   @Override public void conditionRemoved(UpodUniverse u,UpodCondition c) {
      if (c == base_condition) {
         // remove sensor here
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
   private long off_time;

   StateRepr(UpodWorld w) {
      for_world = w;
      timer_task = null;
      start_time = 0;
      off_time = 0;
    }
   
   void handleChange() {
      recheck(0);
      updateStatus(true);
    }

   void handleReset() {
      boolean fg = false;
      if (base_sensor != null) {
         Object ov = base_sensor.getValueInWorld(base_parameter,for_world);
         fg = base_state.equals(ov);
       }
      else if (base_condition != null && !base_condition.isTrigger()) {
         fg = base_condition.getCurrentStatus(for_world) != null;
       }
      
      if (fg) {
	  handleRecheck();
       }
      else  {
	 start_time = 0;
	 off_time = 0;
	 setValueInWorld(state_parameter,Boolean.FALSE,for_world);
       }
    }

   protected void recheck(long when) {
      if (for_world.isCurrent()) {
	 if (timer_task != null) timer_task.cancel();
	 timer_task = null;
	 BasisLogger.logI("RECHECK LATCH DURATION " + getLabel() + " " +  when);
	 if (when <= 0) return;
	 Timer t = BasisWorld.getWorldTimer();
	 timer_task = new TimeChanged(for_world);
	 t.schedule(timer_task,when);
       }
    }

   private void updateStatus(boolean set) {
      if (!for_world.isCurrent()) return;
      
      boolean fg = false;
      if (base_sensor != null) {
         Object ov = base_sensor.getValueInWorld(base_parameter,for_world);
         fg = base_state.equals(ov);
       }
      else if (base_condition != null && !base_condition.isTrigger()) {
         fg = base_condition.getCurrentStatus(for_world) != null;
       }
      else if (base_condition != null && base_condition.isTrigger()) {
         fg = set;
       }

      if (!fg) {
	 Object nv = getValueInWorld(state_parameter,for_world);
	 if (nv == null) setValueInWorld(state_parameter,Boolean.FALSE,for_world);

	 if (off_time <= 0) {
	    if (reset_after == 0) start_time = 0;
	    off_time = for_world.getTime();
	    handleRecheck();
	  }
	 return;
       }

      off_time = 0;
      start_time = for_world.getTime();
      setValueInWorld(state_parameter,Boolean.TRUE,for_world);
      handleRecheck();
    }

   private void handleRecheck() {
      long now = System.currentTimeMillis();

      if (reset_time != null) {
	 long rt = reset_time.getTimeInMillis();
	 rt = rt % T_DAY;
	 long n1 = now % T_DAY;
	 long d = rt - n1;
	 while (d <= 0) d += T_DAY;
	 recheck(d);
       }
      else if (off_after > 0 && off_time > 0) {
	  long when = off_time + off_after;
	  long d = when - now;
	  recheck(d);
       }
      else if (reset_after > 0 && start_time > 0) {
	 long when = start_time + reset_after;
	 long d = when - now;
	 recheck(d);
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
      BasisLogger.logD("LATCH RESET at " + new Date().toString());
      StateRepr sr = active_states.get(for_world);
      if (sr != null) sr.handleReset();
    }

}	// end of inner class TimeChanged



/********************************************************************************/
/*										*/
/*	Reset Transition							*/
/*										*/
/********************************************************************************/

private class Reseter extends BasisTransition {

   Reseter() { }

   @Override public Type getTransitionType()	{ return Type.STATE_CHANGE; }

   @Override public String getName() {
      return BasisSensorLatch.this.getName() + NSEP + "Reset";
    }

   @Override public String getDescription() {
      return "Reset " + BasisSensorLatch.this.getLabel();
    }

   @Override public String getLabel() {
      return "Reset " + BasisSensorLatch.this.getLabel();
    }

}	// end of inner class Reseter



/********************************************************************************/
/*										*/
/*	Parameter condition for a latch 					*/
/*										*/
/********************************************************************************/

private class LatchCondition extends BasisConditionParameter {

   LatchCondition(UpodParameter p,Object v,boolean trig) {
      super(BasisSensorLatch.this,p,v,trig);
    }

   @Override public boolean isConsistentWith(BasisCondition bc) {
      if (!super.isConsistentWith(bc)) return false;
      if (bc instanceof BasisConditionParameter) {
	 BasisConditionParameter sbc = (BasisConditionParameter) bc;
	 if (sbc.getDevice() == base_sensor && sbc.getParameter() == base_parameter) {
	    if (getState() == Boolean.TRUE) {
	       if (sbc.getState() != base_state) return false;
	     }
	    else {
	       if (sbc.getState() == base_state) return false;
	     }
	  }
       }

      return true;
    }

}	// end of inner class LatchCondition


}	// end of class BasisSensorLatch




/* end of BasisSensorLatch.java */

