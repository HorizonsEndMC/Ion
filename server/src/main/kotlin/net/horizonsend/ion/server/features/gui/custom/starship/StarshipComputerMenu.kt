package net.horizonsend.ion.server.features.gui.custom.starship

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.serverErrorActionMessage
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.ITALIC
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipState
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipDetectedEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.setLoreAndGet
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window

class StarshipComputerMenu(val player: Player, val data: StarshipData) {
	fun open() {
		val state: StarshipState? = DeactivatedPlayerStarships.getSavedState(data)
		val title = if (state != null)
			(data as? PlayerStarshipData)?.name?.miniMessage() ?: data.starshipType.actualType.displayNameComponent
			else text("Starship Computer")

		Window.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(title))
			.setGui(formatGui())
			.build()
			.open()
	}

	private fun formatGui(): Gui {
		val gui = Gui.normal()
			.setStructure("1 2 3 4 . . . . 5")
			.addIngredient('1', reDetectButton)
			.addIngredient('2', changePilotsButton)
			.addIngredient('3', changeClassButton)
			.addIngredient('4', toggleLockButton)
			.addIngredient('5', renameButton)

		return gui.build()
	}

	private val reDetectButton = createButton(
		ItemStack(Material.CLOCK)
			.setDisplayNameAndGet(text("Re-Detect Ship").decoration(ITALIC, false))
			.setLoreAndGet(listOf(
				text("Use this button to detect", GRAY).decoration(ITALIC, false),
				text("a new state of your ship.", GRAY).decoration(ITALIC, false),
				text("This is needed if you change", GRAY).decoration(ITALIC, false),
				text("any blocks.", GRAY).decoration(ITALIC, false)
			))
	) { _, player, _ ->
		tryReDetect(player, data)
	}

	private val changePilotsButton = ManagePilotsMenu(this)

	private val changeClassButton = createButton(
		ItemStack(Material.GHAST_TEAR)
			.setDisplayNameAndGet(text("Change Ship Class").decoration(ITALIC, false))
			.setLoreAndGet(listOf(
				ofChildren(text("Current type: ", GRAY), data.starshipType.actualType.displayNameComponent).decoration(ITALIC, false),
				empty(),
				text("Different starship types", GRAY).decoration(ITALIC, false),
				text("support different block", GRAY).decoration(ITALIC, false),
				text("counts, weapons, and tools.", GRAY).decoration(ITALIC, false)
			))
	) { type, player, event ->

	}

	private val toggleLockButton = object : AbstractItem() {
		override fun getItemProvider(): ItemProvider = ItemProvider {
			ItemStack(Material.IRON_DOOR)
				.setDisplayNameAndGet(text("Toggle Ship Lock", if (data.isLockEnabled) GREEN else RED).decoration(ITALIC, false))
				.setLoreAndGet(listOf(
					ofChildren(text("Status: ", GRAY), if (data.isLockEnabled) text("ENABLED", GREEN) else text("DISABLED", RED)).decoration(ITALIC, false),
					empty(),
					text("Ship locks protect your ship", GRAY).decoration(ITALIC, false),
					text("from access by people not", GRAY).decoration(ITALIC, false),
					text("added as pilots. They enable", GRAY).decoration(ITALIC, false),
					text("5 minutes after being released.", GRAY).decoration(ITALIC, false)
				))
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Tasks.async {
			val newValue = !data.isLockEnabled

			DeactivatedPlayerStarships.updateLockEnabled(data, newValue)
			data.isLockEnabled = newValue

			if (newValue) {
				player.success("Enabled Lock")
			} else {
				player.success("Disabled Lock")
			}

			Tasks.sync {
				notifyWindows()
			}
		}
	}

	private val renameButton = RenameButton(this)

	val mainMenuButton = createButton(
		ItemStack(Material.BARRIER).setDisplayNameAndGet(text("Go back to main menu", WHITE).decoration(ITALIC, false))
	) { _, player, _ ->
		player.closeInventory()
		open()
	}

	private val lockMap = mutableMapOf<Oid<out StarshipData>, Any>()

	private fun getLock(dataId: Oid<out StarshipData>): Any = lockMap.getOrPut(dataId) { Any() }

	private fun tryReDetect(player: Player, data: StarshipData) {
		if (ActiveStarships.findByPilot(player) != null) player.userError("WARNING: Redetecting while piloting will not succeed. You must release first, then redetect.")

		Tasks.async {
			synchronized(getLock(data._id)) {
				val state = try {
					StarshipDetection.detectNewState(data, player)
				} catch (e: StarshipDetection.DetectionFailedException) {
					player.serverErrorActionMessage("${e.message} Detection failed!")
					player.hint("Is it touching another structure?")

					return@async
				} catch (e: Exception) {
					e.printStackTrace()
					player.serverErrorActionMessage("An error occurred while detecting")

					return@async
				}

				StarshipDetectedEvent(player, player.world).callEvent()
				player.rewardAchievement(Achievement.DETECT_SHIP)

				DeactivatedPlayerStarships.updateState(data, state)

				player.success("Re-detected! New size ${state.blockMap.size.toText()}")
			}
		}
	}
}
