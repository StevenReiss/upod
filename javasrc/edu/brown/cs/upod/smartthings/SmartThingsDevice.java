/********************************************************************************/
/*                                                                              */
/*              SmartThingsDevice.java                                          */
/*                                                                              */
/*      Generic device attached to SmartThings                                  */
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



package edu.brown.cs.upod.smartthings;

import org.json.JSONObject;
import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.basis.BasisDevice;
import edu.brown.cs.upod.upod.UpodParameter;
import edu.brown.cs.upod.upod.UpodUniverse;
import edu.brown.cs.upod.upod.UpodWorld;
import edu.brown.cs.upod.upod.UpodCapability;

public class SmartThingsDevice extends BasisDevice implements SmartThingsConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String  device_id;
private String  device_label;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public SmartThingsDevice(UpodUniverse uu,String label,String id)
{
   super(uu);
   device_label = label;
   device_id = id;
}



public SmartThingsDevice(UpodUniverse uu,Element xml) 
{
   super(uu,xml);
   
   device_label = IvyXml.getAttrString(xml,"LABEL");
   device_id = IvyXml.getAttrString(xml,"ST_ID");
}



/********************************************************************************/
/*                                                                              */
/*      Setup methods                                                           */
/*                                                                              */
/********************************************************************************/

UpodCapability addSTCapability(UpodCapability uc)
{
   return addCapability(uc);
}



/********************************************************************************/
/*                                                                              */
/*      Abstract Method Implementations                                         */
/*                                                                              */
/********************************************************************************/

@Override public String getDescription()
{
   return device_label;
}



@Override public String getLabel()
{
   return device_label;
}




@Override public String getName()
{
   return device_label.replace(" ","_");
}

String getSTId()
{
   return device_id;
}



/********************************************************************************/
/*                                                                              */
/*      Handle sets                                                             */
/*                                                                              */
/********************************************************************************/

@Override public void setValueInWorld(UpodParameter p,Object val,UpodWorld w)
{
   if (w == null) w = getCurrentWorld();
   
   super.setValueInWorld(p,val,w);
}


/********************************************************************************/
/*                                                                              */
/*      Handle external sets                                                    */
/*                                                                              */
/********************************************************************************/

void issueCommand(String type,String field,String value)
{
   
}




/********************************************************************************/
/*                                                                              */
/*      Handle values from smartthings                                          */
/*                                                                              */
/********************************************************************************/

void handleValue(SmartThingsCapability stc,JSONObject value)
{
   if (value.isNull("timestamp")) return;
   
   Object v = value.get("value");
  
   stc.handleSmartThingsValue(this,v);
}


/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw) 
{
   xw.field("ST_ID",device_id);
}



}       // end of class SmartThingsDevice




/* end of SmartThingsDevice.java */

