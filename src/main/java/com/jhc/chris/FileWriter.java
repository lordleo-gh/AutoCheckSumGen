package com.jhc.chris;

import org.ini4j.Wini;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;

/**
 * Created by KieselmannC on 07/07/2016.
 */
public class FileWriter implements Runnable {
    private static FileWriter instance = null;
    protected FileWriter() {
        // Exists only to defeat instantiation.
    }
    public static FileWriter getInstance() {
        if(instance == null) {
            instance = new FileWriter();
        }
        return instance;
    }

    public void run() {

    }

    private void CreateMetaFile(File file) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String md5HashValue = null;
        boolean writesuccess;
        do {
            writesuccess = true;
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
                System.out.println("md5 Hash is " + md5HashValue + "Modified Date is " + sdf.format(file.lastModified()));
            } catch (NoSuchAlgorithmException e) {
                System.out.println("No such algorythm");
                writesuccess = false;
            } catch (FileNotFoundException e) {
                System.out.println(e.toString());
                writesuccess = false;
            } catch (IOException e) {
                System.out.println("IO exception");
                writesuccess = false;
            } catch (Exception e) {
                System.out.println("Unknown error");
                writesuccess = false;
            }


            // Properties prop = new Properties();
            Wini ini = null;
            FileOutputStream output = null;

            try {
                //ini = new Wini(new File(file.getPath() + ".metafile"));
                ini = new Wini();
                output = new FileOutputStream(file.getPath() + ".metafile");

                ini.put(file.getName(), "FileMD5Signature", md5HashValue);
                ini.put(file.getName(), "FileCreationDate", "");
                ini.put(file.getName(), "FileModificationDate", sdf.format(file.lastModified()));
                ini.put(file.getName(), "FileSize", String.valueOf(file.length()));
                ini.put(file.getName(), "FileVersion", "");

                // save properties
                //      ini.store(output);

            } catch (IOException io) {
                io.printStackTrace();
                writesuccess = false;
            }
        } while (!writesuccess);
    }
}

