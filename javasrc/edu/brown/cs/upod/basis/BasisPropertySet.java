/********************************************************************************/
/*                                                                              */
/*              BasisPropertySet.java                                           */
/*                                                                              */
/*      Implementation of a property set to hold properties for a world.                                       */
/*                                                                              */
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

import org.w3c.dom.*;

import java.util.*;



class BasisPropertySet extends HashMap<String,Object> implements UpodPropertySet
{



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisPropertySet()
{ }


BasisPropertySet(UpodPropertySet ps)
{
   super(ps);
}


BasisPropertySet(UpodParameterSet ps)
{
   super();
   
   for (Map.Entry<UpodParameter,Object> ent : ps.entrySet()) {
      String nm = ent.getKey().getName();
      put(nm,ent.getValue());
    }
}


BasisPropertySet(Element xml) 
{
   this();
   
   if (xml != null) {
      for (Element pe : IvyXml.children(xml,"PARAMETER")) {
         String nm = IvyXml.getAttrString(pe,"NAME");
         String vl = IvyXml.getText(pe);
         put(nm,vl);
       }
    }
}




}       // end of class BasisPropertySet




/* end of BasisPropertySet.java */
