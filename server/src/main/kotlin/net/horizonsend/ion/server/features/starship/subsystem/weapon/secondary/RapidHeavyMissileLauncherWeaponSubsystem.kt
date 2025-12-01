package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.RapidHeavyMissileLauncherBalancing
import net.horizonsend.ion.server.configuration.starship.RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.RapidHeavyMissileLauncherMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace

class RapidHeavyMissileLauncherWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: RapidHeavyMissileLauncherMultiblock
) : TurretWeaponSubsystem<RapidHeavyMissileLauncherBalancing, RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing>(ship, pos, face, ship.balancingManager.getWeaponSupplier(RapidHeavyMissileLauncherWeaponSubsystem::class)), HeavyWeaponSubsystem {
	override val boostChargeNanos: Long get() = balancing.boostChargeNanos

	override fun getName(): Component {
		return Component.text("RHML")
	}
}
