package com.metrocard.data;

public class Rate {
    private final Integer offPeakRate;
    private final Integer peakRate;

    public Rate(Integer offPeakRate , Integer peakRate) {
        this.offPeakRate = offPeakRate;
        this.peakRate = peakRate;

    }

    public Integer getRate(boolean isPeak){
        return (isPeak)? this.peakRate : this.offPeakRate;
    }
}
