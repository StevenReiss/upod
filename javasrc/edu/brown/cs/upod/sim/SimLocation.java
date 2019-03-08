/********************************************************************************/
/*                                                                              */
/*              SimLocation.java                                                */
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



package edu.brown.cs.upod.sim;

import edu.brown.cs.upod.usim.*;
import java.awt.geom.*;


class SimLocation implements UsimLocation
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private double  x_location;
private double  y_location;
private double  z_location;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimLocation()
{
   this(0,0,0);
}


SimLocation(double x,double y)
{
   this(x,y,0);
}


SimLocation(double x,double y,double z)
{
   x_location = x;
   y_location = y;
   z_location = z;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

Point2D getPoint()              
{
   return new Point2D.Double(x_location,y_location);
}


@Override public double getX()          { return x_location; }
@Override public double getY()          { return y_location; }
@Override public double getZ()          { return z_location; }


@Override public UsimLocation move(double x,double y,double z)
{
   return new SimLocation(x_location + x,y_location + y,z_location + z); 
}


@Override public UsimLocation linearOp(UsimLocation a,double m)
{
   return new SimLocation(x_location + a.getX() * m,
         y_location + a.getY() * m,
         z_location + a.getZ() * m);
}

@Override public UsimLocation normalize() {
   double d = Math.sqrt(x_location*x_location+y_location*y_location+z_location*z_location);
   if (d == 0 || d == 1) return this;
   return new SimLocation(x_location/d,y_location/d,z_location/d);
}


/********************************************************************************/
/*                                                                              */
/*      Debugging methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return "(" + x_location + "," + y_location + "," + z_location + ")";
}

}       // end of class SimLocation




/* end of SimLocation.java */

