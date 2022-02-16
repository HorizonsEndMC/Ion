package net.starlegacy.feature.multiblock.starshipweapon.heavy

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.secondary.RocketWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

sealed class RocketStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<RocketWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): RocketWeaponSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return RocketWeaponSubsystem(starship, pos, adjustedFace, this)
	}

	protected abstract fun getAdjustedFace(originalFace: BlockFace): BlockFace
}

object HorizontalRocketStarshipWeaponMultiblock : RocketStarshipWeaponMultiblock() {
	override fun getAdjustedFace(originalFace: BlockFace): BlockFace {
		return originalFace
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(+0).ironBlock()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(+0) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}

			y(+1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+2) {
			y(-2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(-1) {
				x(-2).anyStairs()
				x(-1..+1) { concrete() }
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(-1..+1) { concrete() }
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1..+1) { concrete() }
				x(+2).anyStairs()
			}

			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		z(+3..+4) {
			y(-2) {
				x(-1).anyWall()
				x(+0).ironBlock()
				x(+1).anyWall()
			}

			y(-1) {
				x(-2).anyWall()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyWall()
			}

			y(+0) {
				x(-2).ironBlock()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyWall()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyWall()
			}

			y(+2) {
				x(-1).anyWall()
				x(+0).ironBlock()
				x(+1).anyWall()
			}
		}

		z(+5) {
			y(-2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			y(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+0) {
				x(-2).ironBlock()
				x(+2).ironBlock()
			}

			y(+1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			y(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}

sealed class VerticalRocketStarshipWeaponMultiblock : RocketStarshipWeaponMultiblock() {
	protected abstract fun getYFactor(): Int

	override fun MultiblockShape.buildStructure() {
		val yFactor = getYFactor()

		y(yFactor * 0) {
			z(+0) {
				x(+0).ironBlock()
			}
		}

		y(yFactor * 1) {
			z(-1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			z(+0) {
				x(-1).ironBlock()
				x(+1).ironBlock()
			}

			z(+1) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		y(yFactor * 2) {
			z(-2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			z(-1) {
				x(-2).anyStairs()
				x(-1..+1) { concrete() }
				x(+2).anyStairs()
			}

			z(+0) {
				x(-2).ironBlock()
				x(-1..+1) { concrete() }
				x(+2).ironBlock()
			}

			z(+1) {
				x(-2).anyStairs()
				x(-1..+1) { concrete() }
				x(+2).anyStairs()
			}

			z(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}

		y((yFactor * 3)..(yFactor * 4)) {
			z(-2) {
				x(-1).anyWall()
				x(+0).ironBlock()
				x(+1).anyWall()
			}

			z(-1) {
				x(-2).anyWall()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyWall()
			}

			z(+0) {
				x(-2).ironBlock()
				x(+2).ironBlock()
			}

			z(+1) {
				x(-2).anyWall()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyWall()
			}

			z(+2) {
				x(-1).anyWall()
				x(+0).ironBlock()
				x(+1).anyWall()
			}
		}

		y(yFactor * 5) {
			z(-2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}

			z(-1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			z(+0) {
				x(-2).ironBlock()
				x(+2).ironBlock()
			}

			z(+1) {
				x(-2).anyStairs()
				x(-1).anyStairs()
				x(+1).anyStairs()
				x(+2).anyStairs()
			}

			z(+2) {
				x(-1).anyStairs()
				x(+0).ironBlock()
				x(+1).anyStairs()
			}
		}
	}
}

object UpwardRocketStarshipWeaponMultiblock : VerticalRocketStarshipWeaponMultiblock() {
	override fun getYFactor() = 1

	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.UP
}

object DownwardRocketStarshipWeaponMultiblock : VerticalRocketStarshipWeaponMultiblock() {
	override fun getYFactor() = -1

	override fun getAdjustedFace(originalFace: BlockFace): BlockFace = BlockFace.DOWN
}
