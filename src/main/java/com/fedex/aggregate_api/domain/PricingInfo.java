package com.fedex.aggregate_api.domain;

public class PricingInfo {
    public PricingInfo(String isoCountryCode, Double price) {
        this.isoCountryCode = isoCountryCode;
        this.price = price;
    }
    public PricingInfo(GenericInfo genericInfo) {
        this.isoCountryCode = genericInfo.code;
        this.price = (Double) genericInfo.data;
    }
    public final String isoCountryCode;
    public final Double price;
}
