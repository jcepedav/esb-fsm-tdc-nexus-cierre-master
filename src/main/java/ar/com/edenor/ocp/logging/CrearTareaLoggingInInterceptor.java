package ar.com.edenor.ocp.logging;


import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.slf4j.MDC;

import java.util.Objects;

public class CrearTareaLoggingInInterceptor extends EdenorLoggingInInterceptor {

    protected MaskSensitiveHelper integrationTypeHelper = new MaskSensitiveHelper();
    private String edenorIntegrationType;

    protected void setTypeInterfaceToMessageAndPutToMDC(Message message,String payload) {
        //si no viene el valor en el mensje, seteamos el valor definido en el properties
        message.getExchange().put(LoggingConstants.INTEGRATION_TYPE, edenorIntegrationType);
        MDC.put(LoggingConstants.INTEGRATION_TYPE, edenorIntegrationType);
    }
    public void setEdenorIntegrationType(String edenorIntegrationType) {
        this.edenorIntegrationType = edenorIntegrationType;
    }
}