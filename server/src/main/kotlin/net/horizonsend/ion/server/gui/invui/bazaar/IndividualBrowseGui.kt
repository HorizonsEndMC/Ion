package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarEntry

interface IndividualBrowseGui<T : BazaarEntry> : ItemBrowseGui<T> {
	override fun getItemString(entry: T): String {
		return entry.itemString
	}
}
