/********************************************************************************/
/*                                                                              */
/*              BasisConditionLogical.java                                      */
/*                                                                              */
/*      description of class                                                    */
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



abstract class BasisConditionLogical extends BasisCondition implements UpodCondition, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/


protected List<UpodCondition>     arg_conditions;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected BasisConditionLogical(String name,UpodCondition ... cond)
{
   super(name);
   
   arg_conditions = new ArrayList<UpodCondition>();
   
   for (UpodCondition c : cond) {
      arg_conditions.add(c);
    }
   
   // add triggers
}



/********************************************************************************/
/*                                                                              */
/*      AND implementation                                                      */
/*                                                                              */
/********************************************************************************/

static class And extends BasisConditionLogical {
   
   And(String nm,UpodCondition ... cond) {
      super(nm,cond);
    }
   
   @Override public UpodParameterSet poll(UpodWorld state) throws UpodConditionException {
      UpodParameterSet ups = new BasisParameterSet();
      for (UpodCondition c : arg_conditions) {
         UpodParameterSet ns = c.poll(state);
         if (ns == null) return null;
         ups.putAll(ns);
       }
      return ups;
    }

}       // end of inner class And



/********************************************************************************/
/*                                                                              */
/*      OR implementation                                                       */
/*                                                                              */
/********************************************************************************/

static class Or extends BasisConditionLogical {
   
   Or(String nm,UpodCondition ... cond) {
      super(nm,cond);
    }
   
   @Override public UpodParameterSet poll(UpodWorld state) throws UpodConditionException {
      for (UpodCondition c : arg_conditions) {
         UpodParameterSet ns = c.poll(state);
         if (ns != null) return ns;
       }
      return null;
    }
   
}       // end of inner class Or


/********************************************************************************/
/*                                                                              */
/*      NOT implementation                                                      */
/*                                                                              */
/********************************************************************************/


}       // end of class BasisConditionLogical




/* end of BasisConditionLogical.java */

