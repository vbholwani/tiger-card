package com.metrocard.aggregators;

import com.metrocard.data.Journey;
import com.metrocard.data.Route;
import com.metrocard.data.Week;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

public class Weekly implements Aggregator {
    private final Map<Route, Integer> weeklyCaps;
    private final Map<Week, Integer> weeklyFare;
    private final Map<Week, Route> farthestRouteForAWeek;
    private final Map<LocalDate, Week> daysToWeekCache;

    public Weekly(Map<Route, Integer> weeklyCaps) {
        this.weeklyCaps = weeklyCaps;
        this.weeklyFare = new HashMap<>();
        this.farthestRouteForAWeek = new HashMap<>();
        this.daysToWeekCache = new HashMap<>();
    }

    @Override
    public void add(Journey journey){
        this.checkAndUpdateFarthestRouteForAWeek(journey);
    }

    @Override
    public Integer applyCap(Journey journey, Integer fare) {
        LocalDate date = journey.getDateTime().toLocalDate();
        Route route = journey.getRoute();
        Week week = findWeek(date);
        Integer currentWeekFare = getWeeklyFare(date);
        Integer weeklyCap = getWeeklyCap(week, route);
        if ((currentWeekFare + fare) > weeklyCap) fare = weeklyCap - currentWeekFare;
        return fare;
    }

    @Override
    public void aggregate(Journey journey, Integer fare) {
        LocalDate date = journey.getDateTime().toLocalDate();
        Week week = findWeek(date);
        this.weeklyFare.put(week, getWeeklyFare(date)+fare);
    }

    private void checkAndUpdateFarthestRouteForAWeek(Journey journey) {
        LocalDate date = journey.getDateTime().toLocalDate();
        Route route = journey.getRoute();
        Week week = findWeek(date);
        Route farthestRoute = this.farthestRouteForAWeek.putIfAbsent(week, route);
        if (farthestRoute != null
                && fartherThan(route, farthestRoute)) {

            this.farthestRouteForAWeek.put(week, route);
        }
    }

    private boolean fartherThan(Route routeOne, Route routeTwo) {
        int routeOneZones = abs(routeOne.getToZone() - routeOne.getFromZone());
        int routeTwoZones = abs(routeTwo.getToZone() - routeTwo.getFromZone());

        if(routeOneZones == routeTwoZones)
            return (getWeeklyCap(routeOne, 0) > getWeeklyCap(routeTwo, 0));
        else
            return (routeOneZones > routeTwoZones);
    }

    private Integer getWeeklyFare(LocalDate date) {
        return this.weeklyFare.getOrDefault(findWeek(date), 0);
    }

    private Route getFarthestRouteForAWeek(Week week, Route route) {
        return this.farthestRouteForAWeek.getOrDefault(week, route);
    }

    private Week findWeek(LocalDate date) {
        this.daysToWeekCache.putIfAbsent(date, new Week(findMonday(date), findSunday(date)));
        return this.daysToWeekCache.get(date);
    }

    private LocalDate findMonday(LocalDate date) {
        int daysToSubtract = date.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        return date.minusDays(daysToSubtract);
    }

    private LocalDate findSunday(LocalDate date) {
        int daysToAdd = DayOfWeek.SUNDAY.getValue() - date.getDayOfWeek().getValue();
        return date.plusDays(daysToAdd);
    }

    private Integer getWeeklyCap(Week week, Route route) {
        return this.getWeeklyCap(getFarthestRouteForAWeek(week, route), Integer.MAX_VALUE);
    }

    private Integer getWeeklyCap(Route route, Integer defaultValue){
        return this.weeklyCaps.getOrDefault(route, defaultValue);
    }
}
