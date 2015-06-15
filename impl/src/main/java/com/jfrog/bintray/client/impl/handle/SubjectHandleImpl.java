package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.details.SubjectDetails;
import com.jfrog.bintray.client.api.handle.RepositoryHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.model.Subject;
import com.jfrog.bintray.client.impl.model.RepositoryImpl;
import com.jfrog.bintray.client.impl.model.SubjectImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_REPOS;
import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_USERS;

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
    public RepositoryHandle createRepo(RepositoryDetails repoDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = RepositoryImpl.getCreateJson(repoDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.post(getRepoUri(repoDetails), headers, IOUtils.toInputStream(jsonContent));
        return new RepositoryHandleImpl(bintrayHandle, this, repoDetails.getName());
    }

    @Override
    public Subject get() throws IOException, BintrayCallException {
        HttpResponse response = bintrayHandle.get(API_USERS + subject, null);
        SubjectDetails subjectDetails;
        InputStream jsonContentStream = response.getEntity().getContent();
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            subjectDetails = mapper.readValue(jsonContentStream, SubjectDetails.class);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return new SubjectImpl(subjectDetails);
    }

    private String getRepoUri(RepositoryDetails repoDetails) {
        return String.format(API_REPOS + "%s/%s", subject, repoDetails.getName());
    }
}
