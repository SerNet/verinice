# rcptt testing for verinice

This ant build executes the rcptt tests for verinice.  
It takes a verince.zip and executes the tests.  

most of the configuration properties are placed in the 'gui-test.properties' file.

The default target (init.dev.enviorment) creates some eclipse start configurations which may come handy. It also copies the testing workspace to a location ignored by git and make a copy of the 'gui-test.properties' in the local directory. Please check this file 'local/gui-test.properties' and configure it to your need.

The ant script expect the rcptt runner path in the property __runner-path__. you could set this in the 'local/gui-test.properties' or pass it as -D property when starting the script.

some control properties (can be set to change the flow of execution):

__no-clean__ prevent from cleaning the workspace  
__verinice.zip__ a path to the verinice zip, otherwise it will be search in the build directory  
__no-zip-delete__ don't delete the zip after unpacking  
__skip-tags__ - a Semicolon-separated list of tags which are skipped in the run  
__verinice.zip.selector__ - a selector expression to find the verinice zip  
__no-war-copy__ - don't copy and patch the war file  
__no-server-deployment__ - don't deploy and start the tomcat server

for the complete list refer to the 'local/gui-test.properties'.

### main targets

* init.dev.enviorment - default target
* clean - clean the resources
* test-verinice-client - runs all tests in client mode
* test-verinice-client-test-custom - runs a selection of tests, provide by the property test-list
* test-verinice-server - runs all tests in server mode
* test-verinice-server-test-custom - runs a selection of tests, provide by the property test-list
* start-tomcat-server - start the server
* stop-tomcat-server - stop the server


#### examples 

init the dev environment:

     ant -file start-gui-test.xml

start all tests:

     ant -file start-gui-test.xml -Dverinice.zip={path to your zip} test-verinice-client

start a selection of tests:

     ant -file start-gui-test.xml -Dverinice.zip={path to your zip} Dtest-list=*-performance.test test-verinice-client-test-custom


To test in three tier mode you need an tomcat server and the verinice war. Configure the coresponding properties in the properties files or add the properties via -D switch.

start all tests:

     ant -file start-gui-test.xml test-verinice-server

start a selection of tests:

     ant -file start-gui-test.xml Dtest-list=*-performance.test test-verinice-server-test-custom

some other useful examples:

     ant -f start-gui-test.xml start-tomcat-server

Start the configured tomcat server by deploying and patching the war.

     ant -f start-gui-test.xml -Dno-clean=true  -Dno-server-deployment=true test-verinice-server

Start the tests against a running server, note when the server was deployed with this ant script you need to add the no-clean option as the server workspace would be cleared otherwise.
