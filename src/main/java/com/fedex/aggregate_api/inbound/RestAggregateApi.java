package com.fedex.aggregate_api.inbound;


import com.fedex.aggregate_api.domain.AggregatedInfo;
import com.fedex.aggregate_api.domain.AggregatedInfoDeferredService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import static com.fedex.aggregate_api.util.StringUtil.commaSeparatedtoList;

@RestController
@RequestMapping("aggregation")
public class RestAggregateApi {
    private final AggregatedInfoDeferredService service;
    public RestAggregateApi(AggregatedInfoDeferredService aggregatedInfoDeferredService) {
        this.service = aggregatedInfoDeferredService;
    }

    @GetMapping
    DeferredResult<AggregatedInfo> getAggregatedInfo(
            @RequestParam(required = false) String pricing,
            @RequestParam(required = false) String track,
            @RequestParam(required = false) String shipments) {
        return service.getInfoDeferred(
                commaSeparatedtoList(pricing),
                commaSeparatedtoList(track),
                commaSeparatedtoList(shipments));
    }
}
