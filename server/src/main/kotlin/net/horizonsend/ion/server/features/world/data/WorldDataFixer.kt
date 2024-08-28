package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.world.IonWorld

interface WorldDataFixer : DataFixer {
	fun fix(world: IonWorld)
}
