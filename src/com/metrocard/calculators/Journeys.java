package com.metrocard.calculators;

import com.metrocard.aggregators.Aggregator;
import com.metrocard.data.Journey;
import com.metrocard.data.Rate;
import com.metrocard.data.Route;
import com.metrocard.data.TimeSlot;
import com.metrocard.exceptions.RateNotDefinedException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static java.util.Collections.emptyList;

public class Journeys {
    private final Map<Route, Rate> rateCard;
    private final Map<DayOfWeek, List<TimeSlot>> peakTimeSlots;
    private final List<Aggregator> aggregators;
    private final TreeSet<Journey> journeys;

    public Journeys(Map<Route, Rate> rateCard,
                    Map<DayOfWeek, List<TimeSlot>> peakTimeSlots, List<Aggregator> aggregators) {

        this.rateCard = rateCard;
        this.peakTimeSlots = peakTimeSlots;
        this.aggregators = aggregators;
        this.journeys = new TreeSet<>();
    }

    public void add(LocalDateTime dateTime, int fromZone, int toZone) {
        add(new Journey(dateTime, fromZone, toZone));
    }

    public void add(Journey journey) {
        validate(journey);
        this.journeys.add(journey);
        aggregators.forEach(aggregator -> aggregator.add(journey));
    }

    public int calculate() {
        return this.journeys.stream()
                .map(this::calculate)
                .reduce(0, Integer::sum);
    }

    private Integer calculate(Journey journey) {
        Integer baseFare = this.getBaseFare(journey);

        Integer fare = this.aggregators.stream()
                .map(aggregator -> aggregator.applyCap(journey, baseFare))
                .reduce(baseFare, Math::min);

        this.aggregators.forEach(aggregator -> aggregator.aggregate(journey, fare));

        return fare;
    }

    private Integer getBaseFare(Journey journey) {
        return this.rateCard.get(journey.getRoute()).getRate(isPeak(journey.getDateTime()));
    }

    private void validate(Journey journey) {
        if (!this.rateCard.containsKey(journey.getRoute()))
            throw new RateNotDefinedException("Rate not defined for the Journey " + journey.getRoute());
    }

    private boolean isPeak(LocalDateTime dateTime) {
        LocalTime journeyTime = dateTime.toLocalTime();
        List<TimeSlot> peakTimeSlots = this.peakTimeSlots.getOrDefault(dateTime.getDayOfWeek(), emptyList());
        Optional<TimeSlot> applicablePeakSlot =
                peakTimeSlots.stream().filter(timeSlot -> timeSlot.covers(journeyTime)).findAny();
        return applicablePeakSlot.isPresent();
    }
}
