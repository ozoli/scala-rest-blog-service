#!/bin/sh

# Set environment variables for starting the Blog Service

export BLOG_APP_HOSTNAME=localhost
export BLOG_APP_PORT=9999

export BLOG_DB_URI=mongodb://localhost:27017
export BLOG_DB_NAME=blogdb
export BLOG_DB_COLLECTIONNAME=blogs

export BLOG_RSS_URI=http://your.feed.host/YourBlog?fmt=xml

export COVERALLS_REPO_TOKEN=your-coveralls-token
