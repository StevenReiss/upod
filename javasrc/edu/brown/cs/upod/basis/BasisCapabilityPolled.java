/********************************************************************************/
/*                                                                              */
/*              BasisCapabilityPolled.java                                      */
/*                                                                              */
/*      Capability to provide a polled device                                   */
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

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.Timer;

import edu.brown.cs.upod.upod.UpodDevice;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodWorld;



public class BasisCapabilityPolled extends BasisCapability
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<UpodDevice,Updater> update_task;
private long poll_rate;

private static final String     RATE_PARAMETER = "poll_rate";


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisCapabilityPolled(String name,long rate)
{
   super(name);
   poll_rate = rate;
   update_task = new HashMap<UpodDevice,Updater>();
}


/********************************************************************************/
/*                                                                              */
/*      Add to device                                                           */
/*                                                                              */
/********************************************************************************/

@Override public void addToDevice(UpodDevice d) 
{
   BasisDevice bd = (BasisDevice) d;
   BasisParameter bp = BasisParameter.createIntParameter(RATE_PARAMETER,1,1000*60*60*24);
   bd.addParameter(bp);
   d.setValueInWorld(bp,poll_rate,null);
}


@Override public void startCapability(UpodDevice d)
{
   BasisDevice bd = (BasisDevice) d;
   UpodWorld curworld = bd.getCurrentWorld();
   UpodParameter bp = d.findParameter(RATE_PARAMETER);
   long rate  = ((Number) curworld.getValue(bp)).longValue();
   
   setPolling(bd,rate);
}



private void setPolling(BasisDevice bd,long rate)
{
   Updater upd = update_task.get(bd);
   if (upd != null) {
      if (upd.getRate() == rate) return;
      upd.cancel();
      update_task.remove(bd);
    }
   
   if (rate == 0) return;
   
   Timer t = BasisWorld.getWorldTimer();
   TimerTask tt = new Updater(bd,rate);
   t.schedule(tt,0,rate);
}



/********************************************************************************/
/*                                                                              */
/*      Handle updating                                                         */
/*                                                                              */
/********************************************************************************/

private class Updater extends TimerTask {
   
   private BasisDevice for_device;
   private long update_rate;
   
   Updater(BasisDevice bd,long rate) {
      for_device = bd;
      update_rate = rate;
    }
   
   long getRate()                       { return update_rate; }
   
   @Override public void run() {
      for_device.updateCurrentState();
    }
   
}       // end of inner class Updater

}       // end of class BasisCapabilityPolled




/* end of BasisCapabilityPolled.java */

