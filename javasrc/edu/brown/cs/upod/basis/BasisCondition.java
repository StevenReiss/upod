/********************************************************************************/
/*                                                                              */
/*              BasisCondition.java                                             */
/*                                                                              */
/*      Basic implementation of a condition                                     */
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

import edu.brown.cs.ivy.swing.*;


public abstract class BasisCondition implements UpodCondition, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          cond_name;
private SwingEventListenerList<UpodConditionHandler> condition_handlers;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisCondition(String nm)
{
   cond_name = nm;
   condition_handlers = new SwingEventListenerList<UpodConditionHandler>(
         UpodConditionHandler.class);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()               { return cond_name; }

@Override public boolean isTrigger()                            { return false; }
@Override public boolean isTimeBased()                          { return false; }
@Override public boolean isPositionBased()                      { return false; }
@Override public UpodCalendarEvent getEventTimes()              { return null; }
@Override public UpodSensor getSensor()                         { return null; }
@Override public UpodParameterSet getDefaultParameters()        { return null; }

@Override public UpodConditionConflict getConflicts(UpodCondition c) 
{
   return new BasisConditionConflict(UpodConditionConflict.Type.NO_CONFLICT,this,c);
}

@Override public void addHandler(UpodConditionHandler hdlr)
{
   condition_handlers.add(hdlr);
}

@Override public void removeHandler(UpodConditionHandler hdlr)
{
   condition_handlers.remove(hdlr);
}
  


/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public abstract UpodParameterSet poll(UpodWorld state);






/********************************************************************************/
/*                                                                              */
/*      Trigger methods                                                         */
/*                                                                              */
/********************************************************************************/

public void triggerOn(UpodWorld w,UpodParameterSet input)
{
   for (UpodConditionHandler ch : condition_handlers) {
      ch.conditionOn(w,this,input);
    }
}

public void triggerOff(UpodWorld w)
{
   for(UpodConditionHandler ch : condition_handlers) {
      ch.conditionOff(w,this);
    }
}

public void triggerCondition(UpodWorld w,UpodParameterSet input)
{
   for (UpodConditionHandler ch : condition_handlers) {
      ch.conditionTrigger(w,this,input);
    }
}

public void triggerError(UpodWorld w,Throwable cause)
{
   for (UpodConditionHandler ch : condition_handlers) {
      ch.conditionError(w,this,cause);
    }
}




}       // end of class BasisCondition




/* end of BasisCondition.java */

