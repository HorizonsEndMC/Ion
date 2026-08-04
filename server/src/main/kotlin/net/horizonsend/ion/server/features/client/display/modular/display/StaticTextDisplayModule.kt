package net.horizonsend.ion.server.features.client.display.modular.display

import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.kyori.adventure.text.Component

class StaticTextDisplayModule(
	handler: TextDisplayHandler,
	private val text: Component,
	offsetLeft: Double,
	offsetUp: Double,
	offsetBack: Double,
	scale: Float,
	relativeFace: RelativeFace = RelativeFace.FORWARD,
) : DisplayModule(handler, offsetLeft, offsetUp, offsetBack, scale, relativeFace) {
	override fun register() {}

	override fun deRegister() {}

	override fun buildText(): Component = text
}
