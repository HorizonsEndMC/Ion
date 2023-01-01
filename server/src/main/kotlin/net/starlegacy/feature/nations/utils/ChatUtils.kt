package net.starlegacy.feature.nations.utils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent

fun TextComponent.url(url: String, hover: String? = null): TextComponent {
	if (hover != null) hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("$url ($hover)")))
	clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
	return this
}

fun TextComponent.cmd(command: String, hover: String? = null): TextComponent {
	if (hover != null) hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("$hover")))
	clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command)
	return this
}

fun TextComponent.hover(hover: String): TextComponent {
	hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent(hover))); return this
}

fun TextComponent.color(color: ChatColor): TextComponent {
	this.color = color; return this
}

fun TextComponent.underline(): TextComponent {
	this.isUnderlined = true; return this
}
