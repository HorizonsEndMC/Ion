package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.ACAPTurretBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.ACAPTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class ACAPTurretWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: ACAPTurretMultiblock
) : TurretWeaponSubsystem<ACAPTurretBalancing, ACAPTurretBalancing.ACAPTurretProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(ACAPTurretWeaponSubsystem::class)), PermissionWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val permission: String = "ion.multiblock.acapturret"

	override fun getName(): Component {
		return Component.text("ACAP Turret")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.LOADED_SIEGE_SHELL.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}
