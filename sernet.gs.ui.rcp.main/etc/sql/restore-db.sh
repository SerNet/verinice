#!/bin/sh
#
# This Script deletes an existing PostgresSQL verinice.PRO database
# and created a new one by restoring a databse dump file.
#
# To execute this script, change to directory 'sernet.gs.ui.rcp.main/etc/sql/' first.
# Execute the script as user 'postgres'.
# 
# Parameter:
#  -p <PostgresSQL bin directory>, default: "/opt/PostgreSQL/8.4/bin/"
#  -f <Database SQL dump file>, default: "./verinicedb.sql"

postgresql_bin_directory="/opt/PostgreSQL/8.4/bin/"
database_dump_file="./verinicedb-compendium.sql"   
if [ "-p" == "$1" ]; then
    postgresql_bin_directory=$2
fi
if [ "-f" == "$3" ]; then
    database_dump_file=$4
fi
if [ "-f" == "$1" ]; then
    database_dump_file=$2
fi
if [ "-p" == "$3" ]; then
    postgresql_bin_directory=$4
fi
if [ "-h" == "${1}" ]; then
	echo "usage: $0 [-h|-p <PostgresSQL bin directory> (Add a slash in the end of the path!)|-f <Database SQL dump file>] "
	exit 1
fi
echo "Restoring verinicedb..."
echo "PostgreSQL bin directory is: ${postgresql_bin_directory}"
echo "Database SQL dump file: ${database_dump_file}"
echo "Dropping database verinicedb..."
${postgresql_bin_directory}dropdb verinicedb
echo "Creating new database verinicedb..."
${postgresql_bin_directory}createdb -O verinice verinicedb
echo "Restoring SQL dump..."
${postgresql_bin_directory}pg_restore -d verinicedb ${database_dump_file}
echo "Creating indieces..."
${postgresql_bin_directory}psql -d verinicedb -f ./create-indices.sql
