/********************************************************************************/
/*										*/
/*		BasisCalendarEvent.java 					*/
/*										*/
/*	Implementation of a possibly recurring calendar-based event		*/
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

import edu.brown.cs.ivy.xml.*;
import edu.brown.cs.upod.upod.*;

import org.w3c.dom.*;

import java.util.*;
import java.text.*;


public class BasisCalendarEvent implements UpodCalendarEvent, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Calendar	from_date;
private Calendar	to_date;
private Calendar	from_time;
private Calendar	to_time;
private BitSet		day_set;
private int		repeat_interval;
private Set<Calendar>	exclude_dates;

private static DateFormat date_format = DateFormat.getDateInstance(DateFormat.SHORT);
private static DateFormat time_format = DateFormat.getTimeInstance(DateFormat.SHORT);


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

/**
 *	Create a simple one-shot event given its specific times
 **/

public BasisCalendarEvent(Calendar from,Calendar to)
{
   BasisLogger.log("CREATE CALENDAR EVENT: " + from + " -> " + to);

   from_date = startOfDay(from);
   to_date = startOfNextDay(to);
   from_time = from;
   to_time = to;
   day_set = null;
   repeat_interval = 0;
   exclude_dates = null;

   normalizeTimes();
}


public BasisCalendarEvent(Element xml)
{
   BasisLogger.log("CREATE CALENDAR EVENT: " + IvyXml.convertXmlToString(xml));

   long fdv = IvyXml.getAttrLong(xml,"FROMDATE");
   if (fdv > 0) {
      from_date = Calendar.getInstance();
      from_date.setTimeInMillis(fdv);
    }
   else from_date = null;

   long tdv = IvyXml.getAttrLong(xml,"TODATE");
   if (tdv > 0) {
      to_date = Calendar.getInstance();
      to_date.setTimeInMillis(tdv);
    }
   else to_date = null;

   long ftv = IvyXml.getAttrLong(xml,"FROMTIME");
   if (ftv > 0) {
      from_time = Calendar.getInstance();
      from_time.setTimeInMillis(ftv);
    }
   else from_time = null;

   long ttv = IvyXml.getAttrLong(xml,"TOTIME");
   if (ttv > 0) {
      to_time = Calendar.getInstance();
      to_time.setTimeInMillis(ttv);
    }
   else to_time = null;

   day_set = null;
   repeat_interval = IvyXml.getAttrInt(xml,"INTERVAL",0);
   exclude_dates = null;
   String days = IvyXml.getTextElement(xml,"DAYS");
   setDays(days);
   for (Element ee : IvyXml.children(xml,"EXCLUDE")) {
      Calendar exc = Calendar.getInstance();
      exc.setTimeInMillis(IvyXml.getAttrLong(ee,"DATE"));
      addExcludedDate(exc);
    }

   normalizeTimes();
}



private void normalizeTimes()
{
  if (from_time == null && from_date != null) {
     from_time = (Calendar) from_date.clone();
   }
  if (to_time == null && to_date != null) {
     to_time = (Calendar) to_date.clone();
   }
  if (to_time == null && from_date != null) {
     to_time = startOfNextDay(to_date);
   }

  if (from_date != null) from_date = startOfDay(from_date);
  if (to_date != null) to_date = startOfNextDay(to_date);
}


/********************************************************************************/
/*										*/
/*	Access Methods								*/
/*										*/
/********************************************************************************/

String getDescription()
{
   String rslt = "";
   if (from_date != null) {
      rslt += calDate(from_date);
      if (to_date != null) rslt += " - " + calDate(to_date);
      else rslt += " ON";
    }
   else if (to_date != null) {
      rslt += "UNTIL " + calDate(to_date);
    }
   if (from_time == null && to_time == null) {
      rslt += " All Day";
    }
   else if (from_time != null) {
      rslt += " from ";
      rslt += calTime(from_time);
      if (to_time != null) {
	 rslt += " - ";
	 rslt += calTime(to_time);
       }
    }
   else {
      rslt += " until " + calTime(to_time);
    }

   return rslt;
}


private String calDate(Calendar c)
{
   return date_format.format(c.getTime());
}


private String calTime(Calendar c)
{
   return time_format.format(c.getTime());
}


public void makeAllDay()
{
   from_time = null;
   to_time = null;
}



public void setRepeat(int dayinterval)
{
   repeat_interval = dayinterval;
}


public void setDays(String days)
{
   day_set = getDaySet(days);
}


public void setDays(BitSet days)
{
   day_set = (BitSet) days.clone();
}



public static BitSet getDaySet(String days)
{
   if (days == null || days.length() == 0) return null;

   BitSet dayset = new BitSet();
   days = days.toUpperCase();
   if (days.contains("MON")) dayset.set(Calendar.MONDAY);
   if (days.contains("TUE")) dayset.set(Calendar.TUESDAY);
   if (days.contains("WED")) dayset.set(Calendar.WEDNESDAY);
   if (days.contains("THU")) dayset.set(Calendar.THURSDAY);
   if (days.contains("FRI")) dayset.set(Calendar.FRIDAY);
   if (days.contains("SAT")) dayset.set(Calendar.SATURDAY);
   if (days.contains("SUN")) dayset.set(Calendar.SUNDAY);
   if (dayset.isEmpty()) dayset = null;

   return dayset;
}

String getDays()
{
   if (day_set == null) return null;
   StringBuffer buf = new StringBuffer();
   if (day_set.get(Calendar.MONDAY)) buf.append("MON,");
   if (day_set.get(Calendar.TUESDAY)) buf.append("TUE,");
   if (day_set.get(Calendar.WEDNESDAY)) buf.append("WED,");
   if (day_set.get(Calendar.THURSDAY)) buf.append("THU,");
   if (day_set.get(Calendar.FRIDAY)) buf.append("FRI,");
   if (day_set.get(Calendar.SATURDAY)) buf.append("SAT,");
   if (day_set.get(Calendar.SUNDAY)) buf.append("SUN,");
   return buf.toString();
}



void addImpliedProperties(UpodPropertySet ups)
{
   if (from_date != null) ups.put("*FROMDATE",from_date.getTimeInMillis());
   if (from_time != null) ups.put("*FROMTIME",from_time.getTimeInMillis());
   if (to_date != null) ups.put("*TODATE",to_date.getTimeInMillis());
   if (to_time != null) ups.put("*TOTIME",to_time.getTimeInMillis());
   if (day_set != null) ups.put("*DAYS",day_set);
}



public void addExcludedDate(Calendar date)
{
   if (date == null) exclude_dates = null;
   else {
      date = startOfDay(date);
      if (exclude_dates == null) exclude_dates = new HashSet<>();
      exclude_dates.add(date);
    }
}




/********************************************************************************/
/*										*/
/*	Methods to query the event						*/
/*										*/
/********************************************************************************/

List<Calendar> getSlots(Calendar from,Calendar to)
{
   List<Calendar> rslt = new ArrayList<Calendar>();
   if (to_date != null && from.after(to_date)) return rslt;
   if (from_date != null && to.before(from_date)) return rslt;

   Calendar fday = startOfDay(from);
   Calendar tday = startOfNextDay(to);

   boolean usetimes = false;
   if (day_set != null && !day_set.isEmpty()) usetimes = true;
   if (repeat_interval > 0) usetimes = true;
   if (exclude_dates != null) usetimes = true;

   for (Calendar day = fday; day.before(tday); day.add(Calendar.DAY_OF_YEAR,1)) {
      if (!isDayRelevant(day)) continue;
      if (from_date != null && day.before(from_date)) continue;
      if (to_date != null && !day.before(to_date)) continue;
      // the day is relevant and in range at this point
      // compute the start and stop time on this day
      Calendar start = null;
      Calendar end = null;
      if (sameDay(from,day)) start = setDateAndTime(day,from);
      else start = startOfDay(day);
      if (sameDay(to,day)) end = setDateAndTime(day,to);
      else end = startOfNextDay(day);
      if (from_time != null) {
	 boolean usefromtime = usetimes;
	 if (from_date == null || sameDay(from_date,day)) usefromtime = true;
	 if (usefromtime) {
	    Calendar estart = setDateAndTime(day,from_time);
	    if (estart.after(start)) start = estart;
	  }
       }
      if (to_time != null) {
	 boolean usetotime = usetimes;
	 if (to_date == null || isNextDay(day,to_date)) usetotime = true;
	 if (usetotime) {
	    Calendar endt = setDateAndTime(day,to_time);
	    if (endt.before(end)) end = endt;
	  }
       }

      if (end.compareTo(start) <= 0) continue;
      rslt.add(start);
      rslt.add(end);
    }

   return rslt;
}

List<Calendar> getSlotsOLD(Calendar from,Calendar to)
{
   List<Calendar> rslt = new ArrayList<Calendar>();
   if (to_date != null && from.after(to_date)) return rslt;
   if (from_date != null && to.before(from_date)) return rslt;

   Calendar fday = startOfDay(from);
   Calendar tday = startOfNextDay(to);

   for (Calendar day = fday; day.before(tday); day.add(Calendar.DAY_OF_YEAR,1)) {
      fday = day;
      if (isDayRelevant(day)) break;
    }
   if (!fday.before(tday)) return rslt;
   if (from_date != null && fday.before(from_date)) fday = (Calendar) from_date.clone();

   for (Calendar day = startOfDay(fday);
      day.before(tday);
      day.add(Calendar.DAY_OF_YEAR,1)) {
      if (!isDayRelevant(day)) {
	 tday = day;
	 break;
       }
    }
   if (to_date != null && tday.after(to_date)) tday = (Calendar) to_date.clone();

   if (from_time == null || to_time == null || !sameDay(from_time,to_time)) {
      Calendar start = fday;
      if (from_time != null) start = from_time;
      if (start.before(from)) start = from;
      rslt.add(start);
      Calendar end = tday;
      for (Calendar day = fday; day.before(tday); day.add(Calendar.DAY_OF_YEAR,1)) {
	 if (!isDayRelevant(day)) {
	    end = startOfDay(day);
	    break;
	  }
       }
      if (to_time != null) end = to_time;
      if (end.after(to)) end = to;
      if (end.compareTo(start) <= 0) end.add(Calendar.HOUR,24);
      rslt.add(to);
    }
   else {
      for (Calendar day = fday;
	 day.before(tday);
	 day.add(Calendar.DAY_OF_YEAR,1)) {
	 if (!isDayRelevant(day)) continue;
	 // normal time slot events
	 Calendar start = setDateAndTime(day,from_time);
	 Calendar end = setDateAndTime(day,to_time);
	 if (start.after(to)) continue;
	 if (end.before(from)) continue;
	 if (start.before(from)) start = from;
	 if (end.after(to)) end = to;
	 rslt.add(start);
	 rslt.add(end);
       }
    }

   return rslt;
}

@Override public boolean isActive(long when)
{
   Calendar cal = Calendar.getInstance();
   cal.setTimeInMillis(when);
   if (to_date != null && cal.after(to_date)) return false;
   if (from_date != null && cal.before(from_date)) return false;
   Calendar day = startOfDay(cal);
   if (!isDayRelevant(day)) return false;
   Calendar dstart = startOfDay(day);
   Calendar dend = startOfNextDay(day);

   boolean usetimes = false;
   if (day_set != null && !day_set.isEmpty()) usetimes = true;
   if (repeat_interval > 0) usetimes = true;
   if (exclude_dates != null) usetimes = true;	
   if (from_time != null) {
      boolean usefromtime = usetimes;
      if (from_date == null || sameDay(from_date,day)) usefromtime = true;
      if (usefromtime) {
	 dstart = setDateAndTime(day,from_time);
       }
    }
   if (to_time != null) {
      boolean usetotime = usetimes;
      if (to_date == null || isNextDay(day,to_date)) usetotime = true;
      if (usetotime) {
	 Calendar endt = setDateAndTime(day,to_time);
	 if (endt.before(dend)) dend = endt;
       }
    }

   if (dend.compareTo(dstart) <= 0) return false;
   if (cal.before(dstart)) return false;
   if (cal.after(dend)) return false;

   return true;
}



private boolean isDayRelevant(Calendar day)
{
   // assume that day has time cleared

   if (day_set != null) {
      int dow = day.get(Calendar.DAY_OF_WEEK);
      if (!day_set.get(dow)) return false;
    }

   if (repeat_interval > 0 && from_date != null) {
      long d0 = from_date.getTimeInMillis();
      long d1 = day.getTimeInMillis();
      long delta = (d1-d0 + 12*T_HOUR);
      delta /= T_DAY;
      if (day_set != null) delta = (delta / 7) * 7;
      if ((delta % repeat_interval) != 0) return false;
    }
   else if (repeat_interval < 0) {
      if (day_set != null && from_date != null) {
	 if (day.get(Calendar.WEEK_OF_MONTH) != from_date.get(Calendar.WEEK_OF_MONTH))
	    return false;
       }
      else if (from_date != null) {
	 if (day.get(Calendar.DAY_OF_MONTH) != from_date.get(Calendar.DAY_OF_MONTH))
	    return false;
       }
    }

   if (exclude_dates != null) {
      if (exclude_dates.contains(day)) return false;
    }

   return true;
}



/********************************************************************************/
/*										*/
/*	Utility methods 							*/
/*										*/
/********************************************************************************/

static Calendar startOfDay(Calendar c)
{
   if (c == null) {
      c = Calendar.getInstance();
    }
   Calendar c1 = (Calendar) c.clone();
   c1.set(Calendar.HOUR_OF_DAY,0);
   c1.set(Calendar.MINUTE,0);
   c1.set(Calendar.SECOND,0);
   c1.set(Calendar.MILLISECOND,0);
   return c1;
}


static Calendar startOfNextDay(Calendar c)
{
   Calendar c1 = startOfDay(c);
   c1.add(Calendar.DAY_OF_YEAR,1);
   return c1;
}


private Calendar setDateAndTime(Calendar date,Calendar time)
{
   Calendar c1 = (Calendar) date.clone();
   c1.set(Calendar.HOUR_OF_DAY,time.get(Calendar.HOUR_OF_DAY));
   c1.set(Calendar.MINUTE,time.get(Calendar.MINUTE));
   c1.set(Calendar.SECOND,time.get(Calendar.SECOND));
   c1.set(Calendar.MILLISECOND,time.get(Calendar.MILLISECOND));
   return c1;
}

private boolean sameDay(Calendar c0,Calendar c1)
{
   return c0.get(Calendar.YEAR) == c1.get(Calendar.YEAR) &&
   c0.get(Calendar.DAY_OF_YEAR) == c1.get(Calendar.DAY_OF_YEAR);
}


private boolean isNextDay(Calendar c0,Calendar c1)
{
   Calendar c0a = startOfNextDay(c0);
   Calendar c1a = startOfDay(c1);
   return c0a.equals(c1a);
}




/********************************************************************************/
/*										*/
/*	Check for conflicts							*/
/*										*/
/********************************************************************************/

boolean canOverlap(BasisCalendarEvent evt)
{
   if (from_date != null && evt.to_date != null &&
	 evt.to_date.after(from_date)) return false;
   if (to_date != null && evt.from_date != null &&
	 evt.from_date.before(to_date)) return false;
   if (evt.to_time.after(from_time)) return false;
   if (evt.from_time.before(to_time)) return false;

   if (evt.day_set != null && day_set != null) {
      if (!day_set.intersects(evt.day_set)) return false;
    }

   // Need to handle repeat interval, excluded dates

   return true;
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

void outputXml(IvyXmlWriter xw)
{
   xw.begin("CALEVENT");
   if (from_date != null) xw.field("FROMDATE",from_date.getTimeInMillis());
   if (to_date != null) xw.field("TODATE",to_date.getTimeInMillis());
   if (from_time != null) xw.field("FROMTIME",from_time.getTimeInMillis());
   if (to_time != null) xw.field("TOTIME",to_time.getTimeInMillis());
   xw.field("DAYS",getDays());
   xw.field("INTERVAL",repeat_interval);
   if (exclude_dates != null) {
      for (Calendar c : exclude_dates) {
	 xw.begin("EXCLUDE");
	 xw.field("DATE",c.getTimeInMillis());
	 xw.end("EXCLUDE");
       }
    }
   xw.end("CALEVENT");
}


@Override public String toString()
{
   StringBuffer buf = new StringBuffer();
   if (from_date != null) {
      buf.append(DateFormat.getDateInstance().format(from_date));
      buf.append(" ");
    }
   if (from_time != null) {
      buf.append(DateFormat.getTimeInstance().format(from_time));
    }
   buf.append(":");
   if (to_date != null) {
      buf.append(DateFormat.getDateInstance().format(to_date));
      buf.append(" ");
    }
   if (to_time != null) {
      buf.append(DateFormat.getTimeInstance().format(to_time));
    }
   if (getDays() != null) {
      buf.append(" ");
      buf.append(getDays());
    }
   if (repeat_interval != 0) {
      buf.append(" R");
      buf.append(repeat_interval);
    }
   if (exclude_dates != null) {
      for (Calendar c : exclude_dates) {
	 Date d = new Date(c.getTimeInMillis());
	 buf.append("-");
	 buf.append(DateFormat.getDateInstance().format(d));
       }
    }
   return buf.toString();
}


}	// end of class BasisCalendarEvent




/* end of BasisCalendarEvent.java */

