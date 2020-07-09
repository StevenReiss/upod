/********************************************************************************/
/*										*/
/*		uwebserver.js							*/
/*										*/
/*	Main node.js server for upod master web server				*/
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

var express = require("express");

var session = require("./uwebsession");
var protocol = require("./uwebprotocol");





/********************************************************************************/
/*										*/
/*	Default handlers							*/
/*										*/
/********************************************************************************/

function errorHandler(response)
{
   response.writeHead(404,{"Content-Type" : "text/plain" });
   response.write("404 Not found");
   response.end();
}




/********************************************************************************/
/*										*/
/*	Main program								*/
/*										*/
/********************************************************************************/

function start()
{
   var app = express();
   app.use(express.logger());
   app.use('/web/',express.static(__dirname + "/web/"));
   app.use('/poll',function(req,res) { decoder(req,res,protocol.poll); });
   app.use('/attach',function(req,res) { pollHandler(req,res,protocol.attach); });
   app.use('/check',function(req,res) { pollHandler(req,res,protocol.check); });
   app.use('/data',function(req,res) { pollHandler(req,res,protocol.data); });
   app.use('/register',function(req,res) { pollHandler(req,res,protocol.register); });
   app.use('/user',function(req,res) { pollHandler(req,res,protocol.login); });
   app.use('/cmd',function(req,res) { pollHandler(req,res,protocol.command); });
   app.use('/',express.static(__dirname + "/web/"));

   var http = app.listen(8989);

   console.log("UPOD Node.JS server has started");
}


function decoder(req,res,fct)
{
   var data = "";
   req.setEncoding("utf8");
   req.addListener("data",function(chunk) { data += chunk; });
   req.addListener("end",function() { fct(data,res); });
}





start();

exports.start = start;



/* end of uwebserver.js */
