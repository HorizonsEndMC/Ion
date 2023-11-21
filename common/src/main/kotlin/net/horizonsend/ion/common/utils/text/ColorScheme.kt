package net.horizonsend.ion.common.utils.text

import net.kyori.adventure.text.format.TextColor

/** Standard HE color scheme */
class HEColorScheme(override val adventure: TextColor): PresetColor {
	companion object {
		val HE_LIGHT_GRAY = HEColorScheme(TextColor.fromHexString("#E1E1E1")!!)
		val HE_LIGHT_BLUE = HEColorScheme(TextColor.fromHexString("#D8D7D7")!!)
		val HE_LIGHT_ORANGE = HEColorScheme(TextColor.fromHexString("#F2AE67")!!)
		val HE_MEDIUM_GRAY = HEColorScheme(TextColor.fromHexString("#768A8A")!!)
		val HE_DARK_GRAY = HEColorScheme(TextColor.fromHexString("#414C4C")!!)
	}
}

interface PresetColor : TextColor {
	val adventure: TextColor

	fun rgb(): Int = adventure.value()
	fun hexString(): String = adventure.asHexString()
	override fun value(): Int = adventure.value()
}
