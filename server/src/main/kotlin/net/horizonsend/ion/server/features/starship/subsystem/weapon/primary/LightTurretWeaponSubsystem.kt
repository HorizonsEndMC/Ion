package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.LightTurretBalancing
import net.horizonsend.ion.server.configuration.starship.LightTurretBalancing.LightTurretProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.LightTurretMultiblock
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting.AutoTurretTarget
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class LightTurretWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: LightTurretMultiblock
) : TurretWeaponSubsystem<LightTurretBalancing, LightTurretProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()), AutoWeaponSubsystem {
	override val range: Double get() = balancing.range

	override fun autoFire(target: AutoTurretTarget<*>, dir: Vector) {
		multiblock.shoot(starship.world, pos, face, dir, starship, starship.controller.damager, this)
	}

	override fun getName(): Component {
		return text("Light Turret")
	}
}
