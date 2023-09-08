package ar.com.edenor.ocp.logging;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.time.StopWatch;

@Data
@Builder
public class LogContainerInfo {
    StopWatch watch;
    String endpointAddress;
}
