package com.example.seguesaude;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MultiDotDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final List<Integer> colors;

    public MultiDotDecorator(Collection<CalendarDay> dates, List<Integer> colors) {
        this.dates = new HashSet<>(dates);
        this.colors = colors;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new LineBackgroundSpan() {
            @Override
            public void drawBackground(Canvas canvas, Paint paint, int left, int right, int top,
                                       int baseline, int bottom, CharSequence text,
                                       int start, int end, int lineNum) {

                int radius = 6;
                int spacing = 14;
                int total = colors.size();
                int centerX = (left + right) / 2;
                int startX = centerX - ((total - 1) * spacing / 2);
                int y = bottom + radius + 4;

                int originalColor = paint.getColor();

                for (int i = 0; i < total; i++) {
                    paint.setColor(colors.get(i));
                    canvas.drawCircle(startX + i * spacing, y, radius, paint);
                }

                paint.setColor(originalColor);
            }
        });

        view.addSpan(new ForegroundColorSpan(0xFF000000));
    }
}