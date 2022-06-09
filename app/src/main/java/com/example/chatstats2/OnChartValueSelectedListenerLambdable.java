package com.example.chatstats2;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

/**
 * Use this interface instead of OnChartValueSelectedListener, if you want to express it as a lambda.
 * The onNothingSelected method is already implemented and does nothing.
 */
public interface OnChartValueSelectedListenerLambdable extends OnChartValueSelectedListener {

    @Override
    public default void onNothingSelected() {}

}
