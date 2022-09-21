package com.afoxxvi.alopex.util;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {
    public static String readStream(InputStream inputStream) {
        try {
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] buff = new char[1024];
            int hasRead = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while ((hasRead = reader.read(buff)) > 0) {
                stringBuilder.append(new String(buff, 0, hasRead));
            }
            inputStream.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static InputStream getFileStream(Context context, String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String inputFile(Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            char[] buff = new char[1024];
            int hasRead = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while ((hasRead = reader.read(buff)) > 0) {
                stringBuilder.append(new String(buff, 0, hasRead));
            }
            inputStream.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void outputFile(Context context, String fileName, String document) {
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            PrintStream printStream = new PrintStream(outputStream);
            printStream.print(document);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
