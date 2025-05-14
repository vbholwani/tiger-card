package com.metrocard.data;

import java.time.LocalTime;

public class TimeSlot {
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeSlot(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean covers(LocalTime journeyTime) {
        return ((journeyTime.equals(startTime) || journeyTime.isAfter(startTime)) &&
                (journeyTime.equals(endTime) || journeyTime.isBefore(endTime)));
    }
}
