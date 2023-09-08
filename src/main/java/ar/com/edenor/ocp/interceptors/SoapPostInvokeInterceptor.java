package ar.com.edenor.ocp.interceptors;

import ar.com.edenor.ocp.logging.LogContainerInfo;
import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import java.util.Objects;

@Slf4j(topic = "splunk")
public class SoapPostInvokeInterceptor extends AbstractSoapInterceptor {

    private String sourceSystem;
    private String edenorIntegrationName;


    public SoapPostInvokeInterceptor() {
        super(Phase.RECEIVE );
    }
    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        Integer responseCode = (Integer) soapMessage.get(Message.RESPONSE_CODE);
        String logContainerInfoKey = (String)soapMessage.getExchange().get(Exchange.TO_ENDPOINT);
        LogContainerInfo logContainerInfo = (LogContainerInfo) soapMessage.getExchange().get(logContainerInfoKey);
        StopWatch watch = logContainerInfo.getWatch();
        if (Objects.nonNull(watch)&& watch.isStarted()) {
            watch.stop();
        }
        LogUtils.putPostInvokeLogInfoToMDC(
                (String) soapMessage.getExchange().get(InterceptorConstants.REQUEST_ID),
                responseCode,
                edenorIntegrationName,
                watch,
                sourceSystem,
                logContainerInfo.getEndpointAddress()
        );
        soapMessage.getExchange().remove(logContainerInfo.getEndpointAddress());
        soapMessage.getExchange().remove(logContainerInfoKey);
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setEdenorIntegrationName(String edenorIntegrationName) {
        this.edenorIntegrationName = edenorIntegrationName;
    }
}
