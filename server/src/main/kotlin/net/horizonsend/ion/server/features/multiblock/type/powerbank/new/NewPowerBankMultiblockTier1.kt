package net.horizonsend.ion.server.features.multiblock.type.powerbank.new

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.persistence.PersistentDataType

object NewPowerBankMultiblockTier1 : NewPowerBankMultiblock<NewPowerBankMultiblockTier1.NewPowerBankMultiblockTier1Entity>("&7Tier 1") {
	override val tierMaterial = Material.IRON_BLOCK

	override fun createEntity(data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, signOffset: BlockFace): NewPowerBankMultiblockTier1Entity {
		return NewPowerBankMultiblockTier1Entity(
			this,
			x,
			y,
			z,
			world,
			signOffset,
			data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0)
		)
	}

	class NewPowerBankMultiblockTier1Entity(
		multiblock: NewPowerBankMultiblock<*>,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		signDirection: BlockFace,
		override var powerUnsafe: Int
	) : PowerBankEntity(multiblock, x, y, z, world, signDirection, 300_000) {

		override val position: BlockKey get() = toBlockKey(x, y, z)
	}
}
