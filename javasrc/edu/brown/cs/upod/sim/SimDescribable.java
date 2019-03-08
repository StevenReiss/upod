/********************************************************************************/
/*                                                                              */
/*              SimDescribable.java                                             */
/*                                                                              */
/*      Basic implementation of something describable                           */
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

import java.util.*;


abstract class SimDescribable implements SimConstants, UsimDescribable, UsimIdentifiable
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String  sim_name;
private String  sim_label;
private String  sim_description;
private String  sim_uid;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

protected SimDescribable(String name,String desc)
{
   this(name,desc,null);
}


protected SimDescribable(String name,String desc,String uid)
{
   sim_name = name;
   sim_label = name;
   sim_description = desc;
   sim_uid = uid;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()               { return sim_name; }
@Override public String getLabel()              { return sim_label; }
@Override public String getDescription()        { return sim_description; }

@Override public String getUID() 
{
   if (sim_uid == null) {
      UUID u = UUID.randomUUID();
      sim_uid = "S" + u.toString().replace("-","_");
    }
   return sim_uid;
}

protected void setName(String name)     
{
   sim_name = name;
   if (sim_label == null) sim_label = name;
}


protected void setLabel(String lbl)             { sim_label = lbl; }
protected void setDescription(String d)         { sim_description = d; }



}       // end of class SimDescribable




/* end of SimDescribable.java */

