package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.hyperspace.HyperspaceMovement
import net.horizonsend.ion.server.features.starship.movement.RotationMovement
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.StarshipSubsystem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distance
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.persistence.PersistentDataType
import kotlin.math.roundToInt

class OdometerSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace
) : StarshipSubsystem(starship, pos), DirectionalSubsystem {
	override fun isIntact(): Boolean {
		// not worth
		return true
	}

	private fun getSign() = getBlockIfLoaded(starship.world, pos.x, pos.y, pos.z)?.state as? Sign

	override fun onMovement(movement: StarshipMovement) = Tasks.sync {
		if (movement.newWorld != null) return@sync
		if (movement is RotationMovement) return@sync

		val sign = getSign() ?: return@sync

		val originX = 0
		val originY = 0
		val originZ = 0

		val newX = movement.displaceX(originX, originZ)
		val newY = movement.displaceY(originY)
		val newZ = movement.displaceZ(originZ, originX)

		val diff = distance(originX, originY, originZ, newX, newY, newZ)

		val pdc = sign.persistentDataContainer
		val old = pdc.getOrDefault(NamespacedKeys.BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)
		val oldHyper = pdc.getOrDefault(NamespacedKeys.HYPERSPACE_BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)
		val total = old + diff

		sign.isWaxed = true
		pdc.set(NamespacedKeys.BLOCKS_TRAVELED, PersistentDataType.DOUBLE, total)
		sign.getSide(Side.FRONT).line(1, template(text("Blocks{0} {1}", HE_LIGHT_GRAY), text(':', HE_MEDIUM_GRAY), text(total.roundToInt(), HE_LIGHT_BLUE)))
		sign.getSide(Side.FRONT).line(3, template(text("Total{0} {1}", HE_LIGHT_GRAY), text(':', HE_MEDIUM_GRAY), text((total + oldHyper).roundToInt(), HE_LIGHT_BLUE)))
		sign.update()
	}

	override fun handleJump(jump: HyperspaceMovement) = Tasks.sync {
		if (jump.originWorld != jump.dest.world) return@sync

		val sign = getSign() ?: return@sync

		val distance = distance(jump.originX, 0.0, jump.originZ, jump.dest.x, 0.0, jump.dest.z)

		val pdc = sign.persistentDataContainer
		val old = pdc.getOrDefault(NamespacedKeys.HYPERSPACE_BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)
		val oldOverworld = pdc.getOrDefault(NamespacedKeys.BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)
		val total = old + distance

		sign.isWaxed = true
		pdc.set(NamespacedKeys.HYPERSPACE_BLOCKS_TRAVELED, PersistentDataType.DOUBLE, total)
		sign.getSide(Side.FRONT).line(2, template(text("Hyperspace{0} {1}", HE_LIGHT_GRAY), text(':', HE_MEDIUM_GRAY), text(total.roundToInt(), HE_LIGHT_BLUE)))
		sign.getSide(Side.FRONT).line(3, template(text("Total{0} {1}", HE_LIGHT_GRAY), text(':', HE_MEDIUM_GRAY), text((total + oldOverworld).roundToInt(), HE_LIGHT_BLUE)))
		sign.update()
	}
}
