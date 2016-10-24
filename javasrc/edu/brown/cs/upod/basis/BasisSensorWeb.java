/********************************************************************************/
/*										*/
/*		BasisSensorWeb.java						*/
/*										*/
/*	Web-based sensor							*/
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

import org.w3c.dom.*;

import org.jsoup.Jsoup;

import java.util.*;


public abstract class BasisSensorWeb extends BasisDevice
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private String	access_url;
private String	data_selector;

private long	start_rate;
private long	poll_rate;
private long	cache_rate;
private TimerTask timer_task;

private static BasisWebCache web_cache = new BasisWebCache();

private static long CACHE_RATE = 1*T_MINUTE;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisSensorWeb(UpodUniverse uu,String url,String sel,long time)
{
   super(uu);
   start_rate = time;
   poll_rate = 0;
   cache_rate = CACHE_RATE;
   access_url = url;
   data_selector = sel;
}


protected BasisSensorWeb(UpodUniverse uu,Element xml)
{
   super(uu,xml);
   start_rate = IvyXml.getAttrLong(xml,"POLLRATE");
   cache_rate = IvyXml.getAttrLong(xml,"CACHERATE",CACHE_RATE);
   timer_task = null;
   poll_rate = 0;
   access_url = IvyXml.getTextElement(xml,"ACCESSURL");
   data_selector = IvyXml.getTextElement(xml,"SELECTOR");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

protected void setCacheRate(long rate)
{
   cache_rate = rate;
}



protected void setAccess(String url,String pat)
{
   access_url = url;
   data_selector = pat;
}



/********************************************************************************/
/*										*/
/*	Checking methods							*/
/*										*/
/********************************************************************************/

@Override protected void checkCurrentState()		{ }


@Override protected void updateCurrentState()
{
   String cnts = null;
   String exp = expandUrl(access_url);

   cnts = web_cache.getContents(exp,cache_rate);

   handleContents(cnts);
}


protected String expandUrl(String orig) 		{ return orig; }


protected void handleContents(String cnts)
{
   if (cnts == null) return;

   UpodParameter param = null;
   for (UpodParameter up : getParameters()) {
      if (up.isSensor()) {
	 param = up;
	 break;
       }
    }

   try {
      org.jsoup.nodes.Element doc = Jsoup.parse(cnts);
      org.jsoup.select.Elements elts = doc.select(data_selector);
      String rslt = null;
      if (elts.size() > 0) {
	 rslt = elts.get(0).text();
       }

      if (rslt != null) {
	 UpodWorld cw = getCurrentWorld();
	 setValueInWorld(param,rslt,cw);
       }
    }
   catch (Throwable t) {
      BasisLogger.logE("Problem parsing web data: " + cnts,t);
    }
}




/********************************************************************************/
/*										*/
/*	Output methods								*/
/*										*/
/********************************************************************************/

@Override protected void outputLocalXml(IvyXmlWriter xw)
{
   long rate = poll_rate;
   if (poll_rate == 0) rate = start_rate;
   xw.field("POLLRATE",rate);
   if (cache_rate != rate && cache_rate != 0) xw.field("CACHERATE",cache_rate);
   xw.textElement("ACCESSURL",access_url);
   xw.cdataElement("SELECTOR",data_selector);
}


/********************************************************************************/
/*										*/
/*	Polling methods 							*/
/*										*/
/********************************************************************************/

@Override protected void localStartDevice()
{
   if (start_rate != 0) {
      long r = start_rate;
      start_rate = 0;
      setPolling(r);
    }
}


public void setPolling(long time)
{
   if (poll_rate == time) return;

   if (timer_task != null) timer_task.cancel();
   timer_task = null;

   poll_rate = time;
   if (time == 0) return;

   Timer t = BasisWorld.getWorldTimer();
   TimerTask tt = new Updater();
   t.schedule(tt,0,poll_rate);
}


private class Updater extends TimerTask {

@Override public void run() {
   updateCurrentState();
}

}	// end of inner class Updater




}	// end of class BasisSensorWeb




/* end of BasisSensorWeb.java */

