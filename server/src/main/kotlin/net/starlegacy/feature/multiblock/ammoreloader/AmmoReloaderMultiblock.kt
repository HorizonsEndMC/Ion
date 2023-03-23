package net.starlegacy.feature.multiblock.ammoreloader

import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Material

abstract class AmmoReloaderMultiblock(val tierText: String) : PowerStoringMultiblock(), FurnaceMultiblock {
	protected abstract val tierMaterial: Material

	override fun LegacyMultiblockShape.buildStructure() {

	}
}
