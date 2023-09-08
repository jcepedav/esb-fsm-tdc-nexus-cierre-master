package ar.com.edenor.ocp.security;

import ar.com.edenor.ocp.exceptions.ResponseBuilder;
import ar.com.edenor.ocp.idgenerator.RequestIdGeneratorBean;
import ar.com.edenor.ocp.logging.LogUtils;
import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

@Priority(2000)
public class EdenorAuthorizingFilter implements ContainerRequestFilter {
    private String edenorIntegrationName;
    private AbstractPhaseInterceptor interceptor;
    private String sourceSystem;
    private ResponseBuilder responseBuilder;
    @Value("${edenor.integration.type}")
    private String edenorIntegrationType;
    @Autowired
    private RequestIdGeneratorBean requestIdGeneratorBean;

    public EdenorAuthorizingFilter() {
    }

    public void filter(ContainerRequestContext context) {
        try {
            this.interceptor.handleMessage(JAXRSUtils.getCurrentMessage());
        } catch (AccessDeniedException ex) {
            requestIdGeneratorBean.removeId();
            ResponseBuilderImpl builder = new ResponseBuilderImpl();
            builder.status(Response.Status.UNAUTHORIZED);
            Object responseEndpoint = responseBuilder.build("AuthError", ex.getMessage());
            builder.entity(responseEndpoint);
            Response response = builder.build();
            response.getMetadata().add("Content-Type","application/json");
            LogUtils.putSecurityAccessErrorLogToMDC(requestIdGeneratorBean.getId(sourceSystem),"AuthError", ex.getMessage(), edenorIntegrationName, String.valueOf(Response.Status.UNAUTHORIZED.getStatusCode()),sourceSystem, edenorIntegrationType);
            context.abortWith(response);
        }

    }

    public void setInterceptor(AbstractPhaseInterceptor in) {
        this.interceptor = in;
    }

    public void setEdenorIntegrationName(String edenorIntegrationName) {
        this.edenorIntegrationName = edenorIntegrationName;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }
}

