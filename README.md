# javalidation

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.raniagus/javalidation/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.raniagus/javalidation)

Example library for validating Java code.

## Installation

Javalidation is hosted in the Maven Central Repository. Simply add the following
dependency into your `pom.xml` file:

```xml
    <dependency>
      <groupId>io.github.raniagus</groupId>
      <artifactId>javalidation</artifactId>
      <version>0.4.2</version>
    </dependency>
```

### Snapshots

Also, snapshots of the master branch are deployed automatically with each
successful commit. Instead of Maven Central, use the Sonatype snapshots
repository at:

```xml
<url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
```

You can add the repository in your `pom.xml` file:
  
```xml
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```
