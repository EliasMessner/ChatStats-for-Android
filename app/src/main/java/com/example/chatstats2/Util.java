package com.example.chatstats2;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
    
    public static int[] getLocationOnScreen(View view) {
        int[] outLocation = new int[2];
        view.getLocationOnScreen(outLocation);
        return outLocation;
    }

    public static String capitalizeFirstLowerRest(String input) {
        if (input.length() <= 1) return input.toUpperCase();
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    /**
     * Returns a new list of double values of the same length as the input list, where every value
     * in the new list is the average of the values preceding and succeeding the same index of the
     * original list (including the index itself).
     * @param originalValues the original list
     * @param range the amount of preceding and succeeding values to use for average.
     * @return smoothed list
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<Double> smoothValues(ArrayList<Long> originalValues, int range) {
        List<Double> smoothValues = new ArrayList<>();
        for (int i = 0; i < originalValues.size() - 1; i++) {
            double avg = originalValues.subList(Math.max(i-range, 0), Math.min(i+range, originalValues.size()-1)).stream().mapToLong(a -> a).average().orElse(0);
            smoothValues.set(i, avg);
        }
        return smoothValues;
    }
}
