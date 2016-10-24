/********************************************************************************/
/*                                                                              */
/*              BasisWorldHypothetical.java                                     */
/*                                                                              */
/*      Hypothetical world                                                      */
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

import java.util.*;


class BasisWorldHypothetical extends BasisWorld
        implements UpodWorld, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private Calendar                current_time;
private String                  unique_id;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisWorldHypothetical(UpodWorld w)
{
   super(w.getUniverse());
   current_time = Calendar.getInstance();
   current_time.setTimeInMillis(w.getTime());
   unique_id = getNewUID();
   
   for (UpodDevice ud : getUniverse().getDevices()) {
      if (ud.isEnabled()) {
         for (UpodParameter up : ud.getParameters()) {
            if (up.isSensor()) {
               Object val = ud.getValueInWorld(up,w);
               if (val != null) {
                  ud.setValueInWorld(up,val,this);
                }
             }
          }
       }
    }
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public boolean isCurrent()            { return false; }


@Override public void setTime(Calendar time) 
{
   if (time != null) current_time = time;
}





@Override public long getTime()
{
   if (current_time == null) return System.currentTimeMillis();
   
   return current_time.getTimeInMillis();
}


@Override public String getUID()
{
   return unique_id;
}




@Override public Calendar getCurrentTime()
{
   if (current_time != null) return current_time;
   return Calendar.getInstance();
}






}       // end of class BasisWorldHypothetical




/* end of BasisWorldHypothetical.java */

