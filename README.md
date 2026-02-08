# javalidation

[![MvnRepository](https://badges.mvnrepository.com/badge/io.github.raniagus/javalidation/badge.svg?label=MvnRepository)](https://mvnrepository.com/artifact/io.github.raniagus/javalidation)

Example library for validating Java code.

## Installation

Javalidation is hosted in the Maven Central Repository. Simply add the following
dependency into your `pom.xml` file:

```xml
    <dependency>
      <groupId>io.github.raniagus</groupId>
      <artifactId>javalidation</artifactId>
      <version>0.8.0</version>
    </dependency>
```

### Snapshots

Also, snapshots of the master branch are deployed automatically on each successful
commit. Instead of Maven Central, you have to consume the Sonatype snapshots
repository and add `-SNAPSHOT` suffix to the version identifier.

#### Consuming via Maven

Configure your `pom.xml` file with the following `<repositories>` section:
  
```xml
<repositories>
  <repository>
    <name>Central Portal Snapshots</name>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

#### Consuming via Gradle

Configure your `build.gradle` file with the following:

```gradle
repositories {
  maven {
    name = 'Central Portal Snapshots'
    url = 'https://central.sonatype.com/repository/maven-snapshots/'

    // Only search this repository for the specific dependency
    content {
      includeModule("io.github.raniagus", "javalidation")
    }
  }
  mavenCentral()
}
```
