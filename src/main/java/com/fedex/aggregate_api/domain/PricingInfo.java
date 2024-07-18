package com.fedex.aggregate_api.domain;

import java.util.List;

public record PricingInfo(CountryCode isoCountryCode, Double price) {
    public static List<CountryCode> fromListString(List<String> listString) {
        return listString.stream().map(s -> new CountryCode(s)).toList();
    }
}
