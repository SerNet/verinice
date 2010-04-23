#!/bin/sh
# execute this in directory which contains "sernet.gs.ui.rcp.main"
find ./ ! -path '*classes*' ! -path '*.metadata*' ! -path '*bin*' \
-iname '*messages*.properties' -or -iname 'plugin*.properties' |\
rsync -av --files-from=- . /home/dm/temp/2010-04-22/i18n/
