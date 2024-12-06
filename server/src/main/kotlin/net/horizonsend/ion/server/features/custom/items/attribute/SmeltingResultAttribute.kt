package net.horizonsend.ion.server.features.custom.items.attribute

import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

class SmeltingResultAttribute(val result: Supplier<ItemStack>) : CustomItemAttribute
