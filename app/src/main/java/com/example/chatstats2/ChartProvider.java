package com.example.chatstats2;

import static com.example.chatstats2.Util.capitalizeFirstLowerRest;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

public class ChartProvider {

    Context context;
    Toast toast;

    public ChartProvider(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PieChart getPieChart(SortedMap<String, Long> data, String label, String title) {
        List<PieEntry> entries = new ArrayList<>();
        data.forEach((key, value) -> entries.add(new PieEntry(((float) value), key)));
        PieDataSet pieDataSet = new PieDataSet(entries, label);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        PieChart pieChart = new PieChart(context);
        pieChart.setData(pieData);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setUsePercentValues(true);
        pieChart.getData().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 5f) {
                    return String.format(Locale.getDefault(), "%.0f %%", value);
                }
                return "";
            }
        });
        pieChart.getData().setValueTextSize(12f);
        Description description = new Description();
        description.setText(title);
        pieChart.setDescription(description);
        return pieChart;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public BarChart getBarChart(List<String> keys, List<Long> values, String label, String title) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            entries.add(new BarEntry(i, values.get(i)));
        }
        BarDataSet barDataSet = new BarDataSet(entries, label);
        barDataSet.setColors(Collections.singletonList(ColorTemplate.getHoloBlue()));
        BarChart barChart = new BarChart(context);
        barChart.setData(new BarData(barDataSet));
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(keys));
        Description description = new Description();
        description.setText(title);
        barChart.setDescription(description);
        barChart.getData().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value > 0f) return String.format(Locale.getDefault(), "%.0f", value);
                else return "";
            }
        });
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.getXAxis().setGranularity(1);
        return barChart;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PieChart getUserParticipationChart(SortedMap<String, Long> data, @StringRes int title, @StringRes int messageOnValueSelected) {
        PieChart result = getPieChart(
                data,
                "",
                context.getResources().getString(title)
        );
        result.setOnChartValueSelectedListener((OnChartValueSelectedListenerLambdable) (e, h) -> {
            PieEntry pieEntry = (PieEntry) e;
            showToastForce(context.getResources().getString(messageOnValueSelected, pieEntry.getLabel(), (int) pieEntry.getValue()));
        });
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarChart getMessageCountPerTimeUnitChart(SortedMap<?, Long> data, @StringRes int title, @StringRes int messageOnValueSelected, boolean usePercent) {
        List<String> keys = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        data.entrySet().forEach(entry -> {
            keys.add(capitalizeFirstLowerRest(entry.getKey().toString()));
            values.add(entry.getValue());
        });
        BarChart result = getBarChart(keys, values, "", context.getResources().getString(title));
        result.setOnChartValueSelectedListener((OnChartValueSelectedListenerLambdable) (e, h) -> {
            BarEntry barEntry = (BarEntry) e;
            String key = keys.get((int) barEntry.getX());
            long value = Float.valueOf(barEntry.getY()).longValue();
            if (usePercent) {
                float percentage = (float) 100.0 * value / values.stream().reduce(Long::sum).orElse(0L);
                showToastForce(context.getResources().getString(messageOnValueSelected, percentage, key));
            } else {
                showToastForce(context.getResources().getString(messageOnValueSelected, value, key));
            }
        });
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PieChart getUserParticipationByMessagesChart(ChatAnalyzer chatAnalyzer) {
        SortedMap<String, Long> data = chatAnalyzer.getUserParticipationByMessages();
        return getUserParticipationChart(data, R.string.user_participation_by_messages_title, R.string.user_participation_by_messages_value_selected_toast);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PieChart getUserParticipationByWordsChart(ChatAnalyzer chatAnalyzer) {
        SortedMap<String, Long> data = chatAnalyzer.getUserParticipationByWords();
        return getUserParticipationChart(data, R.string.user_participation_by_words_title, R.string.user_participation_by_words_value_selected_toast);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarChart getMessageCountPerHourChart(ChatAnalyzer chatAnalyzer) {
        SortedMap<Integer, Long> data = chatAnalyzer.getMessageCountPerHour();
        return getMessageCountPerTimeUnitChart(data, R.string.message_count_per_hour_title, R.string.message_count_per_hour_value_selected_toast, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarChart getMessageCountPerWeekDayChart(ChatAnalyzer chatAnalyzer) {
        SortedMap<DayOfWeek, Long> data = chatAnalyzer.getMessageCountPerWeekDay();
        return getMessageCountPerTimeUnitChart(data, R.string.message_count_per_week_day_title, R.string.message_count_per_week_day_value_selected_toast, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private BarChart getMessageCountPerDateChart(ChatAnalyzer chatAnalyzer) {
        SortedMap<LocalDate, Long> data = chatAnalyzer.getMessageCountPerDate();
        return getMessageCountPerTimeUnitChart(data, R.string.message_count_per_date_title, R.string.message_count_per_date_value_selected_toast, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Chart<?>> getAllCharts(ChatAnalyzer chatAnalyzer) {
        List<Chart<?>> charts = new ArrayList<>();
        charts.add(getUserParticipationByMessagesChart(chatAnalyzer));
        charts.add(getUserParticipationByWordsChart(chatAnalyzer));
        charts.add(getMessageCountPerHourChart(chatAnalyzer));
        charts.add(getMessageCountPerWeekDayChart(chatAnalyzer));
        charts.add(getMessageCountPerDateChart(chatAnalyzer));
        return charts;
    }

    private void showToastForce(String message) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

}
