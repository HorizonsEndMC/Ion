package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.TriTurretBalancing.TriTurretProjectileBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.RapidHeavyMissileLauncherWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TriTurretWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs

sealed class RapidHeavyMissileLauncherMultiblock : TurretMultiblock<TriTurretProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): RapidHeavyMissileLauncherWeaponSubsystem {
		return RapidHeavyMissileLauncherWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getYFactor(): Int

	override val displayName: Component get() = text("RHML (${if (getYFactor() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Heavy missile's, great for anti capital weapons.")

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<TriTurretProjectileBalancing> = starship.balancingManager.getWeapon(TriTurretWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> = listOf(
		Vec3i(-2, getYFactor() * 4, +3),
		Vec3i(+0, getYFactor() * 4, +4),
		Vec3i(+2, getYFactor() * 4, +3)
	)

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).anyTerracotta()
				x(-1).anyTerracotta()
				x(0).anyConcrete()
				x(1).anyTerracotta()
				x(2).anyTerracotta()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
			y(4) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
			y(0) {
				x(0).ironBlock()
			}
			y(2) {
				x(0).sponge()
			}
		}
		z(-1) {
			y(3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).anyTerracotta()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).anyTerracotta()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-2).anyTerracotta()
				x(-1).anyTerracotta()
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).anyTerracotta()
				x(2).anyTerracotta()
			}
			y(2) {
				x(-1).sponge()
				x(1).sponge()
			}
			y(4) {
				x(-1).ironBlock()
				x(0).anyTrapdoor(PrepackagedPreset.trapdoor(Bisected.Half.BOTTOM, RelativeFace.FORWARD, false, false))
				x(1).ironBlock()
			}
		}
		z(-2) {
			y(3) {
				x(-3).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-2).anyTerracotta()
				x(-1).anyTerracotta()
				x(0).anyConcrete()
				x(1).anyTerracotta()
				x(2).anyTerracotta()
				x(3).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
			y(4) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
			y(2) {
				x(0).sponge()
			}
		}
		z(1) {
			y(3) {
				x(-2).ironBlock()
				x(-1).anyTerracotta()
				x(0).anyTerracotta()
				x(1).anyTerracotta()
				x(2).ironBlock()
			}
			y(5) {
				x(-2).type(Material.POLISHED_BASALT)
				x(-1).type(Material.POLISHED_BASALT)
				x(1).type(Material.POLISHED_BASALT)
				x(2).type(Material.POLISHED_BASALT)
			}
		}
		z(-3) {
			y(3) {
				x(-2).ironBlock()
				x(-1).anyTerracotta()
				x(0).anyTerracotta()
				x(1).anyTerracotta()
				x(2).ironBlock()
			}
			y(5) {
				x(-2).dispenser()
				x(-1).dispenser()
				x(1).dispenser()
				x(2).dispenser()
			}
		}
		z(2) {
			y(3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(-4) {
			y(3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
	}
}

object TopRapidHeavyMissileLauncherMultiblock : RapidHeavyMissileLauncherMultiblock() {
	override fun getYFactor(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +2)
}

object BottomRapidHeavyMissileLauncherMultiblock : RapidHeavyMissileLauncherMultiblock() {
	override fun getYFactor(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +2)
}
