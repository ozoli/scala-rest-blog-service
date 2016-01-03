# Scala REST Blog Service

Reads (Blog) entries from an RSS feed.

Stores the Blog Entries in a [MongoDB](https://www.mongodb.com) database for retrieval. 

Provides a POST endpoint for Pub/Sub protocol to notify of new Blog entries in the RSS feed. 
Once received this new Blog Entry is added to the MySQL database. 

Use the [Mongo DB Scala Driver](http://mongodb.github.io/casbah/3.1/) 

Uses [Embed Mongo](https://github.com/SimplyScala/scalatest-embedmongo) for integration testing with an embedded MongoDB.


# Acknowledgements

Lots of kudos and high fives must be given to the following which the above is based on:

[Start and stop a Scala application in production](http://flurdy.com/docs/scalainit/startscala.html)
[Scala Style Logging with Grizzled-SLF4J](http://alvinalexander.com/scala/scala-logging-grizzled-slf4j)
Creating a FAT JAR using SBT Assembl[sbt-assembley](https://github.com/sbt/sbt-assembly)

