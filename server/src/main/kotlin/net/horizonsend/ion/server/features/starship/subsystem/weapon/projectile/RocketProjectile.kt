package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.configuration.starship.RocketBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.HorizontalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.ProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.MultipleFacing
import org.bukkit.entity.Entity

class RocketProjectile(
	source: ProjectileSource,
	name: Component,
	loc: Location,
	face: BlockFace,
	shooter: Damager
) : BlockProjectile<RocketBalancing.RocketProjectileBalancing>(source, name, loc, face.direction, shooter, HorizontalRocketStarshipWeaponMultiblock.damageType) {
	companion object {
		private fun getBlockData(
			down: Boolean,
			east: Boolean,
			north: Boolean,
			south: Boolean,
			up: Boolean,
			west: Boolean
		): BlockData {
			return Material.BROWN_MUSHROOM_BLOCK.createBlockData { blockData ->
				check(blockData is MultipleFacing)
				blockData.setFace(BlockFace.DOWN, down)
				blockData.setFace(BlockFace.EAST, east)
				blockData.setFace(BlockFace.NORTH, north)
				blockData.setFace(BlockFace.SOUTH, south)
				blockData.setFace(BlockFace.UP, up)
				blockData.setFace(BlockFace.WEST, west)
			}
		}

		private val blockMaps = mapOf(
			BlockFace.SOUTH to mapOf(
				Vec3i(0, 0, -1) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = false,
					west = true
				)
			),
			BlockFace.NORTH to mapOf(
				Vec3i(0, 0, 1) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = true,
					west = false
				)
			),
			BlockFace.EAST to mapOf(
				Vec3i(-1, 0, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = false,
					up = true,
					west = true
				)
			),
			BlockFace.WEST to mapOf(
				Vec3i(1, 0, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = false,
					west = false
				)
			),
			BlockFace.DOWN to mapOf(
				Vec3i(0, 1, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = false,
					west = true
				)
			),
			BlockFace.UP to mapOf(
				Vec3i(0, -1, 0) to getBlockData(
					down = true,
					east = false,
					north = false,
					south = true,
					up = true,
					west = true
				)
			)
		)
	}

	override val blockMap: Map<Vec3i, BlockData> = blockMaps.getValue(face)

	override var speed: Double = balancing.speed

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		super.impact(newLoc, block, entity)
		newLoc.world.playSound(newLoc, "horizonsend:starship.weapon.rocket.impact", 12f, 0.5f)
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		super.moveVisually(oldLocation, newLocation, travel)
		speed += 5.0 * delta
	}
}
