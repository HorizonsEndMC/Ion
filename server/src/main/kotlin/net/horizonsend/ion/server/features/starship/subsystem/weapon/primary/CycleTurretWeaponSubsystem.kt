package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.StarshipWeapons.CycleTurretBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.CycleTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class CycleTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: CycleTurretMultiblock
) : TurretWeaponSubsystem<CycleTurretBalancing, CycleTurretBalancing.CycleTurretProjectileBalancing>(ship, pos, face, ship.balancingManager.getSupplier()) {
    override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
    override fun getMaxPerShot(): Int = balancing.maxPerShot

	override fun getName(): Component {
		return Component.text("Cycle Turret")
	}
}
