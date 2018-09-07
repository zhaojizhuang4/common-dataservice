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

=================================
Common Data Service Release Notes
=================================

The client and server are released together.  The client is deployed as a jar file to a
Maven Nexus repository. The server is deployed as a Docker image to a Docker registry.

Version 1.18.1, 10 Sep 2018
---------------------------

* New client method to find restricted solutions (ACUMOS-1611)
* Defend against null arguments (ACUMOS-1696)
* Add default value in database scripts for timestamp-type columns (ACUMOS-1703)
* Fix bug in method that builds criteria for querying on value list (ACUMOS-1707)
* Refactor artifact-search controller annotations for Swagger web UI
* Add example federation selector to Swagger annotation
* Move some methods from user to notification controller
* Increase default memory limit to 1GB
* Upgrade Spring-Boot from 1.15.14.RELEASE to 1.15.15.RELEASE.
* Requires database schema version 1.18

Version 1.18.0, 31 Aug 2018
---------------------------

* Add publish request entity with supporting methods (ACUMOS-1642)
* Add tags to User entity (ACUMOS-1643)
* Refactor find-solution methods to enable Swagger UI
* Requires database schema version 1.18

Version 1.17.3, 31 Aug 2018
---------------------------

* Added new API endpoint to get Dataset info, right now it is just prototype (ACUMOS-1182)

Version 1.17.2, 28 Aug 2018
---------------------------

* Revise logging for ONAP recommended output pattern (ACUMOS-625)
* Include solution and revision ID fields in search (ACUMOS-1576)
* Revise message sent when user is locked out temporarily (ACUMOS-1597)
* Filter search results using all tags not any tag (ACUMOS-1601)
* Document Pageable request parameters in Swagger (ACUMOS-1608)
* Enhance search for revision description (ACUMOS-1614)
* Add API to count top-level comments on a revision (ACUMOS-1644)
* Requires database schema version 1.17

Version 1.17.1, 10 Aug 2018
---------------------------

* Create tags automatically when creating or updating a solution (ACUMOS-1546)
* Change data type of picture fields from Byte[] to byte [] (ACUMOS-1557)
* Requires database schema version 1.17

Version 1.17.0, 9 Aug 2018
--------------------------

* Store metadata for user documents in Nexus (ACUMOS-1235)
* Load capitalized role name "Admin" not "admin" (ACUMOS-1526)
* Requires database schema version 1.17

Version 1.16.1, 2 Aug 2018
--------------------------

* Store API token encrypted, not hashed (ACUMOS-1487)
* Requires database schema version 1.16

Version 1.16.0, 24 July 2018
----------------------------

* Add column for solution picture; add table for revision description (ACUMOS-1235)
* Rename ownerId to userId in solution, revision, artifact, peer subscription (ACUMOS-1359)
* Add authors; move provider in solution to publisher in revision (ACUMOS-1359)
* Extend findPortalSolutions to search authors and publisher (ACUMOS-1359)
* Add verification token and expiration date (ACUMOS-1386)
* Add API token attribute to user entity, add loginApi method (ACUMOS-1424)
* Add attributes to track failed login attempts (ACUMOS-1442)
* Enable the admin role in newly created database (ACUMOS-1446)
* Add support for proxy when getting a CDS client instance
* Requires database schema version 1.16

Version 1.15.4, 6 July 2018
---------------------------

* Add method to get solution revision comment count (ACUMOS-1270)
* Show exception details in log files (ACUMOS-1328)
* Requires database schema version 1.15

Version 1.15.3, 27 June 2018
----------------------------

* Add search method to find user's co-owned solutions (ACUMOS-1257)
* Add methods to manage composite solution parent-child membership
* Update Spring library versions to address vulnerabilities flagged by CLM 
* Requires database schema version 1.15

Version 1.15.2, 6 June 2018
---------------------------

* Add toolkit type codes CO, SP (ACUMOS-1013)
* Repair findPortalSolutions API behavior on untagged solutions (ACUMOS-1045)
* Requires database schema version 1.15

Version 1.15.1, 18 May 2018
---------------------------

* Call audit logger in controller methods (ACUMOS-625)
* Add artifact type code LG - Log File (ACUMOS-765)
* Define C_USER table PICTURE column as type LONGBLOB (ACUMOS-888)
* Requires database schema version 1.15

Version 1.15.0, 6 April 2018
----------------------------

* Move the acccess-type and validation-status attributes from Solution to Solution Revision entity (ACUMOS-196)
* Revise field labels in site_config table entry (ACUMOS-346)
* Add unique constraints for name columns; e.g., role name (ACUMOS-435)
* Add fields to site_config table entry (ACUMOS-486)
* Search solutions returns unexpectedly few results (ACUMOS-529)
* User email attribute should not accept null (ACUMOS-603)
* Write details about security-related events to the audit log (ACUMOS-618)
* Check user (in)active status in login and change password methods (ACUMOS-639)
* Define admin user with well-known username and password for all-in-one install (ACUMOS-388)
* Requires database schema version 1.15

Version 1.14.5, 6 June 2018
---------------------------

* Add toolkit type codes CO, SP (ACUMOS-1013)
* Requires database schema version 1.14

Version 1.14.4, 3 May 2018
--------------------------

* Add artifact type code LG - Log File (ACUMOS-765)
* Requires database schema version 1.14

Version 1.14.3, 26 March 2018
-----------------------------

* Search solutions returns unexpectedly few results (ACUMOS-529)
* Requires database schema version 1.14

Version 1.14.2, 15 March 2018
-----------------------------

* Refactor code-name value sets to use properties (ACUMOS-376)
* Add Swagger annotations to required fields in domain models (ACUMOS-399)
* Requires database schema version 1.14

Version 1.14.1, 9 March 2018
----------------------------

* Define created-date columns as "DEFAULT 0" to stop Mariadb from setting to now() on update (ACUMOS-243)
* Cascade solution delete to associated step results (ACUMOS-328)
* Drop unneeded queries in server-side repository methods (ACUMOS-344)
* Add copy constructors to all domain POJO classes (ACUMOS-345)
* Requires database schema version 1.14

Version 1.14.0, 1 March 2018
----------------------------

* Add search-by-date method for federation subscription update (ACUMOS-61)
* Add peer group, solution group and mapping features for federation access control (ACUMOS-62)
* Refactor to drop code-name database tables (ACUMOS-144)
* Add feature for user notification preference and user notification (ACUMOS-166)
* Assert unique constraint on peer subjectName attribute (ACUMOS-214)
* Revise peer status code/name value set (ACUMOS-215)
* Add new toolkit type code for ONAP (ACUMOS-232)
* Add license headers to sql files (ACUMOS-275)
* Apply distinct transformer to avoid duplicate search results (ACUMOS-298)
* Report consistent error message when an item is not found
* Requires database schema version 1.14

Version 1.13.1, 9 February 2018
-------------------------------

* Limit memory use in server JVM to max 512MB
* Correct search method usage of response page wrapper class
* Requires database schema version 1.13

Version 1.13.0, 7 February 2018
-------------------------------

* Add workflow step feature for onboarding and validation result persistence (ACUMOS-56)
* Add origin attribute to solution and revision entities (ACUMOS-59)
* Revise search methods to return a page of results
* Revise peer and peer subscription attributes (ACUMOS-60, ACUMOS-167)
* Add toolkit type PB - Probe (ACUMOS-168)
* Requires database schema version 1.13

Version 1.12.1, 26 January 2018
-------------------------------

* Repair findPortalSolutions endpoint to process multiple values correctly
* Requires database schema version 1.12

Version 1.12.0, 23 January 2018
-------------------------------

* Extend MLPPeerSubscription with required ownerId attribute with user ID
* Extend MLPSolution with optional sourceId attribute with peer ID
* Add alternate client constructor that accepts RestTemplate
* Extend search methods to accept value arrays
* Add two toolkit-type codes, BR and TC
* Add client mock implementation
* Extend enums to have names, not just codes
* Address code-quality issues identified by LF Sonar
* Requires database schema version 1.12

Version 1.11.0, 3 January 2018
------------------------------

* Revise MLPSiteConfig to make userId optional
* Revise MLPThread to add solutionId and revisionId; drop url
* Revise MLPComment to drop url
* Revise MLPPeer to add trustLevel
* Add methods to query for threads and comments using solution and revision IDs
* Requires database schema version 1.11

Version 1.10.2, 20 December 2017
--------------------------------

* Extend MLPSolution with tags and solution web statistics via unidirectional annotations
* Extend the find-solutions method for Portal/Marketplace dynamic search
* Requires database schema version 1.10

Version 1.10.1, 12 December 2017
--------------------------------

* Revert search-solutions method to version of 1.9.0
* New find-solutions method for Portal/Marketplace dynamic search
* Requires database schema version 1.10

Version 1.10.0, 6 December 2017
-------------------------------

* Increase size of details column for solution validation
* Support threads and comments
* Requires database schema version 1.10

Version 1.9.1, 30 November 2017
-------------------------------

* Add method to get rating by key fields solution ID and user ID
* Revise searchSolutions method to accept complex query criteria
* Requires database schema version 1.9

Version 1.9.0, 16 November 2017
-------------------------------

* Add methods to get role count, users-in-role count
* Add methods for bulk update of users in roles
* Add "options" attribute to Peer Subscription
* Requires database schema version 1.9

Version 1.8.0, 9 November 2017
------------------------------

* Add artifact ID to the solution download record
* Add last-download date to the solution web record
* Requires database schema version 1.8

Version 1.7.0, 3 November 2017
------------------------------

* Add support to fetch, create and delete solution deployments
* Add support to fetch, create and delete site configurations
* Add solution web metadata such as featured status
* Change all classes to use package prefix org.acumos
* Revise get-count methods to return long (not CountTransport)
* Revise "RCloud" name to just "R"
* Revise database schema to drop Mysql-specific column types like TINYINT
* Move tests that depend on a deployed instance to the test subproject
* Change default properties to a Derby in-memory database
* Add unit tests for client and server
* Address code-quality issues identified by Sonar
* Requires database schema version 1.7

Version 1.6.1, 18 October 2017
------------------------------

* Repair defect in updateSolutionRating feature
* Revise get-user-notification feature to include viewed status
* Requires database schema version 1.6

Version 1.6.0, 13 October 2017
------------------------------

* Add support for fetching, creating and deleting solution favorites
* Add support for fetching, creating, updating and deleting solution validations
* Add support for fetching, creating and deleting validation sequences
* Store hashes of user passwords using BCrypt algorithm
* Add artifact type codes "BP" and "DS"
* Add model type code "DS"
* Requires database schema version 1.6

Version 1.5.3, 26 September 2017
--------------------------------

* Revise signature of update-password client method
* Add method to get all solutions accessible to specified user
* Rename method to getSolutionAccessUsers (was getSolutionUserAccess)
* Implement server method to fetch role function
* Extend get-user-notification client method to accept page parameter
* Drop unused parameter peerId from several peer-subscription methods
* Use MariaDB client library as JDBC connector
* Requires database schema version 1.5

Version 1.5.2, 20 September 2017
--------------------------------

* Add protobuf as an artifact type with code "PJ"
* Requires database schema version 1.5

Version 1.5.1, 14 September 2017
--------------------------------

* Add update password end point and method
* Add methods to get page of notifications and notification count
* Requires database schema version 1.5

Version 1.5.0, 5 September 2017
-------------------------------

* Change peer entity to have unstructured contact information
* Requires database schema version 1.5

Version 1.4.1, 29 August 2017
-----------------------------

* Add methods to add, drop roles for a user
* Extend user controller to cascade delete to login providers, notifications, roles
* Validate schema on startup
* Requires database schema version 1.4

Version 1.4.0, 23 August 2017
-----------------------------

* Add picture attribute to user entity
* Add statistics for solutions: view count
* Add simple user access control list for solutions
* Fix CD-765, count methods always return zero
* Cache solution download and rating statistics
* Requires database schema version 1.4

Version 1.3.1 update, 15 August 2017
------------------------------------

* Accept valid UUID as ID when creating artifact, solution and other entities
* Requires database schema version 1.3

Version 1.3.1, 9 August 2017
----------------------------

* Add org name attribute to user entity
* Add methods to find solutions by tag, toolkit type
* Extend search methods to select AND/OR conditions
* Use HQL for all queries, no native SQL
* Requires database schema version 1.3

Version 1.3.0, 7 August 2017
----------------------------

* Add solution download feature: get/create/delete items to track downloads and get count
* Add solution rating feature: get/create/update/delete reviews and get average rating
* Add solution tag feature: get/create/delete individual tags, get/add/drop tags on solutions
* Add notification feature: get/create/delete notifications; add/update/drop users as recipients
* Add password-expiration field to user entity
* Match email address when checking login credentials
* Requires database schema version 1.3

Version 1.2.3, 31 July 2017
---------------------------

* Repair client bug in RestPageResponse implementation so iterator returns content
* Repair server-side bug in getSolutionRevisions feature
* Add client methods getHealth(), getVersion(), getRevisionsForArtifact()
* Requires database schema version 1.2

Version 1.2.2, 28 July 2017
---------------------------

* Extend partial-match methods to accept page requests and return paged results
* Stop requiring HTTP authentication on swagger documentation pages
* Requires database schema version 1.2

Version 1.2.1, 27 July 2017
---------------------------

* Add find methods that perform partial matches (like queries)
* Add user web token and social login provider support
* Drop C(r)UD support for artifact type, model type values
* Requires database schema version 1.2

Version 1.2.0, 26 July 2017
---------------------------

* Add entity Peer Subscription and methods for CRUD operations
* Remove collections within models to stop eager fetching of data; e.g., the revisions for a solution
* Revise get-all methods to support pagination: accept max, page and sort parameters
* Add new methods so clients can fetch data lazily; e.g., the revisions for a solution
* Refactor to use Spring repositories instead of custom database query methods
* Requires database schema version 1.2

Version 1.1.3, 21 July 2017
---------------------------

* Repair bugs in client update methods not passing along IDs
* Add methods for CRUD operations on model type; user login.

Version 1.1.2, 18 July 2017
---------------------------

* Extend with Peer and new attributes on Solution.

Version 1.1.1, 5 July 2017
--------------------------

* Extend for solution revisions, which are collections of artifacts.

Version 1.1.0, 30 June 2017
---------------------------

* Adds solution revisions, UUID values as IDs and more.

Version 1.0.0, 15 June 2017
---------------------------

* Supports solutions, artifacts and users.
