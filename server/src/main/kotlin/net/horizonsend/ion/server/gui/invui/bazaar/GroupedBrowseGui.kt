package net.horizonsend.ion.server.gui.invui.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarEntry

interface GroupedBrowseGui<T : BazaarEntry> : ItemBrowseGui<Map.Entry<String, List<T>>> {
	override fun getItemString(entry: Map.Entry<String, List<T>>): String {
		return entry.key
	}
}
