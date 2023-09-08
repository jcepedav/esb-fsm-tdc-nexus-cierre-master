package ar.com.edenor.ocp.logging;


import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedWriter;
import org.apache.cxf.message.Message;
import org.slf4j.MDC;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EdenorLoggingInInterceptor extends LoggingInInterceptor {

    protected MaskSensitiveHelper maskSensitiveHelper = new MaskSensitiveHelper();
    protected MaskSensitiveHelper integrationTypeHelper = new MaskSensitiveHelper();
    private String edenorIntegrationType;

    @Override
    public void handleMessage(Message message) throws Fault {
        final Set<MaskSensitiveHelper.ReplacementPair> replacementsXML = new HashSet<>();
        final Set<MaskSensitiveHelper.ReplacementPair> replacementsJSON = new HashSet<>();

        maskSensitiveHelper.addSensitiveElementNames(new HashSet<>(Arrays.asList("password")),replacementsXML,replacementsJSON);
        if (!isLoggingDisabledNow(message)) {
            this.createExchangeId(message);
            LogEvent event = (new DefaultLogEventMapper()).map(message);
            if (this.shouldLogContent(event)) {
                this.addContent(message, event);
            } else {
                event.setPayload("--- Content suppressed ---");

            }

            final String maskedContent = maskSensitiveHelper.maskSensitiveElements(message, event.getPayload(),replacementsXML,replacementsJSON);
            setTypeInterfaceToMessageAndPutToMDC(message,maskedContent);
            event.setPayload(maskedContent);

            removeHeadersAuthorization(event);

            this.sender.send(event);
        }

    }

    private void addContent(Message message, LogEvent event) {
        try {
            CachedOutputStream cos = (CachedOutputStream)message.getContent(CachedOutputStream.class);
            if (cos != null) {
                this.handleOutputStream(event, message, cos);
            } else {
                removeHeadersAuthorization(event);
                CachedWriter writer = (CachedWriter)message.getContent(CachedWriter.class);
                if (writer != null) {

                    this.handleWriter(event, writer);
                }
            }

        } catch (IOException var5) {
            throw new Fault(var5);
        }
    }

    private void handleOutputStream(LogEvent event, Message message, CachedOutputStream cos) throws IOException {
        String encoding = (String)message.get(Message.ENCODING);
        if (StringUtils.isEmpty(encoding)) {
            encoding = StandardCharsets.UTF_8.name();
        }

        StringBuilder payload = new StringBuilder();
        cos.writeCacheTo(payload, encoding, (long)this.limit);
        cos.close();

        event.setPayload(payload.toString());
        boolean isTruncated = cos.size() > (long)this.limit && this.limit != -1;
        event.setPayload(removeUsernameToken(payload.toString()));
        event.setTruncated(isTruncated);
        event.setFullContentFile(cos.getTempFile());
    }

    private void handleWriter(LogEvent event, CachedWriter writer) throws IOException {
        boolean isTruncated = writer.size() > (long)this.limit && this.limit != -1;
        StringBuilder payload = new StringBuilder();
        writer.writeCacheTo(payload, (long)this.limit);
        writer.close();
        event.setPayload(payload.toString());
        event.setTruncated(isTruncated);
        event.setFullContentFile(writer.getTempFile());
    }

    int getWireTapLimit() {
        if (this.limit == -1) {
            return -1;
        } else {
            return this.limit == 2147483647 ? this.limit : this.limit + 1;
        }
    }

    private String removeUsernameToken(String payloadString) {
        String newPayload;
        newPayload = payloadString.replaceFirst("<Password>(.+?)</Password>", "<Password>**********</Password>");
        return newPayload;

    }
    private LogEvent removeHeadersAuthorization(LogEvent event) {
        event.getHeaders().put("Authorization","******");
        return event;

    }

    protected void setTypeInterfaceToMessageAndPutToMDC(Message message,String payload) {
        //si no viene el valor en el mensje, seteamos el valor definido en el properties
        message.getExchange().put(LoggingConstants.INTEGRATION_TYPE, edenorIntegrationType);
        MDC.put(LoggingConstants.INTEGRATION_TYPE, edenorIntegrationType);
    }

    public void setEdenorIntegrationType(String edenorIntegrationType) {
        this.edenorIntegrationType = edenorIntegrationType;
    }
}