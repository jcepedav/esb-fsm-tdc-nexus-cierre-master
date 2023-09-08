package ar.com.edenor.ocp.interceptors;

import ar.com.edenor.ocp.logging.LogContainerInfo;
import ar.com.edenor.ocp.logging.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Objects;

@Slf4j(topic = "splunk")
public class RestPostInvokeInterceptor extends AbstractPhaseInterceptor<Message> {

    private String edenorIntegrationName;
    private String sourceSystem;
    private String endPointAddress;


    public RestPostInvokeInterceptor() {
        super(Phase.SEND );
    }
    @Override
    public void handleMessage(Message message) throws Fault {
        Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
        LogContainerInfo logContainerInfo = (LogContainerInfo) message.getExchange().get(endPointAddress);
        if (Objects.nonNull(logContainerInfo)) {
            StopWatch watch = logContainerInfo.getWatch();
            if (Objects.nonNull(watch)&& watch.isStarted()) {
                watch.stop();
            }
            LogUtils.putPostInvokeLogInfoToMDC(
                    (String) message.getExchange().get(InterceptorConstants.REQUEST_ID),
                    responseCode,
                    edenorIntegrationName,
                    watch,
                    sourceSystem,
                    logContainerInfo.getEndpointAddress()
            );
            message.getExchange().remove(logContainerInfo);
            message.getExchange().remove(edenorIntegrationName);
        }
    }

    public String getEdenorIntegrationName() {
        return edenorIntegrationName;
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
}
