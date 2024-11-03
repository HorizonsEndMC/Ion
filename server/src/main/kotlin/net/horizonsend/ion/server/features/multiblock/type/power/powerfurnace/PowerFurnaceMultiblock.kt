package net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplay
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplay
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PowerStorage
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.crafting.RecipeType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_20_R3.block.CraftFurnaceFurnace
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import java.util.Optional

abstract class PowerFurnaceMultiblock(tierText: String) : Multiblock(), NewPoweredMultiblock<PowerFurnaceMultiblock.PowerFurnaceMultiblockEntity> {
	override val name = "powerfurnace"

	protected abstract val burnTime: Int
	protected abstract val tierMaterial: Material

	override val signText = createSignText(
		line1 = "&6Power",
		line2 = "&4Furnace",
		line3 = null,
		line4 = tierText
	)

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).wireInputComputer()
				x(+1).anyGlassPane()
			}
			y(+0) {
				x(-1).anyGlassPane()
				x(+0).machineFurnace()
				x(+1).anyGlassPane()
			}
		}

		z(+1) {
			y(-1) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}

			y(+0) {
				x(-1).type(tierMaterial)
				x(+0).anyGlass()
				x(+1).type(tierMaterial)
			}
		}
	}

	override fun createEntity(manager: MultiblockManager, data: PersistentMultiblockData, world: World, x: Int, y: Int, z: Int, structureDirection: BlockFace): PowerFurnaceMultiblockEntity {
		return PowerFurnaceMultiblockEntity(data, manager, this, x, y, z, world, structureDirection)
	}

	class PowerFurnaceMultiblockEntity(
		data: PersistentMultiblockData,
		manager: MultiblockManager,
		override val multiblock: PowerFurnaceMultiblock,
		x: Int,
		y: Int,
		z: Int,
		world: World,
		structureDirection: BlockFace,
	) : MultiblockEntity(manager, multiblock, x, y, z, world, structureDirection), SyncTickingMultiblockEntity, StatusTickedMultiblockEntity, PoweredMultiblockEntity, LegacyMultiblockEntity,
		DisplayMultiblockEntity {
		override val powerStorage: PowerStorage = loadStoredPower(data)
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			PowerEntityDisplay(this, +0.0, +0.0, +0.0, 0.45f),
			StatusDisplay(statusManager, +0.0, -0.10, +0.0, 0.45f)
		).register()

		override fun tick() {
			if (powerStorage.getPower() == 0) {
				sleepWithStatus(text("Insufficient Power", RED), 250)
				return
			}

			val furnace = getInventory(0, 0, 0)?.holder as? Furnace

			if (furnace == null) {
				sleepWithStatus(text("Insufficient Power", RED), 250)
				return
			}

			if (furnace !is CraftFurnaceFurnace) return
			val resultOption = smeltingRecipeCache[furnace]

			if (resultOption.isEmpty) {
				sleepWithStatus(text("Invalid Recipe", RED), 250)
				return
			}

			powerStorage.removePower(30)
			sleepWithStatus(text("Working...", GREEN), multiblock.burnTime)

			furnace.burnTime = multiblock.burnTime.toShort()
			furnace.cookTime = 100.toShort()
			furnace.update()
		}

		override fun storeAdditionalData(store: PersistentMultiblockData, adapterContext: PersistentDataAdapterContext) {
			savePowerData(store)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
		}

		companion object {
			val smeltingRecipeCache: LoadingCache<CraftFurnaceFurnace, Optional<ItemStack>> = CacheBuilder.newBuilder().build(
				CacheLoader.from { furnace ->
					requireNotNull(furnace)
					val furnaceTile = furnace.tileEntity

					val level = Bukkit.getWorlds().first().minecraft

					// Get results for the recipe
					MinecraftServer.getServer().recipeManager
						.getRecipeFor(RecipeType.SMELTING, furnaceTile, level)
						.map {
							println("Recipe holder: $it")
							println("Recipe: ${it.value}")
							val b = it.value.assemble(furnaceTile, level.registryAccess())
							println("B: $b")
							CraftItemStack.asBukkitCopy(b)
						}
				}
			)
		}
	}
}
