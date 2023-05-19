/********************************************************************************/
/*										*/
/*		SmartSignDisplay.java						*/
/*										*/
/*	description of class							*/
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



package edu.brown.cs.upod.smartsign;

import edu.brown.cs.upod.upod.*;
import edu.brown.cs.upod.basis.*;
import edu.brown.cs.ivy.exec.IvyExec;
import edu.brown.cs.ivy.file.*;
import edu.brown.cs.ivy.xml.IvyXml;

import org.apache.batik.transcoder.*;
import org.apache.batik.transcoder.image.*;
import org.w3c.dom.Element;

import java.util.*;
import java.io.*;


public class SmartSignDisplay extends BasisDevice implements SmartSignConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Object current_value;
private UpodParameter display_param;
private File web_file;
private File temp_file;
private Object update_lock;

private static final File BASE_FILE = new File(SMART_SIGN_IMAGE);

private static final String DISPLAY_NAME = "SignDisplay";
private static final String PICTURE_PARAM_NAME = "Display";
private static final String CHANGER_NAME = "SignChanger";

private boolean do_change = true;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

public SmartSignDisplay(UpodUniverse uu,Element xml)
{
   super(uu,xml);

   SignChanger sc = new SignChanger();
   addTransition(sc);
   current_value = null;
   update_lock = new Object();

   String pnm = getName() + NSEP + PICTURE_PARAM_NAME;
   BasisParameter bp  = BasisParameter.createPictureParameter(pnm);
   bp.setIsTarget(true);
   bp.setLabel("Sign Display");
   display_param = addParameter(bp);

   web_file = new File("/pro/web/web/people/spr/status.jpg");
   temp_file = new File("/pro/web/web/people/spr/status.jpg.tmp");
}



/********************************************************************************/
/*										*/
/*	Access methods								*/
/*										*/
/********************************************************************************/

@Override public String getName()
{
   return SMART_SIGN + NSEP + DISPLAY_NAME;
}


@Override public String getDescription()
{
   return "Picture frame that displays the current status";
}

@Override public String getLabel()
{
   return "Display Sign";
}


@Override public String getCurrentStatus(UpodWorld w)
{
   Object svgo = w.getValue(display_param);
   if (w.isCurrent() && svgo == null) svgo = current_value;
   if (svgo == null) return null;
   String svg = svgo.toString();
   Element xml = IvyXml.convertStringToXml(svg);
   if (xml != null) {
      StringBuffer buf = new StringBuffer();
      for (Element te : IvyXml.elementsByTag(xml,"text")) {
	 String txt = IvyXml.getText(te);
	 if (txt != null) {
	    if (buf.length() > 0) buf.append(" ");
	    buf.append(txt);
	  }
       }
      if (buf.length() > 0) return buf.toString();
    }

   return "Sign displaying ???";
}


/********************************************************************************/
/*										*/
/*	Transition methods							*/
/*										*/
/********************************************************************************/

@Override public void apply(UpodTransition t,UpodPropertySet ps,UpodWorld w)
{
   SignChanger sc = (SignChanger) t;
   File f = sc.getSignFile(ps,w);
   if (f == null) return;

   if (!w.isCurrent()) return;

   SignUpdater upd = new SignUpdater(f);
   BasisThreadPool.start(upd);
}




/********************************************************************************/
/*										*/
/*	Actually update the sign						*/
/*										*/
/********************************************************************************/

private class SignUpdater implements Runnable {

   private File sign_file;

   SignUpdater(File f) {
      sign_file = f;
    }

   @Override public void run() {
      if (sign_file == null) return;
      synchronized (update_lock) {
	 File basefile = BASE_FILE;
	 try {
	    IvyFile.copyFile(sign_file,BASE_FILE);
	  }
	 catch (IOException e) { }
	 if (!BASE_FILE.exists()) {
	    basefile = sign_file;
	    do_change = true;
	  }

         
         BasisLogger.logI("SMARTSIGN: Update Sign " + do_change);
         
	 if (do_change) {
	    try {
	       if (web_file != null) {
		  if (temp_file != null) {
		     IvyFile.copyFile(basefile,temp_file);
		     temp_file.renameTo(web_file);
		   }
		  else {
		     IvyFile.copyFile(basefile,web_file);
		   }
                  String cmd = "scp " + basefile.getAbsolutePath() +
                        " eadotc:/vol/web/html/status.jpg";
                  BasisLogger.logI("SMARTSIGN: Execute: " + cmd);
                  IvyExec ex = new IvyExec(cmd);
                  ex.waitFor();
		}
	     }
	    catch (IOException e) { }
	  }

	 sign_file.delete();
       }
    }
}






/********************************************************************************/
/*										*/
/*	Transition Definitions							*/
/*										*/
/********************************************************************************/

private class SignChanger extends BasisTransition  {

   private UpodParameter picture_param;

   SignChanger() {
      String pnm = getName() + NSEP + "SET" + NSEP + PICTURE_PARAM_NAME;
      picture_param = BasisParameter.createPictureParameter(pnm);
      addParameter(picture_param,null);
    }

   @Override public String getName() {
      return SmartSignDisplay.this.getName() + NSEP + CHANGER_NAME;
    }

   @Override public UpodParameter getEntityParameter() {
      return SmartSignDisplay.this.display_param;
    }

   @Override public String getDescription() {
      return "Change sign to given picture";
   }

   @Override public String getLabel() {
      return "Set Sign From Picture";
    }

   @Override public Type getTransitionType()	{ return Type.STATE_CHANGE; }

   File getSignFile(UpodPropertySet ps,UpodWorld w) {
      String vl = ps.get(picture_param.getName()).toString();
   
      Map<String,String> pvals = new HashMap<String,String>();
      if (ps != null) {
         for (Map.Entry<String,Object> ent : ps.entrySet()) {
            Object val = ent.getValue();
            if (val != null) {
               pvals.put(ent.getKey(),val.toString());
             }
            else pvals.put(ent.getKey(),null);
          }
         vl = IvyFile.expandText(vl,pvals);
       }
   
      if (!w.isCurrent()) {
         w.setValue(display_param,vl);
       }
      else {
         if (current_value == null && vl == null) return null;
         if (current_value != null && current_value.equals(vl)) return null;
         current_value = vl;
       }
   
      File f = null;
      try {
         f = File.createTempFile("upod",".jpg");
         f.deleteOnExit();
         StringReader sr = new StringReader(vl);
         TranscoderInput tin = new TranscoderInput(sr);
         FileOutputStream fw = new FileOutputStream(f);
         TranscoderOutput tout =new TranscoderOutput(fw);
         JPEGTranscoder jtr = new JPEGTranscoder();
         jtr.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,Float.valueOf(1.0f));
         // BasisLogger.logI("SIGN: about to transcode : " + vl);
         jtr.transcode(tin,tout);
         fw.flush();
         fw.close();
         // BasisLogger.logI("SIGN: done transcode : " + f.length());
       }
      catch (Throwable e) {
         BasisLogger.logI("SIGN: transcode fail : " + e);
         if (f != null) f.delete();
         f = null;
         BasisLogger.logE("Problem create jpeg from " + vl,e);
       }
   
      return f;
    }

}	// end of inner class SignChangePicture




}	// end of class SmartSignDisplay




/* end of SmartSignDisplay.java */

