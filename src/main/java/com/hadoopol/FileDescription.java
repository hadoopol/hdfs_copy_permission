package com.hadoopol;

import org.apache.hadoop.fs.permission.AclEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileDescription{
    String filename;
    List<String> listAcl;
    boolean aclEnable;
    String permission;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<AclEntry> getListAclEntry() {
        List<AclEntry> newlistAcl = new ArrayList<AclEntry>();
        Iterator iArray = getListAcl().iterator();
        while (iArray.hasNext()) {
            String acl = (String) iArray.next();
            newlistAcl.add(AclEntry.parseAclEntry(acl, true));
        }
        return newlistAcl;
    }

    public List<String> getListAcl() {
        return this.listAcl;
    }

    public void setListAcl(List<AclEntry> listAcl) {
        this.listAcl = new ArrayList<String>();
        Iterator iArray = listAcl.iterator();
        while (iArray.hasNext()) {
            AclEntry acl = (AclEntry) iArray.next();
            this.listAcl.add(acl.toString());
        }
    }

    public boolean isAclEnable() {
        return aclEnable;
    }

    public void setAclEnable(boolean aclEnable) {
        this.aclEnable = aclEnable;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

}

