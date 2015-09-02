#!/bin/bash

echo This will copy all properties file into a flat filelist that
echo is easier to import in OmegaT for translation.
echo they can be copied back to their original spot with 
echo convert-flatfiles-to-temptranslate.sh
echo Make sure you are in the temptranslate folder:
echo $PWD
echo [enter]
read

mkdir flatfiles
find -type f | while read filename rest ; do
    export newname=`echo $filename | perl -npe 's/^\.\///; s/\//°/g;'`
    cp $filename flatfiles/$newname
done
