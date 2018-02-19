.. ===============LICENSE_START=======================================================
.. Acumos
.. ===================================================================================
.. Copyright (C) 2017-2018 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..  
..      http://creativecommons.org/licenses/by/4.0
..  
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

=================================
Acumos Common Data Service Server
=================================

This microservice provides common data services to components in the Acumos machine-learning platform. It is built using the Spring-Boot platform.

Supported Methods and Objects
-----------------------------

The microservice endpoints and objects are documented using Swagger. A running server documents itself at a URL like the following, but
consult the server's configuration for the exact port number (e.g., "8080") and context root (e.g., "ccds") to use::

    http://localhost:8080/ccds/swagger-ui.html


Building and Packaging
----------------------

Prerequisites
~~~~~~~~~~~~~

The build machine needs the following:

1. Java version 1.8
2. Maven version 3
3. Connectivity to Maven Central (for most jars)

Use maven to build and package the service into a single "fat" jar using this command::

    mvn package

Development and Local Testing
-----------------------------

This section provides information for developing and testing the server locally; e.g., on a personal machine.

Using an in-memory database
~~~~~~~~~~~~~~~~~~~~~~~~~~~

The default configuration for the server uses an in-memory Derby database, which is created at launch time.

Using an external database
~~~~~~~~~~~~~~~~~~~~~~~~~~

The server can be configured to use an external database as follows:

    1. Copy the local application.properties file to a new file "application-mydb.properties"
    2. Revise the new "mydb" properties file to have suitable server coordinates and credentials
    3. Ensure the newly configured database server is reachable at the expected port
    4. Check that the database tables have been created and populated
    5. Launch the test as usual, adding this extra argument to use the alternate properties file:
          -Dspring.config.name=application-mydb

Launching
~~~~~~~~~

Start the microservice for development and testing like this::

     mvn clean spring-boot:run

Alternately, launch the service from Eclipse by starting this class::

    org.acumos.cds.CdsApplication.

Production Deployment
---------------------

This section provides information for running the server in a production environment,
assuming that the application is packaged into a docker container for deployment.

Prerequisites
~~~~~~~~~~~~~

1. Java version 1.8 in the runtime environment (e.g., docker container)
2. A Mariadb or Mysql database with the required tables, instructions are shown below
3. The username/password combination to access the database
4. A valid configuration with database coordinates.

Configuring the system
~~~~~~~~~~~~~~~~~~~~~~

First the database must be created and configured using scripts in the
"db-scripts" directory:

- cmn-data-svc-user-mysql.sql: This file is a TEMPLATE can be used to
  create a Mysql/MariaDB database, to create a user, and to grant the
  user permission on the database.  The values in CAPITALS shown in
  the file must be adjusted for each use.
- cmn-data-svc-ddl-cml-mysql-N-N.sql: This has the data-definition and
  data-modeling language statements that create the tables and
  populate them.

Next, configuration parameters must be specified.  A template with
default values can be found in the top level of this project named
application.properties.template, and can be copied a file named
application.properties (but see below for the preferred method).

Details about the database configuration must be supplied in the following
required entries::

    spring.database.driver.classname=org.apache.derby.jdbc.EmbeddedDriver
    spring.datasource.url=jdbc:derby:memory:cdsdb;create=true
    spring.datasource.username = ccds_user
    spring.datasource.password = some-password
    spring.jpa.database-platform=org.hibernate.dialect.DerbyTenSevenDialect

The HTTP server's username and password are configured in the
properties file.  Only one username/password is used to secure the
REST endpoint. The default entries for the server are shown here::

    security.user.name=ccds_client
    security.user.password=(encrypted)

At runtime in production deployments, instead of using a configuration file,
all configuration properties should be supplied using a block of JSON in an
environment variable called SPRING\_APPLICATION\_JSON. This can easily be done
in a docker-compose configuration file.  For example::

      SPRING_APPLICATION_JSON: '{
          "server" : {
              "port" : 8002
          },
          "security" : {
              "user" : {
                  "name"     : "ccds_client",
                  "password" : "ENC(encrypted-string-here)"
              }
          },
          "spring" : {
              "database" : {
                  "driver" : {
                      "classname" : "org.mariadb.jdbc.Driver"
                  }
              },
              "datasource" : {
                  "url"      : "jdbc:mariadb://hostname-db:3306/cds?useSSL=false",
                  "username" : "cds",
                  "password" : "ENC(encrypted-string-here)"
              },
              "jpa" : {
                  "database-platform" : "org.hibernate.dialect.MySQLDialect",
                  "hibernate" : {
                      "ddl-auto" : "validate"
                  }
              }
          }
     }'

Generating Encrypted Passwords
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Clear-text passwords are prohibited in many deployment environments. Use the following commands to generate an encrypted password for the database and the service.

1. Download the jar, for example using wget::

    wget http://central.maven.org/maven2/org/jasypt/jasypt/1.9.2/jasypt-1.9.2.jar

2. Use the Jasypt jar to generate the password. Note that the input 'YourPasswordHere' is the actual database password.  The confusingly named password parameter is used to encrypt the input::

    java -cp jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI algorithm=PBEWithMD5AndDES input='YourPasswordHere' password='EncryptionKey'

Using Encrypted Passwords
~~~~~~~~~~~~~~~~~~~~~~~~~

The same "password" parameter used to encrypt the passwords must be supplied at run time, in any of the following ways:

1. In the application.properties file using the key jasypt.encryptor.password.  For example::

    jasypt.encryptor.password=EncryptionKey

2. Alternately, the password can be supplied on the command line with a JVM argument "-Djasypt.encryptor.password"::

    java -jar cmn-data-svc-server-N.N.jar -Djasypt.encryptor.password=EncryptionKey

Launch Instructions
~~~~~~~~~~~~~~~~~~~

Once the configuration is provided either in an application.properties file or in an environment variable,
start the application with the following command::

    java -Djava.security.egd=file:/dev/./urandom -jar common-dataservice-1.2.3.jar

Troubleshooting
---------------

Spring-Boot throws a confusing exception if the database connection fails, something like this::

    Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException:
    Error creating bean with name 'artifactController': Unsatisfied dependency expressed through field 'artifactService'; nested exception is
    org.springframework.beans.factory.UnsatisfiedDependencyException:
    Error creating bean with name 'artifactService': Unsatisfied dependency expressed through field 'sessionFactory'; nested exception is
    org.springframework.beans.factory.BeanCreationException:
    Error creating bean with name 'sessionFactory' defined in class path resource [.../ccds/hibernate/HibernateConfiguration.class : Invocation of init method failed;
    nested exception is org.hibernate.service.spi.ServiceException:
    Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment]

If you see this exception, first check the database configuration carefully.
