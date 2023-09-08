package ar.com.edenor.ocp.validation;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ValidationError {
    private String attribute;
    private String mensaje;
    private int httpCode;
	public ValidationError(String attribute, String mensaje, int httpCode) {
		super();
		this.attribute = attribute;
		this.mensaje = mensaje;
		this.httpCode = httpCode;
	}

}
