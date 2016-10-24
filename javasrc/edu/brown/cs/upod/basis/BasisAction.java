/********************************************************************************/
/*                                                                              */
/*              BasisAction.java                                                */
/*                                                                              */
/*      Basis implementation of an action                                       */
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

import edu.brown.cs.ivy.xml.*;
import org.w3c.dom.*;


public class BasisAction implements UpodAction, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private UpodDevice     for_entity;
private UpodTransition  for_transition;
private UpodParameterSet parameter_set;
private String          action_description;
private String          action_label;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisAction(UpodDevice e,UpodTransition t)
{
   for_entity = e;
   for_transition = t;
   if (t == null) parameter_set = new BasisParameterSet();
   else  parameter_set = new BasisParameterSet(t.getDefaultParameters());
   action_description = null;
   action_label = null;
}



public BasisAction(UpodProgram bp,Element xml)
{
   for_entity = null;
   for_transition = null;
   Element ee = IvyXml.getChild(xml,"DEVICE");
   for_entity = bp.createDevice(ee);
   Element te = IvyXml.getChild(xml,"TRANSITION");
   for_transition = bp.createTransition(for_entity,te);
   UpodParameterSet ps = null;
   if (for_transition != null) ps = for_transition.getDefaultParameters();
   Element pe = IvyXml.getChild(xml,"PARAMETERS");
   parameter_set = new BasisParameterSet(pe,ps);
   String desc = Coder.unescape(IvyXml.getTextElement(xml,"DESCRIPTION"));
   action_description = null;
   if (desc != null && !desc.equals(getDescription())) {
      action_description = desc;
    }
   String lbl = Coder.unescape(IvyXml.getTextElement(xml,"LABEL"));
   if (lbl != null && !lbl.equals(getLabel())) {
      action_label = lbl;
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()
{
   if (for_transition == null) return for_entity.getName() + "^no_action";
   
   return for_entity.getName() + "^" + for_transition.getName();
}

@Override public String getDescription()
{
   if (action_description != null) return action_description; 
   
   if (for_transition == null) {
      return "Do nothing to " + for_entity.getName();
    }
   
   return "Apply " + for_transition.getName() + " to " + for_entity.getName();
}

@Override public String getLabel()
{
   if (action_label != null) return action_label;
   
   return getDescription();
}

@Override public void setLabel(String l)
{
   action_label = l;
}


@Override public void setDescription(String d) 
{
   action_description = d;
}

@Override public UpodDevice getDevice()                 { return for_entity; }

@Override public UpodTransition getTransition()         { return for_transition; }



/********************************************************************************/
/*                                                                              */
/*      Parameter methods                                                       */
/*                                                                              */
/********************************************************************************/

@Override public void setParameters(UpodParameterSet ps)
{
   parameter_set.clear();
   parameter_set.putAll(ps);
}

@Override public void addParameters(UpodParameterSet ps)
{
   parameter_set.putAll(ps);
}

@Override public UpodParameterSet getParameters()
{
   return parameter_set;
}


@Override public void addImpliedProperties(UpodPropertySet ups)
{
   UpodPropertySet aps = new BasisPropertySet(parameter_set);
   ups.putAll(aps);
}



/********************************************************************************/
/*                                                                              */
/*      Action methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void perform(UpodWorld w,UpodPropertySet ps) 
        throws UpodActionException
{
   UpodPropertySet ups = new BasisPropertySet(parameter_set);
   if (ps != null && !ps.isEmpty()) {
      ups.putAll(ps);
    }
   
   if (for_transition != null) for_transition.perform(w,for_entity,ups);
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("ACTION");
   xw.field("CLASS",getClass().getName());
   xw.field("NAME",getName());
   xw.field("LABEL",Coder.escape(getLabel()));
   xw.textElement("DESC",Coder.escape(getDescription()));
   if (for_entity != null) for_entity.outputXml(xw);
   if (for_transition != null) for_transition.outputXml(xw);
   if (getParameters() != null) getParameters().outputXml(xw);
   xw.end("ACTION");
}






}       // end of class BasisAction




/* end of BasisAction.java */

