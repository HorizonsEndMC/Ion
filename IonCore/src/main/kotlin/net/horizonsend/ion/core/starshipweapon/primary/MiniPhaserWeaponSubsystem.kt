package net.horizonsend.ion.core.starshipweapon.primary

import java.util.concurrent.TimeUnit
import net.horizonsend.ion.core.starshipweapon.projectile.MiniPhaserProjectile
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class MiniPhaserWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	AmmoConsumingWeaponSubsystem {
	override val length: Int = 6
	override val convergeDist: Double = 0.0
	override val extraDistance: Int = 0
	override val angleRadians: Double = Math.toRadians(30.0)
	override val powerUsage: Int = 5000
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(500L)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun canFire(dir: Vector, target: Vector?): Boolean {
		return starship is ActivePlayerStarship && starship.pilot!!.hasPermission("ioncore.eventweapon") && super.canFire(
			dir,
			target
		)
	}

	override fun fire(loc: Location, dir: Vector, shooter: Player, target: Vector?) {
		MiniPhaserProjectile(starship, loc, dir, shooter).fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return ItemStack(Material.EMERALD, 1)
	}
}