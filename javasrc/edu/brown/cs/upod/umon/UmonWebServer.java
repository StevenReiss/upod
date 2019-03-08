/********************************************************************************/
/*										*/
/*		UmonWebServer.java						*/
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



package edu.brown.cs.upod.umon;

import edu.brown.cs.upod.basis.*;
import edu.brown.cs.upod.upod.*;

import edu.brown.cs.ivy.xml.*;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import org.apache.velocity.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.*;

import org.w3c.dom.*;

import java.io.*;
import java.util.*;


class UmonWebServer implements UmonConstants
{


/********************************************************************************/
/*										*/
/*	Private Storage 							*/
/*										*/
/********************************************************************************/

private UmonControl the_control;
private int	using_port;
private HttpServer our_server;
private UmonSessionManager session_manager;
private String	session_cookie;




/********************************************************************************/
/*										*/
/*	Constructors								*/
/*										*/
/********************************************************************************/

UmonWebServer(UmonControl uc,int port)
{
   the_control = uc;

   if (port == 0) port = uc.getUniverse().getWebServerPort();
   if (port == 0) port = WEB_PORT;
   using_port = port;
   session_cookie = SESSION_COOKIE + "." + port;

   session_manager = new UmonSessionManager(uc.getUniverse());

   our_server = new HttpServer();
   try {
      our_server.start(500);
    }
   catch (IOException e) {
      BasisLogger.log("Couldn't start umon web server on port " +
	    using_port + ": " + e);
    }

   Properties p = new Properties();
   p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH,TEMPLATE_DIR);
   p.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE,"false");
   p.setProperty(RuntimeConstants.VM_LIBRARY_AUTORELOAD,"true");
   p.setProperty("velocimacro.permissions.allow.inline.to.replace.global","true");
   p.setProperty("velocimacro.permissions.allow.inline.local.scope","true");
   p.setProperty("file.resourlce.loader.modificationCheckInterval","2");

   Velocity.init(p);
}



/********************************************************************************/
/*										*/
/*	Interpret web commands							*/
/*										*/
/********************************************************************************/

private Response getResponse(String file,String host,Map<String,String> args,
	NanoHTTPD.CookieHandler cookies)
{
   if (file.startsWith("/rest/")) return getResponseREST(file,host,args,cookies);

   String sessionid = cookies.read(session_cookie);
   if (sessionid == null) sessionid = args.get("USID");

   String what = file;

   while (what.startsWith("/")) what = what.substring(1);
   int idx = what.indexOf("/");
   String path = null;
   if (idx >= 0) {
      path = what.substring(idx+1);
      what = what.substring(0,idx);
    }
											
   if (file.equals("/index.html") || file.equals("/") ||
	 file.equals("/home.html")) {
      if (sessionid == null) {
	 sessionid = session_manager.beginSession(host,file,args,cookies);
	 if (sessionid == null) sessionid = session_manager.getSessionForHost(host);
       }
      return generateResponse(HOME_PAGE,null,sessionid);
    }
   else if (what.equalsIgnoreCase("status")) {
      return generateResponse(STATUS_PAGE,null,sessionid);
    }
   else if (what.equalsIgnoreCase("command")) {
      if (!session_manager.validateSession(host,sessionid)) {
	 return HttpServer.newFixedLengthResponse(Status.FORBIDDEN,
	       TEXT_MIME,"Invalid Session");
       }
      String xmla = args.get("C");
      BasisLogger.logI("UMON: COMMAND " + xmla);
      Element xml = IvyXml.convertStringToXml(xmla);
      UmonCommand uc = UmonCommand.createCommand(the_control,xml);
      if (uc == null || !uc.isAllowed(session_manager.getRole(sessionid))) {
	 return HttpServer.newFixedLengthResponse(Status.UNAUTHORIZED,TEXT_MIME,"Operation not permitted");
       }

      String rslt = null;
      try {
	 rslt = uc.evaluate();
       }
      catch (UmonException e) {
	 rslt = "<ERROR>" + e.getMessage() + "</ERROR>";
       }
      catch (Throwable t) {
	 t.printStackTrace();
	 rslt = "<ERROR>" + t.getMessage() + "</ERROR>";
       }
      BasisLogger.logI("UMON: RESULT " + rslt);
      return HttpServer.newFixedLengthResponse(Status.OK,XML_MIME,rslt);
    }
   else if (what.equalsIgnoreCase("ping")) {
      return HttpServer.newFixedLengthResponse(Status.OK,TEXT_MIME,"PONG");
    }
   else if (what.equals("validate")) {
      Map<String,String> gourl = session_manager.validateLogin(sessionid,host,args,session_cookie,cookies);
      if (gourl == null) {
	 cookies.delete(session_cookie);
	 return HttpServer.newFixedLengthResponse(Status.FORBIDDEN,
	       TEXT_MIME,"Invalid Session");
       }											
      String gfile = gourl.remove("FILE");
      if (gfile != null) {
	 return redirectTo(gfile);
       }
    }
   else if (what.equals("logout")) {
      if (sessionid != null) {
	 session_manager.endSession(sessionid);
	 cookies.delete(session_cookie);
       }
      return redirectTo(null);
    }
   else if (what.equalsIgnoreCase("file")) {
      BasisLogger.logI("UMON: FILE: " + path);
      String mime = HTML_MIME;
      if (path.endsWith(".css")) mime = CSS_MIME;
      else if (path.endsWith(".js")) mime = JS_MIME;
      else if (path.endsWith(".svg")) mime = SVG_MIME;
      else if (path.endsWith(".png")) mime = PNG_MIME;
      else if (path.startsWith("svg/")) ;
      else if (path.equals("upod.html")) ;
      else {
	 if (sessionid == null ||
	       !session_manager.validateSession(host,sessionid)) {
	    sessionid = session_manager.beginSession(host,file,args,cookies);
	    if (sessionid == null) {
	       sessionid = session_manager.getSessionForHost(host);
	      path = "home.vel";
	     }
	  }
       }
      return generateResponse(path,mime,sessionid);
    }

   return HttpServer.newFixedLengthResponse(Status.NOT_FOUND,TEXT_MIME,"Not found");
}



/********************************************************************************/
/*										*/
/*	RESTful Response generator						*/
/*										*/
/********************************************************************************/

// URL Decoding:
//	/, /index.html, /home.html	=> HOME PAGE
//	/status 			=> STATUS PAGE
//	/ping				=> PONG response
//	/validate			=> VALIDATE LOGIN
//	/logout 			=> LOG OUT current user
//	/file/<path>			=> RETURN FILE (possibly tempated if .val)
//
//   The following can all start with /rest/
//
//	/stop					=> Exit
//	/universe				=> provide universe information
//
//	/rule/add				=> Add a new rule
//		?RULE=<xml>
//	/rule/list				=> List all rules
//	/rule/restrict				=> List restricted set of rules
//		?SENSOR=id
//		?ENTITY=id
//		?CONDITION=<xml>
//	/rule/:ruleid/replace			=> Replace an existing rule
//		?RULE=<xml>
//	/rule/:ruleid/priority			=> Change rule priority
//		?PRIORITY=<double>
//	/rule/:ruleid/remove			=> Remove a rule
//
//	/world/create				=> Create a new world from default
//	/world/:worldid/create			=> Create a new world from existing world
//	/world/:worldid/remove			=> Remove existing world
//	/world/:worldid/list			=> List state of the given world
//	/world/:worldid/settime 		=> Set time in world
//		?TIME=xxx
//	/world/:worldid/action			=> Take an action in a world
//		?ACTION=<xml>
//		?<param>=value...
//	/world/:worldid/set			=> Set value of parameter on device
//		?DEVIDE=id
//		?PARAMETER=id
//		?STATE=state
//	/world/:worldid/deduce			=> Deduce a new rule
//		?DATE=date
//		?TIME=time
//		?PARAMETERS=<xml>
//
//	/sensor/createlatch
//		?LABEL=label
//		?COND=condname
//		?DEVICE=id
//		?PARAMETER=param
//		?STATE=state
//		?AFTER=<timeafterinmillis>
//		?OFFAFTER=<timeafteroff in millis>
//		?TIME=time of day(hh:mm)
//	/sensor/createtimed
//		?LABEL=name
//		?COND=condname
//		?DEVICE=id
//		?PARAMETER=param
//		?STATE=state
//		?START=<time in millis>
//		?END=<time in millis>
//	/sensor/createfsa
//		?LABEL=name
//		?TRANSITIONS=<xml>
//			xml = <TRANSITIONS><transition>...</TRANSITIONS>
//			    <TRANSITION FROM='state' TO='state" COND='name'/>
//			    <TRANSITION FROM='state' TO='state' AFTER='time'/>
//	/sensor/createor
//		?LABEL=name
//		?CONDITIONS=xml>
//			xml = <CONDITIONS>condition...</CONDITIONS>
//				<CONDITION DEVICE='id' PARAMETER='name' STATE='value' />
//	/sensor/:sensorid/disable
//	/sensor/:sensorid/enable
//

private Response getResponseREST(String file,String host,Map<String,String> args,
      NanoHTTPD.CookieHandler cookies)
{
   String sessionid = cookies.read(session_cookie);
   if (sessionid == null) sessionid = args.get("USID");

   String what = file;
   if (what.startsWith("/rest/")) what = what.substring(6);

   while (what.startsWith("/")) what = what.substring(1);
   String [] components = what.toLowerCase().split("/");

   if (file.equals("/index.html") || file.equals("/") ||
	 file.equals("/home.html") || components.length == 0) {
      if (sessionid == null) {
	 sessionid = session_manager.beginSession(host,file,args,cookies);
	 if (sessionid == null) sessionid = session_manager.getSessionForHost(host);
       }
      return generateResponse(HOME_PAGE,null,sessionid);
    }
   else if (components[0].equals("status")) {
      return generateResponse(STATUS_PAGE,null,sessionid);
    }
   else if (components[0].equals("ping")) {
      return HttpServer.newFixedLengthResponse(Status.OK,TEXT_MIME,"PONG");
    }
   else if (components[0].equals("validate")) {
      Map<String,String> gourl = session_manager.validateLogin(sessionid,host,args,session_cookie,cookies);
      if (gourl == null) {
	 cookies.delete(session_cookie);
	 return HttpServer.newFixedLengthResponse(Status.FORBIDDEN,
	       TEXT_MIME,"Invalid Session");
       }
      String gfile = gourl.remove("FILE");
      if (gfile != null) {
	 return redirectTo(gfile);
       }
    }
   else if (components[0].equals("logout")) {
      if (sessionid != null) {
	 session_manager.endSession(sessionid);
	 cookies.delete(session_cookie);
       }
      return redirectTo(null);
    }
   else if (components[0].equals("file")) {
      int idx1 = what.indexOf("/");
      String path = what.substring(idx1+1);
      System.err.println("UMON: FILE: " + path);
      String mime = HTML_MIME;
      if (path.endsWith(".css")) mime = CSS_MIME;
      else if (path.endsWith(".js")) mime = JS_MIME;
      else if (path.endsWith(".svg")) mime = SVG_MIME;
      else if (path.endsWith(".png")) mime = PNG_MIME;
      else if (path.startsWith("svg/")) ;
      else if (path.equals("upod.html")) ;
      else {
	 if (!session_manager.validateSession(host,sessionid)) {
	    sessionid = session_manager.beginSession(host,file,args,cookies);
	    if (sessionid == null) {
	       sessionid = session_manager.getSessionForHost(host);
	       path = "home.vel";
	     }
	  }
       }
      return generateResponse(path,mime,sessionid);
    }
   else {
      if (!session_manager.validateSession(host,sessionid)) {
	 System.err.println("UMON: Invalid session " + host + " " + sessionid);
	 return HttpServer.newFixedLengthResponse(Status.FORBIDDEN,
	       TEXT_MIME,"Invalid Session");
       }
      UmonCommand uc = null;
      switch (components[0]) {
	 case "stop" :
	    uc = UmonCommand.createStopCommand(the_control);
	    break;
	 case "universe" :
	    uc = UmonCommand.createListWorldCommand(the_control,null);
	    break;
	 case "world" :
	     if (components.length == 2) {
	       switch (components[1]) {
		  case "create" :
		     uc = UmonCommand.createCreateWorldCommand(the_control,null);
		     break;
		}
	     }
	    else if (components.length > 2) {
	       String wid = components[1];
	       switch (components[2]) {
		  case "create" :
		     uc = UmonCommand.createCreateWorldCommand(the_control,wid);
		     break;
		  case "remove" :
		     uc = UmonCommand.createRemoveWorldCommand(the_control,wid);
		     break;
		  case "list" :
		     uc = UmonCommand.createListWorldCommand(the_control,wid);
		     break;
		  case "settime" :
		     long time = getLongArg(args,"TIME",0);
		     if (time > 0) {
			uc = UmonCommand.createSetTimeCommand(the_control,wid,time);
		      }
		     break;
		  case "action" :
		     Map<String,String> actargs = new HashMap<String,String>(args);
		     actargs.remove("ACTION");
		     actargs.remove("USID");
		     actargs.remove("NanoHttpd.QUERY_STRING");
		     uc = UmonCommand.createDoActionCommand(the_control,wid,
			   args.get("ACTION"),actargs);
		     break;
		  case "set" :
		     uc = UmonCommand.createSetSensorCommand(the_control,wid,
			   args.get("DEVICE"),args.get("PARAMETER"),args.get("STATE"));
		     break;
		  case "deduce" :
		     uc = UmonCommand.createDeduceRuleCommand(the_control,wid,
			   args.get("DATE"),args.get("TIME"),args.get("PARAMETERS"));
		     break;
		}
	     }
	    break;
	 case "rule" :
	    if (components.length == 2) {
	       switch (components[1]) {
		  case "add" :
		     uc = UmonCommand.createAddRuleCommand(the_control,null,args.get("RULE"));
		     break;
		  case "list" :
		     uc = UmonCommand.createListProgramCommand(the_control);
		     break;
		  case "restrict" :
		     uc = UmonCommand.createListRestrictCommand(the_control,args.get("SENSOR"),
			   args.get("ENTITY"),args.get("CONDITION"));
		     break;
		}
	     }
	    else if (components.length > 2) {
	       String rid = components[1];
	       switch (components[2]) {
		  case "replace" :
		     uc = UmonCommand.createAddRuleCommand(the_control,rid,args.get("RULE"));
		     break;
		  case "priority" :
		     double pri = getDoubleArg(args,"PRIORITY",-1);
		     if (pri >= 0) {
			uc = UmonCommand.createChangeRulePriorityCommand(the_control,rid,pri);
		      }
		     break;
		  case "remove" :
		     uc = UmonCommand.createRemoveRuleCommand(the_control,rid);
		     break;
		}
	     }
	    break;
	 case "sensor" :
	    if (components.length == 2) {
	       switch (components[1]) {
		  case "createlatch" :
		     long a1 = getLongArg(args,"AFTER",0);
		     long a2 = getLongArg(args,"OFFAFTER",0);
		     uc = UmonCommand.createNewLatchSensorCommand(the_control,
			   args.get("LABEL"),args.get("DEVICE"),args.get("PARAMETER"),
			   args.get("STATE"),args.get("COND"),a1,a2,args.get("TIME"));
		     break;
		  case "createtimed" :
		     long a3 = getLongArg(args,"START",0);
		     long a4 = getLongArg(args,"END",0);
		     uc = UmonCommand.createNewTimedSensorCommand(the_control,
			   args.get("LABEL"),args.get("DEVICE"),args.get("PARAMETER"),
			   args.get("STATE"),args.get("COND"),a3,a4);
		     break;
		  case "createfsa" :
		     String label = args.get("LABEL");
		     String trans = args.get("TRANSITIONS");
		     uc = UmonCommand.createNewFsaSensorCommand(the_control,label,trans);
		     break;
		  case "createor" :
		     label = args.get("LABEL");
		     String conds = args.get("CONDITIONS");
		     uc = UmonCommand.createNewOrSensorCommand(the_control,label,conds);
		     break;
		}
	     }
	    else if (components.length > 2) {
	       String did = components[1];
	       switch (components[2]) {
		  case "disable" :
		     uc = UmonCommand.createEnableCommand(the_control,did,false);
		     break;
		  case "enable" :
		     uc = UmonCommand.createEnableCommand(the_control,did,true);
		     break;
		}
	     }
	    break;
       }

      if (uc == null) return errorResponse();
      if (!uc.isAllowed(session_manager.getRole(sessionid))) {
	 return HttpServer.newFixedLengthResponse(Status.UNAUTHORIZED,
	       TEXT_MIME,"Operation not permitted");
       }
      String rslt = null;
      try {
	 rslt = uc.evaluate();
       }
      catch (UmonException e) {
	 rslt = "<ERROR>" + e.getMessage() + "</ERROR>";
       }
      catch (Throwable t) {
	 t.printStackTrace();
	 rslt = "<ERROR>" + t.getMessage() + "</ERROR>";
       }
      System.err.println("UMON: RESULT " + rslt);
      return HttpServer.newFixedLengthResponse(Status.OK,XML_MIME,rslt);
    }

   return null;
}



private long getLongArg(Map<String,String> args,String name,long def)
{
   String v = args.get(name);
   if (v == null || v.length() == 0) return def;
   try {
      return Long.parseLong(v);
    }
   catch (NumberFormatException e) { }
   return def;
}


private double getDoubleArg(Map<String,String> args,String name,double def)
{
   String v = args.get(name);
   if (v == null || v.length() == 0) return def;
   try {
      return Double.parseDouble(v);
    }
   catch (NumberFormatException e) { }
   return def;
}


private Response errorResponse()
{
   return HttpServer.newFixedLengthResponse(Status.NOT_FOUND,TEXT_MIME,"Not found");
}



private Response redirectTo(String gfile)
{
   if (gfile == null) gfile = "/";

   Response resp = HttpServer.newFixedLengthResponse(Status.REDIRECT,null,null);
   resp.addHeader("Location",gfile);
   return resp;
}



private Response generateResponse(String path,String mime,String sessionid)
{
   if (mime == null) {
      mime = HTML_MIME;
      if (path.endsWith(".css")) mime = CSS_MIME;
      else if (path.endsWith(".js")) mime = JS_MIME;
      else if (path.endsWith(".svg")) mime = SVG_MIME;
      else if (path.endsWith(".png")) mime = PNG_MIME;
    }

   try {
      if (path.endsWith(".vel")) {
	 if (path.startsWith("templates") || path.startsWith("/templates")) {
	    int idx = path.indexOf("/",2);
	    if (idx >= 0) path = path.substring(idx+1);
	  }
	 VelocityContext ctx = buildContext(null,sessionid);

	 StringWriter sw = new StringWriter();
	 Velocity.mergeTemplate(path,"UTF-8",ctx,sw);
	 System.err.println("RESULT: " + sw.toString());
	 return HttpServer.newFixedLengthResponse(Status.OK,HTML_MIME,sw.toString());
       }
      else {
	 File f = new File(HTML_DIR);
	 StringTokenizer tok = new StringTokenizer(path,"/");
	 while (tok.hasMoreTokens()) {
	    String pn = tok.nextToken();
	    if (pn.startsWith(".")) return null;
	    f = new File(f,pn);
	  }
	 FileInputStream fis = new FileInputStream(f);
	 return HttpServer.newChunkedResponse(Status.OK,mime,fis);
       }
    }
   catch (IOException e) {
      return HttpServer.newFixedLengthResponse(Status.NOT_FOUND,TEXT_MIME,"Not found");
    }
   catch (Throwable e) {
      return HttpServer.newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
	    TEXT_MIME,
	    "Server Internal Error: " + e.getMessage());
    }
}




/********************************************************************************/
/*										*/
/*	Construct a Velocity Context representing a world			*/
/*										*/
/********************************************************************************/

VelocityContext buildContext(UpodWorld world,String sessionid)
{
   UpodUniverse uu = the_control.getUniverse();
   UpodProgram pgm = the_control.getProgram();
   if (world == null) {
      world = pgm.getWorld(null);
    }

   VelocityContext ctx = new VelocityContext();
   ctx.put("world",world);
   ctx.put("universe",uu);
   ctx.put("program",pgm);
   ctx.put("devices",uu.getDevices());
   ctx.put("basicconds",uu.getBasicConditions());
   ctx.put("label",uu.getLabel());
   ctx.put("rules",pgm.getRules());

   ctx.put("usid",sessionid);
   ctx.put("user",session_manager.getUser(sessionid));

   return ctx;
}



/********************************************************************************/
/*										*/
/*	Http Server								*/
/*										*/
/********************************************************************************/

private class HttpServer extends NanoHTTPD implements NanoHTTPD.AsyncRunner {

   HttpServer() {
      super(using_port);
      setAsyncRunner(this);
    }

   @Override public Response serve(IHTTPSession session) {
      long start = System.currentTimeMillis();
      Map<String,String> files = new HashMap<String,String>();
      Method method = session.getMethod();
      if (Method.PUT.equals(method) || Method.POST.equals(method)) {
	 try {
	    session.parseBody(files);
	  }
	 catch (IOException e) {
	    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR,TEXT_MIME,
		  "Server Internal Error: " + e.getMessage());
	  }
	 catch (ResponseException e) {
	    return newFixedLengthResponse(e.getStatus(),TEXT_MIME,
		 e.getMessage());
	  }
       }
      Map<String,String> parms = session.getParms();
      parms.put("NanoHttpd.QUERY_STRING",session.getQueryParameterString());
      CookieHandler cookies = session.getCookies();
      String host = session.getHeaders().get("host");

      Response rslt = getResponse(session.getUri(),host,parms,cookies);
      if (rslt == null)
	 return newFixedLengthResponse(Response.Status.NOT_FOUND,TEXT_MIME,"Not Found");

      long end = System.currentTimeMillis();
      System.err.println("UMON: SERVE: " + rslt.getStatus() + " " + (end-start) + " " + Thread.currentThread().getName());

      return rslt;
    }

   @Override public void exec(ClientHandler code) {
      BasisThreadPool.start(code);
    }

   @Override public void closed(ClientHandler code) {
      BasisLogger.logD("CLOSED " + code);
    }

   @Override public void closeAll() { }

}	// end of inner class HttpServer




}	// end of class UmonWebServer




/* end of UmonWebServer.java */
