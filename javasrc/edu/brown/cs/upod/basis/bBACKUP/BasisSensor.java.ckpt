/********************************************************************************/
/*                                                                              */
/*              BasisSensor.java                                                */
/*                                                                              */
/*      description of class                                                    */
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



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.swing.*;

import java.util.*;



public abstract class BasisSensor implements BasisConstants, UpodSensor
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          current_state;
private long            last_time;
private List<String>    known_states;
private SwingEventListenerList<UpodSensorHandler> sensor_handlers;
private Map<String,UpodCondition> computed_conditions;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected BasisSensor()
{
   this(null,null);
}


protected BasisSensor(Collection<String> states,String init)
{
   if (states == null) known_states = null;
   else known_states = new ArrayList<String>(states);
   
   if (init == null && states == null) current_state = null;
   else if (init != null) current_state = init;
   else if (states.size() > 0) current_state = known_states.get(0);
   else current_state = null;
   
   sensor_handlers = new SwingEventListenerList<UpodSensorHandler>(UpodSensorHandler.class);
   computed_conditions = null;
}




/********************************************************************************/
/*                                                                              */
/*      Public Access methods                                                   */
/*                                                                              */
/********************************************************************************/

@Override public abstract String getName();

@Override public Collection<String> getStates() 
{
   if (known_states == null) throw new UpodException("States not defined");
   return new ArrayList<String>(known_states);
}


@Override public String getCurrentState(UpodWorld w)
{
   if (w.isCurrent()) {
      if (current_state == null) throw new UpodException("Current state not defined");
      return current_state;
    }
   else {
      // get state from World
      return null;
    }
}
   

@Override public void addTrigger(UpodSensorHandler hdlr)
{
   sensor_handlers.add(hdlr);
}



@Override public void removeTrigger(UpodSensorHandler hdlr)
{
   sensor_handlers.remove(hdlr);
}


@Override public synchronized Collection<UpodCondition> getConditions()
{
   if (computed_conditions == null) {
      computed_conditions = new HashMap<String,UpodCondition>();
      for (String s : getStates()) {
         SensorCondition sc = new SensorCondition(s);
         computed_conditions.put(s,sc);
       }
    }
   return computed_conditions.values();
}



/********************************************************************************/
/*                                                                              */
/*      Sensor update methods                                                   */
/*                                                                              */
/********************************************************************************/

protected void setState(String state,UpodWorld w) {
   if (state == null) return;
   String oldstate = getCurrentState(w);
   if (state.equals(current_state)) return;
   if (w.isCurrent()) {
      current_state = state;
      last_time = System.currentTimeMillis();
    }
   else {
      w.setProperty(getName(),state);
      w.setProperty(getName()+"@TIME",System.currentTimeMillis());
    }
   
   if (computed_conditions != null) {
      SensorCondition sc = (SensorCondition) computed_conditions.get(oldstate);
      if (sc != null) sc.triggerOff(w);
      sc = (SensorCondition) computed_conditions.get(state);
      if (sc != null) sc.triggerOn(w,null);
    }
   
   for (UpodSensorHandler hdlr : sensor_handlers) {
      hdlr.sensorStateChanged(w,this);
    }
}


@Override public void addProperties(UpodPropertySet props)
{
   props.put(getName(),current_state);
   props.put(getName()+"@TIME",last_time);
}



/********************************************************************************/
/*                                                                              */
/*      Internal condition                                                      */
/*                                                                              */
/********************************************************************************/

private class SensorCondition extends BasisCondition implements UpodCondition {
   
   private String       for_state;
   
   SensorCondition(String s) {
      super(BasisSensor.this.getName() + "=" + s);
      for_state = s;
    }
   
   @Override public UpodSensor getSensor()              { return BasisSensor.this; }
   
   @Override public UpodConditionConflict getConflicts(UpodCondition c) {
      if (c.getSensor() == getSensor()) {
         // return conflict
       }
      return super.getConflicts(c);
    }
   
   @Override public UpodParameterSet poll(UpodWorld state) {
      if (!for_state.equals(getSensor().getCurrentState(state))) return null;
      
      BasisParameterSet ps = new BasisParameterSet();
      // ps.put(BasisSensor.this.getName(),for_state);
      return ps;
    }
   
}       // end of inner class SensorCondition

}       // end of class BasisSensor




/* end of BasisSensor.java */

