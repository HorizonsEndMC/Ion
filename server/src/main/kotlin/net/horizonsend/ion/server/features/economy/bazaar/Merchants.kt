package net.horizonsend.ion.server.features.economy.bazaar

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.configuration.ConfigurationFiles.sharedDataFolder
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.Properties
import kotlin.math.sqrt

object Merchants : IonServerComponent() {

    override fun onEnable() {
		reloadMerchants()
		Tasks.asyncRepeat(20L * 60 * 60, 20L * 60 * 60) {
			reloadMerchants()
		}
	}

	private val merchantPricesFile = File(sharedDataFolder, "merchant_price_data.properties")
	private lateinit var merchantMap: Map<String, Double>

	private fun loadMerchantData(): Properties {
		val properties = Properties()

		if (merchantPricesFile.exists()) {
			FileReader(merchantPricesFile).use { reader ->
				properties.load(reader)
			}
		}

		return properties
	}

	// sets price per merchant price, double average bazaar price if available in bazaars otherwise default price
	// to prevent exploiting, don't let it be less than 1/2th the default price
	private fun reloadMerchants() {
		val properties = loadMerchantData()

		val newMap = mutableMapOf<String, Double>()
		for (key in properties.stringPropertyNames()) {
			newMap[key.toString()] = properties.getProperty(key).toDouble()
		}
		for (key in newMap.keys) {
			val price = calculatePrice(key, newMap)
			if (price > 0) {
				newMap[key] = price
			}
		}

		// shouldn't be any problem setting this async as it's not a big deal if something doesn't get the
		// new reference right away
		merchantMap = newMap
	}

	private fun calculatePrice(key: String, map: MutableMap<String, Double>): Double {
		var weightedTotalPrice = 0.0
		var totalWeight = 0.0
		for (bazaarItem in BazaarItem.find(BazaarItem::itemString eq key)) {
			val weight = sqrt(bazaarItem.stock.toDouble())
			weightedTotalPrice += bazaarItem.price * weight
			totalWeight += weight
		}
		if (totalWeight == 0.0) {
			return 0.0
		}
		val averagePrice = weightedTotalPrice / totalWeight
		val doublePrice = averagePrice * 2
		val defaultPrice = map.getValue(key)
		return doublePrice.coerceIn(defaultPrice * .5, defaultPrice * 1.5)
	}

	private fun saveMerchantData(properties: Properties) {
		FileWriter(merchantPricesFile).use {
			properties.store(it, "merchant npc prices")
		}
	}

	fun setMerchantDefaultPrice(itemString: String, price: Double) {
		val properties = loadMerchantData()
		properties[itemString] = price.toString()
		saveMerchantData(properties)
		Tasks.async {
			reloadMerchants()
		}
	}

	fun removeMerchantItem(itemString: String) {
		val properties = loadMerchantData()
		properties.remove(itemString)
		saveMerchantData(properties)
		Tasks.async {
			reloadMerchants()
		}
	}

	fun onClickMerchantNPC(player: Player, city: TradeCityData) {
		ItemMenu(
			title = text(city.displayName + " Merchant Items"),
			viewer = player,
			guiItems = merchantMap
				.map { (itemString, price) ->
					fromItemString(itemString)
						.updateLore(listOf(
							template(text("Price: {0} per item", HE_MEDIUM_GRAY), price.toCreditComponent()),
							text("/bazaar merchant buy $itemString <amount>", HE_MEDIUM_GRAY)
						))
						.makeGuiButton { _, _ -> player.closeInventory() }
				},
			backButtonHandler = { player.closeInventory() }
		).openGui()
	}

	fun getPrice(itemString: String): Double? = merchantMap[itemString]

	fun getPriceMap(): Map<String, Double> = merchantMap.toMap()
}
