package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.LogisticsTurretBalancing
import net.horizonsend.ion.server.configuration.starship.LogisticsTurretBalancing.LogisticsTurretProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.LogisticTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class LogisticTurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace,
    override val multiblock: LogisticTurretMultiblock
) : TurretWeaponSubsystem<LogisticsTurretBalancing, LogisticsTurretProjectileBalancing>(ship, pos, face, ship.balancingManager.getWeaponSupplier(LogisticTurretWeaponSubsystem::class)) {
    override val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyRadians)
    override var fireCooldownNanos: Long = balancing.fireCooldownNanos
    override fun getMaxPerShot(): Int = balancing.maxPerShot

	override fun getName(): Component {
		return Component.text("Logistics Turret")
	}
}
