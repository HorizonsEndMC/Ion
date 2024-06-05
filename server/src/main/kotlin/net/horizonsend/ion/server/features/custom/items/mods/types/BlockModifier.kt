package net.horizonsend.ion.server.features.custom.items.mods.types

/**
 * A tool modification that edits a list of blocks, e.g. the blocks mined by the drill
 *
 * An execution priority is necessary
 **/
interface BlockModifier {
	val priority: Int
}
