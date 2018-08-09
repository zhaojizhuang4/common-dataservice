.. ===============LICENSE_START=======================================================
.. Acumos CC-BY-4.0
.. ===================================================================================
.. Copyright (C) 2017 AT&T Intellectual Property & Tech Mahindra. All rights reserved.
.. ===================================================================================
.. This Acumos documentation file is distributed by AT&T and Tech Mahindra
.. under the Creative Commons Attribution 4.0 International License (the "License");
.. you may not use this file except in compliance with the License.
.. You may obtain a copy of the License at
..
.. http://creativecommons.org/licenses/by/4.0
..
.. This file is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ===============LICENSE_END=========================================================

============================
CMS User Data Migration Tool
============================

This document explains a utility that migrates user-supplied data from the
Hippo-CMS system to the Acumos Common Data Service version 1.17 and the 
Acumos Nexus repository.  This utility migrates the following data items:

# Solution picture: a user can add a picture to a solution.

# Revision descriptions: a user can add a description appropriate for the COMPANY
  access level and another description appropriate for the PUBLIC access level
  of a single revision. In other words, every revision can have zero, one or two
  descriptions.

# Revision supporting documents: a user can upload many supporting documents for a
  revision, one set visible at the COMPANY access level and another set of documents visible
  at the PUBLIC access level. In other words, every revision can have an arbitrary number
  of supporting documents, divided into two sets.

Prerequisites
-------------

This migration tool pushes data to an Acumos Common Data Service at version 1.17.0 or later.
Use of the tool requires read access to CMS, and write access to CDS and Nexus.

Configuration
-------------

After obtaining valid URLs and appropriate user names and passwords for all three systems,
enter them in a file named "migrate.properties" using the following structure::

	cds.url = http://cdshost.myproject.org:8001/ccds
	cds.user =
	cds.pass =
	
	cms.url = http://cmshost.myproject.org:8085/site
	cms.user =
	cms.pass =
	
	nexus.url = http://nexushost.myproject.org:8081/repository/repo_name
	nexus.user =
	nexus.pass =
	# this is the group prefix; a UUID compnent will be added
	nexus.prefix = org.acumos


Build Instructions
------------------

Clone the Git repository and build the tool as follows::

    git clone https://gerrit.acumos.org/r/common-dataservice
    cd common-dataservice/migrate-cms-to-cds
    mvn clean package


Usage
-----

Run the migration tool like this::

    java target/migrate-cms-to-cds-1.0.0-SNAPSHOT-spring-boot.jar

The tool expects to find file "migrate.properties" in the current directory.
It will write a log file to the current directory.

The migration tool discovers the list of solutions by querying CDS, checks the content
of each solution by querying CMS, and migrates content to CDS and Nexus as needed.

In case of error, the tool can be run repeatedly on the same source and targets.
It will not re-migrate data to CDS nor Nexus for any item.

When the tool is finished it reports statistics in this format::

    14:37:06.784 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Migration statistics:
    14:37:06.784 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Solutions checked: 1392
    14:37:06.785 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Revisions checked: 2379
    14:37:06.785 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Pictures migrated: 2 success, 0 fail
    14:37:06.785 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Descriptions migrated: 0 success, 0 fail
    14:37:06.785 [main] INFO  o.a.cds.migrate.MigrateCmsToCdsApp - Documents migrated: 0 success, 4 fail


Troubleshooting
---------------

The migration tool expects every document to have a file suffix that indicates the type of document;
e.g., ".doc" or ".xlsx".  A document without any suffix cannot be migrated.  Revise the revision to
fix this problem, then re-run the migration.
