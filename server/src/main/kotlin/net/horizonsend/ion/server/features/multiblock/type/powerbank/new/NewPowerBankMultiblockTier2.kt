package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

data object NewPowerBankMultiblockTier2 : NewPowerBankMultiblock<NewPowerBankMultiblockTier2.NewPowerBankMultiblockTier2Entity>("&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK

	override fun createEntity(manager: ChunkMultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): NewPowerBankMultiblockTier2Entity {
		return NewPowerBankMultiblockTier2Entity(
			manager,
			this,
			x,
			y,
			z,
			world,
			signOffset,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}

	class NewPowerBankMultiblockTier2Entity(
		manager: ChunkMultiblockManager,
		multiblock: NewPowerBankMultiblock<NewPowerBankMultiblockTier2Entity>,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		powerUnsafe: Int
	) : PowerBankEntity(manager, multiblock, x, y, z, world, signDirection, 350_000, powerUnsafe)
}
