package ar.com.edenor.ocp.processors;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


@Slf4j
public class DebugProcessor implements Processor {
	

	@Override
	public void process(Exchange exchange)  {
		log.info("DebugProcessor ");

	}

}
