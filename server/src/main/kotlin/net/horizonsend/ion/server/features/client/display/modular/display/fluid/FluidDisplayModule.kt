package net.horizonsend.ion.server.features.client.display.modular.display.fluid

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayModule
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidStorageContainer
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit.getPlayer
import java.text.DecimalFormat

abstract class FluidDisplayModule(
    handler: TextDisplayHandler,
    val container: FluidStorageContainer,
    offsetLeft: Double,
    offsetUp: Double,
    offsetBack: Double,
    scale: Float,
	relativeFace: RelativeFace = RelativeFace.FORWARD,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale, relativeFace, updateRateProvider = provider@{
	val nearbyViewers = (getPossibleViewers(true) ?: return@provider 1000L).mapNotNull(::getPlayer).any { player -> player.location.distance(it) < 10 }
	if (nearbyViewers) 100L else 1000L
}) {
	private val updateHandler: (FluidStorageContainer) -> Unit = {
		runUpdates()
	}

	override fun register() {
		container.registerUpdateListener(updateHandler)
	}

	override fun deRegister() {
		container.registerUpdateListener(updateHandler)
	}

	private companion object {
		val format = DecimalFormat("##.##")
	}

	protected fun formatFluid(): Component {
		val amount = container.getContents().amount
		return ofChildren(text(format.format(amount), NamedTextColor.GOLD), text("L", NamedTextColor.DARK_GRAY))
	}
}
