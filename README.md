phyloutils
==========

_A Java library for phylogenetic tree computations_

 * Provides functionality regarding weighted phylogenetic trees, extending the [trees](http://dev.davidsoergel.com/trac/trees/) package.
 * Parses New Hampshire (aka Newick) tree files.
 * Computes phylogenetic alpha and beta diversity measures, such as Weighted !UniFrac.
 * Computes phylogenetic distances between species based on the [Ciccarelli et al. 2006](http://www.sciencemag.org/cgi/content/abstract/311/5765/1283) tree of life, and based on the [FastTree 16S tree](http://www.microbesonline.org/fasttree/#16S) of [GreenGenes](http://greengenes.lbl.gov) sequences.


Documentation
-------------

 * [API docs](http://davidsoergel.github.io/phyloutils/)

Download
--------

[Maven](http://maven.apache.org/) is by far the easiest way to make use of `phyloutils`.  Just add these to your pom.xml:
```xml
<repositories>
	<repository>
		<id>dev.davidsoergel.com releases</id>
		<url>http://dev.davidsoergel.com/nexus/content/repositories/releases</url>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>
	<repository>
		<id>dev.davidsoergel.com snapshots</id>
		<url>http://dev.davidsoergel.com/nexus/content/repositories/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>com.davidsoergel</groupId>
		<artifactId>phyloutils</artifactId>
		<version>0.9</version>
	</dependency>
</dependencies>
```

If you really want just the jar, you can get the [latest release](http://dev.davidsoergel.com/nexus/content/repositories/releases/edu/berkeley/compbio/phyloutils/) from the Maven repo; or get the [latest stable build](http://dev.davidsoergel.com/jenkins/job/dsutils/lastStableBuild/edu.berkeley.compbio$phyloutils/) from the build server.

