package com.jhc.chris;

import org.ini4j.Wini;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import static java.nio.file.StandardWatchEventKinds.*;
import java.util.UUID;

/**
 * Created by KieselmannC on 07/07/2016.
 */
public class MetadataFileWriter implements Runnable {

    private final ChangedFile file;
    private final UUID uuid = UUID.randomUUID();

//    public void setChangedFile(ChangedFile changedFile){
//        this.changedFile = changedFile;
//    }
//    public synchronized ChangedFile getChangedFile(){
//        return changedFile;
//    }

    public MetadataFileWriter(ChangedFile changedFile){
        this.file = changedFile;
       // uuid = UUID.randomUUID();
    }
    // --- Start Singleton Pattern to have only one Filewriter instance dealing with all
//    private static MetadataFileWriter instance = new MetadataFileWriter();
 //   private ChangedFileLog fileChangesLog = new ChangedFileLog();

//    protected MetadataFileWriter() {
//        // Exists only to defeat instantiation.
//    }
//    public static MetadataFileWriter getInstance() {
//        return instance;
//    }
    // --- End Singleton Pattern

   // private ChangedFileLog fileChangesLog;

    public void run() {

        if (file != null) {
            if (file.kind != ENTRY_DELETE){
                CreateMetaFile();
            } else {
                DeleteMetaFile();
            }
            ClearUp();
        }


    }

    private void CreateMetaFile() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String md5HashValue = null;
        Boolean Success;
        do {
            Success = true;
            try {
            //    System.out.println("Sleep");
                Thread.sleep(1000);
            } catch (InterruptedException ix) {
                Thread.currentThread().interrupt();
            }
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");

                FileInputStream fis = new FileInputStream(file);

                byte[] dataBytes = new byte[1024];

                int nread = 0;
                while ((nread = fis.read(dataBytes)) != -1) {
                    md.update(dataBytes, 0, nread);
                }
                fis.close();
                byte[] mdbytes = md.digest();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < mdbytes.length; i++) {
                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                md5HashValue = sb.toString();
                System.out.println(file.getAbsolutePath() + " md5 Hash is " + md5HashValue + " Modified Date is " + sdf.format(file.lastModified()));
            } catch (NoSuchAlgorithmException e) {
                System.out.println("No such algorythm");
                Success = false;
            } catch (FileNotFoundException e) {
                System.out.println(e.toString());
                Success = false;
            } catch (IOException e) {
                System.out.println("IO exception");
                Success = false;
            } catch (Exception e) {
                System.out.println("Unknown error");
                Success = false;
            }


            // Properties prop = new Properties();
            Wini ini = null;
            FileOutputStream output = null;

            try {
                //ini = new Wini(new File(file.getPath() + ".metafile"));
                ini = new Wini();
                output = new FileOutputStream(file.getPath() + ".metadata");

                ini.put(file.getName(), "FileMD5Signature", md5HashValue);
                ini.put(file.getName(), "FileCreationDate", "01/01/1999");
                ini.put(file.getName(), "FileModificationDate", sdf.format(file.lastModified()));
                ini.put(file.getName(), "FileSize", String.valueOf(file.length()));
                ini.put(file.getName(), "FileVersion", "0.0.0.0");

                // save properties
                ini.store(output);

            } catch (IOException io) {
                io.printStackTrace();
                Success = false;
            }
        } while (!Success);
        System.out.format("%s: Ended %s\n", uuid, file.getAbsolutePath());
//        ThreadList RunningThreadList = ThreadList.getInstance();
//        for (Thread WriterThread: RunningThreadList.getRunningWriterThreads()){
//            if (WriterThread.getName().equals(file.getAbsolutePath())){
//                // Remove finished threads from list - causes ConcurrentModifcationException
//                RunningThreadList.getRunningWriterThreads().remove(WriterThread);
//            }
//        }
    }


    private void DeleteMetaFile(){
        File metaFile = new File(file.getAbsolutePath() + ".metadata");
        if (metaFile.exists()){
            metaFile.delete();
        }
    }

    private void ClearUp(){
        System.out.format("%s Ended %s\n", uuid, file.getAbsolutePath());
        ThreadList RunningThreadList = ThreadList.getInstance();
        for (Thread WriterThread: RunningThreadList.getRunningWriterThreads()){
            if (WriterThread.getName().equals(file.getAbsolutePath())){
                // Remove finished threads from list - causes ConcurrentModifcationException
                RunningThreadList.getRunningWriterThreads().remove(WriterThread);
            }
        }
        System.out.format("File %s", file.getAbsolutePath());
        System.out.println("----------------");
        for (Thread WriterThread: RunningThreadList.getRunningWriterThreads()){
            System.out.format("%s \n",WriterThread);
        }
        System.out.format("-------%s--------\n", RunningThreadList.getRunningWriterThreads().size());
    }
}
