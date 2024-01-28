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
		"<$HE_MEDIUM_GRAY>Commissions and cargo trading are great ways for new players to make money. Feeling confident? Try your hand at a /bounty.",
		"<$HE_MEDIUM_GRAY>Need an item? Use /bazaar browse, although be aware: the prices are quadrupled if you're buying remotely!",
		"<$HE_MEDIUM_GRAY>24 siegeable stations exist in space. Only nations can siege stations, and owning between 1-6 stations reaps benefits for all nation members.",
		"<$HE_MEDIUM_GRAY>Just starting out? Try visiting the planets of Luxiterna or Aret. Both have valuable resources for new players.",
		"<$HE_MEDIUM_GRAY>Level 60 is currently the highest level required to unlock the biggest available ship classes: Destroyers & Heavy Freighters (12,000 blocks).",
		"<$HE_MEDIUM_GRAY>Want to pick a fight with AI ships? It's recommended you take a gunship, at least.",
		"<$HE_MEDIUM_GRAY>Looking for a settlement or nation to join? Be sure to ask in global chat using /g. There are always players recruiting.",
		"<$HE_MEDIUM_GRAY>Setting up machinery? Solar panels and Gas generators are clean and reliable. Do /wiki to learn more.",
		"<$HE_MEDIUM_GRAY>Don't forget to enable your ship lock when parking in territory that's not your own. You can toggle the lock in the ship computer (jukebox).",
		"<$HE_MEDIUM_GRAY>Use /starship when piloting a ship to get a diagnostic overview of your vessel. This is quite useful for ship building and repairs.",
		"<$HE_MEDIUM_GRAY>Unsure who someone is? Use /pinfo [player].",
		"<$HE_MEDIUM_GRAY>Keep in mind: You're only able to buy crates from the same trade city once every 8 hours.",
		"<$HE_MEDIUM_GRAY>Stuck in hyperspace? Commissions broken? Feel free to request help from a Helper or Moderator at any time.",
		"<$HE_MEDIUM_GRAY>Don't know how to build a multiblock? Look for it using /wiki.",
		"<$HE_MEDIUM_GRAY>You can switch to the Creative server by doing /server Creative.",
		"<$HE_MEDIUM_GRAY>Need a certain ore that you can't seem to find? Look at the star systems on /wiki.",
		"<$HE_MEDIUM_GRAY>You can search for specific items in containers using /itemsearch.",
		"<$HE_MEDIUM_GRAY>You can use /ac, /nc, and /sc to switch between ally, nation, and settlement chat.",
		"<$HE_MEDIUM_GRAY>It is highly recommended to use the /map while you navigate through space.",
		"<$HE_MEDIUM_GRAY>Trade cities act as safezones: both pvp and svs (ship vs. ship) are disabled in these regions.",
		"<$HE_MEDIUM_GRAY>Building a speeder (25-100 blocks) is an incredibly fast and easy way to travel around your base. Speeders work planet-side only.",
		"<$HE_MEDIUM_GRAY>Both asteroid mining and planet mining are great ways to amass resources efficiently.",
		"<$HE_MEDIUM_GRAY>Want to fly without a ship? Attach a rocket-boosting module to your power boots.",
		"<$HE_MEDIUM_GRAY>Shot down or lost your vessel? You can always purchase a new starter ship by visiting the ship dealer at any trade city.",
		"<$HE_MEDIUM_GRAY>Not all players are friendly. Be aware of your surroundings with /near.",
		"<$HE_MEDIUM_GRAY>Pickaxes are for boomers. Use a power drill!",
		"<$HE_MEDIUM_GRAY>You can always use /kit controller if you're in need of one on the go.",
		"<$HE_MEDIUM_GRAY>Building a mob defender is a cheap way to protect your home from unwanted visitors.",
		"<$HE_MEDIUM_GRAY>Pro Tip: Ship factories can also be used for printing buildings and infrastructure."

		// expand list here
	)

	private val prefix = bracketed(text("Horizon's End", HE_LIGHT_GRAY, TextDecoration.BOLD))

	private const val INTERVAL = 10L * 60L

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
