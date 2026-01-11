package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.RapidHeavyMissileLauncherBalancing.RapidHeavyMissileLauncherProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.weaponry.StarshipWeaponry
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.RapidHeavyMissileProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RapidHeavyMissileLauncherWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.util.Vector

sealed class RapidHeavyMissileLauncherMultiblock : TurretMultiblock<RapidHeavyMissileLauncherProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): RapidHeavyMissileLauncherWeaponSubsystem {
		return RapidHeavyMissileLauncherWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val displayName: Component get() = text("RHML (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Heavy missiles, anti-capital.")
	fun getName(): Component = text("Rapid Heavy Missile Launcher")

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<RapidHeavyMissileLauncherProjectileBalancing> = starship.balancingManager.getWeapon(RapidHeavyMissileLauncherWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> = listOf(
		Vec3i(+2, getSign() * 5, +3),
		Vec3i(+1, getSign() * 5, +3),
		Vec3i(-2, getSign() * 5, +3),
		Vec3i(-1, getSign() * 5, +3)
	)

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() *3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).terracottaOrDoubleSlab()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
			y(getSign() * 4) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(0) {
			y(getSign() * 3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).terracottaOrDoubleSlab()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).terracottaOrDoubleSlab()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
			y(getSign() * 2) {
				x(-1).sponge()
				x(1).sponge()
			}
			y(getSign() * 4) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
		}
		z(1) {
			y(getSign() * 3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).terracottaOrDoubleSlab()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
			y(getSign() * 4) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).ironBlock()
			}
			y(getSign() * 5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).endRod()
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
		}
		z(2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).ironBlock()
			}
			y(getSign() * 5) {
				x(-2).dispenser()
				x(-1).dispenser()
				x(0).endRod()
				x(1).dispenser()
				x(2).dispenser()
			}
		}
		z(-3) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(3) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}
	override fun shoot(
		world: World,
		pos: Vec3i,
		face: BlockFace,
		dir: Vector,
		starship: ActiveStarship,
		shooter: Damager,
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<RapidHeavyMissileLauncherProjectileBalancing>, RapidHeavyMissileLauncherProjectileBalancing>,
		isAuto: Boolean
	) {
		val initialLaunchDirection = face.direction
		val projectileBalancing = subSystem.balancing.projectile

		for ((index, point) in getAdjustedFirePoints(pos, face).withIndex()) {
			if (starship.isInternallyObstructed(point, dir)) continue
			val target: Vector = StarshipWeaponry.getTarget(point.toLocation(starship.world), dir, starship)

			for (newBoid in 0 until 1) {
				Tasks.syncDelay((index * projectileBalancing.delayMillis/50).toLong()) {
					val randomInitialDir = initialLaunchDirection.clone()
						.rotateAroundX(randomDouble(-0.15, 0.15))
						.rotateAroundY(randomDouble(-0.15, 0.15))
						.rotateAroundZ(randomDouble(-0.15, 0.15))
					val randomLoc = point.toCenterVector().clone()
						.add(randomInitialDir.clone().normalize().multiply(0.1))

					RapidHeavyMissileProjectile(
						StarshipProjectileSource(starship),
						getName(),
						randomLoc.toLocation(starship.world),
						dir,
						randomInitialDir,
						projectileBalancing,
						shooter,
						face,
						target,
						10,
					).fire()

					(0 until 10).forEach { _ ->
						val angle = Math.PI / 12
						val opposite = randomInitialDir.clone()
							.rotateAroundX(randomDouble(-angle, angle))
							.rotateAroundY(randomDouble(-angle, angle))
							.rotateAroundZ(randomDouble(-angle, angle))
						starship.world.spawnParticle(
							Particle.CLOUD,
							randomLoc.toLocation(starship.world),
							0,
							opposite.x,
							opposite.y,
							opposite.z,
							5.0,
							null,
							true
						)
					}
				}
			}
		}
	}
}

object TopRapidHeavyMissileLauncherMultiblock : RapidHeavyMissileLauncherMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +2)
}

object BottomRapidHeavyMissileLauncherMultiblock : RapidHeavyMissileLauncherMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +2)
}
