/********************************************************************************/
/*										*/
/*		SmartHabUniverse.java						*/
/*										*/
/*	Universe definitions for smart hab interface				*/
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



package edu.brown.cs.upod.smarthab;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.swing.SwingColorSet;
import edu.brown.cs.upod.basis.BasisCapability;
import edu.brown.cs.upod.basis.BasisLogger;
import edu.brown.cs.upod.basis.BasisParameter;
import edu.brown.cs.upod.basis.BasisUniverse;
import edu.brown.cs.upod.upod.UpodDevice;
import edu.brown.cs.upod.upod.UpodParameter;

public class SmartHabUniverse extends BasisUniverse implements SmartHabConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	openhab_host;;
private int	openhab_port;
private String	openhab_username;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartHabUniverse(File f,Element xml)
{
   super(f,xml);

   setWebServerPort(8801);

   String host = System.getenv("OPENHAB_HOST");
   if (host == null) host = DEFAULT_HOST;
   String portnm = System.getenv("OPENHAB_PORT");
   int port = DEFAULT_PORT;
   if (portnm != null) {
      try {
	 port = Integer.parseInt(portnm);
       }
      catch (NumberFormatException e) { }
    }
   String user = System.getenv("OPENHAB_USER");
   if (user == null) user = DEFAULT_USER;

   openhab_host = IvyXml.getAttrString(xml,"OPENHAB_HOST",host);
   openhab_port = IvyXml.getAttrInt(xml,"OPENHAB_PORT",port);
   openhab_username = IvyXml.getAttrString(xml,"OPENHAB_USER",user);

   SmartHabCapability.addCapabilities(this);

   loadUniverse();

   OpenHabPoller poller = new OpenHabPoller();
   poller.start();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getIdentity()
{
   return "smartHab_spr_v1";
}

@Override public String getName()
{
   return SMART_HAB_NAME;
}


@Override public String getLabel()
{
   return "The Smart interface for OpenHAB";
}


/********************************************************************************/
/*										*/
/*	Load the initial universe						*/
/*										*/
/********************************************************************************/

private void loadUniverse()
{
   Set<UpodDevice> found = new HashSet<UpodDevice>();
   Map<String,WidgetData> widgettypes = new HashMap<String,WidgetData>();

   Element items = sendRequest("GET","items");
   Element sitemaps = sendRequest("GET","sitemaps");
   for (Element smap : IvyXml.children(sitemaps,"sitemap")) {
      String name = IvyXml.getTextElement(smap,"name");
      Element smapinfo = sendRequest("GET","sitemaps/" + name);
      for (Element wid : IvyXml.elementsByTag(smapinfo,"widget")) {
	 String typ = IvyXml.getTextElement(wid,"type");
	 if (typ == null) continue;
	 WidgetData wd = new WidgetData(typ);
	 int i = 0;
	 for (Element melt : IvyXml.children(wid,"mapping")) {
	    String cmd = IvyXml.getTextElement(melt,"command");
	    String lbl = IvyXml.getTextElement(melt,"label");
	    wd.addMapping(cmd,lbl);
	  }
	 if (i > 0) typ += "]";
	 String minv = IvyXml.getTextElement(wid,"minValue");
	 String maxv = IvyXml.getTextElement(wid,"maxValue");
	 if (minv != null && maxv != null) {
	    try {
	       int minvl = Integer.parseInt(minv);
	       int maxvl = Integer.parseInt(maxv);
	       wd.setRange(minvl,maxvl);
	     }
	    catch (NumberFormatException e) { }
	  }

	 for (Element itm : IvyXml.children(wid,"item")) {
	    String itmname = IvyXml.getTextElement(itm,"name");
	    if (itmname != null) {
	       WidgetData owd = widgettypes.get(itmname);
	       if (owd != null) wd.mergeWith(owd);

	       widgettypes.put(itmname,wd);
	     }
	  }
       }
    }

   for (Element itm : IvyXml.children(items,"item")) {
      System.err.println("ITEM: " + IvyXml.convertXmlToString(itm));
      String typ = IvyXml.getTextElement(itm,"type");
      String nam = IvyXml.getTextElement(itm,"name");
      String state = IvyXml.getTextElement(itm,"state");
      WidgetData wdata = widgettypes.get(nam);
      SmartHabDevice ud = (SmartHabDevice) findDevice(nam);
      if (ud != null) {
	 UpodParameter bp = ud.findParameter("state");
	 if (bp != null) ud.setValueInWorld(bp,state,null);
       }
      else {
	 ud = createDevice(typ,wdata,nam,state);
	 if (ud != null) addDevice(ud);
       }
      if (ud != null) {
	 found.add(ud);
	 UpodParameter param = ud.findParameter("state");
	 state = ud.convertOpenHabToSmartHab(state);
	 if (param != null) {
	    ud.setValueInWorld(param,state,null);
	  }
       }
    }

   // TODO : create hierarchy by looking at group objects and names
   // then set the hierarchy and group values for each item by using
   // underscores in its name.

   for (UpodDevice ud : getDevices()) {
      if (!found.contains(ud)) {
	 ud.enable(false);
       }
    }

   System.err.println("DONE LOADING");
   sendRequest("GET","items/Weather_Temperature");
   sendTextRequest("GET","items/Weather_Temperature/state");
}




private SmartHabDevice createDevice(String typ,WidgetData wdata,String nam,String state)
{
   SmartHabDevice dev = new SmartHabDevice(this,nam);

   BasisCapability cap = findCapability(typ,nam,wdata);
   if (cap != null) {
      cap.addToDevice(dev);
    }

   return dev;
}


private BasisCapability findCapability(String typ,String nam,WidgetData wdata)
{
   BasisCapability cap = findCapability(typ);

   switch (typ) {
      case "GroupItem" :
	 return null;
      case "NumberItem" :
	 if (wdata != null && wdata.getType().equals("Setpoint")) {
	    Point2D rng = wdata.getRange();
	    if (rng == null) rng = new Point2D.Double(-100,100);
	    cap = SmartHabCapability.findTemperatureSetCapability(this,rng.getX(),rng.getY());
	  }
	 else if (wdata != null && wdata.getMapping() != null) {
	    Map<String,String> values = wdata.getMapping();
	    switch (wdata.getType()) {
	       case "Switch" :
	       case "Selection" :
		  cap = SmartHabCapability.findValueSwitchCapability(this,values);
		  break;
	       default :
		  cap = SmartHabCapability.findValuesCapability(this,values);
		  break;
	     }
	  }
	 else if (wdata != null && wdata.getRange() != null) {
	    Point2D rng = wdata.getRange();
	    cap = SmartHabCapability.findNumberRangeCapability(this,rng.getX(),rng.getY());
	  }
	 else if (nam.contains("Temperature")) {
	    cap = findCapability("TemperatureItem");
	  }
	 break;
      default :
	 if (cap == null) BasisLogger.logE("No capability for " + typ + " " + nam);
	 break;
    }

   return cap;
}




/********************************************************************************/
/*										*/
/*	Communicate with the server						*/
/*										*/
/********************************************************************************/

Element sendRequest(String method,String rqst)
{
   try {
      String host = openhab_host;
      if (openhab_username != null) host = openhab_username + "@" + openhab_host;
      URL u = new URL("http",host,openhab_port,"/rest/" + rqst);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setRequestMethod(method);
      conn.setDoOutput(false);
      conn.setDoInput(true);
      conn.setRequestProperty("Accept","application/xml");
      conn.setRequestProperty("User-Agent","smarthab");
      InputStream ins = conn.getInputStream();
      Element xml = IvyXml.loadXmlFromStream(ins);
      ins.close();
      System.err.println("RECEIVED:\n" + IvyXml.convertXmlToString(xml));
      return xml;
    }
   catch (IOException e) {
      System.err.println("SMARTHAB: I/O problem with server: " + e);
      e.printStackTrace();
    }

   return null;
}



String sendTextRequest(String method,String rqst)
{
   try {
      URL u = new URL("http",openhab_host,openhab_port,"/rest/" + rqst);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setRequestMethod(method);
      conn.setDoOutput(false);
      conn.setDoInput(true);
      conn.setRequestProperty("Accept","text/plain");
      conn.setRequestProperty("User-Agent","smarthab");
      InputStream ins = conn.getInputStream();
      String rslt = IvyFile.loadFile(new InputStreamReader(ins));
      ins.close();
      System.err.println("RECEIVED:\n" + rslt);
      return rslt;
    }
   catch (IOException e) {
      System.err.println("SMARTHAB: I/O problem with server: " + e);
      e.printStackTrace();
    }

   return null;
}



void sendCommand(String rqst)
{
   try {
      URL u = new URL("http",openhab_host,openhab_port,"/CMD?" + rqst);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setRequestMethod("GET");
      conn.setDoOutput(false);
      conn.setDoInput(true);
      conn.setRequestProperty("Accept","text/plain");
      conn.setRequestProperty("User-Agent","smarthab");
      conn.connect();
      conn.disconnect();
      InputStream ins = conn.getInputStream();
      String rslt = IvyFile.loadFile(new InputStreamReader(ins));
      ins.close();
      BasisLogger.logD("SMARTHAB: RECEIVED:\n" + rslt);
    }
   catch (IOException e) {
      BasisLogger.logE("SMARTHAB: I/O problem with server",e);
    }
}



/********************************************************************************/
/*										*/
/*	Poll for changes							*/
/*										*/
/********************************************************************************/

private class OpenHabPoller extends Thread
{
   private int request_counter;

   OpenHabPoller() {
      super("OpenHabPoller");
      setDaemon(true);
      request_counter = 0;
    }

   @Override public void run() {
      for ( ; ; ) {
	 try {
	    String host = openhab_host;
	    if (openhab_username != null) host = openhab_username + "@" + openhab_host;
	    URL u = new URL("http",host,openhab_port,"/rest/items" );
	    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
	    conn.setRequestMethod("GET");
	    conn.setDoOutput(false);
	    conn.setDoInput(true);
	    conn.setRequestProperty("Accept","application/xml");
	    conn.setRequestProperty("User-Agent","smarthab");
	    conn.setRequestProperty("X-Atmosphere-Transport","long-polling");
	    conn.setRequestProperty("X-Atmosphere-tracking-id","SMARTHAB_" + request_counter++);
	    InputStream ins = conn.getInputStream();
	    Element xml = IvyXml.loadXmlFromStream(ins);
	    checkForUpdates(xml);
	    ins.close();
	  }
	 catch (IOException e) {
	    BasisLogger.logE("SMARTHAB: I/O problem with server",e);
	  }
       }
    }

   private void checkForUpdates(Element xml) {
      if (IvyXml.isElement(xml,"item")) checkItemForUpdate(xml);
      else {
	 for (Element itm : IvyXml.elementsByTag(xml,"item")) {
	    checkItemForUpdate(itm);
	  }
       }
    }


   private void checkItemForUpdate(Element itm)
   {
      String name = IvyXml.getTextElement(itm,"name");
      String state = IvyXml.getTextElement(itm,"state");
      SmartHabDevice d = (SmartHabDevice) findDevice(name);
      if (d == null) return;
      state = d.convertOpenHabToSmartHab(state);
      if (state == null) return;
      if (state.equals("Undefined")) return;
      if (state.equals("Uninitialized")) return;
      UpodParameter bp = d.findParameter("state");
      if (bp != null) {
	 Object val = d.getValueInWorld(bp,null);
	 if (val == null) val = "";
	 if (state.equals(val.toString())) return;
	 if (val instanceof Double) {
	    try {
	       Double nd = Double.parseDouble(state);
	       if (nd.equals(val)) return;
	     }
	    catch (NumberFormatException e) {
	       return;
	     }
	  }
	 if (val instanceof Integer) {
	    try {
	       Integer nd = Integer.parseInt(state);
	       if (nd.equals(val)) return;
	     }
	    catch (NumberFormatException e) {
	       return;
	     }
	  }
	 if (val instanceof Calendar) {
	    Calendar c1 = BasisParameter.createCalendar(state);
	    if (val.equals(c1)) return;
	  }
	 if (val instanceof Color) {
	    Color c1 = SwingColorSet.getColorByName(state);
	    if (val.equals(c1)) return;
	  }
	 if (val instanceof Point2D) {
	    Point2D p1 = BasisParameter.createLocation(state);
	    if (val.equals(p1)) return;
	  }
	 BasisLogger.logD("COMPARE " + state + " :: " + val);
	 d.setValueInWorld(bp,state,null);
       }
   }
}

}	// end of class SmartHabUniverse




/* end of SmartHabUniverse.java */

