/********************************************************************************/
/*										*/
/*		SmartSignUniverse.java						*/
/*										*/
/*	description of class							*/
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



package edu.brown.cs.upod.smartsign;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;

import org.w3c.dom.*;

import java.util.*;
import java.net.*;
import java.io.*;


public class SmartSignUniverse extends BasisUniverse implements SmartSignConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UpodDevice  phone_sensor;
private UpodDevice  visitor_sensor;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignUniverse(File f,Element xml)
{
   super(f,xml);

   setWebServerPort(8800);

   BasisFactory bf = BasisFactory.getFactory();

   UpodDevice ssd = new SmartSignDisplay(this,null);
   addDevice(ssd);

   SmartSignPresenceSensor ssps = new SmartSignPresenceSensor(this);
   UpodDevice uss = addDevice(ssps);

   UpodDevice ts = bf.createTimedSensor("Stepped out",uss,null,SmartSignPresenceSensor.State.OUT,0,300000);
   addDevice(ts);

   // UpodCondition sout = BasisFactory.getFactory().createTimedCondition(uc,0,5000);
   // sout.setLabel("Stepped out");
   // addBaseCondition(sout);

   UpodDevice ns = new SmartSignOnPhoneSensor(this);
   phone_sensor = addDevice(ns);

   ns = new SmartSignVisitorSensor(this);
   visitor_sensor = addDevice(ns);

   UpodDevice wts = new SmartSignWeatherTempSensor(this);
   wts = addDevice(wts);
   UpodDevice wcs = new SmartSignWeatherCondSensor(this);
   wcs = addDevice(wcs);

   Calendar c = Calendar.getInstance();
   c.set(2016,1,1,3,0);
   UpodDevice inatall = bf.createLatchSensor("In At All",uss,
	 null,SmartSignPresenceSensor.State.IN,c);
   addDevice(inatall);

   SensorHub hub = new SensorHub();
   addHub(hub);

   hub.start();
}

@Override public String getIdentity()
{
   return "smartSign_spr_v1";
}


@Override public String getName()
{
   return SMART_SIGN;
}


@Override public String getLabel()
{
   return "The Sign That Knows What To Say";
}



/********************************************************************************/
/*										*/
/*	Sensor hub								*/
/*										*/
/********************************************************************************/

private class SensorHub extends TimerTask implements UpodHub {

   private UpodWorld current_world;

   private static final String	   SENSOR_HOST = "valerie.cs.brown.edu";
   private static final int	   SENSOR_PORT = 19892;

   SensorHub() {
      current_world = BasisFactory.getFactory().getCurrentWorld(SmartSignUniverse.this);
    }

   void start() {
      String cmd = "ssh valerie -C java spr.automate.MessageChecker &";
      try {
	 new IvyExec(cmd);
       }
      catch (IOException e) { }

      BasisWorld.getWorldTimer().schedule(this,5*T_SECOND,T_MINUTE);
    }

   @Override public void run() {
      try {
         Socket s = new Socket(SENSOR_HOST,SENSOR_PORT);
         OutputStream so = s.getOutputStream();
         so.write("GO\n".getBytes());
         so.flush();
         InputStream si = s.getInputStream();
         Reader r = new InputStreamReader(si);
         char [] buf = new char[10240];
         int ln = r.read(buf);
         if (ln > 0) {
            String rslt = new String(buf,0,ln);
            if (rslt.startsWith("RESULT ")) {
               current_world.startUpdate();
               try {
        	  setSensorState(phone_sensor,rslt.charAt(8) == '1');
        	  setSensorState(visitor_sensor,rslt.charAt(7) == '1');
        	}
               finally {
        	  current_world.endUpdate();
        	}
             }
          }
         s.close();
       }
      catch (IOException e) {
         System.err.printf("Problem getting sensor info",e);
       }
    }

   private void setSensorState(UpodDevice s,boolean fg) {
      UpodParameter p0 = findParameter(s);
      List<Object> sts = p0.getValues();
      if (sts == null) return;
      if (sts.size() < 2) return;
      Object st = (fg ? sts.get(1) : sts.get(0));
      s.setValueInWorld(p0,st,null);
    }

}	// end of inner class SensorHub



}	// end of class SmartSignUniverse




/* end of SmartSignUniverse.java */
