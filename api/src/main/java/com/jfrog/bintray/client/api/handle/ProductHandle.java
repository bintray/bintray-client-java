package com.jfrog.bintray.client.api.handle;

import com.jfrog.bintray.client.api.BintrayCallException;
import com.jfrog.bintray.client.api.details.ProductDetails;
import com.jfrog.bintray.client.api.model.Product;

import java.io.IOException;
import java.util.List;

/**
 * @author Dan Feldman
 */
public interface ProductHandle extends Handle<Product> {

    String name();

    SubjectHandle owner();

    Product get() throws IOException, BintrayCallException;

    ProductHandle update(ProductDetails productDetails) throws IOException, BintrayCallException;

    ProductHandle addPackages(List<String> packages) throws IOException, BintrayCallException;

    ProductHandle delete() throws BintrayCallException;

    boolean exists() throws BintrayCallException;
}