package ar.com.edenor.ocp.exceptions;

import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import javax.ws.rs.core.Response;

@Slf4j
public class BaseInputExceptionMapper {
    private String edenorIntegrationName;
    private String sourceSystem;
    private String endPointAddress;
    private ResponseBuilder responseBuilder;


    protected Response toInternalResponse(String message) {
        ResponseBuilderImpl builder = new ResponseBuilderImpl();
        builder.status(Response.Status.BAD_REQUEST);
        Object responseEndpoint = responseBuilder.build("Estructura Mensaje con Error", message);
        builder.entity(responseEndpoint);
        Response response = builder.build();
        response.getMetadata().add("Content-Type","application/json");
        LogUtils.putInputErrorToMDC(Response.Status.NOT_FOUND.getStatusCode(),edenorIntegrationName,sourceSystem,endPointAddress,"Estructura Mensaje con Error",message);
        return response;
    }

    public void setEdenorIntegrationName(String edenorIntegrationName) {
        this.edenorIntegrationName = edenorIntegrationName;
    }
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    public void setEndPointAddress(String endPointAddress) {
        this.endPointAddress = endPointAddress;
    }
    public void setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }

}

