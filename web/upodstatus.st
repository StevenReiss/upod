<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>UPOD CONTROL PAGE</title>
<style type="text/css">
.header {
    background-color: #FF9;
        height: 40px;
        width: 98%;
        vertical-align: middle;
    text-align:center;
    font-family: sans-serif;
    margin-left:auto;
    margin-right:auto;
    min-width:675px;
}
.headertitle {
        font-size: 36px;
        text-align: center;
        vertical-align: middle;
}
.body {
    min-width:675px;
    width:98%;
    height:400px;
    margin-left:auto;
    margin-right:auto;

}
.navigation {
        background-color: #FF9;
        height: 100%;
        width: 20%;
        float: left;
    text-align:center;
}
.navigation li {
        font-size: 24px;
        list-style-type: none;
}
.main {
        background-color: #CFF;
        height: 400px;
    width:80%;
    float:right;
    text-align:center;

}
    table {
        border-style:solid;
        border-width:2px;
        border-spacing:0px;
        text-align:left;
    }

.sensortable {
        width: 80%;
    margin-left:auto;
    margin-right:auto;
}
.entitytable {
        width: 80%;
    margin-left:auto;
    margin-right:auto;
}
.mainheader {
        font-size: large;
        font-weight: bold;
        text-align: center;
    font-family:sans-serif;
}
button {
    width:120px;
    height:40px;
    font-family:'times new roman';
    font-size:19px;
    text-align:left;
    margin-left:5px;
    margin-right:5px;
    margin-top:2px;
}
    .top{
        margin-top:5px;
    }

</style>
</head>

<body>
<div class='header'>
<span class='headertitle'>UPOD Control Page</span>
</div>
<div class="body">
<div class='navigation'>
    <button class="top">Status</button><br />
<button>Command</button><br />
<button>Rules</button><br />
<button>Rule Editor</button>

</div>
<div class='main'>
  <p class='mainheader'>Sensors</p>
  <table border="1" class='sensortable'>
    <tr>
      <th scope="col" width="10%">NAME</th>
      <th scope="col" width="10%">STATE</th>
      <th scope="col">DESCRIPTION</th>
    </tr>
    $sensors:{sensor|<tr>
       <td>$sensor.name$</td>
       <td>$sensor.currentState$</td>
       <td>$sensor.description</td>
       </tr>}$
  </table>
  <p class='mainheader'>Entities</p>
  <table border="1" class='entitytable'>
    <tr>
      <th scope="col" width="10%">NAME</th>
      <th scope="col" width="10%">STATE</th>
      <th scope="col">DESCRIPTION</th>
    </tr>
    <tr>
      <td>e1</td>
      <td>e1 state</td>
      <td>Entity 1 description goes here</td>
    </tr>
    <tr>
      <td>e2</td>
      <td>e2 state</td>
      <td>Entity 2 description goes here </td>
    </tr>
  </table>
</div>
</div>
</body>
</html>
