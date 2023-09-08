package ar.com.edenor.ocp.interceptors;

import ar.com.edenor.ocp.idgenerator.RequestIdGeneratorBean;
import ar.com.edenor.ocp.logging.LogContainerInfo;
import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

import java.util.Objects;

@Slf4j(topic = "splunk")
public class SoapInPostInvokeInterceptor extends AbstractSoapInterceptor {

    private String edenorIntegrationName;
    private RequestIdGeneratorBean requestIdGeneratorBean;
    private String sourceSystem;
    private String endPointAddress;


    public SoapInPostInvokeInterceptor() {
        super(Phase.SEND );
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        Integer responseCode = (Integer) soapMessage.get(Message.RESPONSE_CODE);
        LogContainerInfo logContainerInfo = (LogContainerInfo) soapMessage.getExchange().get(endPointAddress);
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
        soapMessage.getExchange().remove(edenorIntegrationName);
        soapMessage.getExchange().remove(logContainerInfo);

    }

    public String getEdenorIntegrationName() {
        return edenorIntegrationName;
    }

    public void setEdenorIntegrationName(String edenorIntegrationName) {
        this.edenorIntegrationName = edenorIntegrationName;
    }

    public void setRequestIdGeneratorBean(RequestIdGeneratorBean requestIdGeneratorBean) {
        this.requestIdGeneratorBean = requestIdGeneratorBean;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setEndPointAddress(String endPointAddress) {
        this.endPointAddress = endPointAddress;
    }
}
