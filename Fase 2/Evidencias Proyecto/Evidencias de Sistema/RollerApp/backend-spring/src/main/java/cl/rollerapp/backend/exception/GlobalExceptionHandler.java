package cl.rollerapp.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorRespuesta> manejarApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorRespuesta(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Datos inválidos.");
        return ResponseEntity.badRequest().body(new ErrorRespuesta(mensaje));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonMalformado(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta("El cuerpo de la petición no es JSON válido."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorRespuesta> manejarAccesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorRespuesta("No tienes permisos para realizar esta acción."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorRespuesta> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorRespuesta("Correo o contraseña incorrectos."));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorRespuesta> manejarNoEncontrado(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorRespuesta(ex.getMessage()));
    }

    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRespuesta> manejarTipoParametro(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta("El parámetro '" + ex.getName() + "' tiene un valor inválido."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorRespuesta> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta("Falta el parámetro obligatorio '" + ex.getParameterName() + "'."));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarRestriccionValidacion(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta("Uno o más parámetros no cumplen las validaciones requeridas."));
    }

    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad en operación de base de datos");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorRespuesta("La operación entra en conflicto con datos existentes. Actualiza e intenta nuevamente."));
    }

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespuesta("Ocurrió un error inesperado en el servidor."));
    }
}
