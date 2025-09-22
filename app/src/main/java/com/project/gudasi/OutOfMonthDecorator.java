package com.project.gudasi;

import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import android.text.style.ForegroundColorSpan;

import java.util.Calendar;

public class OutOfMonthDecorator implements DayViewDecorator {

    private final int color;

    public OutOfMonthDecorator(int color) {
        this.color = color;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Calendar cal = Calendar.getInstance();
        cal.set(day.getYear(), day.getMonth() - 1, day.getDay());
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH); // 0~11
        return cal.get(Calendar.MONTH) != currentMonth;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(color));
    }
}
