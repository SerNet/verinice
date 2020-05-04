#!/bin/bash
# COPY ALL PROPERTIES FILES OUT OF PROJECT FOR TRANSLATION (i.e. with OmegaT)
# execute this in directory which contains "sernet.gs.ui.rcp.main"

echo Please make sure that you are in the workspace directory
echo that contains sernet.gs.ui.rcp.main and other projects:
echo $PWD
echo [enter]
read

echo "This will copy all files that need to be translated (.properties, cheatsheets, etc.) into a new directory strucutre called 'temptranslate'. Ok?"
echo [enter]
read


find ./ ! -path '*classes*' ! -path '*.metadata*' ! -path '*bin*' ! -path '*target*' ! -path './sernet.gs.service/src/sernet/verinice/service/bp/importer/messages.properties' \
-iname '*messages*.properties' \
-or -iname 'plugin*.properties' \
-or -iname 'bundle*.properties' \
-or -iname 'root*xhtml' \
-or -path '*/cheatsheets/*.xml' |\
rsync -av --files-from=- . ./temptranslate/
