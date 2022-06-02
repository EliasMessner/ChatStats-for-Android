package com.example.chatstats2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Provides an interface for getting and writing shared preferences and settings, as well as
 * predicting optimal preferences for given chat file.
 */
public class SettingsHandler {

    Context context;

    public SettingsHandler(Context context) {
        this.context = context;
    }

    /**
     * Calls getBestDateAndTimePattern and writes the predicted optimal patterns to shared
     * preferences.
     * @param fileText the raw file text as exported by WhatsApp.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setAutoDateTimePref(String fileText) {
        String[] bestDateAndClockFormat = getBestDateAndTimeFormat(fileText);
        String bestDateFormat = bestDateAndClockFormat[0];
        String bestClockFormat = bestDateAndClockFormat[1];
        writeSharedPrefString(context.getResources().getString(R.string.date_pattern_settings_key), bestDateFormat, null);
        writeSharedPrefString(context.getResources().getString(R.string.time_pattern_settings_key), bestClockFormat, null);
        Toast.makeText(context, R.string.pref_set_auto_done_message, Toast.LENGTH_LONG).show();
    }

    /**
     * Predicts the best matching date and time pattern for a given file text, and returns them as a
     * length-2 String array, where the first element is the predicted optimal date pattern, and the
     * second element is the predicted optimal time pattern.
     * The best matching pattern is the one which can parse the most messages from the file.
     * @param fileText the raw file text as exported by WhatsApp.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String[] getBestDateAndTimeFormat(String fileText) {
        String[] datePatterns = context.getResources().getStringArray(R.array.date_pattern_pref_values);
        String[] timePatterns = context.getResources().getStringArray(R.array.time_pattern_pref_values);
        ArrayList<String[]> dateTimePatterns = new ArrayList<>();
        for (String dp : datePatterns) {
            for (String tp : timePatterns) {
                dateTimePatterns.add(new String[] {dp, tp});
            }
        }

        Comparator<String[]> dateTimePatternComparator = (dateTimePattern1, dateTimePattern2) -> {
            // the pattern string which can parse more messages will be ranked higher
            String datePattern1 = dateTimePattern1[0];
            String timePattern1 = dateTimePattern1[1];
            String datePattern2 = dateTimePattern2[0];
            String timePattern2 = dateTimePattern2[1];
            int score1, score2;
            try {
                Chat chat1 = Chat.parseChat(fileText, String.join(", ", dateTimePattern1), getDateTimeLookAhead(datePattern1, timePattern1));
                score1 = chat1.getMessageList().size();
            } catch (DateTimeParseException | AssertionError | IllegalArgumentException e) {
                score1 = 0;
            }
            try {
                Chat chat2 = Chat.parseChat(fileText, String.join(", ", dateTimePattern2), getDateTimeLookAhead(datePattern2, timePattern2));
                score2 = chat2.getMessageList().size();
            } catch (DateTimeParseException | AssertionError | IllegalArgumentException e) {
                score2 = 0;
            }
            return Integer.compare(score1, score2);
        };

        return Collections.max(dateTimePatterns, dateTimePatternComparator);
    }

    /**
     * Writes given String value to given key in shared preferences, and makes a Toast message if
     * the passed message is not null.
     * @param key the key to be stored to
     * @param val the value to be stored
     * @param message the message to be toasted. If null, nothing will be toasted.
     */
    public void writeSharedPrefString(String key, String val, String message) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, val);
        editor.apply();
        if (message != null)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public String getDateTimePattern() {
        String datePattern = getDatePattern();
        String timePattern = getTimePattern();
        return datePattern + ", " + timePattern;
    }

    public String getTimePattern() {
        String time_pattern_settings_key = context.getString(R.string.time_pattern_settings_key);
        String default_time_pattern = context.getResources().getString(R.string.time_pattern_us);
        return PreferenceManager.getDefaultSharedPreferences(context).getString(time_pattern_settings_key, default_time_pattern);
    }

    public String getDatePattern() {
        String date_pattern_settings_key = context.getString(R.string.date_pattern_settings_key);
        String default_date_pattern = context.getResources().getString(R.string.date_pattern_us);
        return PreferenceManager.getDefaultSharedPreferences(context).getString(date_pattern_settings_key, default_date_pattern);
    }

    /**
     * Returns the date-time-lookahead pattern for the date and time patterns currently stored in
     * shared preferences.
     */
    public String getDateTimeLookAhead() {
        return getDateTimeLookAhead(getDatePattern(), getTimePattern());
    }

    /**
     * Returns the date-time-lookahead pattern for the date and time patterns specified as
     * parameters.
     */
    public String getDateTimeLookAhead(String datePattern, String timePattern) {
        String dateLookahead = getDateLookahead(datePattern);
        String clockLookahead = getTimeLookahead(timePattern);
        return "\\s+(?=" + dateLookahead + ", " + clockLookahead + ")";
    }

    /**
     * Returns the date-lookahead pattern for a specified date-pattern.
     * The mapping from date patterns to date lookahead patterns is read from shared preferences.
     */
    public String getDateLookahead(String datePattern) {
        LinkedHashMap<String, String> patternMapLookAhead = getLinkedHashMap(R.array.date_pattern_keys, R.array.date_look_ahead_values);
        return patternMapLookAhead.get(datePattern);
    }

    /**
     * Returns the time-lookahead pattern for a specified time-pattern.
     * The mapping from time patterns to time lookahead patterns is read from shared preferences.
     */
    public String getTimeLookahead(String timePattern) {
        LinkedHashMap<String, String> patternMapLookAhead = getLinkedHashMap(R.array.time_pattern_keys, R.array.time_look_ahead_values);
        return patternMapLookAhead.get(timePattern);
    }

    /**
     * Takes two resource IDs as input, pointing to arrays.
     * Returns a LinkedHashMap from these arrays, with the first array being the keys and the
     * second array being the values.
     * If the arrays differ in size, the remaining values of the longer one are ignored.
     * @param resIdKeys resource id for keys-array
     * @param resIdValues resource id for values-array
     */
    public LinkedHashMap<String, String> getLinkedHashMap(@ArrayRes int resIdKeys, @ArrayRes int resIdValues) {
        String[] keys = context.getResources().getStringArray(resIdKeys);
        String[] values = context.getResources().getStringArray(resIdValues);
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(keys.length, values.length); ++i) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

}
