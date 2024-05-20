package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

data object NewPowerBankMultiblockTier3 : NewPowerBankMultiblock<NewPowerBankMultiblockTier3.NewPowerBankMultiblockTier3Entity>("&bTier 3") {
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
		multiblock: NewPowerBankMultiblock<NewPowerBankMultiblockTier3Entity>,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		powerUnsafe: Int
	) : PowerBankEntity(multiblock, x, y, z, world, signDirection, 500_000_000, powerUnsafe)
}
