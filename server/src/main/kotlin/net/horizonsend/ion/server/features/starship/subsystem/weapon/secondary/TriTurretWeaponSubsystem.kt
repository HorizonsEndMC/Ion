package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret.TriTurretMultiblock
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TriTurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: TriTurretMultiblock
) : TurretWeaponSubsystem(ship, pos, face), HeavyWeaponSubsystem, AutoWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.triTurret
	override val inaccuracyRadians: Double = Math.toRadians(balancing.inaccuracyRadians)
	override val powerUsage: Int = balancing.powerUsage
	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

	override val range: Double get() = multiblock.getRange(starship)

	override fun autoFire(target: AutoTurretTarget<*>, dir: Vector) {
		multiblock.shoot(starship.world, pos, face, dir, starship, starship.controller.damager, this)
	}

	override fun getName(): Component {
		return Component.text("Tri Turret")
	}
}
