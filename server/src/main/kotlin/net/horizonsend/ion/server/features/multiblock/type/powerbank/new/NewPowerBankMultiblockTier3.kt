package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

data object NewPowerBankMultiblockTier3 : NewPowerBankMultiblock("&bTier 3") {
	override val tierMaterial = Material.DIAMOND_BLOCK

	override fun createEntity(
		manager: ChunkMultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		signOffset: BlockFace
	): PowerBankEntity {
		return PowerBankEntity(
			manager,
			this,
			x,
			y,
			z,
			world,
			signOffset,
			300_000_000,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}
}
