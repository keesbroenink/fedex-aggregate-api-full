package com.fedex.aggregate_api.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
/**
 * Registers async requests and listens to the async responses. Checks which requests are
 * waiting for this data and hands them the data. When all requested data is present
 * the request will be resolved meaning that the HTTP client will receive the response
 * with all the data.
 */
public class FedexApiListener {
    // remember async requests
    private final Map<AggregatedInfo, DeferredResult<AggregatedInfo>> asynContextMap = new ConcurrentHashMap();
    private final AggregatedInfoService aggregatedInfoService;
    private final Logger logger = LoggerFactory.getLogger(FedexApiListener.class);

    public FedexApiListener( AggregatedInfoService aggregatedInfoService) {
        this.aggregatedInfoService = aggregatedInfoService;
    }

    public void addRequest(AggregatedInfo requestedInfo, DeferredResult<AggregatedInfo> waitingRequest) {
        asynContextMap.put(requestedInfo, waitingRequest);
        // set out the request and when it finishes we process the results (errors will be handled by returning empty result)
        publishAndProcess( () -> aggregatedInfoService.getInfo(requestedInfo));
    }

    public AggregatedInfo executeOnTimeout(AggregatedInfo requestedInfo) {
        logger.debug("executeOnTimeout {}", requestedInfo);
        asynContextMap.remove(requestedInfo); // no need to keep the request
        return aggregatedInfoService.getInfoNoLimit(requestedInfo);
    }

    private Mono<Long> delay() {
        return Mono.delay(Duration.ofMillis(1));
    }
    private void publishAndProcess(Supplier<AggregatedInfo> blockingAction)  {
        delay()
                .publishOn(Schedulers.boundedElastic())
                .map(it -> blockingAction.get())
                .subscribe(
                        this::process,
                        err -> logger.error("ERROR: {} ", err.getMessage())
                );
    }

    // Unfortunately the algorithm we must implement requires us to have one processing code block at the time
    // because we must check all waiting clients for this specific piece of retrieved data.
    // If we allow this to happen in parallel we could create errors because we resolve waiting requests more than once.
    private synchronized void process(AggregatedInfo successAggregatedInfo) {
        logger.info("processing...");
        // we have a new FedEx API answer; let's see if we have requests waiting for this info
        asynContextMap.keySet().forEach(requestedInfo -> {
            logger.info("processing {}",requestedInfo);
            DeferredResult<AggregatedInfo> waitingRequest = asynContextMap.get(requestedInfo);
            requestedInfo.merge(successAggregatedInfo);
            if (requestedInfo.isComplete()) {
                logger.info("all data received for {}",requestedInfo);
                // send the waiting HTTP client the data
                waitingRequest.setResult(requestedInfo);
                // remove from the map
                asynContextMap.remove(requestedInfo);
            } else {
                logger.info("data received but still not all for {}",requestedInfo);
            }

        });


    }

}
