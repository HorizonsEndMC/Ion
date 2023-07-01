package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.cache.nations.PlayerCache
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.controllers.PlayerController
import net.horizonsend.ion.server.miscellaneous.gayColors
import net.horizonsend.ion.server.features.multiblock.starshipweapon.StarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.TurretLaserProjectile
import net.starlegacy.util.CARDINAL_BLOCK_FACES
import net.starlegacy.util.Vec3i
import net.starlegacy.util.rightFace
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.util.Vector

abstract class TurretMultiblock : RotatingMultiblock(), StarshipWeaponMultiblock<TurretWeaponSubsystem> {
	init {
		shape.signCentered()
		shape.ignoreDirection()
	}

	override val name: String = "turret"
	override val signText = createSignText("&8Turret", null, null, null)

	abstract val cooldownNanos: Long
	abstract val range: Double
	abstract val sound: String

	abstract val projectileSpeed: Int
	abstract val projectileParticleThickness: Double
	abstract val projectileExplosionPower: Float
	abstract val projectileShieldDamageMultiplier: Int

	protected abstract fun buildFirePointOffsets(): List<Vec3i>
	protected abstract fun getPilotOffset(): Vec3i

	private val firePointOffsets: Map<BlockFace, List<Vec3i>> = CARDINAL_BLOCK_FACES.associate { inward ->
		val right = inward.rightFace

		val newOffsets: List<Vec3i> = buildFirePointOffsets().map { (x, y, z) ->
			Vec3i(x = right.modX * x + inward.modX * z, y = y, z = right.modZ * x + inward.modZ * z)
		}

		return@associate inward to newOffsets
	}

	fun getFirePoints(face: BlockFace): List<Vec3i> {
		return firePointOffsets.getValue(face)
	}

	// the mean coordinates of the fire points
	private val meanFirePoints: Map<BlockFace, Vec3i> = CARDINAL_BLOCK_FACES.associate { inward ->
		var x = 0
		var y = 0
		var z = 0
		val firePoints = getFirePoints(inward)

		for (point in firePoints) {
			x += point.x
			y += point.y
			z += point.z
		}

		x /= firePoints.size
		y /= firePoints.size
		z /= firePoints.size

		return@associate inward to Vec3i(x, y, z)
	}

	fun getMeanFirePoint(face: BlockFace): Vec3i {
		return meanFirePoints.getValue(face)
	}

	fun updateSubsystem(sign: Sign, oldKeys: LongOpenHashSet, newKeys: LongOpenHashSet, newFace: BlockFace) {
		val starship = ActiveStarships.findByBlock(sign.block) ?: return
		val signPos = Vec3i(sign.location)
		val subsystem = starship.turrets.firstOrNull { it.pos == signPos } ?: return
		starship.blocks.removeAll(oldKeys)
		starship.blocks.addAll(newKeys)
		starship.calculateHitbox()
		subsystem.face = newFace
	}

	/** gets the points relative to the sign pos */
	private fun getAdjustedFirePoints(pos: Vec3i, face: BlockFace) = getFirePoints(face)
		.map { Vec3i(it.x + pos.x, it.y + pos.y, it.z + pos.z) }

	fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, starship: ActiveStarship, shooter: Controller?, isAuto: Boolean = true) {
		val color: Color = getColor(starship, shooter, isAuto)
		val speed = projectileSpeed.toDouble()

		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) {
				continue
			}

			val loc = point.toLocation(world).toCenterLocation()

			TurretLaserProjectile(
				starship,
				loc,
				dir,
				speed,
				color,
				range,
				projectileParticleThickness,
				projectileExplosionPower,
				projectileShieldDamageMultiplier,
				sound,
				shooter
			).fire()
		}
	}

	private fun getColor(starship: ActiveStarship, shooter: Controller?, isAuto: Boolean): Color {
		if (starship.rainbowToggle && !isAuto)
			return gayColors.random()

		if (shooter != null && shooter is PlayerController) {
			val nation: Oid<Nation>? = PlayerCache[shooter.player].nationOid

			if (nation != null) {
				return Color.fromRGB(NationCache[nation].color)
			}
		}

		return Color.FUCHSIA
	}
}
