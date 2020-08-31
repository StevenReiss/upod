/********************************************************************************/
/*										*/
/*		BasisUniverse.java						*/
/*										*/
/*	Basic implementation of a universe					*/
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
import java.io.*;
import java.lang.reflect.*;


public abstract class BasisUniverse implements UpodUniverse, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Static creation method							*/
/*										*/
/********************************************************************************/

public static UpodUniverse createUniverse(File file,Class<?> base)
{
   Element xml = IvyXml.loadXmlFromFile(file);

   if (xml != null) {
      String cnm = IvyXml.getAttrString(xml,"CLASS");
      if (cnm == null) xml = null;
      else {
	 try {
	    base = Class.forName(cnm);
	  }
	 catch (ClassNotFoundException e) {
	    xml = null;
	  }
       }
    }

   try {
      Constructor<?> cnst = base.getConstructor(File.class,Element.class);
      Object o = cnst.newInstance(file,xml);
      UpodUniverse uu = (UpodUniverse) o;
      uu.start();
      return uu;
    }
   catch (Exception e) {
      BasisLogger.logE("Problem creating universe " + base.getName(),e);
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private SwingEventListenerList<UpodUniverse.Listener> universe_callbacks;

private List<UpodDevice>	all_devices;
private List<UpodHub>		all_hubs;
private List<UpodCondition>	all_conditions;
private Map<String,BasisCapability> all_capabilities;
private BasisHistoryDeducer	history_deducer;
private BasisAccess		access_info;
private int			port_number;
private boolean 		is_started;

private File			base_file;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisUniverse(File f,Element xml)
{
   universe_callbacks = new SwingEventListenerList<UpodUniverse.Listener>(
	 UpodUniverse.Listener.class);
   all_devices = new ArrayList<UpodDevice>();
   all_hubs = new ArrayList<UpodHub>();
   all_conditions = new ArrayList<UpodCondition>();
   all_capabilities = new HashMap<String,BasisCapability>();
   history_deducer = new BasisHistoryDeducer(this);

   port_number = 0;
   is_started = false;

   loadUniverse(xml);

   base_file = f;

   access_info = new BasisAccess(this);
}




/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

public UpodDevice addDevice(UpodDevice ud)
{
   UpodDevice od = findDevice(ud.getName());
   if (od != null) return od;

   all_devices.add(ud);
   fireDeviceAdded(ud);

   for (UpodCondition c : ud.getConditions()) {
      addBaseCondition(c);
    }

   save();

   // ud.startDevice(); -- done when starting universe

   return ud;
}


protected UpodCondition addBaseCondition(UpodCondition uc)
{
   for (UpodCondition cc : all_conditions) {
      if (cc.getName().equals(uc.getName())) return cc;
    }

   all_conditions.add(uc);
   fireConditionAdded(uc);

   save();

   return uc;
}


public void addHub(UpodHub uh)
{
   all_hubs.add(uh);
}


public void addCapability(BasisCapability bc)
{
   all_capabilities.put(bc.getName(),bc);
}



@Override public void start()
{
   if (is_started) return;
   is_started = true;

   for (UpodDevice ud : all_devices) {
      for (UpodCondition c : ud.getConditions()) {
	 addBaseCondition(c);
       }

      ud.startDevice();
    }
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public Collection<UpodDevice> getDevices()
{
   return new ArrayList<UpodDevice>(all_devices);
}



@Override public Collection<UpodHub> getHubs()
{
   return new ArrayList<UpodHub>(all_hubs);
}


public Collection<UpodCapability> getCapabilities()
{
   return new ArrayList<UpodCapability>(all_capabilities.values());
}



@Override public Collection<UpodCondition> getBasicConditions()
{
   Set<UpodCondition> rslt = new LinkedHashSet<UpodCondition>(all_conditions);

   return rslt;
}


@Override public UpodCondition findBasicCondition(String name)
{
   if (name == null) return null;

   for (UpodCondition uc : all_conditions) {
      if (uc.getName().equalsIgnoreCase(name)) return uc;
    }

   return null;
}


@Override public File getBaseDirectory()
{
   return base_file.getParentFile();
}

BasisHistoryDeducer getHistoryDeducer()
{
   return history_deducer;
}




@Override public UpodDevice findDevice(String id)
{
   if (id == null) return null;

   String id1 = id;
   if (!id.startsWith(getName() + NSEP)) {
      id1 = getName() + NSEP + id;
    }
   for (UpodDevice us : getDevices()) {
      if (us.getName().equals(id)) return us;
      if (us.getUID().equals(id)) return us;
      if (us.getName().equals(id1)) return us;
      if (us.getUID().equals(id1)) return us;
    }

   return null;
}


protected UpodParameter findParameter(UpodDevice ud)
{
   UpodParameter p1 = null;
   for (UpodParameter p : ud.getParameters()) {
      if (p.getName().equals(ud.getUID())) return p;
      if (p1 == null) p1 = p;
    }
   return p1;
}


public @Override BasisCapability findCapability(String id)
{
   return all_capabilities.get(id);
}


@Override public int getWebServerPort()
{
   return port_number;
}


protected void setWebServerPort(int port)
{
   port_number = port;
}



/********************************************************************************/
/*										*/
/*	Authorization methods							     */
/*										*/
/********************************************************************************/

@Override public boolean authorize(String user,String sid,String userkey)
{
   return access_info.authorize(user,sid,userkey);
}


@Override public UpodAccess.Role getRole(String user)
{
   return access_info.getRole(user);
}



/********************************************************************************/
/*										*/
/*	Event management methods						*/
/*										*/
/********************************************************************************/

@Override public void addUniverseListener(UpodUniverse.Listener l)
{
   universe_callbacks.add(l);
}


@Override public void removeUniverseListener(UpodUniverse.Listener l)
{
   universe_callbacks.remove(l);
}


protected void fireDeviceAdded(UpodDevice e)
{
   for (UpodUniverse.Listener ul : universe_callbacks) {
      ul.deviceAdded(this,e);
    }
}

protected void fireDeviceRemoved(UpodDevice e)
{
   for (UpodUniverse.Listener ul : universe_callbacks) {
      ul.deviceRemoved(this,e);
    }
}


protected void fireConditionAdded(UpodCondition c)
{
   for (UpodUniverse.Listener ul : universe_callbacks) {
      ul.conditionAdded(this,c);
    }
}

protected void fireConditionRemoved(UpodCondition c)
{
   for (UpodUniverse.Listener ul : universe_callbacks) {
      ul.conditionRemoved(this,c);
    }
}



/********************************************************************************/
/*										*/
/*	Input/Output methods							*/
/*										*/
/********************************************************************************/

private void save()
{
   if (base_file == null) return;

   try {
      IvyXmlWriter xw = new IvyXmlWriter(base_file);
      outputXml(xw);
      xw.close();
    }
   catch (IOException e) {
      BasisLogger.logE("UPOD: Problem saving universe",e);
    }
}




@Override public void outputXml(IvyXmlWriter xw)
{
   Set<String> groups = new HashSet<String>();

   xw.begin("UNIVERSE");
   xw.field("NAME",getName());
   xw.field("LABEL",getLabel());
   xw.field("ID",getIdentity());
   xw.field("CLASS",getClass().getName());
   xw.field("PORT",port_number);

   for (UpodDevice ue : getDevices()) {
      ue.outputXml(xw);
      groups.addAll(ue.getGroups());
    }

   xw.begin("GROUPS");
   for (String s : groups) {
      xw.textElement("GROUP",s);
    }
   xw.end("GROUPS");

   xw.end("UNIVERSE");
}



private void loadUniverse(Element xml)
{
   port_number = IvyXml.getAttrInt(xml,"PORT",port_number);

   for (Element cxml : IvyXml.children(xml,"CAPABILITY")) {
      String nm = IvyXml.getAttrString(xml,"NAME");
      if (findCapability(nm) == null) {
	 createCapability(cxml);
       }
    }

   for (Element exml : IvyXml.children(xml,"DEVICE")) {
      createDevice(exml);
    }
}


private void createDevice(Element xml)
{
   String cnm = IvyXml.getAttrString(xml,"CLASS");
   try {
      Class<?> c = Class.forName(cnm);
      Object [] args = null;
      Constructor<?> cnst = null;
      for (Constructor<?> cn : c.getConstructors()) {
	 Class<?> [] typs = cn.getParameterTypes();
	 if (typs.length == 0 && args == null) {
	    args = new Object[0];
	    cnst = cn;
	  }
	 else if (typs.length == 1 && (args == null || args.length < 1) &&
	       typs[0].isAssignableFrom(this.getClass())) {
	    args = new Object [] { this };
	    cnst = cn;
	  }
	 else if (typs.length == 2 && (args == null || args.length < 2) &&
	       typs[0].isAssignableFrom(this.getClass()) &&
	       typs[1].isAssignableFrom(Element.class)) {
	    args = new Object [] { this,xml };
	    cnst = cn;
	  }
       }
      if (cnst != null) {
	 Object o = cnst.newInstance(args);
	 UpodDevice ue = (UpodDevice) o;
	 addDevice(ue);
       }
      else {
	 BasisLogger.logE("No constructor found for " + cnm);
       }
    }
   catch (Exception e) {
      BasisLogger.logE("UPOD: probelm creating device",e);
    }
}



protected void createCapability(Element xml)
{
}





}	// end of class BasisUniverse



/* end of BasisUniverse.java */

