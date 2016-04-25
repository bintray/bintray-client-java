package com.jfrog.bintray.client.impl.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.ObjectMapperHelper;
import com.jfrog.bintray.client.api.details.ProductDetails;
import com.jfrog.bintray.client.api.handle.ProductHandle;
import com.jfrog.bintray.client.api.handle.SubjectHandle;
import com.jfrog.bintray.client.api.model.Product;
import com.jfrog.bintray.client.impl.model.ProductImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PRODUCTS;

/**
 * @author Dan Feldman
 */
class ProductHandleImpl implements ProductHandle {
    private static final Logger log = LoggerFactory.getLogger(ProductHandleImpl.class);

    private BintrayImpl bintrayHandle;
    private SubjectHandleImpl owner;
    private String name;

    public ProductHandleImpl(BintrayImpl bintrayHandle, SubjectHandleImpl owner, String productName) {
        this.bintrayHandle = bintrayHandle;
        this.owner = owner;
        this.name = productName;
    }

    @Override
    public String name() {
        return name;
    }


    @Override
    public SubjectHandle owner() {
        return owner;
    }

    @Override
    public Product get() throws IOException, BintrayCallException {
        ProductDetails productDetails = getProductDetails();
        return new ProductImpl(productDetails);
    }

    @Override
    public ProductHandle update(ProductDetails productDetails) throws IOException, BintrayCallException {
        Map<String, String> headers = new HashMap<>();
        String jsonContent = ObjectMapperHelper.get().writeValueAsString(productDetails);
        BintrayImpl.addContentTypeJsonHeader(headers);
        bintrayHandle.patch(getProductUri(), headers, IOUtils.toInputStream(jsonContent));
        return this;
    }

    @Override
    public ProductHandle addPackages(List<String> packages) throws IOException, BintrayCallException {
        ProductDetails details = getProductDetails();
        packages.addAll(details.getPackages());
        details.setPackages(packages);
        update(details);
        return this;
    }

    @Override
    public ProductHandle delete() throws BintrayCallException {
        bintrayHandle.delete(getProductUri(), null);
        return this;
    }

    @Override
    public boolean exists() throws BintrayCallException {
        try {
            bintrayHandle.get(getProductUri(), null);
        } catch (BintrayCallException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            throw e;
        }
        return true;
    }

    private ProductDetails getProductDetails() throws IOException {
        HttpResponse response = bintrayHandle.get(getProductUri(), null);
        ProductDetails productDetails;
        InputStream jsonContentStream = response.getEntity().getContent();
        ObjectMapper mapper = ObjectMapperHelper.get();
        try {
            productDetails = mapper.readValue(jsonContentStream, ProductDetails.class);
        } catch (IOException e) {
            log.error("Can't parse the json file: " + e.getMessage());
            throw e;
        }
        return productDetails;
    }

    private String getProductUri() {
        return String.format(API_PRODUCTS + "%s/%s", owner.name(), name);
    }
}
