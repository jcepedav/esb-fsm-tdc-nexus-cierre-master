package ar.com.edenor.ocp.exceptions;

import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.cxf.interceptor.Fault;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * Class which handles exceptions to generate a standard error response.
 *
 * @author Gabriel Forradellas
 * @version 0.1
 * @since 2019-02-12
 */
@Component("exceptionHandler")
@Slf4j(topic = "splunk")
public  class ExceptionHandler {

    private ResponseBuilder responseBuilder;

    @Value("${endpoint.nexusApi.address}")
    private String clientEndPoint;

    private void respond(Exchange exchange,HttpStatus httpCode , String errorCode, String errorDescription) {
        exchange.getOut().setBody(responseBuilder.build(exchange,errorCode,errorDescription));
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, httpCode.value());
    }


    public <T> T getException(Exchange exchange, Class<T> exceptionClass) {
        T exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, exceptionClass);
        log.error("Internal Error",exception);
        return exception;
    }


    public void genericException(Exchange exchange) {
        Exception exception = getException(exchange, Exception.class);
        LogUtils.putErrorLogToMDC("InternalError", exception.getMessage());
        respond(exchange,HttpStatus.INTERNAL_SERVER_ERROR,"InternalError",(Objects.isNull(exception.getMessage())?"InternalError":exception.getMessage()));

    }

    public void onSoapFaultException(Exchange exchange) {
        Exception exception = getException(exchange, Exception.class);
        LogUtils.putSoapErrorLogToMDC("InternalError", exception.getMessage());
        respond(exchange,HttpStatus.INTERNAL_SERVER_ERROR,"InternalError","Error al invocar " + clientEndPoint +  ": "+ exception.getMessage());
        //logueamos el error para imprimir los detalles de la invocacion
        log.info(exception.getMessage());

    }

    public void onInterceptorFaultException(Exchange exchange) {
        Fault exception = (Fault)getException(exchange, Exception.class);
        String errorDescription = exception.getMessage();
        String errorCode = "connectionError";
        if (Objects.nonNull(exception.getCause())) {
            errorDescription = exception.getCause().getMessage();
            if (errorDescription.contains("Authorization")) {
                errorCode = "AuthError";
                errorDescription ="Error autentication OCP--NEXUS";
            }
         }
        LogUtils.putSoapErrorLogToMDC(errorCode, errorDescription);
        respond(exchange,HttpStatus.INTERNAL_SERVER_ERROR,errorCode,errorDescription);
        //logueamos el error para imprimir los detalles de la invocacion
        log.info(errorDescription);

    }
    public void onConnectionException(Exchange exchange) {
        Exception exception = getException(exchange, Exception.class);
        LogUtils.putErrorLogToMDC("connectionError", exception.getMessage());

        String errorDescription = null;
        if (Objects.nonNull(exception.getCause())) {
            errorDescription = exception.getCause().getMessage();
        } else {
            errorDescription = "Error al invocar a " + clientEndPoint;
        }
        respond(exchange,HttpStatus.INTERNAL_SERVER_ERROR,"ConnectionError",errorDescription);
        log.error("Internal Error ", exception);
    }

    public void onHttpException(Exchange exchange) {
        Exception exception = getException(exchange, Exception.class);
        LogUtils.putErrorLogToMDC("ConnectionError", exception.getMessage());
        respond(exchange,HttpStatus.INTERNAL_SERVER_ERROR,"ConnectionError",exception.getMessage());
        log.error("Internal Error ", exception);
    }

    public void onBusinessException(Exchange exchange) {
        BusinessException exception = getException(exchange, BusinessException.class);
        exchange.setProperty("errorCode",exception.getErrorCode());
        exchange.setProperty("errorDescription",exception.getMessage());
        LogUtils.putErrorLogToMDC(exception.getErrorCode(), exception.getMessage());
        respond(exchange, HttpStatus.BAD_REQUEST,exception.getErrorCode(), exception.getMessage());
        log.error("Error capturado: "+ exception.getMessage());
    }


    public void setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }
}
