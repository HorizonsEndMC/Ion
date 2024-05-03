package net.horizonsend.ion.server.features.multiblock

import net.horizonsend.ion.server.features.world.data.DataFixer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType

object MultiblockNameUpgrader : DataFixer {
	private val upgraders: MutableMap<String, String> = mutableMapOf()

	override fun registerFixers() {
		registerUpgrader("NavigationComputerMultiblockAdvanced", "HorizontalNavigationComputerMultiblockAdvanced")
	}

	private fun registerUpgrader(previous: String, new: String) {
		upgraders[previous] = new
	}

	fun upgrade(sign: Sign) {
		var nextName: String? = sign.persistentDataContainer.get(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING) ?: return
		var newName: String? = null

		while (nextName != null) {
			newName = nextName
			nextName = upgraders[nextName]
		}

		if (newName == null) return
		sign.persistentDataContainer.set(NamespacedKeys.MULTIBLOCK, PersistentDataType.STRING, newName)
		Tasks.syncBlocking { sign.update() }
	}
}
