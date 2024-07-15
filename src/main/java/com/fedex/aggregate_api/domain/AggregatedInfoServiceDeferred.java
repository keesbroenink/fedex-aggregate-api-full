package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@Service
public class AggregatedInfoServiceDeferred {

    private final Logger logger = LoggerFactory.getLogger(AggregatedInfoServiceDeferred.class);
    private final FedexApiListener fedexApiListener;
    private final long timeoutSeconds;

    public AggregatedInfoServiceDeferred(FedexApiListener fedexApiListener,
                                         @Value("${fedexapi.service.timeout.seconds}") long timeoutSeconds) {
        this.fedexApiListener = fedexApiListener;
        this.timeoutSeconds = timeoutSeconds;
    }

    public DeferredResult<AggregatedInfo> getInfoDeferred(List<String> pricingIso2CountryCodes,
                                                          List<String> trackOrderNumbers,
                                                          List<String> shipmentsOrderNumbers) {
        logger.info("getInfoDeferred() with timeout {} seconds for pricingIso2CountryCodes {}, trackOrderNumbers {}, shipmentsOrderNumbers {}",
                timeoutSeconds, pricingIso2CountryCodes, trackOrderNumbers, shipmentsOrderNumbers);
        AggregatedInfo result = new AggregatedInfo(pricingIso2CountryCodes,trackOrderNumbers,shipmentsOrderNumbers);
        DeferredResult<AggregatedInfo> deferredResult = new DeferredResult(timeoutSeconds*1000,
                () -> fedexApiListener.executeOnTimeout(result));
        fedexApiListener.addRequest(result, deferredResult, timeoutSeconds);
        return deferredResult; // the HTTP client does not get an answer at this point (waits)
    }

}
