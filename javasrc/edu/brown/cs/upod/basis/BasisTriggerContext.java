/********************************************************************************/
/*                                                                              */
/*              BasisTriggerContext.java                                        */
/*                                                                              */
/*      Handle information about pending triggers                               */
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
import java.util.concurrent.*;


class BasisTriggerContext implements UpodTriggerContext, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Map<UpodCondition,UpodPropertySet>      pending_triggers;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisTriggerContext()
{
   pending_triggers = new ConcurrentHashMap<UpodCondition,UpodPropertySet>();
}

BasisTriggerContext(UpodCondition uc,UpodPropertySet us)
{
   this();
   addCondition(uc,us);
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

void addCondition(UpodCondition uc,UpodPropertySet us) 
{
   if (us == null) us = new BasisPropertySet();
   us.put("*TRIGGER*",Boolean.TRUE);
   pending_triggers.put(uc,us);
}


void addContext(BasisTriggerContext ctx)
{
   pending_triggers.putAll(ctx.pending_triggers);
}


void clear()
{
   pending_triggers.clear();
}


@Override public UpodPropertySet checkCondition(UpodCondition c)
{
   return pending_triggers.get(c);
}


}       // end of class BasisTriggerContext




/* end of BasisTriggerContext.java */

