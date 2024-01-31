package com.dapex.aog.factory;

import com.dapex.aog.dto.AOGReturn;
import com.dapex.aog.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EnableWebMvc
@ControllerAdvice
public class RestResponseHeaderFactory extends ResponseEntityExceptionHandler {

    public static ResponseEntity<?> renderResponseEntity(final Object body) {
        return new ResponseEntity<Object>(body, new HttpHeaders(), HttpStatus.OK);
    }

    public static ResponseEntity<?> renderResponseCreatedEntity(final Object body) {
        return new ResponseEntity<Object>("Success", new HttpHeaders(), HttpStatus.ACCEPTED);
    }

    private static final Logger LOGGER = LogManager.getLogger();

    @ExceptionHandler(value = {
            NullPointerException.class,
            CacheInitializationFailureException.class,
            UpdateFailureException.class,
            TablePopulateFailureException.class
    })
    private ResponseEntity<Object> httpErrorOnInternalServerError(final RuntimeException runtimeException, final WebRequest webRequest) {
        final String stackTrace = writeStackTrace(runtimeException);
        LOGGER.error(runtimeException.getMessage());
        LOGGER.error(stackTrace);
        return handleExceptionInternal(
                runtimeException,
                stackTrace,
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                webRequest
        );
    }

    @ExceptionHandler(value = {
            EmptyAOGException.class,
            OpenXML4JException.class,
            IOException.class
    })
    private ResponseEntity<Object> httpErrorOnEmptyAOGContent(final RuntimeException runtimeException, final WebRequest webRequest) {
        final String stackTrace = writeStackTrace(runtimeException);
        LOGGER.error(runtimeException.getMessage());
        LOGGER.error(stackTrace);
        return httpEmptyResponseHandler(runtimeException, webRequest, new AOGReturn());
    }

    @ExceptionHandler(value = {
            EmptyAOGListException.class
    })
    private ResponseEntity<Object> httpErrorOnEmptyAOGListContent(final RuntimeException runtimeException, final WebRequest webRequest) {
        final String stackTrace = writeStackTrace(runtimeException);
        final List<AOGReturn> list = Collections.singletonList(new AOGReturn());
        LOGGER.error(runtimeException.getMessage());
        LOGGER.error(stackTrace);
        return httpEmptyResponseHandler(runtimeException, webRequest, list);
    }

    @ExceptionHandler(value = {
            EmptyListException.class
    })
    private ResponseEntity<Object> httpErrorOnEmptyList(final RuntimeException runtimeException, final WebRequest webRequest) {
        final String stackTrace = writeStackTrace(runtimeException);
        LOGGER.error(runtimeException.getMessage());
        LOGGER.error(stackTrace);
        return httpEmptyResponseHandler(runtimeException, webRequest, new ArrayList<>());
    }

    /**
     * A Generalized No_Content Response
     *
     * @param runtimeException exception thrown
     * @param webRequest       web request sent by client
     * @param body             embedded body to be sent back to client indicate no content
     * @return a response entity with No_Content
     */
    private ResponseEntity<Object> httpEmptyResponseHandler(final RuntimeException runtimeException, final WebRequest webRequest, final Object body) {
        return handleExceptionInternal(
                runtimeException,
                body,
                new HttpHeaders(),
                HttpStatus.NO_CONTENT,
                webRequest
        );
    }

    /**
     * Print necessary stack trace since java doesn't have a "proper" getStackTrace,
     * meaning that java's getStackTrace is a complete stack trace, and we only want abbreviated ones
     *
     * @param runtimeException exception thrown below controller level
     * @return abbreviated stackTrace
     */
    private String writeStackTrace(final RuntimeException runtimeException) {
        final StringWriter stackTraceWriter = new StringWriter();
        final PrintWriter stackTracePrinter = new PrintWriter(stackTraceWriter, true);
        runtimeException.printStackTrace(stackTracePrinter);
        final String stackTrace = stackTraceWriter.getBuffer().toString();
        stackTracePrinter.close();
        stackTracePrinter.close();
        return stackTrace;
    }

}
