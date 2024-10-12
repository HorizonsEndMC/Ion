package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.TestAOEProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class FireWaveWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace
) : WeaponSubsystem(starship, pos),
	ManualWeaponSubsystem,
	HeavyWeaponSubsystem,
	PermissionWeaponSubsystem,
	DirectionalSubsystem {

	override val powerUsage: Int = 1

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return target
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return true
	}

	override fun isIntact(): Boolean {
		return true
	}

	override val balancing: StarshipWeapons.StarshipWeapon = StarshipWeapons().protonTorpedo

	override val boostChargeNanos: Long = 2L

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		val firePos = pos.toLocation(starship.world).add(face.direction.multiply(10))
		TestAOEProjectile(starship, shooter, firePos).fire()
	}

	override val permission: String = "ioncore.eventweapon"

	override fun getName(): Component {
		return Component.text("Abyssal Wave")
	}
}
