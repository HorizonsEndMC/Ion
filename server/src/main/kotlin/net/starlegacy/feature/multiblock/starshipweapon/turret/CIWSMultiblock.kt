package net.starlegacy.feature.multiblock.starshipweapon.turret

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.TurretWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.primary.CIWSWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

sealed class CIWSMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return CIWSWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val cooldownNanos: Long = 0
	override val range: Double = 0.0
	override val sound: String = ""

	override val projectileSpeed: Int = 0
	override val projectileParticleThickness: Double = 0.0
	override val projectileExplosionPower: Float = 0f
	override val projectileShieldDamageMultiplier: Int = 0

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +2), Vec3i(1, getSign() * 4, +2))

	override fun LegacyMultiblockShape.buildStructure() {
	}
}

object TopCIWSMultiblock : HeavyTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomCIWSMultiblock : HeavyTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
