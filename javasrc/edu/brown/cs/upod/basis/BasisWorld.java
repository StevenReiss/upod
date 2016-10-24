/********************************************************************************/
/*                                                                              */
/*              BasisWorld.java                                                 */
/*                                                                              */
/*      Basis implementationn of a real/hypothetical world                      */
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

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.*;

import java.util.*;
import java.util.concurrent.locks.*;

public abstract class BasisWorld implements UpodWorld, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private UpodUniverse            our_universe;
private BasisParameterSet       world_parameters;
private BasisTriggerContext     trigger_context;
private int                     update_counter;
private ReentrantLock           update_lock;
private Condition               update_condition;


private static Timer    world_timer = new Timer("BASIS_WORLD_TIMER",true);



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected BasisWorld(UpodUniverse uu) 
{
   our_universe = uu;
   world_parameters = new BasisParameterSet();
   trigger_context = null;
   update_counter = 0;
   update_lock = new ReentrantLock();
   update_condition = update_lock.newCondition();
}
               

/********************************************************************************/
/*                                                                              */
/*      New world methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public UpodUniverse getUniverse()             { return our_universe; }


@Override abstract public boolean isCurrent();


@Override public UpodWorld createClone()
{
   return new BasisWorldHypothetical(this);
}



/********************************************************************************/
/*                                                                              */
/*      Parameter methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public Object getValue(UpodParameter p)
{
   return world_parameters.get(p);
}


@Override public void setValue(UpodParameter p,Object v)
{
   world_parameters.put(p,v);
}


@Override public UpodParameterSet getParameters()
{
   return world_parameters;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("WORLD");
   xw.field("ID",getUID());
   xw.field("CURRENT",isCurrent());
   xw.end("WORLD");
}




/********************************************************************************/
/*                                                                              */
/*     Timer to use in current world                                            */
/*                                                                              */
/********************************************************************************/

public static Timer getWorldTimer()
{
   return world_timer;
}


public static String getNewUID()
{
   UUID u = UUID.randomUUID();
   
   return UIDP + u.toString().replace("-","_");
}


/********************************************************************************/
/*                                                                              */
/*      Condition methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override  public void addTrigger(UpodCondition c,UpodPropertySet ps)
{
   if (ps == null) ps = new BasisPropertySet();
   
   update_lock.lock();
   try {
      if (trigger_context == null) trigger_context = new BasisTriggerContext();
      trigger_context.addCondition(c,ps);
    }
   finally { 
      update_lock.unlock();
    }
}


@Override public void startUpdate()
{
   update_lock.lock();
   try {
      ++update_counter;
    }
   finally {
      update_lock.unlock();
    }
}


@Override public void endUpdate()
{
   update_lock.lock();
   try {
      --update_counter;
      if (update_counter == 0) 
         update_condition.signalAll();
    }
   finally {
      update_lock.unlock();
    }
}



@Override public UpodTriggerContext waitForUpdate()
{
   update_lock.lock();
   try {
      while (update_counter > 0) {
         update_condition.awaitUninterruptibly();
       }
      UpodTriggerContext ctx = trigger_context;
      trigger_context = null;
      return ctx;
    }
   finally {
      update_lock.unlock();
    }
}

@Override public void updateLock()
{
   update_lock.lock();
}


@Override public void updateUnlock()
{
   update_lock.unlock();
}



}       // end of class BasisWorld




/* end of BasisWorld.java */

