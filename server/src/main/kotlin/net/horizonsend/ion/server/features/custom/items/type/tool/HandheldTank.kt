package net.horizonsend.ion.server.features.custom.items.type.tool

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.DyedItemColor
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.HudIcons.FLUID_INFO_ID
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.FluidStorage
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.TickReceiverModule
import net.horizonsend.ion.server.features.custom.items.misc.Wrench.DISPLAY_TICK_INTERVAL
import net.horizonsend.ion.server.features.custom.items.misc.Wrench.createHudEntity
import net.horizonsend.ion.server.features.custom.items.misc.Wrench.removeEntity
import net.horizonsend.ion.server.features.custom.items.misc.Wrench.updateHudEntity
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage.FluidRestriction
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.inputs.IOType
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.FluidCollisionMode
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.RayTraceResult
import kotlin.math.roundToInt

object HandheldTank : CustomItem(
	CustomItemKeys.HANDHELD_TANK,
	Component.text("Handheld Tank"),
	ItemFactory.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel("tool/handheld_tank")
		.build()
) {
	private val fluidStorage = FluidStorage(100.0, FluidRestriction.Unlimited, ::resetColor)

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.FLUID_STORAGE, fluidStorage)

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@HandheldTank) { event, _, item ->
			tryWithdraw(event.player, item, event)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@HandheldTank) { event, _, item ->
			tryDeposit(event.player, item, event)
		})

		addComponent(CustomComponentTypes.TICK_RECEIVER, TickReceiverModule(DISPLAY_TICK_INTERVAL) { entity, _, _, _ ->
			giveTips(entity as? Player ?: return@TickReceiverModule)
		})
	}

	private const val WITHDRAW_AMOUNT = 100.0

	fun tryWithdraw(player: Player, item: ItemStack, event: PlayerInteractEvent) = Tasks.async {
		val block = event.clickedBlock ?: return@async
		val key = toBlockKey(block.x, block.y, block.z)
		val inputs = player.world.ion.transportManager.getInputProvider().getPorts(IOType.FLUID, key)
		val input = inputs.firstOrNull() ?: return@async

		val multiblockStore = input.metaData.connectedStore
		if (multiblockStore.isEmpty()) return@async
		val storageContents = multiblockStore.getContents()

		val itemRoom = fluidStorage.getRemainingRoom(item)
		if (itemRoom <= 0.0) return@async

		val toRemove = minOf(itemRoom, WITHDRAW_AMOUNT, multiblockStore.getContents().amount)
		val clone = storageContents.asAmount(toRemove)
		clone.amount -= multiblockStore.removeAmount(toRemove)

		fluidStorage.addContents(item, this, clone, player.location)
		player.information("Withdrew {0} {1}", clone.amount, clone.getDisplayName())
	}

	fun tryDeposit(player: Player, item: ItemStack, event: PlayerInteractEvent) = Tasks.async {
		val block = event.clickedBlock ?: return@async
		val key = toBlockKey(block.x, block.y, block.z)
		val inputs = player.world.ion.transportManager.getInputProvider().getPorts(IOType.FLUID, key)
		val input = inputs.firstOrNull() ?: return@async

		val multiblockStore = input.metaData.connectedStore
		if (multiblockStore.isFull()) return@async

		val itemContents = fluidStorage.getContents(item)
		if (itemContents.amount <= 0) return@async

		val toDeposit = minOf(multiblockStore.getRemainingRoom(), WITHDRAW_AMOUNT, itemContents.amount)
		val stack = itemContents.asAmount(toDeposit)
		stack.amount -= fluidStorage.removeAmount(item, this, toDeposit)

		multiblockStore.addFluid(stack, player.location)
		player.information("Deposited {0} {1}", stack.amount, stack.getDisplayName().itemName)
	}

	private fun giveTips(player: Player) = Tasks.async {
		val hitResult: RayTraceResult? = player.rayTraceBlocks(7.0, FluidCollisionMode.NEVER)
		val targeted = hitResult?.hitBlock ?: return@async removeEntity(player)
		val targetedLocation = hitResult.hitPosition

		val key = toBlockKey(targeted.x, targeted.y, targeted.z)
		val inputs = player.world.ion.transportManager.getInputProvider().getPorts(IOType.FLUID, key)

		// Unlikely case for there to multiple in one spot, but only handle 1 if there are
		val input = inputs.firstOrNull() ?: return@async removeEntity(player)
		val store = input.metaData.connectedStore

		val text = Component.text()
			.append(ofChildren(store.getContents().getDisplayName(), Component.text(": ", HEColorScheme.HE_DARK_GRAY), Component.text(store.getContents().amount, NamedTextColor.WHITE), Component.text("/", HEColorScheme.HE_DARK_GRAY), Component.text(store.capacity, NamedTextColor.WHITE)))
			.append(FluidUtils.formatFluidProperties(store.getContents()))
			.append(Component.newline(), Component.text("Left-click to withdraw", HEColorScheme.HE_MEDIUM_GRAY))
			.append(Component.newline(), Component.text("Right-click to deposit", HEColorScheme.HE_MEDIUM_GRAY))
			.build()

		val projectedLocation = targetedLocation.add(player.location.direction.clone().multiply(-1)).toLocation(player.world).add(0.0, 0.3, 0.0)
		val scale = maxOf(player.eyeLocation.distance(projectedLocation).roundToInt() * 0.2f, 0.5f)

		if (ClientDisplayEntities[player.uniqueId]?.get(FLUID_INFO_ID) == null)
			createHudEntity(player, projectedLocation, text, scale)
		else updateHudEntity(player, projectedLocation, text, scale)

		Tasks.asyncDelay(DISPLAY_TICK_INTERVAL.toLong()) async2@{
			if (
				player.inventory.itemInMainHand.customItem?.key != CustomItemKeys.HANDHELD_TANK &&
				player.inventory.itemInOffHand.customItem?.key != CustomItemKeys.HANDHELD_TANK
			) return@async2 removeEntity(player)

			val hitResult: RayTraceResult? = player.rayTraceBlocks(7.0, FluidCollisionMode.NEVER)
			val targeted = hitResult?.hitBlock ?: return@async2 removeEntity(player)
			val newKey = toBlockKey(targeted.x, targeted.y, targeted.z)

			if (player.world.ion.inputManager.getPorts(IOType.FLUID, newKey).isEmpty()) return@async2 removeEntity(player)
		}
	}

	fun resetColor(itemStack: ItemStack, contents: FluidStack) {
		if (contents.isEmpty()) {
			itemStack.unsetData(DataComponentTypes.DYED_COLOR)
			itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString("transparent_liquid"))
			return
		}
		itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(contents.type.getValue().displayProperties.color, false))
		itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addString(contents.type.getValue().displayProperties.tankKey))
	}
}
