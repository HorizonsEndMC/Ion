package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.landsieges.tank.IFVCannonSubsystem
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TurretMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

object IFVCannonMultiblock : TurretMultiblock()  {
	override val cooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.ifvCannon.fireCooldownNanos)
	override val range: Double = IonServer.balancing.starshipWeapons.ifvCannon.range
	override val sound: String = IonServer.balancing.starshipWeapons.ifvCannon.soundName

	override val projectileSpeed: Int = IonServer.balancing.starshipWeapons.ifvCannon.speed.toInt()
	override val projectileParticleThickness: Double = IonServer.balancing.starshipWeapons.ifvCannon.particleThickness
	override val projectileExplosionPower: Float = IonServer.balancing.starshipWeapons.ifvCannon.explosionPower
	override val projectileShieldDamageMultiplier: Double = IonServer.balancing.starshipWeapons.ifvCannon.shieldDamageMultiplier

	override fun MultiblockShape.buildStructure() {
		z(+1) {
			y(+0) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+1) {
				x(+0).anyStairs()
			}
		}

		z(+2) {
			y(+0) {
				x(-2).anyStairs()

				x(-1).concrete()
				x(+0).concrete()
				x(+1).concrete()

				x(+2).anyStairs()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+3) {
			y(+0) {
				x(-2).ironBlock()
				x(-1).concrete()
				x(+0).stainedTerracotta()
				x(+1).concrete()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyTrapdoor()
				x(-1).anyStairs()
				x(+0).grindstone()
				x(+1).anyStairs()
				x(+2).anyTrapdoor()
			}
		}

		z(+4) {
			y(+0) {
				x(-2).anyStairs()
				x(-1).concrete()
				x(+0).stainedTerracotta()
				x(+1).concrete()
				x(+2).anyStairs()
			}

			y(+1) {
				x(-1).anyTrapdoor()
				x(+0).endRod()
				x(+1).anyTrapdoor()
			}
		}

		z(+5) {
			y(+0) {
				x(-1).anyWall()
				x(+0).ironBlock()
				x(+1).anyWall()
			}

			y(+1) {
				x(+0).endRod()
			}
		}
	}
	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(0, 2, +6))

	override fun getPilotOffset() = Vec3i(+0, +3, +1)
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace) =
		IFVCannonSubsystem(starship, pos, face)
}
