package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.QuadTurretBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.QuadTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class QuadTurretWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: QuadTurretMultiblock
) : TurretWeaponSubsystem<QuadTurretBalancing, QuadTurretBalancing.QuadTurretProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(QuadTurretWeaponSubsystem::class)), PermissionWeaponSubsystem, AmmoConsumingWeaponSubsystem {
	override val permission: String = "ion.multiblock.quadturret"

	override fun getName(): Component {
		return Component.text("Quad Turret")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.LOADED_SHELL.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}
