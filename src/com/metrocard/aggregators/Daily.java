package com.metrocard.aggregators;

import com.metrocard.data.Journey;
import com.metrocard.data.Route;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;

public class Daily implements Aggregator {
    private final Map<Route, Integer> dailyCaps;
    private final Map<LocalDate, Integer> dailyFare;
    private final Map<LocalDate, Route> farthestRouteForADay;

    public Daily(Map<Route, Integer> dailyCaps) {
        this.dailyCaps = dailyCaps;
        this.dailyFare = new HashMap<>();
        this.farthestRouteForADay = new HashMap<>();
    }

    @Override
    public void add(Journey journey){
        this.checkAndUpdateFarthestRouteForADay(journey);
    }

    @Override
    public Integer applyCap(Journey journey, Integer fare) {
        LocalDate date = journey.getDateTime().toLocalDate();
        Route route = journey.getRoute();
        Integer currentDayFare = getDailyFare(date);
        Integer dailyCap = getDailyCap(date, route);
        if ((currentDayFare + fare) > dailyCap) fare = dailyCap - currentDayFare;
        return fare;
    }

    @Override
    public void aggregate(Journey journey, Integer fare) {
        LocalDate date = journey.getDateTime().toLocalDate();
        this.dailyFare.put(date, getDailyFare(date)+fare);
    }

    private void checkAndUpdateFarthestRouteForADay(Journey journey) {
        LocalDate date = journey.getDateTime().toLocalDate();
        Route route = journey.getRoute();

        Route farthestRoute = this.farthestRouteForADay.putIfAbsent(date, route);
        if (farthestRoute != null
                && fartherThan(route, farthestRoute)) {

            this.farthestRouteForADay.put(date, route);
        }
    }

    private boolean fartherThan(Route routeOne, Route routeTwo) {
        int routeOneZones = abs(routeOne.getToZone() - routeOne.getFromZone());
        int routeTwoZones = abs(routeTwo.getToZone() - routeTwo.getFromZone());

        if(routeOneZones == routeTwoZones)
            return (getDailyCap(routeOne, 0) > getDailyCap(routeTwo, 0));
        else
            return (routeOneZones > routeTwoZones);
    }

    private Integer getDailyFare(LocalDate date) {
        return this.dailyFare.getOrDefault(date, 0);
    }

    private Integer getDailyCap(LocalDate date, Route route) {
        return getDailyCap(getFarthestRouteForADay(date, route), Integer.MAX_VALUE);
    }

    private Integer getDailyCap(Route route, Integer defaultValue) {
        return this.dailyCaps.getOrDefault(route, defaultValue);
    }

    private Route getFarthestRouteForADay(LocalDate date, Route route) {
        return this.farthestRouteForADay.getOrDefault(date, route);
    }
}
