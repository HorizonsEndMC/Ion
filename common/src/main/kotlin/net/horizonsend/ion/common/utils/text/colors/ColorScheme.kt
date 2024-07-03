package net.horizonsend.ion.common.utils.text.colors

import net.kyori.adventure.text.format.TextColor

/** Standard HE color scheme */
class HEColorScheme(override val adventure: TextColor): PresetColor {
	companion object {
		val HE_LIGHT_GRAY = HEColorScheme(TextColor.fromHexString("#E1E1E1")!!)
		val HE_LIGHT_BLUE = HEColorScheme(TextColor.fromHexString("#B8D7D7")!!)
		val HE_LIGHT_ORANGE = HEColorScheme(TextColor.fromHexString("#F2AE67")!!)
		val HE_DARK_ORANGE = HEColorScheme(TextColor.fromHexString("#CE6F0E")!!)
		val HE_MEDIUM_GRAY = HEColorScheme(TextColor.fromHexString("#768A8A")!!)
		val HE_DARK_GRAY = HEColorScheme(TextColor.fromHexString("#414C4C")!!)
	}

	override fun toString(): String = asHexString()
}

interface PresetColor : TextColor {
	val adventure: TextColor

	override fun value(): Int = adventure.value()

	companion object {
		fun presetColor(value: String): PresetColor = object : PresetColor {
			override val adventure: TextColor = TextColor.fromHexString(value)!!
		}
	}
}
