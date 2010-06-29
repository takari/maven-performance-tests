Running Maven 3.0 performance tests and collecting test data

1. Prerequisites

java 1.6 (1.5 should work too, but was not tested)
maven 2.1.0 (other versions may work too, but were not tested)
cvs
svn
patch

Add the following to settings.xml
<mirrors>
  <mirror>
    <id>nexus</id>
    <mirrorOf>*</mirrorOf>
    <url>http://repository.sonatype.org/content/groups/public</url>
  </mirror>
</mirrors>


2. Eclipse Performance Test Plug-In

2.1. Checkout org.eclipse.test.performance project from eclipse.org CVS repository

cvs -d :pserver:anonymous@dev.eclipse.org:/cvsroot/eclipse co org.eclipse.test.performance

2.2. Apply org.eclipse.test.performance.patch

wget http://svn.sonatype.org/m2eclipse/sandbox/trunk/maven-performance-tests/org.eclipse.test.performance.patch
cd org.eclipse.test.performance/
patch -u -p0 < ../org.eclipse.test.performance.patch

2.3. Build and install org.eclipse.test.performance

mvn clean install

3. Setup derby network server

See http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.test.performance/doc/Performance%20Tests%20HowTo.html?view=co

DBROOT=/var/derby on slowandsteady (maven performance testing machine located at sonatype office) 


4. Setting up Hudson




5. Setting up Maven 3.0 build

svn co http://svn.apache.org/repos/asf/maven/components/trunk
export MAVEN_OPTS="-Xmx128m"
mvn clean install

6. Setting up maven-performance-tests (this project)

svn co http://svn.sonatype.org/m2eclipse/sandbox/trunk/maven-performance-tests
export MAVEN_OPTS="-Xmx128m"
cd trunk
/opt/maven/bin/mvn test -Pmaven-3.0-SNAPSHOT -Dmaven.basedir=/home/hudson/.hudson/jobs/maven-3.0/workspace/trunk
