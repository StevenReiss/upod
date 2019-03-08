/********************************************************************************/
/*                                                                              */
/*              SimSimulator.java                                               */
/*                                                                              */
/*      Main simulator class                                                    */
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

import java.awt.geom.*;
import java.util.*;


class SimSimulator implements UsimSimulator, SimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private SimTime         current_time;
private Set<SimObject>  all_objects;
private Rectangle2D     xy_bounds;
private Bounds          z_bounds;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimSimulator()
{
   current_time = new SimTime();
   xy_bounds = new Rectangle2D.Double();
   z_bounds = new Bounds();
   all_objects = new HashSet<SimObject>();
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public SimTime getTime()             { return current_time; }



synchronized void addObject(SimObject so)
{
   all_objects.add(so);
   
   Rectangle2D bnds = so.getXYBounds();
   if (xy_bounds.getWidth() == 0 && xy_bounds.getHeight() == 0)
      xy_bounds.setRect(bnds);
   else 
      xy_bounds.add(bnds);
   
   Bounds zbnd = so.getZBounds();
   if (z_bounds.getSpan() == 0) z_bounds.set(zbnd);
   else z_bounds.add(zbnd);
   
   z_bounds.add(zbnd);
}


synchronized Collection<SimObject> getObjects()
{
   return new ArrayList<SimObject>(all_objects);
}



/********************************************************************************/
/*                                                                              */
/*      Simulation methods                                                      */
/*                                                                              */
/********************************************************************************/

@Override public void noteValueChange(UsimDevice d,UsimParameter p,Object v)
{
   BasisLogger.logD("SIM: " + d.getName() + ":" + p.getName() + " = " + v);
}


}       // end of class SimSimulator




/* end of SimSimulator.java */

