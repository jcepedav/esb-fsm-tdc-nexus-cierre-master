package ar.com.edenor.ocp.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationsUtil {

    public static String toString(Set<? extends ConstraintViolation<?>> constraintViolations) {
        return constraintViolations.stream()
                .map( cv -> cv == null ? "null" : /*cv.getPropertyPath() + ": " +*/ cv.getMessage() )
                .collect( Collectors.joining( ", " ) );
    }

    public static <T>  Set<ConstraintViolation<T>> checkEntity(final T resource) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> constraintViolations = validator.validate(resource);

/*
        if (constraintViolations.size() > 0) {
            throw new BusinessException(toString(constraintViolations));
        }
        return resource;
*/
        return constraintViolations;
    }

}



