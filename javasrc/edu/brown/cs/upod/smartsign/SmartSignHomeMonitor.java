/********************************************************************************/
/*										*/
/*		SmartSignHomeMonitor.java					*/
/*										*/
/*	Application to run at home to signal presence and activities		*/
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
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE  cd ..	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.smartsign;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.json.JSONObject;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyFileLocker;

public class SmartSignHomeMonitor implements SmartSignConstants
{

/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

public static void main(String ... args)
{
   SmartSignHomeMonitor mon = new SmartSignHomeMonitor(args);
   mon.startMonitor();
}



/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private long	last_idle;
private Boolean last_zoom;
private String	last_personal;
private int	last_wait;
private int	cur_wait;
private long	last_error;

private final String IDLE_COMMAND = "sh -c 'ioreg -c IOHIDSystem | fgrep HIDIdleTime'";

private final String ZOOM_COMMAND = "sh -c 'ps -ax | fgrep zoom | fgrep CptHost'";

private final String ALERT_COMMAND = "/usr/bin/osascript -e 'display notification \"You have someone waiting\" with title \"Knock, Knock\" sound name \"Basso\" '";

private final File LOCK_FILE = IvyFile.expandFile("$(HOME)/.smartsignhomemonitor.lock");

private final String STATUS_REQUEST = "https://conifer2.cs.brown.edu:6060/status";
// private final String STATUS_REQUEST = "http://conifer2.cs.brown.edu:6061/status";




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

private SmartSignHomeMonitor(String [] args)
{
   last_idle = -1;
   last_zoom = null;
   last_personal = null;
   last_wait = 0;
   cur_wait = 0;
   last_error = 0;
}



/********************************************************************************/
/*										*/
/*	Run the monitor 							*/
/*										*/
/********************************************************************************/

private void startMonitor()
{
   IvyFileLocker locker = new IvyFileLocker(LOCK_FILE);
   if (!locker.tryLock()) {
      System.exit(0);
    }

   int tries = 32;

   for ( ; ; ) {
      int port = SMART_SIGN_MONITOR_PORT + tries - 1;
      Socket s = null;
      try  {
	 for (int i = 0; i < tries; ++i) {
	    System.err.println("TRY PORT " + port);
	    try {
	       s = new Socket(SMART_SIGN_HOST,port);
	       System.err.println("CONNECT SUCCESS " + port);
	       break;
	     }
	    catch (SocketException e) {
	       port -= 1;
	       s = null;
	     }
	    catch (SocketTimeoutException e) {
	       port -= 1;
	       s = null;
	     }
	  }
	 if (s != null) {
	    last_idle = -1;
	    last_zoom = null;
	    try (OutputStream so = s.getOutputStream()) {
	       PrintWriter pw = new PrintWriter(so);
	       for ( ; ; ) {
		  sendUpdate(pw);
		  if (pw.checkError()) {
		     System.err.println("ERROR DETECTED");
		     break;
		   }
		  try {
		     Thread.sleep(30000);
		   }
		  catch (InterruptedException e) { }
		}
	     }
	  }
       }
      catch (IOException e) {
	 System.err.println("Connect exception: " + e);
       }
      if (s != null) {
	 try {
	    s.close();
	    s = null;
	  }
	 catch (IOException e) { }
       }
      try {
	 Thread.sleep(30000);
       }
      catch (InterruptedException e) { }
      //    if success, send updates every 30 seconds until fail
    }
}


private void sendUpdate(PrintWriter pw)
{
   boolean write = false;
   long idle = getIdleTime();
   Boolean zoom = usingZoom();
   String psts = getPersonalStatus();
   System.err.println("CHECK " + idle + " " + zoom + " " + psts);
   if (last_error > 0) {
      // force write if there was an error previously
      if (System.currentTimeMillis() - last_error > 5*60*1000) write = true;
    }

   if (idle > 0) {
      if (idle < 300) {
	 if (last_idle >= 300 || last_idle < 0) {
	    pw.println("HomePresenceSensor=WORKING");
	    write = true;
	  }
       }
      else if (idle < 3600) {
	 if (last_idle >= 3600 || last_idle < 300) {
	    pw.println("HomePresenceSensor=IDLE");
	    write = true;
	  }
       }
      else if (last_idle < 3600) {
	 pw.println("HomePresenceSensor=AWAY");
	 write = true;
       }
      last_idle = idle;
    }
   if (zoom != null) {
      if (zoom != last_zoom) {
	 pw.println("HomeZoomSensor=" + (zoom ? "ON_ZOOM" : "NOT_ON_ZOOM"));
	 write = true;
       }
      last_zoom = zoom;
    }
   if (psts != null) {
      if (last_personal == null || !psts.equals(last_personal)) {
	 pw.println("ZoomPersonalMeeting=" + psts);
	 write = true;
       }
      last_personal = psts;
    }
   if (write) {
      pw.flush();
      if (pw.checkError()) {
	 System.err.println("SEND UPDATE ERROR");
	 last_error = System.currentTimeMillis();
       }
      else {
	 System.err.println("SEND UPDATE OK");
	 write = false;
	 last_error = 0;
       }
    }

   if (cur_wait != last_wait) {
      if (cur_wait > 0 && last_wait == 0) {
	 try {
	    IvyExec ex = new IvyExec(ALERT_COMMAND);
	    ex.waitFor();
	  }
	 catch (IOException e) {
	    System.err.println("SMARTSIGN: Problem sending alert: " + e);
	  }
       }
      last_wait = cur_wait;
    }
}



private long getIdleTime()
{
   try {
      IvyExec ex = new IvyExec(IDLE_COMMAND,IvyExec.READ_OUTPUT);
      InputStreamReader isr = new InputStreamReader(ex.getInputStream());
      try (BufferedReader br = new BufferedReader(isr)) {
	 for ( ; ; ) {
	    String ln = br.readLine();
	    if (ln == null) break;
	    if (ln.contains("HIDIdleTime")) {
	       int idx = ln.indexOf("=");
	       if (idx < 0) continue;
	       String nums = ln.substring(idx+1).trim();
	       long lv = Long.parseLong(nums);
	       lv /= 1000000000;
	       return lv;
	     }
	  }
       }
    }
   catch (IOException e) { }

   return -1;
}




private Boolean usingZoom()
{
   try {
      IvyExec ex = new IvyExec(ZOOM_COMMAND,IvyExec.READ_OUTPUT);
      InputStreamReader isr = new InputStreamReader(ex.getInputStream());
      try (BufferedReader br = new BufferedReader(isr)) {
	 for ( ; ; ) {
	    String ln = br.readLine();
	    if (ln == null) break;
	    if (ln.contains("sh -c")) continue;
	    if (ln.contains("zoom") && ln.contains("CptHost")) {
	       return true;
	     }
	  }
	 return false;
       }
    }
   catch (IOException e) { }

   return null;
}


private String getPersonalStatus()
{
   String status = null;

   try {
      URL u = new URL(STATUS_REQUEST);
      HttpURLConnection hc = (HttpURLConnection) u.openConnection();
      hc.setReadTimeout(5000);
      hc.setConnectTimeout(5000);
      hc.setUseCaches(false);
      hc.setInstanceFollowRedirects(true);
      hc.connect();
      Reader ins = new InputStreamReader(hc.getInputStream());
      String prslt = IvyFile.loadFile(ins);
      ins.close();
      JSONObject obj = new JSONObject(prslt);
      int otherct = obj.getInt("in_other");
      Object perobj = obj.get("in_personal");
      boolean personal = false;
      if (perobj instanceof Boolean) personal = ((Boolean) perobj).booleanValue();
      else if (perobj instanceof Number) personal = ((Number) perobj).intValue() > 0;
      boolean active = obj.getBoolean("personal_active");
      int waitct = obj.getInt("wait_count");
      cur_wait = waitct;
      int activect = obj.getInt("active_count");
      System.err.println("STATUS : " + active + " " + personal + " " + otherct + " " + waitct + " " + activect);
      if (!active || !personal || otherct > 0) status = "NOT_ACTIVE";
      else if (activect > 0 || waitct > 0) status = "BUSY";
      else status = "ACTIVE";
    }
   catch (IOException e) {
      System.err.println("Problem accessing personal status: " + e);
    }

   return status;			
}



}	// end of class SmartSignHomeMonitor




/* end of SmartSignHomeMonitor.java */

