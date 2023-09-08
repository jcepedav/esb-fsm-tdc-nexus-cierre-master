package ar.com.edenor.ocp.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.MDC;

@Slf4j
public abstract class BaseProcessor implements Processor {

    protected void copyHeadersAndBody(Exchange exchange) {
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        exchange.getOut().setBody(exchange.getIn().getBody());
    }

    protected void log(String message, String target) {
        MDC.put("target",target);
        log.info(message);
    }

}
