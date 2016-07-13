package com.jhc.chris;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by KieselmannC on 12/07/2016.
 */
public class ThreadList {
    private static ThreadList uniqueInstance = new ThreadList();
    private final List<Thread> RunningWriterThreads = new CopyOnWriteArrayList<Thread>();
//    public void setRunningWriterThreads(List<Thread> RunningWriterThreads){
//        this.RunningWriterThreads = RunningWriterThreads;
//    }
    public List<Thread> getRunningWriterThreads(){
//        if (this.RunningWriterThreads == null)
//        {
//            RunningWriterThreads = new CopyOnWriteArrayList<Thread>();
//        }
        return RunningWriterThreads;
    }
    private ThreadList() {}

    public static ThreadList getInstance(){
        return uniqueInstance;
    }
}

