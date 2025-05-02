package net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

abstract class MiningLaserMultiblock : Multiblock(), EntityMultiblock<MiningLaserMultiblock.MiningLaserMultiblockEntity>, DisplayNameMultilblock {
	override val name = "mininglaser"
	abstract val range: Double

	abstract val beamOrigin: Triple<Int, Int, Int>
	abstract val mineRadius: Int
	abstract val beamCount: Int
	abstract val maxBroken: Int
	abstract val sound: String
	abstract val side: BlockFace
	abstract val tier: Int

	abstract val outputOffset: Vec3i
	abstract val maxPower: Int

	abstract fun getFirePointOffset(): Vec3i

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): MiningLaserMultiblockEntity {
		return MiningLaserMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class MiningLaserMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: MiningLaserMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : SimplePoweredEntity(data, multiblock, manager, x ,y ,z, world, structureDirection, multiblock.maxPower), LegacyMultiblockEntity {
		override val displayHandler = standardPowerDisplay(this)

		fun getFirePos(): Vec3i {
			val (right, up, forward) = multiblock.getFirePointOffset()
			return getPosRelative(
				right = right,
				up = up,
				forward = forward
			)
		}

		fun getOutput(): Inventory? = getInventory(
			right = multiblock.outputOffset.x,
			up = multiblock.outputOffset.y,
			forward = multiblock.outputOffset.z,
		)

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
