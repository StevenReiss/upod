/********************************************************************************/
/*										*/
/*		BasicCalendarChecker.java					*/
/*										*/
/*	Code to periodically check calendars					*/
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

import java.util.*;


class BasisCalendarChecker implements BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private WeakHashMap<BasisCondition,Boolean>	check_conditions;
private Set<UpodWorld>				active_worlds;


/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

BasisCalendarChecker()
{
   check_conditions = new WeakHashMap<BasisCondition,Boolean>();
   active_worlds = new HashSet<UpodWorld>();
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

synchronized void addCondition(BasisCondition bc)
{
   BasisLogger.logI("Add Calendar condition " + bc);

   check_conditions.put(bc,false);
   setTime();
}


/********************************************************************************/
/*										*/
/*	Set up for next check							*/
/*										*/
/********************************************************************************/

private void setTime()
{
   Set<UpodWorld> worlds = new HashSet<UpodWorld>();
   List<BasisCondition> tocheck = new ArrayList<BasisCondition>();
   synchronized(this) {
      tocheck.addAll(check_conditions.keySet());
    }
   for (BasisCondition bc : tocheck) {
      UpodUniverse uu = bc.getUniverse();
      worlds.add(BasisFactory.getFactory().getCurrentWorld(uu));
    }

   for (UpodWorld uw : worlds) {
      setTime(uw);
    }

}


private void setTime(UpodWorld uw)
{
   BasisLogger.logI("Check Calendar world " + uw);

   synchronized (this) {
      if (active_worlds.contains(uw)) return;
      active_worlds.add(uw);
    }

   long delay = T_HOUR; 		// check at least each hour to allow new events
   long now = System.currentTimeMillis();	

   BasisGoogleCalendar gc = BasisGoogleCalendar.getCalendar(uw);
   if (gc == null) {
      BasisLogger.logI("No Calendar found for world " + uw);
      removeActive(uw);
      return;
    }

   Collection<CalendarEvent> evts = gc.getActiveEvents(now);
   for (CalendarEvent ce : evts) {
      long tim = ce.getStartTime();
      if (tim <= now) tim = ce.getEndTime();
      BasisLogger.logI("Calendar Event " + tim + " " + now + " " + ce);
      if (tim <= now) continue;
      delay = Math.min(delay,tim-now);
    }
   delay = Math.max(delay,10000);
   BasisLogger.logI("Schedule Calendar check for " + uw + " " + delay + " at " + (new Date(now+delay).toString()));

   BasisWorld.getWorldTimer().schedule(new CheckTimer(uw),delay);
}



private synchronized void removeActive(UpodWorld uw)
{
   BasisLogger.logI("Remove Calendar world " + uw);
   active_worlds.remove(uw);
}




/********************************************************************************/
/*										*/
/*	Recheck pending events							*/
/*										*/
/********************************************************************************/

private void recheck(UpodWorld uw)
{
   List<BasisCondition> tocheck = new ArrayList<BasisCondition>();
   synchronized(this) {
      tocheck.addAll(check_conditions.keySet());
    }

   for (BasisCondition bc : tocheck) {
      UpodUniverse uu = bc.getUniverse();
      UpodWorld cw = BasisFactory.getFactory().getCurrentWorld(uu);
      if (uw == cw) bc.setTime(cw);
    }
}



/********************************************************************************/
/*										*/
/*	TimerTask for checking							*/
/*										*/
/********************************************************************************/

private class CheckTimer extends TimerTask {

   private UpodWorld for_world;

   CheckTimer(UpodWorld uw) {
      for_world = uw;
    }

   @Override public void run() {
      BasisLogger.logI("Checking google Calendar for " + for_world + " at " + (new Date().toString()));
      removeActive(for_world);
      recheck(for_world);
      setTime();
    }

}	// end of inner class CheckTimer



}	// end of class BasicCalendarChecker




/* end of BasicCalendarChecker.java */

