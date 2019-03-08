/********************************************************************************/
/*										*/
/*		UmonSessionManager.java 					*/
/*										*/
/*	Handle sessions, login, etc.						*/
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



package edu.brown.cs.upod.umon;

import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.upod.basis.*;
import edu.brown.cs.upod.upod.UpodAccess;
import edu.brown.cs.upod.upod.UpodUniverse;
import fi.iki.elonen.NanoHTTPD;

import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class UmonSessionManager implements UmonConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private Map<String,Session>	host_map;
private Map<String,Session>	session_map;
private UpodUniverse		for_universe;
private File			session_file;



/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

UmonSessionManager(UpodUniverse uu)
{
   for_universe = uu;
   host_map = new ConcurrentHashMap<String,Session>();
   session_map = new ConcurrentHashMap<String,Session>();
   session_file = new File(uu.getBaseDirectory(),SESSION_FILE);

   loadSessions();
}




/********************************************************************************/
/*										*/
/*	Session setup methods							*/
/*										*/
/********************************************************************************/

String beginSession(String host,String file,Map<String,String> args,
	NanoHTTPD.CookieHandler cookies)
{
   Session s = new Session(host,file,args);
   host_map.put(host,s);
   session_map.put(s.getSessionId(),s);

   if (for_universe.authorize(null,null,null)) {
      s.makeValid(null,for_universe.getRole(null));
      saveSessions();
      return s.getSessionId();
    }

   return null;
}


String getSessionForHost(String host)
{
   if (host == null) return null;
   Session s = host_map.get(host);
   if (s == null) return null;
   return s.getSessionId();
}


void endSession(String sid)
{
   if (sid == null || sid.length() == 0) return;
   Session s = session_map.get(sid);
   if (s == null) return;
   session_map.remove(sid);
   host_map.remove(s.getHost());
   saveSessions();
}



/********************************************************************************/
/*										*/
/*	Validation methods							*/
/*										*/
/********************************************************************************/

boolean validateSession(String host,String sid)
{
   if (host.startsWith("localhost:") || host.equals("localhost")) return true;

   if (sid == null) return false;
   Session s = session_map.get(sid);
   if (s == null) return false;
   if (s.validate(host,false)) return true;

   session_map.remove(sid);
   String h = s.getHost();
   if (h != null && host_map.get(h) == s) {
      host_map.remove(h);
    }

   return false;
}



Map<String,String> validateLogin(String sid,String host,Map<String,String> args,String cookie,
      NanoHTTPD.CookieHandler cookies)
{
   Session s = session_map.get(sid);
   if (s == null) return null;
   if (!s.validate(host,true)) return null;

   String user = args.get("userid");
   String auth = args.get("auth");
   if (for_universe.authorize(user,sid,auth)) {
      Map<String,String> rslt = s.makeValid(user,for_universe.getRole(user));
      saveSessions();
      if (rslt == null) rslt = new HashMap<String,String>();
      cookies.set(cookie,sid,1);
      rslt.put("USID",sid);
      return rslt;
    }

   return null;
}


UpodAccess.Role getRole(String sid)
{
   if (sid == null) return UpodAccess.Role.NONE;
   Session s = session_map.get(sid);
   if (s == null) return UpodAccess.Role.NONE;
   return s.getRole();
}


String getUser(String sid)
{
   if (sid == null) return null;
   Session s = session_map.get(sid);
   if (s == null) return null;
   return s.getUser();
}


/********************************************************************************/
/*										*/
/*	Session I/O methods							*/
/*										*/
/********************************************************************************/

private void loadSessions()
{
   Element xml = IvyXml.loadXmlFromFile(session_file);
   if (xml != null) {
      for (Element sxml : IvyXml.children(xml,"SESSION")) {
	 Session s = new Session(sxml);
	 host_map.put(s.getHost(),s);
	 session_map.put(s.getSessionId(),s);
       }
    }
}


private void saveSessions()
{
   try {
      IvyXmlWriter xw = new IvyXmlWriter(session_file);
      xw.begin("SESSIONS");
      for (Session s : session_map.values()) {
	 s.outputXml(xw);
       }
      xw.end("SESSIONS");
      xw.close();
    }
   catch (IOException e) { }
}



/********************************************************************************/
/*										*/
/*	Information about a session						*/
/*										*/
/********************************************************************************/

private class Session {

   private String session_id;
   private String host_id;
   private String user_id;
   private Map<String,String> url_args;
   private long   last_used;
   private boolean is_valid;
   private UpodAccess.Role user_role;

   Session(String host,String file,Map<String,String> args) {
      session_id = BasisWorld.getNewUID();
      host_id = host;
      user_id = null;
      user_role = UpodAccess.Role.NONE;
      url_args = new HashMap<String,String>(args);
      url_args.put("FILE",file);
      last_used = System.currentTimeMillis();
      is_valid = false;
    }

   Session(Element xml) {
      session_id = IvyXml.getAttrString(xml,"ID");
      host_id = IvyXml.getAttrString(xml,"HOST");
      user_id = IvyXml.getAttrString(xml,"USER");
      user_role = IvyXml.getAttrEnum(xml,"ROLE",UpodAccess.Role.NONE);
      last_used = IvyXml.getAttrLong(xml,"LAST");
      is_valid = true;
      url_args = new HashMap<String,String>();
    }

   String getSessionId()		{ return session_id; }
   String getHost()			{ return host_id; }
   String getUser() {
      if (!is_valid) return null;
      return user_id;
    }

   UpodAccess.Role getRole()		{ return user_role; }

   boolean validate(String host,boolean login) {
      long now = System.currentTimeMillis();
      if (now - last_used > SESSION_TIMEOUT) return false;
      if (!host.equals(host_id)) return false;
      if (!login && !is_valid) return false;

      last_used = now;
      user_role = for_universe.getRole(user_id);

      return true;
    }

   Map<String,String> makeValid(String userid,UpodAccess.Role role) {
      if (userid != null) {
	 user_id = userid;
	 user_role = role;
       }
      is_valid = true;
      Map<String,String> rslt = url_args;
      url_args = null;
      last_used = System.currentTimeMillis();
      return rslt;
    }

   private void outputXml(IvyXmlWriter xw) {
      if (!is_valid) return;
      xw.begin("SESSION");
      xw.field("ID",session_id);
      xw.field("HOST",host_id);
      xw.field("USER",user_id);
      xw.field("LAST",last_used);
      xw.field("ROLE",user_role);
      xw.end("SESSION");
    }

}	// end of inner class Session

}	// end of class UmonSessionManager




/* end of UmonSessionManager.java */
