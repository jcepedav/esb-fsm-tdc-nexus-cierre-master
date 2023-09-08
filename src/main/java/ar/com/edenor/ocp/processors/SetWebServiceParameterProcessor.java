package ar.com.edenor.ocp.processors;

import ar.com.edenor.ocp.api.ApiRestRequest;
import ar.com.edenor.ocp.exceptions.BusinessException;
import ar.com.edenor.ocp.mapper.CierreTdcInsertRequestMapper;
import com.prometium.idms.interfaces.fsm.gen.cerrar.CerrarTdCRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;


@Slf4j
public class SetWebServiceParameterProcessor extends BaseProcessor {

    @Autowired
    private CierreTdcInsertRequestMapper cierreTedcCierreTdcInsertRequestMapper;

    @Override
    public void process(Exchange exchange)  {
        ApiRestRequest request = exchange.getIn().getBody(ApiRestRequest.class);
        if (!Objects.isNull(request)) {
          CerrarTdCRequest insertRequest = cierreTedcCierreTdcInsertRequestMapper.toCerrarTdC(request);
            exchange.getOut().setBody(insertRequest);
            exchange.getOut().setHeader("operationName","CerrarTdC");
        } else {
            throw new BusinessException("Mensaje vacio");
        }
    }
}
