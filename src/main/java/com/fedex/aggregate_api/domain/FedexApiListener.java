package com.fedex.aggregate_api.domain;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FedexApiListener {
    // remember async requests
    private Map<AggregatedInfo, DeferredResult<AggregatedInfo>> asynContextMap = new ConcurrentHashMap();
    private final FedexApi fedexApi;
    private final AggregatedInfoService aggregatedInfoService;

    public FedexApiListener(FedexApi fedexApi, AggregatedInfoService aggregatedInfoService) {
        this.fedexApi = fedexApi;
        this.aggregatedInfoService = aggregatedInfoService;
    }

    public void addRequest(AggregatedInfo requestedInfo, DeferredResult<AggregatedInfo> waitingRequest) {
        asynContextMap.put(requestedInfo, waitingRequest);
        // set out the request and when it finishes we process the results
        Mono<AggregatedInfo> infoInTheFuture = aggregatedInfoService.getInfo(requestedInfo);
        infoInTheFuture.doOnSuccess(successAggregatedInfo -> process(successAggregatedInfo));
        // but when we do not finish in time we force the collection of data
        waitingRequest.onTimeout(forceCollectionOfData(waitingRequest, infoInTheFuture));
    }

    private Runnable forceCollectionOfData(DeferredResult<AggregatedInfo> waitingRequest,
                                           Mono<AggregatedInfo> infoInTheFuture) {
        return new Runnable() {
            @Override
            public void run() {
                waitingRequest.setResult(infoInTheFuture.block());
            }
        };
    }


    // Unfortunately the algorithm we must implement requires us to have one processing code block at the time
    // because we must check all waiting clients for this specific piece of retrieved data.
    // If we allow this to happen in parallel we could create errors because we resolve waiting requests more than once.
    private synchronized void process(AggregatedInfo successAggregatedInfo) {
        // we have a new FedEx API answer; let's see if we have requests waiting for this info
        asynContextMap.keySet().forEach(requestedInfo -> {
            DeferredResult<AggregatedInfo> waitingRequest = asynContextMap.get(requestedInfo);
            requestedInfo.merge(successAggregatedInfo);
            if (requestedInfo.isComplete()) {
                // send the waiting HTTP client the data
                waitingRequest.setResult(successAggregatedInfo);
                // remove from the map
                asynContextMap.remove(requestedInfo);
            }

        });


    }

}
