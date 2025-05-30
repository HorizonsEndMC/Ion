package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.stripAttributes
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.searchEntires
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bson.conversions.Bson
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.eq
import java.util.function.Consumer

class SearchGui(
	private val player: Player,
	private val resultStringConsumer: Consumer<String>,
	private val contextName: String,
	private val rawItemBson: Bson,
	private val backButtonHandler: () -> Unit
) : CommonGuiWrapper {
	override fun openGui() {
		Tasks.async {
			val entires = BazaarItem.find(rawItemBson).mapTo(mutableSetOf(), BazaarItem::itemString)

			Tasks.sync {
				player.searchEntires(
					entries = entires,
					searchTermProvider = { itemString ->
						val displayName = fromItemString(itemString).displayNameString
						listOf(itemString, displayName)
					},
					prompt = text("Search $contextName listings"),
					description = Component.empty(),
					backButtonHandler = { backButtonHandler.invoke() },
					componentTransformer = { itemString -> fromItemString(itemString).displayName() },
					itemTransformer = { itemString ->
						val sellers = BazaarItem.find(and(rawItemBson, BazaarItem::itemString eq itemString))

						val sellerCount = sellers.count()
						val totalStock = sellers.sumOf { it.stock }
						val minPrice = sellers.minOfOrNull { it.price } ?: 0
						val maxPrice = sellers.maxOfOrNull { it.price } ?: 0

						fromItemString(itemString)
							.stripAttributes()
							.updateLore(listOf(
								template(text("{0} listing${if (sellerCount != 1) "s" else ""} with a total stock of {1}", HE_MEDIUM_GRAY), sellerCount, totalStock),
								ofChildren(text("Min price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), minPrice.toCreditComponent()),
								ofChildren(text("Max price of listing${if (sellerCount != 1) "s" else ""}: ", HE_MEDIUM_GRAY), maxPrice.toCreditComponent()),
							))
					},
					handler = { _, result -> resultStringConsumer.accept(result) }
				)
			}
		}
	}
}
