package net.horizonsend.ion.server.features.multiblock.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.server.features.multiblock.InteractableMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SubsystemMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.misc.OdometerSubsystem
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

object OdometerMultiblock : Multiblock(), SubsystemMultiblock<OdometerSubsystem>, InteractableMultiblock {
	override val name: String = "odometer"
	override val signText: Array<Component?> = arrayOf(
		text("Ship Odometer", AQUA),
		null,
		null,
		null
	)

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): OdometerSubsystem {
		return OdometerSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(-1, 0, 0).anyWall()
		at(0, 0, 0).anyGlass()
		at(1, 0, 0).anyWall()
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val pdc = sign.persistentDataContainer
		val dist = pdc.getOrDefault(NamespacedKeys.BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)
		val hyperDist = pdc.getOrDefault(NamespacedKeys.HYPERSPACE_BLOCKS_TRAVELED, PersistentDataType.DOUBLE, 0.0)

		player.information("Overworld blocks traveled: ${dist.roundToHundredth()}")
		player.information("Hyperspace blocks traveled: ${hyperDist.roundToHundredth()}")
		player.information("Total blocks traveled: ${(dist + hyperDist).roundToHundredth()}")
	}
}
