package com.project.gudasi;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final Drawable highlightDrawable;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
        highlightDrawable = new ColorDrawable(color);
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable); // 배경 색상 변경
        // 또는 view.addSpan(new DotSpan(10, Color.RED)); → 점만 찍기 가능

        // 글자색 검은색으로
        view.addSpan(new android.text.style.ForegroundColorSpan(Color.BLACK));
    }
}

