#! /bin/csh -fx


set db = upodwebdb


set host = "-h db"
set run = psql


set runcmd = '\set ON_ERROR_STOP';
set rlike = '~'
set useviews = 1
set dogrant =
set group =
set ENDTABLE = ')'
set DEFAULTDB = postgres
set ENCODE = "TEMPLATE template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8'"
set iddeftype = 'serial'

$run $host $DEFAULTDB << EOF
DROP DATABASE $db;
EOF

$run $host $DEFAULTDB << EOF
CREATE DATABASE $db $ENCODE;
EOF


$run $host $db <<EOF

$runcmd

CREATE TABLE Users (
   userid text NOT NULL,
   hostid text NOT NULL,
   PRIMARY KEY(userid)
$ENDTABLE;

CREATE INDEX users_host ON Users (hostid);

EOF



