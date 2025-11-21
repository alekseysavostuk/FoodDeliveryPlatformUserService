package v1.foodDeliveryPlatform.rest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import v1.foodDeliveryPlatform.exception.EmailNotConfirmedException;
import v1.foodDeliveryPlatform.exception.ExceptionBody;
import v1.foodDeliveryPlatform.exception.ResourceNotFoundException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdviceControllerTest {

    private AdviceController adviceController;

    @BeforeEach
    void setUp() {
        adviceController = new AdviceController();
    }

    @Test
    void handleResourceNotFound_ShouldReturnNotFoundStatus() {

        String errorMessage = "User not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        ExceptionBody result = adviceController.handleResourceNotFound(exception);

        assertNotNull(result);
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleEmailNotConfirmed_ShouldReturnForbiddenStatus() {

        String errorMessage = "Please confirm your email";
        EmailNotConfirmedException exception = new EmailNotConfirmedException(errorMessage);

        ExceptionBody result = adviceController.handleEmailNotConfirmed(exception);

        assertNotNull(result);
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorizedStatus() {

        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        ExceptionBody result = adviceController.handleBadCredentials(exception);

        assertNotNull(result);
        assertEquals("Invalid email or password", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleIllegalState_ShouldReturnBadRequestStatus() {

        String errorMessage = "Invalid operation state";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        ExceptionBody result = adviceController.handleIllegalState(exception);

        assertNotNull(result);
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleAccessDenied_CustomAccessDeniedException_ShouldReturnForbiddenStatus() {

        ExceptionBody result = adviceController.handleAccessDenied();

        assertNotNull(result);
        assertEquals("Access denied", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleAccessDenied_SpringAccessDeniedException_ShouldReturnForbiddenStatus() {

        ExceptionBody result = adviceController.handleAccessDenied();

        assertNotNull(result);
        assertEquals("Access denied", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequestWithValidationErrors() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class); // ДОБАВЬТЕ ЭТУ СТРОКУ
        FieldError fieldError1 = new FieldError("userDto", "email", "Email must be valid");
        FieldError fieldError2 = new FieldError("userDto", "password", "Password must not be empty");

        when(exception.getBindingResult()).thenReturn(bindingResult); // ДОБАВЬТЕ ЭТУ СТРОКУ
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ExceptionBody result = adviceController.handleMethodArgumentNotValid(exception);

        assertNotNull(result);
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getErrors());
        assertEquals(2, result.getErrors().size());
        assertEquals("Email must be valid", result.getErrors().get("email"));
        assertEquals("Password must not be empty", result.getErrors().get("password"));
    }

    @Test
    void handleMethodArgumentNotValid_DuplicateFieldErrors_ShouldConcatenateMessages() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class); // ДОБАВЬТЕ ЭТУ СТРОКУ
        FieldError fieldError1 = new FieldError("userDto", "email", "Email must be valid");
        FieldError fieldError2 = new FieldError("userDto", "email", "Email must not be empty");

        when(exception.getBindingResult()).thenReturn(bindingResult); // ДОБАВЬТЕ ЭТУ СТРОКУ
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ExceptionBody result = adviceController.handleMethodArgumentNotValid(exception);

        assertNotNull(result);
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("Email must be valid Email must not be empty", result.getErrors().get("email"));
    }

    @Test
    void handleConstraintViolation_ShouldReturnBadRequestWithValidationErrors() {

        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = createConstraintViolation("user.email", "Email must be valid");
        ConstraintViolation<?> violation2 = createConstraintViolation("user.password", "Password must be strong");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ExceptionBody result = adviceController.handleConstraintViolation(exception);

        assertNotNull(result);
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getErrors());
        assertEquals(2, result.getErrors().size());
        assertEquals("Email must be valid", result.getErrors().get("user.email"));
        assertEquals("Password must be strong", result.getErrors().get("user.password"));
    }

    @Test
    void handleAuthentication_ShouldReturnBadRequestStatus() {

        AuthenticationServiceException exception = new AuthenticationServiceException("Authentication service unavailable");

        ExceptionBody result = adviceController.handleAuthentication(exception);

        assertNotNull(result);
        assertEquals("Authentication failed", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleException_GenericException_ShouldReturnInternalServerError() {

        Exception exception = new Exception("Database connection failed");

        ExceptionBody result = adviceController.handleException(exception);

        assertNotNull(result);
        assertEquals("Internal error", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleException_RuntimeException_ShouldReturnInternalServerError() {

        RuntimeException exception = new RuntimeException("Unexpected runtime error");

        ExceptionBody result = adviceController.handleException(exception);

        assertNotNull(result);
        assertEquals("Internal error", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleException_NullPointerException_ShouldReturnInternalServerError() {

        NullPointerException exception = new NullPointerException("Null reference encountered");

        ExceptionBody result = adviceController.handleException(exception);

        assertNotNull(result);
        assertEquals("Internal error", result.getMessage());
        assertNull(result.getErrors());
    }

    @Test
    void handleMethodArgumentNotValid_EmptyFieldErrors_ShouldReturnEmptyErrorsMap() {

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ExceptionBody result = adviceController.handleMethodArgumentNotValid(exception);

        assertNotNull(result);
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void handleConstraintViolation_EmptyViolations_ShouldReturnEmptyErrorsMap() {

        ConstraintViolationException exception = new ConstraintViolationException(Set.of());

        ExceptionBody result = adviceController.handleConstraintViolation(exception);

        assertNotNull(result);
        assertEquals("Validation failed", result.getMessage());
        assertNotNull(result.getErrors());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void exceptionBody_ConstructorAndGetters_ShouldWorkCorrectly() {

        String message = "Test error message";

        ExceptionBody exceptionBody = new ExceptionBody(message);

        assertNotNull(exceptionBody);
        assertEquals(message, exceptionBody.getMessage());
        assertNull(exceptionBody.getErrors());
    }

    @Test
    void exceptionBody_Setters_ShouldWorkCorrectly() {

        ExceptionBody exceptionBody = new ExceptionBody("Initial message");

        exceptionBody.setMessage("Updated message");
        exceptionBody.setErrors(Map.of("field", "error message"));

        assertEquals("Updated message", exceptionBody.getMessage());
        assertNotNull(exceptionBody.getErrors());
        assertEquals("error message", exceptionBody.getErrors().get("field"));
    }

    @Test
    void handleAccessDenied_MultipleCalls_ShouldReturnConsistentResponse() {

        ExceptionBody result1 = adviceController.handleAccessDenied();
        ExceptionBody result2 = adviceController.handleAccessDenied();

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals("Access denied", result1.getMessage());
        assertEquals("Access denied", result2.getMessage());
        assertNull(result1.getErrors());
        assertNull(result2.getErrors());
    }

    private ConstraintViolation<?> createConstraintViolation(String propertyPath, String message) {
        return new ConstraintViolation<>() {
            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public String getMessageTemplate() {
                return null;
            }

            @Override
            public Object getRootBean() {
                return null;
            }

            @Override
            public Class<Object> getRootBeanClass() {
                return Object.class;
            }

            @Override
            public Object getLeafBean() {
                return null;
            }

            @Override
            public Object[] getExecutableParameters() {
                return new Object[0];
            }

            @Override
            public Object getExecutableReturnValue() {
                return null;
            }

            @Override
            public Path getPropertyPath() {
                return new Path() {
                    @Override
                    public String toString() {
                        return propertyPath;
                    }

                    @Override
                    public Iterator<Node> iterator() {
                        return null;
                    }
                };
            }

            @Override
            public Object getInvalidValue() {
                return null;
            }

            @Override
            public ConstraintDescriptor<?> getConstraintDescriptor() {
                return null;
            }

            @Override
            public <U> U unwrap(Class<U> type) {
                return null;
            }
        };
    }
}

