package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.GaussCannonMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class GaussCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: GaussCannonMultiblock
) : TurretWeaponSubsystem<GaussCannonBalancing, GaussCannonBalancing.GaussCannonProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(GaussCannonWeaponSubsystem::class)) {

	override fun getName(): Component {
		return Component.text("Gauss Cannon")
	}
}
