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
      <version>0.5.0</version>
    </dependency>
```

### Snapshots

Also, snapshots of the master branch are deployed automatically with each
successful commit. Instead of Maven Central, use the Sonatype snapshots
repository at:

```xml
<url>https://central.sonatype.com/repository/maven-snapshots/</url>
```

You can add the repository in your `pom.xml` file:
  
```xml
  <repositories>
    <repository>
      <id>sonatype-snapshots</id>
      <url>https://central.sonatype.com/repository/maven-snapshots/</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```
