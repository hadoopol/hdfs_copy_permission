package com.hadoopol.parsecmd;

import com.hadoopol.Hadoop;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.io.IOException;
import java.util.concurrent.Callable;

@Command(name = "fromfile")
public class FromFile implements Callable<Integer> {
    @Option(names = "-i", description = "Read input file", paramLabel = "inputFile", required = true)
    private String inputFile;

    @Option(names = "-d", description = "Debug.", paramLabel="debug")
    private boolean debug;

    @Override
    public Integer call() throws IOException {
        Hadoop hadoop = new Hadoop(this.debug);
        hadoop.deserialize(this.inputFile);
        hadoop.showPermissions();
        return 0;
    }

}
