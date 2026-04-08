package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.features.space.data.CompoundTagType
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack as NMSItemStack
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack as BukkitItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * PersistentDataType implementation for storing a full Bukkit [BukkitItemStack]
 * inside a [PersistentDataContainer].
 *
 * This serializer bridges Bukkit's PDC system and Minecraft's item serialization
 * by converting a Bukkit item into its NMS/compound-tag form and back again.
 *
 * Intended use:
 * - persistent filter entries that need to remember an exact item
 * - plugin-owned metadata where a full item snapshot is needed
 *
 * This is more expensive and more version-sensitive than storing only a material
 * or a few simple fields, so it should be used when exact item preservation is
 * actually required.
 *
 * * Used by [FilterType.ItemType] to save filter entry values into PDC.
 */
object ItemSerializer : PersistentDataType<PersistentDataContainer, BukkitItemStack> {
	override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
	override fun getComplexType(): Class<BukkitItemStack> = BukkitItemStack::class.java

	/**
	 * Returns the primitive PDC representation used to store serialized item data.
	 *
	 * The item is encoded into an NBT-like compound structure and then wrapped as a
	 * [PersistentDataContainer] so it can be written into Bukkit persistent data.
	 */
	override fun toPrimitive(complex: BukkitItemStack, context: PersistentDataAdapterContext): PersistentDataContainer {
		val nms = CraftItemStack.asNMSCopy(complex)
		val ops = MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE)

		val compound = if (nms.isEmpty) {
			CompoundTag()
		} else {
			NMSItemStack.CODEC.encode(nms, ops, CompoundTag()).orThrow as CompoundTag
		}

		return CompoundTagType.toPrimitive(compound, context)
	}

	/**
	 * Reconstructs a Bukkit [BukkitItemStack] from its serialized persistent-data form.
	 *
	 * The stored compound data is decoded through Minecraft's item codec and then
	 * converted back into a Bukkit item stack.
	 */
	override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): BukkitItemStack {
		val compound = CompoundTagType.fromPrimitive(primitive, context)
		val ops = MinecraftServer.getServer().registryAccess().createSerializationContext(NbtOps.INSTANCE)

		val nms = NMSItemStack.CODEC.parse(ops, compound).resultOrPartial { itemId ->
            ComponentLogger.logger("Ion").error("Tried to load invalid item: $itemId")
        }.get()

		return CraftItemStack.asBukkitCopy(nms)
	}
}
