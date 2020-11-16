# HDFS tool to copy/save permission.

* Summary 
* How it works
* Download and build the tool
* Using the tool
* Todo
* BUGS
* Developer

### Summary 

The purpose of this tool is to copy HDFS file permissions from one cluster to another or to just generate a local file with current HDFS file permissions for audit purposes.

### How it works

The steps to get current cluster permission and apply to other cluster are:

1. Get the HDFS current permission from the cluster running the tool and choosing the option "getpermission". By default the permissions are listed to screen only.

1. The tool offer option to save a list with files and permissions to a local file. The file can be used as source of file permissions and for audit purposes as well. 

1. Run the tool again choosing the option "setpermission" and select the destination cluster to apply the permission. An inputfile with permissions is required

### Download and build the tool

1. Clone the repository using git ```git clone <repo>```

1. Build the the tool using maven```mvn clean package```

1. A file named copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar will be build.


### Using the tool 

1 -  Running the tool and looking the help dialog

>java -jar copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar getpermission fromcluster

```
Missing required options: '-n=namenodeHost', '-p=path'
Usage: subcommands getpermission fromcluster [-dr] [-k=User] -n=namenodeHost
       [-o=outputFile] -p=path [-t=TicketCacheFile]
  -d                    Debug.
  -k=User               Kerberos user credentials accessing cluster
  -n=namenodeHost       HDFS namenode hostname
  -o=outputFile         Save permissions to output file
  -p=path               HDFS directory/file
  -r                    Get permissions recursively.
  -t=TicketCacheFile    Kerberos ticket cache file. Usually is located in
                          /tmp/krb5cc_+uid. If in doubt, type klist
```  
2 - Connecting to namenode located on host cluster1.hadoopol.com, getting permission from directory /user/kleber_povoacao recursively and saving output to permission.out 

>java -jar target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar getpermission fromcluster -n cluster1.hadoopol.com -r -o permission.out -p /user/kleber_povoacao

3 - Listing the permissions saved in file permission.out

>java -jar target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar getpermission fromfile -i permission.out 

```
 normal,/user/kleber_povoacao,rwxr-xr-x
 normal,/user/kleber_povoacao/.Trash,rwx------
 normal,/user/kleber_povoacao/.sparkStaging,rwxr-xr-x
 normal,/user/kleber_povoacao/out,rwx------
 normal,/user/kleber_povoacao/out/airports_lat_40_plus.text,rwx------
 normal,/user/kleber_povoacao/testPerm/noacl,rwxr-xr-x
 normal,/user/kleber_povoacao/testPerm/withacl,rwxrwxr-x
 acl,/user/kleber_povoacao/testPerm/withacl,group::r-x
 acl,/user/kleber_povoacao/testPerm/withacl,group:dirperm:rwx
```

4 - Running the tool and looking the help dialog

>java -jar copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar setpermission

```
Missing required options: '-n=namenodeHost', '-p=path', '-i=inputFile'
Usage: subcommands setpermission [-dr] -i=inputFile [-k=User] -n=namenodeHost
                                 -p=path [-t=TicketCacheFile]
  -d                    Debug.
  -i=inputFile          Read input file
  -k=User               Kerberos user credentials accessing cluster
  -n=namenodeHost       HDFS namenode hostname
  -p=path               HDFS directory/file.
  -r                    Set permissions recursively.
  -t=TicketCacheFile    Kerberos ticket cache file. Usually is located in
                          /tmp/krb5cc_+uid. If in doubt, type klist
```

5 - Reading file permission.out and applying permissions in the cluster DR1.hadoopol.com recursively on path /user/kleber_povoacao

>java -jar target/copypermission-1.0-SNAPSHOT-jar-with-dependencies.jar setpermission -i permission.out -n DR1.hadoopol.com -d -r -p /user/kleber_povoacao

### TODO

1. [ ] Improve the logs
1. [ ] Create unit tests
1. [ ] Improve functional tests
1. [ ] Create method to compare two permission files

### BUGS

### DEVELOPER

If you want to be a contributor send a mail to developer at hadoopol.com