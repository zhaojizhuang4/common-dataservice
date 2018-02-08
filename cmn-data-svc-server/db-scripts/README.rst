==================================
Acumos Common Data Service Scripts
==================================

This directory has scripts for creating and populating a database used by the Common Data Service.
The simple semantic versioning system for scripts uses only 2 digits. So for example, database 
scripts at version 1.3 can support service version 1.3.0, version 1.3.1 and so on.  That last
digit refers to bug fixes and other changes that do not break the database/field contracts.

Install Instructions
--------------------

To create a new database, run the script appropriate for the version.  For example:

::

    mysql> create database cds1130;
    mysql> use cds1130;
    mysql> source cmn-data-svc-ddl-dml-mysql-1-13.sql;

Upgrade Instructions
--------------------

To upgrade a database, copy the data into a new database then run the upgrade script(s) appropriate for the version. For example:

::

    mysql> create database cds1130;
    $ mysqldump cds1120 | mysql cds1130
    mysql> source cds-mysql-upgrade-1-12-to-1-13.sql;

Please note this skips details of logging in to the database etc.
