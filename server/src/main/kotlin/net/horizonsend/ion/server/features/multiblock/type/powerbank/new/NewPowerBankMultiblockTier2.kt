package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

object NewPowerBankMultiblockTier2 : NewPowerBankMultiblock<NewPowerBankMultiblockTier2.NewPowerBankMultiblockTier2Entity>("&eTier 2") {
	override val tierMaterial = Material.GOLD_BLOCK

	override fun createEntity(data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): NewPowerBankMultiblockTier2Entity {
		return NewPowerBankMultiblockTier2Entity(
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
		multiblock: NewPowerBankMultiblock<*>,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		override var powerUnsafe: Int
	) : NewPowerBankMultiblock.PowerBankEntity(multiblock, x, y, z, world, signDirection, 350_000) {

		override val position: BlockKey get() = toBlockKey(x, y, z)
	}
}
