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

import edu.brown.cs.upod.upod.*;

import java.util.*;

public abstract class BasisWorld implements UpodWorld, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private UpodUniverse            our_universe;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected BasisWorld(UpodUniverse uu) 
{
   our_universe = uu;
}
               


/********************************************************************************/
/*                                                                              */
/*      Methods for creating conditions                                         */
/*                                                                              */
/********************************************************************************/

@Override public UpodCondition createAndCondition(UpodCondition ... act)
{
   return null;
}

@Override public UpodCondition createOrCondition(UpodCondition ... act)
{
   return null;
}

@Override public UpodCondition createNotCondition(UpodCondition act)
{
   return null;
}

@Override public UpodCondition createTimeCondition(Calendar from,Calendar to)
{
   return null;
}

@Override public UpodCondition createTimedCondition(UpodCondition cond,long ontime)
{
   return null;
}

/********************************************************************************/
/*                                                                              */
/*      Methods for creating rules                                              */
/*                                                                              */
/********************************************************************************/

@Override public UpodRule createNewRule(UpodCondition c,UpodAction act,double pr)
{
   String name = c.getName() + "=>" + act.getName();
   BasisRule br = new BasisRule(name,c,act,pr);
   return br;
}


/********************************************************************************/
/*                                                                              */
/*      Methods for creating actions                                                          */
/*                                                                              */
/********************************************************************************/

@Override public UpodAction createNewAction(UpodEntity ent,UpodTransition t)
{
   String name = ent.getName() + "^" + t.getName();
   BasisAction ba = new BasisAction(name,ent,t);
   return ba;
}


/********************************************************************************/
/*                                                                              */
/*      Universe access methods                                                 */
/*                                                                              */
/********************************************************************************/

@Override public Collection<UpodEntity> getEntities()
{
   return our_universe.getEntities();
}


@Override public Collection<UpodSensor> getSensors()
{
   return our_universe.getSensors();
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
   // return new BasisWorldHypothetical(this);
   return null;
}



}       // end of class BasisWorld




/* end of BasisWorld.java */

