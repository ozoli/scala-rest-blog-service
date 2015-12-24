# Scala REST Blog Service

Reads (Blog) entries from an RSS feed.

Stores the Blog Entries in a MySQL database for retrieval. 

Provides a POST endpoint for Pub/Sub protocol to notify of new Blog entries in the RSS feed. 
Once received this new Blog Entry is added to the MySQL database. 

[Slick 3.0](http://slick.typesafe.com/doc/3.0.0/introduction.html) is used to drop, create and persist the Blog entries read from the RSS feed. 


# Acknowledgements

Lots of kudos and high fives must be given to the following which the above is based on:

[Start and stop a Scala application in production](http://flurdy.com/docs/scalainit/startscala.html)
[Scala Style Logging with Grizzled-SLF4J](http://alvinalexander.com/scala/scala-logging-grizzled-slf4j)
Creating a FAT JAR using SBT Assembl[sbt-assembley](https://github.com/sbt/sbt-assembly)

