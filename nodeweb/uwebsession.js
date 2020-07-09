/********************************************************************************/
/*										*/
/*		uwebsession.js							*/
/*										*/
/*	Code for handling sessions for upod web access				*/
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

var uuid = require("node-uuid");

var db = require("./uwebdatabase");




/********************************************************************************/
/*										*/
/*	Global storage								*/
/*										*/
/********************************************************************************/

var sessions = [];




/********************************************************************************/
/*										*/
/*	Create a new session							*/
/*										*/
/********************************************************************************/

function UwebSession()
{
   this.session_id = uuid.v4();
   this.db_client = db.createSession();
   this.last_time = new Date().getMilliseconds();
   this.messages = [];

   sessions[this.session_id] = this;

   this.close = 	function() { }

   this.validate =	function() {
      // if idle too long, remove session and return false
      this.last_time = new Date().getMilliseconds();
      return true;
    }

   this.push =		function(cmd,resp) {
      messages.push({ command: cmd, response: resp });
    }

   this.pop =		function() {
      var val = message.pop();
      if (val == undefined) return null;
      return val;
    }

}	// end of class UwebSession




/********************************************************************************/
/*										*/
/*	Find a session								*/
/*										*/
/********************************************************************************/

function findSession(id)
{
   return sessions[id];
}


/********************************************************************************/
/*										*/
/*	Clean up methods							*/
/*										*/
/********************************************************************************/

function cleanup(socket,tbl)
{
   if (db_client != undefined) {
     db_client.end();
    }
}



/********************************************************************************/
/*										*/
/*	Exports 								*/
/*										*/
/********************************************************************************/

exports.UwebSession = UwebSession;
exports.findSession = findSession;



/* end of uwebsession.js */

