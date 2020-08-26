/********************************************************************************/
/*										*/
/*		zoomwebserver.js						*/
/*										*/
/*	Main node.js server for upod zoom monitoring				*/
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
var fs = require('fs');
var request = require("request");
var https = require('https');



/********************************************************************************/
/*										*/
/*	HTTPS credentials							*/
/*										*/
/********************************************************************************/

const private_key = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/privatekey.pem','utf8');
const certificate = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/cert.pem','utf8');
const ca = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/chain.pem','utf8');

const credentials = {
   key : private_key,
   cert : certificate,
   ca: ca
};



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
   app.get('/zoomauth',handleAuth);
   app.get('/status',handleStatus);

   var sapp = https.createServer(credentials,app);
   var httpsserver = sapp.listen(6060);
   var httpserver = app.listen(6061);

   console.log("UPOD Node.JS server has started");
}


function decoder(req,res,fct)
{
   var data = "";
   req.setEncoding("utf8");
   req.addListener("data",function(chunk) { data += chunk; });
   req.addListener("end",function() { fct(data,res); });
}



function handleAuth(req,res)
{
   console.log("AUTH RESPONSE",req);
}


function handleStatus(req,res)
{
   console.log("STATUS",req);
}


function requestToken()
{
   let cid = fs.readFileSync('zoom.id');
   let csec = fs.readFileSync('zoom.secret');

   let options = {
      method: "POST",
      url: "https//zoom.us/oauth/token",
      qs: {
	  grant_type: 'authorization_code',
	  code: 'xxxxxx',
	  redirect_uri: 'https://conifer2.cs.brown.edu:6060/zoomauth'
       },
      headers: {
	  Authorization: 'Basic ' + Buffer.from(cid + ":" + csec).toString('base64')
       }
    };

   request(options,function(err,resp,body) {
	      if (error) throw new Error(error);
	      console.log(body);
	    } );
}




start();

exports.start = start;



/* end of uwebserver.js */
