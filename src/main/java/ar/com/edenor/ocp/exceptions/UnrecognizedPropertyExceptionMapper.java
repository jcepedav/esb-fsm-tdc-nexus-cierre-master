package ar.com.edenor.ocp.exceptions;

import ar.com.edenor.ocp.logging.LogUtils;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class UnrecognizedPropertyExceptionMapper extends BaseInputExceptionMapper  implements ExceptionMapper<UnrecognizedPropertyException> {

    @Override
    public Response toResponse(UnrecognizedPropertyException unrecognizedPropertyException) {
        return toInternalResponse(unrecognizedPropertyException.getMessage());
    }

}

