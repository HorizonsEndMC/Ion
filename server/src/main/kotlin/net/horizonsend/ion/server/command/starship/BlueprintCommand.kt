package net.horizonsend.ion.server.command.starship

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BlockState
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.common.database.schema.starships.PlayerStarshipData
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.isAlphanumeric
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.starship.DeactivatedPlayerStarships
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.StarshipComputers
import net.horizonsend.ion.server.features.starship.StarshipDetection
import net.horizonsend.ion.server.features.starship.StarshipSchematic
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.factory.PrintItem
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.horizonsend.ion.server.gui.invui.misc.BlueprintMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.miscellaneous.registrations.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.createData
import net.horizonsend.ion.server.miscellaneous.utils.loadClipboard
import net.horizonsend.ion.server.miscellaneous.utils.nms
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.toBukkitBlockData
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.world.level.block.EntityBlock
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.litote.kmongo.save
import java.util.LinkedList
import java.util.Locale
import java.util.UUID

@CommandAlias("blueprint|bp")
object BlueprintCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "blueprints") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId
			Blueprint.col.find(Blueprint::owner eq slPlayerId).map { it.name }.toList()
		}

		registerAsyncCompletion(manager, "sharedblueprints") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId
			Blueprint.col.find(
				or(
					Blueprint::trustedPlayers contains slPlayerId,
					Blueprint::trustedNations contains SLPlayer[slPlayerId]?.nation
				)
			).map { it.name }.toList()
		}
	}

	private fun getMaxBlueprints(player: Player): Int {
		return Levels[player] * 3 + 20
	}

	private fun validateName(name: String) {
		failIf(name != name.lowercase(Locale.getDefault())) {
			"Name must be lowercase"
		}
		failIf(name.length !in 2..50) {
			"Name length is ${name.length}, must be between 2 and 50"
		}
		failIf(!name.replace('-', ' ').replace('_', ' ').isAlphanumeric(includeSpaces = true)) {
			"Name must only contain letters, numbers, and - or _"
		}
	}

	@Subcommand("save")
	fun onSave(sender: Player, name: String, @Optional confirm: String?) = asyncCommand(sender) {
		failIf(confirm != "confirm") {
			"YOUR BLUEPRINT IS NOT SAVED. READ THIS NOTICE FIRST!\nTo save a blueprint, you must acknowledge that you understand that you cannot save blueprints for any purpose of, for example, copying ship designs, or basically saving blueprints of any ships that you didn't design or get permission from the designer to save. This also applies to schematica and similar mods. To acknowledge this, do /blueprint save <name> confirm.\nYOUR BLUEPRINT IS NOT SAVED. READ THIS NOTICE FIRST!"
		}

		val createNew = Blueprint.none(and(Blueprint::owner eq sender.slPlayerId, Blueprint::name eq name))
		val slPlayerId = sender.slPlayerId
		val starship = getStarshipPiloting(sender)

		val starshipData = starship.data
		failIf(starshipData is PlayerStarshipData && starshipData.disallowBlueprinting) { "You cannot blueprint ships you do did not create without explicit permission from the creator!" }

		validateName(name)
		var pilotLoc = Vec3i(sender.location)
		failIf(!starship.isWithinHitbox(pilotLoc.x, pilotLoc.y, pilotLoc.z, 1)) {
			"Must be inside the ship."
		}

		val schem = Tasks.getSyncBlocking { StarshipSchematic.createSchematic(starship) }
		val data = Blueprint.createData(schem)

		pilotLoc = Vec3i(pilotLoc.x - schem.origin.x(), pilotLoc.y - schem.origin.y(), pilotLoc.z - schem.origin.z())

		if (createNew) {
			failIf(Blueprint.count(Blueprint::owner eq slPlayerId) > getMaxBlueprints(sender)) {
				"You can only have up to ${getMaxBlueprints(sender)} blueprints."
			}

			Blueprint.create(slPlayerId, name, starship.data.starshipType, pilotLoc, starship.initialBlockCount, data)
			sender.success("Saved blueprint $name")
		} else {
			val target = sender.slPlayerId

			val blueprint = getBlueprint(target, name)

			blueprint.blockData = data
			blueprint.pilotLoc = pilotLoc
			blueprint.type = starship.data.starshipType
			blueprint.size = starship.initialBlockCount

			saveBlueprint(blueprint)
			sender.success("Updated blueprint $name")
		}
	}

	/**
	 * Gets the blueprint with the given [name] for the given [sender].
	 *
	 * There should not be any duplicates as [onSave] should handle updating existing blueprints.
	 */
	private fun getBlueprint(sender: SLPlayerId, name: String): Blueprint {
		return Blueprint.find(and(Blueprint::owner eq sender, Blueprint::name eq name)).first()
			?: fail { "You don't have a blueprint named $name." }
	}

	/**
	 * Gets all blueprints with the given [name] that are shared to [sender].
	 *
	 * There may be duplicates as other people may have shared the same blueprint with this user.
	 */
	private fun getSharedBlueprints(sender: SLPlayerId, name: String): Set<Blueprint> {
		return Blueprint.find(
			and(
				or(
					Blueprint::trustedPlayers contains sender,
					Blueprint::trustedNations contains SLPlayer[sender]?.nation
				),
				Blueprint::name eq name
			)).toSet()
	}

	private fun saveBlueprint(blueprint: Blueprint) {
		Blueprint.col.save(blueprint)
	}

	@Subcommand("delete")
	@CommandCompletion("@blueprints")
	fun onDelete(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId

		val blueprint = getBlueprint(target, name)
		// TODO: confirm menu
		Blueprint.delete(blueprint._id)
		sender.success("Deleted blueprint ${blueprint.name}")
	}

	@Subcommand("delete other")
	@CommandPermission("starships.blueprint.delete.other")
	@CommandCompletion("@players blueprintName")
	fun onDeleteOther(sender: Player, player: String, blueprint: String) = asyncCommand(sender) {
		val target = SLPlayer[player]?._id // Database lookup so it works when the player is offline

		if (target == null) {
			sender.userError("Player $player not found or not online.")
			return@asyncCommand
		}

		val blueprint = getBlueprint(target, blueprint)

		Blueprint.delete(blueprint._id)
		sender.success("Deleted blueprint ${blueprint.name} from $player")
	}

	fun blueprintInfo(blueprint: Blueprint): List<String> {
		val list = LinkedList<String>()
		val cost = calculateBlueprintCost(blueprint)
		list.add("<gray>Size<dark_gray>: <gold>${blueprint.size}")
		list.add("<gray>Cost<dark_gray>: <gold>$$cost")
		list.add("<gray>Class<dark_gray>: <light_purple>${blueprint.type}")
		if (blueprint.trustedPlayers.isNotEmpty()) {
			list.add("<gray>Trusted Players<dark_gray>: <aqua>${blueprint.trustedPlayers.joinToString { getPlayerName(it) }}")
		}
		if (blueprint.trustedNations.isNotEmpty()) {
			list.add("<gray>Trusted Nations<dark_gray>: <aqua>${blueprint.trustedNations.joinToString { NationCache[it].name }}")
		}
		return list
	}

	private fun calculateBlueprintCost(blueprint: Blueprint): Int {
		val clipboard = blueprint.loadClipboard()

		return clipboard.region
			.map { clipboard.getBlock(it).toBukkitBlockData() }
			.filter { !it.material.isAir }
			.sumOf { ShipFactoryMaterialCosts.getPrice(it) }
			.toInt()
	}

	@Suppress("Unused")
	@Subcommand("list personal")
	fun onListPersonal(sender: Player) = asyncCommand(sender) {
		val slPlayerId = sender.slPlayerId

		failIf(!Blueprint.any(Blueprint::owner eq slPlayerId)) { "You have no blueprints!" }

		BlueprintMenu(sender) { blueprint, player ->
			player.closeInventory()
			Tasks.async { showMaterials(player, blueprint) }
		}.openGui()
	}

	@Suppress("Unused")
	@Subcommand("list shared")
	fun onListShared(sender: Player) = asyncCommand(sender) {
		val slPlayerId = sender.slPlayerId

		val accessibleBlueprints = or(
			Blueprint::trustedPlayers contains slPlayerId,
			Blueprint::trustedNations contains SLPlayer[slPlayerId]?.nation
		)

		failIf(!Blueprint.any(accessibleBlueprints)) { "You have no shared blueprints!" }

		BlueprintMenu(sender, shared = true) { blueprint, player ->
			player.closeInventory()
			Tasks.async { showMaterials(player, blueprint) }
		}.openGui()
	}

	@Suppress("Unused")
	@Subcommand("list other")
	@CommandPermission("starships.blueprint.list.other")
	@CommandCompletion("@players")
	fun onListOther(sender: Player, player: String) = asyncCommand(sender) {
		val target = SLPlayer[player] ?: fail { "Player $player not found or not online." } // Database lookup so it works when the player is offline

		failIf(!Blueprint.any(Blueprint::owner eq target._id)) { "${target.lastKnownName} has no blueprints!" }

		BlueprintMenu(sender, target._id) { blueprint, _ ->
			sender.closeInventory()
			Tasks.async { showMaterials(sender, blueprint) }
		}.openGui()
	}

	@Suppress("Unused")
	@Subcommand("list shared other")
	@CommandPermission("starships.blueprint.list.other")
	@CommandCompletion("@players")
	fun onListSharedOther(sender: Player, player: String) = asyncCommand(sender) {
		val target = SLPlayer[player] ?: fail { "Player $player not found or not online." } // Database lookup so it works when the player is offline

		val accessibleBlueprints = or(
			Blueprint::trustedPlayers contains target._id,
			Blueprint::trustedNations contains SLPlayer[target._id]?.nation
		)

		failIf(!Blueprint.any(accessibleBlueprints)) {
			sender.userError("${target.lastKnownName} have no shared blueprints!").toString()
		}

		BlueprintMenu(sender, shared = true) { blueprint, _ ->
			sender.closeInventory()
			Tasks.async { showMaterials(sender, blueprint) }
		}.openGui()
	}

	@Suppress("Unused")
	@Subcommand("info personal")
	@CommandCompletion("@blueprints")
	fun onInfoPersonal(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprint = getBlueprint(target, name)
		sender.sendRichMessage(blueprintInfo(blueprint).joinToString("\n"))
	}

	@Suppress("Unused")
	@Subcommand("info shared")
	@CommandCompletion("@sharedblueprints")
	fun onInfoShared(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprints = getSharedBlueprints(target, name)

		handleMultipleFoundBlueprints(sender, blueprints, name) { player, foundBlueprint ->
			player.sendRichMessage(blueprintInfo(foundBlueprint).joinToString("\n"))
		}
	}

	@Suppress("Unused")
	@Subcommand("materials personal")
	@CommandCompletion("@blueprints")
	fun onMaterialsPersonal(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprint = getBlueprint(target, name)
		showMaterials(sender, blueprint)
	}

	@Suppress("Unused")
	@Subcommand("materials shared")
	@CommandCompletion("@sharedblueprints")
	fun onMaterialsShared(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprints = getSharedBlueprints(target, name)

		handleMultipleFoundBlueprints(sender, blueprints, name) { player, foundBlueprint ->
			showMaterials(player, foundBlueprint)
		}
	}

	@Suppress("Unused")
	@Subcommand("load personal")
	@CommandPermission("starships.blueprint.load")
	@CommandCompletion("@blueprints")
	fun onLoadPersonal(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprint = getBlueprint(target, name)
		val schematic: Clipboard = blueprint.loadClipboard()
		val pilotLoc = blueprint.pilotLoc

		Tasks.syncBlocking {
			checkObstruction(sender.location, schematic, Vec3i(pilotLoc))

			loadSchematic(sender.location, schematic, Vec3i(pilotLoc)) { origin ->
				tryPilot(sender, origin, blueprint.type.actualType, blueprint.name)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("load shared")
	@CommandPermission("starships.blueprint.load")
	@CommandCompletion("@sharedblueprints")
	fun onLoadShared(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprints = getSharedBlueprints(target, name)

		handleMultipleFoundBlueprints(sender, blueprints, name) { player, foundBlueprint ->
			val schematic: Clipboard = foundBlueprint.loadClipboard()
			val pilotLoc = foundBlueprint.pilotLoc

			Tasks.syncBlocking {
				checkObstruction(sender.location, schematic, Vec3i(pilotLoc))

				loadSchematic(sender.location, schematic, Vec3i(pilotLoc)) { origin ->
					tryPilot(sender, origin, foundBlueprint.type.actualType, foundBlueprint.name)
				}
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("load personal other")
	@CommandPermission("starships.blueprint.load.other")
	@CommandCompletion("@players blueprintName")
	fun onLoadPersonalOther(sender: Player, player: String, blueprintName: String) = asyncCommand(sender) {
		val target = SLPlayer[player]?._id ?: fail { "Player $player not found" }
		val blueprint = getBlueprint(target, blueprintName)
		val schematic: Clipboard = blueprint.loadClipboard()
		val pilotLoc = blueprint.pilotLoc

		Tasks.syncBlocking {
			checkObstruction(sender.location, schematic, Vec3i(pilotLoc))

			loadSchematic(sender.location, schematic, Vec3i(pilotLoc)) { origin ->
				tryPilot(sender, origin, blueprint.type.actualType, blueprint.name)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("load shared other")
	@CommandPermission("starships.blueprint.load.other")
	@CommandCompletion("@players blueprintName")
	fun onLoadSharedOther(sender: Player, player: String, blueprintName: String) = asyncCommand(sender) {
		val target = SLPlayer[player]?._id ?: fail { "Player $player not found" }
		val blueprints = getSharedBlueprints(target, blueprintName)

		handleMultipleFoundBlueprints(sender, blueprints, blueprintName) { player, foundBlueprint ->
			val schematic: Clipboard = foundBlueprint.loadClipboard()
			val pilotLoc = foundBlueprint.pilotLoc

			Tasks.syncBlocking {
				checkObstruction(sender.location, schematic, Vec3i(pilotLoc))

				loadSchematic(sender.location, schematic, Vec3i(pilotLoc)) { origin ->
					tryPilot(sender, origin, foundBlueprint.type.actualType, foundBlueprint.name)
				}
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("fix")
	@CommandPermission("starships.blueprint.load")
	@CommandCompletion("@blueprints")
	fun onFix(sender: Player, name: String) = asyncCommand(sender) {
		val target = sender.slPlayerId
		val blueprint = getBlueprint(target, name)
		val schematic: Clipboard = blueprint.loadClipboard()
		val pilotLoc = blueprint.pilotLoc

		Tasks.syncBlocking {
			checkObstruction(sender.location, schematic, Vec3i(pilotLoc))

			loadSchematic(sender.location, schematic, Vec3i(pilotLoc)) { origin ->
				tryPilot(sender, origin, blueprint.type.actualType, blueprint.name) { starship ->

					starship.iterateBlocks { x, y, z ->
						val block = starship.world.getBlockAt(x, y, z)
						val blockData = block.blockData
						if (blockData.nms.block is EntityBlock) {
							return@iterateBlocks
						}
						block.setType(Material.AIR, false)
						block.setBlockData(blockData, true)
					}
					onSave(sender, name, "confirm")
				}
			}
		}
	}

	fun checkObstruction(location: Location, schematic: Clipboard, pilotLoc: Vec3i) {
		val world = BukkitAdapter.adapt(location.world)
		val vec: BlockVector3 = getPasteVector(location, pilotLoc)
		val region = schematic.region.clone()
		val offset = vec.subtract(schematic.origin)
		for (point in region) {
			failIf(!isAir(schematic.getBlock(point)) && !isAir(world.getBlock(point.add(offset)))) {
				"Obstructed at $point"
			}
		}
	}

	fun loadSchematic(location: Location, schematic: Clipboard, pilotLoc: Vec3i, callback: (Vec3i) -> Unit = {}) {
		val vec: BlockVector3 = getPasteVector(location, pilotLoc)
		val vec3i = Vec3i(vec.x(), vec.y(), vec.z())

		placeSchematicEfficiently(schematic, location.world, vec3i, true) {
			callback(vec3i)
		}
	}

	fun getPasteVector(origin: Location, pilotLoc: Vec3i): BlockVector3 {
		return BukkitAdapter.asVector(origin).toBlockPoint().subtract(BlockVector3.at(pilotLoc.x, pilotLoc.y, pilotLoc.z))
	}

	fun tryPilot(
		sender: Player,
		origin: Vec3i,
		type: StarshipType,
		name: String,
		callback: (Starship) -> Unit = {}
	) {
		val block = sender.world.getBlockAtKey(origin.toBlockKey())

		if (block.type != StarshipComputers.COMPUTER_TYPE) {
			sender.userError("${block.type} at $origin was not a starship computer, failed to pilot")
			return
		}

		DeactivatedPlayerStarships.createPlayerShipAsync(block.world, block.x, block.y, block.z, sender.uniqueId, name) { data ->
			Tasks.async {
				try {
					DeactivatedPlayerStarships.updateType(data, type)
					val state = StarshipDetection.detectNewState(data, sender)
					DeactivatedPlayerStarships.updateState(data, state)
					Tasks.sync {
						PilotedStarships.tryPilot(sender, data) {
							callback(it)
						}
					}
				} catch (e: StarshipDetection.DetectionFailedException) {
					sender.userError("Detection failed: ${e.message}")
				}
			}
		}
	}

	fun isAir(state: BlockState?) = state?.blockType?.material?.isAir != false

	fun showMaterials(sender: Player, blueprint: Blueprint) {
		val clipboard = blueprint.loadClipboard()

		val map = mutableMapOf<PrintItem, Int>()

		for (vec in clipboard.region) {
			val state = clipboard.getBlock(vec) ?: continue
			val blockData = state.toBukkitBlockData()

			if (blockData.material.isAir) {
				continue
			}

			val printItem = PrintItem[blockData] ?: continue
			val amount = StarshipFactories.getRequiredAmount(blockData)

			map[printItem] = map.getOrDefault(printItem, 0) + amount
		}

		sender.sendMessage(StarshipFactories.getPrintItemCountString(map))
	}

	@Suppress("Unused")
	@Subcommand("trust player")
	@CommandCompletion("@blueprints @players")
	fun onTrustPlayer(sender: Player, name: String, player: String) {
		val target = SLPlayer[sender.uniqueId]?._id ?: return // Silently fail if SLPlayer is somehow null
		val blueprint = getBlueprint(target, name)
		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId
		failIf(blueprint.trustedPlayers.contains(slPlayerId)) {
			"$player is already trusted, you might be looking for /blueprint untrust player $name $player"
		}
		blueprint.trustedPlayers.add(slPlayerId)
		saveBlueprint(blueprint)
		Notify.playerCrossServer(playerId, MiniMessage.miniMessage().deserialize("<aqua>${sender.name} <gray>trusted you to their blueprint <aqua>$name"))
		sender.success("Trusted $player to blueprint $name")
	}

	@Suppress("Unused")
	@Subcommand("untrust player")
	@CommandCompletion("@blueprints @players")
	fun onUntrustPlayer(sender: Player, name: String, player: String) {
		val target = SLPlayer[sender.uniqueId]?._id ?: return // Silently fail if SLPlayer is somehow null
		val blueprint = getBlueprint(target, name)
		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId
		failIf(!blueprint.trustedPlayers.contains(slPlayerId)) {
			"$player is not trusted, you might be looking for /blueprint trust player $name $player"
		}
		blueprint.trustedPlayers.remove(slPlayerId)
		saveBlueprint(blueprint)
		Notify.playerCrossServer(playerId, MiniMessage.miniMessage().deserialize("<aqua>${sender.name} <gray>un-trusted you from their blueprint <aqua>$name"))
		sender.success("Un-trusted $player from blueprint $name")
	}

	@Suppress("Unused")
	@Subcommand("trust nation")
	@CommandCompletion("@blueprints @nations")
	fun onTrustNation(sender: Player, name: String, nation: String) {
		val target = SLPlayer[sender.uniqueId]?._id ?: return // Silently fail if SLPlayer is somehow null
		val blueprint = getBlueprint(target, name)
		val nationId = resolveNation(nation)
		failIf(blueprint.trustedNations.contains(nationId)) {
			"$nation is already trusted, you might be looking for /blueprint untrust nation $name $nation"
		}
		blueprint.trustedNations.add(nationId)
		saveBlueprint(blueprint)
		sender.success("Trusted nation $nation to blueprint $name")
	}

	@Suppress("Unused")
	@Subcommand("untrust nation")
	@CommandCompletion("@blueprints @nations")
	fun onUntrustNation(sender: Player, name: String, nation: String) {
		val target = SLPlayer[sender.uniqueId]?._id ?: return // Silently fail if SLPlayer is somehow null
		val blueprint = getBlueprint(target, name)
		val nationId = resolveNation(nation)
		failIf(!blueprint.trustedNations.contains(nationId)) {
			"$nation is not trusted, you might be looking for /blueprint trust nation $name $nation"
		}
		blueprint.trustedNations.remove(nationId)
		saveBlueprint(blueprint)
		sender.success("Un-trusted nation $nation from blueprint $name")
	}

	@Subcommand("rename")
	@CommandCompletion("@blueprints newName")
	fun onRename(sender: Player, oldName: String, newName: String) = asyncCommand(sender) {
		validateName(newName)

		val createNew = Blueprint.none(and(Blueprint::owner eq sender.slPlayerId, Blueprint::name eq newName))

		if (!createNew) {
			sender.userError("You already have a blueprint named $newName")
			return@asyncCommand
		}

		val target = SLPlayer[sender.uniqueId]?._id ?: return@asyncCommand // Silently fail if SLPlayer is somehow null
		val blueprint = getBlueprint(target, oldName)
		blueprint.name = newName
		saveBlueprint(blueprint)
		sender.success("Renamed '$oldName' to '$newName'")
	}

	private fun handleMultipleFoundBlueprints(sender: Player, blueprints: Set<Blueprint>, searchedName: String, resultConsumer: (Player, Blueprint) -> Unit) {
		failIf(blueprints.isEmpty()) {
			sender.userError("You have no shared blueprints with the name $searchedName!").toString()
		}

		if (blueprints.size == 1) {
			resultConsumer.invoke(sender, blueprints.first())
			return
		}

		ItemMenu.selector(
			title = GuiText("")
				.addBackground()
				.addBackground(
					GuiText.GuiBackground(
						backgroundChar = BACKGROUND_EXTENDER,
						verticalShift = -11
					)
				)
				.add(text("Multiple shared blueprints found: "), line = -2, verticalShift = -4)
				.build(),
			player = sender,
			entries = blueprints,
			resultConsumer = { _, blueprint -> resultConsumer.invoke(sender, blueprint) },
			itemTransformer = { blueprint ->
				blueprint.type.actualType.menuItem.clone()
					.updateDisplayName(text(blueprint.name))
					.updateLore(blueprintInfo(blueprint).map(String::miniMessage))
			},
			backButtonHandler = { sender.closeInventory() }
		)
	}
}
