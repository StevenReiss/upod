/********************************************************************************/
/*                                                                              */
/*              BasisRule.java                                                  */
/*                                                                              */
/*      Basic implementation of a rule                                          */
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


public class BasisRule implements UpodRule, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          rule_name;
private UpodCondition   for_condition;
private UpodAction      for_action;
private double          rule_priority;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisRule(String name,UpodCondition c,UpodAction a,double priority)
{
   if (name == null) name = c.getName() + "=>" + a.getName();
   rule_name = name;
   for_condition = c;
   for_action = a;
   rule_priority = priority;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()                       { return rule_name; }

@Override public UpodCondition getCondition()           { return for_condition; }

@Override public UpodAction getAction()                 { return for_action; }

@Override public double getPriority()                   { return rule_priority; }

@Override public void setPriority(double p)             { rule_priority = p; }




/********************************************************************************/
/*                                                                              */
/*      Application methods                                                     */
/*                                                                              */
/********************************************************************************/

@Override public boolean apply(UpodWorld w) throws UpodConditionException, UpodActionException
{
   UpodParameterSet ps = null;
   if (for_condition != null) {
      ps = for_condition.poll(w);
      if (ps == null) return false;
    }
   
   if (for_action != null) {
      for_action.perform(w,ps);
    }
   
   return true;
}


@Override public boolean applyAsync(UpodWorld w,UpodStatusHandler status)
        throws UpodConditionException, UpodActionException
{
   UpodParameterSet ps = null;
   if (for_condition != null) {
      ps = for_condition.poll(w);
      if (ps == null) return false;
    }
   
   if (for_action != null) {
      for_action.performAsync(w,ps,status);
    }
   
   return true;
}
        
        
}       // end of class BasisRule




/* end of BasisRule.java */

