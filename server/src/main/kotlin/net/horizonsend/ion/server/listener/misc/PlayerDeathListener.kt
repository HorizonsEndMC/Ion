package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.custom.items.type.tool.PowerDrill
import net.horizonsend.ion.server.features.custom.items.type.weapon.blaster.Blaster
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.damage.DamageType
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
		val verb = when(customItem){
			is EnergySword-> energySwordVerbs.random()
			is Blaster<*> -> blasterVerbs[customItem.identifier]?.random() ?: "shot"
			is PowerDrill -> "drilled into"
			is Wrench -> "wrenched apart"
			else -> "killed"
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
		"trashed", "whacked", "bested", "executed", "knocked out", "killed", "butchered", "carved", "vanquished",
		"dispatched", "gutted", "destroyed", "eliminated", "smoked", "neutralised", "bit"
	)
	val blasterVerbs = mapOf<String, List<String>>(
		"BLASTER_SNIPER" to listOf("sniped", "assassinated"),
		"BLASTER_SHOTGUN" to listOf("blasted", "blasted away", "blown away"),
		"BLASTER_RIFLE" to listOf("shot", "shot down", "gunned down", "picked off"),
		"SUBMACHINE_BLASTER" to listOf("shredded", "mowed down", "bombarded"),
		"BLASTER_PISTOL" to listOf("pelted", "dunked on", "fired at", "struck"),
		"BLASTER_CANNON" to listOf("bombarded", "blown away", "blasted")
	)
}
