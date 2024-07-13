package com.fedex.aggregate_api.inbound;


import com.fedex.aggregate_api.domain.AggregatedInfo;
import com.fedex.aggregate_api.domain.AggregatedInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.fedex.aggregate_api.util.StringUtil.commaSeparatedtoList;

@RestController
@RequestMapping("aggregation")
public class RestAggregateApi {
    private final AggregatedInfoService service;
    public RestAggregateApi(AggregatedInfoService aggregatedInfoService) {
        this.service = aggregatedInfoService;
    }
    @GetMapping("/")
    Mono<AggregatedInfo> getAggregatedInfo(
            @RequestParam(required = false) String pricing,
            @RequestParam(required = false) String track,
            @RequestParam(required = false) String shipments) {
        return service.getInfo(
                commaSeparatedtoList(pricing),
                commaSeparatedtoList(track),
                commaSeparatedtoList(shipments));
    }


}
