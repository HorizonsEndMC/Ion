package net.horizonsend.ion.common.utils.text

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

/** Wrapper enum for the 16 colors of minecraft **/
enum class DefaultColor(val adventure: TextColor) {
	BLACK(NamedTextColor.BLACK),
	DARK_BLUE(NamedTextColor.DARK_BLUE),
	DARK_GREEN(NamedTextColor.DARK_GREEN),
	DARK_AQUA(NamedTextColor.DARK_AQUA),
	DARK_RED(NamedTextColor.DARK_RED),
	DARK_PURPLE(NamedTextColor.DARK_PURPLE),
	GOLD(NamedTextColor.GOLD),
	GRAY(NamedTextColor.GRAY),
	DARK_GRAY(NamedTextColor.DARK_GRAY),
	BLUE(NamedTextColor.BLUE),
	GREEN(NamedTextColor.GREEN),
	AQUA(NamedTextColor.AQUA),
	RED(NamedTextColor.RED),
	LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE),
	YELLOW(NamedTextColor.YELLOW),
	WHITE(NamedTextColor.WHITE),
}
