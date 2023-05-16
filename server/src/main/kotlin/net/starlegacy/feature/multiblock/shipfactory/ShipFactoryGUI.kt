package net.starlegacy.feature.multiblock.shipfactory

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHIP_FACTORY_DATA
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.nations.gui.AnvilInput
import net.starlegacy.feature.nations.gui.inputs
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

class ShipFactoryGUI(val player: Player, val multiblock: Sign) {
	val data get(): ShipFactoryData = multiblock.persistentDataContainer.get(SHIP_FACTORY_DATA, ShipFactoryData) ?: ShipFactoryData()
	val blueprint get(): Blueprint? = Blueprint.get(player.uniqueId.slPlayerId, data.blueprintName)
	val clipboard get(): Clipboard? = (blueprint)?.blockData?.let { Blueprint.parseData(it) }

	fun show(player: Player) = MenuHelper.apply {
		val gui = gui(
			6,
			text("Ship Factory Configuration")
				.color(TextColor.fromHexString("#eb9234"))
				.decoration(TextDecoration.BOLD, true)
				.decoration(TextDecoration.ITALIC, false)
		)

		val topBuffer = staticPane(0, 0, 9, 1)
		topBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE)) { it.isCancelled = true }
		val bottomBuffer = staticPane(0, 5, 9, 1)
		bottomBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE)) { it.isCancelled = true }

		gui.addPane(topBuffer)
		gui.addPane(bottomBuffer)

		val mainMenu = staticPane(0, 1, 9, 4)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.NAME_TAG)
			) {
				it.isCancelled = true
				onChangeBlueprint()
			}.setName(text("Change Blueprint", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			1, 0
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.KNOWLEDGE_BOOK)
			) {
				it.isCancelled = true
				//TODO
			}.setName(text("Open Materials Menu", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			3, 0
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.BOOK)
			) {
				it.isCancelled = true
				//TODO
			}.setName(text("Print Materials List", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			5, 0
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.GOLD_BLOCK)
			) { it.isCancelled = true }.setName(
				text().decoration(TextDecoration.ITALIC, false)
					.append(text("Remaining Price: ", NamedTextColor.GRAY))
					.append(text(getRemainingPrice(), NamedTextColor.GOLD))
					.build()
			),
			7, 0
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA)
			) {
				it.isCancelled = true
				onManageOffsets()
			}.setName(text("Change offsets", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			1, 2
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.ENDER_EYE)
			) {
				it.isCancelled = true
				//TODO
			}.setName(text("Preview", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			3, 2
		)

		mainMenu.addItem(
			GuiItem(
				ItemStack(Material.IRON_BARS)
			) {
				it.isCancelled = true
				//TODO
			}.setName(text("Show Bounding Box", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			5, 2
		)

		val toggleRunning = ToggleButton(7, 3, 1, 1)

		toggleRunning.setDisabledItem(
			GuiItem(
				ItemStack(Material.RED_CONCRETE)
			) { player.information("Enabled") }.setName(text("OFFLINE", NamedTextColor.RED).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
		)

		toggleRunning.setEnabledItem(
			GuiItem(
				ItemStack(Material.LIME_CONCRETE)
			) { player.information("Disabled") }.setName(text("RUNNING", NamedTextColor.GREEN).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false))
		)

		gui.addPane(mainMenu)
		gui.addPane(toggleRunning)
		gui.show(player)
	}

	private fun onChangeBlueprint() {
		player.inputs(
			AnvilInput("New Blueprint") { _, answer ->
				val blueprint = Blueprint.col.findOne(and(Blueprint::name eq answer, Blueprint::owner eq player.slPlayerId))

				if (blueprint == null) {
					player.userError("Blueprint $answer not found!")
					return@AnvilInput null
				}

				this.data.blueprintName = answer
				this.data.update(multiblock)

				return@AnvilInput answer
			}
		)
	}

//	fun calculateRemainingCost(): Double {}

//	fun getBounds(origin: BlockPos): Pair<BlockPos, BlockPos> {
//		val data = data!!
//		val clipboard = clipboard!!
//
//		val (offsetX: Int , offsetY: Int , offsetZ: Int) = data
//		val rotation = data.rotation
//
//		val (signX, signY, signZ) = origin
//
//		val originX = signX + offsetX
//		val originY = signY + offsetY
//		val originZ = signZ + offsetZ
//
//
//	}

	fun drawBoundingBox() {
		val (originX: Int , originY: Int , originZ: Int) = data
		val rotation = data.rotation


	}

	private fun onManageOffsets() {}

	private fun onPrintMaterials() {}

	private fun onOpenMaterialsMenu() {}

	fun onToggle() {}

	fun getRemainingPrice(): Int = 1

//	fun checkObstructions(): Boolean {}

	fun iterateRegion(origin: BlockPos, action: (blueprintBlock: Block, worldBlock: Block) -> Unit) {
		val clipboard = clipboard ?: return

		val (offsetX: Int , offsetY: Int , offsetZ: Int) = data
		val rotation = data.rotation

		val (signX, signY, signZ) = origin

		val originX = signX + offsetX
		val originY = signY + offsetY
		val originZ = signZ + offsetZ

		val region = clipboard.region.clone()
		val targetBlockVector: BlockVector3 = BlockVector3.at(data.offsetX, data.offsetY, data.offsetZ)
		val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)


	}
}
