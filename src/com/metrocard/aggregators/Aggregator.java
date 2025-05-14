package com.metrocard.aggregators;

import com.metrocard.data.Journey;

public interface Aggregator {

    void add(Journey journey);

    Integer applyCap(Journey journey, Integer fare);

    void aggregate(Journey journey, Integer fare);
}
