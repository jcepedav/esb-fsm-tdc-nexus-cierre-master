package ar.com.edenor.ocp.mapper;

import ar.com.edenor.ocp.api.ApiRestResponse;
import com.prometium.idms.interfaces.fsm.gen.cerrar.CerrarTdCResponse;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface CierreTdcInsertResponseMapper {

    ApiRestResponse toCerrarTareaResponse(CerrarTdCResponse workOrderInsertResponse);
}

