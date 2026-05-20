package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import kotlin.math.abs
import kotlin.random.Random

class WreckHackingGui(
	viewer: Player,
	val chest: Chest,
	private var attemptsRemaining: Int = 3
) : InvUIWindowWrapper(viewer) {

	companion object {
		const val GRID_SIZE = 7
		const val LIVES_PER_ATTEMPT = 3

		const val NODE_EMPTY = 1
		const val NODE_TRAP = 2
		const val NODE_CORE = 3

		const val TRAP_CHANCE = 0.25
	}

	private var grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
	private var revealed = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
	private var playerRow = 0
	private var playerCol = 0
	private var coreRow = 0
	private var coreCol = 0
	private var livesRemaining = LIVES_PER_ATTEMPT

	init {
		generateGrid()
	}

	private fun generateGrid() {
		grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
		revealed = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
		livesRemaining = LIVES_PER_ATTEMPT

		// place player on a random edge node
		when (Random.nextInt(4)) {
			0 -> { playerRow = 0; playerCol = Random.nextInt(GRID_SIZE) }
			1 -> { playerRow = GRID_SIZE - 1; playerCol = Random.nextInt(GRID_SIZE) }
			2 -> { playerRow = Random.nextInt(GRID_SIZE); playerCol = 0 }
			else -> { playerRow = Random.nextInt(GRID_SIZE); playerCol = GRID_SIZE - 1 }
		}

		// place core anywhere at least 4 distance from player
		do {
			coreRow = Random.nextInt(GRID_SIZE)
			coreCol = Random.nextInt(GRID_SIZE)
		} while (abs(coreRow - playerRow) + abs(coreCol - playerCol) < 4)

		// Fill grid
		for (r in 0 until GRID_SIZE) {
			for (c in 0 until GRID_SIZE) {
				grid[r][c] = when {
					r == coreRow && c == coreCol -> NODE_CORE
					r == playerRow && c == playerCol -> NODE_EMPTY
					else -> if (Random.nextDouble() < TRAP_CHANCE) NODE_TRAP else NODE_EMPTY
				}
			}
		}

		// starting position
		revealed[playerRow][playerCol] = true
	}

	private fun isAdjacentToCore(row: Int, col: Int): Boolean {
		return (abs(row - coreRow) == 1 && col == coreCol) ||
			(abs(col - coreCol) == 1 && row == coreRow)
	}

	private fun isAdjacentToPlayer(row: Int, col: Int): Boolean {
		return (abs(row - playerRow) == 1 && col == playerCol) ||
			(abs(col - playerCol) == 1 && row == playerRow)
	}

	private fun getNodeItem(row: Int, col: Int): AbstractItem {
		val isPlayer = row == playerRow && col == playerCol
		val isRevealed = revealed[row][col]
		val isAdjacent = isAdjacentToPlayer(row, col)

		val item = when {
			isPlayer -> ItemBuilder(ItemStack(Material.GREEN_WOOL))
				.setDisplayName("§aYou are here")

			isRevealed && isAdjacentToCore(row, col) -> ItemBuilder(ItemStack(Material.ORANGE_STAINED_GLASS_PANE))
				.setDisplayName("§6Something is nearby...")

			isRevealed -> ItemBuilder(ItemStack(Material.WHITE_STAINED_GLASS_PANE))
				.setDisplayName("§fClear")

			isAdjacent -> ItemBuilder(ItemStack(Material.GRAY_STAINED_GLASS_PANE))
				.setDisplayName("§7Unknown - Click to move here")

			else -> ItemBuilder(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
				.setDisplayName("§8Inaccessible")
		}

		return object : AbstractItem() {
			override fun getItemProvider(): ItemProvider = item

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				if (!isAdjacent || isPlayer) return
				handleMove(row, col)
			}
		}
	}

	private fun handleMove(row: Int, col: Int) {
		val nodeType = grid[row][col]

		when (nodeType) {
			NODE_CORE -> {
				// found winning thing
				currentWindow?.close()
				WreckChestListener.unlockChest(chest, viewer)
				return
			}
			NODE_TRAP -> {
				// found trap
				livesRemaining--
				viewer.sendMessage(text("⚡ Firewall hit! ${livesRemaining} lives remaining.", RED))

				if (livesRemaining <= 0) {
					attemptsRemaining--
					viewer.sendMessage(text("Attempt failed! $attemptsRemaining attempts remaining.", RED))

					if (attemptsRemaining <= 0) {
						// Lock chest forever
						currentWindow?.close()
						Tasks.sync {
							chest.persistentDataContainer.set(NamespacedKeys.WRECK_CHEST_LOCKED, PersistentDataType.BOOLEAN, false)
							chest.persistentDataContainer.set(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN, false)
							chest.inventory.clear()
							chest.update()
						}
						viewer.sendMessage(text("The system has locked you out permanently!", RED))
						return
					}

					// reset grid for new attempt
					generateGrid()
					refreshButtons()
					return
				}

				// stay on current node and just refresh
				refreshButtons()
				return
			}
			NODE_EMPTY -> {
				// move to node
				playerRow = row
				playerCol = col
				revealed[row][col] = true
				refreshButtons()
			}
		}
	}

	override fun buildTitle(): Component = GuiText("Hacking Terminal").build()

	override fun buildWindow(): Window = normalWindow(buildGui())

	private fun buildGui(): Gui {
		val gui = Gui.empty(GRID_SIZE, GRID_SIZE)

		for (row in 0 until GRID_SIZE) {
			for (col in 0 until GRID_SIZE) {
				val slot = row * GRID_SIZE + col
				gui.setItem(slot, tracked { _ -> getNodeItem(row, col) })
			}
		}

		return gui
	}
}
