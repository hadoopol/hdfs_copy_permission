#!/usr/bin/env bash
#Script used for debug purpose only.

BASEDIR=$(dirname $0)
cd $BASEDIR

#Variables
namenode="horton"

echo "type debug"
read op
if [[ ${op} == "debug" ]]; then
  j="java -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y -jar ../../target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar "
else
  j="java -jar ../../target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar "
fi

whoami=$(whoami)
testDir="/user/${whoami}/testPerm"
$j setpermission -i permission.bin -n ${namenode} -r -p ${testDir}/withacl


#run java process debug mode.
#-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:
