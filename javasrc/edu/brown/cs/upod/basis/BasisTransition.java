/********************************************************************************/
/*										*/
/*		BasisTransition.java						*/
/*										*/
/*	Basic implementation of a transition					*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss		      */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/



package edu.brown.cs.upod.basis;

import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;

import java.util.*;


public abstract class BasisTransition implements UpodTransition, BasisConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private BasisParameterSet	default_parameters;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisTransition(UpodParameterSet dflts)
{
   default_parameters = new BasisParameterSet(dflts);
}

protected BasisTransition()
{
   this(null);
}



/********************************************************************************/
/*										*/
/*	Setup methods								*/
/*										*/
/********************************************************************************/

protected void addParameter(UpodParameter p,Object value)
{
   if (value != null) {
      default_parameters.put(p,value);
    }
   else {
      default_parameters.addParameter(p);
    }
}


/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public abstract String getName();

@Override public abstract String getDescription();

@Override public String getLabel()
{
   return getName();
}


@Override public UpodParameterSet getDefaultParameters()
{
   return new BasisParameterSet(default_parameters);
}

@Override public Collection<UpodParameter> getParameterSet()
{
   return default_parameters.getValidParameters();
}


@Override public UpodParameter getEntityParameter()	{ return null; }


@Override public UpodParameter findParameter(String nm)
{
   String nm1 = nm;
   String nm2 = nm;
   if (!nm.startsWith(getName() + NSEP)) {
      nm1 = getName() + NSEP + nm;
      nm2 = getName() + NSEP + "SET" + NSEP + nm;
    }
   for (UpodParameter up : default_parameters.getValidParameters()) {
      if (up.getName().equals(nm)) return up;
      if (up.getName().equals(nm1)) return up;
      if (up.getName().equals(nm2)) return up;
    }

   return null;
}



/********************************************************************************/
/*										*/
/*	Action methods								*/
/*										*/
/********************************************************************************/

@Override public void perform(UpodWorld w,UpodDevice e,UpodPropertySet params)
	throws UpodActionException
{
   if (e == null) throw new UpodActionException("No entity to act on");
   if (w == null) throw new UpodActionException("No world to act in");
   try {
      e.apply(this,params,w);
    }
   catch (UpodActionException ex) {
      throw ex;
    }
   catch (Throwable t) {
      throw new UpodActionException("Action aborted",t);
    }
}



/********************************************************************************/
/*										*/
/*	Output Methods								*/
/*										*/
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("TRANSITION");
   xw.field("CLASS",getClass().getName());
   xw.field("NAME",getName());
   xw.field("LABEL",getLabel());
   xw.field("TYPE",getTransitionType());
   UpodParameter ep = getEntityParameter();
   if (ep != null) {
      xw.field("ENTITYPARAM",ep.getName());
    }
   xw.textElement("DESC",getDescription());
   for (UpodParameter up : default_parameters.getValidParameters()) {
      Object v = default_parameters.get(up);
      up.outputXml(xw,v);
    }
   xw.end("TRANSITION");
}




}	// end of class BasisTransition




/* end of BasisTransition.java */

