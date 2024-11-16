package net.horizonsend.ion.server.features.starship.control.controllers.player

import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.horizonsend.ion.server.features.starship.damager.damager
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.round

abstract class PlayerController(
	val player: Player,
	starship: ActiveStarship, name: String
) : Controller(player.damager(), starship, name) {
	override val yaw: Float get() = player.location.yaw
	override val pitch: Float get() = player.location.pitch

	override fun getColor(): Color {
		if (starship.rainbowToggle) {
			val random = ThreadLocalRandom.current()
			return Color.fromRGB(random.nextInt(50, 255), random.nextInt(50, 255), random.nextInt(50, 255))
		}

		return PlayerCache[player].nationOid?.let { Color.fromRGB( NationCache[it].color ) } ?: super.getColor()
	}

	override fun canDestroyBlock(block: Block): Boolean = BlockBreakEvent(block, player).callEvent()

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block) =
		BlockPlaceEvent(block, block.state, placedAgainst, player.activeItem, player, true, EquipmentSlot.HAND).callEvent()

	override fun audience(): Audience = player

	override val pilotName: Component get() = player.displayName()

	override fun directControlMovementVector(direction : BlockFace): Vector {
		// Use the player's location
		val pilotLocation = player.location

		var center = starship.directControlCenter
		if (center == null) {
			center = pilotLocation.toBlockLocation().add(0.5, 0.0, 0.5)
			starship.directControlCenter = center
		}

		// Calculate the movement vector
		var vector = pilotLocation.toVector().subtract(center.toVector())
		vector.setY(0)
		vector.normalize()

		// Clone the vector to do some additional math
		val directionWrapper = center.clone()
		directionWrapper.direction = Vector(direction.modX, direction.modY, direction.modZ)

		val playerDirectionWrapper = center.clone()
		playerDirectionWrapper.direction = pilotLocation.direction

		val vectorWrapper = center.clone()
		vectorWrapper.direction = vector

		vectorWrapper.yaw = vectorWrapper.yaw - (playerDirectionWrapper.yaw - directionWrapper.yaw)
		vector = vectorWrapper.direction

		vector.x = round(vector.x)
		vector.setY(0)
		vector.z = round(vector.z)
		return  vector
	}

	override fun toString(): String {
		return "$name [${player.name}]"
	}
}
