package com.fedex.aggregate_api.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregate_api.domain.*;
import jakarta.servlet.AsyncListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.fedex.aggregate_api.util.StringUtil.listToCommaSeparated;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestAggregateApi.class)
@Import({AggregatedInfoDeferredService.class,FedexApiListener.class,AggregatedInfoService.class})
public class IntegrationTestRestAggregateApi {
    @Autowired
    MockMvc mvc;

    @MockBean
    FedexApi fedexApi;

    final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGetAggregatedInfoOnTimeout() throws Exception {
        // client with four requested items will get a response after timeout
        List<String> orderNumbers = List.of("1","2","3","4","5","6","7","8","9","10","11");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        AggregatedInfo requestResponse = new AggregatedInfo(emptyList(), trackingOrderNumbers, emptyList());
        List<TrackingInfo> tracking = List.of(
                new TrackingInfo(trackingOrderNumbers.get(0),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(1),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(3),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(4),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(5),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(6),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(7),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(8),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(9),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(10),"NEW"));
        requestResponse.addTracking(tracking);
        given(fedexApi.getTrackingStatus(any())).willReturn(Mono.just(tracking));
        MvcResult asyncResult = mvc
                .perform(
                        get( "/aggregation?track="+listToCommaSeparated(orderNumbers))
                                .contentType( MediaType.APPLICATION_JSON))
                .andReturn();
        // force DeferredResult timeout is called
        MockAsyncContext ctx = (MockAsyncContext) asyncResult.getRequest().getAsyncContext();
        for (AsyncListener listener : ctx.getListeners()) {
            listener.onTimeout(null);
        }
        mvc.perform( asyncDispatch( asyncResult))
                .andExpect( status().isOk())
                .andExpect( content().string(mapper.writeValueAsString(requestResponse) ));
    }

}
