/********************************************************************************/
/*										*/
/*		uwebprotocol.js 						*/
/*										*/
/*	Handle communications protocol with the actual client			*/
/*										*/
/********************************************************************************/
/*	Copyright 2013 Brown University -- Steven P. Reiss			*/
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

var querystring = require("querystring");
var crypto = require("crypto");

var database = require("./uwebdatabase");
var session = require("./uwebsession");



/********************************************************************************/
/*										*/
/*	Global storage								*/
/*										*/
/********************************************************************************/

var USERID = "keywords[keywords][userid]";
var HOSTID = "keywords[keywords][hostid]";
var KEYID = "keywords[keywords][keyid]";
var SESSID = "keywords[keywords][session]";
var DATA = "keywords[keywords][data]";

var login_queue = new Object();




/********************************************************************************/
/*										*/
/*	Entries for handling requests from in-home server			*/
/*										*/
/********************************************************************************/

function poll(data,response)
{
   var q = querystring.parse(data);
   var host = q[hostid];
   var login = login_queue[host];
   if (login == null) {
      sendBackXml("<NONE />");
    }
   login1(login,resp);
}




function attach(data,response)
{
   var q = querystring.parse(data);
   var host = q[hostid];
   var login = login_queue[host];
   var key = q[keyid];
   if (login == null || login.pending == null) {
      sendBackXml(resp,"<FAIL REASON='Login not in progress' />");
    }
   else if (key == undefined) {
      sendBackXml(login.pending,"<FAIL REASON='Couldn't Validate' />");
      sendBackXml(resp,"<FAIL REASON='Not validated' />");
    }
   else {
      login2(login,key,resp);
    }
}




function register(data,response)
{
   var q = querystring.parse(data);
   var host = q[HOSTID];
   var user = q[USERID];
   // validateHostFormat(host);
   // validateUserName(user);
   register0(host,user,response);
}




function check(data,response)
{
   var q = querystring.parse(data);
   var sessid = q[SESSID];
   var sess = session.findSession(sessid);
   if (sess == null || !sess.validate()) {
      sendBackXml(response,"<FAIL REASON='Invalidate or stale session' />");
    }
   else {
      var cmd = sess.pop();
      if (cmd == null) {
	 sendBackXml(response,"<NONE />");
       }
      else {
	 sess.pending = response;
	 sendBackXml(response,cmd.command);
       }
    }
}



function data(data,response)
{
   var q = querystring.parse(data);
   var sessid = q[SESSID];
   var sess = session.findSession(sessid);
   if (sess.pending == null) {
      sendBackXml(response,"<FAIL REASON='No pending query' />");
    }
   else {
      var x = q[DATA];
      sendBackXml(sess.pending,x);
      sess.pending = null;
      sendBackXml(response,"<OK />");
    }
}



/********************************************************************************/
/*										*/
/*	Methods for handling requests for users 				*/
/*										*/
/********************************************************************************/

function login(data,response)
{
   var q = querystring.parse(data);
   var user = q[USERID];
   var pwd = q[PASSWD];
   login0(user,pwd,response);
}




function command(data,response)
{
}



/********************************************************************************/
/*										*/
/*	Handle standard messages						*/
/*										*/
/********************************************************************************/

function notFound(response)
{
   response.writeHead(404,{"Content-Type" : "text/plain" });
   response.write("404 Not found");
   response.end();
}



function sendBackXml(response,xml)
{
   response.writeHead(200,{"Content-Type" : "text/xml" });
   response.write(xml);
   resonse.end();
}




/********************************************************************************/
/*										*/
/*	Registration logic							*/
/*										*/
/********************************************************************************/

function register0(host,user,resp)
{
   var db = database.createSession();

   var query = db.query({ text: 'SELECT hostid FROM Users WHERE userid = $1',
			  values: [user]
			 });
   query.on('row',function (row,result) { result.addRow(row); });
   query.on('end',function(result) {
	       if (result.rowCount != 0) {
		  sendBackXml(resp,"<FAIL REASON='User Name Taken' />");
		}
	       else register1(db,host,user,resp); });
}


function register1(db,host,user,resp)
{
   var query = db.query({ text: 'SELECT userid FROM Users WHERE hostid = $1',
			  values: [host]
			 });
   query.on('row',function (row,result) { result.addRow(row); });
   query.on('end',function(result) {
	       if (result.rowCount != 0) {
		  sendBackXml(resp,"<FAIL REASON='Host already registered' />");
		}
	       else register2(db,host,user,resp); });
}



function register2(db,host,user,resp)
{
   var query = db.query({ text: 'INSERT INTO Users VALUES ( $1, $2 )',
			  values : [user, host ]
			 });
   query.on('end',function(result) {
	       if (result.error != null) {
		  sendBackXml(resp,"<FAIL REASON='Not unique' />");
		}
	       else {
		  sendBackXml(resp,"<ACCEPT />");
		}
	       db.close();
	     });
}




/********************************************************************************/
/*										*/
/*	Login logic								*/
/*										*/
/********************************************************************************/

function login0(user,pwd,resp)
{
   var db = database.createSession();
   var host = null;
   var query = db.query({ text: 'SELECT hostid FROM Users WHERE userid = $1',
			  values: [user]
			 });
   query.on('row',function (row,result) { host = row['hostid']; });
   query.on('end',function(result) {
	       if (result.rowCount == 0 || host == null) {
		  sendBackXml(resp,"<FAIL REASON='Invalid user id' />");
		}
	       else {
		  login_queue[host] = { userid : user, hostid : host, password: pwd, response: response };
		}
	       });
}



function login1(login,resp)
{
   var hc = null;
   crypto.randomBytes(48,function (ex,buf) { hc = buf.toString('hex'); });
   var xml = "<ATTACH PWD='" + login.password + "' HC='" + hc + "' USER='" + login.userid + "'/>";
   sendBackXml(login.response,xml);
   login.response  = null;
   login.pending = resp;
}




function login2(login,key,resp)
{
    var sess = new UWebSession();
    sess.userid = login.userid;
    sess.hostid = login.hostid;
    sess.keyid = key;
    var xml = "<SESSION ID='" + sess.session_id + "' />";
    sendBackXml(login.pending,xml);
    sendBackXml(resp,xml);
    login.pending = null;
}



/********************************************************************************/
/*										*/
/*	Exports 								*/
/*										*/
/********************************************************************************/

exports.poll = poll;
exports.attach = attach;
exports.check = check;
exports.data = data;
exports.register = register;
exports.login = login;
exports.command = command;



/* end of uwebprotocol.js */


















