INTEGRATION TESTING
===================
cm-subscribed-events-nbi integration tests execute a suite of arquillian tests.

TESTSUITE STRUCTURE
-------------------

|  -  testsuite (all non functional tests, maven module) -Dts (to enable)
   |  - integration (contains test war dependencies)
       |  -  jee (JEE related integration tests) -Dts.skip.integration.jee (to skip jee tests)


TESTSUITE EXECUTION
-------------------
Test suites can be run against an already running jboss instance or one can be downloaded during test execution.

To download a new jboss instance and run integration tests run:

    $ mvn clean install -Dts -Pjboss_managed_local

To skip jee integration tests run:

      $ mvn clean install -Dts -Dts.skip.integration.jee

To run integration tests against an already running JBOSS run:

    $ mvn clean install -Dts -Pjboss_remote

