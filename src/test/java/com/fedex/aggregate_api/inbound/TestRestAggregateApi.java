package com.fedex.aggregate_api.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fedex.aggregate_api.domain.AggregatedInfo;
import com.fedex.aggregate_api.domain.AggregatedInfoDeferredService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

import static com.fedex.aggregate_api.util.StringUtil.listToCommaSeparated;
import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestAggregateApi.class)
public class TestRestAggregateApi {
    @Autowired
    MockMvc mvc;

    @MockBean
    AggregatedInfoDeferredService infoServiceDeferred;

    final ObjectMapper mapper = new ObjectMapper();
    @Test
    void testGetAggregatedInfoNoWait() throws Exception {
        // client with five requested items will get a response without waiting
        List<String> orderNumbers = List.of("1","2","3","4","5");
        AggregatedInfo response = new AggregatedInfo(emptyList(),orderNumbers,emptyList());
        given( infoServiceDeferred.getInfoDeferred(emptyList(),orderNumbers,emptyList()))
                .willReturn(buildMockDeferredResult(response));

        MvcResult asyncResult = mvc
                .perform(
                    get( "/aggregation?track="+listToCommaSeparated(orderNumbers))
                        .contentType( MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        mvc.perform( asyncDispatch( asyncResult))
                .andExpect( status().isOk())
                .andExpect( content().string( mapper.writeValueAsString(response)));
    }

    void testGetAggregatedInfoOnTimeout() {
        //TODO
    }

    private DeferredResult<AggregatedInfo> buildMockDeferredResult(AggregatedInfo info) {
        DeferredResult<AggregatedInfo> deferredResult = new DeferredResult<>();
        deferredResult.setResult(info);
        return deferredResult;
    }

}
