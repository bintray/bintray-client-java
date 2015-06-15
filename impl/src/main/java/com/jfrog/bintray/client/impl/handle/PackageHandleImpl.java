package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.PackageDetails;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.handle.PackageHandle;
import com.jfrog.bintray.client.api.handle.RepositoryHandle;
import com.jfrog.bintray.client.api.handle.VersionHandle;
import com.jfrog.bintray.client.api.model.Pkg;
import com.jfrog.bintray.client.api.model.Version;
import com.jfrog.bintray.client.impl.model.PackageImpl;
import com.jfrog.bintray.client.impl.model.VersionImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PKGS;

/**
 * @author Dan Feldman
 */
class PackageHandleImpl implements PackageHandle {
    private static final Logger log = LoggerFactory.getLogger(PackageHandleImpl.class);

    private BintrayImpl bintrayHandle;
    private RepositoryHandle repositoryHandle;
    private String name;
    private AttributesSearchQueryImpl searchQuery = null;

    PackageHandleImpl(BintrayImpl bintrayHandle, RepositoryHandle repositoryHandle, String name) {
        this.bintrayHandle = bintrayHandle;
        this.repositoryHandle = repositoryHandle;
        this.name = name;
    }

    @Override
    public RepositoryHandle repository() {
        return repositoryHandle;
    }

    @Override
    public VersionHandle version(String versionName) {
        return new VersionHandleImpl(bintrayHandle, this, versionName);
    }

    @Override
    public Pkg get() throws IOException, BintrayCallException {
        HttpResponse response = bintrayHandle.get(getCurrentPackageUri(), null);
        PackageDetails pkgDetails;
        String jsonContentStream = IOUtils.toString(response.getEntity().getContent());
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            pkgDetails = mapper.readValue(jsonContentStream, PackageDetails.class);
        } catch (IOException e) {
            log.error("Can't parse the json file: " + e.getMessage());
            throw e;
        }
        return new PackageImpl(pkgDetails);
    }

    @Override
    public PackageHandle update(PackageDetails packageDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = PackageImpl.getCreateUpdateJson(packageDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getCurrentPackageUri(), headers, IOUtils.toInputStream(jsonContent));

        return this.updateAttributes(packageDetails);
    }

    @Override
    public PackageHandle delete() throws BintrayCallException {
        bintrayHandle.delete(getCurrentPackageUri(), null);
        return this;
    }

    @Override
    public boolean exists() throws BintrayCallException {
        try {
            bintrayHandle.head(getCurrentPackageUri(), null);
        } catch (BintrayCallException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public VersionHandle createVersion(VersionDetails versionDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = VersionImpl.getCreateUpdateJson(versionDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.post(getCurrentPackageUri() + "/versions", headers, IOUtils.toInputStream(jsonContent));

        return new VersionHandleImpl(bintrayHandle, this, versionDetails.getName()).setAttributes(versionDetails);
    }


    @Override
    public PackageHandle setAttributes(PackageDetails packageDetails) throws IOException, BintrayCallException {
        return setAttributes(packageDetails.getAttributes());
    }

    @Override
    public PackageHandle setAttributes(List<Attribute> attributes) throws IOException, BintrayCallException {
        if (attributes == null) {
            return this;
        }
        Map<String, String> headers = new HashMap<>();
        String jsonContent = Attribute.getJsonFromAttributeList(attributes);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.post(getCurrentPackageUri() + "/attributes", headers, IOUtils.toInputStream(jsonContent));

        return this;
    }

    @Override
    public PackageHandle updateAttributes(PackageDetails packageDetails) throws IOException, BintrayCallException {
        return updateAttributes(packageDetails.getAttributes());
    }

    @Override
    public PackageHandle updateAttributes(List<Attribute> attributes) throws IOException, BintrayCallException {
        if (attributes == null) {
            return this;
        }
        Map<String, String> headers = new HashMap<>();
        String jsonContent = Attribute.getJsonFromAttributeList(attributes);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getCurrentPackageUri() + "/attributes", headers, IOUtils.toInputStream(jsonContent));

        return this;
    }

    public void addQuery(AttributesSearchQueryImpl query) {
        searchQuery = query;
    }

    /**
     * Searches for versions according to the attribute search query supplied to addQuery
     *
     * @return a list of packages that were returned by Bintray's search
     * @throws BintrayCallException
     */
    public List<Version> attributeSearch() throws BintrayCallException {
        ObjectMapper mapper = ObjectMapperHelper.get();
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, searchQuery);
        } catch (IOException e) {
            log.error("Error writing search query to json: ", e);
            return null;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        HttpResponse response = bintrayHandle.post("/search/attributes/" + repositoryHandle.owner().name() +
                        "/" + repositoryHandle.name() + "/" + name + "/versions", headers,
                IOUtils.toInputStream(writer.toString()));

        List<VersionDetails> answer = new ArrayList<>();

        try {
            answer = mapper.readValue(response.getEntity().getContent(), new TypeReference<List<VersionDetails>>() {
            });
        } catch (IOException e) {
            log.error("Error parsing query response");
        }

        List<Version> packages = new ArrayList<>();
        for (VersionDetails verDetails : answer) {
            packages.add(new VersionImpl(verDetails));
        }
        searchQuery = null;
        return packages;
    }

    @Override
    public String getCurrentPackageUri() {
        return String.format(API_PKGS + "%s/%s/%s", repositoryHandle.owner().name(), repositoryHandle.name(), name);
    }

    @Override
    public String name() {
        return name;
    }
}
