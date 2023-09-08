package ar.com.edenor.ocp.idgenerator;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;


@Component
public class RequestIdGeneratorBean {
    final ThreadLocal<String> copyOnThreadLocal = new ThreadLocal();

    @Named("requestId")
    public String getId(String sourceSystem) {
        String value = copyOnThreadLocal.get();
        if (Objects.isNull(value)) {
            value = UUID.randomUUID().toString() + "-" + sourceSystem;
            copyOnThreadLocal.set(value);
        }
        return value;
    }

    public void removeId() {
        String value = copyOnThreadLocal.get();
        if (Objects.nonNull(value)) {
            copyOnThreadLocal.remove();
        }
    }


}