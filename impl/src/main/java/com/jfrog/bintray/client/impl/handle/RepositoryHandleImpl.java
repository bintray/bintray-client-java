package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.RepositoryDetails;
import com.jfrog.bintray.client.api.handle.AttributesSearchQuery;
import com.jfrog.bintray.client.api.handle.PackageHandle;
import com.jfrog.bintray.client.api.handle.RepositoryHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Repository;
import com.jfrog.bintray.client.impl.model.PackageImpl;
import com.jfrog.bintray.client.impl.model.RepositoryImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS;
import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_REPOS;

/**
 * @author Dan Feldman
 */
class RepositoryHandleImpl implements RepositoryHandle {
    private static final Logger log = LoggerFactory.getLogger(RepositoryHandleImpl.class);

    private BintrayImpl bintrayHandle;
    private SubjectHandleImpl owner;
    private String name;
    private AttributesSearchQueryImpl searchQuery = null;

    public RepositoryHandleImpl(BintrayImpl bintrayHandle, SubjectHandleImpl owner, String repoName) {
        this.bintrayHandle = bintrayHandle;
        this.owner = owner;
        this.name = repoName;
    }

    @Override
    public SubjectHandle owner() {
        return owner;
    }

    @Override
    public Repository get() throws IOException, BintrayCallException {
        HttpResponse response = bintrayHandle.get(getRepositoryUri(), null);
        RepositoryDetails repoDetails;
        InputStream jsonContentStream = response.getEntity().getContent();
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            repoDetails = mapper.readValue(jsonContentStream, RepositoryDetails.class);
        } catch (IOException e) {
            log.error("Can't parse the json file: " + e.getMessage());
            throw e;
        }
        return new RepositoryImpl(repoDetails);
    }

    @Override
    public RepositoryHandle update(RepositoryDetails repositoryDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = RepositoryImpl.getUpdateJson(repositoryDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getRepositoryUri(), headers, IOUtils.toInputStream(jsonContent));
        return this;
    }

    @Override
    public PackageHandle pkg(String packageName) {
        return new PackageHandleImpl(bintrayHandle, this, packageName);
    }

    @Override
    public PackageHandle createPkg(PackageDetails packageDetails) throws IOException, BintrayCallException {
        String jsonContent = PackageImpl.getCreateUpdateJson(packageDetails);
        bintrayHandle.post(String.format(API_PKGS + "%s/%s", owner.name(), name), null, IOUtils.toInputStream(jsonContent));
        return new PackageHandleImpl(bintrayHandle, this, packageDetails.getName()).setAttributes(packageDetails);
    }

    @Override
    public AttributesSearchQuery searchForPackage() {
        return new AttributesSearchQueryImpl(this);
    }

    @Override
    public RepositoryHandle delete() throws BintrayCallException {
        bintrayHandle.delete(getRepositoryUri(), null);
        return this;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean exists() throws BintrayCallException {
        try {
            bintrayHandle.head(getRepositoryUri(), null);
        } catch (BintrayCallException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public String getRepositoryUri() {
        return String.format(API_REPOS + "%s/%s", owner.name(), name);
    }

    public void addQuery(AttributesSearchQueryImpl query) {
        searchQuery = query;
    }

    /**
     * Searches for packages according to the attribute search query supplied to addQuery
     *
     * @return a list of packages that were returned by Bintray's search
     * @throws IOException
     * @throws BintrayCallException
     */
    public List<Pkg> attributeSearch() throws IOException, BintrayCallException {
        ObjectMapper mapper = ObjectMapperHelper.get();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, searchQuery);
        } catch (IOException e) {
            log.error("Error writing search query to json: ", e);
            throw e;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        HttpResponse response = bintrayHandle.post("/search/attributes/" + owner.name() + "/" + name, headers,
                IOUtils.toInputStream(writer.toString()));

        List<PackageDetails> answer;
        answer = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<PackageDetails>>() {
        });

        List<Pkg> packages = new ArrayList<>();
        for (PackageDetails pkgDetails : answer) {
            packages.add(new PackageImpl(pkgDetails));
        }
        searchQuery = null;
        return packages;
    }
}
