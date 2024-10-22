package net.horizonsend.ion.server.miscellaneous.utils.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

val Component.itemName get() = Component.text()
	.decoration(TextDecoration.ITALIC, false)
	.append(this)
	.build()
