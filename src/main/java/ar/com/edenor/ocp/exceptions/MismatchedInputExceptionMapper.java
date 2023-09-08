package ar.com.edenor.ocp.exceptions;

import ar.com.edenor.ocp.logging.LogUtils;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class MismatchedInputExceptionMapper extends BaseInputExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException mismatchedInputException) {
        return toInternalResponse(mismatchedInputException.getMessage());
    }
}