package net.horizonsend.ion.server.features.multiblock.entity.type.power

interface UpdatedPowerDisplayEntity : PoweredMultiblockEntity {
	val displayUpdates: MutableList<(UpdatedPowerDisplayEntity) -> Unit>

	override fun updatePowerVisually() {
		displayUpdates.forEach { it.invoke(this) }
	}
}
