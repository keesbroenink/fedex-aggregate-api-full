package com.fedex.aggregate_api.outbound;

import org.springframework.http.HttpStatusCode;

public class FedexApiClientException extends RuntimeException{
    public FedexApiClientException(Throwable t) {
        super(t);
    }

    public FedexApiClientException(HttpStatusCode httpStatusCode) {
        super("Error: "+httpStatusCode.toString());
    }
}
