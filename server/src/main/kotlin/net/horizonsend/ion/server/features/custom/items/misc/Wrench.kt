package net.horizonsend.ion.server.features.custom.items.misc

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.command.qol.FixExtractorsCommand
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.rotateToFaceVector2d
import net.horizonsend.ion.server.features.client.display.HudIcons.FLUID_INFO_ID
import net.horizonsend.ion.server.features.client.display.teleportDuration
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
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.minecraft.world.entity.Display
import org.bukkit.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.RayTraceResult
import org.joml.Quaternionf
import org.joml.Vector3f
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

	private fun giveFluidTips(player: Player) = Tasks.async {
		val hitResult: RayTraceResult? = player.rayTraceBlocks(7.0, FluidCollisionMode.NEVER)
		val targeted = hitResult?.hitBlock ?: return@async removeEntity(player)
		val targetedLocation = hitResult.hitPosition

		val key = toBlockKey(targeted.x, targeted.y, targeted.z)

		val network = player.world.ion.transportManager.fluidGraphManager.getByLocation(key) ?: return@async removeEntity(player)
		network as FluidNetwork

		val fluid = network.networkContents

		val text = ofChildren(
			FluidUtils.formatFluidInfo(fluid),
			Component.newline(),
			text(" • ", HE_MEDIUM_GRAY),
			text("Flow Rate"),
			text(": ", HE_DARK_GRAY),
			text(network.getFlow(key).roundToHundredth()), text(" L/s", HE_MEDIUM_GRAY)
		)

		val projectedLocation = targetedLocation.add(player.location.direction.clone().multiply(-1)).toLocation(player.world).add(0.0, 0.3, 0.0)
		val scale = maxOf(player.eyeLocation.distance(projectedLocation).roundToInt() * 0.2f, 0.5f)

		if (ClientDisplayEntities[player.uniqueId]?.get(FLUID_INFO_ID) == null)
			createHudEntity(player, projectedLocation, text, scale)
		else updateHudEntity(player, projectedLocation, text, scale)

		Tasks.asyncDelay(FLUID_TICK_INTERVAL.toLong()) async2@{
			val hitResult: RayTraceResult? = player.rayTraceBlocks(7.0, FluidCollisionMode.NEVER)
			val targeted = hitResult?.hitBlock ?: return@async2 removeEntity(player)
			val key = toBlockKey(targeted.x, targeted.y, targeted.z)

			if (player.world.ion.transportManager.fluidGraphManager.getByLocation(key) == null) return@async2 removeEntity(player)
		}
	}

	fun removeEntity(player: Player) {
		val entity = ClientDisplayEntities[player.uniqueId]?.remove(FLUID_INFO_ID) ?: return
		ClientDisplayEntities.deleteDisplayEntityPacket(player.minecraft, entity)
	}

	fun createHudEntity(player: Player, location: Location, info: Component, scale: Float) {
		val entity = ClientDisplayEntities.createTextEntity(
			location,
			info,
			FLUID_TICK_INTERVAL.toLong() + 1,
			scale = scale,
			backgroundColor = Color.fromARGB(255, 0, 0, 0),
			seeThrough = true,
		)

		ClientDisplayEntities[player.uniqueId]?.set(FLUID_INFO_ID, entity)
		ClientDisplayEntities.sendEntityPacket(player, entity)
	}

	fun updateHudEntity(player: Player, location: Location, info: Component, scale: Float) {
		val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(FLUID_INFO_ID) as? Display.TextDisplay ?: return

		nmsEntity.text = PaperAdventure.asVanilla(info)

		val transformation = com.mojang.math.Transformation(
			Vector3f(),
			rotateToFaceVector2d(Vector3f()),
			Vector3f(scale),
			Quaternionf()
		)

		nmsEntity.transformationInterpolationDuration = FLUID_TICK_INTERVAL
		nmsEntity.teleportDuration = FLUID_TICK_INTERVAL

		ClientDisplayEntities.moveDisplayEntityPacket(player.minecraft, nmsEntity, location.x, location.y, location.z)
		ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)
	}
}
