package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlerHolder
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler

interface DisplayMultiblockEntity : DisplayHandlerHolder {
	val displayHandler: TextDisplayHandler
}
