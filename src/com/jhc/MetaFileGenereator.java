package com.jhc;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Created by KieselmannC on 27/06/2016.
 */
public class MetaFileGenereator {

    static void CreateMetaFile(File file){

        String PropertyValue = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            PropertyValue = sb.toString();
            System.out.println("MD5 Hash: " + PropertyValue);



        }
        catch (NoSuchAlgorithmException e) {
            System.err.println("No such algorythm");
        }
        catch (FileNotFoundException e){
            System.err.println("File not found");
        }
        catch (IOException e) {
            System.err.println("IO Exception");
        }

        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream(file.getPath() + ".metafile");

            // set the properties value
            prop.setProperty("FileMD5Signature",PropertyValue);

            // save properties to project root folder

            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //static void CreateMetaFile(dict)

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
                return fileExtString;
            }

    }
}
