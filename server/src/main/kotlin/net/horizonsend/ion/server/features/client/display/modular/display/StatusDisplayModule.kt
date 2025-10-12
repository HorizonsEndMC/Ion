package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit.getPlayer

class StatusDisplayModule(
	handler: TextDisplayHandler,
    private val statusSupplier: StatusMultiblockEntity.StatusManager,
    offsetLeft: Double = 0.0,
    offsetUp: Double = STATUS_TEXT_LINE,
    offsetBack: Double = 0.0,
    scale: Float = MATCH_SIGN_FONT_SIZE,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale, updateRateProvider = provider@{
	val nearbyViewers = (getPossibleViewers(true) ?: return@provider 1000L).mapNotNull(::getPlayer).any { player -> player.location.distance(it) < 10 }
	if (nearbyViewers) 100L else 1000L
}) {
	private val updateHandler: Runnable = Runnable { runUpdates() }

	override fun register() {
		statusSupplier.updateManager.add(updateHandler)
	}

	override fun deRegister() {
		statusSupplier.updateManager.remove(updateHandler)
	}

	override fun buildText(): Component {
		return statusSupplier.status
	}
}
