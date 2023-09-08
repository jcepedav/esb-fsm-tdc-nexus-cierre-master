package ar.com.edenor.ocp.interceptors;

import ar.com.edenor.ocp.idgenerator.RequestIdGeneratorBean;
import ar.com.edenor.ocp.logging.LogContainerInfo;
import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

@Slf4j(topic = "splunk")
public class SoapInPreInvokeInterceptor extends AbstractSoapInterceptor {

    private String edenorIntegrationName;
    private RequestIdGeneratorBean requestIdGeneratorBean;
    private String sourceSystem;
    private String endPointAddress;

    public SoapInPreInvokeInterceptor() {
        super(Phase.POST_LOGICAL );
    }

    @Override
    public void handleMessage(SoapMessage soapMessage) throws Fault {
        requestIdGeneratorBean.removeId();
        StopWatch watch = new StopWatch();
        watch.start();
        soapMessage.getExchange().put(endPointAddress, LogContainerInfo
                .builder()
                .watch(watch)
                .endpointAddress(endPointAddress)
                .build());
        String requestId = requestIdGeneratorBean.getId(sourceSystem);
        soapMessage.getExchange().put(InterceptorConstants.REQUEST_ID,requestId);
        LogUtils.putPreInvokeLogInfoToMDC(requestId,sourceSystem,edenorIntegrationName,endPointAddress);
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
