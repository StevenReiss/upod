/********************************************************************************/
/*                                                                              */
/*              BasisSensorOr.java                                              */
/*                                                                              */
/*      Create a sensor that is the OR of a set of conditions                   */
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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.UpodCondition;
import edu.brown.cs.upod.upod.UpodDevice;
import edu.brown.cs.upod.upod.UpodDeviceHandler;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodUniverse;
import edu.brown.cs.upod.upod.UpodWorld;



public class BasisSensorOr extends BasisDevice implements BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String  sensor_label;
private String  sensor_name;
private List<Condition> sensor_conditions;
private UpodParameter result_parameter;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisSensorOr(String id,UpodDevice base,UpodParameter p,Object state) 
{
   super(base.getUniverse());
   
   sensor_label = id;
   sensor_conditions = new ArrayList<Condition>();
   addCondition(base,p,state);

   setup();
}


public BasisSensorOr(String id,UpodCondition c)
{
   super(c.getUniverse());
   sensor_label = id;
   sensor_conditions = new ArrayList<Condition>();   
   addCondition(c);
   
   setup();
}



public BasisSensorOr(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   
   sensor_label = IvyXml.getAttrString(xml,"LABEL");
   sensor_name = IvyXml.getAttrString(xml,"NAME");
   
   sensor_conditions = new ArrayList<Condition>();   
   for (Element x : IvyXml.children(xml,"CONDITION")) {
      Condition c = new Condition(uu,x);
      sensor_conditions.add(c);
    }
   
   setup();
}



private void setup()
{
   BasisParameter bp = BasisParameter.createBooleanParameter(getUID());
   bp.setIsSensor(true);
   bp.setLabel(getLabel());
   result_parameter = addParameter(bp);
   setValueInWorld(bp,Boolean.FALSE,null);
   
   String nml = sensor_label.replace(" ",WSEP);
   sensor_name = getUniverse().getName() + NSEP + nml;
   addConditions(result_parameter);
   UpodCondition uc = getCondition(result_parameter,Boolean.TRUE);
   uc.setLabel(sensor_label);
}




/********************************************************************************/
/*                                                                              */
/*      Definition methods                                                      */
/*                                                                              */
/********************************************************************************/

public void addCondition(UpodDevice d,UpodParameter p,Object v) 
{
   Condition c = new Condition(d,p,v);
   sensor_conditions.add(c);
}


public void addCondition(UpodCondition c)
{
   if (c instanceof BasisConditionParameter) {
      BasisConditionParameter bp = (BasisConditionParameter) c;
      UpodDevice ud = bp.getDevice();
      UpodParameter up = bp.getParameter();
      Object v = bp.getState();
      addCondition(ud,up,v);
    }
   else {
      BasisLogger.logE("Can't add or condition for " + c);
    }
}



@Override protected void localStartDevice()
{
   for (Condition c : sensor_conditions) {
      c.getDevice().addDeviceHandler(new SensorChanged());
    }
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public String getDescription()
{
   StringBuffer buf = new StringBuffer();
   
   for (Condition c : sensor_conditions) {
      String ctag = c.getLabel();
      if (buf.length() > 0) buf.append(" OR ");
      buf.append(ctag);
    }
   
   return buf.toString();
}




@Override public String getName()
{
   return sensor_name;
}



@Override public String getLabel()
{
   return sensor_label;
}




/********************************************************************************/
/*                                                                              */
/*      State update methods                                                    */
/*                                                                              */
/********************************************************************************/

private void handleStateChanged(UpodWorld w)
{
   boolean fg = false;
   for (Condition c : sensor_conditions) {
      fg |= c.checkInWorld(w);
      if (fg) break;
    }
   
   System.err.println("SET OR SENSOR " + getLabel() + " = " + fg);
   
   setValueInWorld(result_parameter,fg,w);
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw) 
{
    for (Condition c : sensor_conditions) {
       c.outputXml(xw);
     }
    super.outputLocalXml(xw);
}




/********************************************************************************/
/*                                                                              */
/*      Representation of a condition                                           */
/*                                                                              */
/********************************************************************************/

private static class Condition {

   private UpodDevice base_sensor;
   private UpodParameter base_parameter;
   private Object     base_state;
   
   Condition(UpodDevice ud,UpodParameter up,Object v) {
      base_sensor = ud;
      base_parameter = up;
      base_state = base_parameter.normalize(v);
    }
    
   Condition(UpodUniverse uu,Element xml) {
       base_sensor = uu.findDevice(IvyXml.getAttrString(xml,"DEVICE"));
       String pnm = IvyXml.getAttrString(xml,"PARAM");
       if (pnm == null) pnm = base_sensor.getUID();
       base_parameter = base_sensor.findParameter(pnm);
       base_state = IvyXml.getAttrString(xml,"SET");
    }
   
   UpodDevice getDevice()               { return base_sensor; }
   
   String getLabel() {
      return base_parameter.getLabel() + " = " + base_state;
    }
   
   boolean checkInWorld(UpodWorld w) {
      Object ov = base_sensor.getValueInWorld(base_parameter,w);
      if (base_state.equals(ov)) return true;
      return false;
    }
   
   void outputXml(IvyXmlWriter xw) {
      xw.begin("CONDITION");
      xw.field("SET",base_state);
      xw.field("PARAM",base_parameter.getName());
      xw.field("DEVICE",base_sensor.getUID());
      xw.end("CONDITION");
    }
   
}       // end of inner class Condition



/********************************************************************************/
/*                                                                              */
/*      Sensor update methods                                                   */
/*                                                                              */
/********************************************************************************/

private class SensorChanged implements UpodDeviceHandler {

   @Override public void stateChanged(UpodWorld w,UpodDevice s) {
      handleStateChanged(w);
    }
   
}       // end of inner class SensorChanged

}       // end of class BasisSensorOr




/* end of BasisSensorOr.java */

