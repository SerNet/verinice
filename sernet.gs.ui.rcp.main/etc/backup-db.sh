#!/bin/sh
#backup script:
#--------------------------------------------------

set -- `date`
export DAY=$1
export TIME=`echo $4|cut -d: -f1`
set --

pg_dump -Upostgres veriniceDB > \
/var/backups/verinice/veriniceDump$DAY-$TIME.sql
#--------------------------------------------------

