package com.hadoopol.parsecmd;

import com.hadoopol.Hadoop;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "setpermission")
public class SetPermission implements Callable<Integer> {
    @Option(names = "-n", description = "HDFS namenode hostname", paramLabel="namenodeHost", required = true)
    private String namenode;

    @Option(names = "-p", description = "HDFS directory/file.", paramLabel = "path", required = true)
    private String path;

    @Option(names = "-i", description = "Read input file", paramLabel = "inputFile", required = true)
    private String inputFile;

    @Option(names = "-k", description = "Kerberos user credentials accessing cluster", paramLabel="User")
    private String kuser;

    @Option(names = "-t", description = "Kerberos ticket cache file. Usually is located in /tmp/krb5cc_+uid. If in doubt, type klist", paramLabel="TicketCacheFile")
    private String kTicketCacheFile;

    @Option(names = "-r", description = "Set permissions recursively.", paramLabel="recursive")
    private boolean recursive;

    @Option(names = "-d", description = "Debug.", paramLabel="debug")
    private boolean debug;

    @Override
    public Integer call() throws IOException {
        Hadoop hadoop;
        if(this.kuser == null){
            hadoop = new Hadoop(this.namenode, this.path, this.recursive, this.debug);
        }else {
            hadoop = new Hadoop(this.namenode, this.path, this.recursive, this.debug, this.kuser, this.kTicketCacheFile);
        }
        hadoop.deserialize(this.inputFile);
        return hadoop.setPermissions(path);
    }
}
