package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.AutoTurretTargeting
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.CthulhuBeamProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.sqrt

class CthulhuBeamSubsystem(starship: ActiveStarship, pos: Vec3i, override var face: BlockFace) :
	WeaponSubsystem(starship, pos),
	DirectionalSubsystem,
	AutoWeaponSubsystem,
	PermissionWeaponSubsystem,
	ManualWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.cthulhuBeam
	override val permission: String = "ioncore.eventweapon"
	override val powerUsage: Int = balancing.powerUsage
	override val range: Double = balancing.range

	override fun getMaxPerShot(): Int {
		return (sqrt(starship.initialBlockCount.toDouble()) / 32).toInt()
	}

	private fun getFirePos() = Vec3i(pos.x + face.modX * 5, pos.y + face.modY * 5, pos.z + face.modZ * 5)

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		val origin = getFirePos().toCenterVector()
		val adjustedDir = target.clone().subtract(origin)
		val horizontalAxis = adjustedDir.clone()

		horizontalAxis.y = 0.0
		horizontalAxis.rotateAroundY(90.0)
		horizontalAxis.normalize()

		adjustedDir.rotateAroundAxis(horizontalAxis, Math.toRadians(randomDouble(-0.01, 0.01)))
		adjustedDir.rotateAroundY(Math.toRadians(randomDouble(-0.01, 0.01)))

		return adjustedDir.normalize()
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	override fun isIntact(): Boolean {
		for (i in 0 until 3) {
			val x = pos.x + face.modX * i
			val y = pos.y + face.modY * i
			val z = pos.z + face.modZ * i
			if (starship.world.getBlockAt(x, y, z).type.isAir) {
				return false
			}
		}

		return true
	}

	override fun autoFire(target: AutoTurretTargeting.AutoTurretTarget<*>, dir: Vector) {
		lastFire = System.nanoTime()
		val world = starship.world

		val shooter = (starship as? ActiveControlledStarship)?.controller ?: return
		val loc = getFirePos().toCenterVector().toLocation(world)
		CthulhuBeamProjectile(starship, getName(), loc, dir, shooter.damager).fire()
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		lastFire = System.nanoTime()
		val loc = getFirePos().toCenterVector().toLocation(starship.world)

		CthulhuBeamProjectile(starship, getName(), loc, dir, shooter).fire()
	}

	override fun shouldTargetRandomBlock(target: Player): Boolean {
		// TODO: only return false if there's a clear path
		return true
	}

	override fun getName(): Component {
		return Component.text("Eldritch Beam")
	}
}
