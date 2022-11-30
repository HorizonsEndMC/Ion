package net.horizonsend.ion.server.customitems.blasters

import net.horizonsend.ion.server.customitems.CustomItemList
import net.horizonsend.ion.server.customitems.blasters.constructors.Magazine

object StandardMagazine: Magazine() {
	override val capacity: Int = 30
	override val customItemlist: CustomItemList = CustomItemList.STANDARD_MAGAZINE
}