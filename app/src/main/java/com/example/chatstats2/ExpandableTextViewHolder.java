package com.example.chatstats2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintSet;

/**
 * A LinearLayout that contains a TextView and enables the user to collapse or expand the
 * contained text via a button.
 */
public class ExpandableTextViewHolder extends LinearLayout {

    int maxLinesCollapsed;
    int maxLinesExpanded;
    boolean expanded;
    TextView textView;
    Button showMoreButton;

    public ExpandableTextViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextViewHolder, 0, 0);
        maxLinesCollapsed = a.getInt(R.styleable.ExpandableTextViewHolder_maxLinesCollapsed, R.integer.max_lines_collapsed);
        maxLinesExpanded = a.getInt(R.styleable.ExpandableTextViewHolder_maxLinesExpanded, R.integer.max_lines_expanded);
        expanded = a.getBoolean(R.styleable.ExpandableTextViewHolder_expanded, false);
        a.recycle();
        addShowMoreButton();
        addTextView();
        connectComponents();
    }

    private void addShowMoreButton() {
        showMoreButton = new Button(getContext());
        showMoreButton.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        showMoreButton.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        showMoreButton.setOnClickListener(v -> toggleExpanded());
        showMoreButton.setBackgroundColor(Color.TRANSPARENT);
        addView(showMoreButton);
    }

    private void addTextView() {
        textView = new TextView(getContext());
        textView.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        addView(textView);
    }

    private void connectComponents() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.connect(showMoreButton.getId(), ConstraintSet.BOTTOM, textView.getId(), ConstraintSet.TOP);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        update();
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        setExpanded(!expanded);
    }

    public CharSequence getText() {
        return textView.getText();
    }

    public int getMaxLinesCollapsed() {
        return maxLinesCollapsed;
    }

    public int getMaxLinesExpanded() {
        return maxLinesExpanded;
    }

    public int getLineCount() {
        return textView.getLineCount();
    }

    public void setMaxLinesCollapsed(int maxLinesCollapsed) {
        this.maxLinesCollapsed = maxLinesCollapsed;
        update();
    }

    public void setMaxLinesExpanded(int maxLinesExpanded) {
        this.maxLinesExpanded = maxLinesExpanded;
        update();
    }

    public void setText(CharSequence text) {
        textView.setText(text);
        update();
    }

    protected void update() {
        // For updating the view, we need the lineCount of the textView.
        // Before textView is rendered, getLineCount() will return 0. Therefore, add a runnable to
        // its layout queue. It will be executed after rendering.
        textView.post(() -> {
            int lineCount =
                    textView.getText().toString().endsWith("\n")
                    ? textView.getLineCount() - 1
                    : textView.getLineCount();
            if (lineCount <= maxLinesCollapsed) {
                showMoreButton.setVisibility(GONE);
            } else {
                showMoreButton.setVisibility(VISIBLE);
            }
            if (expanded) {
                textView.setMaxLines(maxLinesExpanded);
                showMoreButton.setText(R.string.show_less);
            } else {
                textView.setMaxLines(maxLinesCollapsed);
                showMoreButton.setText(R.string.show_more);
            }
            invalidate();
            requestLayout();
        });
    }

}
