package net.horizonsend.ion.server.command.nations.stationZones

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.StationZone
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.hint
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSpaceStation
import net.horizonsend.ion.server.features.nations.region.types.RegionStationZone
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("stationplot|stplot")
object StationPlotCommand : SLCommand() {
    override fun onEnable(manager: PaperCommandManager) {
        registerAsyncCompletion(manager, "stationPlots") { c ->
            val player = c.player ?: throw InvalidCommandArgument("Players only")
            val slPlayerId = player.slPlayerId

            Regions.getAllOf<RegionStationZone>()
                .filter{ it.owner == slPlayerId }
                .map { it.name }
        }
    }

    @Subcommand("buy")
    @Description("Buy the zone you're standing in as a plot (with confirmation)")
    fun onBuy(sender: Player, @Optional price: Int?) = asyncCommand(sender) {
        requireEconomyEnabled()

        val station = Regions.findFirstOf<RegionSpaceStation<*, *>>(sender.location) ?: fail { "You are not standing in a station" }
        val zone = StationZoneCommand.getZones(SpaceStationCache[station.name]!!).firstOrNull() { zone ->
            zone.contains(sender.location)
        }
        failIf(zone == null) { "No zone at your current location" }

        val realPrice = zone!!.cachedPrice ?: fail { "Zone ${zone.name} is not for sale" }

        requireMoney(sender, realPrice, "purchase zone ${zone.name}")

        failIf(realPrice != price) {
            "This zone costs ${realPrice.toCreditsString()} to buy, " +
                    "with a rent of ${zone.cachedRent?.toCreditsString() ?: "NONE"}. " +
                    "You need to confirm by specifying the price of the zone. " +
                    "Run the command: /s plot buy $realPrice"
        }

        StationZone.setOwner(zone.id, sender.slPlayerId)

        VAULT_ECO.withdrawPlayer(sender, realPrice.toDouble())
        sender.success("Purchased zone ${zone.name} for ${realPrice.toCreditsString()}")
    }

    @Subcommand("list")
    @Description("List all of your station plots")
    fun onList(sender: Player) = asyncCommand(sender) {
        val zones = Regions.getAllOf<RegionStationZone>().filter { it.owner == sender.slPlayerId }

        failIf(zones.none()) { "You don't have any plots! Go to a station zone and use /stplot buy to get one." }

        val items = zones.map { zone: RegionStationZone ->
            val centerX = (zone.maxPoint.x - zone.minPoint.x) / 2 + zone.minPoint.x
            val centerY = (zone.maxPoint.y - zone.minPoint.y) / 2 + zone.minPoint.y
            val centerZ = (zone.maxPoint.z - zone.minPoint.z) / 2 + zone.minPoint.z

            val coordinatesString = ofChildren(
                Component.text("'${zone.name}' Coordinates", NamedTextColor.GOLD),
                Component.text(": ", NamedTextColor.DARK_GRAY),
                Component.text(zone.world, NamedTextColor.AQUA),
                Component.text("@", NamedTextColor.YELLOW),
                Component.text("[", NamedTextColor.DARK_GRAY),
                Component.text("$centerX", NamedTextColor.RED),
                Component.text(",", NamedTextColor.GRAY),
                Component.text("$centerY", NamedTextColor.GREEN),
                Component.text(",", NamedTextColor.GRAY),
                Component.text("$centerZ", NamedTextColor.BLUE),
                Component.text("]", NamedTextColor.DARK_GRAY),
            )

            val trustedPlayers = zone.trustedPlayers?.joinToString { getPlayerName(it) } ?: "None"
            val trustedSettlements = zone.trustedSettlements?.joinToString { getSettlementName(it) } ?: "None"
            val trustedNations = zone.trustedNations?.joinToString { getNationName(it) } ?: "None"

            ItemStack(Material.KNOWLEDGE_BOOK)
                .updateDisplayName(Component.text(zone.name))
                .updateLore(
                    listOf(
                        ofChildren(
                            Component.text("Station: ", NamedTextColor.GRAY),
                            Component.text(SpaceStationCache[zone.station]?.name ?: "None", NamedTextColor.AQUA)
                        ),
                        ofChildren(
                            Component.text("World: ", NamedTextColor.GRAY),
                            Component.text(zone.world, NamedTextColor.DARK_GREEN)
                        ),
                        ofChildren(
                            Component.text("Coordinates: ", NamedTextColor.GRAY),
                            Component.text("$centerX, $centerY, $centerZ", NamedTextColor.GREEN)
                        ),
                        ofChildren(
                            Component.text("Trusted Players: ", NamedTextColor.GRAY),
                            Component.text(trustedPlayers, NamedTextColor.LIGHT_PURPLE)
                        ),
                        ofChildren(
                            Component.text("Trusted Settlements: ", NamedTextColor.GRAY),
                            Component.text(trustedSettlements, NamedTextColor.DARK_AQUA)
                        ),
                        ofChildren(
                            Component.text("Trusted Nations: ", NamedTextColor.GRAY),
                            Component.text(trustedNations, NamedTextColor.RED)
                        ),
                        ofChildren(
                            Component.text("Min Build Access: ", NamedTextColor.GRAY),
                            Component.text(
                                "${zone.minBuildAccess ?: Settlement.ForeignRelation.STRICT}",
                                NamedTextColor.DARK_PURPLE
                            )
                        ),
                    )
                ).makeGuiButton { _, _ ->
                    sender.sendMessage(coordinatesString)
                    sender.closeInventory()
                }
        }

        ItemMenu(
            title = Component.text("Settlement Zone Plots"),
            viewer = sender,
            guiItems = items,
            backButtonHandler = { sender.closeInventory() }
        ).openGui()
    }

    private fun requireOwnsZone(sender: Player, zone: RegionStationZone) {
        failIf(zone.owner != sender.slPlayerId) { "You aren't the owner of the zone ${zone.name}" }
    }


    @Subcommand("sell")
    @Description("Put a plot of yours up for sale")
    @CommandCompletion("@stationPlots -1|100|1000|10000")
    fun onSell(sender: Player, zone: RegionStationZone, price: Int) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)

        if (price == -1) {
            failIf(zone.cachedPrice == null) { "${zone.name} is already not for sale" }

            StationZone.setPrice(zone.id, null)

            sender.success("Made plot ${zone.name} no longer for sale")
            return@asyncCommand
        }

        failIf(price < 1) { "Price must be at least 1 credit" }

        failIf(price == zone.cachedPrice) { "${zone.name}'s price is already set to ${price.toCreditsString()}" }

        StationZone.setPrice(zone.id, price)

        sender.success("Put your plot ${zone.name} up for sale with price ${price.toCreditsString()}")
        sender.hint("(To make it no longer for sale, use /stplot sell -1)")
    }

    @Subcommand("unclaim")
    @Description("Unclaims the specific plot ")
    @CommandCompletion("@stationPlots")
    fun onUnclaim(sender: Player, zone: RegionStationZone) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)

        StationZone.setOwner(zone.id, null)

        sender.success("Unclaimed zone ${zone.name}")
    }

    @Subcommand("trusted add player")
    @Description("Add a player as a trusted player in your plot")
    @CommandCompletion("@stationPlots @players")
    fun onTrustedAddPlayer(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val player = resolveOfflinePlayer(name).slPlayerId
        failIf(zone.trustedPlayers?.contains(player) == true) { "$name is already added to ${zone.name}" }
        StationZone.addTrustedPlayer(zone.id, player)
        sender.success("Added player $name to plot ${zone.name}")
    }

    @Subcommand("trusted add nation")
    @Description("Add a nation as a trusted nation in your plot")
    @CommandCompletion("@plots @nations")
    fun onTrustedAddNation(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val nation = resolveNation(name)
        failIf(zone.trustedNations?.contains(nation) == true) { "$name is already added to ${zone.name}" }
        StationZone.addTrustedNation(zone.id, nation)
        sender.success("Added nation $name to plot ${zone.name}")
    }

    @Subcommand("trusted add settlement")
    @Description("Add a settlement as a trusted settlement in your plot")
    @CommandCompletion("@plots @settlements")
    fun onTrustedAddSettlement(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val settlement = resolveSettlement(name)
        failIf(zone.trustedSettlements?.contains(settlement) == true) { "$name is already added to ${zone.name}" }
        StationZone.addTrustedSettlement(zone.id, settlement)
        sender.success("Added settlement $name to plot ${zone.name}")
    }

    @Subcommand("trusted remove player")
    @Description("Remove a player as a trusted player in your plot")
    @CommandCompletion("@plots @players")
    fun onTrustedRemovePlayer(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val player = resolveOfflinePlayer(name).slPlayerId
        failIf(zone.trustedPlayers?.contains(player) != true) { "$name is not added to ${zone.name}" }
        StationZone.removeTrustedPlayer(zone.id, player)
        sender.success("Removed player $name from plot ${zone.name}")
    }

    @Subcommand("trusted remove nation")
    @Description("Remove a nation as a trusted nation in your plot")
    @CommandCompletion("@plots @nations")
    fun onTrustedRemoveNation(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val nation = resolveNation(name)
        failIf(zone.trustedNations?.contains(nation) != true) { "$name is not added to ${zone.name}" }
        StationZone.removeTrustedNation(zone.id, nation)
        sender.success("Removed nation $name from plot ${zone.name}")
    }

    @Subcommand("trusted remove settlement")
    @Description("Remove a settlement as a trusted settlement in your plot")
    @CommandCompletion("@plots @settlements")
    fun onTrustedRemoveSettlement(sender: Player, zone: RegionStationZone, name: String) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val settlement = resolveSettlement(name)
        failIf(zone.trustedSettlements?.contains(settlement) != true) { "$name is not added to ${zone.name}" }
        StationZone.removeTrustedSettlement(zone.id, settlement)
        sender.success("Removed settlement $name from plot ${zone.name}")
    }

    @Subcommand("allowFriendlyFire")
    @Description("Allow members of the same nation or allies to damage each other")
    @CommandCompletion("@plots true|false")
    fun onSetFriendlyFire(sender: Player, zone: RegionStationZone, state: Boolean) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        StationZone.setAllowFriendlyFire(zone.id, state)
        sender.success("Changed ${zone.name} to ${if (state) "allow" else "disallow"} friendly fire")
    }

    @Subcommand("interactableBlocks add")
    @Description("Allow a block to be interacted with by any player")
    @CommandCompletion("@plots @anyBlock")
    fun onAddInteractableBlock(sender: Player, zone: RegionStationZone, blockString: String) {
        requireOwnsZone(sender, zone)
        val material = validateBlock(blockString)
        StationZone.addInteractableBlock(zone.id, material.name)
        sender.success("Added $blockString to ${zone.name}'s list of interactable blocks")
    }

    @Subcommand("interactableBlocks remove")
    @Description("Remove a block from the list of interactable blocks")
    @CommandCompletion("@plots @anyBlock")
    fun onRemoveInteractableBlock(sender: Player, zone: RegionStationZone, blockString: String) {
        requireOwnsZone(sender, zone)
        val material = validateBlock(blockString)
        StationZone.removeInteractableBlock(zone.id, material.name)
        sender.success("Removed $blockString from ${zone.name}'s list of interactable blocks")
    }

    @Subcommand("interactableBlocks list")
    @Description("Gets list of interactable blocks")
    @CommandCompletion("@plots")
    fun onListInteractableBlocks(sender: Player, zone: RegionStationZone) {
        requireOwnsZone(sender, zone)
        sender.information(zone.getInteractableBlocks())
    }

    private fun validateBlock(blockString: String): Material {
        try {
            val material = Material.matchMaterial(blockString)
            failIf(material == null || !material.isBlock) { "$blockString is not a block" }
            return material!! // fails if material is null
        } catch (e: Exception) {
            fail { "Invalid block string $blockString! To see a block's string, use /bazaar string" }
        }
    }

    @Subcommand("minbuildaccess")
    @Description("Allow certain types of players to build in the plot, e.g. allies (strict for exceptions only, default)")
    fun onMinBuildAccess(
        sender: Player,
        zone: RegionStationZone,
        level: Settlement.ForeignRelation
    ) = asyncCommand(sender) {
        requireOwnsZone(sender, zone)
        val description = when (level) {
            Settlement.ForeignRelation.NONE -> "ANYONE can build in the plot."
            Settlement.ForeignRelation.ALLY -> "Allies of the plot's settlement's nation can build in the plot."
            Settlement.ForeignRelation.NATION_MEMBER -> "Members of the plot's settlement's nation can build in the plot."
            Settlement.ForeignRelation.SETTLEMENT_MEMBER -> "Members of the plot's settlement can build in the plot."
            Settlement.ForeignRelation.STRICT -> "Only those with explicit access can build in the plot."
        }
        StationZone.setMinBuildAccess(zone.id, level)
        sender.success("Changed updated min build access of ${zone.name}. Description of $level: $description")
    }
}