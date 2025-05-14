package com.metrocard.data;

import java.time.LocalDate;
import java.util.Objects;

public class Week {
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Week(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Week week = (Week) o;
        return startDate.equals(week.startDate) &&
                endDate.equals(week.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
}
