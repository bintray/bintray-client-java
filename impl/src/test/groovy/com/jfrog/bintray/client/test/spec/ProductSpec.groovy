package com.jfrog.bintray.client.test.spec

import com.jfrog.bintray.client.api.BintrayCallException
import com.jfrog.bintray.client.api.details.PackageDetails
import com.jfrog.bintray.client.api.details.ProductDetails
import com.jfrog.bintray.client.api.details.RepositoryDetails
import com.jfrog.bintray.client.api.handle.ProductHandle
import com.jfrog.bintray.client.api.handle.RepositoryHandle
import com.jfrog.bintray.client.impl.model.ProductImpl
import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.codehaus.jackson.map.ObjectMapper
import spock.lang.IgnoreIf
import spock.lang.Specification

import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_PRODUCTS
import static com.jfrog.bintray.client.api.BintrayClientConstatnts.API_REPOS
import static com.jfrog.bintray.client.test.BintraySpecSuite.*

/**
 * @author Dan Feldman
 */
class ProductSpec extends Specification {

    @IgnoreIf({ !testOrgDefined() })
    def 'Product creation using ProductDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        ProductDetails productDetails = mapper.readValue(productJson, ProductDetails.class)

        when:
        JsonSlurper slurper = new JsonSlurper()
        ProductHandle productHandle = bintray.subject(connectionProperties.org).createProduct(productDetails)
        ProductImpl locallyCreated = new ProductImpl(productDetails);

        ProductImpl product = productHandle.get() as ProductImpl
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/" + API_PRODUCTS + connectionProperties.org + "/" + TEST_PRODUCT_NAME, null).getEntity().getContent()))

        then:
        //ProductImpl
        locallyCreated.getName().equals(product.getName())
        locallyCreated.getDescription().equals(product.getDescription())
        locallyCreated.getOwner().equals(product.getOwner())
        product.getPackages().isEmpty()
        locallyCreated.getVcsUrl().equals(product.getVcsUrl())
        locallyCreated.getWebsiteUrl().equals(product.getWebsiteUrl())

        and:
        //jsons
        TEST_PRODUCT_NAME.equals(directJson.name)
        productDetails.getDescription().equals(directJson.desc)
        productDetails.getOwner().equals(directJson.owner)
        productDetails.getVcsUrl().equals(directJson.vcs_url)
        productDetails.getWebsiteUrl().equals(directJson.website_url)
    }

    @IgnoreIf({ !testOrgDefined() })
    def 'Product update using ProductDetails'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        ProductDetails productDetails = mapper.readValue(productJson, ProductDetails.class)
        RepositoryDetails repositoryDetails = mapper.readValue(repoJson, RepositoryDetails.class)
        repositoryDetails.setPremium(true)
        PackageDetails packageDetails = mapper.readValue(pkgJson, PackageDetails.class)
        ProductHandle productHandle = bintray.subject(connectionProperties.org).createProduct(productDetails)
        RepositoryHandle repoHandle = bintray.subject(connectionProperties.org).createRepo(repositoryDetails)
        repoHandle.createPkg(packageDetails)

        productDetails.setDescription("new Description")
        List<String> pkgs = new ArrayList<String>();
        pkgs.add(repoHandle.name() + "/" + packageDetails.name)
        productDetails.setPackages(pkgs)

        when:
        productHandle.update(productDetails)
        JsonSlurper slurper = new JsonSlurper()
        def directJson = slurper.parseText(IOUtils.toString(restClient.get("/" + API_PRODUCTS + connectionProperties.org + "/" + TEST_PRODUCT_NAME, null).getEntity().getContent()))

        then:
        productDetails.getName().equals(directJson.name)
        productDetails.getDescription().equals(directJson.desc)
        productDetails.getPackages().equals(directJson.packages)
    }

    @IgnoreIf({ !testOrgDefined() })
    def 'Delete product'() {
        setup:
        ObjectMapper mapper = new ObjectMapper()
        ProductDetails productDetails = mapper.readValue(productJson, ProductDetails.class)
        ProductHandle productHandle = bintray.subject(connectionProperties.org).createProduct(productDetails)

        when:
        bintray.subject(connectionProperties.org).product(productHandle.name()).exists()
        bintray.subject(connectionProperties.org).product((productHandle.name())).delete()

        then:
        !bintray.subject(connectionProperties.org).product((productHandle.name())).exists()
    }

    def cleanup() {
        String delProduct = "/" + API_PRODUCTS + connectionProperties.org + "/" + TEST_PRODUCT_NAME
        try {
            restClient.delete(delProduct, null)
        } catch (BintrayCallException bce) {
            if (bce.getStatusCode() != 404) {
                System.err.println("cleanup: " + bce)
            }
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
        String delRepo = "/" + API_REPOS + connectionProperties.org + "/" + REPO_CREATE_NAME
        try {
            restClient.delete(delRepo, null)
        } catch (BintrayCallException bce) {
            if (bce.getStatusCode() != 404) {
                System.err.println("cleanup: " + bce)
            }
        } catch (Exception e) {
            System.err.println("cleanup: " + e)
        }
    }

    public static boolean testOrgDefined() {
        def tempConnProps = new Properties()
        def streamFromProperties = this.class.getResourceAsStream('/bintray-client.properties')
        if (streamFromProperties) {
            streamFromProperties.withStream {
                tempConnProps.load(it)
            }
        }
        def orgFromEnv = System.getenv('BINTRAY_ORG')
        if (orgFromEnv) {
            tempConnProps.org = orgFromEnv
        }
        tempConnProps.org != null;
    }
}