package com.example.chatstats2;

import static com.example.chatstats2.Util.getLocationOnWindow;

import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatstats2.databinding.ActivityResultsBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    String fileText;
    List<Chart<?>> chartsYetToBeAnimated;
    Chat chat;
    SettingsHandler settingsHandler;
    ChartProvider chartProvider;
    private ActivityResultsBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.scrollView.getViewTreeObserver().addOnScrollChangedListener(this::onScrollChanged);
        chartsYetToBeAnimated = new ArrayList<>();
        settingsHandler = new SettingsHandler(this);
        chartProvider = new ChartProvider(this);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        fileText = getIntent().getStringExtra("fileText");
        assert fileText != null;
        chat = Chat.parseChat(fileText, settingsHandler.getDateTimePattern(), settingsHandler.getDateTimeLookAhead());
        showResults();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showResults() {
        // create the charts
        ChatAnalyzer chatAnalyzer = new ChatAnalyzer(chat);
        List<Chart<?>> charts = chartProvider.getAllCharts(chatAnalyzer);
        // display the charts
        charts.forEach(this::displayChart);
        chartsYetToBeAnimated.addAll(charts);
    }

    private void displayChart(Chart<?> chart) {
        // Make the description show up as Title above the chart
        TextView title = new TextView(this);
        title.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        title.setPadding(0, 40, 0, 20);
        title.setText(chart.getDescription().getText());
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getInteger(R.integer.chart_title_text_size));
        chart.getDescription().setEnabled(false);
        binding.resultsView.addView(title);
        // add the chart to the resultsView
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        binding.resultsView.addView(chart);
        chart.invalidate();
        // Make the chart's width and height match that of the resultsView. To get resultsView's w&h,
        // add runnable to the resultsView's layout queue, so that it will be executed only after
        // resultView was rendered. Before being rendered, resultView's width and height will be 0.
        binding.resultsView.post(() -> {
            chart.setMinimumWidth(binding.resultsView.getWidth());
            chart.setMinimumHeight(chart.getMinimumWidth());
        });
    }

    /**
     * Animate a chart when it appears on the screen for the first time.
     */
    private void onScrollChanged() {
        for (int i = 0; i < binding.resultsView.getChildCount(); i++) {
            View child = binding.resultsView.getChildAt(i);
            if (child instanceof Chart<?> && chartsYetToBeAnimated.contains((Chart<?>) child)) {
                Chart<?> chart = (Chart<?>) child;
                if (getLocationOnWindow(chart)[1] > binding.getRoot().getHeight()) continue;
                chartsYetToBeAnimated.remove(chart);
                if (chart instanceof PieChart) {
                    ((PieChart) chart).spin( getResources().getInteger(R.integer.animation_pie_chart_duration_millis),0,-360f, Easing.EaseInOutQuad);
                } else if (chart instanceof BarChart) {
                    chart.animateY(R.integer.animation_bar_chart_duration_millis);
                }
            }
        }
    }
}