package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.AutocannonMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class AutocannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: AutocannonMultiblock
) : TurretWeaponSubsystem<AutocannonBalancing, AutocannonBalancing.AutocannonProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(AutocannonWeaponSubsystem::class)) {

	override fun getName(): Component {
		return Component.text("Autocannon")
	}
}
