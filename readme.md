== Overview

The basic idea of performance regression tests is to compare memory, cpu and disk i/o
metrics between between two builds. The test is considered "pass" if performance of the 
two builds is within a certain tolerance range. 

For performance metrics to be comparable, all builds must be performed under the same
conditions. In most cases hardware (new cpu, more memory, etc) and software (new jvm, 
jvm start up options) changes invalidate previous test metrics.

== Hudson slave

All performance tests run on a dedicated vm perfy.takari.io, which is configured as Perfy slave
in hudson running on http://ci.takari.io:8080.

    http://ci.takari.io:8080/computer/Perfy/

== Eclipse performance regression testing harness

Maven performance regression tests use Eclipse performance test harness. To make the harness
usable from Maven builds, the code was extracted from eclipse git repository and pushed

    https://github.com/takari/takari-performance-harness 

== Derby, historical test performance metrics

Tests use embedded Derby to persist performance test metrics. Database files are located at
/var/derby.

== Configured performance test jobs

=== Helper jobs

takari-performance-harness builds and deploys eclipse performance test harness

maven_master builds and deploys latest maven master SNAPSHOT builds. Also used to
trigger performance regression tests.

performance-template cascading template job for all performance baseline and
performance regression test jobs. Does not provide maven-related configuration
because of how Hudson job configuration inheritance works.

=== Performance baseline builds

maven-performance-tests_3.0.5 and maven-performance-tests_3.2.2 jobs runs performance tests and store
metrics metrics in the database but they do not assert performance (which would be rather pointless
for already released code). 

Unfortunately, it is not easy to have both maven 3.0.x and 3.1+ support due to aether groupId change,
so maven-performance-tests_3.0.5 is currently disabled.

=== Performance regression test build

maven-performance-tests_master runs performance tests with the latest maven 3.2.3-SNAPSHOT version
and compares test performance metrics with performance of baseline build. The tests are considered 
"failed" if performance is not within -20/+10% range of the baseline.
