package com.jfrog.bintray.client.api;

/**
 * Holds Bintray REST API related constants
 *
 * @author Dan Feldman
 */
public interface BintrayClientConstatnts {

    String API_USERS = "users/";
    String API_REPOS = "repos/";
    String API_PKGS = "packages/";
    String API_VER = "versions/";
    String API_GPG = "gpg/";
    String API_CONTENT = "content/";
    String API_PRODUCTS = "products/";

    String API_PUBLISH = "/publish";
    String API_ATTR = "/attributes";

    String GPG_SIGN_HEADER = "X-GPG-PASSPHRASE";
}
