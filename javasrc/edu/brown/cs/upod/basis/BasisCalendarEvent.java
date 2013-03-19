/********************************************************************************/
/*                                                                              */
/*              BasisCalendarEvent.java                                         */
/*                                                                              */
/*      Implementation of a possibly recurring calendar-based event             */
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

import java.util.*;


public class BasisCalendarEvent implements UpodCalendarEvent, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Calendar        from_date;
private Calendar        to_date;
private Calendar        from_time;
private Calendar        to_time;
private BitSet          day_set;
private int             repeat_interval;
private Set<Calendar>   exclude_dates;

private static final long       HOUR_MS = 1000*60*60;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

/**
 *      Create a simple one-shot event given its specific times
 **/

public BasisCalendarEvent(Calendar from,Calendar to)
{
   from_date = startOfDay(from);
   to_date = startOfNextDay(to);
   from_time = from;
   to_time = to;
   day_set = null;
   repeat_interval = 0;
   exclude_dates = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access Methods                                                          */
/*                                                                              */
/********************************************************************************/

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
   if (days == null) {
      day_set = null;
    }
   else {
      day_set = new BitSet();
      days = days.toUpperCase();
      if (days.contains("MON")) day_set.set(Calendar.MONDAY);
      if (days.contains("TUE")) day_set.set(Calendar.TUESDAY);
      if (days.contains("WED")) day_set.set(Calendar.WEDNESDAY);
      if (days.contains("THU")) day_set.set(Calendar.THURSDAY);
      if (days.contains("FRI")) day_set.set(Calendar.FRIDAY);
      if (days.contains("SAT")) day_set.set(Calendar.SATURDAY);
      if (days.contains("SUN")) day_set.set(Calendar.SUNDAY);
    }
}



public void addExcludedDate(Calendar date)
{
   if (date == null) exclude_dates = null;
   else {
      date = startOfDay(date);
      exclude_dates.add(date);
    }
}


   
   
/********************************************************************************/
/*                                                                              */
/*      Methods to query the event                                              */
/*                                                                              */
/********************************************************************************/

@Override public List<Calendar> getSlots(Calendar from,Calendar to)
{
   List<Calendar> rslt = new ArrayList<Calendar>();
   if (from.after(to_date)) return rslt;
   if (to.before(from_date)) return rslt;
   
   Calendar fday = startOfDay(from);
   if (fday.before(from_date)) fday = from_date;
   Calendar tday = startOfNextDay(to);
   if (tday.after(to_date)) tday = to_date;
   
   if (from_time == null || to_time == null || !sameDay(from_time,to_time)) {
       Calendar start = fday;
       if (from_time != null) start = from_time;
       if (start.before(from)) start = from;
       rslt.add(start);
       Calendar end = tday;
       if (to_time != null) end = to_time;
       if (end.after(to)) end = to;
       rslt.add(to);
    }
   else {
      for (Calendar day = fday; day.before(tday); day.add(Calendar.DAY_OF_YEAR,1)) {
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


private boolean isDayRelevant(Calendar day)
{
   // assume that day has time cleared
   
   if (day_set != null) {
      int dow = day.get(Calendar.DAY_OF_WEEK);
      if (!day_set.get(dow)) return false;
    }
 
   if (repeat_interval > 0) {
      long d0 = from_date.getTimeInMillis();
      long d1 = day.getTimeInMillis();
      long delta = (d1-d0 + 12*HOUR_MS);
      delta /= 24*HOUR_MS;
      if ((delta % repeat_interval) != 0) return false; 
    }
   
   if (exclude_dates != null) {
      if (exclude_dates.contains(day)) return false;
    }
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Utility methods                                                         */
/*                                                                              */
/********************************************************************************/

private Calendar startOfDay(Calendar c)
{
   Calendar c1 = (Calendar) c.clone();
   c1.set(Calendar.HOUR_OF_DAY,0);
   c1.set(Calendar.MINUTE,0);
   c1.set(Calendar.SECOND,0);
   c1.set(Calendar.MILLISECOND,0);
   return c1;
}


private Calendar startOfNextDay(Calendar c)
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

}       // end of class BasisCalendarEvent




/* end of BasisCalendarEvent.java */

