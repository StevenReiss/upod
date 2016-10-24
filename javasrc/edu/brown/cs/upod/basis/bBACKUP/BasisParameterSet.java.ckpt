/********************************************************************************/
/*                                                                              */
/*              BasisParameterSet.java                                          */
/*                                                                              */
/*      Basic implementation of a parameter set                                 */
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

import edu.brown.cs.ivy.xml.*;
import edu.brown.cs.upod.upod.*;

import org.w3c.dom.*;

import java.util.*;



public class BasisParameterSet extends HashMap<UpodParameter,Object>
        implements UpodParameterSet, BasisConstants
{



/********************************************************************************/
/*                                                                              */
/*      Private storage                                                         */
/*                                                                              */
/********************************************************************************/

private Set<UpodParameter>      valid_parameters;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public BasisParameterSet()
{
   valid_parameters = new HashSet<UpodParameter>();
}


public BasisParameterSet(Collection<UpodParameter> valids)
{
   this();
   
   if (valids != null) valid_parameters.addAll(valids);
}

public BasisParameterSet(UpodParameterSet ps)
{
   this();
   
   if (ps != null) {
      putAll(ps);
      valid_parameters = new HashSet<UpodParameter>(ps.getValidParameters());
    }
}


public BasisParameterSet(Element xml,UpodParameterSet dflt)
{
   this();
   
   if (dflt != null) {
      valid_parameters = new HashSet<UpodParameter>(dflt.getValidParameters());
      putAll(dflt);
    }
   
   if (xml != null) {
      for (Element pe : IvyXml.children(xml,"PARAMETER")) {
         String nm = IvyXml.getAttrString(pe,"NAME");
         String vl = Coder.unescape(IvyXml.getText(pe));
         BasisLogger.logD("SET PARAMETER " + nm + "=" + vl);
         setParameter(nm,vl);
       }
    }
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public Collection<UpodParameter> getValidParameters()
{
   return valid_parameters;
}


public void addParameter(UpodParameter up)
{
   valid_parameters.add(up);
}


public void addParameters(Collection<UpodParameter> ups) 
{
   valid_parameters.addAll(ups);
}


@Override public Object put(UpodParameter up,Object o)
{
   addParameter(up);
   o = up.normalize(o);
   return super.put(up,o);
}

@Override public void putAll(Map<? extends UpodParameter,? extends Object> vals)
{
   super.putAll(vals);
   for (UpodParameter up : vals.keySet()) addParameter(up);
}


@Override public void setParameter(String nm,Object val)
{
   UpodParameter parm = null;
   for (UpodParameter up : getValidParameters()) {
      if (up.getName().equals(nm)) {
         parm = up;
         break;
       }
    }
   if (parm == null) return;
   
   if (val == null) {
      remove(parm);
      return;
    }
   
   Object rvl = parm.normalize(val);
   put(parm,rvl);
}


/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public void outputXml(IvyXmlWriter xw)
{
   xw.begin("PARAMETERS");
   for (UpodParameter up : valid_parameters) {
      Object val = get(up);
      up.outputXml(xw,val);
    } 
   xw.end("PARAMETERS");
}

}       // end of class BasisParameterSet




/* end of BasisParameterSet.java */

