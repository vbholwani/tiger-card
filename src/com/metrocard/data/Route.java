package com.metrocard.data;

import java.util.Objects;

public class Route {
    private final int fromZone;
    private final int toZone;

    public Route(int fromZone, int toZone) {
        this.fromZone = fromZone;
        this.toZone = toZone;
    }

    public int getFromZone() {
        return fromZone;
    }

    public int getToZone() {
        return toZone;
    }

    @Override
    public String toString() {
        return "Route{" +
                "fromZone=" + fromZone +
                ", toZone=" + toZone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return fromZone == route.fromZone &&
                toZone == route.toZone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromZone, toZone);
    }
}
