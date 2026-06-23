package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.common.database.ColorDB
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color

/**
 * Legacy SLTextStyle enum names mapped to RGB values
 */
private val LEGACY_COLOR_MAP = mapOf(
	"AQUA" to Color.fromRGB(85, 255, 255),
	"BLACK" to Color.fromRGB(0, 0, 0),
	"BLUE" to Color.fromRGB(85, 85, 255),
	"DARK_AQUA" to Color.fromRGB(0, 170, 170),
	"DARK_BLUE" to Color.fromRGB(0, 0, 170),
	"DARK_GRAY" to Color.fromRGB(85, 85, 85),
	"DARK_GREEN" to Color.fromRGB(0, 170, 0),
	"DARK_PURPLE" to Color.fromRGB(170, 0, 170),
	"DARK_RED" to Color.fromRGB(170, 0, 0),
	"GOLD" to Color.fromRGB(255, 170, 0),
	"GRAY" to Color.fromRGB(170, 170, 170),
	"GREEN" to Color.fromRGB(85, 255, 85),
	"LIGHT_PURPLE" to Color.fromRGB(255, 85, 255),
	"RED" to Color.fromRGB(255, 85, 85),
	"WHITE" to Color.fromRGB(255, 255, 255),
	"YELLOW" to Color.fromRGB(255, 255, 85)
)

/**
 * Convert ColorDB (format: "R,G,B") to Bukkit Color
 * Also handles legacy SLTextStyle enum names for backward compatibility
 */
fun ColorDB.toBukkitColor(): Color {
	// Check if this is a legacy enum name
	LEGACY_COLOR_MAP[this]?.let { return it }

	// Parse as RGB format
	val parts = this.split(",")
	require(parts.size == 3) { "Invalid color format: $this. Expected format: R,G,B or legacy color name" }
	val r = parts[0].toIntOrNull() ?: error("Invalid red value: ${parts[0]}")
	val g = parts[1].toIntOrNull() ?: error("Invalid green value: ${parts[1]}")
	val b = parts[2].toIntOrNull() ?: error("Invalid blue value: ${parts[2]}")
	require(r in 0..255) { "Red value must be 0-255, got $r" }
	require(g in 0..255) { "Green value must be 0-255, got $g" }
	require(b in 0..255) { "Blue value must be 0-255, got $b" }
	return Color.fromRGB(r, g, b)
}

/**
 * Convert Bukkit Color to ColorDB format (R,G,B)
 */
fun Color.toColorDB(): ColorDB = "${this.red},${this.green},${this.blue}"

/**
 * Convert ColorDB to Kyori TextColor for Adventure API
 */
fun ColorDB.toTextColor(): TextColor {
	val color = this.toBukkitColor()
	return TextColor.color(color.red, color.green, color.blue)
}

/**
 * Create ColorDB from RGB values
 */
fun colorDB(red: Int, green: Int, blue: Int): ColorDB {
	require(red in 0..255) { "Red value must be 0-255, got $red" }
	require(green in 0..255) { "Green value must be 0-255, got $green" }
	require(blue in 0..255) { "Blue value must be 0-255, got $blue" }
	return "$red,$green,$blue"
}

/**
 * Migrate legacy SLTextStyle color name to RGB format
 * Returns the input if already in RGB format
 */
fun migrateLegacyColor(colorDB: ColorDB): ColorDB {
	// If it's already in RGB format, return as-is
	if (colorDB.contains(",")) return colorDB

	// Convert legacy name to RGB
	val color = LEGACY_COLOR_MAP[colorDB] ?: return colorDB
	return color.toColorDB()
}



