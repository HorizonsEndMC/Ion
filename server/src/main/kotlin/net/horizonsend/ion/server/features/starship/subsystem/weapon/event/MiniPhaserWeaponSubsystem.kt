package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.starship.MiniPhaserBalancing
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.MiniPhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class MiniPhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem<MiniPhaserBalancing>(starship, pos, face, starship.balancingManager.getWeaponSupplier()), AmmoConsumingWeaponSubsystem, PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val length: Int = 6

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		MiniPhaserProjectile(StarshipProjectileSource(starship), getName(), loc, dir, shooter).fire()
	}

	override fun getName(): Component {
		return Component.text("Mini Phaser")
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireMaterial(item, Material.EMERALD, 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}
}
