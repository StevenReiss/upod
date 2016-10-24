/********************************************************************************/
/*										*/
/*		BasisSensorRssFeed.java 					*/
/*										*/
/*	Device to trigger on new rss feed entries				*/
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

import java.util.*;
import java.text.*;


public abstract class BasisSensorRssFeed extends BasisSensorWeb
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private long		last_update;
private long		last_hash;
private RssCondition	trigger_condition;


private static DateFormat rss_date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ");




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

protected BasisSensorRssFeed(UpodUniverse uu,String url)
{
   super(uu,url,null,300000);

   initialize();
}


protected BasisSensorRssFeed(UpodUniverse uu,Element xml)
{
   super(uu,xml);

   initialize();
}


private void initialize()
{
   last_update = System.currentTimeMillis();
   last_hash = 0;
   trigger_condition = new RssCondition();
}




/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public List<UpodCondition> getConditions()
{
   List<UpodCondition> rslt = new ArrayList<UpodCondition>();
   rslt.add(trigger_condition);
   return rslt;
}



/********************************************************************************/
/*										*/
/*	RSS Processing								*/
/*										*/
/********************************************************************************/

@Override protected void handleContents(String cnts)
{
   int hash = cnts.hashCode();
   if (hash == last_hash) return;
   last_hash = hash;

   Element xml = IvyXml.convertStringToXml(cnts);
   if (xml == null) return;
   String lbd = IvyXml.getTextElement(xml,"lastBuildDate");
   if (dateAfter(lbd) <= 0) return;

   long newdate = 0;
   List<Element> trigs = new ArrayList<Element>();

   for (Element itm : IvyXml.children(xml,"item")) {
      String pdate = IvyXml.getTextElement(itm,"pubDate");
      long ndate = dateAfter(pdate);
      if (ndate <= 0) break;
      newdate = Math.max(newdate,ndate);
      trigs.add(itm);
    }

   if (trigs.isEmpty()) return;
   last_update = newdate;

   for (int i = trigs.size() - 1; i >= 0; --i) {
      Element itm = trigs.get(i);
      trigger_condition.trigger(itm);
    }
}



private long dateAfter(String lbd)
{
   if (lbd == null) return 0;

   try {
      Date d = rss_date.parse(lbd);
      long t = d.getTime();
      if (t > last_update) return t;
    }
   catch (ParseException e) {
      BasisLogger.logE("Bad Date in RSS feed: " + lbd);
    }

   return 0;
}




private class RssCondition extends BasisCondition {

   RssCondition() {
      super(BasisSensorRssFeed.this.getUniverse());
    }

   @Override public void addImpliedProperties(UpodPropertySet ups) { }

   @Override public String getName() {
      return BasisSensorRssFeed.this.getName() + "_TRIGGER";
    }

   @Override public void getSensors(Collection<UpodDevice> rslt) {
      rslt.add(BasisSensorRssFeed.this);
   }

   @Override public String getDescription() {
      return "New Item for " + BasisSensorRssFeed.this.getDescription();
    }

   @Override public void setTime(UpodWorld w)			{ }

   @Override public boolean isConsistentWith(BasisCondition c)	{ return true; }

   @Override public void outputXml(IvyXmlWriter xw) {
      outputHeader(xw);
      BasisSensorRssFeed.this.outputXml(xw);
      outputTrailer(xw);
    }

   @Override public boolean isTrigger() 	   { return true; }

   void trigger(Element itm) {
      UpodWorld w = BasisFactory.getFactory().getCurrentWorld(for_universe);
      BasisPropertySet props = new BasisPropertySet();
      props.put("ITEM",IvyXml.getTextElement(itm,"title"));
      props.put("DESC",IvyXml.getTextElement(itm,"description"));
      String lnk = IvyXml.getTextElement(itm,"link");
      if (lnk != null) props.put("LINK",lnk);
      fireTrigger(w,props);
    }

}	// end of inner class RssCondition



}	// end of class BasisSensorRssFeed




/* end of BasisSensorRssFeed.java */

