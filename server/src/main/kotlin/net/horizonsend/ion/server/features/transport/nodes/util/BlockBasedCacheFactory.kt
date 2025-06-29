package net.horizonsend.ion.server.features.transport.nodes.util

import com.google.common.collect.Multimap
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.transport.manager.holders.CacheHolder
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.getTypeSafe
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import kotlin.reflect.KClass

class BlockBasedCacheFactory<T : Any> private constructor(private val materialHandlers: Multimap<Material, MaterialHandler<T, *>>) {
	fun cache(block: Block, holder: CacheHolder<*>): T? {
		val type = block.getTypeSafe() ?: return null

		val forMaterial = materialHandlers[type] ?: return null
		val blockData = getBlockDataSafe(block.world, block.x, block.y, block.z) ?: return null

		val filtered = forMaterial.filter { handler -> handler.blockDataClass.isInstance(blockData) }

		return filtered.firstNotNullOfOrNull { handler -> handler.construct(blockData, toBlockKey(block.x, block.y, block.z), holder) }
	}

	class Builder<T : Any> {
		val materialHandlers = multimapOf<Material, MaterialHandler<T, *>>()

		inline fun <reified B: BlockData> addDataHandler(material: Material, noinline constructor: (B, BlockKey, CacheHolder<*>) -> T?): Builder<T> {
			this.materialHandlers[material].add(MaterialHandler(B::class, constructor))
			return this
		}

		inline fun <reified B: BlockData> addDataHandler(customBlock: IonRegistryKey<CustomBlock, out CustomBlock>, noinline constructor: (B, BlockKey, CacheHolder<*>) -> T?): Builder<T> {
			val blockData = customBlock.getValue().blockData
			require(blockData is B)

			return addDataHandler<B>(blockData.material) { data, lng, holder ->
				if (data.customBlock != customBlock) return@addDataHandler null
				constructor.invoke(data, lng, holder)
			}
		}

		inline fun <reified B: BlockData> addDataHandler(materials: Iterable<Material>, noinline constructor: (B, BlockKey, CacheHolder<*>) -> T): Builder<T> {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(B::class, constructor))
			return this
		}

		inline fun <reified B: BlockData> addDataHandler(vararg materials: Material, noinline constructor: (B, BlockKey, CacheHolder<*>) -> T?): Builder<T> {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(B::class, constructor))
			return this
		}

		fun addSimpleNode(materials: Iterable<Material>, constructor: (BlockKey, Material, CacheHolder<*>) -> T): Builder<T> {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { data, key, holder -> constructor(key, data.material, holder) })
			return this
		}

		fun addSimpleNode(vararg materials: Material, constructor: (BlockKey, Material, CacheHolder<*>) -> T): Builder<T> {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { data, key, holder -> constructor(key, data.material, holder) })
			return this
		}

		fun addSimpleNode(material: Material, constructor: (BlockKey, CacheHolder<*>) -> T): Builder<T> {
			this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, key, holder -> constructor(key, holder) })
			return this
		}

		fun addSimpleNode(material: Material, node: T): Builder<T> {
			this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, _, _ -> node })
			return this
		}

		inline fun <reified B: BlockData> addSimpleNode(customBlock: IonRegistryKey<CustomBlock, out CustomBlock>, node: T): Builder<T> {
			return addDataHandler<B>(customBlock.getValue().blockData.material) { data, lng, _ ->
				if (data.customBlock?.key != customBlock) return@addDataHandler null
				node
			}
		}

		fun addSimpleNode(materials: Iterable<Material>, node: T): Builder<T> {
			for (material in materials) this.materialHandlers[material].add(MaterialHandler(BlockData::class) { _, _, _ -> node })
			return this
		}

		fun addSimpleNode(vararg material: Material, node: T) = addSimpleNode(material.toSet(), node)

		fun build(): BlockBasedCacheFactory<T> {
			return BlockBasedCacheFactory(materialHandlers)
		}
	}

	class MaterialHandler<T : Any, B: BlockData>(val blockDataClass: KClass<B>, val constructor: (B, BlockKey, CacheHolder<*>) -> T?) {
		@Synchronized
		fun construct(blockData: BlockData, key: BlockKey, holder: CacheHolder<*>): T? {
			@Suppress("UNCHECKED_CAST")
			return runCatching { constructor.invoke(blockData as B, key, holder) }
				.onFailure { exception -> exception.printStackTrace() }
				.getOrNull()
		}
	}

	companion object {
		fun <T : Any> builder(): Builder<T> = Builder<T>()
	}
}
