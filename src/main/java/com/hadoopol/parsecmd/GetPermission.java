package com.hadoopol.parsecmd;
import picocli.CommandLine.Command;
import java.util.concurrent.Callable;

@Command(name = "getpermission", subcommands = { FromFile.class, FromCluster.class})
public class GetPermission implements Callable<Integer> {

    @Override
    public Integer call(){
        System.out.println("fromfile or fromcluster");
        return 0;
    }
}
