/********************************************************************************/
/*                                                                              */
/*              upodlibrary.js                                                  */
/*                                                                              */
/*      Code for managing library display and selection                         */
/*                                                                              */
/********************************************************************************/




/********************************************************************************/
/*                                                                              */
/*      Variables                                                               */
/*                                                                              */
/********************************************************************************/

var image_map = new Object();



/********************************************************************************/
/*                                                                              */
/*      Initializations                                                         */
/*                                                                              */
/********************************************************************************/

$(function() {
    loadLibrary(setupLibraryPage);
});




/********************************************************************************/
/*                                                                              */
/*      Handle loading library data                                             */
/*                                                                              */
/********************************************************************************/

function loadLibrary(post)
{
   var tgt = window.parent;
   var eid = tgt.upodEntity;
   var tid = tgt.upodTransition;
   var pid = tgt.upodParameter;

   var q = "<LIBRARY PROPERTY='" + pid + "'>";
   q += "<DEVICE ID='" + eid + "'/>";
   if (tid != null) q += "<TRANSITION NAME='" + tid + "'/>";
   q += "</LIBRARY>";

   loadJson(q,function(libdata) {
               var lib = [];
               var elts = makeArray(libdata.ELEMENT);
               for (var i = 0; i < elts.length; ++i) {
                  var elt = elts[i];
                  var lbl = getAttr(elt,"LABEL");
                  var val = getTextElement(elt,"VALUE");
                  lib.push({ label: lbl, value: val });
                }
               post(lib);
             });
}




/********************************************************************************/
/*                                                                              */
/*      Handle setup after library data is loaded                               */
/*                                                                              */
/********************************************************************************/

function setupLibraryPage(elts)
{
   var tbl = "<table>";
   for (var i = 0; i < elts.length; ++i) {
      var elt = elts[i];
      var enc = Base64.encode(elt.value);
      var hc = elt.value.hashCode();
      image_map[hc] = elt;

      tbl += "<tr><td>";
      tbl += "<a onclick='selectImage(" + hc + ")' >";
      tbl += "<img class='libimage' width='160' height='120' src='data:image/svg+xml;base64,";
      tbl+= enc + "'></a></td>";
      tbl += "<td><span class='liblabel'><a onclick='selectImage(" + hc + ")'>" + elt.label;
      tbl += "</a></span></td></tr>";
    }
   tbl += "</table>";
   $('#libtable').html(tbl);
}





/********************************************************************************/
/*                                                                              */
/*      Post selected image to svg editor                                       */
/*                                                                              */
/********************************************************************************/

function selectImage(id)
{
   var target = window.parent;
   var elt = image_map[id];

   // Send metadata (also indicates file is about to be sent)
   var meta_str = JSON.stringify({ name: elt.label, id: elt.label });
   target.postMessage(meta_str, "*");

   data = '|' + elt.label + '|' + elt.value;
   target.postMessage(data,"*");

   return false;
}




/* end of upodlibrary.js */
