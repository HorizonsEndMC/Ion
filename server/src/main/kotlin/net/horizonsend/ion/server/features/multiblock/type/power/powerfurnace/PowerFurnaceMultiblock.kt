package net.horizonsend.ion.server.features.multiblock.type.power.powerfurnace

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.client.display.modular.DisplayHandlers
import net.horizonsend.ion.server.features.client.display.modular.display.PowerEntityDisplayModule
import net.horizonsend.ion.server.features.client.display.modular.display.StatusDisplayModule
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.entity.type.DisplayMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.FurnaceBasedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.LegacyMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.SimplePoweredEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.StatusTickedMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.SyncTickingMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.ticked.TickedMultiblockEntityParent
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack as BukkitItemStack
import java.util.Optional

abstract class PowerFurnaceMultiblock(tierText: String) : Multiblock(), EntityMultiblock<PowerFurnaceMultiblock.PowerFurnaceMultiblockEntity>, DisplayNameMultilblock {
	override val name = "powerfurnace"

	abstract val maxPower: Int
	protected abstract val burnTime: Int
	protected abstract val tierMaterial: Material

	override val signText = createSignText(
		line1 = "$tierText &6Power",
		line2 = "&4Furnace",
		line3 = null,
		line4 = null
	)

	override val displayName: Component = ofChildren(legacyAmpersand.deserialize(tierText), text(" Power Furnace"))
	override val description: Component = text("Smelts items using power instead of fuel.")

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(-1).anyGlassPane()
				x(+0).powerInput()
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
	) : SimplePoweredEntity(data, multiblock, manager, x, y, z, world, structureDirection, multiblock.maxPower), SyncTickingMultiblockEntity, StatusTickedMultiblockEntity, PoweredMultiblockEntity, LegacyMultiblockEntity, FurnaceBasedMultiblockEntity,
		DisplayMultiblockEntity {
		override val tickingManager: TickedMultiblockEntityParent.TickingManager = TickedMultiblockEntityParent.TickingManager(interval = 20)
		override val statusManager: StatusMultiblockEntity.StatusManager = StatusMultiblockEntity.StatusManager()

		override val displayHandler = DisplayHandlers.newMultiblockSignOverlay(
			this,
			{ PowerEntityDisplayModule(it, this) },
			{ StatusDisplayModule(it, statusManager) }
		).register()

		override fun tick() {
			if (powerStorage.getPower() == 0) {
				sleepWithStatus(text("Insufficient Power", RED), 50)
				return
			}

			val furnaceInventory = getFurnaceInventory()

			if (furnaceInventory == null) {
				sleepWithStatus(text("Not Intact!", RED), 50)
				return
			}

			val smelted = furnaceInventory.smelting
			if (smelted == null) {
				sleepWithStatus(text("Sleeping...", BLUE), 50)
				return
			}

			val resultOption = smeltingRecipeCache[smelted]

			if (resultOption.isEmpty) {
				sleepWithStatus(text("Invalid Recipe", RED), 50)
				return
			}

			powerStorage.removePower(30)
			sleepWithStatus(text("Working...", GREEN), multiblock.burnTime)

			// Extra 25 because scheduling is usually a bit behind, causes a bit of lost time otherwise
			setBurningForTicks(multiblock.burnTime + 25)
		}

		override fun loadFromSign(sign: Sign) {
			migrateLegacyPower(sign)
			resetSign(sign, multiblock)
		}

		companion object {
			val smeltingRecipeCache: LoadingCache<BukkitItemStack, Optional<BukkitItemStack>> = CacheBuilder.newBuilder().build(
				CacheLoader.from { itemStack ->
					requireNotNull(itemStack)
					val nms = CraftItemStack.asNMSCopy(itemStack)
					val level = Bukkit.getWorlds().first().minecraft

					val input = SingleRecipeInput(nms)

					// Get results for the recipe
					MinecraftServer.getServer().recipeManager
						.getRecipeFor(RecipeType.SMELTING, input, level)
						.map {
							val b = it.value.assemble(input, level.registryAccess())
							CraftItemStack.asBukkitCopy(b)
						}
				}
			)
		}
	}
}
