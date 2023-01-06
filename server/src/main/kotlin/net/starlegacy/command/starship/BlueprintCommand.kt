package net.starlegacy.command.starship

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.world.block.BlockState
import net.horizonsend.ion.server.legacy.ShipFactoryMaterialCosts
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.minecraft.world.level.block.BaseEntityBlock
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.starships.Blueprint
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.progression.Levels
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.PilotedStarships
import net.starlegacy.feature.starship.StarshipComputers
import net.starlegacy.feature.starship.StarshipDetection
import net.starlegacy.feature.starship.StarshipSchematic
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.factory.PrintItem
import net.starlegacy.feature.starship.factory.StarshipFactories
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.isAlphanumeric
import net.starlegacy.util.msg
import net.starlegacy.util.nms
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.toBukkitBlockData
import org.bukkit.Material
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.save
import java.util.LinkedList
import java.util.Locale
import java.util.UUID
import kotlin.collections.set

@CommandAlias("blueprint")
object BlueprintCommand : SLCommand() {

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
		failIf(!name.replace('-', ' ').replace('_', ' ').isAlphanumeric()) {
			"Name must only contain letters, numbers, and - or _"
		}
	}

	@Subcommand("save")
	fun onSave(sender: Player, name: String, @Optional confirm: String?) = asyncCommand(sender) {
		val createNew = Blueprint.none(and(Blueprint::owner eq sender.slPlayerId, Blueprint::name eq name))
		// TODO: confirm accept rules
		val slPlayerId = sender.slPlayerId
		val starship = getStarshipPiloting(sender)
		validateName(name)
		var pilotLoc = Vec3i(sender.location)
		failIf(!starship.isWithinHitbox(pilotLoc.x, pilotLoc.y, pilotLoc.z, 1)) {
			"Must be inside the ship."
		}

		val schem = Tasks.getSyncBlocking { StarshipSchematic.createSchematic(starship) }
		val data = Blueprint.createData(schem)

		pilotLoc = Vec3i(pilotLoc.x - schem.origin.x, pilotLoc.y - schem.origin.y, pilotLoc.z - schem.origin.z)

		if (createNew) {
			failIf(Blueprint.count(Blueprint::owner eq slPlayerId) > getMaxBlueprints(sender)) {
				"You can only have up to ${getMaxBlueprints(sender)} blueprints."
			}
			Blueprint.create(slPlayerId, name, starship.data.starshipType, pilotLoc, starship.initialBlockCount, data)
			sender msg "&aSaved blueprint $name"
		} else {
			val blueprint = getBlueprint(sender, name)
			blueprint.blockData = data
			blueprint.pilotLoc = pilotLoc
			blueprint.type = starship.data.starshipType
			saveBlueprint(blueprint)
			sender msg "&aUpdated blueprint $name"
		}

		failIf(confirm != "confirm") {
			"To save a blueprint, you must acknowledge that you understand that you cannot save blueprints for any purpose of, for example, copying ship designs, or basically saving blueprints of any ships that you didn't design or get permission from the designer to save. This also applies to schematica and similar mods. To acknowledge this, do /blueprint save <name> confirm."
		}
	}

	private fun getBlueprint(sender: Player, name: String): Blueprint {
		return Blueprint.find(and(Blueprint::owner eq sender.slPlayerId, Blueprint::name eq name)).first()
			?: fail { "You don't have a blueprint named $name." }
	}

	private fun saveBlueprint(blueprint: Blueprint) {
		Blueprint.col.save(blueprint)
	}

	@Subcommand("delete")
	@CommandCompletion("@blueprints")
	fun onDelete(sender: Player, name: String) = asyncCommand(sender) {
		val blueprint = getBlueprint(sender, name)
		// TODO: confirm menu
		Blueprint.delete(blueprint._id)
		sender msg "&aDeleted blueprint ${blueprint.name}"
	}

	private fun blueprintInfo(blueprint: Blueprint): List<String> {
		val list = LinkedList<String>()
		var blueprintcost = calculateBlueprintCost(blueprint)
		list.add("&7Size&8: &6${blueprint.size}")
		list.add("&7Cost&8: &6$${blueprintcost.toInt()}")
		list.add("&7Class&8: &d${blueprint.type}")
		if (blueprint.trustedNations.isNotEmpty()) {
			list.add("&7Trusted Players&8: &b${blueprint.trustedPlayers.joinToString { getPlayerName(it) }}}")
			list.add("&7Trusted Nations&8: &a${blueprint.trustedNations.joinToString { NationCache[it].name }}")
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

	@Subcommand("list")
	fun onList(sender: Player) = asyncCommand(sender) {
		val slPlayerId = sender.slPlayerId
		val blueprints: List<Blueprint> = Blueprint
			.find(Blueprint::owner eq slPlayerId)
			.descendingSort(Blueprint::size)
			.toList()
		failIf(blueprints.isEmpty()) {
			"You have no blueprints"
		}
		MenuHelper.apply {
			val items: List<GuiItem> = blueprints.map { blueprint ->
				guiButton(blueprint.type.menuItem) {
					playerClicker.closeInventory()
					Tasks.async { showMaterials(playerClicker, blueprint) }
				}.setName(blueprint.name).setLore(blueprintInfo(blueprint))
			}
			Tasks.sync {
				sender.openPaginatedMenu("Your Blueprints", items)
			}
		}
	}

	@Subcommand("info")
	@CommandCompletion("@blueprints")
	fun onInfo(sender: Player, name: String) = asyncCommand(sender) {
		val blueprint = getBlueprint(sender, name)
		sender msg blueprintInfo(blueprint).joinToString("\n")
	}

	@Subcommand("materials")
	@CommandCompletion("@blueprints")
	fun onMaterials(sender: Player, name: String) = asyncCommand(sender) {
		val blueprint = getBlueprint(sender, name)
		showMaterials(sender, blueprint)
	}

	@Subcommand("load")
	@CommandPermission("starships.blueprint.load")
	@CommandCompletion("@blueprints")
	fun onLoad(sender: Player, name: String) = asyncCommand(sender) {
		val blueprint = getBlueprint(sender, name)
		val schematic: Clipboard = blueprint.loadClipboard()
		val pilotLoc = blueprint.pilotLoc

		Tasks.syncBlocking {
			checkObstruction(sender, schematic, pilotLoc)

			loadSchematic(sender, schematic, pilotLoc) { origin ->
				tryPilot(sender, origin, blueprint.type, blueprint.name)
			}
		}
	}

	@Subcommand("fix")
	@CommandPermission("starships.blueprint.load")
	@CommandCompletion("@blueprints")
	fun onFix(sender: Player, name: String) = asyncCommand(sender) {
		val blueprint = getBlueprint(sender, name)
		val schematic: Clipboard = blueprint.loadClipboard()
		val pilotLoc = blueprint.pilotLoc

		Tasks.syncBlocking {
			checkObstruction(sender, schematic, pilotLoc)

			loadSchematic(sender, schematic, pilotLoc) { origin ->
				tryPilot(sender, origin, blueprint.type, blueprint.name) { starship ->

					starship.iterateBlocks { x, y, z ->
						val block = starship.world.getBlockAt(x, y, z)
						val blockData = block.blockData
						if (blockData.nms.block is BaseEntityBlock) {
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

	fun checkObstruction(sender: Player, schematic: Clipboard, pilotLoc: Vec3i) {
		val world = BukkitAdapter.adapt(sender.world)
		val vec: BlockVector3 = getPasteVector(sender, pilotLoc)
		val region = schematic.region.clone()
		val offset = vec.subtract(schematic.origin)
		for (point in region) {
			failIf(!isAir(schematic.getBlock(point)) && !isAir(world.getBlock(point.add(offset)))) {
				"&cObstructed at $point"
			}
		}
	}

	fun loadSchematic(sender: Player, schematic: Clipboard, pilotLoc: Vec3i, callback: (Vec3i) -> Unit = {}) {
		val vec: BlockVector3 = getPasteVector(sender, pilotLoc)
		val vec3i = Vec3i(vec.blockX, vec.blockY, vec.blockZ)

		placeSchematicEfficiently(schematic, sender.world, vec3i, true) {
			callback(vec3i)
		}
	}

	private fun getPasteVector(sender: Player, pilotLoc: Vec3i): BlockVector3 {
		val playerLocation = sender.location

		return BukkitAdapter.asVector(playerLocation).toBlockPoint()
			.subtract(BlockVector3.at(pilotLoc.x, pilotLoc.y, pilotLoc.z))
	}

	private fun tryPilot(
		sender: Player,
		origin: Vec3i,
		type: StarshipType,
		name: String,
		callback: (ActivePlayerStarship) -> Unit = {}
	) {
		val block = sender.world.getBlockAtKey(origin.toBlockKey())

		if (block.type != StarshipComputers.COMPUTER_TYPE) {
			sender msg "${block.type} at $origin was not a starship computer, failed to pilot"
			return
		}

		DeactivatedPlayerStarships.createAsync(block.world, block.x, block.y, block.z, sender.uniqueId, name) { data ->
			Tasks.async {
				try {
					DeactivatedPlayerStarships.updateType(data, type)
					val state = StarshipDetection.detectNewState(data)
					DeactivatedPlayerStarships.updateState(data, state)
					Tasks.sync {
						PilotedStarships.tryPilot(sender, data) {
							callback(it)
						}
					}
				} catch (e: StarshipDetection.DetectionFailedException) {
					sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Detection failed: ${e.message}")
				}
			}
		}
	}

	private fun isAir(state: BlockState?) = state?.blockType?.material?.isAir != false

	private fun showMaterials(sender: Player, blueprint: Blueprint) {
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

		sender msg StarshipFactories.getPrintItemCountString(map)
	}

	@Subcommand("trust player")
	@CommandCompletion("@blueprints @players")
	fun onTrustPlayer(sender: Player, name: String, player: String) {
		val blueprint = getBlueprint(sender, name)
		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId
		failIf(blueprint.trustedPlayers.contains(slPlayerId)) {
			"$player is already trusted, you might be looking for /blueprint untrust player $name $player"
		}
		blueprint.trustedPlayers.add(slPlayerId)
		saveBlueprint(blueprint)
		Notify.player(playerId, "&b${sender.name} &7trusted you to their blueprint &b$name")
		sender msg "&7Trusted &c$player&7 to blueprint $name"
	}

	@Subcommand("untrust player")
	@CommandCompletion("@blueprints @players")
	fun onUntrustPlayer(sender: Player, name: String, player: String) {
		val blueprint = getBlueprint(sender, name)
		val playerId: UUID = resolveOfflinePlayer(player)
		val slPlayerId = playerId.slPlayerId
		failIf(!blueprint.trustedPlayers.contains(slPlayerId)) {
			"$player is not trusted, you might be looking for /blueprint trust player $name $player"
		}
		blueprint.trustedPlayers.remove(slPlayerId)
		saveBlueprint(blueprint)
		Notify.player(playerId, "&b${sender.name} &7un-trusted you from their blueprint &b$name")
		sender msg "&7Un-trusted &c$player&7 from blueprint $name"
	}

	@Subcommand("trust nation")
	@CommandCompletion("@blueprints @nations")
	fun onTrustNation(sender: Player, name: String, nation: String) {
		val blueprint = getBlueprint(sender, name)
		val nationId = resolveNation(nation)
		failIf(blueprint.trustedNations.contains(nationId)) {
			"$nation is already trusted, you might be looking for /blueprint untrust nation $name $nation"
		}
		blueprint.trustedNations.add(nationId)
		saveBlueprint(blueprint)
		sender msg "&7Trusted nation &c$nation&7 to blueprint $name"
	}

	@Subcommand("untrust nation")
	@CommandCompletion("@blueprints @nations")
	fun onUntrustNation(sender: Player, name: String, nation: String) {
		val blueprint = getBlueprint(sender, name)
		val nationId = resolveNation(nation)
		failIf(!blueprint.trustedNations.contains(nationId)) {
			"$nation is not trusted, you might be looking for /blueprint trust nation $name $nation"
		}
		blueprint.trustedNations.remove(nationId)
		saveBlueprint(blueprint)
		sender msg "&7Un-trusted nation &c$nation&7 from blueprint $name"
	}
}
