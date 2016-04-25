package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.MultipleBintrayCallException;
import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.Attribute;
import com.jfrog.bintray.client.api.details.VersionDetails;
import com.jfrog.bintray.client.api.handle.PackageHandle;
import com.jfrog.bintray.client.api.handle.VersionHandle;
import com.jfrog.bintray.client.api.model.Version;
import com.jfrog.bintray.client.impl.model.VersionImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
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

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.*;

/**
 * @author Dan Feldman
 */
class VersionHandleImpl implements VersionHandle {
    private static final Logger log = LoggerFactory.getLogger(VersionHandleImpl.class);
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
    public Version get() throws BintrayCallException, IOException {
        HttpResponse response = bintrayHandle.get(getVersionUri(), null);
        VersionDetails versionDetails;
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            InputStream jsonContentStream = response.getEntity().getContent();
            versionDetails = mapper.readValue(jsonContentStream, VersionDetails.class);
        } catch (IOException e) {
            log.debug("{}", e);
            log.error("Can't process the json file: {}", e.getMessage());
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
        bintrayHandle.post(getVersionUri() + API_ATTR, headers, IOUtils.toInputStream(jsonContent));
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
        bintrayHandle.patch(getVersionUri() + API_ATTR, headers, IOUtils.toInputStream(jsonContent));
        return this;
    }

    @Override
    public VersionHandle upload(Map<String, InputStream> content) throws MultipleBintrayCallException {
        Map<String, InputStream> uriConvertedContent = new HashMap<>();
        for (String path : content.keySet()) {
            uriConvertedContent.put(getUploadUriWithPath(path), content.get(path));
        }

        bintrayHandle.putBinary(uriConvertedContent, null);
        return this;
    }

    @Override
    public VersionHandle upload(String path, InputStream content) throws BintrayCallException {
        bintrayHandle.putBinary(getUploadUriWithPath(path), null, content);
        return this;
    }

    public VersionHandle uploadVagrant(String path, String boxProvider,
            InputStream content) throws BintrayCallException {
        bintrayHandle.putBinary(getVagrantUploadUri(path, boxProvider), null, content);
        return this;
    }

    public VersionHandle uploadDebian(String path, String distribution, String component, String architecture,
            InputStream content) throws BintrayCallException {
        bintrayHandle.putBinary(getUploadUriWithPath(path),
                getDebianCoordinatesHeaders(distribution, component, architecture), content);
        return this;
    }

    public VersionHandle uploadDebian(String path, List<String> distributions, List<String> components,
            List<String> architectures, InputStream content) throws BintrayCallException {
        bintrayHandle.putBinary(getUploadUriWithPath(path),
                getDebianCoordinatesHeaders(distributions, components, architectures), content);
        return this;
    }

    @Override
    public VersionHandle publish() throws BintrayCallException {
        bintrayHandle.post(getCurrentVersionContentUri() + API_PUBLISH, null);
        return this;
    }

    @Override
    public VersionHandle publishSync() throws BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        BintrayImpl.addContentTypeJsonHeader(headers);
        int timeout = -1;
        String wait = "{\"publish_wait_for_secs\":" + timeout + "}";
        bintrayHandle.post(getCurrentVersionContentUri() + API_PUBLISH, headers, IOUtils.toInputStream(wait));
        return this;
    }

    @Override
    public VersionHandle discard() throws BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        BintrayImpl.addContentTypeJsonHeader(headers);
        String discard = "{\n\"discard\":true\n}";
        bintrayHandle.post(getCurrentVersionContentUri() + API_PUBLISH, headers, IOUtils.toInputStream(discard));
        return this;
    }


    @Override
    public VersionHandle sign(int fileCount) throws BintrayCallException {
        return sign(null, fileCount);
    }

    @Override
    public VersionHandle sign(String passphrase, int fileCount) throws BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        if (!(passphrase == null) && !passphrase.equals("")) {
            headers.put(GPG_SIGN_HEADER, passphrase);
        }
        bintrayHandle.sign(getCurrentVersionGpgUri(), headers, fileCount);
        return null;
    }

    /**
     * @return packages/$owner/$repo/$package/versions/$version/
     */
    @Override
    public String getVersionUri() {
        return API_PKGS + getCurrentVersionFullyQualifiedUri();
    }

    /**
     * @return gpg/$owner/$repo/$package/versions/$version/
     */
    public String getCurrentVersionGpgUri() {
        return API_GPG + getCurrentVersionFullyQualifiedUri();
    }

    /**
     * @return content/$owner/$repo/$package/$version/
     */
    public String getCurrentVersionContentUri() {
        return String.format(API_CONTENT + "%s/%s/%s/%s/", packageHandle.repository().owner().name(),
                packageHandle.repository().name(), packageHandle.name(), name);
    }

    /**
     * @return $owner/$repo/$package/versions/$version/
     */
    private String getCurrentVersionFullyQualifiedUri() {
        return String.format("%s/%s/%s/" + API_VER + "%s/", packageHandle.repository().owner().name(),
                packageHandle.repository().name(), packageHandle.name(), name);
    }

    private String getVagrantUploadUri(String path, String boxProvider) {
        return getUploadUriWithPath(path) + "?box_provider=" + boxProvider;
    }

    private String getUploadUriWithPath(String path) {
        return getCurrentVersionContentUri() + path;
    }

    private Map<String, String> getDebianCoordinatesHeaders(List<String> distributions, List<String> components,
            List<String> architectures) {
        return getDebianCoordinatesHeaders(StringUtils.join(distributions, ","), StringUtils.join(components, ","),
                StringUtils.join(architectures, ","));
    }

    private Map<String, String> getDebianCoordinatesHeaders(String distribution, String component,
            String architecture) {
        Map<String, String> coordinatesHeaders = new HashMap<>();
        coordinatesHeaders.put("X-Bintray-Debian-Distribution", distribution);
        coordinatesHeaders.put("X-Bintray-Debian-Component", component);
        coordinatesHeaders.put("X-Bintray-Debian-Architecture", architecture);
        return coordinatesHeaders;
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
