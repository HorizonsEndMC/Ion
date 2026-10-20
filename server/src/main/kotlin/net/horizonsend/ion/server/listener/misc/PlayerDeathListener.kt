package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.misc.MultimeterItem
import net.horizonsend.ion.server.features.custom.items.misc.PackagedMultiblock
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.features.custom.items.type.food.FoodItem
import net.horizonsend.ion.server.features.custom.items.type.tool.Battery
import net.horizonsend.ion.server.features.custom.items.type.tool.CratePlacer
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerChainsaw
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerHoe
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.isFiveDollar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.damage.DamageType
import org.bukkit.entity.Bat
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import kotlin.math.roundToInt

object PlayerDeathListener : SLEventListener() {
	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!ConfigurationFiles.serverConfiguration().crossServerDeathMessages) return

		val message = event.deathMessage()
		message?.let {
			Notify.chatAndGlobal(message)
			event.deathMessage(null)
		}
	}

	@EventHandler
	fun onPlayerDeathAboveMaxHeight(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (event.damageSource.damageType != DamageType.OUT_OF_WORLD) return

		val player = event.player
		if (player.location.y < player.location.world.maxHeight + 64) return

		event.deathMessage(Component.text("${player.name} ascended above the world"))
	}

	@EventHandler
	fun onPlayerDeathInTutorialWorld(event: PlayerDeathEvent) {
		if (event.isCancelled) return

		if (!event.player.world.hasFlag(WorldFlag.TUTORIAL_WORLD)) return

		event.drops.clear()
		event.keepInventory = true

		event.player.inventory.removeItemAnySlot(CustomItemKeys.CHETHERITE.getValue().constructItemStack())
		event.player.inventory.addItem(CustomItemKeys.CHETHERITE.getValue().constructItemStack(4))

		// Restart tutorial on death
		SequenceManager.endSequence(event.player, SequenceKeys.TUTORIAL.getValue())
		SequenceManager.endSequence(event.player, SequenceKeys.TUTORIAL_TRANSIT_HUB.getValue())
		SequenceManager.clearSequenceData(event.player)
		SequenceManager.startPhase(event.player, SequenceKeys.TUTORIAL, SequenceKeys.TUTORIAL.getValue().firstPhase)
	}


	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerPvpDeath(event: PlayerDeathEvent){
		val victim = event.player
		val killer = event.entity.killer ?: return
		val customItem = killer.inventory.itemInMainHand.customItem ?: return
		val arena: String = if (killer.world.hasFlag(WorldFlag.ARENA)) "<#555555>[<#ffff66>Arena<#555555>]<reset> " else ""

		val name = customItem.displayName
		val victimColor = if (victim.hasMetadata("NPC")) "<#FFFFFF>" else "<#" + Integer.toHexString((PlayerCache[victim].nationOid?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

		val killerColor = "<#" + Integer.toHexString((PlayerCache[killer].nationOid?.let { Nation.findById(it) }?.color ?: 16777215)) + ">"

		val distance = killer.location.distance(victim.location)
		val verb = when(victim.isFiveDollar()){
			true-> when(customItem) {
			is EnergySword -> energySwordVerbs.random()
			is Blaster<*> -> blasterVerbs[customItem.identifier]?.random() ?: "shot"
			is PowerDrill -> powerdrillVerbs.random()
			is Wrench -> "wrenched apart"
			is MultimeterItem -> multimeterMeterVerbs.random()
			is PackagedMultiblock -> "got turned into a Chinese Safety Video"
			is FoodItem -> foodVerbs.random()
			is Battery -> multimeterMeterVerbs.random()
			is CratePlacer -> "put in their place"
			is PowerChainsaw -> chainsawVerbs.random()
			is PowerHoe -> hoeVerbs.random()
			is CustomBlockItem -> "was boxed like a fish"
			else -> "killed"
			}
			false -> "killed"
		}

		val newMessage = MiniMessage.miniMessage()
			.deserialize(
				"$arena$victimColor${victim.name}<reset> was $verb by $killerColor${killer.name}<reset> from ${distance.roundToInt()} blocks away, using "
			)
			.append(name)

		event.deathMessage(newMessage)
	}

	val energySwordVerbs = listOf(
		"cut down", "kebabed", "stabbed", "sliced", "mauled", "slain", "pierced", "slashed", "clobbered",
		"poked", "felled", "wrecked", "cleaved", "discombobulated", "bamboozled", "clowned on", "diced", "skewered",
		"trashed", "whacked", "bested", "executed", "killed", "butchered", "carved", "vanquished", "victimized",
		"dispatched", "gutted", "destroyed", "eliminated", "smoked", "neutralised", "bit", "turned into a trophy",
	)
	val blasterVerbs = mapOf<String, List<String>>(
		"BLASTER_SNIPER" to listOf("sniped", "assassinated"),
		"BLASTER_SHOTGUN" to listOf("blasted", "blasted away", "blown away"),
		"BLASTER_RIFLE" to listOf("shot", "shot down", "gunned down", "picked off"),
		"SUBMACHINE_BLASTER" to listOf("shredded", "mowed down", "bombarded"),
		"BLASTER_PISTOL" to listOf("pelted", "dunked on", "fired at", "struck", "switched down", "was beaten in a standoff"),
		"BLASTER_CANNON" to listOf("bombarded", "blown away", "blasted")
	)

	val powerdrillVerbs = listOf(
		"excavated", "drilled into", "dug in", "lobotomized"
	)

	val multimeterMeterVerbs = listOf(
		"zapped", "shocked", "thunderstruck"
	)

	val foodVerbs = listOf(
		"egged on",
		"was put into a food coma",
		"was given hyperglycemia",
		"was brought into a food fight",
		"scrambled",
		"stir fried"
	)

	val chainsawVerbs = listOf(
		"massacred",
		"cut down",
		"felled",
		"turned to timber",
		"made into mulch",
		"given a fresh haircut",
		"trimmed"
	)

	val hoeVerbs = listOf(
		"reaped",
		"tilled",
		"put in the dirt",
		"harvested",
		"weeded out",
		"whacked",
		"trimmed",
		"pruned"
	)
}
