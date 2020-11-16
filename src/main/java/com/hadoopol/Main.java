package com.hadoopol;
import com.hadoopol.parsecmd.GetPermission;
import com.hadoopol.parsecmd.SetPermission;
import picocli.CommandLine;
import java.util.List;


@CommandLine.Command(name = "subcommands", subcommands = { GetPermission.class, SetPermission.class})
public class Main implements Runnable{

    @Override
    public void run(){
        System.out.println("Use: [ getpermission or setpermission ]");
    }

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
