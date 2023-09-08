package ar.com.edenor.ocp.api;

import ar.com.edenor.ocp.exceptions.ResponseBuilder;
import org.apache.camel.Exchange;

public class CerrarTareaResponseImpl implements ResponseBuilder {
    @Override
    public Object build(Exchange exchange, String errorCode, String descriptionError) {
        final String sourceSystem = exchange.getProperty("SOURCE_SYSTEM",String.class);
        final String externalCode = exchange.getProperty("NUMERO_ORDEN",String.class);
        return
            ApiRestResponse.builder()
                .status("KO")
                .errorCode(errorCode)
                .errorDescription(descriptionError)
                .build();
    }

    @Override
    public Object build(String errorCode, String descriptionError) {
        return
                ApiRestResponse.builder()
                        .status("KO")
                        .errorCode(errorCode)
                        .errorDescription(descriptionError)
                        .build();
    }
}
