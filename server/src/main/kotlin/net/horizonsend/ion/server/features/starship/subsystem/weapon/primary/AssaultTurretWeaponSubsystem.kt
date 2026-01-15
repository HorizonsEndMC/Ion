package net.horizonsend.ion.server.features.starship.subsystem.weapon.primary

import net.horizonsend.ion.server.configuration.starship.AssaultTurretBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.AssaultTurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack

class AssaultTurretWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace,
	override val multiblock: AssaultTurretMultiblock
) : TurretWeaponSubsystem<AssaultTurretBalancing, AssaultTurretBalancing.AssaultTurretProjectileBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier(AssaultTurretWeaponSubsystem::class))
, AmmoConsumingWeaponSubsystem{

	override fun getName(): Component {
		return Component.text("Assault Turret")
	}
	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemKeys.LOADED_SHELL.getValue(), 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}
