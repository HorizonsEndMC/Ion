package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.starship.RapidHeavyMissileLauncherBalancing
import net.horizonsend.ion.server.configuration.starship.RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event.RapidHeavyMissileLauncherMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class RapidHeavyMissileLauncherWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: RapidHeavyMissileLauncherMultiblock
) : TurretWeaponSubsystem<RapidHeavyMissileLauncherBalancing, RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing>(ship, pos, face, ship.balancingManager.getWeaponSupplier(RapidHeavyMissileLauncherWeaponSubsystem::class)), ManualWeaponSubsystem,
	DirectionalSubsystem,
	AmmoConsumingWeaponSubsystem {
	override val fireCooldownNanos: Long get() = balancing.fireCooldownNanos
	override fun getName(): Component {
		return Component.text("RHML")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.HEAVY_MISSILE.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}
