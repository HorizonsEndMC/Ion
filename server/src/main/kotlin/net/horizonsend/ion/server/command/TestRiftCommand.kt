package net.horizonsend.ion.server.command

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import io.papermc.paper.advancement.AdvancementDisplay
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.environment.Environment
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementType
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemConsumeEvent
import net.minecraft.world.item.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import net.minecraft.network.chat.Component
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.random.Random

@CommandAlias("rift")
object TestRiftCommand: SLCommand() {
	@Default
	fun rift(sender: Player) {
		val worlds = IonServer.server.worlds // Returns a list of all loaded server worlds.
		val potionEffects =
			listOf( // This is a list of all effects the rift should apply. `duration` is measured in ticks.
				PotionEffect(PotionEffectType.NAUSEA, 120, 2, false),
				PotionEffect(PotionEffectType.HUNGER, 60, 1, false),
				PotionEffect(PotionEffectType.DARKNESS, 50, 2, false),
				PotionEffect(PotionEffectType.POISON, 40, 1, false)
			)

		val world =
			worlds.random() // returns a random value in the list of worlds, TODO: see if we can randomize it in the server.worlds call
		val loc = generateRandomCoordinatesInBorder(world)

		// TODO: confirm that player does not end up in a block other than air. if player is going to end up in an invalid block, teleport player to nearest viable block. MAKE SURE THE LOCATION IS WITHIN THE WORLDBORDER
		// TODO: cool particles and effects, have said effects linger in the arrival and departure for several seconds.

		for (effect in potionEffects) {
			sender.addPotionEffect(effect)
		}

		sender.teleport(loc)
		sender.gameMode = GameMode.SPECTATOR
	}

	fun findValidYCoordinate(world: World, side: Double): Double {
		val minHeight = world.minHeight
		val maxHeight = world.maxHeight
		val t = {
			for (y in minHeight..maxHeight) {
				if (world.getBlockAt(
						Location(
							world,
							side,
							y.toDouble(),
							side
						)
					).blockData != Material.AIR.createBlockData()
				) y.toDouble() else continue
			}
		}
		return 120.0
	}

	fun generateRandomCoordinatesInBorder(world: World): Location {
		val borderCenter = world.worldBorder.center
		val minSquare = (borderCenter.x - (world.worldBorder.size / 2)).toInt() // gets us negative side
		val maxSquare = (borderCenter.x + (world.worldBorder.size / 2)).toInt() // gets us positive side

		val side = (minSquare..maxSquare).random() // can be used for both x and z
		val y = findValidYCoordinate(world, side.toDouble())

		val loc = Location(
			world,
			side.toDouble(),
			y,
			side.toDouble()
		)

		IonServer.logger.warning("$minSquare, $maxSquare, $side, $loc")
		return loc // design: from center of square, measure size, calculate area, make a range (1.0..50000.0) of valid x and y values
	}
	// PlayerItemConsumeEvent for cheth
}
