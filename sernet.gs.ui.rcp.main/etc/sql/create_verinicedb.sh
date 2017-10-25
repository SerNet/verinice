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
postgresql_bin_directory=""
if [ "-p" == "$1" ]; then
    postgresql_bin_directory=$2
fi
if [ "-h" == "${1}" ]; then
	echo "usage: $0 [-h|-p <PostgresSQL bin directory>] (Add a slash in the end of the path!)"
	exit 1
fi
${postgresql_bin_directory}dropdb verinicedb
${postgresql_bin_directory}createdb -O verinice verinicedb
${postgresql_bin_directory}pg_restore -d verinicedb ./verinicedb.sql.tar.gz
