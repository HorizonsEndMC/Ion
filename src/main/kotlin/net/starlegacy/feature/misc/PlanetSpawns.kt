package net.starlegacy.feature.misc

import com.github.stefvanschie.inventoryframework.GuiItem
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import java.io.File
import java.nio.file.Files
import java.util.LinkedList
import java.util.Locale
import net.starlegacy.PLUGIN
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.nations.utils.ACTIVE_AFTER_TIME
import net.starlegacy.feature.space.CachedPlanet
import net.starlegacy.feature.space.Space
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.colorize
import net.starlegacy.util.msg
import org.bukkit.World
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.gte
import org.litote.kmongo.`in`

object PlanetSpawns : SLComponent() {
	fun openMenu(player: Player) {

		Tasks.async {
			val planets = LinkedList(Space.getPlanets())
			planets.removeAll {
				!File(
					PLUGIN.sharedDataFolder,
					"planet_spawn_descriptions/${it.name.lowercase(Locale.getDefault())}"
				).exists()
			}

			// check how many active players live on each planet
			val settlementsMap: Multimap<CachedPlanet, Oid<Settlement>> = HashMultimap.create()

			for (settlementData in SettlementCache.all()) {
				val territoryId: Oid<Territory> = settlementData.territory
				val cachedTerritory: RegionTerritory = Regions[territoryId]
				val planetName: String = cachedTerritory.world
				val planet: CachedPlanet = Space.getPlanet(planetName) ?: continue
				settlementsMap[planet].add(settlementData.id)
			}

			val activityQuery = SLPlayer::lastSeen gte ACTIVE_AFTER_TIME

			val activeSettlerMap: Map<CachedPlanet, Int> = planets.associateWith { planet ->
				SLPlayer.count(and(activityQuery, SLPlayer::settlement `in` settlementsMap[planet])).toInt()
			}

			val totalSettlerMap: Map<CachedPlanet, Int> = planets.associateWith { planet ->
				SLPlayer.count(SLPlayer::settlement `in` settlementsMap[planet]).toInt()
			}

			val orderedPlanets = planets.sortedByDescending { activeSettlerMap[it] ?: 0 }

			val parentFile = File(PLUGIN.sharedDataFolder, "planet_spawn_descriptions")

			if (!parentFile.exists()) {
				parentFile.mkdirs()
			}

			val extraLores: Map<CachedPlanet, List<String>> = orderedPlanets.associateWith { planet ->
				val file =
					File(
						PLUGIN.sharedDataFolder,
						"planet_spawn_descriptions/${planet.name.lowercase(Locale.getDefault())}"
					)

				if (!file.exists()) {
					file.createNewFile()
				}

				return@associateWith Files.readAllLines(file.toPath()).map { it.colorize() }
			}

			Tasks.sync {
				if (!player.isOnline) {
					return@sync
				}

				MenuHelper.apply {
					// one row for every 9 planets plus one if it's not divisible by 9, since it rounds down.
					val rows = (planets.size / 9) + (if (planets.size % 9 == 0) 0 else 1)

					val pane = outlinePane(x = 0, y = 0, length = 9, height = rows)

					for (planet in orderedPlanets) {
						val nameLower: String = planet.name.lowercase(Locale.getDefault())
						val planetIcon = (CustomItems["planet_icon_$nameLower"] ?: CustomItems.DETONATOR).itemStack(1)

						val planetName: String = planet.name

						val lore = mutableListOf(
							"&7Active Population: &a${activeSettlerMap[planet]}",
							"&7Total Population: &e${totalSettlerMap[planet]}"
						)

						lore.addAll(extraLores.getValue(planet))

						val button: GuiItem = guiButton(planetIcon) {
							selectPlanet(playerClicker, planetName)
						}.setName(planet.name).setLore(lore)

						pane.addItem(button)
					}

					gui(rows, "Planets")
						.withPane(pane)
						.show(player)
				}
			}
		}
	}

	private fun selectPlanet(player: Player, planetName: String) {
		val planet: CachedPlanet = Space.getPlanet(planetName) ?: return
		val world: World = planet.planetWorld ?: return player msg "&cPlanet world not loaded!"
		player.teleport(world.spawnLocation)
	}
}
