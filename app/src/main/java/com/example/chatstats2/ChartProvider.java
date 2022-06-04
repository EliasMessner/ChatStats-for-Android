package com.example.chatstats2;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChartProvider {

    Context context;

    public ChartProvider(Context context) {
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public PieChart getPieChart(Map<String, Long> data, String label, String title) {
        List<PieEntry> entries = new ArrayList<>();
        data.forEach((key, value) -> entries.add(new PieEntry(((float) value), key)));
        PieDataSet pieDataSet = new PieDataSet(entries, label);
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        PieChart pieChart = new PieChart(context);
        pieChart.setData(pieData);
        pieChart.setHoleColor(Color.TRANSPARENT);
        Description description = new Description();
        description.setText(title);
        pieChart.setDescription(description);
        return pieChart;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Chart<?>> getAllCharts(ChatAnalyzer chatAnalyzer) {
        List<Chart<?>> charts = new ArrayList<>();
        charts.add(getPieChart(
                chatAnalyzer.getUserParticipationByMessages(),
                "",
                context.getResources().getString(R.string.user_participation_by_messages_title)
        ));
        charts.add(getPieChart(
                chatAnalyzer.getUserParticipationByWords(),
                "",
                context.getResources().getString(R.string.user_participation_by_words_title)
        ));
        return charts;
    }

}
