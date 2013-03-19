/********************************************************************************/
/*                                                                              */
/*              BasisAction.java                                                */
/*                                                                              */
/*      Basis implementation of an action                                       */
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


public class BasisAction implements UpodAction, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          action_name;
private UpodEntity      for_entity;
private UpodTransition  for_transition;
private UpodParameterSet parameter_set;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisAction(String name,UpodEntity e,UpodTransition t)
{
   action_name = name;
   for_entity = e;
   for_transition = t;
   parameter_set = t.getDefaultParameters();
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()                       { return action_name; }

@Override public UpodEntity getEntity()                 { return for_entity; }

@Override public UpodTransition getTransition()         { return for_transition; }



/********************************************************************************/
/*                                                                              */
/*      Parameter methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public void setParameters(UpodParameterSet ps)
{
   parameter_set.clear();
   parameter_set.putAll(ps);
}

@Override public void addParameters(UpodParameterSet ps)
{
   parameter_set.putAll(ps);
}

@Override public UpodParameterSet getParameters()
{
   return parameter_set;
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public boolean validate()
{
   //TODO: check validity of parameters
   return true;
}


@Override public void perform(UpodWorld w,UpodParameterSet ps) throws UpodActionException
{
   UpodParameterSet ups = parameter_set;
   if (ps != null && !ps.isEmpty()) {
      ups = new BasisParameterSet(parameter_set);
      ups.putAll(ps);
    }
   
   for_transition.perform(w,for_entity,ups);
}


@Override public void performAsync(UpodWorld w,UpodParameterSet ps,UpodStatusHandler sts)
        throws UpodActionException
{
   UpodParameterSet ups = parameter_set;
   if (ps != null && !ps.isEmpty()) {
      ups = new BasisParameterSet(parameter_set);
      ups.putAll(ps);
    }
      
   for_transition.performAsync(w,sts,for_entity,ups);
}




/********************************************************************************/
/*                                                                              */
/*      Conflict methods                                                        */
/*                                                                              */
/********************************************************************************/

@Override public UpodActionConflict getConflicts(UpodAction act) 
{
   //TODO: need to compute comflicts
   
   return new BasisActionConflict(UpodActionConflict.Type.NO_CONFLICT,this,act);
}



}       // end of class BasisAction




/* end of BasisAction.java */

