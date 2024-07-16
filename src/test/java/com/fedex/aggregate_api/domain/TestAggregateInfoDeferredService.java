package com.fedex.aggregate_api.domain;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
public class TestAggregateInfoDeferredService {
    AggregatedInfoService infoService = mock();
    AggregatedInfoDeferredService deferredService;
    FedexApiListener listener;

    private void setup(int timeoutSeconds) {
        listener = new FedexApiListener(infoService);
        deferredService = new AggregatedInfoDeferredService(listener, timeoutSeconds);
    }

    @Test
    void testGetAggregatedInfoNoWait() throws InterruptedException {
        setup(2);
        List<String> orderNumbers = List.of("1","2","3","4","5");
        AggregatedInfo answer = new AggregatedInfo(emptyList(),orderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(new TrackingInfo("1","NEW"),
                new TrackingInfo("2","NEW"),new TrackingInfo("3","NEW"),
                new TrackingInfo("4","NEW"),new TrackingInfo("5","NEW"));
        answer.addTracking(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> response = deferredService.getInfoDeferred(emptyList(), orderNumbers, emptyList());
        Thread.sleep(500);
        AggregatedInfo info = (AggregatedInfo) response.getResult();
        assertEquals(answer, info);
    }
    @Test
    void testGetAggregatedInfoWait() throws InterruptedException {
        setup(2);
        List<String> orderNumbers = List.of("1","2","3","4","5");
        AggregatedInfo answer = new AggregatedInfo(emptyList(),orderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(new TrackingInfo("1","NEW"),
                new TrackingInfo("2","NEW"),new TrackingInfo("3","NEW"),
                new TrackingInfo("4","NEW"));
        answer.addTracking(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> response = deferredService.getInfoDeferred(emptyList(), orderNumbers, emptyList());
        Thread.sleep(500);
        AggregatedInfo info = (AggregatedInfo) response.getResult();
        assertNull(info); // DeferredResult.setResult is not called because AggregatedInfo track map does not have 5 entries
    }

    @Test
    void testGetAggregatedInfoTwoClientsComplete() throws InterruptedException {
        setup(2);
        List<String> orderNumbers = List.of("1","2","3","4","5");
        AggregatedInfo fullAnswer = new AggregatedInfo(emptyList(),orderNumbers,emptyList());
        List<TrackingInfo> fullTracking = List.of(new TrackingInfo("1","NEW"),
                new TrackingInfo("2","NEW"),new TrackingInfo("3","NEW"),
                new TrackingInfo("4","NEW"),new TrackingInfo("5","NEW"));
        fullAnswer.addTracking(fullTracking);
        AggregatedInfo answer = new AggregatedInfo(emptyList(),orderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(new TrackingInfo("1","NEW"),
                new TrackingInfo("2","NEW"),new TrackingInfo("3","NEW"),
                new TrackingInfo("4","NEW"));
        answer.addTracking(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> response = deferredService.getInfoDeferred(emptyList(), orderNumbers, emptyList());
        // now complete with the fifth
        List<String> secondOrderNumbers = List.of("5");
        AggregatedInfo secondAnswer = new AggregatedInfo(emptyList(),secondOrderNumbers,emptyList());
        List<TrackingInfo> oneTracking = List.of(new TrackingInfo("5","NEW"));
        secondAnswer.addTracking(oneTracking);
        given( infoService.getInfo(secondAnswer)).willReturn(secondAnswer);
        DeferredResult<AggregatedInfo> secondResponse = deferredService.getInfoDeferred(emptyList(), secondOrderNumbers, emptyList());
        Thread.sleep(500);
        AggregatedInfo secondInfo = (AggregatedInfo) secondResponse.getResult();
        assertEquals(secondAnswer, secondInfo);
        // the first client should now have all data
        assertEquals(fullAnswer, response.getResult());
    }

}
