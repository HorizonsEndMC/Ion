package net.horizonsend.ion.common.utils.text

import kotlin.math.pow

object Colors {
	const val SERVER_ERROR = 0xff3f3f
	const val USER_ERROR = 0xff7f3f
	const val ALERT = SERVER_ERROR
	const val INFORMATION = 0x7f7fff
	const val SPECIAL = 0xffd700
	const val SUCCESS = 0x3fff3f
	const val HINT = 0x7f7f7f

	enum class ColorSafety {
		TOO_BRIGHT,
		ACCEPTABLE,
		TOO_DARK
	}

	fun calculateColorSafety(red: Int, green: Int, blue: Int): ColorSafety {
		val perceivedLightness = calculatePerceivedLightness(red, green, blue)
		if (perceivedLightness > 0.8) return ColorSafety.TOO_BRIGHT
		if (perceivedLightness < 0.4) return ColorSafety.TOO_DARK
		return ColorSafety.ACCEPTABLE
	}

	/**
	 * Calculates the "perceived lightness" of the color, that being how bright the color appears to the human eye.
	 *
	 * Based on: https://stackoverflow.com/questions/56678483
	 * @return Perceived lightness on a scale of 0 to 1, where 0 is pure black and 1 is pure white.
	 */
	private fun calculatePerceivedLightness(red: Int, green: Int, blue: Int): Double {
		val decimalRed = red / 255.0
		val decimalGreen = green / 255.0
		val decimalBlue = blue / 255.0

		val linearRed = if (decimalRed <= 0.04045) decimalRed / 12.92 else ((decimalRed + 0.055) / 1.055).pow(2.4)
		val linearGreen = if (decimalGreen <= 0.04045) decimalGreen / 12.92 else ((decimalGreen + 0.055) / 1.055).pow(2.4)
		val linearBlue = if (decimalBlue <= 0.04045) decimalBlue / 12.92 else ((decimalBlue + 0.055) / 1.055).pow(2.4)

		val luminance = 0.2126 * linearRed + 0.7152 * linearGreen + 0.0722 * linearBlue

		val perceivedLightness = if (luminance <= (216.0 / 24389.0)) luminance * (24389.0 / 27.0) else luminance.pow(1.0 / 3.0) * 116 - 16

		return perceivedLightness / 100.0
	}

}
