package net.horizonsend.ion.common.utils.text

fun createHtmlLink(text: String, link: String, color: String? = null): String = "<a href=\"$link\"${color?.let { "style=\"color:$color\"" } ?: ""}>$text</a>"

fun wrapStyle(text: String, tag: String, vararg styles: String): String = "<$tag style=\"${styles.joinToString(";")};\">$text</$tag>"
