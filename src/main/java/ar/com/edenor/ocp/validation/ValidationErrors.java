package ar.com.edenor.ocp.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrors {

	public void add(String attribute, String mensaje, int httpCode) {
		validationErrors.add(
				new ValidationError(attribute,mensaje,httpCode)
		);
	}
	
	public List<ValidationError> validationErrors = new ArrayList<>();

}
