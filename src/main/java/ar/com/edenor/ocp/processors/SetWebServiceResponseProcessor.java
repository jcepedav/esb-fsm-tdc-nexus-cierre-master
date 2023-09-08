package ar.com.edenor.ocp.processors;

import ar.com.edenor.ocp.api.ApiRestResponse;
import ar.com.edenor.ocp.exceptions.BusinessException;
import ar.com.edenor.ocp.mapper.CierreTdcInsertResponseMapper;
import com.prometium.idms.interfaces.fsm.gen.cerrar.CerrarTdCResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.cxf.message.MessageContentsList;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Objects;


@Slf4j
public class SetWebServiceResponseProcessor extends BaseProcessor {

	@Autowired
	private CierreTdcInsertResponseMapper cierreTdcInsertResponseMapper;


	@Override
	public void process(Exchange exchange)  {
		MessageContentsList messageContentsList = exchange.getIn().getBody(MessageContentsList.class);
		if (Objects.nonNull(messageContentsList) && !messageContentsList.isEmpty() && Objects.nonNull(messageContentsList.get(0))) {
			CerrarTdCResponse cerrarTdcResponseImplemen = (CerrarTdCResponse) messageContentsList.get(0);
			ApiRestResponse crearTareaResponse = cierreTdcInsertResponseMapper.toCerrarTareaResponse(cerrarTdcResponseImplemen);
			validateResponse(crearTareaResponse);
			exchange.getOut().setBody(crearTareaResponse);
		} else {
			throw new BusinessException("Respuesta vacia desde Geocall");
		}
	}

	private void validateResponse(ApiRestResponse response) {
		if (Objects.nonNull(response.getStatus()) && response.getStatus().equals("KO")) {
			throw new BusinessException(response.getErrorCode(),response.getErrorDescription());
		}
	}
}
