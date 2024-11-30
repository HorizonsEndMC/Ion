package net.horizonsend.ion.server.features.gui.custom.starship

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.schema.starships.StarshipData
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.serverErrorActionMessage
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toText
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.wrap
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.gui.custom.starship.pilots.ManagePilotsMenu
import net.horizonsend.ion.server.features.gui.custom.starship.type.ChangeTypeButton
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipComputers.canTakeOwnership
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipState
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipDetectedEvent
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.setDisplayNameAndGet
import net.horizonsend.ion.server.miscellaneous.utils.setLoreAndGet
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.kyori.adventure.text.Component
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
import java.util.concurrent.CompletableFuture

class StarshipComputerMenu(val player: Player, val data: PlayerStarshipData) {
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
			.setStructure("1 2 3 4 . . . 5 6")
			.addIngredient('1', reDetectButton)
			.addIngredient('2', changePilotsButton)
			.addIngredient('3', changeTypeButton)
			.addIngredient('4', toggleLockButton)
			.addIngredient('6', renameButton)

		if (canTakeOwnership(player, data)) {
			gui.addIngredient('5', takeOwnershipButton)
		}

		return gui.build()
	}

	private val reDetectButton = object : AbstractItem() {
		var lore = listOf<Component>(
			text("Use this button to detect", GRAY).itemName,
			text("a new state of your ship.", GRAY).itemName,
			text("This is needed if you change", GRAY).itemName,
			text("any blocks.", GRAY).itemName
		)

		val provider = ItemProvider {
			ItemStack(Material.CLOCK)
				.setDisplayNameAndGet(text("Re-Detect Ship").itemName)
				.setLoreAndGet(lore)
		}

		override fun getItemProvider(): ItemProvider = provider

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val result = tryReDetect(player, data)

			Tasks.async {
				result.thenAccept {
					lore = it.information

					Tasks.sync {
						notifyWindows()
					}
				}
			}
		}
	}

	private val changePilotsButton = ManagePilotsMenu(this)

	private val changeTypeButton = ChangeTypeButton(this)

	private val toggleLockButton = object : AbstractItem() {
		override fun getItemProvider(): ItemProvider = ItemProvider {
			ItemStack(Material.IRON_DOOR)
				.setDisplayNameAndGet(text("Toggle Ship Lock", if (data.isLockEnabled) GREEN else RED).itemName)
				.setLoreAndGet(listOf(
					ofChildren(text("Status: ", GRAY), if (data.isLockEnabled) text("ENABLED", GREEN) else text("DISABLED", RED)).itemName,
					empty(),
					text("Ship locks protect your ship", GRAY).itemName,
					text("from access by people not", GRAY).itemName,
					text("added as pilots. They enable", GRAY).itemName,
					text("5 minutes after being released.", GRAY).itemName
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

	private val takeOwnershipButton = createButton(
		ItemStack(Material.RECOVERY_COMPASS)
			.setDisplayNameAndGet(text("Take ownership").itemName)
			.setLoreAndGet(listOf<Component>(
				template(text("Current owner: {0}.", GRAY), SLPlayer.getName(data.captain)).itemName,
				text("Use this button to take", GRAY).itemName,
				text("ownership of this starship.", GRAY).itemName
			))
	) { _, player, _ ->
		StarshipComputers.takeOwnership(player, data)
		player.success("Took ownership of this computer")
	}

	private val renameButton = RenameButton(this)

	val mainMenuButton = createButton(
		ItemStack(Material.BARRIER).setDisplayNameAndGet(text("Go back to main menu", WHITE).itemName)
	) { _, player, _ ->
		player.closeInventory()
		open()
	}

	private val lockMap = mutableMapOf<Oid<out StarshipData>, Any>()

	private fun getLock(dataId: Oid<out StarshipData>): Any = lockMap.getOrPut(dataId) { Any() }

	private fun tryReDetect(player: Player, data: StarshipData): CompletableFuture<DetectionResult> {
		if (ActiveStarships.findByPilot(player) != null) player.userError("WARNING: Redetecting while piloting will not succeed. You must release first, then redetect.")
		val future = CompletableFuture<DetectionResult>()

		Tasks.async {
			synchronized(getLock(data._id)) {
				val state = try {
					StarshipDetection.detectNewState(data, player)
				} catch (e: StarshipDetection.DetectionFailedException) {
					player.serverErrorActionMessage("${e.message} Detection failed!")
					player.hint("Is it touching another structure?")

					val split = text(e.message ?: "Unspecified", RED).itemName.wrap(150).toTypedArray()

					future.complete(DetectionResult(listOf(
						text("Detection failed!", RED).itemName,
						*split
					)))

					return@async
				} catch (e: Exception) {
					e.printStackTrace()
					player.serverErrorActionMessage("An error occurred while detecting")
					future.complete(DetectionResult(listOf(
						text("There was an unknown error during detection.").itemName
							.hoverEvent(text(e.message ?: "NULL")),
						text("Please report this to staff").itemName
							.hoverEvent(text(e.message ?: "NULL"))
					)))

					return@async
				}

				StarshipDetectedEvent(player, player.world).callEvent()
				player.rewardAchievement(Achievement.DETECT_SHIP)

				DeactivatedPlayerStarships.updateState(data, state)

				if (state.blockMap.size > data.starshipType.actualType.maxSize) {
					future.complete(DetectionResult(listOf(
						text("Success!", GREEN).itemName,
						text("Re-detected! New size ${state.blockMap.size.toText()}", GREEN).itemName,
						text("Detected starship is oversized! It will suffer a performance penalty.", RED).itemName
					)))

					player.success("Re-detected! New size ${state.blockMap.size.toText()}")
					player.userError("Detected starship is oversized! It will suffer a performance penalty.")

					return@async
				}

				future.complete(DetectionResult(listOf(
					text("Success!", GREEN).itemName,
					text("Re-detected! New size ${state.blockMap.size.toText()}", GREEN).itemName
				)))

				player.success("Re-detected! New size ${state.blockMap.size.toText()}")
			}
		}

		return future
	}

	data class DetectionResult(val information: List<Component>)
}
