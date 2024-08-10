package net.horizonsend.ion.server.features.chat.messages

import net.horizonsend.ion.common.database.DbObject
import net.horizonsend.ion.common.database.Oid
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

data class NationsChatMessage<A : DbObject>(
	val id: Oid<A>,
	override val ionPrefix: Component,
	override val luckPermsPrefix: Component?,
	override val playerDisplayName: Component,
	override val luckPermsSuffix: Component?,
	override val message: Component,
	override val playerInfo: Component,

	override val color: TextColor
) : ChatMessage()
