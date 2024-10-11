package net.horizonsend.ion.server.features.multiblock.type.starship.mininglasers

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.inventory.Inventory

abstract class MiningLaserMultiblock : Multiblock(), NewPoweredMultiblock<MiningLaserMultiblock.MiningLaserMultiblockEntity> {
	override val name = "mininglaser"
	abstract val range: Double

	abstract val beamOrigin: Triple<Int, Int, Int>
	abstract val mineRadius: Int
	abstract val beamCount: Int
	abstract val maxBroken: Int
	abstract val sound: String
	abstract val side: BlockFace
	abstract val tier: Int
	abstract val mirrored: Boolean

	abstract val outputOffset: Vec3i

	abstract fun getFirePointOffset(): Vec3i

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): MiningLaserMultiblockEntity {
		return MiningLaserMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class MiningLaserMultiblockEntity(
        data: PersistentMultiblockData,
        manager: MultiblockManager,
        override val poweredMultiblock: MiningLaserMultiblock,
        x: Int,
        y: Int,
        z: Int,
        world: World,
        structureDirection: BlockFace,
	) : SimplePoweredMultiblockEntity(data, manager, poweredMultiblock, x ,y ,z, world, structureDirection), LegacyMultiblockEntity {
		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.5f)
		).register()

		fun getFirePos(): Vec3i {
			val (right, up, forward) = poweredMultiblock.getFirePointOffset()
			return getPosRelative(
				right = right,
				up = up,
				forward = forward
			)
		}

		fun getOutput(): Inventory? = getInventory(
			right = poweredMultiblock.outputOffset.x,
			up = poweredMultiblock.outputOffset.y,
			forward = poweredMultiblock.outputOffset.z,
		)

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}
	}
}
