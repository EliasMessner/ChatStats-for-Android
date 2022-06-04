package com.example.chatstats2;

import android.app.Activity;
import android.content.ContentResolver;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Util {

    private Util() {
        throw new UnsupportedOperationException("Utility class and its subclasses must not be instantiated.");
    }

    public static String getTextFromContentProviderUri(android.net.Uri contentUri, Activity context) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();
        InputStream is = contentResolver.openInputStream(contentUri);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder result = new StringBuilder();
        for (String line; (line = br.readLine()) != null; ) {
            result.append(line).append('\n');
        }
        return result.toString();
    }

    public static String stackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String getFileContentAsString(String filePath) throws IOException {
        StringBuilder contentAsString = new StringBuilder();
        File myExternalFile = new File(filePath);
        FileInputStream fis = new FileInputStream(myExternalFile);
        DataInputStream in = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null) {
            contentAsString.append(strLine).append("\n");
        }
        br.close();
        in.close();
        fis.close();
        return contentAsString.toString();
    }

    public static String[] wordTokenize(String text) {
        return clean(text).split("\\s+");
    }

    public static String clean(String text) {
        String punctuations = "\\p{Punct}";
        return text.replaceAll(punctuations, " ");
    }
    
    public static int[] getLocationOnWindow(View view) {
        int[] outLocation = new int[2];
        view.getLocationInWindow(outLocation);
        return outLocation;
    }
}
