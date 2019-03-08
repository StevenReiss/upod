/********************************************************************************/
/*                                                                              */
/*              SimObject.java                                                  */
/*                                                                              */
/*      Basic implementation of an object                                       */
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

import edu.brown.cs.ivy.xml.*;

import org.w3c.dom.*;

import com.jogamp.opengl.*;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.*;


class SimObject extends SimDescribable implements UsimConstants, UsimObject
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<SimShape> object_shapes;
private AffineTransform object_transform;
private double          z_transform;
private double          camera_height;
private double  r_value;

protected static AtomicInteger object_counter = new AtomicInteger(1);



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimObject(Element xml)
{
   this(IvyXml.getAttrString(xml,"NAME"),IvyXml.getAttrString(xml,"DESCRIPTION"));
   r_value = IvyXml.getAttrDouble(xml,"R",0);
   camera_height = IvyXml.getAttrDouble(xml,"CAMERA",-1);
}



SimObject(String name,String desc)
{
   super(name,desc);
   object_shapes = new ArrayList<SimShape>();
   object_transform = new AffineTransform();
   camera_height = -1;
   r_value = 0;
   z_transform = 0;
}
   



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

void addShape(SimShape shape)
{
   object_shapes.add(shape);
}


void addRectPrism(Element xml)
{
   addRectPrism(xml,false);
}


void addRectPrism(Element xml,boolean flip)
{
   double x0 = IvyXml.getAttrDouble(xml,"X0",0);
   double y0 = IvyXml.getAttrDouble(xml,"Y0",0);
   double x1 = IvyXml.getAttrDouble(xml,"X1",x0);
   double y1 = IvyXml.getAttrDouble(xml,"Y1",y0);
   double z0 = IvyXml.getAttrDouble(xml,"Z0",0);
   double z1 = IvyXml.getAttrDouble(xml,"Z1",z0);
   double dy = y1-y0;
   double dx = x1-x0;
   
   Rectangle2D rect;
   double angle = 0;
   
   if (IvyXml.getAttrPresent(xml,"WIDTH")) {
      double width = IvyXml.getAttrDouble(xml,"WIDTH",0);
      angle = Math.toDegrees(Math.atan2(dy,dx));
      double length = Math.sqrt(dx*dx+dy*dy);
      rect = new Rectangle2D.Double(0,0,length,width);
    }
   else {
      rect = new Rectangle2D.Double(0,0,Math.abs(dx),Math.abs(dy));
      x0 = Math.min(x0,x1);
      y0 = Math.min(y0,y1);
    }
   
   SimShape shape = SimShape.createPrism(rect,Math.min(z0,z1),Math.max(z0,z1),flip);
   if (angle != 0)  {
      shape.rotate(null,angle);
    }
   
   shape.translate(x0,y0,0);
   
   if (IvyXml.getAttrPresent(xml,"COLOR")) {
      SimColorSet colors = new SimColorSet(xml);
      shape.setColors(colors);
    }
   
   addShape(shape);
}


void setCameraHeight(double  ht)
{
   camera_height = ht;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

protected SimLocation getLocation()
{
   if (object_transform == null) return new SimLocation();
   
   return new SimLocation(object_transform.getTranslateX(),
         object_transform.getTranslateY(),
         z_transform);
}



@Override public Rectangle2D getXYBounds() 
{
   Rectangle2D bnd = null;
   
   for (SimShape shp : object_shapes) {
      Rectangle2D shapebnd = shp.getXYBounds();
      if (bnd == null) bnd = shapebnd;
      else bnd.add(shapebnd);
    }
   
   if (bnd == null) return new Rectangle2D.Double();
   
   Area a = new Area(bnd);
   a = a.createTransformedArea(object_transform);
   return a.getBounds2D();
}


@Override public Bounds getZBounds()
{
   Bounds bnd = null;
   for (SimShape shp : object_shapes) {
      Bounds shapebnd = shp.getZBounds();
      if (bnd == null) bnd = shapebnd;
      else bnd.add(shapebnd);
    }
   bnd.translate(z_transform);
   return bnd;
}



@Override public UsimLocation getCameraLookAt()
{
   if (camera_height < 0) return null;
   
   UsimLocation loc = getCameraPosition();
   Point2D pt = new Point2D.Double(0,-20);
   pt = object_transform.transform(pt,pt);
   SimLocation nloc = new SimLocation(pt.getX(),pt.getY(),loc.getZ());  
   return nloc; 
}


@Override public UsimLocation getCameraPosition()
{
   if (camera_height < 0) return null;
   
   Bounds bnds = getZBounds();
   
   return new SimLocation(object_transform.getTranslateX(),
         object_transform.getTranslateY(),
         bnds.getCenter());
}



@Override public UsimLocation getCameraRight()
{
   if (camera_height < 0) return null;
   
   Point2D pt = new Point2D.Double(1,0);
   pt = object_transform.deltaTransform(pt,pt);
   UsimLocation loc = new SimLocation(pt.getX(),pt.getY(),0);
   loc = loc.normalize();
   return loc;
}




@Override public UsimLocation getCameraUp()
{
   if (camera_height < 0) return null;
   
   return new SimLocation(0,0,1);
}




/********************************************************************************/
/*                                                                              */
/*      Moving methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public boolean isMovable()            { return false; }

@Override public void moveTo(UsimLocation loc)
{
   object_transform.translate(loc.getX()-object_transform.getTranslateX(),
         loc.getY()-object_transform.getTranslateY());
   z_transform = loc.getZ();
}



void moveDelta(boolean fwd)                     { }
void rotateDelta(boolean cw)                    { }

void moveDelta(UsimLocation loc) 
{
   if (isMovable()) {
      Point2D pt = new Point2D.Double(loc.getX(),loc.getY());
      pt = object_transform.deltaTransform(pt,pt);
      object_transform.translate(pt.getX(),pt.getY());
      z_transform += loc.getZ();
    }
}


void rotate(UsimLocation where,double angle)
{
   double x,y;
   
   if (where == null) {
      x = 0;
      y = 0;
    }
   else {
      x = where.getX();
      y = where.getY();
    }
   
   double theta = Math.toRadians(angle);
   object_transform.rotate(theta,x,y);
}



@Override public UsimLocation checkMove(UsimObject obj,UsimLocation loc)
        throws UsimException
{
   return null;
}

double getRValue()                              { return r_value; }



/********************************************************************************/
/*                                                                              */
/*      Drawing methods                                                         */
/*                                                                              */
/********************************************************************************/

@Override public void draw(GL2 gl)
{
   if (!object_transform.isIdentity() || z_transform != 0) {
      applyTransform(gl);
    }
   
   for (SimShape ss : object_shapes) {
      ss.draw(gl);
    }
}



protected void applyTransform(GL2 gl)
{
   double [] flat = new double[6];
   object_transform.getMatrix(flat);
   double [] mat = new double[16];
   mat[0] = flat[0];
   mat[1] = flat[1];
   mat[2] = 0;
   mat[3] = 0;
   mat[4] = flat[2];
   mat[5] = flat[3];
   mat[6] = 0;
   mat[7] = 0;
   mat[8] = 0;
   mat[9] = 0;
   mat[10] = 1;
   mat[11] = 0;
   mat[12] = flat[4];
   mat[13] = flat[5];
   mat[14] = z_transform;
   mat[15] = 1;
   gl.glMultMatrixd(mat,0);
}






}       // end of class SimObject




/* end of SimObject.java */

