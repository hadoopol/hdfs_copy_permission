package com.hadoopol;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.sun.security.auth.module.UnixSystem;
import com.esotericsoftware.kryo.io.Output;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclStatus;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.UserGroupInformation;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hadoop {
    private Configuration confSource;
    private FileSystem fsSource;
    private Path rootPath;
    private Boolean recursive;
    private List<FileDescription> files;
    private Logger logger;

    private void appendFile(FileDescription fd){
        this.files.add(fd);
    }

    public Boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    public List<FileDescription> getFiles() {
        return files;
    }

    public void setFiles(List<FileDescription> files) {
        this.files = files;
    }

    public Path getRootPath() {
        return rootPath;
    }

    public void setRootPath(Path rootPath) {
        this.rootPath = rootPath;
    }

    public Configuration getConfSource() {
        return confSource;
    }

    public void setConfSource(Configuration confSource) {
        this.confSource = confSource;
    }

    public FileSystem getFsSource() {
        return fsSource;
    }

    public void setFsSource(FileSystem fsSource) {
        this.fsSource = fsSource;
    }

    private void setConfiguration(Configuration c, String key, String value){
        c.set(key,value);
    }

    private void init_logger(Boolean debug) throws IOException {
        FileHandler handler = new FileHandler("Copypermissions.log");
        handler.setFormatter(new SimpleFormatter());
        this.logger = Logger.getLogger(Hadoop.class.getName());
        if(debug) {
            this.logger.setLevel(Level.FINE);
            this.logger.addHandler(handler);
        }else
            this.logger.setLevel(Level.INFO);
    }

    private void init(String namenode, String rootPath, Boolean recursive, Boolean debug) throws IOException{
        setRootPath(new Path(rootPath));
        setConfiguration(getConfSource(),"fs.defaultFS", "hdfs://" + namenode + ":8020/");
        setConfiguration(getConfSource(),"fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        setFsSource(FileSystem.get(getConfSource()));
        setFiles(new ArrayList<>());
        setRecursive(recursive);
        init_logger(debug);
    }

    public Hadoop(String namenode, String rootPath, Boolean recursive, Boolean debug) throws IOException {
        setConfSource(new Configuration());
        init(namenode, rootPath, recursive, debug);
    }

    public Hadoop(String namenode, String rootPath, Boolean recursive, Boolean debug, String kuser, String kTicketCacheFile) throws IOException {
        setConfSource(new Configuration());
        kerberosEnable(kuser, kTicketCacheFile);
        init(namenode, rootPath, recursive, debug);
    }

    public Hadoop(Boolean debug) throws IOException {
        init_logger(debug);
    }

    private int kerberosEnable(String kuser, String kTicketCacheFile){
        setConfiguration(getConfSource(),"hadoop.security.authentication", "kerberos");
        UserGroupInformation.setConfiguration(getConfSource());
        long uid = new UnixSystem().getUid();
        kTicketCacheFile = kTicketCacheFile==null?"/tmp/krb5cc_"+uid:kTicketCacheFile;
        try {
            UserGroupInformation.getUGIFromTicketCache(kTicketCacheFile,kuser);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return 0;
    }

    public boolean getPermissions() {
        Queue<Path> fileQueue = new LinkedList<>();
        fileQueue.add(getRootPath());
        while (!fileQueue.isEmpty()) {
            Path filePath = fileQueue.remove();
            String pathString = Path.getPathWithoutSchemeAndAuthority(filePath).toString();
            FileStatus fsStat = null;
            try {
                fsStat = getFsSource().getFileStatus(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            FsPermission fsPerm = fsStat.getPermission();
            FileDescription file = new FileDescription();
            file.setFilename(pathString);
            file.setPermission(fsPerm.toString());
            this.logger.fine(" Checking if Acl is enabled for "+pathString);
            file.setAclEnable(false);
            try {
                AclStatus aclFile = getFsSource().getAclStatus(filePath);
                List<AclEntry> listAcl = aclFile.getEntries();
                if ((listAcl.size())>0){
                    file.setAclEnable(true);
                    this.logger.fine("ACL enabled");
                    file.setListAcl(listAcl);
                }
            } catch (IOException e) {
                this.logger.fine("ACL not enabled");
            }

            if (!fsStat.isFile() && isRecursive()) {
                FileStatus[] fileStatus = new FileStatus[0];
                try {
                    fileStatus = getFsSource().listStatus(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (FileStatus fileStat : fileStatus) {
                    fileQueue.add(fileStat.getPath());
                }
            }
            appendFile(file);
        }
        return true;
    }

    public void showPermissions() {
        Iterator iArray = getFiles().iterator();
        while (iArray.hasNext()) {
            FileDescription file = (FileDescription) iArray.next();
            this.logger.fine(" Filename: " + file.getFilename());
            this.logger.fine(" Permission: " + file.getPermission());
            System.out.println("normal,"+file.getFilename()+","+file.getPermission());
            this.logger.fine(" Checking if Acl is enabled for "+file.getFilename());
            if(file.isAclEnable()) {
                this.logger.fine(" Acl is enable");
                Iterator iListAcl = file.getListAclEntry().iterator();
                while (iListAcl.hasNext()) {
                    AclEntry acl = (AclEntry) iListAcl.next();
                    System.out.println("acl,"+file.getFilename()+","+acl.toString());
                }
            }
        }
    }

    public void serialize(String filename){
        Kryo kryo = new Kryo();
        kryo.register(FileDescription.class);
        kryo.register(ArrayList.class);
        try {
            Output output = new Output(new FileOutputStream(filename));
            kryo.writeClassAndObject(output, getFiles());
            output.close();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public void deserialize(String filename){
        Kryo kryo = new Kryo();
        kryo.register(FileDescription.class);
        kryo.register(ArrayList.class);
        try{
            Input input = new Input( new FileInputStream(filename));
            Object o = kryo.readClassAndObject(input);
            setFiles((List<FileDescription>)o);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    public int setPermissions(String path){
        Iterator iArray = getFiles().iterator();
        Pattern splitPattern = Pattern.compile("\\G([rwx-]{3})([rwx-]{3})([rwxt-]{3})");
        Pattern userPattern = Pattern.compile("\\Guser::.*");
        Pattern groupPattern = Pattern.compile("\\Ggroup::.*");
        Pattern otherPattern = Pattern.compile("\\Gother::.*");
        Pattern pathPatternBegin = Pattern.compile("\\G("+path+").*");
        Pattern pathPattern = Pattern.compile("\\G"+path+"$");
        while (iArray.hasNext()) {
            FileDescription file = (FileDescription) iArray.next();
            Path currPath = new Path(file.getFilename());
            if( ! pathPatternBegin.matcher(file.getFilename()).matches()) continue;
            if( pathPattern.matcher(file.getFilename()).matches() || isRecursive()){
                try {
                    if (file.isAclEnable()) {
                        List<AclEntry> listAcl = file.getListAclEntry();
                        Matcher m ;
                        Iterator iListAclPermission = file.getListAcl().iterator();

                        int foundUser, foundGroup, foundOther;
                        foundUser=0;
                        foundGroup=0;
                        foundOther=0;
                        while (iListAclPermission.hasNext()){
                            String aclPerm = (String)iListAclPermission.next();
                            if ((userPattern.matcher(aclPerm).matches())) {
                                foundUser = 1;
                            }else if((groupPattern.matcher(aclPerm).matches())){
                                foundGroup = 1;
                            }else if((otherPattern.matcher(aclPerm).matches())) {
                                foundOther = 1;
                            }else if((foundUser+foundGroup+foundOther) == 3){
                                break;
                            }
                        }

                        if (foundUser==0) {
                            m = splitPattern.matcher(file.getPermission());
                            m.matches();
                            String userPerm = "user::"+m.group(1);
                            listAcl.add(AclEntry.parseAclEntry(userPerm,true));
                        }
                        if (foundGroup==0) {
                            m = splitPattern.matcher(file.getPermission());
                            m.matches();
                            String groupPerm = "group::"+m.group(2);
                            listAcl.add(AclEntry.parseAclEntry(groupPerm,true));
                        }
                        if (foundOther==0){
                            m = splitPattern.matcher(file.getPermission());
                            m.matches();
                            String otherPerm = "other::"+m.group(3);
                            listAcl.add(AclEntry.parseAclEntry(otherPerm,true));
                        }

                        getFsSource().setAcl(currPath, listAcl);
                    }else{
                        FsPermission fspermission = FsPermission.valueOf("="+file.getPermission());
                        this.fsSource.setPermission(currPath, fspermission);
                    }
                } catch (IOException e) {
                    this.logger.fine(" Error applying permission to : "+file.getFilename());
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}