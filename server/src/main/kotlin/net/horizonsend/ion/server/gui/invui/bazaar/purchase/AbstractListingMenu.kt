package net.horizonsend.ion.server.gui.invui.bazaar.purchase

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.EnumScrollButton
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarSort
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.changeTitle
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.eq
import kotlin.math.ceil

abstract class AbstractListingMenu(viewer: Player, val backButtonHandler: () -> Unit = {}) : InvUIWindowWrapper(viewer, async = true) {
    protected var pageNumber = 0
    protected lateinit var items: List<BazaarItem>

    private var sortingMethod: BazaarSort = BazaarSort.HIGHEST_LISTINGS

    protected fun generateItemListings(): List<AsyncItem> {
        val items = BazaarItem.find(BazaarItem::seller eq viewer.slPlayerId)
        sortingMethod.sort(items)
        this.items = items.toList()

        return this.items.map { item ->
            val city = cityName(Regions[item.cityTerritory])
            val stock = item.stock
            val uncollected = item.balance.toCreditComponent()
            val price = item.price.toCreditComponent()

            AsyncItem(
                resultProvider = {
                    fromItemString(item.itemString)
                        .updateLore(listOf(
                            ofChildren(template(text("City: {0}", HE_MEDIUM_GRAY), useQuotesAroundObjects = false, city)),
                            ofChildren(template(text("Stock: {0}", HE_MEDIUM_GRAY), stock)),
                            ofChildren(template(text("Balance: {0}", HE_MEDIUM_GRAY), uncollected)),
                            ofChildren(template(text("Price: {0}", HE_MEDIUM_GRAY), price))
                        ))
                },
                handleClick = {
                    println("Click: $it")
                }
            )
        }
    }

    protected fun refreshWindowText() {
        currentWindow?.changeTitle(buildGuiText())
    }

    protected abstract fun buildGuiText() : Component

    protected fun addPageNumber(listingsPerPage: Int): Component {
        val maxPageNumber = ceil(items.size.toDouble() / (listingsPerPage.toDouble())).toInt()
        val pageNumberString = "${pageNumber + 1} / $maxPageNumber"

        return GuiText("").add(
            text(pageNumberString),
            line = 10,
            GuiText.TextAlignment.CENTER,
            verticalShift = 4
        ).build()
    }

    protected val backButton = GuiItem.CANCEL.makeItem(text("Go back")).makeGuiButton { _, _ -> backButtonHandler.invoke() }
    protected val infoButton = GuiItem.INFO.makeItem(text("Information")).makeGuiButton { _, _ -> backButtonHandler.invoke() }

    protected fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName
        ?: "<{Unknown}>" // this will be used if the city is disbanded but their items remain there

    protected val sortButton = EnumScrollButton(
        providedItem = { GuiItem.FILTER.makeItem(text("Change Sorting Method")) },
        increment = 1,
        value = {
            sortingMethod
        },
        enum = BazaarSort::class.java,
        nameFormatter = { it.displayName },
        subEntry = arrayOf(BazaarSort.MIN_PRICE, BazaarSort.MAX_PRICE, BazaarSort.HIGHEST_STOCK, BazaarSort.LOWEST_STOCK, BazaarSort.HIGHEST_BALANCE, BazaarSort.LOWEST_BALANCE),
        valueConsumer = {
            sortingMethod = it
            openGui()
        }
    )

    protected val searchButton = GuiItem.MAGNIFYING_GLASS.makeItem().makeGuiButton { _, _ ->  }
}