package net.starlegacy.feature.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.primary.HeavyTurretWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.block.BlockFace
import java.util.concurrent.TimeUnit

sealed class HeavyTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return HeavyTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val cooldownNanos: Long = TimeUnit.MILLISECONDS.toNanos(IonServer.balancing.starshipWeapons.HeavyTurret.fireCooldownNanos)
	override val range: Double = IonServer.balancing.starshipWeapons.HeavyTurret.range
	override val sound: String = IonServer.balancing.starshipWeapons.HeavyTurret.soundName

	override val projectileSpeed: Int = IonServer.balancing.starshipWeapons.HeavyTurret.speed.toInt()
	override val projectileParticleThickness: Double = IonServer.balancing.starshipWeapons.HeavyTurret.particleThickness
	override val projectileExplosionPower: Float = IonServer.balancing.starshipWeapons.HeavyTurret.explosionPower
	override val projectileShieldDamageMultiplier: Int = IonServer.balancing.starshipWeapons.HeavyTurret.shieldDamageMultiplier

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +2), Vec3i(1, getSign() * 4, +2))

	override fun LegacyMultiblockShape.buildStructure() {
		z(-2) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}
		}
		z(-1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-2).anyStairs()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).anyStairs()
			}
			y(getSign() * 4) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-2).anyStairs()
				x(-1).stainedTerracotta()
				x(+0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).anyStairs()
			}
			y(getSign() * 4) {
				x(-1).type(GRINDSTONE)
				x(+0).anyStairs()
				x(+1).type(GRINDSTONE)
			}
		}
		z(+1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-2).anyStairs()
				x(-1).stainedTerracotta()
				x(+0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).anyStairs()
			}
			y(getSign() * 4) {
				x(-1).endRod()
				x(+0).type(IRON_TRAPDOOR)
				x(+1).endRod()
			}
		}
		z(+2) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).anyStairs()
				x(+1).anyStairs()
			}
		}
	}
}

object TopHeavyTurretMultiblock : HeavyTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomHeavyTurretMultiblock : HeavyTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
