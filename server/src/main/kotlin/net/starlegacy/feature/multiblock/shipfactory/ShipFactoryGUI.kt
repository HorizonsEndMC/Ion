package net.starlegacy.feature.multiblock.shipfactory

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.core.BlockPos
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.nations.gui.guiButton
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.MenuHelper.setLoreComponent
import net.starlegacy.util.MenuHelper.setName
import net.starlegacy.util.Tasks
import net.starlegacy.util.component1
import net.starlegacy.util.component2
import net.starlegacy.util.component3
import net.starlegacy.util.toBukkitBlockData
import net.starlegacy.util.updateMeta
import net.wesjd.anvilgui.AnvilGUI
import net.wesjd.anvilgui.AnvilGUI.Completion
import net.wesjd.anvilgui.AnvilGUI.ResponseAction
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ShipFactoryGUI(val player: Player, val multiblock: Sign, val data: ShipFactoryData) {
	val blueprint get(): Blueprint? = Blueprint.get(player.uniqueId.slPlayerId, data.blueprintName)

	private val MAX_OFFSET = 100

	private val shipFactoryGUI = MenuHelper.gui(
		6,
		text("Ship Factory Configuration")
			.color(TextColor.fromHexString("#eb9234"))
			.decoration(TextDecoration.BOLD, true)
			.decoration(TextDecoration.ITALIC, false)
	)

	init {
		val topBuffer = MenuHelper.staticPane(0, 0, 9, 1)
		topBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE)) { it.isCancelled = true }
		val bottomBuffer = MenuHelper.staticPane(0, 5, 9, 1)
		bottomBuffer.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE)) { it.isCancelled = true }

		shipFactoryGUI.addPane(topBuffer)
		shipFactoryGUI.addPane(bottomBuffer)
	}

	fun show(): MenuHelper = MenuHelper.apply {
		val mainMenu = staticPane(0, 1, 9, 4)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.NAME_TAG)
			) {
				onSetBlueprint()
			}.setName(text("Change Blueprint", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			1, 0
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.KNOWLEDGE_BOOK)
			) {
				if (blueprint == null) {
					player.userError("Enter a valid blueprint first!")
				} else {
					val items = getMaterials(blueprint!!).map { (material, count) ->
						guiButton(ItemStack(material, count).updateMeta { it.lore(listOf(text(count, NamedTextColor.WHITE))) })
					}

					for (item in items) {
						println(item.item.itemMeta)
					}

					println(items.toMutableList().removeIf { it.item.itemMeta == null })

					openMaterialsMenu(0, items)
				}
			}.setName(text("Open Materials Menu", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			3, 0
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.BOOK)
			) {
				if (blueprint == null) {
					player.userError("Enter a valid blueprint first!")
				} else { onPrintMaterials(blueprint!!) }
			}.setName(text("Print Materials List", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			5, 0
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.GOLD_BLOCK)
			) {
				//TODO
			}.setName(
				text().decoration(TextDecoration.ITALIC, false)
					.append(text("Remaining Price: ", NamedTextColor.GRAY))
					.append(text(getRemainingPrice(), NamedTextColor.GOLD))
					.build()
			),
			7, 0
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA)
			) {
				openOffsetsMenu()
			}.setName(text("Change offsets", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			1, 2
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.ENDER_EYE)
			) {
				//TODO
			}.setName(text("Preview", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			3, 2
		)

		mainMenu.addItem(
			guiButton(
				ItemStack(Material.IRON_BARS)
			) {
				//TODO
			}.setName(text("Show Bounding Box", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)),
			5, 2
		)

		mainMenu.addItem(
			if (data.isRunning) {
				guiButton(
					ItemStack(Material.RED_CONCRETE)
				) { onToggle(true) }
					.setName(text("OFFLINE", NamedTextColor.RED).decorate(TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false)
					)
			} else {
				guiButton(
					ItemStack(Material.LIME_CONCRETE)
				) { onToggle(false) }
					.setName(
						text("RUNNING", NamedTextColor.GREEN)
							.decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)
					)
			},
			7, 4
		)

		shipFactoryGUI.addPane(mainMenu)
		shipFactoryGUI.show(player)
	}

	private fun onSetBlueprint(): Unit = Tasks.sync {
		AnvilGUI.Builder()
			.plugin(IonServer)
			.itemLeft(ItemStack(Material.NAME_TAG))
			.text("New Blueprint")
			.title("Change Blueprint")
			.onComplete { completion: Completion ->
				val answer = completion.text

				if (Blueprint.col.findOne(and(Blueprint::name eq answer, Blueprint::owner eq player.slPlayerId)) == null) {
					return@onComplete listOf(ResponseAction.replaceInputText("Blueprint $answer not found!"))
				}

				this.data.blueprintName = answer
				this.data.update(multiblock)
				multiblock.line(2, text(answer))
				multiblock.update()

				return@onComplete listOf(
					ResponseAction.close(),
					ResponseAction.run {
						this.show()
						player.success("Blueprint changed successfully.")
					}
				)
			}
			.open(player)
	}

	private fun openOffsetsMenu() = MenuHelper.run {
		val offsetsGUI = gui(3, text("Manage Offsets", NamedTextColor.DARK_PURPLE))
		val pane = StaticPane(0, 0, 9, 3)

		pane.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE), 1, 0, 1, 3) {
			this.isCancelled = true
		}

		pane.addItem(
			guiButton(ItemStack(Material.REDSTONE_BLOCK)) {
				show()
			}.setName(text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)),
			0, 0
		)

		pane.addItem(xyzLabel('X'), 2, 0)
		pane.addItem(xyzLabel('Y'), 2, 1)
		pane.addItem(xyzLabel('Z'), 2, 2)

		pane.addItem(
			arrowButton(105, text("Lower X Offset").color(NamedTextColor.RED), data.offsetX) {
				adjustOffset('X', data.offsetX - 1, playerClicker)
			}, 6, 0
		)
		pane.addItem(
			arrowButton(105, text("Lower Y Offset").color(NamedTextColor.RED), data.offsetY) {
				adjustOffset('Y', data.offsetY - 1, playerClicker)
			}, 6, 1
		)
		pane.addItem(
			arrowButton(105, text("Lower Z Offset").color(NamedTextColor.RED), data.offsetZ) {
				adjustOffset('Z', data.offsetZ - 1, playerClicker)
			}, 6, 2
		)

		pane.addItem(
			guiButton(Material.ENDER_PEARL) {
				promptSetOffset('X')
			}
				.setName(text("Set X offset").decoration(TextDecoration.ITALIC, false))
				.setLoreComponent(
					listOf(
						text().decoration(TextDecoration.ITALIC, false)
						.append(text("Current Value: ", NamedTextColor.GRAY))
						.append(text(data.offsetX).color(NamedTextColor.WHITE))
						.build()
				)
			),
			7, 0
		)
		pane.addItem(
			guiButton(Material.ENDER_PEARL) {
				promptSetOffset('Y')
			}
				.setName(text("Set Y offset").decoration(TextDecoration.ITALIC, false))
				.setLoreComponent(
					listOf(
						text().decoration(TextDecoration.ITALIC, false)
							.append(text("Current Value: ", NamedTextColor.GRAY))
							.append(text(data.offsetY).color(NamedTextColor.WHITE))
							.build()
					)
				),
			7, 1
		)
		pane.addItem(
			guiButton(Material.ENDER_PEARL) {
				promptSetOffset('Z')
			}
				.setName(text("Set Z offset").decoration(TextDecoration.ITALIC, false))
				.setLoreComponent(
					listOf(
						text().decoration(TextDecoration.ITALIC, false)
							.append(text("Current Value: ", NamedTextColor.GRAY))
							.append(text(data.offsetZ).color(NamedTextColor.WHITE))
							.build()
					)
				),
			7, 2
		)

		pane.addItem(
			arrowButton(103, text("Raise X Offset").color(NamedTextColor.GREEN), data.offsetX) {
				adjustOffset('X', data.offsetX + 1, playerClicker)
			}, 8, 0
		)
		pane.addItem(
			arrowButton(103, text("Raise Y Offset").color(NamedTextColor.GREEN), data.offsetY) {
				adjustOffset('Y', data.offsetY + 1, playerClicker)
			}, 8, 1
		)
		pane.addItem(
			arrowButton(103, text("Raise Z Offset").color(NamedTextColor.GREEN), data.offsetZ) {
				adjustOffset('Z', data.offsetZ + 1, playerClicker)
			}, 8, 2
		)

		offsetsGUI.addPane(pane)
		offsetsGUI.show(player)
	}

	private fun promptSetOffset(offset: Char): Unit = Tasks.sync {
		AnvilGUI.Builder()
			.plugin(IonServer)
			.itemLeft(ItemStack(Material.NAME_TAG))
			.text("0")
			.title("Change $offset Offset")
			.onComplete { completion: Completion ->
				val answer = completion.text

				val newValue = answer.toIntOrNull() ?: return@onComplete null; player.userError("Must enter a number")

				adjustOffset(offset, newValue, player)

				return@onComplete listOf(
					ResponseAction.close(),
					ResponseAction.run {
						openOffsetsMenu()
					}
				)
			}
			.open(player)
	}

	private fun adjustOffset(offset: Char, newValue: Int, clicker: Player): Unit = MenuHelper.run {
		when (offset) {
			'X' -> data.offsetX = newValue
			'Y' -> data.offsetY = newValue
			'Z' -> data.offsetZ = newValue
		}

		this@ShipFactoryGUI.data.update(multiblock)
		openOffsetsMenu()
		clicker.success("Updated $offset offset to $newValue")
	}

	private fun arrowButton(modelData: Int, name: Component, currentValue: Int, action: InventoryClickEvent.() -> Unit): GuiItem {
		val item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(modelData) }

		return guiButton(item, action).setName(name.decoration(TextDecoration.ITALIC, false))
			.setLoreComponent(listOf(
				text().decoration(TextDecoration.ITALIC, false)
					.append(text("Current Value: ", NamedTextColor.GRAY))
					.append(text(currentValue).color(NamedTextColor.WHITE))
					.build()
				)
			)
	}

	private fun xyzLabel(type: Char): GuiItem {
		val currentValue: Int

		val modelData = when (type) {
			'X' -> {
				currentValue = data.offsetX
				106
			}
			'Y' -> {
				currentValue = data.offsetY
				107
			}
			'Z' -> {
				currentValue = data.offsetZ
				108
			}
			else -> throw NoSuchElementException()
		}

		return MenuHelper.guiButton(ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.setCustomModelData(modelData)
		}).setName(text("$type: $currentValue").decoration(TextDecoration.ITALIC, false))
	}

	private fun onPrintMaterials(blueprint: Blueprint) { Tasks.async { player.userError(getMaterials(blueprint).toString()) } }

	private fun openMaterialsMenu(page: Int, items: List<GuiItem>): MenuHelper = MenuHelper.apply { //FIXME
		val header = StaticPane(0, 0, 9, 1)

		header.fillWith(ItemStack(Material.BLACK_STAINED_GLASS_PANE), 1, 0, 6, 1)
			{ this.isCancelled = true }

		header.addItem(
			guiButton(ItemStack(Material.REDSTONE_BLOCK)) {
				show()
			}.setName(text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)),
			0, 0
		)

		val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(105) }
		val rightArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta { it.setCustomModelData(103) }

		val materials = PaginatedPane(0, 1, 9, 5)
		materials.populateWithGuiItems(items)

		if (materials.pages <= page) materials.page = page

		header.addItem(
			guiButton(leftArrow) {
				if (page >= 1) {
					openMaterialsMenu(materials.page - 1, items)
				}
			}.setName(text("Previous Page").decoration(TextDecoration.ITALIC, false)),
			7, 0
		)

		header.addItem(
			guiButton(rightArrow) {
				openMaterialsMenu(materials.page + 1, items)
			}.setName(text("Next Page").decoration(TextDecoration.ITALIC, false)),
			8, 0
		)

		val gui = gui(6, text("Materials: Page ${materials.page}"))
		gui.addPane(header)
		gui.addPane(materials)
		gui.show(player)
	}

	private fun getClipboard(blueprint: Blueprint): Clipboard = Blueprint.parseData(blueprint.blockData)

	private fun getMaterials(blueprint: Blueprint): Map<Material, Int> {
		val materials = mutableMapOf<Material, AtomicInteger>()

		val clipboard = getClipboard(blueprint)

		for (vec in clipboard.region) {
			val state = clipboard.getBlock(vec) ?: continue
			val blockData = state.toBukkitBlockData()

			if (blockData.material.isAir) {
				continue
			}

			materials.getOrPut(blockData.material) { AtomicInteger(0) }.incrementAndGet()
		}

		return materials.mapValues { (_, atomic) -> atomic.toInt() }
	}

	private fun onToggle(running: Boolean) { //FIXME
		player.information(if (running) "Enabled" else "Disabled")
		show()

		data.isRunning = running
		data.update(multiblock)

		multiblock.line(3, if (running) text("Running", NamedTextColor.GREEN) else text("Disabled", NamedTextColor.RED))
		multiblock.update()
	}

	fun getRemainingPrice(): Int = 1 //TODO

//	fun checkObstructions(): Boolean {}

	private fun applyTransform(vector: BlockVector3): BlockVector3 {
		val x = vector.x
		val y = vector.y
		val z = vector.z
		val rotation = data.rotation

		val degrees: Double = (rotation.toDouble() * 90.0)

		val newX = (cos(degrees) * (x)) - (sin(degrees) * x)
		val newZ = (sin(degrees) * (z)) + (cos(degrees) * z)

		return BlockVector3.at(
			(newX + data.offsetX).toInt(),
			y + data.offsetY,
			(newZ + data.offsetZ).toInt()
		)
	}

	fun iterateRegion(blueprint: Blueprint, origin: BlockPos, action: (blueprintBlock: Block, worldBlock: Block) -> Unit) {
		val clipboard = getClipboard(blueprint)

		val (offsetX: Int , offsetY: Int , offsetZ: Int) = data

		val (signX, signY, signZ) = origin

		val originX = signX + offsetX
		val originY = signY + offsetY
		val originZ = signZ + offsetZ

		val region = clipboard.region.clone()
		val targetBlockVector: BlockVector3 = BlockVector3.at(data.offsetX, data.offsetY, data.offsetZ)
		val offset: BlockVector3 = targetBlockVector.subtract(clipboard.origin)


	}
}
