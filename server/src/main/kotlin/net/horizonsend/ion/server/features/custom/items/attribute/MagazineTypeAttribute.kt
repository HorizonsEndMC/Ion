package net.horizonsend.ion.server.features.custom.items.attribute

import net.horizonsend.ion.server.features.custom.NewCustomItem
import java.util.function.Supplier

class MagazineTypeAttribute(val magazineType: Supplier<NewCustomItem>) : CustomItemAttribute
