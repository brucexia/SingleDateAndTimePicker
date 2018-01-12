package com.github.florent37.singledateandtimepicker.widget;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.AttributeSet;

import com.github.florent37.singledateandtimepicker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WheelDayPicker extends WheelPicker {

    public static final int DAYS_PADDING = 100;
    private int defaultIndex;

    private int todayPosition;
    boolean showOlder = false;

    private SimpleDateFormat simpleDateFormat;

    private OnDaySelectedListener onDaySelectedListener;

    Adapter adapter;
    List<DayItem> customItems;

    public WheelDayPicker(Context context) {
        this(context, null);
    }

    public WheelDayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.simpleDateFormat = new SimpleDateFormat("EEE d MMM", getCurrentLocale());
        this.adapter = new Adapter();
        setAdapter(adapter);
        defaultIndex = 1;
        updateDays();
    }

    public void setCustomItems(List<DayItem> customItems) {
        this.customItems = customItems;
        updateDays();
    }

    public WheelDayPicker setDayFormatter(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
        updateDays();
        return this;
    }

    @Override
    protected void onItemSelected(int position, Object item) {
        if (item instanceof DayItem && onDaySelectedListener != null) {
//            final String itemText = (String) item;
//            final Date date = convertItemToDate(position);
            onDaySelectedListener.onDaySelected(this, position, (DayItem) item);
        }
    }

    @Override
    protected void onItemCurrentScroll(int position, Object item) {
    }

    @Override
    public int getDefaultItemPosition() {
        return defaultIndex;
    }

    private void updateDays() {
        final List<DayItem> data = new ArrayList<>();

        Calendar instance = Calendar.getInstance();

        if (showOlder) {
            instance.add(Calendar.DATE, -1 * DAYS_PADDING - 1);
            for (int i = (-1) * DAYS_PADDING; i < 0; ++i) {
                instance.add(Calendar.DAY_OF_MONTH, 1);
                Date d = instance.getTime();
                data.add(new DateItem(getFormattedValue(d), d.getTime()));
            }
        }
        if (customItems != null && !customItems.isEmpty()) {
            data.addAll(customItems);
        }
        todayPosition = data.size();

        //today
        data.add(new DateItem(getResources().getString(R.string.picker_today), instance.getTimeInMillis()));

        instance = Calendar.getInstance();

        for (int i = 0; i < DAYS_PADDING; ++i) {
            instance.add(Calendar.DATE, 1);
            Date d = instance.getTime();
            data.add(new DateItem(getFormattedValue(d), d.getTime()));
        }

        adapter.setData(data);
        notifyDatasetChanged();
    }

    protected String getFormattedValue(Object value) {
        return simpleDateFormat.format(value);
    }

    public void setOnDaySelectedListener(OnDaySelectedListener onDaySelectedListener) {
        this.onDaySelectedListener = onDaySelectedListener;
    }

    public void setCurrentDate(Date date) {

    }

    public void updateDefaultDay(int index) {
        defaultIndex = index;
        setSelectedItemPosition(defaultIndex);
    }

    public void updateDefaultDay(Date index) {
        defaultIndex = findDateIndex(index);
        setSelectedItemPosition(defaultIndex);
    }

    public static boolean isSameDay(long date1, long date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(date2);
        boolean sameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        boolean sameMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
        boolean sameDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
        return (sameDay && sameMonth && sameYear);

//        long julianDayNumber1 = date1 / DateUtils.DAY_IN_MILLIS;
//        long julianDayNumber2 = date2 / DateUtils.DAY_IN_MILLIS;
//
//        // If they now are equal then it is the same day.
//        return julianDayNumber1 == julianDayNumber2;

    }

    int findDateIndex(Date date) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.getItem(i) instanceof DateItem
                    && isSameDay(date.getTime(), ((DateItem) adapter.getItem(i)).getTimeInMillis()))
                return i;
        }
        return 1;
    }

    public int getDefaultDayIndex() {
        return defaultIndex;
    }

    public DayItem getItem(int pos) {
        return (DayItem) adapter.getItem(pos);
    }

    public Date getCurrentDate() {
        return convertItemToDate(super.getCurrentItemPosition());
    }

    public boolean isDateSelected() {
        return getItem(getCurrentItemPosition()) instanceof DateItem;
    }

    private Date convertItemToDate(int itemPosition) {
        Date date = null;
        String itemText = adapter.getItemText(itemPosition);
        final Calendar todayCalendar = Calendar.getInstance();
        if (itemPosition == todayPosition) {
            date = todayCalendar.getTime();
        } else {
            try {
                date = simpleDateFormat.parse(itemText);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (date != null) {
            //try to know the year
            final Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);

            todayCalendar.add(Calendar.DATE, (itemPosition - todayPosition));

            dateCalendar.set(Calendar.YEAR, todayCalendar.get(Calendar.YEAR));
            date = dateCalendar.getTime();
        }

        return date;
    }

    public String getCurrentDay() {
        return adapter.getItemText(getCurrentItemPosition());
    }

    public void setTodayText(String todayText) {
        int index = adapter.getData().indexOf(getResources().getString(R.string.picker_today));
        if (index != -1) {
            adapter.getData().set(index, todayText);
            notifyDatasetChanged();
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(WheelDayPicker picker, int position, DayItem item);
    }

    public static abstract class DayItem {
        CharSequence display;

        public DayItem(CharSequence display) {
            this.display = display;
        }

        public CharSequence getDisplay() {
            return display;
        }

        @Override
        public String toString() {
            return String.valueOf(display);
        }
    }

    public abstract static class CustomItem extends DayItem {
        public CustomItem(CharSequence display) {
            super(display);
        }

        public abstract void onClick();
    }

    public static class DateItem extends DayItem {
        long timeInMillis;

        public DateItem(CharSequence display, long timeInMillis) {
            super(display);
            this.timeInMillis = timeInMillis;
        }

        public long getTimeInMillis() {
            return timeInMillis;
        }
    }

    public static class Adapter extends WheelPicker.Adapter {
        @Override
        public Object getItem(int position) {
            return getData().get(position);
        }
    }
}