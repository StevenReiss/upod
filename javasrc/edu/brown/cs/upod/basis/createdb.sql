#! /bin/csh -f

set host = "-h db"
set dbname = "upod"

dropdb $host $dbname
createdb $host $dbname -T template0 -E utf8

psql $host $dbname <<EOF

CREATE SEQUENCE user_ids_seq START WITH 1001;


CREATE TABLE users (
   userid int NOT NULL DEFAULT NEXTVAL('user_ids_seq'),
   username text UNIQUE,
   email text UNIQUE,
   salt char(32),
   password text,
   valid boolean DEFAULT FALSE
);


EOF
