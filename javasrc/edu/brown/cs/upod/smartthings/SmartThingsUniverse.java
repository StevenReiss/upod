/********************************************************************************/
/*										*/
/*		SmartThingsUniverse.java					*/
/*										*/
/*	Universe of objects for SmartThings					*/
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



package edu.brown.cs.upod.smartthings;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.upod.basis.BasisLogger;
import edu.brown.cs.upod.basis.BasisUniverse;
import edu.brown.cs.upod.upod.UpodCapability;
import edu.brown.cs.upod.upod.UpodDevice;


public class SmartThingsUniverse extends BasisUniverse implements SmartThingsConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	access_token;
private String	api_address;
private Map<String,SmartThingsDevice> device_map;
private JSONObject api_endpoint;

private static Map<String,String> known_capabilities;


static {
   known_capabilities = new HashMap<String,String>();
   known_capabilities.put("acceleration", "Acceleration Sensor");
   known_capabilities.put("actuator", "Actuator");
   known_capabilities.put("alarm", "Alarm");
   known_capabilities.put("battery", "Battery");
   known_capabilities.put("beacon", "Beacon");
   known_capabilities.put("button", "Button");
   known_capabilities.put("carbonMonoxideDetector", "Carbon Monoxide Detector");
   known_capabilities.put("colorControl", "Color Control");
   known_capabilities.put("configuration", "Configuration");
   known_capabilities.put("contact", "Contact Sensor");
   known_capabilities.put("doorControl", "Door Control");
   known_capabilities.put("energyMeter", "Energy Meter");
   known_capabilities.put("illuminanceMeasurement", "Illuminance Measurement");
   known_capabilities.put("lock", "Lock");
   known_capabilities.put("momentary", "Momentary");
   known_capabilities.put("motion", "Motion Sensor");
   known_capabilities.put("notification", "Notification");
   known_capabilities.put("polling", "Polling");
   known_capabilities.put("powerMeter", "Power Meter");
   known_capabilities.put("presence", "Presence Sensor");
   known_capabilities.put("refresh", "Refresh");
   known_capabilities.put("relativeHumidityMeasurement", "Relative Humidity Measurement");
   known_capabilities.put("relaySwitch", "Relay Switch");
   known_capabilities.put("sensor", "Sensor");
   known_capabilities.put("signalStrength", "Signal Strength");
   known_capabilities.put("sleepSensor", "Sleep Sensor");
   known_capabilities.put("smokeDetector", "Smoke Detector");
   known_capabilities.put("stepSensor", "Step Sensor");
   known_capabilities.put("switch", "Switch");
   known_capabilities.put("temperature", "Temperature Measurement");
   known_capabilities.put("thermostatCoolingSetpoint", "Thermostat Cooling Setpoint");
   known_capabilities.put("thermostatFanMode", "Thermostat Fan Mode");
   known_capabilities.put("thermostatHeatingSetpoint", "Thermostat Heating Setpoint");
   known_capabilities.put("thermostatMode", "Thermostat Mode");
   known_capabilities.put("thermostatOperatingState", "Thermostat Operating State");
   known_capabilities.put("thermostatSetpoint", "Thermostat Setpoint");
   known_capabilities.put("threeAxis", "ThreeAxis");
   known_capabilities.put("tone", "Tone");
   known_capabilities.put("touch", "Touch Sensor");
   known_capabilities.put("valve", "Valve");
   known_capabilities.put("waterSensor", "Water Sensor");
}




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartThingsUniverse(File f,Element xml)
{
   super(f,xml);

   setWebServerPort(8802);

   SmartThingsCapability.addCapabilities(this);

   loadConnectionData();

   loadUniverse();

   SmartThingsPoller poller = new SmartThingsPoller();
   poller.start();
}



private void loadConnectionData()
{
   try {
      File f = new File(SMART_THINGS_DATA);
      String data = IvyFile.loadFile(f);
      JSONObject obj = new JSONObject(data);
      access_token = obj.getString("access_token");
      api_address = obj.getString("api");
      // api_location = obj.getString("api_location");
      // client_id = obj.getString("client_id");
      // client_secret = obj.getString("client_secret");
    }
   catch (IOException e) {
      BasisLogger.logE("Couldn't load SmartThings connection data",e);
      System.exit(1);
    }
}


/********************************************************************************/
/*										*/
/*     Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getIdentity()
{
   return "smartThings_spr_v1";
}



@Override public String getName()		{ return SMART_THINGS_NAME; }

@Override public String getLabel()
{
   return "The Smarter Interface for SmartThings";
}



/********************************************************************************/
/*										*/
/*	Load the initial universe						*/
/*										*/
/********************************************************************************/

private void loadUniverse()
{
   // first find the endpoints
   String q = api_address + "?access_token=" + access_token;
   JSONArray endpoints = sendArrayRequest("GET",q);
   if (endpoints == null || endpoints.length() == 0) {
      BasisLogger.logE("SMARTTHINGS: No endpoint found");
      System.exit(1);
    }
   api_endpoint = endpoints.getJSONObject(0);

   device_map = new HashMap<String,SmartThingsDevice>();
   for (UpodDevice ud : getDevices()) {
      if (ud instanceof SmartThingsDevice) {
	 SmartThingsDevice std = (SmartThingsDevice) ud;
	 device_map.put(std.getSTId(),std);
       }
    }

   List<SmartThingsDevice> adddevices = new ArrayList<SmartThingsDevice>();

   for (UpodCapability uc : getCapabilities()) {
      if (!(uc instanceof SmartThingsCapability)) continue;
      SmartThingsCapability stc = (SmartThingsCapability) uc;
      String key = stc.getAccessName();
      String q1 = "https://graph.api.smartthings.com" + api_endpoint.getString("url") + "/" + key;
      BasisLogger.logD("SEND: " + q1);

      JSONArray devs = sendArrayRequest("GET",q1);
      for (int i = 0; i < devs.length(); ++i) {
	 JSONObject devobj = devs.getJSONObject(i);
	 BasisLogger.logD("DEVICE: " + devobj);
	 String id = devobj.getString("id");
	 JSONObject value = devobj.getJSONObject("value");
	 String lbl = devobj.getString("label");
	 SmartThingsDevice bd = device_map.get(id);
	 if (bd == null) {
	    bd = new SmartThingsDevice(this,lbl,id);
	    adddevices.add(bd);
	    device_map.put(id,bd);
	  }
	 bd.addSTCapability(uc);
	 bd.handleValue(stc,value);
       }
    }

   for (SmartThingsDevice std : adddevices) {
      addDevice(std);
    }

   for (UpodDevice ud : getDevices()) {
      if (ud instanceof SmartThingsDevice) {
	 SmartThingsDevice std = (SmartThingsDevice) ud;
	 if (device_map.get(std.getSTId()) == null) {
	    ud.enable(false);
	  }
       }
    }
}


/********************************************************************************/
/*										*/
/*	Communications methods							*/
/*										*/
/********************************************************************************/

JSONArray sendArrayRequest(String method,String rqst)
{
   String json = sendRequest(method,rqst);
   if (json == null) return null;
   return new JSONArray(json);
}


JSONObject sendObjectRequest(String method,String rqst)
{
   String json = sendRequest(method,rqst);
   if (json == null) return null;
   return new JSONObject(json);
}



synchronized void sendCommand(String type,SmartThingsDevice std,JSONObject rslt)
{
   String urlstr = "https://graph.api.smartthings.com" + api_endpoint.getString("url") + "/" + type;
   urlstr += "/" + std.getSTId();

   String cnts = rslt.toString();
   try {
      URL u = new URL(urlstr);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setRequestMethod("PUT");
      conn.setDoOutput(true);
      conn.setDoInput(true);
      conn.setRequestProperty("Accept","application/json");
      conn.setRequestProperty("Content-Type","application/json");
      conn.setRequestProperty("Content-Length",Integer.toString(cnts.length()));
      conn.setRequestProperty("User-Agent","smarthab");
      conn.setRequestProperty("Authorization","Bearer " + access_token);
      OutputStreamWriter ots = new OutputStreamWriter(conn.getOutputStream());
      ots.write(cnts);
      ots.close();
      InputStreamReader ins = new InputStreamReader(conn.getInputStream());
      String json = IvyFile.loadFile(ins);
      BasisLogger.logD("COMMAND RESULT = " + json);
      ins.close();
    }
   catch (IOException e) {
      BasisLogger.logE("Problem sending command: " + e,e);
    }

   BasisLogger.logD("TRY: curl -H 'Authorization: Bearer " + access_token + "' " +
				  urlstr + " -X PUT -d '" + cnts + "'");

   BasisLogger.logD("SEND: " + urlstr);

}



synchronized String sendRequest(String method,String rqst)
{
   try {
      String urlstr = rqst;
      URL u = new URL(urlstr);
      HttpURLConnection conn = (HttpURLConnection) u.openConnection();
      conn.setRequestMethod(method);
      conn.setDoOutput(false);
      conn.setDoInput(true);
      conn.setRequestProperty("Accept","application/json");
      conn.setRequestProperty("User-Agent","smarthab");
      conn.setRequestProperty("Authorization","Bearer " + access_token);
      InputStreamReader ins = new InputStreamReader(conn.getInputStream());
      String json = IvyFile.loadFile(ins);
      ins.close();
      // BasisLogger.logD("RECEIVED:\n" + json);
      return json;
    }
   catch (IOException e) {
      BasisLogger.logE("SMARTTHINGS: I/O problem with server for " + method + " " + rqst,e);
    }

   return null;
}




/********************************************************************************/
/*										*/
/*	Device polling								*/
/*										*/
/********************************************************************************/

private class SmartThingsPoller extends Thread {

   SmartThingsPoller() {
      super("SmartThings Polling thread");
      setDaemon(true);
    }

   @Override public void run() {
      int errct = 0;
      while (errct < 100) {
	 String qp = "https://graph.api.smartthings.com" + api_endpoint.getString("url") + "/x/y/poll";
	 JSONArray prslt = sendArrayRequest("POST",qp);
	 if (prslt == null) ++errct;
	 else errct = 0;
	 if (prslt != null && prslt.length() > 0) {
	    for (int i = 0; i < prslt.length(); ++i) {
	       JSONObject poll = prslt.getJSONObject(i);
	       try {
		  String id = poll.getString("id");
		  String lbl = poll.getString("label");
		  String typ = poll.optString("type");
		  if (typ == null) {
		     BasisLogger.logE("Problem parsing result (no type): " + poll);
		   }
		  JSONObject val = poll.getJSONObject("value");
		  SmartThingsDevice std = device_map.get(id);
		  if (std == null) std = (SmartThingsDevice) findDevice(lbl);
		  if (std != null && typ != null) {
		     for (UpodCapability uc : std.getCapabilities()) {
			if (uc instanceof SmartThingsCapability) {
			   SmartThingsCapability stc = (SmartThingsCapability) uc;
			   if (typ.equals(stc.getAccessName())) {
			      std.handleValue(stc,val);
			      break;
			    }
			 }
		      }
		   }
		}
	       catch (Throwable t) {
		  BasisLogger.logE("Problem parsing result: " + poll,t);
		}
	     }
	    BasisLogger.logD("POLL YIELDS: " + prslt);
	  }
	 try {
	    Thread.sleep(333);
	  }
	 catch (InterruptedException e) { }
       }
      BasisLogger.logE("Exited due to too many errors");
   }
}


}	// end of class SmartThingsUniverse




/* end of SmartThingsUniverse.java */

