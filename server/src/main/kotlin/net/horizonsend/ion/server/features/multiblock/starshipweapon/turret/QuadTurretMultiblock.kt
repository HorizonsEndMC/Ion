package net.horizonsend.ion.server.features.multiblock.starshipweapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.QuadTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.block.BlockFace

sealed class QuadTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return QuadTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int


	override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.quadTurret

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-2, getSign() * 4, +3), Vec3i(-1, getSign() * 4, +4), Vec3i(1, getSign() * 4, +4), Vec3i(2, getSign() * 4, + 3))

	override fun MultiblockShape.buildStructure() {
		z(-4) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(0).stainedTerracotta()
				x(+1).anyStairs()
			}
		}
		z(-3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).carbyne()
				x(0).carbyne()
				x(+1).carbyne()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs()
				x(0).anySlab()
				x(+1).anyStairs()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).anyStairs()
				x(-1).stainedTerracotta()
				x(0).anyStairs()
				x(+1).stainedTerracotta()
				x(+2).anyStairs()
			}
		}
		z(-1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-4).anyStairs()
				x(-3).carbyne()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).carbyne()
				x(+4).anyStairs()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).stainedTerracotta()
				x(-1).stainedTerracotta()
				x(+0).stainedTerracotta()
				x(+1).stainedTerracotta()
				x(+2).stainedTerracotta()
				x(+3).anySlab()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-4).stainedTerracotta()
				x(-3).carbyne()
				x(-2).stainedTerracotta()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).stainedTerracotta()
				x(+3).carbyne()
				x(+4).stainedTerracotta()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).type(GRINDSTONE)
				x(-1).stainedTerracotta()
				x(+0).anyStairs()
				x(+1).stainedTerracotta()
				x(+2).type(GRINDSTONE)
				x(+3).anySlab()
			}
		}
		z(+1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-4).anyStairs()
				x(-3).carbyne()
				x(-2).stainedTerracotta()
				x(-1).stainedTerracotta()
				x(+0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).stainedTerracotta()
				x(+3).carbyne()
				x(+4).anyStairs()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).endRod()
				x(-1).type(GRINDSTONE)
				x(+0).anySlab()
				x(+1).type(GRINDSTONE)
				x(+2).endRod()
				x(+3).anySlab()
			}
		}
		z(+2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).stainedTerracotta()
				x(-1).stainedTerracotta()
				x(+0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).stainedTerracotta()
				x(+3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).endRod()
				x(-1).endRod()
				x(0).type(IRON_TRAPDOOR)
				x(+1).endRod()
				x(+2).endRod()
			}
		}
		z(+3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).stainedTerracotta()
				x(0).carbyne()
				x(+1).stainedTerracotta()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).endRod()
				x(0).type(IRON_TRAPDOOR)
				x(1).endRod()
			}
		}
		z(+4) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(0).stainedTerracotta()
				x(+1).anyStairs()
			}
		}
	}
}

object TopQuadTurretMultiblock : QuadTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomQuadTurretMultiblock : QuadTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
