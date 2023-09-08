package ar.com.edenor.ocp.mapper;

import ar.com.edenor.ocp.api.ApiRestRequest;
import ar.com.edenor.ocp.idgenerator.RequestIdGeneratorBean;
import com.prometium.idms.interfaces.fsm.gen.cerrar.CerrarTdCRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring" , uses = RequestIdGeneratorBean.class)
public interface CierreTdcInsertRequestMapper {
    @Mapping(source="sourceSystem" ,target="requestId" ,  qualifiedByName = "requestId")
    @Mapping(target = "code", constant = "GCALL")
    @Mapping(target = "description", constant = "NOVEDADES GEOCALL")
    CerrarTdCRequest toCerrarTdC(ApiRestRequest crearTareaRequest);

}



