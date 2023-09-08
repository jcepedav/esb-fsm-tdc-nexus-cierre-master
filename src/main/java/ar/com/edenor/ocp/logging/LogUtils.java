package ar.com.edenor.ocp.logging;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.MDC;

import java.util.Objects;

public class LogUtils {
    public static String SPLUNK_TARGET = "splunk";
    public static String PRE_INVOKE_CALL = "REQUEST";
    public static String POST_INVOKE_CALL = "RESPONSE";

    public static String checkStatus(Integer statusCode) {
        String status = "ERROR";
        if (Objects.nonNull(statusCode)) {
            if (statusCode.intValue() >= 200 && statusCode.intValue() <300) {
                status = "OK";
            }
        }
        return status;
    }

    public static void putPreInvokeLogInfoToMDC(String requestId, String sourceSystem, String edenorIntegrationName, String endpoint) {
        MDC.clear();
        MDC.put("target",SPLUNK_TARGET);
        MDC.put("eventCall",PRE_INVOKE_CALL);
        MDC.put("endpointCall",endpoint);
        MDC.put("sourceSystem",sourceSystem);
        MDC.put("requestId",requestId);
        MDC.put("integrationName",edenorIntegrationName);
    }

    public static void putPostInvokeLogInfoToMDC(String requestId, Integer responseCode, String edenorIntegrationName, StopWatch watch , String sourceSystem , String endpoint) {
        MDC.put("target",SPLUNK_TARGET);
        MDC.put("eventCall",POST_INVOKE_CALL);
        MDC.put("endpointCall",endpoint);
        MDC.put("requestId",requestId);
        MDC.put("endpointResponseCode",responseCode.toString());
        MDC.put("status", LogUtils.checkStatus(responseCode));
        MDC.put("integrationName",edenorIntegrationName);
        MDC.put("sourceSystem",sourceSystem);
        MDC.put("elapsedTime", Objects.nonNull(watch)?Long.toString(watch.getTime()):"0");
    }

    public static void putErrorLogToMDC(String errorCode, String errorDescription) {
        MDC.remove("eventCall");
        MDC.remove("endpointCall");
        MDC.remove("endpointResponseCode");
        MDC.remove("status");
        MDC.remove("elapsedTime");
        MDC.put("errorCode",errorCode);
        MDC.put("errorDescription",errorDescription);
    }

    public static void putSoapErrorLogToMDC(String errorCode, String errorDescription) {
        MDC.put("errorCode",errorCode);
        MDC.put("errorDescription",errorDescription);
    }

    public static void putSecurityAccessErrorLogToMDC(String requestId,String errorCode, String errorDescription, String edenorIntegrationName, String responseCode, String sourceSystem, String edenorIntegrationType) {
        MDC.put("target",SPLUNK_TARGET);
        MDC.put("integrationName",edenorIntegrationName);
        MDC.put("requestId",requestId);
        MDC.put("endpointResponseCode",responseCode);
        MDC.put("errorCode",errorCode);
        MDC.put("errorDescription",errorDescription);
        MDC.put("sourceSystem",sourceSystem);
        MDC.put("status", "ERROR");
        MDC.put("integrationType", edenorIntegrationType);
    }

   public static void putInputErrorToMDC(int responseCode, String edenorIntegrationName, String sourceSystem , String endpoint,String errorCode, String errorDescription) {
        MDC.put("target",SPLUNK_TARGET);
        MDC.put("eventCall",PRE_INVOKE_CALL);
        MDC.put("endpointCall",endpoint);
        MDC.put("endpointResponseCode",Integer.toString(responseCode));
        MDC.put("integrationName",edenorIntegrationName);
        MDC.put("sourceSystem",sourceSystem);
        MDC.put("status", "ERROR");
        MDC.put("errorCode",errorCode);
        MDC.put("errorDescription",errorDescription);
    }


}
