package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.command.qol.FixExtractorsCommand
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendText
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.TickReceiverModule
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.features.transport.fluids.FluidUtils
import net.horizonsend.ion.server.features.transport.manager.graph.fluid.FluidNetwork
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.RayTraceResult
import kotlin.math.roundToInt

object Wrench : CustomItem(
	CustomItemKeys.WRENCH,
	text("Wrench"),
	ItemFactory.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel("tool/wrench")
		.build()
) {
	private const val FLUID_TICK_INTERVAL = 2

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@Wrench) { event, _, itemStack ->
			handleRightClick(event.player, event)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@Wrench) { event, _, itemStack ->
			checkStructure(event.player, event)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(FLUID_TICK_INTERVAL) { entity, itemStack, customItem, _ ->
			giveFluidTips(entity as? Player ?: return@TickReceiverModule)
		})
	}

	private fun checkStructure(player: Player, event: PlayerInteractEvent) {
		val hitBlock = event.clickedBlock
		if (hitBlock?.type?.isWallSign != true) return

		val sign = hitBlock.state as? Sign ?: return
		val multiblock = MultiblockAccess.getFast(sign) ?: return

		if (multiblock.signMatchesStructure(sign, loadChunks = false, particles =  false)) {
			player.information("Multiblock structure is correct.")
			return
		}

		MultiblockCommand.onCheck(player, multiblock, sign.x, sign.y, sign.z)
		FixExtractorsCommand.tryFixMultiblock(player, sign)
	}

	private fun handleRightClick(player: Player, event: PlayerInteractEvent?) {
		val clickedBlock = event?.clickedBlock ?: return
		val state = clickedBlock.state
		val customBlock = clickedBlock.blockData.customBlock

		if (player.isSneaking && state is Sign) return tryPickUpMultiblock(player, state)
		if (player.isSneaking && customBlock is WrenchRemovable) return tryPickUpBlock(player, clickedBlock, customBlock)
	}

	private fun tryPickUpMultiblock(player: Player, sign: Sign) {
		PrePackaged.pickUpStructure(player, sign)
	}

	private fun tryPickUpBlock(player: Player, block: Block, customBlock: WrenchRemovable) {
		val event = BlockBreakEvent(block, player)
		CustomBlockListeners.noDropEvents.add(event)

		if (!event.callEvent()) return

		val item = (customBlock as CustomBlock).customItem.constructItemStack()
		customBlock.decorateItem(item, block)

		block.type = Material.AIR
		block.world.dropItem(
			block.location.toCenterLocation(),
			item
		)
	}

	private fun giveFluidTips(player: Player)= Tasks.async {
		val hitResult: RayTraceResult? = player.rayTraceBlocks(7.0, FluidCollisionMode.NEVER)
		val targeted = hitResult?.hitBlock ?: return@async
		val targetedLocation = hitResult.hitPosition

		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val network = player.world.ion.transportManager.fluidGraphManager.getByLocation(key) ?: return@async
		network as FluidNetwork

		val fluid = network.networkContents

		val text = FluidUtils.formatFluidInfo(fluid)

		val projectedLocation = targetedLocation.add(player.location.direction.clone().multiply(-1)).toLocation(player.world).add(0.0, 0.3, 0.0)
		val scale = maxOf(player.eyeLocation.distance(projectedLocation).roundToInt() * 0.2f, 0.5f)

		player.sendText(
			projectedLocation,
			text,
			FLUID_TICK_INTERVAL.toLong() + 1,
			scale = scale,
			backgroundColor = Color.fromARGB(255, 0, 0, 0),
			seeThrough = true,
		)
	}
}
