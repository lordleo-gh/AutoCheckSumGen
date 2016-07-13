package com.jhc.chris;

import java.io.File;
import java.net.URI;
import java.nio.file.WatchEvent;

/**
 * Created by KieselmannC on 11/07/2016.
 */
public class ChangedFile extends File {
    //public File myFile;
    public WatchEvent.Kind kind;
    protected ChangedFile(String pathname){
        super(pathname);
    }
    protected ChangedFile(String parent, String child){
        super(parent, child);
    }
    protected ChangedFile(File parent, String child){
        super(parent,child);
    }
    protected ChangedFile(URI uri){
        super(uri);
    }

    static String getFileExtension(File fileToCheck) {

        if (fileToCheck == null) {
            return null;
        }
        String fileExtString = fileToCheck.getPath();
        int extensionPos = fileExtString.lastIndexOf('.');
        int lastUnixPos = fileExtString.lastIndexOf('/');
        int lastWindowsPos = fileExtString.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            fileExtString = fileExtString.substring(index + 1);
            return fileExtString.toUpperCase();
        }

    }

    public File[] listFiles()
    {
        return this.listFiles();
    }

}
