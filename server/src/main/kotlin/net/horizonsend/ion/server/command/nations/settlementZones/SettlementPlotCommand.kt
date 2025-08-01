package net.horizonsend.ion.server.command.nations.settlementZones

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementZone
import net.horizonsend.ion.common.database.slPlayerId
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.deserializeComponent
import net.horizonsend.ion.common.utils.text.legacyAmpersand
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.colorize
import net.horizonsend.ion.server.miscellaneous.utils.msg
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@CommandAlias("settlementplot|splot")
internal object SettlementPlotCommand : net.horizonsend.ion.server.command.SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "plots") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId

			Regions.getAllOf<RegionSettlementZone>()
				.filter { it.owner == slPlayerId }
				.map { it.name }
		}
	}

	@Subcommand("buy")
	@Description("Buy the zone you're standing in as a plot (with confirmation)")
    fun onBuy(sender: Player, @Optional price: Int?) = asyncCommand(sender) {
		requireEconomyEnabled()

		val zone = Regions.findFirstOf<RegionSettlementZone>(sender.location)
			?: fail { "You're not standing in a settlement zone." }

		val realPrice = zone.cachedPrice ?: fail { "Zone ${zone.name} is not for sale" }

		requireMoney(sender, realPrice, "purchase zone ${zone.name}")

		failIf(realPrice != price) {
			"This zone costs ${realPrice.toCreditsString()} to buy, " +
				"with a rent of ${zone.cachedRent?.toCreditsString() ?: "NONE"}. " +
				"You need to confirm by specifying the price of the zone. " +
				"Run the command: /s plot buy $realPrice"
		}

		SettlementZone.setOwner(zone.id, sender.slPlayerId)

		VAULT_ECO.withdrawPlayer(sender, realPrice.toDouble())
		sender msg "&aPurchased zone ${zone.name} for ${realPrice.toCreditsString()}"
	}

	@Subcommand("list")
	@Description("List all of your plots")
    fun onList(sender: Player) = asyncCommand(sender) {
		val zones = Regions.getAllOf<RegionSettlementZone>()

		failIf(zones.none()) { "You don't have any plots! Go to a settlement zone and use /s plot buy to get one." }

		val items = zones.filter { it.owner == sender.slPlayerId }.map { zone: RegionSettlementZone ->
			val centerX = (zone.maxPoint.x - zone.minPoint.x) / 2 + zone.minPoint.x
			val centerY = (zone.maxPoint.y - zone.minPoint.y) / 2 + zone.minPoint.y
			val centerZ = (zone.maxPoint.z - zone.minPoint.z) / 2 + zone.minPoint.z

			val coordinatesString = (
				"&6'${zone.name}' Coordinates&8: " +
					"&b${zone.world}&e@&8[&c$centerX&7,&a$centerY&7,&9$centerZ&8]"
				)
				.colorize().intern()

			val trustedPlayers = zone.trustedPlayers?.joinToString { getPlayerName(it) }
				?: "None"
			val trustedNations = zone.trustedNations?.joinToString { getNationName(it) }
				?: "None"
			val trustedSettlements = zone.trustedSettlements?.joinToString { getSettlementName(it) }
				?: "None"

			ItemStack(Material.KNOWLEDGE_BOOK)
				.updateDisplayName(Component.text(zone.name))
				.updateLore(listOf(
					deserializeComponent("&7Settlement&8:&b ${getSettlementName(zone.settlement)}", legacyAmpersand),
					deserializeComponent("&7World&8:&2 ${zone.world}", legacyAmpersand),
					deserializeComponent("&7Coordinates&8:&a $centerX, $centerY, $centerZ", legacyAmpersand),
					deserializeComponent("&7Trusted Players&8:&d $trustedPlayers", legacyAmpersand),
					deserializeComponent("&7Trusted Nations&8:&c $trustedNations", legacyAmpersand),
					deserializeComponent("&7Trusted Settlements&8:&3 $trustedSettlements", legacyAmpersand),
					deserializeComponent("&7Min Build Access&8:&5 ${zone.minBuildAccess ?: Settlement.ForeignRelation.STRICT}", legacyAmpersand)
				)).makeGuiButton { _, _ ->
					sender.sendMessage(deserializeComponent(coordinatesString, legacyAmpersand))
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

	private fun requireOwnsZone(sender: Player, zone: RegionSettlementZone) {
		failIf(zone.owner != sender.slPlayerId) { "You aren't the owner of the zone ${zone.name}" }
	}

	@Subcommand("sell")
	@Description("Put a plot of yours up for sale")
	@CommandCompletion("@plots -1|100|1000|10000")
    fun onSell(sender: Player, zone: RegionSettlementZone, price: Int) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)

		if (price == -1) {
			failIf(zone.cachedPrice == null) { "${zone.name} is already not for sale" }

			SettlementZone.setPrice(zone.id, null)

			sender msg "&aMade plot ${zone.name} no longer for sale"
			return@asyncCommand
		}

		failIf(price < 1) { "Price must be at least 1 credit" }

		failIf(price == zone.cachedPrice) { "${zone.name}'s price is already set to ${price.toCreditsString()}" }

		SettlementZone.setPrice(zone.id, price)

		sender msg "&aPut your plot ${zone.name} up for sale with price ${price.toCreditsString()}."
		sender msg "&7&o(To make it no longer for sale, use /s plot sell -1)"
	}

	@Subcommand("unclaim")
	@Description("Unclaimed the specific plot")
	@CommandCompletion("@plots")
    fun onUnclaim(sender: Player, zone: RegionSettlementZone) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)

		SettlementZone.setOwner(zone.id, null)

		sender msg "&aUnclaimed zone ${zone.name}"
	}

	@Subcommand("trusted add player")
	@Description("Add a player as a trusted player in your plot")
	@CommandCompletion("@plots @players")
    fun onTrustedAddPlayer(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val player = resolveOfflinePlayer(name).slPlayerId
		failIf(zone.trustedPlayers?.contains(player) == true) { "$name is already added to ${zone.name}" }
		SettlementZone.addTrustedPlayer(zone.id, player)
		sender msg "&aAdded player $name to plot ${zone.name}"
	}

	@Subcommand("trusted add nation")
	@Description("Add a nation as a trusted nation in your plot")
	@CommandCompletion("@plots @nations")
    fun onTrustedAddNation(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val nation = resolveNation(name)
		failIf(zone.trustedNations?.contains(nation) == true) { "$name is already added to ${zone.name}" }
		SettlementZone.addTrustedNation(zone.id, nation)
		sender msg "&aAdded nation $name to plot ${zone.name}"
	}

	@Subcommand("trusted add settlement")
	@Description("Add a settlement as a trusted settlement in your plot")
	@CommandCompletion("@plots @settlements")
    fun onTrustedAddSettlement(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val settlement = resolveSettlement(name)
		failIf(zone.trustedSettlements?.contains(settlement) == true) { "$name is already added to ${zone.name}" }
		SettlementZone.addTrustedSettlement(zone.id, settlement)
		sender msg "&aAdded settlement $name to plot ${zone.name}"
	}

	@Subcommand("trusted remove player")
	@Description("Remove a player as a trusted player in your plot")
	@CommandCompletion("@plots @players")
    fun onTrustedRemovePlayer(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val player = resolveOfflinePlayer(name).slPlayerId
		failIf(zone.trustedPlayers?.contains(player) != true) { "$name is not added to ${zone.name}" }
		SettlementZone.removeTrustedPlayer(zone.id, player)
		sender msg "&aRemoved player $name from plot ${zone.name}"
	}

	@Subcommand("trusted remove nation")
	@Description("Remove a nation as a trusted nation in your plot")
	@CommandCompletion("@plots @nations")
    fun onTrustedRemoveNation(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val nation = resolveNation(name)
		failIf(zone.trustedNations?.contains(nation) != true) { "$name is not added to ${zone.name}" }
		SettlementZone.removeTrustedNation(zone.id, nation)
		sender msg "&aRemoved nation $name from plot ${zone.name}"
	}

	@Subcommand("trusted remove settlement")
	@Description("Remove a settlement as a trusted settlement in your plot")
	@CommandCompletion("@plots @settlements")
    fun onTrustedRemoveSettlement(sender: Player, zone: RegionSettlementZone, name: String) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		val settlement = resolveSettlement(name)
		failIf(zone.trustedSettlements?.contains(settlement) != true) { "$name is not added to ${zone.name}" }
		SettlementZone.removeTrustedSettlement(zone.id, settlement)
		sender msg "&aRemoved settlement $name from plot ${zone.name}"
	}

	@Subcommand("allowFriendlyFire")
	@Description("Allow members of the same nation or allies to damage each other")
	@CommandCompletion("@plots true|false")
    fun onSetFriendlyFire(sender: Player, zone: RegionSettlementZone, state: Boolean) = asyncCommand(sender) {
		requireOwnsZone(sender, zone)
		SettlementZone.setAllowFriendlyFire(zone.id, state)
		sender msg "&aChanged ${zone.name} to ${if (state) "allow" else "disallow"} friendly fire"
	}

	@Subcommand("interactableBlocks add")
	@Description("Allow a block to be interacted with by any player")
	@CommandCompletion("@plots @anyBlock")
    fun onAddInteractableBlock(sender: Player, zone: RegionSettlementZone, blockString: String) {
		requireOwnsZone(sender, zone)
		val material = validateBlock(blockString)
		SettlementZone.addInteractableBlock(zone.id, material.name)
		sender msg "&aAdded $blockString to ${zone.name}'s list of interactable blocks"
	}

	@Subcommand("interactableBlocks remove")
	@Description("Remove a block from the list of interactable blocks")
	@CommandCompletion("@plots @anyBlock")
    fun onRemoveInteractableBlock(sender: Player, zone: RegionSettlementZone, blockString: String) {
		requireOwnsZone(sender, zone)
		val material = validateBlock(blockString)
		SettlementZone.removeInteractableBlock(zone.id, material.name)
		sender msg "&aRemoved $blockString from ${zone.name}'s list of interactable blocks"
	}

	@Subcommand("interactableBlocks list")
	@Description("Gets list of interactable blocks")
	@CommandCompletion("@plots")
    fun onListInteractableBlocks(sender: Player, zone: RegionSettlementZone) {
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
		zone: RegionSettlementZone,
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
		SettlementZone.setMinBuildAccess(zone.id, level)
		sender msg "&aChanged updated min build access of ${zone.name}. Description of $level:&2&o $description"
	}
}
