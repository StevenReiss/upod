/********************************************************************************/
/*                                                                              */
/*              SimTime.java                                                    */
/*                                                                              */
/*      Implementation of timer queue for simulation                            */
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



package edu.brown.cs.upod.sim;

import edu.brown.cs.upod.basis.BasisLogger;
import edu.brown.cs.upod.usim.*;

import edu.brown.cs.ivy.swing.*;

import java.util.*;

class SimTime implements UsimTime, UsimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SwingEventListenerList<UsimTimeHandler> time_handlers;
private double tick_rate;
private long   tick_unit;
private long   current_time;
private boolean sim_paused;

private Ticker  ticker_thread;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimTime()
{
   tick_rate = DEFAULT_TIME_RATE;
   tick_unit = DEFAULT_TIME_UNIT;
   current_time = System.currentTimeMillis();
   time_handlers = new SwingEventListenerList<UsimTimeHandler>(UsimTimeHandler.class);
   
   sim_paused = true;
   
   ticker_thread = new Ticker();
   ticker_thread.start();
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void setRate(double r)
{
   tick_rate = r;
   ticker_thread.interrupt();
}


@Override public void setUnit(long u)
{
   tick_unit = u;
   ticker_thread.interrupt();
}


@Override public void setTime(Calendar c)
{
   ticker_thread.setTime(c.getTimeInMillis());
}


@Override public long getTime()
{
   return current_time;
}




/********************************************************************************/
/*                                                                              */
/*      Callback methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public void addTimeHandler(UsimTimeHandler th)
{
   time_handlers.add(th);
}



@Override public void removeTimeHandler(UsimTimeHandler th)
{
   time_handlers.remove(th);
}


/********************************************************************************/
/*                                                                              */
/*      Control methods                                                         */
/*                                                                              */
/********************************************************************************/

void start()
{
   resume();
}



void pause()
{
   sim_paused = true;
}


void resume()
{
   sim_paused = false;
}



/********************************************************************************/
/*                                                                              */
/*      Handle time changes                                                     */
/*                                                                              */
/********************************************************************************/

private void reportCurrentTime()
{
   for (UsimTimeHandler th : time_handlers) {
      try {
         th.handleSetTime(this);
       }
      catch (Throwable t) {
         BasisLogger.logE("Problem with time set",t);
       }
    }
}


private void reportTick()
{
   for (UsimTimeHandler th : time_handlers) {
      try {
         th.handleTick(this);
       }
      catch (Throwable t) {
         System.err.println("Problem with time tick: " + t);
         t.printStackTrace();
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Ticker implementation                                                   */
/*                                                                              */
/********************************************************************************/

private class Ticker extends Thread {
   
   private long         last_tick;
   private long         time_set;
   
   Ticker() {
      super("SimulationTimer");
      last_tick = System.currentTimeMillis();
      time_set = 0;
    }
   
   @Override public void run() {
      for ( ; ; ) {         long next = last_tick + (long)(tick_unit / tick_rate);
         long delta = next - System.currentTimeMillis();
         if (delta > 0) {
            try {
               Thread.sleep(delta);
             }
            catch (InterruptedException e) { }
          }
         last_tick = System.currentTimeMillis();
         if (Thread.interrupted()) {
            if (time_set != 0) {
               current_time = time_set;
               time_set = 0;
               reportCurrentTime();
             }
            continue;
          }
         else if (!sim_paused) {
            current_time += tick_unit;
            reportTick();
          }
       }
    }
   
   void setTime(long when) {
      time_set = when;
      interrupt();
    }
   
}       // end of inner class Ticker




}       // end of class SimTime




/* end of SimTime.java */

