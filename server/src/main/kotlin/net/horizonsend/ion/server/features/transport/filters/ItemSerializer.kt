package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.features.space.data.CompoundTagType
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
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
		val nms = CraftItemStack.asNMSCopy(complex)
		val compound = if (nms.isEmpty) CompoundTag() else NMSItemStack.CODEC.encode(
			nms,
			MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE),
			CompoundTag()
		).result().get()
		return CompoundTagType.toPrimitive(compound as CompoundTag, context)
	}

	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BukkitItemStack {
		val compound = CompoundTagType.fromPrimitive(primitive, context)
		val nms = NMSItemStack.CODEC.decode(MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE), compound).resultOrPartial().get()
		return CraftItemStack.asBukkitCopy(nms.first)
	}
}
