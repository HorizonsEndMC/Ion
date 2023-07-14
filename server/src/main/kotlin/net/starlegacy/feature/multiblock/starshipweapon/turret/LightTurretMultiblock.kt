package net.starlegacy.feature.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.primary.LightTurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Material.GRINDSTONE
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

sealed class LightTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return LightTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val cooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.lightTurret.fireCooldownNanos)
	override val range: Double = IonServer.balancing.starshipWeapons.lightTurret.range
	override val sound: String = IonServer.balancing.starshipWeapons.lightTurret.soundName

	override val projectileSpeed: Int = IonServer.balancing.starshipWeapons.lightTurret.speed.toInt()
	override val projectileParticleThickness: Double = IonServer.balancing.starshipWeapons.lightTurret.particleThickness
	override val projectileExplosionPower: Float = IonServer.balancing.starshipWeapons.lightTurret.explosionPower
	override val projectileShieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.lightTurret.shieldDamageMultiplier

	override fun buildFirePointOffsets(): List<Vec3i> = listOf(Vec3i(0, +4 * getSign(), +2))

	override fun LegacyMultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
			y(getSign() * 4) {
				x(+0).anyStairs()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-1).stainedTerracotta()
				x(+0).ironBlock()
				x(+1).stainedTerracotta()
			}
			y(getSign() * 4) {
				x(-1).anySlab()
				x(+0).type(GRINDSTONE)
				x(+1).anySlab()
			}
		}
		z(+1) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).stainedTerracotta()
				x(+1).anyStairs()
			}
			y(getSign() * 4) {
				x(+0).endRod()
			}
		}
	}
}

object TopLightTurretMultiblock : LightTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomLightTurretMultiblock : LightTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
