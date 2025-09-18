package com.stayintouch.kioskapp.lib;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SaveAndLoad {
    //Source: https://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android

    public static void writeToFile(String filename, String data, Context ctx) {
        try {
            FileOutputStream fou = ctx.openFileOutput(filename, Context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fou);
            try {
                osw.write(data);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String filename, Context context) {
        String ret = "";

        try {
            InputStream inputStream;
            File file = new File(new File(context.getFilesDir(), "Video"), filename);
            inputStream = new FileInputStream(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String receiveString;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (IOException e) {
            Log.e("readFromFile", "Error reading file: " + e.toString());
        }

        return ret;
    }

    public static InputStream readFromFileAndReturnInputStream(String filename, Context context) {
        try {
            File file = new File(new File(context.getFilesDir(), "Video"), filename);
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e("readFromFile", "File not found: " + e.toString());
        }
        return null;
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
