package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack as NMSItemStack
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

object ItemSerializer : PersistentDataType<PersistentDataContainer, BukkitItemStack> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<BukkitItemStack> = BukkitItemStack::class.java

	override fun toPrimitive(complex: BukkitItemStack, context: PersistentDataAdapterContext): PersistentDataContainer {
		val compound = CraftItemStack.asNMSCopy(complex).saveOptional(MinecraftServer.getServer().registryAccess()) as CompoundTag

		return CraftPersistentDataContainer(
			compound.allKeys.associateWithNotNull(compound::get),
			(context.newPersistentDataContainer() as CraftPersistentDataContainer).dataTagTypeRegistry
		)
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BukkitItemStack {
		val raw = (primitive as CraftPersistentDataContainer).toTagCompound()
		val nms = NMSItemStack.parseOptional(MinecraftServer.getServer().registryAccess(), raw)
		return CraftItemStack.asBukkitCopy(nms)
	}
}
