package ar.com.edenor.ocp.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

@Slf4j
public class SoapOutFaultSoapInterceptor extends AbstractSoapInterceptor {

    public SoapOutFaultSoapInterceptor() {
            super(Phase.MARSHAL);
        }


    @Override
        public void handleMessage(SoapMessage message) throws Fault {
            Fault fault = (Fault) message.getContent(Exception.class);
            log.info("handleMessage");
        }

        @Override
        public void handleFault(SoapMessage message) {
            Fault fault = (Fault) message.getContent(Exception.class);

        log.info("handleFault");
        }
}
