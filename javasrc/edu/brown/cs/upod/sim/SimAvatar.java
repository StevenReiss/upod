/********************************************************************************/
/*                                                                              */
/*              SimAvatar.java                                                  */
/*                                                                              */
/*      Represent an avatar for a user                                          */
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


import edu.brown.cs.ivy.swing.SwingColorSet;


class SimAvatar extends SimObject implements SimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimAvatar(String name,String desc,SimLocation loc)
{
   super(name,desc);
   
   SimColorSet colors = new SimColorSet(SwingColorSet.getColorByName("brown"),
         SwingColorSet.getColorByName("tan"),
         SwingColorSet.getColorByName("pink"));
   
   SimShape s1 = SimShape.createCylinder(0.3,0.3,2.5);
   s1.translate(-0.5,0,0);
   s1.setColors(new SimColorSet(colors.getSideColor(0)));
   addShape(s1);
   
   SimShape s2 = SimShape.createCylinder(0.3,0.3,2.5);
   s2.translate(0.5,0,0);
   s2.setColors(new SimColorSet(colors.getSideColor(0))); 
   addShape(s2);
   
   SimShape s3 = SimShape.createCylinder(1.0,0.75,2.5);
   s3.translate(0,0,2.5);
   s3.setColors(new SimColorSet(colors.getSideColor(1)));
   addShape(s3);
   
   SimShape s4 = SimShape.createSphere(0.5);
   s4.translate(0,0,5.25);
   s4.setColors(new SimColorSet(colors.getSideColor(2))); 
   addShape(s4);
   
   setCameraHeight(5.0);
   
   moveTo(loc);
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public boolean isMovable()            { return true; }

@Override void moveDelta(boolean fwd)
{
   SimLocation delta;
   if (fwd) delta = new SimLocation(0,-0.5,0);
   else delta = new SimLocation(0,0.5,0);
   moveDelta(delta);
}

@Override void rotateDelta(boolean cw)
{
   double angle = (cw ? -15 : 15);
   rotate(null,angle);
}





}       // end of class SimAvatar




/* end of SimAvatar.java */

