# rcptt testing for verinice

This ant build executes the rcptt tests for verinice.  
It takes a verince.zip and executes the tests.  

most of the configuration properties are placed in the 'gui-test.properties' file.

The default target creates some eclipse start configurations which may come handy. It also copies the testing workspace to a location ignored by git and make a copy of the 'gui-test.properties' in the local directory. Please check this file 'local/gui-test.properties' and configure it to your need.

The ant script expect the rcptt runner path in the property __runner-path__. you could set this in the 'local/gui-test.properties' or pass it as -D property when starting the script.

some control properties (can be set to change the flow of execution):

__no-clean__ prevent from cleaning the workspace  
__verinice.zip__ a path to the verinice zip, otherwise it will be search in the build directory  
__no-zip-delete__ don't delete the zip after unpacking  
__skip-tags__ - a Semicolon-separated list of tags which are skipped in the run  
__verinice.zip.selector__ - a selector expression to find the verinice zip  

### example 

init the dev environment:

     ant -file start-gui-test.xml

start all tests:

     ant -file start-gui-test.xml -Dverinice.zip={path to your zip} test-verinice-client

start a selection of tests:

     ant -file start-gui-test.xml -Dverinice.zip={path to your zip} Dtest-list=*-performance.test test-verinice-client-test-custom






