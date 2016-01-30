package io.ozoli.blog.domain

import java.time.LocalDateTime

/**
 * A representation of an article in a RSS Feed here call a BlogEntry
 *
 * @param pubDate the publication date
 * @param title the title
 * @param linkTitle the link title ie the title without spaces or punctuation
 * @param body the body
 * @param category the category string a space separated string of tags
 */
case class BlogEntry(pubDate: LocalDateTime, title: String, linkTitle: String, body: String, category: String)

