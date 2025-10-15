package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.TriTurretBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TriTurretMultiblock
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class TriTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: TriTurretMultiblock
) : TurretWeaponSubsystem<TriTurretBalancing, TriTurretBalancing.TriTurretProjectileBalancing>(ship, pos, face, ship.balancingManager.getSubsystemSupplier(TriTurretWeaponSubsystem::class)), HeavyWeaponSubsystem, AutoWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	override val range: Double get() = multiblock.getRange(starship)

	override fun autoFire(target: AutoTurretTarget<*>, dir: Vector) {
		multiblock.shoot(starship.world, pos, face, dir, starship, starship.controller.damager, this)
	}

	override fun getName(): Component {
		return Component.text("Tri Turret")
	}
}
