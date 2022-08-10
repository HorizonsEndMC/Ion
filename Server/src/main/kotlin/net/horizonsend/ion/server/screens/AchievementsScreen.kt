package net.horizonsend.ion.server.screens

import kotlin.math.ceil
import kotlin.math.min
import net.horizonsend.ion.common.database.Achievement
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage

class AchievementsScreen(target: String, pageNumber: Int = 0) : TextScreen(buildPageText(target, pageNumber)) {
	companion object {
		private val String.minecraftLength: Int get() =
			this.sumOf {
				@Suppress("Useless_Cast")
				when (it) {
					'i', '!', ',', '.', '\'', ':' -> 2
					'l' -> 3
					'I', 't', ' ' -> 4
					'k', 'f' -> 5
					else -> 6
				} as Int
			}

		private val Int.resetCode: Char get() = (0xDFFF + this).toChar()

		private fun buildPageText(target: String, pageNumber: Int): TextComponent {
			val header = "$target's Achievements"
			var string = "<white><font:horizonsend:special>\uE007\uF8FF\uE0A8<reset>$header"

			val startIndex = pageNumber * 5

			var cursorXPosition = header.minecraftLength - 22 // We want a 22 pixel indent on the left for icon items

			for (achievementIndex in startIndex .. min(startIndex + 4, Achievement.values().size - 1)) {
				val achievement = Achievement.values()[achievementIndex]
				val y = (achievementIndex - startIndex) * 18 + 12

				val lineOne = "${achievement.title} ${achievement.creditReward}"
				val lineTwo = "${achievement.description} ${achievement.experienceReward} ${achievement.chetheriteReward}"

				string += "<font:horizonsend:special>${cursorXPosition.resetCode}"
				string += "<font:horizonsend:y${y    }>$lineOne"
				string += "<font:horizonsend:special>${lineOne.minecraftLength.resetCode}"
				string += "<font:horizonsend:y${y + 9}>$lineTwo"

				cursorXPosition = lineTwo.minecraftLength
			}

			val pageNumberString = "Page $pageNumber / ${ceil(Achievement.values().size / 5.0)}"

			string += "<font:horizonsend:special>${pageNumberString.minecraftLength.resetCode}"
			string += "<font:horizonsend:y106>$pageNumberString"

			return MiniMessage.miniMessage().deserialize(string) as TextComponent
		}
	}
}