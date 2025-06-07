package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import java.util.concurrent.atomic.AtomicInteger

data class AvailableItemInformation(val amount: AtomicInteger, val references: MutableList<ItemReference>)
