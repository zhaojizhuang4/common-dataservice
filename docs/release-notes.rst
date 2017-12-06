=============
Release Notes
=============

The server and client are released together.  The server is deployed within a Docker image
in the Docker registry.  The client is deployed as a jar file to a Maven Nexus repository.

Version 1.10.0, ? December 2017
-------------------------------

* Increase size of details column for solution validation
* Support threads and comments.

Version 1.9.1, 30 November 2017
-------------------------------

* Add method to get rating by key fields solution ID and user ID
* Revise searchSolutions method to accept complex query criteria

Version 1.9.0, 16 November 2017
-------------------------------

* Add methods to get role count, users-in-role count
* Add methods for bulk update of users in roles
* Add "options" attribute to Peer Subscription

Version 1.8.0, 9 November 2017
------------------------------

* Add artifact ID to the solution download record
* Add last-download date to the solution web record

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

Version 1.6.1, 18 October 2017
------------------------------

* Repair defect in updateSolutionRating feature
* Revise get-user-notification feature to include viewed status

Version 1.6.0, 13 October 2017
------------------------------

* Add support for fetching, creating and deleting solution favorites
* Add support for fetching, creating, updating and deleting solution validations
* Add support for fetching, creating and deleting validation sequences
* Store hashes of user passwords using BCrypt algorithm
* Add artifact type codes "BP" and "DS"
* Add model type code "DS"

Version 1.5.3, 26 September 2017
--------------------------------

* Revise signature of update-password client method
* Add method to get all solutions accessible to specified user
* Rename method to getSolutionAccessUsers (was getSolutionUserAccess)
* Implement server method to fetch role function
* Extend get-user-notification client method to accept page parameter
* Drop unused parameter peerId from several peer-subscription methods
* Use MariaDB client library as JDBC connector

Version 1.5.2, 20 September 2017
--------------------------------

* Add protobuf as an artifact type with code "PJ"

Version 1.5.1, 14 September 2017
--------------------------------

* Add update password end point and method
* Add methods to get page of notifications and notification count

Version 1.5.0, 5 September 2017
-------------------------------

* Change peer entity to have unstructured contact information

Version 1.4.1, 29 August 2017
-----------------------------

* Add methods to add, drop roles for a user
* Extend user controller to cascade delete to login providers, notifications, roles
* Validate schema on startup

Version 1.4.0, 23 August 2017
-----------------------------

* Add picture attribute to user entity
* Add statistics for solutions: view count
* Add simple user access control list for solutions
* Fix CD-765, count methods always return zero
* Cache solution download and rating statistics 

Version 1.3.1 update, 15 August 2017
------------------------------------

* Accept valid UUID as ID when creating artifact, solution and other entities

Version 1.3.1, 9 August 2017
----------------------------

* Add org name attribute to user entity
* Add methods to find solutions by tag, toolkit type
* Extend search methods to select AND/OR conditions
* Use HQL for all queries, no native SQL

Version 1.3.0, 7 August 2017
----------------------------

* Add solution download feature: get/create/delete items to track downloads and get count
* Add solution rating feature: get/create/update/delete reviews and get average rating
* Add solution tag feature: get/create/delete individual tags, get/add/drop tags on solutions
* Add notification feature: get/create/delete notifications; add/update/drop users as recipients
* Add password-expiration field to user entity
* Match email address when checking login credentials

Version 1.2.3, 31 July 2017
---------------------------

* Repair client bug in RestPageResponse implementation so iterator returns content
* Repair server-side bug in getSolutionRevisions feature
* Add client methods getHealth(), getVersion(), getRevisionsForArtifact()

Version 1.2.2, 28 July 2017
---------------------------

* Extend partial-match methods to accept page requests and return paged results
* Stop requiring HTTP authentication on swagger documentation pages

Version 1.2.1, 27 July 2017
---------------------------

* Add find methods that perform partial matches (like queries)
* Add user web token and social login provider support
* Drop C(r)UD support for artifact type, model type values

Version 1.2.0, 26 July 2017
---------------------------

* Add entity Peer Subscription and methods for CRUD operations
* Remove collections within models to stop eager fetching of data; e.g., the revisions for a solution
* Revise get-all methods to support pagination: accept max, page and sort parameters
* Add new methods so clients can fetch data lazily; e.g., the revisions for a solution
* Refactor to use Spring repositories instead of custom database query methods

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
