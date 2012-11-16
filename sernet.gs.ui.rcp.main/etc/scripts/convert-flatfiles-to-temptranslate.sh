#!/bin/bash

echo This will copy the flatfile list back to a temptranslate folder
echo that can be copied back into the workspace folder hierarchy
echo Make sure you are in the flatfiles folder:
echo $PWD
echo [enter]
read

mkdir temptranslate
find -type f | while read filename rest ; do
    export newname=`echo $filename | perl -npe 's/^.\///; s/°/\//g'`
    export dirname=`echo $newname | perl -nle 'm/^(.*)\/.*?$/; print $1'`
    echo $dirname
    mkdir -p temptranslate/$dirname
    cp $filename temptranslate/$newname
done
