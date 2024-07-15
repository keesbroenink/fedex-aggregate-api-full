package com.fedex.aggregate_api.outbound;

public class FedexApiClientException extends RuntimeException{
    public FedexApiClientException(Throwable t) {
        super(t);
    }

}
