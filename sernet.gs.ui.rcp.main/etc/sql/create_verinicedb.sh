#!/bin/sh
#
# This Script creates an initial PostgresSQL database for verinice.PRO.
# To execute change to directory 'sernet.gs.ui.rcp.main/etc/sql/'.
# Execute as user 'postgres'.
#
# Execution steps:
#
# - Drop database verinicedb.
# - Create a new database verinicedb
# - Import file verinicedb.sql.tar.gz into verinicedb
#
# verinicedb.sql.tar.gz creates all tables and indices.
# It imports an organization with two accounts: 
# An admin account with login 'rr' and password 'geheim' and 
# an normal user with login 'nn' and password 'geheim'.
#
dropdb verinicedb
createdb -O verinice verinicedb
pg_restore -d verinicedb ./verinicedb.sql.tar.gz
