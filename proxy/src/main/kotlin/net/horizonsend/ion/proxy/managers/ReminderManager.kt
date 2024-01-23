package net.horizonsend.ion.proxy.managers

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.proxy.PLUGIN
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
import java.util.concurrent.TimeUnit

object ReminderManager : IonComponent() {
	private val messages = listOf(
		"<$HE_MEDIUM_GRAY>Low on <dark_purple>chetherite</dark_purple>? Get your votes in, today! /vote",
		"<$HE_MEDIUM_GRAY>Be sure to visit Prometheus Station to be able to buy or sell up to <gold>30,000C</gold> worth of crops per day.",
		"<$HE_MEDIUM_GRAY>Bed not working? Just got blasted out of oblivion? Use /kit starter to build a cryopod.",
		"<$HE_MEDIUM_GRAY>Commissions and cargo trading are great ways for new players to make money. Feeling confident? Try your hand at /bounty.",
		"<$HE_MEDIUM_GRAY>Need an item? Use /bazaar browse, although be aware: the prices are quadrupled if you're buying remotely!",
		"<$HE_MEDIUM_GRAY>24 siegeable stations exist in space. Only nations can siege stations, and owning between 1-6 stations reaps benefits for all nation members.",
		"<$HE_MEDIUM_GRAY>Just starting out? Try visiting the planets of Luxiterna or Aret. Both have resources valuable for new players.",
		"<$HE_MEDIUM_GRAY>Level 60 is currently the highest level required to unlock the biggest available ship class: Destroyers / Heavy Frighteners (12,000 blocks).",
		"<$HE_MEDIUM_GRAY>Want to pick a fight with NPCs? It's recommended you take a gunship, at least.",
		"<$HE_MEDIUM_GRAY>Looking for a settlement or nation to join? Be sure to ask in global chat using /g. There are always players recruiting.",
		"<$HE_MEDIUM_GRAY>Setting up machinery? Solar power and Gas generators are clean and reliable. Do /wiki to learn more.",
		"<$HE_MEDIUM_GRAY>Don't forget to enable your ship lock when parking in territory that's not your own. The lock is toggleable in the ship computer (jukebox).",
		"<$HE_MEDIUM_GRAY>Use /starship when piloting a ship to get a diagnostic overview of your vessel. This is quite useful for shipbuilding and repairs.",
		"<$HE_MEDIUM_GRAY>Unsure who someone is? Use /pinfo [player]."

		// expand list here
	)

	private val prefix = bracketed(text("Horizon's End", HE_LIGHT_GRAY, TextDecoration.BOLD))

	private const val INTERVAL = 5L * 60L

	override fun onEnable() {
		PLUGIN.proxy.scheduler.repeat(INTERVAL, INTERVAL, TimeUnit.SECONDS, ::sendBroadcast)
	}

	private var index = 0

	private fun sendBroadcast() {
		val message = messages.getOrNull(index) ?: return

		val formatted = ofChildren(prefix, text(" » ", HE_DARK_GRAY), miniMessage.deserialize(message))

		PLUGIN.proxy.sendMessage(formatted)

		index = (index + 1) % messages.size
	}
}
