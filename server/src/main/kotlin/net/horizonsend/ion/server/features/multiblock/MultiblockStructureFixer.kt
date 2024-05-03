package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.world.data.DataFixer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType

/**
 * Upgrades a multiblock's structure to a new version, before detection
 **/
object MultiblockStructureFixer : DataFixer {
	/**
	 * Map of multiblock identifying strings to upgraders
	 **/
	private val fixers = mutableMapOf<String, MultiblockUpgrader>()

	override fun registerFixers() {

	}

	fun upgrade(sign: Sign): Boolean {
		val name = sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return false

		return upgrade(sign)
	}

	private class StructureUpgrader(
		override val multiblock: Multiblock,
		override val dataVersion: Int,
		val previousShape: MultiblockShape,
		val newShape: MultiblockShape
	) : MultiblockUpgrader {
		override fun apply(sign: Sign) {
			//TODO
		}
	}

	private class SignUpgrader(override val multiblock: Multiblock, override val dataVersion: Int) : MultiblockUpgrader {
		override fun apply(sign: Sign) {
			//TODO
		}
	}

	private sealed interface MultiblockUpgrader {
		val dataVersion: Int
		val multiblock: Multiblock

		fun apply(sign: Sign)
	}
}
