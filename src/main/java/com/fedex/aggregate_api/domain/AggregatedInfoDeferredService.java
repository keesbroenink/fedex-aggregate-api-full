package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

/**
 * This service is used by the REST API to provide the requested aggregated FedEx info for pricing,
 * tracking and shipment. If the HTTP client requests less items than <code>fedexapi.service.minimal.requests</code>
 * configured in <code>application.properties</code> it will wait until another request (of the same of other client)
 * will number up to the minimal number.
 * The HTTP client will wait for a maximum of <code>timeoutSeconds</code>. After that it will
 * just get the requested data.
 * This class works together with {@link FedexApiListener} and {@link AggregatedInfoService}.
 */
@Service
public class AggregatedInfoDeferredService {

    private final Logger logger = LoggerFactory.getLogger(AggregatedInfoDeferredService.class);
    private final FedexApiListener fedexApiListener;
    private final long timeoutSeconds;

    public AggregatedInfoDeferredService(FedexApiListener fedexApiListener,
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
        fedexApiListener.addRequest(result, deferredResult);
        return deferredResult; // the HTTP client does not get an answer at this point (waits)
    }

}
