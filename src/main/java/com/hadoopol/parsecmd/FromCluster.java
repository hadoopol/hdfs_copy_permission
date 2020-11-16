package com.hadoopol.parsecmd;

import com.hadoopol.Hadoop;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "fromcluster")
public class FromCluster implements Callable<Integer> {
    @Option(names = "-n", description = "HDFS namenode hostname", paramLabel="namenodeHost", required = true)
    private String namenode;

    @Option(names= "-p", description = "HDFS directory/file", paramLabel = "path", required = true)
    private String path;

    @Option(names = "-o", description = "Save permissions to output file", paramLabel = "outputFile")
    private String outputFile;

    @CommandLine.Option(names = "-k", description = "Kerberos user credentials accessing cluster", paramLabel="User")
    private String kuser;

    @CommandLine.Option(names = "-t", description = "Kerberos ticket cache file. Usually is located in /tmp/krb5cc_+uid. If in doubt, type klist", paramLabel="TicketCacheFile")
    private String kTicketCacheFile;

    @Option(names = "-r", description = "Get permissions recursively.", paramLabel="recursive")
    private boolean recursive ;

    @Option(names = "-d", description = "Debug.", paramLabel="debug")
    private boolean debug;

    @Override
    public Integer call() throws IOException {
        if(this.kuser == null){
            Hadoop hadoop = new Hadoop(this.namenode, this.path, this.recursive, this.debug);
            hadoop.getPermissions();
            if (this.outputFile != null) {
                hadoop.serialize(this.outputFile);
            }else{
                hadoop.showPermissions();
            }
        }else {
            Hadoop hadoop = new Hadoop(this.namenode, this.path, this.recursive, this.debug, this.kuser, this.kTicketCacheFile);
            hadoop.getPermissions();
            if (this.outputFile != null) {
                hadoop.serialize(this.outputFile);
            } else {
                hadoop.showPermissions();
            }
        }
        return 0;
    }
}
