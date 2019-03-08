/********************************************************************************/
/*                                                                              */
/*              SimShape.java                                                   */
/*                                                                              */
/*      Representation of a shape to draw as part of an object                  */
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

import java.awt.geom.*;
import java.awt.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.*;




abstract class SimShape implements SimConstants
{


/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

static SimShape createPrism(Shape shape,double zmin,double zmax,boolean flip)
{
   return new PrismShape(shape,zmin,zmax,flip);
}


static SimShape createCylinder(double br,double tr,double ht)
{
   return new CylinderShape(br,tr,ht);
}


static SimShape createSphere(double rad)
{
   return new SphereShape(rad);
}



/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

protected AffineTransform       shape_transform;
protected double                z_transform;
private SimColorSet             color_set;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected SimShape() 
{
   shape_transform = null;
   z_transform = 0;
   color_set = new SimColorSet();
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

void setColors(SimColorSet colors)              { color_set = colors; }

void translate(double x,double y,double z)
{
   if (x == 0 && y == 0 && z == 0) return;
   
   if (shape_transform == null) shape_transform = new AffineTransform();
   shape_transform.translate(x,y);
   z_transform += z;
}


void rotate(SimLocation point,double angle)
{
   double x,y;
   
   if (angle == 0) return;
   
   if (point == null) {
      x = 0;
      y = 0;
    }
   else {
      x = point.getX();
      y = point.getY();
    }
   
   double theta = Math.toRadians(angle);
   if (shape_transform == null) shape_transform = new AffineTransform();
   shape_transform.rotate(theta,x,y);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

abstract Rectangle2D getXYBounds();
abstract Bounds getZBounds();

protected Color getTopColor()           { return color_set.getTopColor(); }
protected Color getBottomColor()        { return color_set.getBottomColor(); }
protected Color getSideColor(int idx)   { return color_set.getSideColor(idx); }




/********************************************************************************/
/*                                                                              */
/*      Drawing methods                                                         */
/*                                                                              */
/********************************************************************************/

void draw(GL2 gl)
{
   if (shape_transform != null || z_transform != 0) {
      gl.glPushMatrix();
      applyTransform(gl);
    }
   
   localDraw(gl);
   
   if (shape_transform != null || z_transform != 0) {
      gl.glPopMatrix();
    }
}



abstract void localDraw(GL2 gl);



protected void drawSide(GL2 gl,double [] p0,double [] p1,double zmin,double zmax,Color c)
{
   if (c == null) return;
   if (zmin == zmax) return;
   
   gl.glBegin(GL2.GL_QUADS);
   setColor(gl,c);
   gl.glVertex3d(p0[0],p0[1],zmin);
   gl.glVertex3d(p1[0],p1[1],zmin);
   gl.glVertex3d(p1[0],p1[1],zmax);
   gl.glVertex3d(p0[0],p0[1],zmax);
   gl.glEnd();
}



protected void drawSurface(GL2 gl,double [] p0,double [] p1,double [] p2,double z,Color c,boolean flip)
{
   if (c == null) return;
   if (p0[0] == p1[0] && p0[1] == p1[1]) return;
   if (p0[0] == p2[0] && p0[1] == p2[1]) return; 
   if (p2[0] == p1[0] && p2[1] == p1[1]) return;   
   
   gl.glBegin(GL2.GL_TRIANGLES);
   setColor(gl,c);
   if (flip) {
      gl.glVertex3d(p0[0],p0[1],z);
      gl.glVertex3d(p2[0],p2[1],z);
      gl.glVertex3d(p1[0],p1[1],z);
      gl.glVertex3d(p0[0],p0[1],z);
    }
   else {
      gl.glVertex3d(p0[0],p0[1],z);
      gl.glVertex3d(p1[0],p1[1],z);
      gl.glVertex3d(p2[0],p2[1],z);
      gl.glVertex3d(p0[0],p0[1],z);
    }
   gl.glEnd();
}



protected void setColor(GL2 gl,Color c)
{
   gl.glColor4f(c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f,
         c.getAlpha()/255f);
   gl.glCullFace(GL.GL_FRONT);
}


protected void applyTransform(GL2 gl)
{
   double [] flat = new double[6];
   shape_transform.getMatrix(flat);
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



/********************************************************************************/
/*                                                                              */
/*      Prism shape                                                             */
/*                                                                              */
/********************************************************************************/

private static class PrismShape extends SimShape {
   
   private Shape base_shape;
   private double z_min;
   private double z_max;
   private boolean flip_view;
   
   PrismShape(Shape shape,double zmin,double zmax,boolean flip) {
      base_shape = shape;
      z_min = zmin;
      z_max = zmax;
      flip_view = flip;
    }
   
   @Override Rectangle2D getXYBounds() {
      Rectangle2D bnds = null;
      if (shape_transform != null && !shape_transform.isIdentity()) {
         Area a = new Area(base_shape);
         a = a.createTransformedArea(shape_transform);
         bnds = a.getBounds2D();
       }
      else {
         bnds = base_shape.getBounds2D();
       }
      
      return bnds;       
    }
   
   @Override Bounds getZBounds() {
      return new Bounds(z_min+z_transform,z_max+z_transform);
    }
   
   @Override void localDraw(GL2 gl) {
      double [] coords = new double[6];
      double [] prior = new double[6];
      double [] first = new double[6];
      
      int index = 0;
      
      for (PathIterator pi = base_shape.getPathIterator(null,1.0);
         !pi.isDone();
         pi.next()) {
         int what = pi.currentSegment(coords);
         switch (what) {
            case PathIterator.SEG_MOVETO :
               System.arraycopy(coords,0,first,0,6);
               index = 0;
               break;
            case PathIterator.SEG_LINETO :
               drawSide(gl,prior,coords,z_min,z_max,getSideColor(index));
               if (index > 0) {
                  drawSurface(gl,first,prior,coords,z_min,getBottomColor(),flip_view);
                  if (z_min != z_max) {
                     drawSurface(gl,first,prior,coords,z_max,getTopColor(),flip_view);
                   }
                }
               ++index;
               break;
            case PathIterator.SEG_CLOSE :
               drawSide(gl,prior,first,z_min,z_max,getSideColor(index));
               break;
            default :
               break;
          }
         System.arraycopy(coords,0,prior,0,6);
       }
    }  
   
}       // end of inner class PrismShape



/********************************************************************************/
/*                                                                              */
/*      Cylinder shape                                                          */
/*                                                                              */
/********************************************************************************/

private static class CylinderShape extends SimShape {
   
   private GLU glu_object;
   private GLUquadric draw_quad;
   private double base_radius;
   private double top_radius;
   private double cyl_height;
   
   CylinderShape(double br,double tr,double ht) {
      base_radius = br;
      top_radius = tr;
      cyl_height = ht;
      glu_object = new GLU();
      draw_quad = null;
    }
   
   @Override Rectangle2D getXYBounds() {
      double rad = Math.max(base_radius,top_radius);
      double xpos = 0;
      double ypos = 0;
      if (shape_transform != null) {
         xpos = shape_transform.getTranslateX();
         ypos = shape_transform.getTranslateY();
       }
      Rectangle2D bnds = new Rectangle2D.Double(xpos-rad,ypos-rad,2*rad,2*rad);
      return bnds;
    }
   
   @Override Bounds getZBounds() {
      return new Bounds(z_transform,z_transform+cyl_height);
    }
   
   @Override void localDraw(GL2 gl) {
      if (draw_quad == null) draw_quad = glu_object.gluNewQuadric();
      setColor(gl,getSideColor(0));
      glu_object.gluCylinder(draw_quad,base_radius,top_radius,cyl_height,8,8);  
    }
   
}       // end of inner class CylinderShape





/********************************************************************************/
/*                                                                              */
/*      Shpere shape                                                            */
/*                                                                              */
/********************************************************************************/

private static class SphereShape extends SimShape {

   private GLU glu_object;
   private GLUquadric draw_quad;
   private double sphere_radius;
   
   SphereShape(double r) {
      sphere_radius = r;
      glu_object = new GLU();
      draw_quad = null;
    }
   
   @Override Rectangle2D getXYBounds() {
      double xpos = 0;
      double ypos = 0;
      double rad = sphere_radius;;
      if (shape_transform != null) {
         xpos = shape_transform.getTranslateX();
         ypos = shape_transform.getTranslateY();
       }
      Rectangle2D bnds = new Rectangle2D.Double(xpos-rad,ypos-rad,2*rad,2*rad);
      return bnds;
    }
   
   @Override Bounds getZBounds() {
      return new Bounds(z_transform,z_transform+2*sphere_radius);
    }
   
   @Override void localDraw(GL2 gl) {
      if (draw_quad == null) draw_quad = glu_object.gluNewQuadric();
      setColor(gl,getSideColor(0));
      glu_object.gluSphere(draw_quad,sphere_radius,8,8);  
    }

}       // end of inner class SphereShape



}       // end of class SimShape




/* end of SimShape.java */

