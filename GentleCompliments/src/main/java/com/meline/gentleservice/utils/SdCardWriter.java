package com.meline.gentleservice.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SdCardWriter {
    private File mFile;

    public SdCardWriter(String fileName) {
        File externalStorageDir = Environment.getExternalStorageDirectory();
        this.mFile = new File(externalStorageDir, fileName);
    }

    private boolean deleteIfGreater(long bites){
        if(mFile.length() > bites){
            mFile.delete();
            return true;
        }

        return false;
    }

    public void appendNewLine(String text){
        try {

            if(!mFile.exists()){
                mFile.createNewFile();
            }else{
                long FILE_MAX_SIZE = 2000000; // 2 MB
                if(this.deleteIfGreater(FILE_MAX_SIZE)){
                    this.appendNewLine(text);
                }
            }

            FileOutputStream fileOutputStream = new FileOutputStream(mFile, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.append("\r\n");
            outputStreamWriter.append(text);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (Exception e) {
        }
    }
}
