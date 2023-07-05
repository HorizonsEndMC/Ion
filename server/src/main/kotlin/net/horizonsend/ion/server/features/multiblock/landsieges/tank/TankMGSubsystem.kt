package net.horizonsend.ion.server.features.multiblock.landsieges.tank

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.gayColors
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.RestrictedWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.RightClickManual
import net.starlegacy.feature.starship.subsystem.weapon.projectile.LaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class TankMGSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face), AmmoConsumingWeaponSubsystem, HeavyWeaponSubsystem, RightClickManual, RestrictedWeaponSubsystem {
	override val powerUsage: Int get() = IonServer.balancing.starshipWeapons.tankMG.powerUsage
	override val length: Int get() = IonServer.balancing.starshipWeapons.tankMG.length
	override val angleRadians: Double get() = Math.toRadians(IonServer.balancing.starshipWeapons.tankMG.angleRadians)

	override val convergeDist: Double get() = IonServer.balancing.starshipWeapons.tankMG.convergeDistance
	override val extraDistance: Int get() = IonServer.balancing.starshipWeapons.tankMG.extraDistance
	override val boostChargeNanos: Long get() = IonServer.balancing.starshipWeapons.tankMG.boostChargeNanos
	override val fireCooldownNanos: Long
		get() = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.tankMG.fireCooldownNanos)

	override fun fire(loc: Location, dir: Vector, shooter: Controller, target: Vector?) {
		object : LaserProjectile(starship, loc, dir, shooter) {
			override val range: Double get() = IonServer.balancing.starshipWeapons.tankMG.range
			override val speed: Double get() = IonServer.balancing.starshipWeapons.tankMG.speed
			override val shieldDamageMultiplier: Double get() = IonServer.balancing.starshipWeapons.tankMG.shieldDamageMultiplier
			override val color: Color
				get() = if (starship!!.rainbowToggle) gayColors.random() else starship.weaponColor
			override val thickness: Double get() = IonServer.balancing.starshipWeapons.tankMG.thickness
			override val particleThickness: Double get() = IonServer.balancing.starshipWeapons.tankMG.particleThickness
			override val explosionPower: Float get() = IonServer.balancing.starshipWeapons.tankMG.explosionPower
			override val volume: Int get() = IonServer.balancing.starshipWeapons.tankMG.volume
			override val soundName: String get() = IonServer.balancing.starshipWeapons.tankMG.soundName
		}.fire()
	}

	override fun getRequiredAmmo(): ItemStack = ItemStack(Material.STONE)
	override fun isRestricted(starship: ActiveStarship) =
		starship.type != StarshipType.TANK
}
