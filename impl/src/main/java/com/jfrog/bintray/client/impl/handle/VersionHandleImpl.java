package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.handle.PackageHandle;
import com.jfrog.bintray.client.api.handle.VersionHandle;
import com.jfrog.bintray.client.api.model.Version;
import com.jfrog.bintray.client.impl.model.VersionImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dan Feldman
 */
class VersionHandleImpl implements VersionHandle {
    private static final Logger log = LoggerFactory.getLogger(VersionHandleImpl.class);
    private static final String GPG_SIGN_HEADER = "X-GPG-PASSPHRASE";
    private BintrayImpl bintrayHandle;
    private String name;
    private PackageHandle packageHandle;


    public VersionHandleImpl(BintrayImpl bintrayHandle, PackageHandle packageHandle, String versionName) {
        this.bintrayHandle = bintrayHandle;
        this.packageHandle = packageHandle;
        this.name = versionName;
    }

    @Override
    public PackageHandle pkg() {
        return packageHandle;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Version get() throws IOException, BintrayCallException {
        HttpResponse response = bintrayHandle.get(getVersionUri(), null);
        VersionDetails versionDetails;
        InputStream jsonContentStream = response.getEntity().getContent();
        ObjectMapper mapper = ObjectMapperHelper.objectMapper;
        try {
            versionDetails = mapper.readValue(jsonContentStream, VersionDetails.class);
        } catch (IOException e) {
            log.error("Can't process the json file: " + e.getMessage());
            throw e;
        }
        return new VersionImpl(versionDetails);
    }

    @Override
    public VersionHandle update(VersionDetails versionDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = VersionImpl.getCreateUpdateJson(versionDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getVersionUri(), headers, IOUtils.toInputStream(jsonContent));

        return this.updateAttributes(versionDetails);
    }

    @Override
    public VersionHandle delete() throws BintrayCallException {
        bintrayHandle.delete(getVersionUri(), null);
        return this;
    }

    @Override
    public boolean exists() throws BintrayCallException {
        try {
            bintrayHandle.head(getVersionUri(), null);
        } catch (BintrayCallException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public VersionHandle setAttributes(VersionDetails versionDetails) throws IOException, BintrayCallException {
        return setAttributes(versionDetails.getAttributes());
    }

    @Override
    public VersionHandle setAttributes(List<Attribute> attributes) throws IOException, BintrayCallException {
        if (attributes == null) {
            return this;
        }
        Map<String, String> headers = new HashMap<>();
        String jsonContent = Attribute.getJsonFromAttributeList(attributes);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.post(getVersionUri() + "/attributes", headers, IOUtils.toInputStream(jsonContent));
        return this;
    }

    @Override
    public VersionHandle updateAttributes(VersionDetails versionDetails) throws IOException, BintrayCallException {
        return updateAttributes(versionDetails.getAttributes());
    }

    @Override
    public VersionHandle updateAttributes(List<Attribute> attributes) throws IOException, BintrayCallException {
        if (attributes == null) {
            return this;
        }
        Map<String, String> headers = new HashMap<>();
        String jsonContent = Attribute.getJsonFromAttributeList(attributes);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getVersionUri() + "/attributes", headers, IOUtils.toInputStream(jsonContent));
        return this;
    }

    @Override
    public VersionHandle upload(Map<String, InputStream> content) throws BintrayCallException {
        Map<String, InputStream> uriConvertedContent = new HashMap<>();
        for (String path : content.keySet()) {
            uriConvertedContent.put(getCurrentVersionContentUri() + path, content.get(path));
        }

        bintrayHandle.putBinary(uriConvertedContent, null);
        return this;
    }

    @Override
    public VersionHandle upload(String path, InputStream content) throws BintrayCallException {
        bintrayHandle.putBinary(getCurrentVersionContentUri() + path, null, content);
        return this;
    }


    @Override
    public VersionHandle publish() throws BintrayCallException {
        bintrayHandle.post(getCurrentVersionContentUri() + "/publish", null);
        return this;
    }

    @Override
    public VersionHandle discard() throws BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        BintrayImpl.addContentTypeJsonHeader(headers);
        String discard = "{\n\"discard\":true\n}";
        bintrayHandle.post(getCurrentVersionContentUri() + "/publish", headers, IOUtils.toInputStream(discard));
        return this;
    }


    @Override
    public VersionHandle sign() throws BintrayCallException {
        return sign(null);
    }

    @Override
    public VersionHandle sign(String passphrase) throws BintrayCallException {
        Map<String, String> headers = null;
        if ((passphrase == null) || passphrase.equals("")) {
            headers = new HashMap<>();
            headers.put(GPG_SIGN_HEADER, passphrase);
        }
        bintrayHandle.post(getCurrentVersionGpgUri(), headers);
        return null;
    }

    /**
     * @return packages/$owner/$repo/$package/versions/$version/
     */
    @Override
    public String getVersionUri() {
        return "packages" + getCurrentVersionFullyQualifiedUri();
    }

    /**
     * @return gpg/$owner/$repo/$package/versions/$version/
     */
    public String getCurrentVersionGpgUri() {
        return "gpg" + getCurrentVersionFullyQualifiedUri();
    }

    /**
     * @return content/$owner/$repo/$package/$version/
     */
    public String getCurrentVersionContentUri() {
        return String.format("content/%s/%s/%s/%s/", packageHandle.repository().owner().name(),
                packageHandle.repository().name(), packageHandle.name(), name);
    }

    /**
     * @return $owner/$repo/$package/versions/$version/
     */
    private String getCurrentVersionFullyQualifiedUri() {
        return String.format("/%s/%s/%s/versions/%s/", packageHandle.repository().owner().name(),
                packageHandle.repository().name(), packageHandle.name(), name);
    }

    private VersionHandle upload(List<File> content, boolean recursive) {
        // TODO: implement upload of files
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private VersionHandle upload(File directory, boolean recursive) {
        // TODO: implement upload of directories
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
