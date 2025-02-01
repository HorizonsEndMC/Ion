package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material.GRINDSTONE
import org.bukkit.block.BlockFace

sealed class LightTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return LightTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	override val displayName: Component get() = text("Light Turret (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system effective against small targets. Can be auto-targeting.")

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.lightTurret

	override fun buildFirePointOffsets(): List<Vec3i> = listOf(Vec3i(0, +4 * getSign(), +2))

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleslab()
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
				x(-1).terracottaOrDoubleslab()
				x(+0).ironBlock()
				x(+1).terracottaOrDoubleslab()
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
				x(+0).terracottaOrDoubleslab()
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
