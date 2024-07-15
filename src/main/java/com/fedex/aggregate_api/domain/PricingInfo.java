package com.fedex.aggregate_api.domain;

public class PricingInfo {
    public PricingInfo(String isoCountryCode, Double price) {
        this.isoCountryCode = isoCountryCode;
        this.price = price;
    }
    public final String isoCountryCode;
    public final Double price;
}
