#!/bin/sh

#set -v
#set -x


for jar in lib/bdbxml-4.3.29/lib/*.jar; do
    jars=$jar:$jars
done

junit="/home/aprack/java/eclipse/plugins/org.junit4_4.1.0/junit-4.1.jar"
libPath="${PWD}/lib/bdbxml-4.3.29/lib"

export LD_LIBRARY_PATH=${libPath}

java -cp bin:${jars}${junit} \
 -Djava.library.path="$libPath" \
 sernet.hui.server.connect.TestBdbXml $@

