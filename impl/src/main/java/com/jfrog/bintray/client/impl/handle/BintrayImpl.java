package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.MultipleBintrayCallException;
import com.jfrog.bintray.client.api.handle.*;
import com.jfrog.bintray.client.impl.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * @author Dan Feldman
 */
public class BintrayImpl implements Bintray {
    private static final Logger log = LoggerFactory.getLogger(BintrayImpl.class);
    ExecutorService executorService;
    private CloseableHttpClient client;
    private ResponseHandler<HttpResponse> responseHandler = new BintrayResponseHandler();
    private String baseUrl;
    private int signRequestTimeoutPerFile;


    public BintrayImpl(CloseableHttpClient client, String baseUrl, int threadPoolSize, int signRequestTimeoutPerFile) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.signRequestTimeoutPerFile = signRequestTimeoutPerFile;
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
     * Executes a sign request using the ExecutorService and uses the file count to set a timeout to avoid timing out
     * on long requests
     *
     * @throws BintrayCallException
     */
    public HttpResponse sign(String uri, Map<String, String> headers, int fileCount) throws BintrayCallException {
        HttpPost signRequest = new HttpPost(createUrl(uri));
        setHeaders(signRequest, headers);
        signRequest.setConfig(RequestConfig.custom().setSocketTimeout(signRequestTimeoutPerFile * fileCount)
                .setConnectionRequestTimeout(signRequestTimeoutPerFile * fileCount)
                .setConnectTimeout(signRequestTimeoutPerFile * fileCount).build());
        RequestRunner runner = new RequestRunner(signRequest, client, responseHandler);
        Future<String> signResponse = executorService.submit(runner);
        try {
            signResponse.get();
        } catch (Exception e) {
            BintrayCallException bce;
            if (e.getCause() instanceof BintrayCallException) {
                bce = (BintrayCallException) e.getCause();
            } else {
                bce = new BintrayCallException(409, e.getMessage(), (e.getCause() == null) ? ""
                        : ", " + e.getCause().toString() + " : " + e.getCause().getMessage());
            }
            log.error(bce.toString());
            log.debug("{}", e.getMessage(), e);
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
        List<Future<String>> executions = new ArrayList<>();
        List<BintrayCallException> errors = new ArrayList<>();
        List<RequestRunner> runners = createPutRequestRunners(uriAndStreamMap, headers, errors);
        try {
            executions = executorService.invokeAll(runners);
        } catch (InterruptedException e) {
            BintrayCallException bce = new BintrayCallException(409, e.getMessage(), (e.getCause() == null) ? ""
                    : e.getCause().toString() + " : " + e.getCause().getMessage());
            log.error(bce.toString());
            log.debug("{}", e.getMessage(), e);
            errors.add(bce);
        }
        collectResults(executions, errors);

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

    private List<RequestRunner> createPutRequestRunners(Map<String, InputStream> uriAndStreamMap, Map<String, String> headers, List<BintrayCallException> errors) {
        List<RequestRunner> runners = new ArrayList<>();
        List<HttpPut> requests = new ArrayList<>();
        log.debug("Creating PUT requests and RequestRunners for execution");
        for (String apiPath : uriAndStreamMap.keySet()) {
            HttpPut putRequest;
            try {
                putRequest = new HttpPut(createUrl(apiPath));
            } catch (BintrayCallException bce) {
                errors.add(bce);
                continue;
            }
            HttpEntity requestEntity = new InputStreamEntity(uriAndStreamMap.get(apiPath));
            putRequest.setEntity(requestEntity);
            setHeaders(putRequest, headers);
            requests.add(putRequest);
        }

        for (HttpPut request : requests) {
            RequestRunner runner = new RequestRunner(request, client, responseHandler);
            runners.add(runner);
        }
        return runners;
    }

    private void collectResults(List<Future<String>> executions, List<BintrayCallException> errors) {
        //Wait until all executions are done
        while (!executions.isEmpty()) {
            log.debug("Querying execution Futures for results");
            for (Iterator<Future<String>> executionIter = executions.iterator(); executionIter.hasNext(); ) {
                Future<String> execution = executionIter.next();
                if (execution.isDone()) {
                    try {
                        String response = execution.get();
                        log.debug("Got complete execution: {}", response);
                    } catch (Exception e) {
                        BintrayCallException bce;
                        if (e.getCause() instanceof BintrayCallException) {
                            bce = (BintrayCallException) e.getCause();
                        } else {
                            bce = new BintrayCallException(400, e.getMessage(), (e.getCause() == null) ? ""
                                    : e.getCause().getMessage());
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
    }

    private String createUrl(String queryPath) throws BintrayCallException {
        log.debug("Trying to encode uri: '{}' with base url: {}", queryPath, baseUrl);
        try {
            return URIUtil.encodeQuery(baseUrl + "/" + queryPath);
        } catch (HttpException e) {
            throw new BintrayCallException(HttpStatus.SC_BAD_REQUEST, "Malformed url, request will not be sent: ",
                    e.getMessage());
        }
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
        log.debug("Executing {} request to path '{}', with headers: {}", request.getMethod(), request.getURI(),
                Arrays.toString(request.getAllHeaders()));
        try {
            if (context != null) {
                return client.execute(request, responseHandler, context);
            } else {
                return client.execute(request, responseHandler);
            }
        } catch (BintrayCallException bce) {
            log.debug("{}", bce.toString(), bce);
            throw bce;
        } catch (IOException ioe) {
            //Underlying IOException form the client
            String underlyingCause = (ioe.getCause() == null) ? "" : ioe.toString() + " : " + ioe.getCause().getMessage();
            log.debug("{}", ioe.getMessage(), ioe);
            throw new BintrayCallException(400, ioe.getClass() + " : " + ioe.getMessage(), underlyingCause);
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
            log.debug("Executing {} request to path '{}', with headers: {}", request.getMethod(), request.getURI(),
                    Arrays.toString(request.getAllHeaders()));
            StringBuilder errorResultBuilder;
            String requestPath;
            if (request instanceof HttpPut) {
                requestPath = request.getURI().getPath().substring(9); //Substring cuts the '/content/' part from the URI
                log.info("Pushing " + requestPath);
                errorResultBuilder = new StringBuilder(" Pushing " + requestPath + " failed: ");
            } else {
                requestPath = request.getURI().getPath();
                errorResultBuilder = new StringBuilder(request.getMethod() + " " + requestPath + " failed: ");
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
                log.debug("IOException occurred: '{}'", ioe.getMessage(), ioe);
                String cause = (ioe.getCause() != null) ? (", caused by: " + ioe.getCause().toString() + " : "
                        + ioe.getCause().getMessage()) : "";
                errorResultBuilder.append(ioe.toString()).append(" : ").append(ioe.getMessage()).append(cause);
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
            return request.getMethod() + " " + requestPath + ": " + String.valueOf(response.getStatusLine().getStatusCode());
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
            InputStream entity = new ByteArrayInputStream("".getBytes(Charset.forName("UTF-8")));
            if (response.getEntity() != null && statusCode != 500 && statusCode != 405) {
                try {
                    entity = IOUtils.toBufferedInputStream(response.getEntity().getContent());
                } catch (IOException | NullPointerException e) {
                    //Null entity - Ignore
                } finally {
                    HttpClientUtils.closeQuietly((CloseableHttpResponse) response);
                }
            }

            HttpResponse newResponse = DefaultHttpResponseFactory.INSTANCE.newHttpResponse(response.getStatusLine(),
                    new HttpClientContext());
            newResponse.setEntity(new InputStreamEntity(entity));
            newResponse.setHeaders(response.getAllHeaders());
            return newResponse;
        }
    }
}
