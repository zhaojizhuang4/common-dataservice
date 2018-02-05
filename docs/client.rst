=========================================
Acumos Common Data Service Client Library
=========================================

This library provides a client for using the Common Data Service in the Acumos machine-learning platform.

Maven Dependency
----------------

The client jar is deployed to these Nexus repositories at the Linux Foundation:: 

	<repository>
		<id>snapshots</id>
		<url>https://nexus.acumos.org/content/repositories/snapshots</url> 
	</repository>
	<repository>
		<id>releases</id>
		<url>https://nexus.acumos.org/content/repositories/releases</url> 
	</repository>

Use this dependency information; please note the version number shown below might not reflect the latest release::

	<dependency>
		<groupId>org.acumos.common-dataservice</groupId>
		<artifactId>cmn-data-svc-client</artifactId>
		<version>1.x.x-SNAPSHOT</version>
	</dependency>

Building and Packaging
----------------------

Prerequisites
~~~~~~~~~~~~~

The build machine needs the following:

1. Java version 1.8
2. Maven version 3
3. Connectivity to Maven Central (for most jars)

Use maven to build and package the client jar using this command::

    mvn package

Run Prerequisites
-----------------

1. Java version 1.8
2. A running Common Data Service server.
3. A username/password combination to access the service.

Usage Example
-------------

Example code that demonstrates use of the client is shown below::

    package org.acumos.cds.client.test;

    import java.net.URL;

    import org.acumos.cds.AccessTypeCode;
    import org.acumos.cds.ArtifactTypeCode;
    import org.acumos.cds.ModelTypeCode;
    import org.acumos.cds.ToolkitTypeCode;
    import org.acumos.cds.ValidationStatusCode;
    import org.acumos.cds.client.CommonDataServiceRestClientImpl;
    import org.acumos.cds.client.ICommonDataServiceRestClient;
    import org.acumos.cds.domain.MLPArtifact;
    import org.acumos.cds.domain.MLPSolution;
    import org.acumos.cds.domain.MLPSolutionRevision;
    import org.acumos.cds.domain.MLPUser;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.web.client.HttpStatusCodeException;

    /**
     * Demonstrates use of the CDS client.
     */
    public class BasicSequenceDemo {

        private static Logger logger = LoggerFactory.getLogger(BasicSequenceDemo.class);

        private static String hostname = "localhost";
        private static final String contextPath = "/ccds";
        private static final int port = 8080;
        private static final String userName = "cds_web_user";
        private static final String password = "cds_web_pass";

        public static void main(String[] args) throws Exception {

            URL url = new URL("http", hostname, port, contextPath);
            logger.info("createClient: URL is {}", url);
            ICommonDataServiceRestClient client = CommonDataServiceRestClientImpl.getInstance(url.toString(), userName,
                    password);

            try {
                MLPUser cu = new MLPUser("user_login1", true);
                cu.setLoginHash("user_pass");
                cu.setFirstName("First Name");
                cu.setLastName("Last Name");
                cu = client.createUser(cu);
                logger.info("Created user {}", cu);

                MLPSolution cs = new MLPSolution("solution name", cu.getUserId(), true);
                cs.setValidationStatusCode(ValidationStatusCode.IP.name());
                cs.setProvider("Big Data Org");
                cs.setAccessTypeCode(AccessTypeCode.PB.name());
                cs.setModelTypeCode(ModelTypeCode.CL.name());
                cs.setToolkitTypeCode(ToolkitTypeCode.CP.name());
                cs = client.createSolution(cs);
                logger.info("Created solution {}", cs);

                MLPSolutionRevision cr = new MLPSolutionRevision(cs.getSolutionId(), "1.0R", cu.getUserId());
                cr.setDescription("Some description");
                cr = client.createSolutionRevision(cr);
                logger.info("Created solution revision {}", cr);

                MLPArtifact ca = new MLPArtifact("1.0A", ArtifactTypeCode.DI.toString(), "artifact name",
                        "http://nexus/artifact", cu.getUserId(), 1);
                ca = client.createArtifact(ca);
                logger.info("Created artifact {}", ca);

                logger.info("Adding artifact to revision");
                client.addSolutionRevisionArtifact(cs.getSolutionId(), cr.getRevisionId(), ca.getArtifactId());

                logger.info("Deleting objects");
                client.dropSolutionRevisionArtifact(cs.getSolutionId(), cr.getRevisionId(), ca.getArtifactId());
                client.deleteArtifact(ca.getArtifactId());
                client.deleteSolutionRevision(cs.getSolutionId(), cr.getRevisionId());
                client.deleteSolution(cs.getSolutionId());
                client.deleteUser(cu.getUserId());

            } catch (HttpStatusCodeException ex) {
                logger.error("basicSequenceDemo failed, server reports: {}", ex.getResponseBodyAsString());
                throw ex;
            }
        }

    }

