package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.front
import net.kyori.adventure.text.Component
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType

interface LegacyMultiblockEntity {
	fun loadFromSign(sign: Sign)

	fun migrateLegacyPower(sign: Sign) {
		this as PoweredMultiblockEntity
		val oldPower = sign.persistentDataContainer.get(NamespacedKeys.POWER, PersistentDataType.INTEGER) ?: return

		storage.setPower(oldPower)

		sign.persistentDataContainer.remove(NamespacedKeys.POWER)
		sign.front().line(2, Component.empty())
		sign.update()
	}
}
