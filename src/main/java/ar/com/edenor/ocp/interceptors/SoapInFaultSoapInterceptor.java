package ar.com.edenor.ocp.interceptors;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.Soap11FaultInInterceptor;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.StaxUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.logging.Logger;

@Slf4j
public class SoapInFaultSoapInterceptor extends AbstractSoapInterceptor {
    private static final Logger LOG = LogUtils.getL7dLogger(Soap11FaultInInterceptor.class);

    public SoapInFaultSoapInterceptor() {
            super(Phase.RECEIVE);
        }


    public void handleMessage(SoapMessage message) throws Fault {
        XMLStreamReader reader = (XMLStreamReader)message.getContent(XMLStreamReader.class);
        message.setContent(Exception.class, unmarshalFault(message, reader));
    }

    public static SoapFault unmarshalFault(SoapMessage message, XMLStreamReader reader) {
        String exMessage = "";
        QName faultCode = null;
        String role = null;
        Element detail = null;
        String lang = null;

        try {
            while(reader.nextTag() == 1) {
                if (reader.getLocalName().equals("faultcode")) {
                    faultCode = StaxUtils.readQName(reader);
                } else if (reader.getLocalName().equals("faultstring")) {
                    lang = reader.getAttributeValue("http://www.w3.org/XML/1998/namespace", "lang");
                    exMessage = reader.getElementText();
                } else if (reader.getLocalName().equals("faultactor")) {
                    role = reader.getElementText();
                } else if (reader.getLocalName().equals("detail")) {
                    detail = StaxUtils.read(reader).getDocumentElement();
                }
            }
        } catch (XMLStreamException var8) {
            throw new SoapFault("Could not parse message.", var8, message.getVersion().getSender());
        }

        if (faultCode == null) {
            faultCode = Soap11.getInstance().getReceiver();
            exMessage = (new Message("INVALID_FAULT", LOG, new Object[0])).toString();
        }

        SoapFault fault = new SoapFault(exMessage, faultCode);
        fault.setDetail(detail);
        fault.setRole(role);
        if (!StringUtils.isEmpty(lang)) {
            fault.setLang(lang);
        }

        return fault;
    }
}

/*
        @Override
        public void handleMessage(SoapMessage message) throws Fault {
        //Todo manejo error 500 - fallo en la autenticacoin
            log.info("handleMessage");
        }


    @Override
        public void handleFault(SoapMessage message) {

        log.info("handleFault");
        }
}

*/
