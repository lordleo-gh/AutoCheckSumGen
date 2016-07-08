package com.jhc.chris;

import java.io.File;
import java.util.LinkedHashMap;


/**
 * Created by KieselmannC on 08/07/2016.
 */
public class ChangedFileLog {
    LinkedHashMap<String,File> changedFiles;

    public void addItem(File ItemToAdd){
        String temp = ItemToAdd.getAbsoluteFile().toString();
        changedFiles.put(temp,ItemToAdd);
        // checks if item is in if it is then ignore else add to top
    }

    public File getOldestItem(){
        // gets oldest item and removes it from list
        String key = changedFiles.entrySet().iterator().next().getKey();
        File value = changedFiles.get(key);
        changedFiles.remove(key);
        return value;
    }


}


