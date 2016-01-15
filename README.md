# Scala REST Blog Service

Reads (Blog) entries from an RSS feed.

Stores the Blog Entries in a [MongoDB](https://www.mongodb.com) database for retrieval.

Provides a POST endpoint for Pub/Sub protocol of [PubSubHubbub](https://en.wikipedia.org/wiki/PubSubHubbub) to notify of new Blog entries in the RSS feed. Wordpress uses the same protocol or at least (used to)[https://en.blog.wordpress.com/2010/03/03/rub-a-dub-dub-in-the-pubsubhubbub/].
Once received this new Blog Entry is added to the MySQL database. 

Uses the [Mongo DB Scala Driver](http://mongodb.github.io/mongo-scala-driver/).

For testing [ScalaTest](http://www.scalatest.org) is used with [Embed Mongo](https://github.com/SimplyScala/scalatest-embedmongo) for integration testing with an embedded MongoDB.

# Notes 

Run the tests with sbt clean compile test Running sbt test multiple times will result in test failures.

# Acknowledgements

Lots of kudos and high fives must be given to the following which the above is based on:

[Start and stop a Scala application in production](http://flurdy.com/docs/scalainit/startscala.html)
[Scala Style Logging with Grizzled-SLF4J](http://alvinalexander.com/scala/scala-logging-grizzled-slf4j)
Creating a FAT JAR using SBT Assembley[sbt-assembley](https://github.com/sbt/sbt-assembly)

