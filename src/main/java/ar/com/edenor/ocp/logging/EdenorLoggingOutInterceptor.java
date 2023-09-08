package ar.com.edenor.ocp.logging;

import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.ext.logging.AbstractLoggingInterceptor;
import org.apache.cxf.ext.logging.event.DefaultLogEventMapper;
import org.apache.cxf.ext.logging.event.LogEvent;
import org.apache.cxf.ext.logging.event.LogEventSender;
import org.apache.cxf.ext.logging.event.PrintWriterEventSender;
import org.apache.cxf.ext.logging.slf4j.Slf4jVerboseEventSender;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.slf4j.MDC;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class EdenorLoggingOutInterceptor extends AbstractLoggingInterceptor {

    protected MaskSensitiveHelper errorCodeHelper = new MaskSensitiveHelper();

    protected MaskSensitiveHelper maskSensitiveHelper = new MaskSensitiveHelper();
    public EdenorLoggingOutInterceptor() {
        this((LogEventSender)(new Slf4jVerboseEventSender()));
    }

    public EdenorLoggingOutInterceptor(PrintWriter writer) {
        this((LogEventSender)(new PrintWriterEventSender(writer)));
    }

    public EdenorLoggingOutInterceptor(LogEventSender sender) {
        super("pre-stream", sender);
        this.addBefore(StaxOutInterceptor.class.getName());
    }

    public void handleMessage(Message message) throws Fault {
        if (!isLoggingDisabledNow(message)) {
            this.createExchangeId(message);
            OutputStream os = (OutputStream)message.getContent(OutputStream.class);
            if (os != null) {
                EdenorLoggingOutInterceptor.LoggingCallback callback = new EdenorLoggingOutInterceptor.LoggingCallback(this.sender, message, os, this.limit);
                message.setContent(OutputStream.class, this.createCachingOut(message, os, callback));
            } else {
                Writer iowriter = (Writer)message.getContent(Writer.class);
                if (iowriter != null) {
                    message.setContent(Writer.class, new EdenorLoggingOutInterceptor.LogEventSendingWriter(this.sender, message, iowriter, this.limit));
                }
            }

        }
    }

    private OutputStream createCachingOut(Message message, OutputStream os, CachedOutputStreamCallback callback) {
        CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
        if (this.threshold > 0L) {
            newOut.setThreshold(this.threshold);
        }

        if (this.limit > 0) {
            newOut.setCacheLimit((long)this.getCacheLimit());
        }

        newOut.registerCallback(callback);
        return newOut;
    }

    private int getCacheLimit() {
        return this.limit == 2147483647 ? this.limit : this.limit + 1;
    }

    public class LoggingCallback implements CachedOutputStreamCallback {
        private final Message message;
        private final OutputStream origStream;
        private final int lim;
        private LogEventSender sender;

        public LoggingCallback(LogEventSender sender, Message msg, OutputStream os, int limit) {
            this.sender = sender;
            this.message = msg;
            this.origStream = os;
            this.lim = limit == -1 ? 2147483647 : limit;
        }

        public void onFlush(CachedOutputStream cos) {
        }

        public void onClose(CachedOutputStream cos) {
            LogEvent event = (new DefaultLogEventMapper()).map(this.message);
            if (EdenorLoggingOutInterceptor.this.shouldLogContent(event)) {
                this.copyPayload(cos, event);
            } else {
                event.setPayload("--- Content suppressed ---");
            }

            removeHeadersAuthorization(event);
            this.sender.send(event);

            try {
                cos.lockOutputStream();
                cos.resetOut((OutputStream)null, false);
            } catch (Exception var4) {
            }

            this.message.setContent(OutputStream.class, this.origStream);
        }

        private void copyPayload(CachedOutputStream cos, LogEvent event) {
            try {
                String encoding = (String)this.message.get(Message.ENCODING);
                StringBuilder payload = new StringBuilder();
                this.writePayload(payload, cos, encoding, event.getContentType());
                event.setPayload(removeUsernameToken(payload.toString()));
                event.setPayload(payload.toString());
                setTypeInterfaceToMDC(message);
                evaluateResponseMessage(message,payload.toString());
                boolean isTruncated = cos.size() > (long) EdenorLoggingOutInterceptor.this.limit && EdenorLoggingOutInterceptor.this.limit != -1;
                event.setTruncated(isTruncated);
            } catch (Exception var6) {
            }

        }

        protected void writePayload(StringBuilder builder, CachedOutputStream cos, String encoding, String contentType) throws Exception {
            if (StringUtils.isEmpty(encoding)) {
                cos.writeCacheTo(builder, (long)this.lim);
            } else {
                cos.writeCacheTo(builder, encoding, (long)this.lim);
            }

        }
    }

    private class LogEventSendingWriter extends FilterWriter {
        StringWriter out2;
        int count;
        Message message;
        final int lim;
        private LogEventSender sender;

        LogEventSendingWriter(LogEventSender sender, Message message, Writer writer, int limit) {
            super(writer);
            this.sender = sender;
            this.message = message;
            if (!(writer instanceof StringWriter)) {
                this.out2 = new StringWriter();
            }

            this.lim = limit == -1 ? 2147483647 : limit;
        }

        public void write(int c) throws IOException {
            super.write(c);
            if (this.out2 != null && this.count < this.lim) {
                this.out2.write(c);
            }

            ++this.count;
        }

        public void write(char[] cbuf, int off, int len) throws IOException {
            super.write(cbuf, off, len);
            if (this.out2 != null && this.count < this.lim) {
                this.out2.write(cbuf, off, len);
            }

            this.count += len;
        }

        public void write(String str, int off, int len) throws IOException {
            super.write(str, off, len);
            if (this.out2 != null && this.count < this.lim) {
                this.out2.write(str, off, len);
            }

            this.count += len;
        }

        public void close() throws IOException {
            LogEvent event = (new DefaultLogEventMapper()).map(this.message);
            StringWriter w2 = this.out2;
            if (w2 == null) {
                w2 = (StringWriter)this.out;
            }

            String payload = EdenorLoggingOutInterceptor.this.shouldLogContent(event) ? this.getPayload(event, w2) : "--- Content suppressed ---";
            event.setPayload(removeUsernameToken(payload));
            event.setPayload(payload);
            removeHeadersAuthorization(event);
            this.sender.send(event);
            this.message.setContent(Writer.class, this.out);
            super.close();
        }

        private String getPayload(LogEvent event, StringWriter w2) {
            StringBuilder payload = new StringBuilder();

            try {
                this.writePayload(payload, w2, event);
            } catch (Exception var5) {
            }

            return payload.toString();
        }

        protected void writePayload(StringBuilder builder, StringWriter stringWriter, LogEvent event) throws Exception {
            StringBuffer buffer = stringWriter.getBuffer();
            if (buffer.length() > this.lim) {
                builder.append(buffer.subSequence(0, this.lim));
                event.setTruncated(true);
            } else {
                builder.append(buffer);
                event.setTruncated(false);
            }

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

    private void evaluateResponseMessage(Message message , String payload) {
        MDC.remove(LoggingConstants.ERROR_CODE);
        MDC.remove(LoggingConstants.ERROR_DESCRIPTION);
        Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
        if (Objects.nonNull(responseCode) && (responseCode.intValue() <200 || responseCode.intValue()  > 299)) {
            if (Objects.nonNull(payload)) {
                String  errorCode = obtainErrorCode(message,payload);
                String errorDescription = errorCodeHelper.findValueIntoJsonMessage(LoggingConstants.ERROR_DESCRIPTION,payload);
                if (Objects.nonNull(errorCode) && Objects.nonNull(errorDescription) ) {
                    MDC.put(LoggingConstants.ERROR_CODE,errorCode);
                    MDC.put(LoggingConstants.ERROR_DESCRIPTION,errorDescription);
                }
            }
        }
    }

    private void setTypeInterfaceToMDC(Message message) {
        String integrationType =  (String) message.getExchange().get(LoggingConstants.INTEGRATION_TYPE);
        if (Objects.nonNull(integrationType)) {
            MDC.put(LoggingConstants.INTEGRATION_TYPE, integrationType);
        }
    }

    private String obtainErrorCode(Message message, String payload) {
        String errorCode =   errorCodeHelper.findValueIntoJsonMessage(LoggingConstants.ERROR_CODE,payload);
        Integer responseCode = (Integer) message.get(Message.RESPONSE_CODE);
        if (Objects.isNull(errorCode) || errorCode.isEmpty()) {
            if (responseCode.intValue() == 400) {
                errorCode = "BusinessError";
            } else if (responseCode.intValue() == 404) {
                errorCode = "ConnectionError";
            } else if ((responseCode.intValue() == 500) || (responseCode.intValue() == 503)) {
                errorCode = "InternalError";
            } else if (responseCode.intValue() == 401) {
                errorCode = "AuthError";
            }
        }
        return errorCode;
    }

}