package com.fedex.aggregate_api.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregate_api.domain.TrackingInfo;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestFedexApiClient {
    static MockWebServer mockWebServer;
    FedexApiClient apiClient;
    @Autowired
    WebClient.Builder springWebBuilder;

    ObjectMapper mapper = new ObjectMapper();
    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }
    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.close();
    }
    @Test
    void testGetTrackStatus()throws JsonProcessingException {
        apiClient = new FedexApiClient(springWebBuilder,mockWebServer.url("").toString());
        String orderNumber = "123";
        String trackingStatus = "DELIVERED";
        Map<String,String> map = Map.of(orderNumber, trackingStatus);
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(HttpStatus.OK.value())
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(getMockedResponse(map))
        );

        Mono<List<TrackingInfo>> result = apiClient.getTrackingStatus(List.of(orderNumber));
        StepVerifier.create(result)
                .expectNextMatches(genericInfoList -> genericInfoList.getFirst().trackingOrderNumber().orderNumber().equals(orderNumber)
                                                   && genericInfoList.getFirst().status().equals(trackingStatus)    )
                .verifyComplete();
    }

    @Test
    void testErrorResponse()throws JsonProcessingException {
        apiClient = new FedexApiClient(springWebBuilder,mockWebServer.url("").toString());
        String orderNumber = "123";
        String trackingStatus = "DELIVERED";
        Map<String,String> map = Map.of(orderNumber, trackingStatus);
        mockWebServer.enqueue(
                new MockResponse().setResponseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(getMockedResponse(map))
        );

        Mono<List<TrackingInfo>> result = apiClient.getTrackingStatus(List.of(orderNumber));
        StepVerifier.create(result)
                .expectNextMatches(genericInfoList -> genericInfoList.getFirst().trackingOrderNumber().orderNumber().equals(orderNumber)
                                  && genericInfoList.getFirst().status() == null    )
                .verifyComplete();
    }
    private String getMockedResponse(Map<String,String> data) throws JsonProcessingException {
        return mapper.writeValueAsString(data);
    }
}
