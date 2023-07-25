package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

sealed class TriTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return TriTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getYFactor(): Int

	override val cooldownNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.triTurret.fireCooldownNanos)
	override val range: Double = IonServer.balancing.starshipWeapons.triTurret.range
	override val sound: String = IonServer.balancing.starshipWeapons.triTurret.soundName

	override val projectileSpeed: Int = IonServer.balancing.starshipWeapons.triTurret.speed.toInt()
	override val projectileParticleThickness: Double = IonServer.balancing.starshipWeapons.triTurret.particleThickness
	override val projectileExplosionPower: Float = IonServer.balancing.starshipWeapons.triTurret.explosionPower
	override val projectileShieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.triTurret.shieldDamageMultiplier

	override fun buildFirePointOffsets(): List<Vec3i> = listOf(
		Vec3i(-2, getYFactor() * 4, +3),
		Vec3i(+0, getYFactor() * 4, +4),
		Vec3i(+2, getYFactor() * 4, +3)
	)

	override fun MultiblockShape.buildStructure() {
		y(getYFactor() * 2) {
			z(-1) {
				x(+0).sponge()
			}

			z(+0) {
				x(-1).sponge()
				x(+1).sponge()
			}

			z(+1) {
				x(+0).sponge()
			}
		}

		y(getYFactor() * 3) {
			z(-3) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}

			z(-2) {
				x(-2).ironBlock()
				x(-1..+1) { concrete() }
				x(+2).ironBlock()
			}

			z(-1) {
				x(-3).anyStairs()
				x(-2..+2) { concrete() }
				x(+3).anyStairs()
			}

			z(+0) {
				x(-3..-2) { stainedTerracotta() }
				x(-1..+1) { concrete() }
				x(+2..+3) { stainedTerracotta() }
			}

			z(+1) {
				x(-3).anyStairs()
				x(-2).stainedTerracotta()
				x(-1).concrete()
				x(+0).stainedTerracotta()
				x(+1).concrete()
				x(+2).stainedTerracotta()
				x(+3).anyStairs()
			}

			z(+2) {
				x(-2).ironBlock()
				x(-1).concrete()
				x(+0).stainedTerracotta()
				x(+1).concrete()
				x(+2).ironBlock()
			}

			z(+3) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}
		}

		y(getYFactor() * 4) {
			z(-3) {
				x(+0).anyStairs()
			}

			z(-2) {
				x(-2).anySlab()
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
				x(+2).anySlab()
			}

			z(-1) {
				x(-2).stainedTerracotta()
				x(-1).stainedTerracotta()
				x(+0).stainedTerracotta()
				x(+1).stainedTerracotta()
				x(+2).stainedTerracotta()
			}

			z(+0) {
				x(-3).anyStairs()
				x(-2).type(GRINDSTONE)
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
				x(+2).type(GRINDSTONE)
				x(+3).anyStairs()
			}

			z(+1) {
				x(-2).endRod()
				x(-1).type(IRON_TRAPDOOR)
				x(+0).type(GRINDSTONE)
				x(+1).type(IRON_TRAPDOOR)
				x(+2).endRod()
			}

			z(+2) {
				x(-2).endRod()
				x(-1).type(IRON_TRAPDOOR)
				x(+0).endRod()
				x(+1).type(IRON_TRAPDOOR)
				x(+2).endRod()
			}

			z(+3) {
				x(+0).endRod()
			}
		}
	}
}

object TopTriTurretMultiblock : TriTurretMultiblock() {
	override fun getYFactor(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +2)
}

object BottomTriTurretMultiblock : TriTurretMultiblock() {
	override fun getYFactor(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +2)
}
