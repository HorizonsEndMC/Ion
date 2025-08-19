package net.horizonsend.ion.proxy.features

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
		"<$HE_MEDIUM_GRAY>Low on <dark_purple>chetherite</dark_purple>? Get your /vote in, today!",
		"<$HE_MEDIUM_GRAY>Be sure to visit Prometheus Station to be able to buy or sell up to <gold>60,000C</gold> worth of crops per day.",
		"<$HE_MEDIUM_GRAY>Bed not working? Just got blasted out of oblivion? Use /kit starter to build a cryopod.",
		"<$HE_MEDIUM_GRAY>Commissions and cargo trading are great ways for new players to make money. Feeling confident? Try your hand at a /bounty.",
		"<$HE_MEDIUM_GRAY>Need an item? Use /bazaar browse, although be aware: the prices are quadrupled if you're buying remotely!",
		"<$HE_MEDIUM_GRAY>24 siegeable stations exist in space. Only nations can siege stations, and owning between 1-6 stations reaps benefits for all nation members.",
		"<$HE_MEDIUM_GRAY>Just starting out? Try visiting the planets of Luxiterna or Aret. Both have valuable resources for new players.",
		"<$HE_MEDIUM_GRAY>Level 80 is currently the highest level required to unlock the biggest available ship classes: Battlecruisers & Barges (20,000 blocks).",
		"<$HE_MEDIUM_GRAY>Want to pick a fight with AI ships? It's recommended you take on easy ai first.",
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
		"<$HE_MEDIUM_GRAY>Building a speeder (25-100 blocks) is an incredibly fast and easy way to travel around your base.",
		"<$HE_MEDIUM_GRAY>Both asteroid mining and wreck salvaging are great ways to amass resources efficiently.",
		"<$HE_MEDIUM_GRAY>Want to fly without a ship? Attach a rocket-boosting module to your power boots.",
		"<$HE_MEDIUM_GRAY>Shot down or lost your vessel? You can always purchase a new starter ship by visiting the ship dealer at any trade city.",
		"<$HE_MEDIUM_GRAY>Not all players are friendly. Be aware of your surroundings with /near.",
		"<$HE_MEDIUM_GRAY>Pickaxes are for boomers. Use a power drill!",
		"<$HE_MEDIUM_GRAY>You can always use /kit controller if you're in need of one on the go.",
		"<$HE_MEDIUM_GRAY>Building a mob defender is a cheap way to protect your home from unwanted visitors.",
		"<$HE_MEDIUM_GRAY>Pro Tip: Ship factories can also be used for printing buildings and infrastructure.",
		"<$HE_MEDIUM_GRAY>AI comes in five difficulties, a Dagger in the Trench will hit harder than one in Asteri!",
		"<$HE_MEDIUM_GRAY>Looking for a challenging fight to do with friends? Use /encounterstatus to find upcoming AI Loci!.",
		"<$HE_MEDIUM_GRAY>If you need more practice in SvS, /aiopponent in /server creative is a good place to start, but does not beat field experience.",
		"<$HE_MEDIUM_GRAY>Convoys and Patrols are fleets of ships that travel the server. Expect a challenging fight and big rewards.",
		"<$HE_MEDIUM_GRAY>AI Loci offer increase spawning rates and are a great way to level up. Start easy with Bootcamps, or challenge yourself with Unknown Signals.",
		"<$HE_MEDIUM_GRAY>Privateer patrols expect you to be on your best behavior when they come visit. Players with high bounties may be attacked on sight!",
		"<$HE_MEDIUM_GRAY>The /settings command is your go to for tweaking your experience to suit your needs.",
		"<$HE_MEDIUM_GRAY>Want to adjust your contacts bar? Use /settings",
		"<$HE_MEDIUM_GRAY>Hard time viewing some effects? Use /settings",
		"<$HE_MEDIUM_GRAY>Remember to /vote for the server and earn 24 <dark_purple>chetherite</dark_purple> and <gold>1,000C</gold> daily!",
		"<$HE_MEDIUM_GRAY>Top up your supply of chetherite and credits every day with /vote!",
		"<$HE_MEDIUM_GRAY>Did you /vote yet today?",
		"<$HE_MEDIUM_GRAY>Want to grow or improve the server? Consider /vote to help attract more players, or contribute to the server at /github.",
		"<$HE_MEDIUM_GRAY>Horizon's End is open source. Contribute to the server code or resource pack at /github.",
		"<$HE_MEDIUM_GRAY>Found a bug? Squash it in #support on the /discord.",
		"<$HE_MEDIUM_GRAY>Having trouble with another player breaking the rules? Report them in #support on the /discord.",



		// expand list here
	)

	private val prefix = bracketed(text("Horizon's End", HE_LIGHT_GRAY, TextDecoration.BOLD))

	private const val INTERVAL = 10L * 60L

	override fun onEnable() {
		PLUGIN.proxy.scheduler.repeat(INTERVAL, INTERVAL, TimeUnit.SECONDS, ReminderManager::sendBroadcast)
	}

	private var index = 0

	private fun sendBroadcast() {
		val message = messages.getOrNull(index) ?: return

		val formatted = ofChildren(prefix, text(" » ", HE_DARK_GRAY), miniMessage.deserialize(message))

		PLUGIN.proxy.sendMessage(formatted)

		index = (index + 1) % messages.size
	}
}
