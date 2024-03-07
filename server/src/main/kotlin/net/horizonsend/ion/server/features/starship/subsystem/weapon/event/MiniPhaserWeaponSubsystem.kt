package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.CannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.MiniPhaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class MiniPhaserWeaponSubsystem(
    starship: ActiveStarship,
    pos: Vec3i,
    face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face), AmmoConsumingWeaponSubsystem, PermissionWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.miniPhaser

	override val permission: String = "ioncore.eventweapon"
	override val length: Int = balancing.length
	override val convergeDist: Double = balancing.convergeDistance
	override val extraDistance: Int = balancing.extraDistance
	override val angleRadiansHorizontal: Double = Math.toRadians(balancing.angleRadiansHorizontal)
	override val angleRadiansVertical: Double = Math.toRadians(balancing.angleRadiansVertical)
	override val powerUsage: Int = balancing.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(balancing.fireCooldownMillis)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun fire(loc: Location, dir: Vector, shooter: Damager, target: Vector) {
		MiniPhaserProjectile(starship, getName(), loc, dir, shooter).fire()
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
