/********************************************************************************/
/*                                                                              */
/*              BasisHistoryDeducer.java                                        */
/*                                                                              */
/*      Learn from the user's history                                           */
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

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.Element;

import java.util.*;
import java.io.*;

class BasisHistoryDeducer implements BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private UpodUniverse    for_universe;
private long            last_update;
private SortedSet<SensorEvent> event_set;

private static final long  UPDATE_EVERY = T_DAY;
private static final long  TEMPORARY_TIME = T_MINUTE*5;
private static final long  DELTA_TIME = T_MINUTE*10;
// private static final long  MAX_TIME = T_HOUR*3;





/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisHistoryDeducer(UpodUniverse uu)
{
   for_universe = uu;
   last_update = 0;
   event_set = null;
}



/********************************************************************************/
/*                                                                              */
/*      Find information about time                                             */
/*                                                                              */
/********************************************************************************/

BasisCalendarEvent findImpliedEvent(long when)
{
   Calendar whenc = Calendar.getInstance();
   whenc.setTimeInMillis(when);
   
   // need to ensure base < ende mod T_DAY
   // need to ensur base < baseee mod T_DAY
   
   SensorEvent base = new SensorEvent(when - DELTA_TIME);
   SensorEvent basee = new SensorEvent(when + DELTA_TIME);
   
   buildEventSet();
   if (event_set == null) return null;
   
   int [] daycts = new int[7];
   for (int i = 0; i < 7; ++i) daycts[i] = 0;
   
   long whenstart = when - when % T_DAY;
   long tot = whenstart;
   int ntot = 1;
   for (SensorEvent se : event_set.subSet(base,basee)) {
      long etime = se.getTimeInMillis();
      long start = etime % T_DAY;
      if (start <= whenstart) {
         tot += start;
         ++ntot;
       }
      Calendar ctime = se.getTime();
      int day = ctime.get(Calendar.DAY_OF_WEEK) - 1;
      daycts[day]++;
    }
   tot = tot/ntot;
   
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Method to build event set for future processing                         */
/*                                                                              */
/********************************************************************************/

private synchronized void buildEventSet()
{
   long now = System.currentTimeMillis();
   if (event_set != null && now - last_update < UPDATE_EVERY) return;
   
   SortedSet<SensorEvent> newset = new TreeSet<SensorEvent>();
   Map<UpodParameter,SensorEvent> usemap = new HashMap<UpodParameter,SensorEvent>();
   String fnm = HISTORY_FILE + "." + for_universe.getName() + ".xml";
   
   try (IvyXmlReader xr = new IvyXmlReader(new FileReader(fnm))) {
      for ( ; ; ) {
         String xmls = xr.readXml();
         if (xmls == null) break;
         Element xml = IvyXml.convertStringToXml(xmls);
         if (xml == null) continue;
         if (!IvyXml.isElement(xml,"CHANGE")) continue;
         SensorEvent se = new SensorEvent(for_universe,xml);
         if (se.isValid() && !se.getParameter().isContinuous()) {
            UpodParameter up = se.getParameter();
            SensorEvent last = usemap.get(up);
            if (last != null) {
               if (se.getTimeInMillis() - last.getTimeInMillis() > TEMPORARY_TIME) {
                  newset.add(last);
                }
             }
            usemap.put(up,se);
          }
       }
      for (SensorEvent se : usemap.values()) {
         if (se.getTimeInMillis() - now > TEMPORARY_TIME) newset.add(se);
       }
    }
   catch (IOException e) {
      return;
    }
   
   event_set = newset;
   last_update = now;
}



/********************************************************************************/
/*                                                                              */
/*      Sensor Event information                                                */
/*                                                                              */
/********************************************************************************/

private static class SensorEvent implements Comparable<SensorEvent> {

   private UpodDevice for_device;
   private UpodParameter for_parameter;
   private String param_value;
   private Calendar     at_time;
   
   SensorEvent(UpodUniverse uu,Element xml) {
      String did = IvyXml.getAttrString(xml,"DEVICE");
      for_device = uu.findDevice(did);
      if (for_device == null) {
         String dnm = IvyXml.getAttrString(xml,"NAME");
         for_device = uu.findDevice(dnm);
       }
      for_parameter = null;
      param_value = null;
      if (for_device != null) {
         Element pelt = IvyXml.getChild(xml,"PARAM");
         if (pelt != null) {
            for_parameter = for_device.findParameter(IvyXml.getAttrString(pelt,"NAME"));
            param_value = IvyXml.getText(pelt);
          }
       }
      long when = IvyXml.getAttrLong(xml,"WHEN");
      at_time = Calendar.getInstance();
      at_time.setTimeInMillis(when);
    }
   
   SensorEvent(long when) {
      for_device = null;
      for_parameter = null;
      param_value = null;
      at_time = Calendar.getInstance();
      at_time.setTimeInMillis(when);
    }
   
   // UpodDevice getDevice()                       { return for_device; }
   Calendar getTime()                           { return at_time; }
   long getTimeInMillis()                       { return at_time.getTimeInMillis(); } 
   UpodParameter getParameter()                 { return for_parameter; }
   
   boolean isValid() {
      if (for_device == null) return false;
      if (for_parameter == null) return false;
      if (param_value == null) return false;
      return true;
    }
   
   @Override public int compareTo(SensorEvent se) {
      long x0 = getTimeInMillis() % T_DAY;
      long x1 = se.getTimeInMillis() % T_DAY;
      long t0 = x0 - x1;
      if (t0 < 0) return -1;
      if (t0 > 0) return 1;
      t0 = getTimeInMillis() - se.getTimeInMillis();
      if (t0 < 0) return -1;
      if (t0 > 0) return 1;
      if (for_device == null) return -1;
      if (se.for_device == null) return 1;
      int t1 = for_device.getUID().compareTo(se.for_device.getUID());
      if (t1 != 0) return t1;
      if (for_parameter == null) return -1;
      if (se.for_parameter == null) return 1;
      int t2 = for_parameter.getName().compareTo(se.for_parameter.getName());
      if (t2 != 0) return t2;
      if (param_value == null) return -1;
      if (se.param_value == null) return 1;
      return param_value.compareTo(se.param_value);
    }
   
   @Override public String toString() {
      if (!isValid()) return "INVALID SENSOR EVENT";
      StringBuffer buf = new StringBuffer();
      buf.append(for_device.getName());
      buf.append(".");
      buf.append(for_parameter.getName());
      buf.append("=");
      buf.append(param_value);
      buf.append("@");
      buf.append(new Date(getTimeInMillis()).toString());
      return buf.toString();
    }
   
}       // end of inner class SensorEvent




}       // end of class BasisHistoryDeducer




/* end of BasisHistoryDeducer.java */

