package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AssaultTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.block.BlockFace

sealed class AssaultTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return AssaultTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.assaultTurret

	override fun buildFirePointOffsets(): List<Vec3i> =
			listOf(Vec3i(0, getSign() * 4, +2))

	override fun MultiblockShape.buildStructure() {
		z(-2) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(getSign() * 4) {
				x(+0).anyStairs()
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
				x(-1).type(IRON_TRAPDOOR)
				x(+0).ironBlock()
				x(+1).type(IRON_TRAPDOOR)
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).carbyne()
				x(+0).terracottaOrDoubleslab()
				x(+1).carbyne()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).type(IRON_TRAPDOOR)
				x(-1).anySlab()
				x(+0).type(GRINDSTONE)
				x(+1).anySlab()
				x(+2).type(IRON_TRAPDOOR)
			}
		}
		z(+1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-2).anyStairs()
				x(-1).carbyne()
				x(+0).terracottaOrDoubleslab()
				x(+1).carbyne()
				x(+2).anyStairs()
			}
			y(getSign() * 4) {
				x(-1).type(IRON_TRAPDOOR)
				x(+0).endRod()
				x(+1).type(IRON_TRAPDOOR)
			}
		}
		z(+2) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
			y(getSign() * 4) {
				x(+0).endRod()
			}
		}
	}
}

object TopAssaultTurretMultiblock : AssaultTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomAssaultTurretMultiblock : AssaultTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
