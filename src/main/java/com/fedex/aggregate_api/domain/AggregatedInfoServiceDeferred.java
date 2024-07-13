package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AggregatedInfoServiceDeferred {
    private static final long REQUEST_TIME_OUT_MS = 5000; // could become a configuration parameter
    private final Logger logger = LoggerFactory.getLogger(AggregatedInfoServiceDeferred.class);
    private final FedexApi fedexApi;
    private final FedexApiListener fedexApiListener;

    public AggregatedInfoServiceDeferred(FedexApi fedexApi, FedexApiListener fedexApiListener) {
        this.fedexApi = fedexApi;
        this.fedexApiListener = fedexApiListener;
    }

    public DeferredResult<AggregatedInfo> getInfoDeferred(List<String> pricingIso2CountryCodes,
                                                          List<String> trackOrderNumbers,
                                                          List<String> shipmentsOrderNumbers) {
        logger.info("getInfoDeferred() pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                pricingIso2CountryCodes, trackOrderNumbers, shipmentsOrderNumbers);
        AggregatedInfo result = new AggregatedInfo(pricingIso2CountryCodes,trackOrderNumbers,shipmentsOrderNumbers);
        DeferredResult<AggregatedInfo> deferredResult = new DeferredResult( REQUEST_TIME_OUT_MS);
        fedexApiListener.addRequest(result, deferredResult);
        return deferredResult; // the HTTP client does not get an answer at this point (waits)
    }

}
