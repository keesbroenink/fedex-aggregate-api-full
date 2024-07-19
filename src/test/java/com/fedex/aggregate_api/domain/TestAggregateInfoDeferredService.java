package com.fedex.aggregate_api.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    ObjectMapper mapper = new ObjectMapper();

    private void setup(int timeoutSeconds) {
        listener = new FedexApiListener(infoService);
        deferredService = new AggregatedInfoDeferredService(listener, timeoutSeconds);
    }

    @Test
    void testGetAggregatedInfoNoWait() throws InterruptedException {
        setup(2);
        List<String> orderNumbers = List.of("1","2","3","4","5");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        AggregatedInfo answer = new AggregatedInfo(emptyList(),trackingOrderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(
                new TrackingInfo(trackingOrderNumbers.get(0),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(1),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(2),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(3),"NEW"),
                new TrackingInfo(trackingOrderNumbers.get(4),"NEW"));
        answer.addTracking(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> response = deferredService.getInfoDeferred(answer);
        Thread.sleep(500);
        AggregatedInfo info = (AggregatedInfo) response.getResult();
        assertEquals(answer, info);
    }
    @Test
    void testGetAggregatedInfoWait() throws InterruptedException {
        setup(2);
        List<String> orderNumbers = List.of("1","2","3","4","5");
        List<TrackingOrderNumber> trackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        AggregatedInfo answer = new AggregatedInfo(emptyList(),trackingOrderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(
                new TrackingInfo(trackingOrderNumbers.get(0),"DELIVERED"),
                new TrackingInfo(trackingOrderNumbers.get(1),"DELIVERED"),
                new TrackingInfo(trackingOrderNumbers.get(2),"DELIVERED"),
                new TrackingInfo(trackingOrderNumbers.get(3),"DELIVERED"));
        answer.addTrackingAlways(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> response = deferredService.getInfoDeferred(answer);
        Thread.sleep(500);
        AggregatedInfo info = (AggregatedInfo) response.getResult();
        assertNull(info); // DeferredResult.setResult is not called because AggregatedInfo track map does not have 5 entries
    }

    @Test
    void testGetAggregatedInfoTwoClientsComplete() throws InterruptedException, JsonProcessingException {
        setup(2);

        List<String> orderNumbers = List.of("1","2","3","4","5");
        List<TrackingOrderNumber> fullTrackingOrderNumbers = TrackingInfo.fromListString(orderNumbers);
        AggregatedInfo fullAnswer = new AggregatedInfo(emptyList(),fullTrackingOrderNumbers,emptyList());
        List<TrackingInfo> fullTracking = List.of(
                new TrackingInfo(fullTrackingOrderNumbers.get(0),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(1),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(2),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(3),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(4),"WAITING"));
        fullAnswer.addTracking(fullTracking);
        // now ask for 5 and return 4
        AggregatedInfo answer = new AggregatedInfo(emptyList(),fullTrackingOrderNumbers,emptyList());
        List<TrackingInfo> tracking = List.of(
                new TrackingInfo(fullTrackingOrderNumbers.get(0),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(1),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(2),"WAITING"),
                new TrackingInfo(fullTrackingOrderNumbers.get(3),"WAITING"));
        answer.addTracking(tracking);
        given( infoService.getInfo(answer)).willReturn(answer);
        DeferredResult<AggregatedInfo> responseClient1 = deferredService.getInfoDeferred(answer);
        // now complete with the fifth
        List<String> secondOrderNumbers = List.of("5");
        List<TrackingOrderNumber> fifthOrderNumber = TrackingInfo.fromListString(secondOrderNumbers);
        AggregatedInfo secondAnswer = new AggregatedInfo(emptyList(),fifthOrderNumber,emptyList());
        List<TrackingInfo> oneTracking = List.of(new TrackingInfo(fifthOrderNumber.getFirst(),"WAITING"));
        secondAnswer.addTracking(oneTracking);
        given( infoService.getInfo(secondAnswer)).willReturn(secondAnswer);
        DeferredResult<AggregatedInfo> responseClient2 = deferredService.getInfoDeferred(secondAnswer);
        Thread.sleep(500);
        AggregatedInfo secondInfo = (AggregatedInfo) responseClient2.getResult();
        assertEquals(secondAnswer, secondInfo);
        // the first client should now have all data
        assertEquals(mapper.writeValueAsString(fullAnswer), mapper.writeValueAsString(responseClient1.getResult()));
    }

}
