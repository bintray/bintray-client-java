package com.jfrog.bintray.client.api;

import com.jfrog.bintray.client.api.details.ObjectMapperHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * An exception thrown for failed operations against the Bintray api.
 *
 * @author danf
 */
public class BintrayCallException extends HttpResponseException {

    private int statusCode;
    private String reason;
    private String message;

    public BintrayCallException(int statusCode, String reason, String message) {
        super(statusCode, reason);
        this.statusCode = statusCode;
        this.reason = reason;
        this.message = message;
    }

    public BintrayCallException(HttpResponse response) {
        super(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        String message = " ";
        String entity = null;
        try {
            entity = IOUtils.toString(response.getEntity().getContent());
            ObjectMapper mapper = ObjectMapperHelper.objectMapper;
            JsonNode node = mapper.readTree(entity);
            message = node.get("message").getTextValue();
        } catch (IOException | NullPointerException e) {
            //Null entity?
            if (entity != null) {
                message = entity;
            }
        }
        this.statusCode = response.getStatusLine().getStatusCode();
        this.reason = response.getStatusLine().getReasonPhrase();
        this.message = message;
    }

    public BintrayCallException(Exception e) {
        super(HttpStatus.SC_BAD_REQUEST, e.getMessage());
        this.statusCode = HttpStatus.SC_BAD_REQUEST;
        this.reason = e.getMessage();
        this.message = (e.getCause() == null) ? " " : " : " + e.getCause().getMessage();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        this.message = newMessage;
    }

    @Override
    public String toString() {
        return statusCode + ", " + reason + " " + message;
    }

}