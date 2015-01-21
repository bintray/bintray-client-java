package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.handle.*;
import com.jfrog.bintray.client.impl.BintrayClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author Dan Feldman
 */
public class BintrayImpl implements Bintray {
    private static final Logger log = LoggerFactory.getLogger(BintrayImpl.class);
    ExecutorService executorService = Executors.newCachedThreadPool();
    private CloseableHttpClient client;
    private ResponseHandler<HttpResponse> responseHandler = new BintrayResponseHandler();
    private String baseUrl;


    public BintrayImpl(CloseableHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = (baseUrl == null || baseUrl.isEmpty()) ? BintrayClient.BINTRAY_API_URL : baseUrl;
    }

    static public void addContentTypeJsonHeader(Map<String, String> headers) {
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    static public void addContentTypeBinaryHeader(Map<String, String> headers) {
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.DEFAULT_BINARY.getMimeType());
    }

    @Override
    public SubjectHandle subject(String subject) {
        return new SubjectHandleImpl(this, subject);
    }

    @Override
    public RepositoryHandle repository(String repositoryPath) {
        // TODO: implement full path resolution that receives /subject/repo/
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    public PackageHandle pkg(String packagePath) {
        // TODO: implement full path resolution that receives /subject/repo/pkg
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    public VersionHandle version(String versionPath) {
        // TODO: implement full path resolution that receives /subject/repo/pkg/version
        throw new UnsupportedOperationException("Not yet supported");
    }

    @Override
    public void close() {
        executorService.shutdown();
        HttpClientUtils.closeQuietly(client);
    }

    public HttpResponse get(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpGet getRequest = new HttpGet(createUrl(uri));
        return setHeadersAndExecute(getRequest, headers);
    }

    public HttpResponse head(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpHead headRequest = new HttpHead(createUrl(uri));
        return setHeadersAndExecute(headRequest, headers);
    }

    public HttpResponse post(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpPost postRequest = new HttpPost(createUrl(uri));
        return setHeadersAndExecute(postRequest, headers);
    }

    public HttpResponse post(String uri, Map<String, String> headers, InputStream elementInputStream)
            throws BintrayCallException {
        HttpPost postRequest = new HttpPost(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        postRequest.setEntity(requestEntity);
        return setHeadersAndExecute(postRequest, headers);
    }

    public HttpResponse patch(String uri, Map<String, String> headers, InputStream elementInputStream)
            throws BintrayCallException {
        HttpPatch patchRequest = new HttpPatch(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        patchRequest.setEntity(requestEntity);
        return setHeadersAndExecute(patchRequest, headers);
    }

    public HttpResponse delete(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpDelete deleteRequest = new HttpDelete(createUrl(uri));
        return setHeadersAndExecute(deleteRequest, headers);
    }

    public HttpResponse putBinary(String uri, Map<String, String> headers, InputStream elementInputStream)
            throws BintrayCallException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        addContentTypeBinaryHeader(headers);
        return put(uri, headers, elementInputStream);
    }

    public HttpResponse putBinary(Map<String, InputStream> uriAndStreamMap, Map<String, String> headers)
            throws BintrayCallException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        addContentTypeBinaryHeader(headers);
        return put(uriAndStreamMap, headers);
    }

    public HttpResponse put(String uri, Map<String, String> headers, InputStream elementInputStream)
            throws BintrayCallException {
        HttpPut putRequest = new HttpPut(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        putRequest.setEntity(requestEntity);
        return setHeadersAndExecute(putRequest, headers);
    }

    public HttpResponse put(Map<String, InputStream> uriAndStreamMap, Map<String, String> headers)
            throws BintrayCallException {

        List<HttpPut> requests = new ArrayList<>();
        List<CloseableHttpResponse> responses = new ArrayList<>();
        for (String uri : uriAndStreamMap.keySet()) {
            HttpPut putRequest = new HttpPut(createUrl(uri));
            HttpEntity requestEntity = new InputStreamEntity(uriAndStreamMap.get(uri));
            putRequest.setEntity(requestEntity);
            setHeaders(putRequest, headers);
            requests.add(putRequest);
        }
        List<String> errors = new ArrayList<>();
        try {
            errors = put(requests);
        } catch (InterruptedException e) {
            String error = "Error in execution of put request: " + e.getMessage() + " , " + e.getCause().getMessage();
            log.error(error);
            log.debug("{}", e);
            errors.add(error);
        }

        //Populate a response with all errors or return OK
        int finalStatus = 400;
        if (errors.isEmpty()) {
            finalStatus = 201;
            errors.add("Operation Successful");
        }
        HttpResponse newResponse = newResponse = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(
                new ProtocolVersion("HTTP", 1, 1), finalStatus, new HttpClientContext());
        StringWriter writer = new StringWriter();
        try {
            IOUtils.writeLines(errors, "\n", writer);
            newResponse.setEntity(new StringEntity(writer.toString()));
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            log.debug("{}", ioe);
            throw new BintrayCallException(ioe.getMessage(), 500, ioe.getCause().getMessage());
        }
        if (newResponse.getStatusLine().getStatusCode() != 201) {
            throw new BintrayCallException("There were errors while executing the put requests: ", 400,
                    "\n\n" + writer.toString());
        }
        return newResponse;
    }

    /**
     * Concurrently executes a list of {@link HttpPut} requests, which are not handled by the default response handler
     * to avoid any BintrayCallExceptions being thrown before all requests have executed.
     *
     * @param requests requests to execute
     * @return A list of all errors thrown while performing the requests or empty list if all requests finished OK
     * @throws BintrayCallException
     * @throws InterruptedException
     */
    private List<String> put(List<HttpPut> requests) throws BintrayCallException, InterruptedException {
        List<PutRequestRunner> runners = new ArrayList<>();
        List<Future<HttpResponse>> executions = new ArrayList<>();
        List<HttpResponse> responses = new ArrayList<>();
        List<String> errors = new ArrayList<>();                    //Propagates all errors, if exist
        for (HttpPut request : requests) {
            PutRequestRunner runner = new PutRequestRunner(request, client, responseHandler);
            runners.add(runner);
        }
        try {
            executions = executorService.invokeAll(runners, BintrayClient.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            String error = e.getMessage() + " , " + e.getCause().getMessage();
            log.error(error);
            log.debug("{}", e);
            errors.add(error);
        }

        //Wait until all executions are done
        while (!executions.isEmpty()) {
            for (Iterator<Future<HttpResponse>> executionIter = executions.iterator(); executionIter.hasNext(); ) {
                Future<HttpResponse> execution = executionIter.next();
                if (execution.isDone()) {
                    try {
                        responses.add(execution.get());
                        executionIter.remove();                 //Iterate over still active executions only
                    } catch (ExecutionException | RuntimeException e) {
                        String error = "";
                        if (e.getCause() instanceof BintrayCallException) {
                            BintrayCallException bce = (BintrayCallException) e.getCause();
                            error = bce.getStatusCode() + " : " + bce.getMessage() + ", " + bce.getReason();
                            log.error(error);
                            log.debug("{}", bce);
                        } else {
                            if (e.getCause() != null) {
                                error = e.getMessage() + " , " + e.getCause().getMessage();
                            } else {
                                error = e.getMessage();
                            }
                            log.error(error);
                            log.debug("{}", e);
                        }
                        executionIter.remove();  //Remove failed execution
                        errors.add(error);
                    }
                }
            }
        }
        for (HttpResponse response : responses) {
            if (statusNotOk(response.getStatusLine().getStatusCode())) {
                String responseBody = "";
                try {
                    responseBody = IOUtils.toString(response.getEntity().getContent());
                } catch (IOException ioe) {
                    //null response body, nothing to do
                }
                String error = String.valueOf(response.getStatusLine().getStatusCode())
                        + " : " + response.getStatusLine().getReasonPhrase()
                        + " , " + responseBody;
                errors.add(error);
                log.error(error);
            }
        }
        return errors;
    }

    private String createUrl(String uri) {
        return baseUrl + "/" + uri;
    }

    private void setHeaders(HttpUriRequest request, Map<String, String> headers) {
        if (headers != null) {
            for (String header : headers.keySet()) {
                request.setHeader(header, headers.get(header));
            }
        }
    }

    private HttpResponse setHeadersAndExecute(HttpUriRequest request, Map<String, String> headers)
            throws BintrayCallException {
        setHeaders(request, headers);
        return execute(request);
    }

    private HttpResponse execute(HttpUriRequest request) throws BintrayCallException {
        try {
            return client.execute(request, responseHandler);
        } catch (IOException ioe) {
            if (ioe instanceof BintrayCallException) {
                throw (BintrayCallException) ioe;
            }
            //Underlying IOException form the client
            String underlyingCause = (ioe.getCause() == null) ? "" : ioe.getCause().getMessage();
            log.debug("{}", ioe);
            throw new BintrayCallException(ioe.getMessage(), -1, underlyingCause);
        }
    }

    private boolean statusNotOk(int statusCode) {
        return (statusCode != HttpStatus.SC_OK)
                && (statusCode != HttpStatus.SC_CREATED)
                && (statusCode != HttpStatus.SC_ACCEPTED);
    }

    //Currently only implemented for put requests
    private static class PutRequestRunner implements Callable<HttpResponse> {

        private final HttpPut request;
        private final CloseableHttpClient client;
        private final HttpClientContext context;
        private final ResponseHandler<HttpResponse> responseHandler;

        public PutRequestRunner(HttpPut request, CloseableHttpClient client, ResponseHandler<HttpResponse> responseHandler) {
            this.request = request;
            this.client = client;
            this.context = HttpClientContext.create();
            this.responseHandler = responseHandler;
        }

        @Override
        public HttpResponse call() throws Exception {
            log.info("Pushing " + request.getURI().getPath().substring(9));
            return client.execute(request, responseHandler, context);
        }
    }

    /**
     * gets responses from the underlying HttpClient and closes them (so you don't have to) the response body is
     * buffered in an intermediary byte array.
     * Will throw a {@link BintrayCallException} if the request failed.
     */
    private class BintrayResponseHandler implements ResponseHandler<HttpResponse> {

        @Override
        public HttpResponse handleResponse(HttpResponse response) throws IOException, BintrayCallException {
            int statusCode = response.getStatusLine().getStatusCode();

            //Response entity might be null, 500 and 405 also give the html itself so skip it
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (response.getEntity() != null && response.getEntity().getContent() != null && statusCode != 500
                    && statusCode != 405) {
                try {
                    IOUtils.copy(response.getEntity().getContent(), output);
                } catch (IOException ioe) {
                    //Messed up somehow..?
                }
            }

            if (statusNotOk(statusCode)) {
                BintrayCallException ex = new BintrayCallException(output.toString(), statusCode,
                        response.getStatusLine().getReasonPhrase());

                //We're using CloseableHttpClient so it's ok
                HttpClientUtils.closeQuietly((CloseableHttpResponse) response);
                throw ex;
            }

            HttpResponse newResponse = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(response.getStatusLine(), new HttpClientContext());

            newResponse.setEntity(new ByteArrayEntity(output.toByteArray()));
            newResponse.setHeaders(response.getAllHeaders());
            HttpClientUtils.closeQuietly((CloseableHttpResponse) response);
            return newResponse;
        }
    }
}
