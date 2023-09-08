package ar.com.edenor.ocp.exceptions;

import org.apache.camel.Exchange;

public interface ResponseBuilder {
    Object build(Exchange exchange, String errorCode, String descriptionError);
    Object build( String errorCode, String descriptionError);
}
