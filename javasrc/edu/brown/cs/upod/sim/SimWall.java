/********************************************************************************/
/*                                                                              */
/*              SimWall.java                                                    */
/*                                                                              */
/*      Representation of a wall object                                         */
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


import com.jogamp.opengl.GL2;

import org.w3c.dom.*;


class SimWall extends SimObject
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

SimWall(SimSimulator ss,Element xml)
{
   super(xml);
   
   if (getName() == null) setName("WALL_" + object_counter.getAndIncrement());
   
   addRectPrism(xml);
}



/********************************************************************************/
/*                                                                              */
/*      Drawing methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public void draw(GL2 gl)
{
   float [] amb = new float [] { 0.5f, 0.5f, 0.5f, 1.0f };
   gl.glMaterialfv(GL2.GL_FRONT,GL2.GL_AMBIENT,amb,0);
   float [] shn = new float [] { 5.0f };
   gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,GL2.GL_SHININESS,shn,0);
   super.draw(gl);
}





}       // end of class SimWall




/* end of SimWall.java */

