package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.SubjectDetails;
import com.jfrog.bintray.client.api.handle.RepositoryHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.model.Subject;
import com.jfrog.bintray.client.impl.model.SubjectImpl;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dan Feldman
 */
class SubjectHandleImpl implements SubjectHandle {
    private static final Logger log = LoggerFactory.getLogger(SubjectHandleImpl.class);

    private BintrayImpl bintrayHandle;
    private String subject;

    SubjectHandleImpl(BintrayImpl bintrayHandle, String subject) {
        this.bintrayHandle = bintrayHandle;
        this.subject = subject;
    }

    @Override
    public String name() {
        return subject;
    }

    @Override
    public RepositoryHandle repository(String repoName) {
        return new RepositoryHandleImpl(bintrayHandle, this, repoName);
    }

    @Override
    public Subject get() throws IOException, BintrayCallException {
        HttpResponse response = bintrayHandle.get("users/" + subject, null);
        SubjectDetails subjectDetails;
        InputStream jsonContentStream = response.getEntity().getContent();
        ObjectMapper mapper = ObjectMapperHelper.objectMapper;
        try {
            subjectDetails = mapper.readValue(jsonContentStream, SubjectDetails.class);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return new SubjectImpl(subjectDetails);
    }
}
