package com.metrocard;

import com.metrocard.aggregators.Aggregator;
import com.metrocard.aggregators.Daily;
import com.metrocard.aggregators.Weekly;
import com.metrocard.calculators.Journeys;
import com.metrocard.data.Journey;
import com.metrocard.data.Rate;
import com.metrocard.data.Route;
import com.metrocard.data.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

public class TigerCard {
    private final Journeys journeys;

    TigerCard(Map<Route, Rate> rateCard,
              Map<DayOfWeek, List<TimeSlot>> peakTimeSlots,
              Map<Route, Integer> dailyCaps,
              Map<Route, Integer> weeklyCaps) {

        List<Aggregator> aggregators = new ArrayList<>();
        aggregators.add(new Daily(dailyCaps));
        aggregators.add(new Weekly(weeklyCaps));
        this.journeys = new Journeys(rateCard, peakTimeSlots, aggregators);
    }

    public void add(LocalDateTime dateTime, int fromZone, int toZone) {
        this.journeys.add(dateTime, fromZone, toZone);
    }

    public void add(Journey journey) {
        this.journeys.add(journey);
    }

    public void add(List<Journey> journeyList) {
        journeyList.forEach(this::add);
    }

    public int getFare() {
        return this.journeys.calculate();
    }
}
