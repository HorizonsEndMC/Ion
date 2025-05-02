package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.features.space.data.CompoundTagType
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack as NMSItemStack
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ItemSerializer : PersistentDataType<PersistentDataContainer, BukkitItemStack> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<BukkitItemStack> = BukkitItemStack::class.java

	override fun toPrimitive(complex: BukkitItemStack, context: PersistentDataAdapterContext): PersistentDataContainer {
		val compound = CraftItemStack.asNMSCopy(complex).saveOptional(MinecraftServer.getServer().registryAccess()) as CompoundTag
		return CompoundTagType.toPrimitive(compound, context)
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BukkitItemStack {
		val compound = CompoundTagType.fromPrimitive(primitive, context)
		val nms = NMSItemStack.parse(MinecraftServer.getServer().registryAccess(), compound).get()
		return CraftItemStack.asBukkitCopy(nms)
	}
}
