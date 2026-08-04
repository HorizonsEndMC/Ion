package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.giveOrDropItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.random.Random

class WreckHackingGui(
	viewer: Player,
	val chest: Chest,
	private var attemptsRemaining: Int = 5
) : InvUIWindowWrapper(viewer) {

	companion object {
		const val WINDOW_WIDTH = 9
		const val WINDOW_HEIGHT = 6
		const val LIVES_PER_ATTEMPT = 4

		const val NODE_EMPTY = 1
		const val NODE_TRAP = 2
		const val NODE_CORE = 3

		enum class Difficulty(val horizontalGridSize: Int, val verticalGridSize: Int, val trapCount: Int) {
			EASY(5, 4, 3),
			MEDIUM(7, 5, 5),
			HARD(9, 6, 7)
		}
	}

	private val difficulty: Difficulty = Difficulty.entries.random()
	private val horizontalGridSize: Int = difficulty.horizontalGridSize
	private val verticalGridSize: Int = difficulty.verticalGridSize
	private val horizontalOffset: Int = (WINDOW_WIDTH - horizontalGridSize) / 2
	private val verticalOffset: Int = (WINDOW_HEIGHT - verticalGridSize) / 2

	private var grid = MutableList(verticalGridSize) { MutableList(horizontalGridSize) { NODE_EMPTY } }
	private var revealed = MutableList(verticalGridSize) { MutableList(horizontalGridSize) { false } }
	private var playerRow = 0
	private var playerCol = 0
	private var coreRow = 0
	private var coreCol = 0
	private var livesRemaining = LIVES_PER_ATTEMPT
	private var success = false

	init {
		generateGrid()
	}

	private fun generateGrid() {
		grid = MutableList(verticalGridSize) { MutableList(horizontalGridSize) { NODE_EMPTY } }
		revealed = MutableList(verticalGridSize) { MutableList(horizontalGridSize) { false } }
		livesRemaining = LIVES_PER_ATTEMPT

		// place player on a random edge node
		when (Random.nextInt(4)) {
			0 -> { playerRow = 0; playerCol = Random.nextInt(horizontalGridSize) }
			1 -> { playerRow = verticalGridSize - 1; playerCol = Random.nextInt(horizontalGridSize) }
			2 -> { playerRow = Random.nextInt(verticalGridSize); playerCol = 0 }
			else -> { playerRow = Random.nextInt(verticalGridSize); playerCol = horizontalGridSize - 1 }
		}

		// place core anywhere at least 4 distance from player
		do {
			coreRow = Random.nextInt(verticalGridSize)
			coreCol = Random.nextInt(horizontalGridSize)
		} while (abs(coreRow - playerRow) + abs(coreCol - playerCol) < 4)

		// Fill grid with empty cells plus the core
		for (r in 0 until verticalGridSize) {
			for (c in 0 until horizontalGridSize) {
				grid[r][c] = if (r == coreRow && c == coreCol) NODE_CORE else NODE_EMPTY
			}
		}

		// Place exactly difficulty.trapCount traps in random cells that aren't the player, the core, or adjacent to the player
		val trapCandidates = mutableListOf<Pair<Int, Int>>()
		for (r in 0 until verticalGridSize) {
			for (c in 0 until horizontalGridSize) {
				if ((r == playerRow && c == playerCol) || (r == coreRow && c == coreCol) || isAdjacentToPlayer(r, c)) continue
				trapCandidates += r to c
			}
		}
		trapCandidates.shuffle()
		for (i in 0 until minOf(difficulty.trapCount, trapCandidates.size)) {
			val (r, c) = trapCandidates[i]
			grid[r][c] = NODE_TRAP
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

	private fun isFirewall(row: Int, col: Int): Boolean =
		grid[row][col] == NODE_TRAP

	private fun isAdjacentToUnrevealedFirewall(row: Int, col: Int): Boolean {
		val neighbors = listOf(
			row - 1 to col,
			row + 1 to col,
			row to col - 1,
			row to col + 1
		)
		return neighbors.any { (r, c) ->
			r in 0 until verticalGridSize &&
				c in 0 until horizontalGridSize &&
				isFirewall(r, c) && !revealed[r][c]
		}
	}

	private fun getNodeItem(row: Int, col: Int): AbstractItem {
		return object : AbstractItem() {
			override fun getItemProvider(): ItemProvider {
				val isPlayer = row == playerRow && col == playerCol
				val isRevealed = revealed[row][col]
				val isAdjacent = isAdjacentToPlayer(row, col)

				return when {
					isPlayer && (isAdjacentToCore(row, col) || isAdjacentToUnrevealedFirewall(row, col)) ->ItemBuilder(ItemStack(Material.YELLOW_WOOL))
						.setDisplayName(AdventureComponentWrapper(text("You are here (something is nearby...)", GOLD)))

					isPlayer -> ItemBuilder(ItemStack(Material.GREEN_WOOL))
						.setDisplayName(AdventureComponentWrapper(text("You are here", GREEN)))

					isRevealed && isFirewall(row, col) -> ItemBuilder(ItemStack(Material.RED_STAINED_GLASS_PANE))
						.setDisplayName(AdventureComponentWrapper(text("Firewall", RED)))

					isRevealed && (isAdjacentToCore(row, col) || isAdjacentToUnrevealedFirewall(row, col)) -> ItemBuilder(ItemStack(Material.ORANGE_STAINED_GLASS_PANE))
						.setDisplayName(AdventureComponentWrapper(text("Something is nearby...", GOLD)))

					isRevealed -> ItemBuilder(ItemStack(Material.WHITE_STAINED_GLASS_PANE))
						.setDisplayName(AdventureComponentWrapper(text("Clear", WHITE)))

					isAdjacent -> ItemBuilder(ItemStack(Material.GRAY_STAINED_GLASS_PANE))
						.setDisplayName(AdventureComponentWrapper(text("Unknown - Click to move here", GRAY)))

					else -> ItemBuilder(ItemStack(Material.BLACK_STAINED_GLASS_PANE))
						.setDisplayName(AdventureComponentWrapper(text("Inaccessible", DARK_GRAY)))
				}
			}

			override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
				val isPlayer = row == playerRow && col == playerCol
				if (isPlayer || !isAdjacentToPlayer(row, col)) return
				handleMove(row, col)
			}
		}
	}

	private fun handleMove(row: Int, col: Int) {
		val nodeType = grid[row][col]

		when (nodeType) {
			NODE_CORE -> {
				// found winning thing
				success = true
				currentWindow?.close()
				unlockChest()
				return
			}
			NODE_TRAP -> {
				// found trap
				if (revealed[row][col]) return

				livesRemaining--
				viewer.sendMessage(text("⚡ Firewall hit! $livesRemaining lives remaining.", RED))
				viewer.world.playSound(Sound.sound(key("horizonsend:wrecks.hacking.firewall"), Sound.Source.PLAYER, 5.0f, 1.0f), viewer)
				revealed[row][col] = true

				if (livesRemaining <= 0) {
					attemptsRemaining--
					viewer.sendMessage(text("Attempt failed! $attemptsRemaining attempts remaining.", RED))

					if (attemptsRemaining <= 0) {
						// Lock chest forever
						viewer.userError("Too many failed hacking attempts!")
						destroyChest()
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

	private fun unlockChest() {
		Tasks.sync {
			// Make this no longer a wreck chest
			chest.persistentDataContainer.remove(NamespacedKeys.WRECK_CHEST)
			chest.update()

			// fill with rewards
			val inventory = chest.inventory
			inventory.clear()

			giveOrDropItems(CustomItemKeys.DATA_CHIP.getValue().constructItemStack(), Random.nextInt(5, 13), viewer)
			giveOrDropItems(CustomItemKeys.GUIDANCE_SYSTEM.getValue().constructItemStack(), Random.nextInt(1, 4), viewer)
			giveOrDropItems(CustomItemKeys.SUPERCONDUCTOR.getValue().constructItemStack(), Random.nextInt(3, 13), viewer)
			giveOrDropItems(CustomItemKeys.SCORDITE.getValue().constructItemStack(), Random.nextInt(0, 18), viewer)
			giveOrDropItems(CustomItemKeys.VANADIUM.getValue().constructItemStack(), Random.nextInt(0, 9), viewer)
			if (ThreadLocalRandom.current().nextDouble(0.0, 1.0) < 0.25) {
				giveOrDropItems(CustomItemKeys.ZIRCON.getValue().constructItemStack(), Random.nextInt(1, 4), viewer)
			}
			if (ThreadLocalRandom.current().nextDouble(0.0, 1.0) < 0.05) {
				giveOrDropItems(CustomItemKeys.ZIRCON_BLOCK.getValue().constructItemStack(), Random.nextInt(1, 2), viewer)
			}

			viewer.world.playSound(Sound.sound(key("horizonsend:wrecks.hacking.completion"), Sound.Source.PLAYER, 5.0f, 1.0f), viewer)
			viewer.success("Hacking successful! The chest has been unlocked.")
		}
	}

	private fun destroyChest() {
		if (success) return
		Tasks.sync {
			chest.persistentDataContainer.remove(NamespacedKeys.WRECK_CHEST)
			chest.update()
			chest.inventory.clear()
		}
		currentWindow?.close()
		viewer.userError("The security system has self-destructed the contents within.")
	}

	override fun buildTitle(): Component = GuiText("Hacking Terminal").build()

	override fun buildWindow(): Window = normalWindow(buildGui(), closeHandlers = listOf(Runnable {
		if (!success) {
			viewer.userError("The hacking sequence was interrupted!")
		}
		destroyChest()
	}))

	private fun buildGui(): Gui {
		val gui = Gui.empty(WINDOW_WIDTH, WINDOW_HEIGHT)

		for (row in 0 until verticalGridSize) {
			for (col in 0 until horizontalGridSize) {
				val slot = (row + verticalOffset) * WINDOW_WIDTH + (col + horizontalOffset)
				gui.setItem(slot, tracked { _ -> getNodeItem(row, col) })
			}
		}

		return gui
	}
}
