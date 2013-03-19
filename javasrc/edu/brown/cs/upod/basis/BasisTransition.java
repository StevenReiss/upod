/********************************************************************************/
/*                                                                              */
/*              BasisTransition.java                                            */
/*                                                                              */
/*      Basic implementation of a transition                                    */
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


public class BasisTransition implements UpodTransition, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String                  transition_name;
private UpodParameterSet        default_parameters;
private List<UpodParameter>     all_parameters;
private long                    transition_timing;
private boolean                 is_trigger;
private boolean                 is_checkable;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisTransition(String name,Collection<UpodParameter> parms,UpodParameterSet dflts,
      long timing,boolean trig,boolean chk)
{
   transition_name = name;
   
   all_parameters = new ArrayList<UpodParameter>();
   if (parms != null) all_parameters.addAll(parms);
 
   default_parameters = (default_parameters == null ? null : new BasisParameterSet(dflts));
   
   transition_timing = timing;
   is_trigger = trig;
   is_checkable = chk;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()                       { return transition_name; }

@Override public boolean isCheckable()                  { return is_checkable; }

@Override public boolean isTrigger()                    { return is_trigger; }

@Override public long getTransitionTiming()             { return transition_timing; }

@Override public UpodParameterSet getDefaultParameters() 
{
   if (default_parameters == null) return new BasisParameterSet();
   return new BasisParameterSet(default_parameters);
}

@Override public Collection<UpodParameter> getParameterSet()
{
   return Collections.unmodifiableCollection(all_parameters);
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void perform(UpodWorld w,UpodEntity e,UpodParameterSet params)
        throws UpodActionException
{
   if (e == null) throw new UpodActionException("No entity to act on");
   if (w == null) throw new UpodActionException("No world to act in");
   if (!e.canApply(this,w)) {
      throw new UpodActionException("Can't apply " + getName() + " to " + e.getName());
    }
  
   e.apply(this,params,w);
}


@Override public void performAsync(UpodWorld w,UpodStatusHandler sts,UpodEntity e,
      UpodParameterSet params) throws UpodActionException
{
   if (e == null) throw new UpodActionException("No entity to act on");
   if (w == null) throw new UpodActionException("No world to act in");
   if (!e.canApply(this,w)) {
      throw new UpodActionException("Can't apply " + getName() + " to " + e.getName());
    } 
   
   if (w.isCurrent()) {
      AsyncAction aa = new AsyncAction(w,sts,e,params);
      BasisThreadPool.start(aa);
    }
   else {
      try {
         e.apply(this,params,w);
         if (sts != null) sts.success();
       }
      catch (Throwable t) {
         if (sts != null) sts.failure(t);
       }
    }
}
   


private class AsyncAction implements Runnable {
   
   private UpodWorld for_world;
   private UpodStatusHandler status_handler;
   private UpodEntity for_entity;
   private UpodParameterSet parameter_set;
   
   AsyncAction(UpodWorld w,UpodStatusHandler sts,UpodEntity e,UpodParameterSet ps) {
      for_world = w;
      status_handler = sts;
      for_entity = e;
      parameter_set = ps;
    }
   
   @Override public void run() {
      try {
         for_entity.apply(BasisTransition.this,parameter_set,for_world);
         if (status_handler != null) status_handler.success();
       }
      catch (Throwable t) {
         if (status_handler != null) status_handler.failure(t);
       }  
    }
   
}       // end of inner class AsyncAction




}       // end of class BasisTransition




/* end of BasisTransition.java */

