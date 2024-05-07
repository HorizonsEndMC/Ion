package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

object NewPowerBankMultiblockTier3 : NewPowerBankMultiblock<NewPowerBankMultiblockTier3.NewPowerBankMultiblockTier3Entity>("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK

	override fun createEntity(data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): NewPowerBankMultiblockTier3Entity {
		return NewPowerBankMultiblockTier3Entity(
			this,
			x,
			y,
			z,
			world,
			signOffset,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}

	class NewPowerBankMultiblockTier3Entity(
		multiblock: NewPowerBankMultiblock<*>,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		override var powerUnsafe: Int
	) : NewPowerBankMultiblock.PowerBankEntity(multiblock, x, y, z, world, signDirection, 500_000) {
		override fun storeAdditionalData(store: PersistentMultiblockData) {
			store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, maxPower)
		}

		override val position: BlockKey get() = toBlockKey(x, y, z)
	}
}
