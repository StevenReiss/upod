/********************************************************************************/
/*                                                                              */
/*              SimDoor.java                                                    */
/*                                                                              */
/*      Class representing a door                                               */
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

import java.awt.geom.Rectangle2D;
import java.awt.Color;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.upod.usim.*;

import org.w3c.dom.*;


class SimDoor extends SimDevice implements UsimTimeHandler
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private static SimParameter door_open = new SimParameter("DoorIsOpen",
      "Whether the door is open or not",UsimType.getBoolean());

private double  target_angle;
private boolean is_moving;
private double  door_angle;

private static double delta_angle = 5;
private static double min_angle = 5;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SimDoor(SimSimulator ss,Element xml)
{
   super(ss,xml);
   
   if (getName() == null) setName("DOOR_" + object_counter.getAndIncrement());
   
   SimColorSet colors = new SimColorSet(xml);
   
   double x0 = IvyXml.getAttrDouble(xml,"X0",0);
   double y0 = IvyXml.getAttrDouble(xml,"Y0",0);
   double x1 = IvyXml.getAttrDouble(xml,"X1",x0);
   double y1 = IvyXml.getAttrDouble(xml,"Y1",y0);
   double z0 = IvyXml.getAttrDouble(xml,"Z0",0);
   double z1 = IvyXml.getAttrDouble(xml,"Z1",z0);
   double dy = y1-y0;
   double dx = x1-x0;
   
   double width = IvyXml.getAttrDouble(xml,"WIDTH",0);
   double angle = Math.toDegrees(Math.atan2(dy,dx));
   double length = Math.sqrt(dx*dx+dy*dy); 
   Rectangle2D rect = new Rectangle2D.Double(0,0,length,width);
   
   SimShape shape = SimShape.createPrism(rect,Math.min(z0,z1),Math.max(z0,z1),false); 
   if (angle != 0) shape.rotate(null,angle);
   shape.setColors(colors);
   addShape(shape);
   
   Rectangle2D knob = new Rectangle2D.Double(0,0,0.25,width+0.2);
   double ht = (z0+z1)/2 - 0.5;
   SimShape kshape = SimShape.createPrism(knob,ht,ht+0.75,false);
   kshape.rotate(null,angle);
   kshape.translate(length-0.5,-0.1,0);
   kshape.setColors(new SimColorSet(Color.BLACK));
   addShape(kshape);
   
   moveTo(new SimLocation(x0,y0,0));
   
   door_angle = 0;
   addParameter(door_open,false);
   
   addCommand(new DoorOpen());
   addCommand(new DoorClose());
   
   target_angle = 0;
   is_moving = false;
}



/********************************************************************************/
/*                                                                              */
/*      Time handling methods                                                   */
/*                                                                              */
/********************************************************************************/

@Override public void handleSetTime(UsimTime t)
{
   if (is_moving) {
      setDoorAngle(target_angle);
      setOpenClosed();
      is_moving = false;
    }
}



@Override public void handleTick(UsimTime t)
{
   if (is_moving) {
      double v = door_angle;
      if (target_angle < v) {
         v = v - delta_angle;
         if (v < target_angle) v = target_angle;
       }
      else {
         v = v + delta_angle;
         if (v > target_angle) v = target_angle;
       }
      if (v == target_angle) is_moving = false;
      setDoorAngle(v);
      setOpenClosed();
    }
}



private void setOpenClosed()
{
   if (door_angle < min_angle) setValue(door_open,false);
   else setValue(door_open,true);
}


private void setDoorAngle(double v)
{
   double delta = door_angle - v;
   if (delta == 0) return;
   rotate(getLocation(),delta);
   door_angle = v;
}




/********************************************************************************/
/*                                                                              */
/*      Door open command                                                       */
/*                                                                              */
/********************************************************************************/

private class DoorOpen extends SimCommand {
   
   DoorOpen() {
      super("DoorOpen","Open the door");
    }
   
   @Override public void execute(UsimDevice ud,Object [] values) {
      target_angle = 120;
      is_moving = true;
    }
   
}       // end of inner class DoorOpen


private class DoorClose extends SimCommand {

   DoorClose() {
      super("DoorClose","Close the door");
    }
   
   @Override public void execute(UsimDevice ud,Object [] values) {
      target_angle = 0;
      is_moving = false;
    }

}       // end of inner class DoorOpen





}       // end of class SimDoor




/* end of SimDoor.java */

