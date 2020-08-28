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
var bodyparser = require('body-parser');
var morgan = require('morgan');


/********************************************************************************/
/*										*/
/*	HTTPS credentials							*/
/*										*/
/********************************************************************************/

const private_key = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/privkey.pem','utf8');
const certificate = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/cert.pem','utf8');
const ca = fs.readFileSync('/etc/letsencrypt/live/conifer2.cs.brown.edu/chain.pem','utf8');
const eventtoken = fs.readFileSync('zoom.event','utf8').trim();
const personalmtg = fs.readFileSync('zoom.personal','utf8').trim();
const personalid = fs.readFileSync('zoom.whoami','utf8').trim();

const credentials = {
   key : private_key,
   cert : certificate,
   ca: ca
};

/********************************************************************************/
/*										*/
/*	Status Result								*/
/*										*/
/********************************************************************************/

var status = {
   in_other: 0,
   in_personal : false,
   personal_active : false,
   wait_count: 0,
   active_count : 0,
}


/********************************************************************************/
/*										*/
/*	Local Storage								*/
/*										*/
/********************************************************************************/

var current_code = "test";



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
  
   app.use(morgan('combined'));
   app.use(bodyparser.urlencoded({ extended : false }));
   app.use(express.json({}));

   app.get('/zoomauth',handleAuth);
   app.get('/zoomtoken',handleToken);
   app.post("/zoomhook",handleWebHook)
   app.get("/zoomhook",handleWebHookCheck);
   app.get('/status',handleStatus);
   app.all('*',handle404);

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
   console.log("AUTH RESPONSE",req.query);

   let code = req.query.code;

   res.type('txt').send("OK");
}


function handleToken(req,res)
{
   console.log("AUTH RESPONSE",req.body);

   res.type('txt').send("OK");
}


function handleStatus(req,res)
{
   res.end(JSON.stringify(status));

}



function handleWebHook(req,res)
{
    console.log("WEBHOOK");

    let clientid = req.headers.authorization;
    if (clientid != eventtoken) {
            console.log("BAD CLIENT ID ",clientid,eventtoken);
    }
    let event = req.body.payload;
    let what = req.body.event;
    let meeting = event.object;
    let who = meeting.participant;
    console.log(what,meeting,who);

    switch (what) {
      case 'meeting.started' :
         if (meeting.id == personalmtg) status.personal_active = true;
         break;
      case 'meeting.ended' :
         if (meeting.id == personalmtg) status.personal_active = false;
         break;
      case 'meeting.participant_joined_waiting_room' :
         if (meeting.id == personalmtg) status.wait_count++;
         break;
      case 'meeting.participant_left_waiting_room' :
         if (meeting.id == personalmtg) {
            status.wait_count--;
            if (status.num_waiting < 0) status.wait_count = 0;
         }
         break;
      case 'meeting.participant_jbh_joined' :
         if (meeting.id == personalmtg) status.active_count++;
         break;
      case 'meeting.participant_jbh_waiting' :
         if (meeting.id == personalmtg) status.num_waiting++;
         break;
      case 'meeting.participant_joined' :
         if (meeting.id == personalmtg) {
            if (who.user_id == personalid) status.in_personal = true;
            else status.active_count++
         }
         else if (who.user_id == personalid) status.in_other++;
         break;
      case 'meeting.participant_left' :
         if (meeting.id == personalmtg) {
            if (who.user_id == personalid) status.in_personal = false;
            else {
               status.active_count--;
               if (status.active_count < 0) status.active_count = 0;
            }
         }
         else if (who.user_id == personalid) {
            status.in_other--;
            if (status.in_other < 0) status.in_other = 0;
         }
         break;
      default :
         console.log("UNKNOWN EVENT",what);
         break;
    }
    
    res.status(200);
    res.type('txt').send("OK");
//     let evt = JSON.parse(req.body);
//     console.log("RECEIVED",evt);
}


function handleWebHookCheck(req,res)
{
    console.log("WEBHOOK",req);
    res.status(200);
    res.type('txt').send("OK");
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
	  code: current_code,
	  redirect_uri: 'https://conifer2.cs.brown.edu:6060/zoomtoken'
       },
      headers: {
	  Authorization: 'Basic ' + Buffer.from(cid + ":" + csec).toString('base64')
       }
    };

   request(options,function(err,resp,body) {
	      if (error) throw new Error(error);
	      console.log("REQUEST",body);
	    } );
}


function handle404(req,res)
{
        res.status(404);
        res.type('txt').send("Not Found");
}



start();

exports.start = start;



/* end of uwebserver.js */
