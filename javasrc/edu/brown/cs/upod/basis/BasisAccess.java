/********************************************************************************/
/*                                                                              */
/*              BasisAccess.java                                                */
/*                                                                              */
/*      Maintain access information                                             */
/*                                                                              */
/*      This class uses the access file ($BASE/access.xml) which assigns each   */
/*      user a password and a role.  The file is of the form                    */
/*              <ACCESS>                                                        */
/*                <USER NAME='name' ROLE='READ|WRITE' PASSWORD='xxx' />         */
/*                ...                                                           */
/*              <ACCESS>                                                        */
/*      The PASSWORD entry is replaces with a SH256 code when it is seen.  A    */
/*      user entry with no NAME field serves as the default user.               */
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



package edu.brown.cs.upod.basis;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;
import edu.brown.cs.upod.upod.*;


final class BasisAccess implements UpodAccess, BasisConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private BasisUniverse           for_universe;
private UserData                default_user;
private Map<String,UserData>    user_data;
private File                    access_file;
private long                    last_modified;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

BasisAccess(BasisUniverse uu)
{
   for_universe = uu;
   default_user = null;
   user_data = new HashMap<String,UserData>();
   last_modified = 0;
   access_file = new File(uu.getBaseDirectory(),"access.xml");
   
   loadAccessData();
}


/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public UpodUniverse getUniverse()     { return for_universe; }


@Override public Role getRole(String uid)
{
   UserData ud = user_data.get(uid);
   if (ud == null) ud = default_user;
   if (ud != null) return ud.getRole();
   
   return Role.NONE;
}



/********************************************************************************/
/*                                                                              */
/*      Authorization methods                                                   */
/*                                                                              */
/********************************************************************************/

@Override public boolean authorize(String uid,String sid,String userkey)
{
   loadAccessData();
   
   UserData ud = user_data.get(uid);
   if (ud == null) {
      ud = default_user;
    }  
   if (ud == null) return false;
   if (ud.getRole() == Role.NONE) return false;
   if (ud.getHash() == null) return true;       // no password authenticates
   if (userkey == null) return false;
   
   String msg = "UPOD@";
   if (ud.getName() != null) msg += ud.getName();
   msg += "@UPOD@";
   if (ud.getHash() != null) msg += ud.getHash();
   if (sid != null) msg += "@UPOD@" + sid;
   msg += "@UPOD";
   
   String key = null;
   try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte [] rslt = md.digest(msg.getBytes());
      key = Base64.getEncoder().encodeToString(rslt);
//      key = javax.xml.bind.DatatypeConverter.printBase64Binary(rslt);
    }
   catch (NoSuchAlgorithmException e) {
      return false;
    }
   
   return key.equals(userkey);
}



/********************************************************************************/
/*                                                                              */
/*      Read access information                                                 */
/*                                                                              */
/********************************************************************************/

private void loadAccessData()
{
   if (!access_file.exists() || !access_file.canRead()) return;
   if (access_file.lastModified() <= last_modified) return;

   Element accxml = IvyXml.loadXmlFromFile(access_file);
   boolean rewrite = false;
   for (Element user : IvyXml.children(accxml,"USER")) {
      UserData ud = new UserData(user);
      if (ud.getName() != null) {
         user_data.put(ud.getName(),ud);
       }
      else if (default_user == null) default_user = ud;
      rewrite |= ud.isDirty();
    }
   
   if (rewrite) writeAccessData();
   
   last_modified = access_file.lastModified();
}




/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

private void writeAccessData()
{
   try {
      IvyXmlWriter xw = new IvyXmlWriter(access_file);
      xw.begin("ACCESS");
      for (UserData ud : user_data.values()) {
         ud.outputXml(xw);
       }
      if (default_user != null) default_user.outputXml(xw);
      xw.end("ACCESS");
      xw.close();
    }
   catch (IOException e) {
      BasisLogger.logE("Problem writing access file: " + e);
    }
}




/********************************************************************************/
/*                                                                              */
/*      User Data -- access information for a user                                                              */
/*                                                                              */
/********************************************************************************/

private static class UserData {
   
   private String user_name;
   private String user_sh256;
   private Role   user_role;
   private boolean is_dirty;
   
   UserData(Element xml) {
      is_dirty = false;
      user_name = IvyXml.getAttrString(xml,"NAME");
      user_sh256 = IvyXml.getAttrString(xml,"SH256");
      if (user_sh256 == null) {
         String pwd = IvyXml.getAttrString(xml,"PASSWORD");
         if (pwd != null) {
            try {
               MessageDigest md = MessageDigest.getInstance("SHA-256");
               byte [] rslt = md.digest(pwd.getBytes());
               user_sh256 = Base64.getEncoder().encodeToString(rslt);
//               user_sh256 = javax.xml.bind.DatatypeConverter.printBase64Binary(rslt);
               is_dirty = true;
             }
            catch (NoSuchAlgorithmException e) {
               BasisLogger.logE("Can't compute passwords");
               user_sh256 = "*";
             }
          }
       }
      user_role = IvyXml.getAttrEnum(xml,"ROLE",Role.NONE);
    }
   
   String getName()                     { return user_name; }
   String getHash()                     { return user_sh256; }
   Role getRole()                       { return user_role; }
   boolean isDirty()                    { return is_dirty; }
   
   void outputXml(IvyXmlWriter xw) {
      xw.begin("USER");
      if (user_name != null) xw.field("NAME",user_name);
      if (user_sh256 != null) xw.field("SH256",user_sh256);
      xw.field("ROLE",user_role);
      xw.end("USER");
      is_dirty = false;
    }
   
}




}       // end of class BasisAccess




/* end of BasisAccess.java */

