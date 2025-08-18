package net.horizonsend.ion.server.features.economy.chestshops

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.IonServerComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.SnbtPrinterTagVisitor
import net.minecraft.server.MinecraftServer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as NMSItemStack

object ChestShops : IonServerComponent() {

	@EventHandler()
	fun onClickSign(event: PlayerInteractEvent) {
		event.player.information(getStringRepresentation(event.item ?: return))
	}

	fun getStringRepresentation(itemStack: ItemStack): String {
		val nms = CraftItemStack.asNMSCopy(itemStack)
		val tag: CompoundTag = nms.save(MinecraftServer.getServer().registryAccess(), CompoundTag()) as CompoundTag
		return SnbtPrinterTagVisitor().visit(tag)
	}

	fun loadItem(string: String): ItemStack {
		val nbt = NbtUtils.snbtToStructure(string)

		val nmsStack = NMSItemStack.parse(MinecraftServer.getServer().registryAccess(), nbt).get()

		return CraftItemStack.asCraftMirror(nmsStack)
	}

	fun verifyItem(itemStack: ItemStack) {

	}
}
