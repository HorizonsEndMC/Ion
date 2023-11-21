package net.horizonsend.ion.common

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

/** Wrapper enum for the 16 colors of minecraft **/
enum class PresetColor(val kyori: TextColor) {
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

    // Standard HE color scheme
    HE_LIGHT_GRAY(TextColor.fromHexString("#E1E1E1")!!),
    HE_LIGHT_BLUE(TextColor.fromHexString("#D8D7D7")!!),
    HE_LIGHT_ORANGE(TextColor.fromHexString("#F2AE67")!!),
    HE_MEDIUM_GRAY(TextColor.fromHexString("#607070")!!),
    HE_DARK_GRAY(TextColor.fromHexString("#414C4C")!!)

    ;

    val rgb: Int = kyori.value()
    val hexString: String = kyori.asHexString()
}
