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

A Java class named "BasicSequenceDemo" demonstrates use of the client.
Please browse for this file in the client project test area using this link:
`BasicSequenceDemo.java <https://gerrit.acumos.org/r/gitweb?p=common-dataservice.git;a=blob;f=cmn-data-svc-client/src/test/java/org/acumos/cds/client/test/BasicSequenceDemo.java;hb=refs/heads/master>`_.
