#!/usr/bin/env bash
#//TODO:  This test should be improved some day
set -x
BASEDIR=$(dirname $0)
cd $BASEDIR

tempDir=$(mktemp -d)

#Variables
namenode="horton"

#prepare files and dirs
whoami=$(whoami)
testDir=/user/${whoami}/testPerm
hdfs dfs -rm -r ${testDir}
hdfs dfs -mkdir ${testDir}
hdfs dfs -mkdir ${testDir}/withacl
hdfs dfs -mkdir ${testDir}/noacl
hdfs dfs -mkdir ${testDir}/withacl/dir1
hdfs dfs -mkdir ${testDir}/withacl/dir2
hdfs dfs -mkdir ${testDir}/noacl/dir1
hdfs dfs -mkdir ${testDir}/noacl/dir2

touch ${tempDir}/t1perm
touch ${tempDir}/t2perm
hdfs dfs -put ${tempDir}/t1perm ${testDir}/withacl/dir1
hdfs dfs -put ${tempDir}/t2perm ${testDir}/noacl
rm -rf ${tempDir}/t1perm ${tempDir}/t2perm

hdfs dfs -setfacl -m group:dirperm:rwx ${testDir}/withacl
hdfs dfs -setfacl -m group:dirperm:r-x ${testDir}/withacl/dir1/t1perm

j="java -jar ../../target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar "
$j getpermission fromcluster -r -n ${namenode} -o permission.bin -p ${testDir}
$j getpermission fromfile -i permission.bin
echo List of file permission colected. Press ENTER to continue
read
hdfs dfs -setfacl -b ${testDir}/withacl/dir1/t1perm
hdfs dfs -setfacl -b ${testDir}/withacl
$j setpermission -i permission.bin -n ${namenode} -p ${testDir}/withacl
hdfs dfs -getfacl ${testDir}/withacl
hdfs dfs -getfacl ${testDir}/withacl/dir1/t1perm
echo Only whithacl should have permission applied. Press ENTER to continue
read
$j setpermission -i permission.bin -n ${namenode} -r -p ${testDir}/withacl
hdfs dfs -getfacl ${testDir}/withacl
hdfs dfs -getfacl ${testDir}/withacl/dir1/t1perm
echo file whithacl and file t1perm should have permission applied. Press ENTER to continue
read


