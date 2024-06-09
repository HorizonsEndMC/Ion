package net.horizonsend.ion.server.features.custom.items.minerals

import org.bukkit.inventory.ItemStack
import java.util.function.Supplier

interface Smeltable {
    val smeltingResult: Supplier<ItemStack>
}
