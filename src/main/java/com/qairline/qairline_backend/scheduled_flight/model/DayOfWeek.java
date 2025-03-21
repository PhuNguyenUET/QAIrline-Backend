package com.qairline.qairline_backend.scheduled_flight.model;

public enum DayOfWeek {
    SUN, MON,TUE,WED,THU,FRI,SAT;

    public int getValue() {
        return this.ordinal() + 1;
    }

    public static DayOfWeek fromInt(int i) {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            if (dayOfWeek.getValue() == i) {
                return dayOfWeek;
            }
        }
        return null;
    }
}
