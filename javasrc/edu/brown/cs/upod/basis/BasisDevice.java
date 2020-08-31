/********************************************************************************/
/*										*/
/*		BasisDevice.java						*/
/*										*/
/*	Basis implementation of a device					*/
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

import edu.brown.cs.ivy.swing.*;
import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.Element;

import java.util.*;

public abstract class BasisDevice implements UpodDevice, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

protected UpodUniverse	for_universe;
private boolean 	is_enabled;
private SwingEventListenerList<UpodDeviceHandler> device_handlers;
private String		device_uid;
private List<UpodParameter> parameter_set;
private List<UpodTransition> transition_set;
private Map<UpodParameter,Map<Object,UpodCondition>> cond_map;
private List<UpodCapability> capability_set;
private Set<String>     device_groups;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisDevice(UpodUniverse uu)
{
   initialize(uu);
   device_uid = BasisWorld.getNewUID();
}


protected BasisDevice(UpodUniverse uu,Element xml)
{
   initialize(uu);

   device_uid = IvyXml.getAttrString(xml,"ID");
   if (device_uid == null) device_uid = BasisWorld.getNewUID();

   is_enabled = IvyXml.getAttrBool(xml,"ENABLED",is_enabled);
   
   for (Element s : IvyXml.children(xml,"GROUP")) {
      String g = IvyXml.getText(s).trim();
      if (g != null && g.length() > 0) device_groups.add(g);
    }
   
   for (Element c : IvyXml.children(xml,"CAPABILITY")) {
      String cid = IvyXml.getAttrString(c,"NAME");
      UpodCapability uc = uu.findCapability(cid);
      if (uc != null) addCapability(uc);
    }
}



private void initialize(UpodUniverse uu)
{
   for_universe = uu;
   device_handlers =
      new SwingEventListenerList<UpodDeviceHandler>(UpodDeviceHandler.class);

   device_uid = null;
   is_enabled = true;
   parameter_set = new ArrayList<UpodParameter>();
   cond_map = new HashMap<UpodParameter,Map<Object,UpodCondition>>();
   transition_set = new ArrayList<UpodTransition>();
   capability_set = new ArrayList<UpodCapability>();
   device_groups = new HashSet<String>();
}




/********************************************************************************/
/*										*/
/*	Public Access methods							*/
/*										*/
/********************************************************************************/

@Override public abstract String getName();
@Override public abstract String getDescription();

@Override public final String getUID()		{ return device_uid; }

@Override public String getLabel()		{ return getName(); }

@Override public UpodUniverse getUniverse()	{ return for_universe; }



@Override public Collection<String> getGroups()
{
   return Collections.unmodifiableSet(device_groups);
}


@Override public Collection<UpodCapability> getCapabilities()
{
   return new ArrayList<UpodCapability>(capability_set);
}


protected UpodWorld getCurrentWorld()
{
   return BasisFactory.getFactory().getCurrentWorld(for_universe);
}


@Override public String getCurrentStatus()
{
   return getCurrentStatus(getCurrentWorld());
}


@Override public String getCurrentStatus(UpodWorld w)
{
   UpodParameter bp = findParameter(getUID());
   if (bp != null) {
      Object val = w.getValue(bp);
      if (val != null) return val.toString();
    }

   if (parameter_set.size() == 1) {
      bp = parameter_set.get(0);
      Object val = w.getValue(bp);
      if (val != null) return val.toString();
    }

   if (parameter_set.size() > 1) {
      StringBuffer buf = new StringBuffer();
      for (UpodParameter up : parameter_set) {
	 Object val = w.getValue(up);
	 if (val != null) {
	    if (buf.length() > 0) buf.append(", ");
	    buf.append(up.getName());
	    buf.append("=");
	    buf.append(val.toString());
	  }
       }
      if (buf.length() > 0) return buf.toString();
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Parameter methods							*/
/*										*/
/********************************************************************************/

@Override public Collection<UpodParameter> getParameters()
{
   return parameter_set;
}


@Override public UpodParameter findParameter(String id)
{
   if (id == null) return null;
   
   for (UpodParameter up : parameter_set) {
      if (up.getName().equals(id)) return up;
      if (up.getLabel().equals(id)) return up;
    }

   return null;
}


public UpodParameter addParameter(UpodParameter p)
{
   for (UpodParameter up : parameter_set) {
      if (up.getName().equals(p.getName())) return up;
    }

   parameter_set.add(p);

   return p;
}


@Override public UpodCondition getCondition(UpodParameter p,Object v)
{
   Map<Object,UpodCondition> m1 = cond_map.get(p);
   if (m1 == null) return null;
   v = p.normalize(v);
   return m1.get(v);
}


protected void addConditions(UpodParameter p)
{
   List<Object> vals = p.getValues();
   if (vals != null) {
      for (Object v : vals) {
	 addCondition(p,v,false);
       }
    }
}


protected void addTriggerConditions(UpodParameter p)
{
   List<Object> vals = p.getValues();
   if (vals != null) {
      for (Object v : vals) {
	 addCondition(p,v,true);
       }
    }
}


protected UpodCondition addCondition(UpodParameter p,Object v,boolean trig)
{
   UpodCondition c = getCondition(p,v);
   if (c == null) {
      c = createParameterCondition(p,v,trig);
      Map<Object,UpodCondition> m1 = cond_map.get(p);
      if (m1 == null) {
	 m1 = new HashMap<Object,UpodCondition>();
	 cond_map.put(p,m1);
       }
      m1.put(v,c);
    }
   return c;
}



protected UpodCondition createParameterCondition(UpodParameter p,Object v,boolean trig)
{
   return new BasisConditionParameter(this,p,v,trig);
}


protected void setHierarchy(String h)
{
   if (h != null) {
      int idx = -1;
      for ( ; ; ) {
         idx = h.indexOf(".",idx+1);
         if (idx < 0) break;
         device_groups.add(h.substring(0,idx));
       }
      device_groups.add(h);
    }
}


protected void addGroup(String g)
{
   device_groups.add(g);
}


@Override public Collection<UpodCondition> getConditions()
{
   List<UpodCondition> rslt = new ArrayList<UpodCondition>();
   for (Map<Object,UpodCondition> m1 : cond_map.values()) {
      rslt.addAll(m1.values());
    }
   return rslt;
}



public UpodTransition addTransition(UpodTransition t)
{
   for (UpodTransition ut : transition_set) {
      if (ut.getName().equals(t.getName())) return ut;
    }
   transition_set.add(t);
   return t;
}

@Override public Collection<UpodTransition> getTransitions()
{
   return transition_set;
}

@Override public boolean hasTransitions()
{
   if (transition_set == null || transition_set.size() == 0) return false;
   return true;
}


@Override public UpodTransition getTransition(UpodParameter p)
{
   return null;
}



/********************************************************************************/
/*										*/
/*	Device handler commands 						*/
/*										*/
/********************************************************************************/

@Override public void addDeviceHandler(UpodDeviceHandler hdlr)
{
   device_handlers.add(hdlr);
}



@Override public void removeDeviceHandler(UpodDeviceHandler hdlr)
{
   device_handlers.remove(hdlr);
}


protected void fireChanged(UpodWorld w)
{
   w.startUpdate();
   try {
      for (UpodDeviceHandler hdlr : device_handlers) {
	 try {
	    hdlr.stateChanged(w,this);
	  }
	 catch (Throwable t) {
	    BasisLogger.logE("Problem with device handler",t);
	  }
       }
    }
   finally {
      w.endUpdate();
    }
}



/********************************************************************************/
/*										*/
/*	Capability commands							*/
/*										*/
/********************************************************************************/

protected UpodCapability addCapability(UpodCapability uc)
{
   for (UpodCapability cc : capability_set) {
      if (cc.getName().equals(uc.getName())) return cc;
    }
   capability_set.add(uc);

   uc.addToDevice(this);

   return uc;
}


public boolean hasCapability(String name)
{
   for (UpodCapability cc : capability_set) {
      if (cc.getName().equals(name)) return true;
    }
   return false;
}



/********************************************************************************/
/*										*/
/*	State update methods							*/
/*										*/
/********************************************************************************/

@Override public final void startDevice()
{
   localStartDevice();

   for (UpodCapability uc : capability_set) {
      uc.startCapability(this);
    }
}



protected void localStartDevice()
{ }



@Override public Object getValueInWorld(UpodParameter p,UpodWorld w)
{
   if (!isEnabled()) return null;
   if (w == null) w = getCurrentWorld();

   if (w.isCurrent()) {
      checkCurrentState();
      return w.getValue(p);
    }
   else {
      updateWorldState(w);
      return w.getValue(p);
    }
}



@Override public void setValueInWorld(UpodParameter p,Object val,UpodWorld w)
{
   if (!isEnabled()) return;
   if (w == null) w = getCurrentWorld();

   val = p.normalize(val);

   UpodParameter timep = getTimeParameter(p);

   Object prev = getValueInWorld(p,w);
   if ((val == null && prev == null) || (val != null && val.equals(prev))) {
      if (w.isCurrent()) return;
      if (timep == null) return;
      Object v = w.getValue(timep);
      if (v == null) return;
      Calendar c = (Calendar) v;
      long tm = c.getTimeInMillis();
      if (tm <= w.getTime()) return;
    }

   w.setValue(p,val);
   if (timep != null) w.setValue(timep,w.getTime());

   BasisLogger.logI("Set " + getName() + "." + p + " = " + val);

   fireChanged(w);
}

protected void checkCurrentState()		{ updateCurrentState(); }
protected void updateCurrentState()		{ }
protected void updateWorldState(UpodWorld w)	{ }

protected UpodParameter getTimeParameter(UpodParameter p)
{
   String nm = p.getName() + "_TIME";
   for (UpodParameter up : parameter_set) {
      if (up.getName().equals(nm)) return up;
    }

   return null;
}

protected UpodParameter createTimeParameter(UpodParameter p)
{
   String nm = p.getName() + "_TIME";
   UpodParameter up = BasisParameter.createTimeParameter(nm,UpodParameter.ParameterType.DATETIME);
   up = addParameter(up);
   return up;
}





@Override public void enable(boolean fg)
{
   if (fg == is_enabled) return;

   UpodWorld w = BasisFactory.getFactory().getCurrentWorld(getUniverse());
   is_enabled = fg;
   fireChanged(w);

   if (fg) updateCurrentState();
}

@Override public boolean isEnabled()		{ return is_enabled ; }



/********************************************************************************/
/*										*/
/*	Transition methods							*/
/*										*/
/********************************************************************************/

@Override public void apply(UpodTransition t,UpodPropertySet ps,
      UpodWorld w) throws UpodActionException
{
   throw new UpodActionException("Transition not allowed");
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("DEVICE");
   xw.field("CLASS",getClass().getName());
   xw.field("NAME",getName());
   xw.field("DESC",getDescription());
   xw.field("LABEL",getLabel());
   xw.field("ID",getUID());
   xw.field("ENABLED",isEnabled());

   outputLocalXml(xw);

   for (UpodParameter p : parameter_set) {
      p.outputXml(xw,null);
    }

   for (UpodTransition ut : transition_set) {
      ut.outputXml(xw);
    }

   for (UpodCapability uc : capability_set) {
      uc.outputXml(xw);
    }

   for (String s : device_groups) {
      xw.textElement("GROUP",s);
    }
   
   xw.end("DEVICE");
}


protected void outputLocalXml(IvyXmlWriter xw)		{ }



/********************************************************************************/
/*										*/
/*	Auxilliary methods for sensors						*/
/*										*/
/********************************************************************************/

protected Timer getTimer()
{
   return BasisWorld.getWorldTimer();
}




}	// end of class BasisDevice




/* end of BasisDevice.java */

