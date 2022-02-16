package net.starlegacy.feature.economy.cargotrade

import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.pow
import net.starlegacy.SLComponent
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.CargoCrate
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.city.TradeCityType
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.util.Tasks
import net.starlegacy.util.randomDouble
import net.starlegacy.util.randomRange
import net.starlegacy.util.roundToHundredth

object ShipmentGenerator : SLComponent() {
	override fun onEnable() {
		scheduleRegenerateTask()
	}

	private fun scheduleRegenerateTask() {
		val interval = TimeUnit.MINUTES.toMillis(balancing.generator.regenerateIntervalMinutes.toLong()) / 50

		Tasks.asyncRepeat(0, interval) {
			ShipmentManager.regenerateShipmentsAsync()
		}
	}

	/**
	 * Goes through every city and creates a list of shipments for it.
	 * @return a map of city territory IDs to shipments.
	 */
	fun generateShipmentMap(): Map<Oid<Territory>, List<UnclaimedShipment>> {
		return TradeCities.getAll().associate { city ->
			city.territoryId to generateShipmentList(city)
		}
	}

	/**
	 * Generates a specific amount of shipments for the city when called.
	 * The amount is defined by `Trade.settings.shipments.shipmentsPerCity`
	 * The shipments are put into the list of available shipments for the planet
	 */
	private fun generateShipmentList(city: TradeCityData): List<UnclaimedShipment> {
		val territory = Regions.get<RegionTerritory>(city.territoryId)
		val planet: String = territory.world

		// crates with positive value are exportable
		val exportingCrates: List<CargoCrate> = CargoCrates.crates.filter { it.getValue(planet) > 0 }

		if (exportingCrates.isEmpty()) {
			log.warn("Planet $planet has no crates to export!")
			return listOf()
		}

		val importingCities: Map<CargoCrate, List<TradeCityData>> = findImportingCities(exportingCrates)

		if (importingCities.isEmpty()) {
			log.warn("No cities to import from ${city.displayName}!")
			return listOf()
		}

		// make a range from 1 to shipmentsPerCity, for each value in the range generate a shipment
		val amount: Int = balancing.generator.shipmentsPerCity
		return (1..amount).map {
			generateShipment(city, planet, importingCities)
		}
	}

	/**
	 * Map a list of crates to the cities that import them.
	 * The map may exclude crates if they don't have any cities to import them.
	 */
	private fun findImportingCities(exportingCrates: List<CargoCrate>): Map<CargoCrate, List<TradeCityData>> {
		return exportingCrates
			// map to cities that do import it
			.associate { crate ->
				val importingPlanets: Set<String> = crate.values.filterValues { it < 0 }.keys

				val importingCities: List<TradeCityData> = TradeCities.getAll().filter {
					importingPlanets.contains(Regions.get<RegionTerritory>(it.territoryId).world.uppercase(Locale.getDefault()))
				}.toList()

				return@associate crate to importingCities
			}.filterValues { it.isNotEmpty() } // exclude ones with no importing cities
	}

	/**
	 * Picks a crate to export for the given planet and available exporting crates.
	 * Assuming there is at least one exporting crate.
	 * Picks a random destination from the cities that import the crate.
	 * Should also generate the value etc. of the shipment.
	 * Finally it constructs and returns a shipment object.
	 */
	private fun generateShipment(
		city: TradeCityData,
		planet: String,
		exportingCrates: Map<CargoCrate, List<TradeCityData>>
	): UnclaimedShipment {
		val (crate: CargoCrate, importingCities: List<TradeCityData>) = pickCrateAndCities(planet, exportingCrates)

		val destination: TradeCityData = pickDestination(importingCities)
		val destinationPlanet = Regions.get<RegionTerritory>(destination.territoryId).world

		val costPerCrate = calculateCost(crate, planet, destinationPlanet)
		val creditsPerCrate = calculateRevenue(costPerCrate)

		val expiryDays = randomRange(balancing.generator.minExpireDays, balancing.generator.maxExpireDays)

		return UnclaimedShipment(
			crate = crate._id,
			from = city,
			to = destination,
			crateCost = costPerCrate,
			crateRevenue = creditsPerCrate,
			fromPlanet = planet,
			toPlanet = destinationPlanet,
			expiryDays = expiryDays
		)
	}

	/**
	 * Picks a crate from the list of crates that the specified planet exports,
	 * randomly chosen and weighted by the value of the crate at that planet.
	 * Crates that are more valuable at a planet are more likely to be chosen.
	 */
	private fun pickCrateAndCities(
		planet: String,
		exportingCrates: Map<CargoCrate, List<TradeCityData>>
	): Pair<CargoCrate, List<TradeCityData>> {
		val balancedObjects = exportingCrates.entries.associate {
			it.toPair() to abs(it.key.getValue(planet))
		}
		return getRandomWeighted(balancedObjects)
	}

	/**
	 * Picks a random city to use as the destination from the given list of importing cities.
	 * Gives higher weight to settlement cities so most shipments go to them if possible.
	 */
	private fun pickDestination(importingCities: List<TradeCityData>): TradeCityData {
		return getRandomWeighted(importingCities.associate {
			it to when (it.type) {
				TradeCityType.SETTLEMENT -> balancing.generator.settlementCityChance
				TradeCityType.NPC -> balancing.generator.npcCityChance
			}
		})
	}

	/**
	 * Calculates the cost to buy each crate, and the revenue received for each crate
	 *
	 * How it works:
	 * 1. Gets the base price of the crate
	 * 2. Gets the "export value" of the route, meaning the combined absolute values
	 *    of the crate for the exporting and importing planet.
	 *
	 *    Example: Exporting water from Terram (0.3 value) to Harenum (-1) value
	 *             would have an export value of 1.3
	 * 3. Calculates a market random which is between balancing.generator.randomMarketFactor(Min/Max), defaults 1.0 and 1.15
	 * 4. **Cost is crate base price times export value times market random**
	 */
	private fun calculateCost(crate: CargoCrate, originPlanet: String, destinationPlanet: String): Double {
		// base price of the crate, defined separately for each crate
		val crateBasePrice = getPrice(crate)

		val originValue = crate.getValue(originPlanet)
		val destinationValue = crate.getValue(destinationPlanet)

		// route value is the abundance of the crate
		// it is based on the crate's value for the origin and the destination
		// subtract negative value as it is always negative, ends up the sum of their absolute values
		// use configurable exponent in order to make more profitable routes even MORE profitable
		val routeValue = getRouteValue(originValue, destinationValue).pow(balancing.generator.routeValueExponent)

		// random market value to simulate shifting markets
		// generates a random number between the specified min/max
		val marketRandom = randomDouble(balancing.generator.marketFactorMin, balancing.generator.marketFactorMax)

		// the cost per crate of the shipment
		val cost = crateBasePrice * marketRandom * routeValue
		return cost.roundToHundredth()
	}

	/**
	 * **Revenue is based on the cost times a randomized profit percent.**
	 *    So, it calculates a "revenue multiplier" from balancing.generator.profitFactor(Min/Max), defaults 0.9 and 2.0
	 */
	private fun calculateRevenue(cost: Double): Double {
		// used to determine the difference between cost and profit
		// generates a random number between the specified min/max
		// a 2.0 would be a 100% increase, a 0.9 would be a 10% decrease
		val profitMultiplier = randomDouble(balancing.generator.profitFactorMin, balancing.generator.profitFactorMax)
		return cost * profitMultiplier
	}

	fun getRouteValue(originValue: Double, destinationValue: Double): Double {
		return originValue - destinationValue
	}

	private val warnedCrateNames = mutableSetOf<String>()

	private fun getPrice(crate: CargoCrate): Double {
		return balancing.generator.prices[crate.name] ?: run {
			if (warnedCrateNames.add(crate.name)) {
				log.warn("No price defined for ${crate.name}! Using default of ${balancing.generator.defaultPrice}")
			}
			return@run balancing.generator.defaultPrice
		}
	}

	// adapted from https://stackoverflow.com/a/36230607/3530389
	@Throws(IllegalArgumentException::class)
	private fun <E> getRandomWeighted(balancedObjects: Map<E, Double>): E {
		val totalWeight: Double = balancedObjects.values.sumOf { it }

		if (totalWeight <= 0) throw IllegalArgumentException("Total weight must be positive.")

		val value: Double = Math.random() * totalWeight
		var weight = 0.0

		for ((key, valueWeight) in balancedObjects) {
			weight += valueWeight
			if (value < weight) return key
		}

		throw IllegalStateException("No random weighted value could be picked!")
	}
}
