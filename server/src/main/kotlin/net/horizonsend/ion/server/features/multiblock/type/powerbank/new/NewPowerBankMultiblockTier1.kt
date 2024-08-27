package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.world.ChunkMultiblockManager
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

data object NewPowerBankMultiblockTier1 : NewPowerBankMultiblock("&7Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK

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
			100_000,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}
}
