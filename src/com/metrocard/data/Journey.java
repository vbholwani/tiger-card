package com.metrocard.data;

import java.time.LocalDateTime;

public class Journey implements Comparable{
    private final LocalDateTime dateTime;
    private final Route route;

    public Journey(LocalDateTime dateTime, int fromZone, int toZone) {
        this.dateTime = dateTime;
        this.route = new Route(fromZone, toZone);
    }

    public Route getRoute() {
        return route;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public int compareTo(Object obj) {
        return this.dateTime.compareTo(((Journey)obj).dateTime);
    }
}
