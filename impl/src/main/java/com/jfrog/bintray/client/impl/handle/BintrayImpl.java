package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.MultipleBintrayCallException;
import com.jfrog.bintray.client.api.handle.*;
import com.jfrog.bintray.client.impl.BintrayClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author Dan Feldman
 */
public class BintrayImpl implements Bintray {
    private static final Logger log = LoggerFactory.getLogger(BintrayImpl.class);
    ExecutorService executorService;
    private CloseableHttpClient client;
    private ResponseHandler<HttpResponse> responseHandler = new BintrayResponseHandler();
    private String baseUrl;


    public BintrayImpl(CloseableHttpClient client, String baseUrl, int threadPoolSize) {
        this.client = client;
        this.baseUrl = (baseUrl == null || baseUrl.isEmpty()) ? BintrayClient.BINTRAY_API_URL : baseUrl;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    static public void addContentTypeJsonHeader(Map<String, String> headers) {
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    }

    static public void addContentTypeBinaryHeader(Map<String, String> headers) {
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.DEFAULT_BINARY.getMimeType());
    }

    private static boolean statusNotOk(int statusCode) {
        return statusCode != HttpStatus.SC_OK
                && statusCode != HttpStatus.SC_CREATED
                && statusCode != HttpStatus.SC_ACCEPTED;
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

    /**
     * Executes a sign request using the ExecutorService to avoid timing out
     *
     * @throws BintrayCallException
     */
    public HttpResponse sign(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpPost signRequest = new HttpPost(createUrl(uri));
        setHeaders(signRequest, headers);
        RequestRunner runner = new RequestRunner(signRequest, client, responseHandler);
        Future<String> signResponse = executorService.submit(runner);

        try {
            signResponse.get();
        } catch (Exception e) {
            BintrayCallException bce;
            if (e.getCause() instanceof BintrayCallException) {
                bce = (BintrayCallException) e.getCause();
            } else {
                bce = new BintrayCallException(400, e.getMessage(), (e.getCause() == null) ? "" : e.getCause().getMessage());
            }
            log.error(bce.toString());
            log.debug("{}", e);
            throw bce;
        }

        //Return ok
        String entity = "Signing the version was successful";
        HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(
                new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_CREATED, new HttpClientContext());
        response.setEntity(new StringEntity(entity, Charset.forName("UTF-8")));
        return response;
    }

    public HttpResponse post(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpPost postRequest = new HttpPost(createUrl(uri));
        return setHeadersAndExecute(postRequest, headers);
    }

    public HttpResponse post(String uri, Map<String, String> headers, InputStream elementInputStream) throws BintrayCallException {
        HttpPost postRequest = new HttpPost(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        postRequest.setEntity(requestEntity);
        return setHeadersAndExecute(postRequest, headers);
    }

    public HttpResponse patch(String uri, Map<String, String> headers, InputStream elementInputStream) throws BintrayCallException {
        HttpPatch patchRequest = new HttpPatch(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        patchRequest.setEntity(requestEntity);
        return setHeadersAndExecute(patchRequest, headers);
    }

    public HttpResponse delete(String uri, Map<String, String> headers) throws BintrayCallException {
        HttpDelete deleteRequest = new HttpDelete(createUrl(uri));
        return setHeadersAndExecute(deleteRequest, headers);
    }

    public HttpResponse putBinary(String uri, Map<String, String> headers, InputStream elementInputStream) throws BintrayCallException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        addContentTypeBinaryHeader(headers);
        return put(uri, headers, elementInputStream);
    }

    public HttpResponse putBinary(Map<String, InputStream> uriAndStreamMap, Map<String, String> headers) throws MultipleBintrayCallException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        addContentTypeBinaryHeader(headers);
        return put(uriAndStreamMap, headers);
    }

    public HttpResponse put(String uri, Map<String, String> headers, InputStream elementInputStream) throws BintrayCallException {
        HttpPut putRequest = new HttpPut(createUrl(uri));
        HttpEntity requestEntity = new InputStreamEntity(elementInputStream);
        putRequest.setEntity(requestEntity);
        return setHeadersAndExecute(putRequest, headers);
    }

    public HttpResponse put(Map<String, InputStream> uriAndStreamMap, Map<String, String> headers) throws MultipleBintrayCallException {
        List<HttpPut> requests = new ArrayList<>();
        for (String uri : uriAndStreamMap.keySet()) {
            HttpPut putRequest = new HttpPut(createUrl(uri));
            HttpEntity requestEntity = new InputStreamEntity(uriAndStreamMap.get(uri));
            putRequest.setEntity(requestEntity);
            setHeaders(putRequest, headers);
            requests.add(putRequest);
        }
        return put(requests);
    }

    /**
     * Concurrently executes a list of {@link HttpPut} requests, which are not handled by the default response handler
     * to avoid any BintrayCallExceptions being thrown before all requests have executed.
     *
     * @param requests requests to execute
     * @return A list of all errors thrown while performing the requests or empty list if all requests finished OK
     */
    private HttpResponse put(List<HttpPut> requests) throws MultipleBintrayCallException {
        List<RequestRunner> runners = new ArrayList<>();
        List<Future<String>> executions = new ArrayList<>();
        List<BintrayCallException> errors = new ArrayList<>();
        for (HttpPut request : requests) {
            RequestRunner runner = new RequestRunner(request, client, responseHandler);
            runners.add(runner);
        }
        try {
            executions = executorService.invokeAll(runners, 10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            BintrayCallException bce = new BintrayCallException(400, e.getMessage(), (e.getCause() == null) ? "" : e.getCause().getMessage());
            log.error(bce.toString());
            log.debug("{}", e);
            errors.add(bce);
        }

        //Wait until all executions are done
        while (!executions.isEmpty()) {
            for (Iterator<Future<String>> executionIter = executions.iterator(); executionIter.hasNext(); ) {
                Future<String> execution = executionIter.next();
                if (execution.isDone()) {
                    try {
                        execution.get();
                    } catch (Exception e) {
                        BintrayCallException bce;
                        if (e.getCause() instanceof BintrayCallException) {
                            bce = (BintrayCallException) e.getCause();
                        } else {
                            bce = new BintrayCallException(400, e.getMessage(), (e.getCause() == null) ? "" : e.getCause().getMessage());
                        }
                        log.error(bce.toString());
                        log.debug("{}", e.getMessage(), e);
                        errors.add(bce);
                    } finally {
                        executionIter.remove();     //Remove completed execution from iteration
                    }
                }
            }
        }

        //Return ok or throw errors
        if (errors.isEmpty()) {
            String entity = "Operation Successful";
            HttpResponse response = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(
                    new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_CREATED, new HttpClientContext());
            response.setEntity(new StringEntity(entity, Charset.forName("UTF-8")));
            return response;
        } else {
            throw new MultipleBintrayCallException(errors);
        }
    }

    private String createUrl(String uri) {
        return baseUrl + "/" + uri;
    }

    private void setHeaders(HttpUriRequest request, Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            for (String header : headers.keySet()) {
                request.setHeader(header, headers.get(header));
            }
        }
    }

    private HttpResponse setHeadersAndExecute(HttpUriRequest request, Map<String, String> headers) throws BintrayCallException {
        setHeaders(request, headers);
        return execute(request, null);
    }

    private HttpResponse execute(HttpUriRequest request, HttpClientContext context) throws BintrayCallException {
        try {
            if (context != null) {
                return client.execute(request, responseHandler, context);
            } else {
                return client.execute(request, responseHandler);
            }
        } catch (BintrayCallException bce) {
            log.debug("{}", bce);
            throw bce;
        } catch (IOException ioe) {
            //Underlying IOException form the client
            String underlyingCause = (ioe.getCause() == null) ? "" : ioe.getCause().getMessage();
            log.debug("{}", ioe);
            throw new BintrayCallException(400, ioe.getMessage(), underlyingCause);
        }
    }

    /**
     * A callable that executes a single put request, returns a String containing an error or '200' if successful
     */
    private static class RequestRunner implements Callable<String> {

        private final HttpRequestBase request;
        private final CloseableHttpClient client;
        private final HttpClientContext context;
        private final ResponseHandler<HttpResponse> responseHandler;

        public RequestRunner(HttpRequestBase request, CloseableHttpClient client, ResponseHandler<HttpResponse> responseHandler) {
            this.request = request;
            this.client = client;
            this.context = HttpClientContext.create();
            this.responseHandler = responseHandler;
        }

        @Override
        public String call() throws BintrayCallException {
            StringBuilder errorResultBuilder;
            if (request instanceof HttpPut) {
                String pushPath = request.getURI().getPath().substring(9); //Substring cuts the '/content/' part from the URI
                log.info("Pushing " + pushPath);
                errorResultBuilder = new StringBuilder(" Pushing " + pushPath + " failed: ");
            } else {
                errorResultBuilder = new StringBuilder(request.getMethod() + " " + request.getURI().getPath() + " failed:");
            }
            HttpResponse response;
            try {
                response = client.execute(request, responseHandler, context);
            } catch (BintrayCallException bce) {
                log.debug("{}", bce.getMessage(), bce);
                errorResultBuilder.append(bce.getMessage());
                bce.setMessage(errorResultBuilder.toString());
                throw bce;
            } catch (IOException ioe) {
                log.debug("IOException occured: '{}'", ioe.getMessage(), ioe);
                String cause = (ioe.getCause() == null) ? ((ioe.getMessage() != null && !ioe.getMessage().equals("")) ? ioe.getMessage() : ioe.toString())
                        : " : " + ((ioe.getCause().getMessage() != null && !ioe.getCause().getMessage().equals("")) ? ioe.getCause().getMessage() : ioe.getCause().toString());
                errorResultBuilder.append(ioe.getMessage()).append(cause);
                throw new BintrayCallException(HttpStatus.SC_BAD_REQUEST, ioe.getMessage(), errorResultBuilder.toString());
            } finally {
                request.releaseConnection();
            }
            if (statusNotOk(response.getStatusLine().getStatusCode())) {
                BintrayCallException bce = new BintrayCallException(response);
                errorResultBuilder.append(bce.getMessage());
                bce.setMessage(errorResultBuilder.toString());
                throw bce;
            }
            return String.valueOf(response.getStatusLine().getStatusCode());
        }
    }

    /**
     * gets responses from the underlying HttpClient and closes them (so you don't have to) the response body is
     * buffered in an intermediary byte array.
     * Will throw a {@link BintrayCallException} if the request failed.
     */
    private class BintrayResponseHandler implements ResponseHandler<HttpResponse> {

        @Override
        public HttpResponse handleResponse(HttpResponse response) throws BintrayCallException {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusNotOk(statusCode)) {
                BintrayCallException bce = new BintrayCallException(response);

                //We're using CloseableHttpClient so it's ok
                HttpClientUtils.closeQuietly((CloseableHttpResponse) response);
                throw bce;
            }

            //Response entity might be null, 500 and 405 also give the html itself so skip it
            String entity = "";
            if (response.getEntity() != null && statusCode != 500 && statusCode != 405) {
                try {
                    entity = IOUtils.toString(response.getEntity().getContent());
                } catch (IOException | NullPointerException e) {
                    //Null entity - Ignore
                } finally {
                    HttpClientUtils.closeQuietly((CloseableHttpResponse) response);
                }
            }

            HttpResponse newResponse = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(response.getStatusLine(), new HttpClientContext());
            newResponse.setEntity(new StringEntity(entity, Charset.forName("UTF-8")));
            newResponse.setHeaders(response.getAllHeaders());
            return newResponse;
        }
    }
}
