package net.horizonsend.ion.server.legacy.starshipweapon.primary

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.starshipweapon.projectile.MiniPhaserProjectile
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
import java.util.concurrent.TimeUnit

class MiniPhaserWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	face: BlockFace
) : CannonWeaponSubsystem(starship, pos, face),
	AmmoConsumingWeaponSubsystem {
	override val length: Int = IonServer.balancing.starshipWeapons.miniPhaser.length
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.miniPhaser.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.miniPhaser.extraDistance
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.miniPhaser.angleRadians)
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.miniPhaser.powerUsage
	override var fireCooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.miniPhaser.fireCooldownNanos)

	override fun isAcceptableDirection(face: BlockFace) = true

	override fun canFire(dir: Vector, target: Vector): Boolean {
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
