package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.HeavyTurretBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.HeavyTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class HeavyTurretWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: HeavyTurretMultiblock
) : TurretWeaponSubsystem<HeavyTurretBalancing, HeavyTurretBalancing.HeavyTurretProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()) {

	override fun getName(): Component {
		return Component.text("Heavy Turret")
	}
}
