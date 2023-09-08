package ar.com.edenor.ocp.interceptors;

import ar.com.edenor.ocp.idgenerator.RequestIdGeneratorBean;
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

@Slf4j(topic = "splunk")
public class SoapPreInvokeInterceptor extends AbstractSoapInterceptor {

    public SoapPreInvokeInterceptor() {
        super(Phase.SEND);
    }
    private RequestIdGeneratorBean requestIdGeneratorBean;
    private String edenorIntegrationName;
    private String sourceSystem;
    private String endPointAddress;


    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        String logContainerInfoKey = (String)soapMessage.getExchange().get(Exchange.TO_ENDPOINT);
        String endPointAddress = (String) soapMessage.get(Message.ENDPOINT_ADDRESS);
        StopWatch watch = new StopWatch();
        soapMessage.getExchange().put(logContainerInfoKey,LogContainerInfo
                                    .builder()
                                        .watch(watch)
                                        .endpointAddress(endPointAddress)
                                    .build());
        watch.start();
        String requestId = requestIdGeneratorBean.getId(sourceSystem);
        soapMessage.getExchange().put(InterceptorConstants.REQUEST_ID,requestId);
        LogUtils.putPreInvokeLogInfoToMDC(requestId,sourceSystem,edenorIntegrationName,endPointAddress);
    }

    public void setRequestIdGeneratorBean(RequestIdGeneratorBean requestIdGeneratorBean) {
        this.requestIdGeneratorBean = requestIdGeneratorBean;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public void setEdenorIntegrationName(String edenorIntegrationName) {
        this.edenorIntegrationName = edenorIntegrationName;
    }

    public void setEndPointAddress(String endPointAddress) {
        this.endPointAddress = endPointAddress;
    }
}
