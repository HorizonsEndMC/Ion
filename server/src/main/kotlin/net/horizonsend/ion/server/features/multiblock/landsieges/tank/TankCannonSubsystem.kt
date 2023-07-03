package net.horizonsend.ion.server.features.multiblock.landsieges.tank

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.gayColors
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.CannonWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.LaserProjectile
import net.starlegacy.util.Vec3i
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class TankCannonSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) :
	CannonWeaponSubsystem(starship, pos, face), AmmoConsumingWeaponSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.tankCannon.powerUsage
	override val length: Int = IonServer.balancing.starshipWeapons.tankCannon.length
	override val angleRadians: Double = Math.toRadians(IonServer.balancing.starshipWeapons.tankCannon.angleRadians)
	override val convergeDist: Double = IonServer.balancing.starshipWeapons.tankCannon.convergeDistance
	override val extraDistance: Int = IonServer.balancing.starshipWeapons.tankCannon.extraDistance

	override fun fire(
		loc: Location,
		dir: Vector,
		shooter: Controller,
		target: Vector?
	) {
		object : LaserProjectile(starship, loc, dir, shooter) {
			override val range: Double get() = IonServer.balancing.starshipWeapons.plasmaCannon.range
			override val speed: Double get() = IonServer.balancing.starshipWeapons.plasmaCannon.speed
			override val shieldDamageMultiplier: Int get() = IonServer.balancing.starshipWeapons.plasmaCannon.shieldDamageMultiplier
			override val color: Color
				get() = if (starship!!.rainbowToggle) gayColors.random() else starship.weaponColor
			override val thickness: Double get() = IonServer.balancing.starshipWeapons.plasmaCannon.thickness
			override val particleThickness: Double get() = IonServer.balancing.starshipWeapons.plasmaCannon.particleThickness
			override val explosionPower: Float get() = IonServer.balancing.starshipWeapons.plasmaCannon.explosionPower
			override val volume: Int get() = IonServer.balancing.starshipWeapons.plasmaCannon.volume
			override val soundName: String get() = IonServer.balancing.starshipWeapons.plasmaCannon.soundName
		}.fire()
	}

	override fun getRequiredAmmo() = ItemStack(Material.STONE)
}
