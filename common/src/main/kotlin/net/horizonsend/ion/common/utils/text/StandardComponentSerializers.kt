package net.horizonsend.ion.common.utils.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

typealias MiniMessageString = String
val miniMessage = MiniMessage.miniMessage()

/** Restricted minimessage serializer that is less likely to be abused */
val restrictedMiniMessageSerializer = MiniMessage.builder()
	.preProcessor { enteredText ->
		enteredText
			.replace("\uE032", "")
			.replace("\uE033", "")
	}
	.tags(TagResolver.resolver(
		StandardTags.font(),
		StandardTags.decorations(TextDecoration.ITALIC),
		StandardTags.decorations(TextDecoration.BOLD),
		StandardTags.decorations(TextDecoration.STRIKETHROUGH),
		StandardTags.decorations(TextDecoration.UNDERLINED),
		StandardTags.reset(),
		StandardTags.color(),
		StandardTags.rainbow(),
		StandardTags.transition(),
		StandardTags.gradient()
	)).build()

/** Converts the provided MiniMessage string to a component using the MiniMessage serializer. */
fun String.miniMessage() = deserializeComponent(this, miniMessage)

typealias LegacyTextString = String
val legacyAmpersand = LegacyComponentSerializer.legacyAmpersand()

typealias GsonComponentString = String
val gson = GsonComponentSerializer.gson()

/** Skip building the serializer */
val plainText = PlainTextComponentSerializer.plainText()

/** Converts the provided Component to a string using the PlainText serializer. */
fun ComponentLike.plainText(): String = serialize(plainText)

fun <R : Any> ComponentLike.serialize(serializer: ComponentSerializer<Component, out Component, R>): R = serializer.serialize(this.asComponent())
fun <R : Any> deserializeComponent(raw: R, serializer: ComponentSerializer<Component, out Component, R>): Component = serializer.deserialize(raw)
