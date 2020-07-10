/********************************************************************************/
/*                                                                              */
/*              SmartSignHomeMonitor.java                                       */
/*                                                                              */
/*      Application to run at home to signal presence and activities            */
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



package edu.brown.cs.upod.smartsign;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.IvyFile;
import edu.brown.cs.ivy.file.IvyFileLocker;

public class SmartSignHomeMonitor implements SmartSignConstants
{



/********************************************************************************/
/*                                                                              */
/*      Main program                                                            */
/*                                                                              */
/********************************************************************************/

public static void main(String ... args) 
{
   SmartSignHomeMonitor mon = new SmartSignHomeMonitor(args);
   mon.start();
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private long    last_idle;
private Boolean last_zoom;

private final String IDLE_COMMAND = "sh -c 'ioreg -c IOHIDSystem | fgrep HIDIdleTime'";

private final String ZOOM_COMMAND = "sh -c 'ps -ax | fgrep zoom | fgrep caphost'";

private final File LOCK_FILE = IvyFile.expandFile("$(HOME)/.smartsignhomemonitor.lock");


        

/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

private SmartSignHomeMonitor(String [] args)
{
   last_idle = -1;
   last_zoom = null;
}



/********************************************************************************/
/*                                                                              */
/*      Run the monitor                                                         */
/*                                                                              */
/********************************************************************************/

@SuppressWarnings("resource")
private void start()
{
   IvyFileLocker locker = new IvyFileLocker(LOCK_FILE);
   if (!locker.tryLock()) {
      System.exit(0);
    }
   
   for ( ; ; ) {
      try (Socket s = new Socket(SMART_SIGN_HOST,SMART_SIGN_MONITOR_PORT)) {
         last_idle = -1;
         last_zoom = null;
         try (OutputStream so = s.getOutputStream()) {
            PrintWriter pw = new PrintWriter(so);
            for ( ; ; ) {
               sendUpdate(pw);
               try {
                  Thread.sleep(30000);
                }
               catch (InterruptedException e) { }
             }
          }
       }
      catch (IOException e) { }
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
   if (idle > 0) {
      if (idle < 30) {
         if (last_idle >= 30 || last_idle < 0) {
            pw.println("HomePresenceSensor=WORKING");
            write = true;
          }
       }
      else if (last_idle < 30) {
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
   if (write) 
      pw.flush();
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
            if (ln.contains("zoom") && ln.contains("caphost")) {
               return true;
             }
          }
         return false;
       }
    }
   catch (IOException e) { }
   
   return null;
}

}       // end of class SmartSignHomeMonitor




/* end of SmartSignHomeMonitor.java */

