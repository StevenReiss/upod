/********************************************************************************/
/*                                                                              */
/*              UpodAccess.java                                                 */
/*                                                                              */
/*      Access control for a universe                                           */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.upod.upod;



public interface UpodAccess
{


/**
 *      Permission types
 **/

// Might want other roles to allow investigation or setting of properties in
// either real or artificial worlds.  
// Writing state of the world might be different from writing rule set

enum Role {
   NONE,
   READ,
   WRITE
};



/**
 *      Return the associated universe
 **/

UpodUniverse getUniverse();


/**
 *      Check access for a user
 **/

Role getRole(String uid);



/**
 *      Authorize a user.  This method takes a user name (uid) and a random string (sid, 
 *      typically the session id) that has been passed to the web server.  It checks that
 *      the key string passed back is the hash of the uid,sid, and proper password.
 **/

boolean authorize(String uid,String sid,String key);


}       // end of interface UpodAccess




/* end of UpodAccess.java */

