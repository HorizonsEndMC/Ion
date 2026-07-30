package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveSiegeData
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.nations.SiegeCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionRegionalObjective
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.litote.kmongo.setValue
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class SiegeRewardsGui(
	viewer: Player,
	val solarSiegeIds: List<Oid<SolarSiegeData>>,
	val regionalObjectiveSiegeIds: List<Oid<RegionalObjectiveSiegeData>>
) : ListInvUIWindow<SiegeRewardsGui.RewardEntry>(viewer, async = true) {

	override val listingsPerPage: Int = 7

	sealed class RewardEntry {
		abstract val name: String
		abstract val rewards: MutableMap<String, Int>

		data class SolarRewardEntry(
			val id: Oid<SolarSiegeData>,
			override val name: String,
			override val rewards: MutableMap<String, Int>
		) : RewardEntry()

		data class RegionalObjectiveRewardEntry(
			val id: Oid<RegionalObjectiveSiegeData>,
			override val name: String,
			override val rewards: MutableMap<String, Int>
		) : RewardEntry()
	}

	override fun buildTitle(): Component = GuiText("Siege Rewards")
		.setSlotOverlay(
			"# # # # # # # # #",
			"# . . . . . . . #",
			"# # # # # # # # #",
		)
		.build()

	override fun buildWindow(): Window? {
		val gui = PagedGui.items()
			.setStructure(
				". . . . . . . . .",
				". # # # # # # # .",
				". l . . . . . r ."
			)
			.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
			.addIngredient('r', GuiItems.PageRightItem())
			.addIngredient('l', GuiItems.PageLeftItem())
			.setContent(items)
			.build()

		return normalWindow(gui)
	}

	override fun createItem(entry: RewardEntry): Item {
		return GuiItem.STAR
			.makeItem(text(entry.name))
			.updateLore(entry.rewards.entries.map { (reward, amount) ->
				val rewardName = fromItemString(reward).displayNameComponent
				ofChildren(rewardName, text(": ", HEColorScheme.HE_DARK_GRAY), text(amount, HEColorScheme.HE_LIGHT_GRAY))
			})
			.makeGuiButton { _, _ -> IndividualRewardsGui(viewer, entry).openGui(this) }
	}

	override fun generateEntries(): List<RewardEntry> {
		val viewerNation = PlayerCache[viewer].nationOid ?: return listOf()
		val entries = mutableListOf<RewardEntry>()

		// Solar siege rewards
		for (id in solarSiegeIds) {
			if (!SolarSieges.isWinner(id, viewerNation)) continue
			val siegerName = runCatching { NationCache[SolarSiegeData.findOnePropById(id, SolarSiegeData::attacker)!!].name }.getOrNull()
			val name = "${siegerName}'s siege of ${SiegeCommand.getSiegeRegionName(id)}"
			val rewards = SolarSiegeData.findOnePropById(id, SolarSiegeData::availableRewards) ?: mutableMapOf()
			if (rewards.isEmpty()) continue
			entries.add(RewardEntry.SolarRewardEntry(id, name, rewards))
		}

		for (id in regionalObjectiveSiegeIds) {
			if (RegionalObjectiveSiegeData.findOnePropById(id, RegionalObjectiveSiegeData::winner) != viewerNation) continue
			val objectiveId = RegionalObjectiveSiegeData.findOnePropById(id, RegionalObjectiveSiegeData::objective) ?: continue
			val objectiveName = runCatching {
				Regions.getAllOf<RegionRegionalObjective>().firstOrNull { it.id == objectiveId }?.name
			}.getOrNull() ?: "Unknown Objective"
			val rewards = RegionalObjectiveSiegeData.findOnePropById(id, RegionalObjectiveSiegeData::availableRewards) ?: mutableMapOf()
			if (rewards.isEmpty()) continue
			val isPassive = RegionalObjectiveSiegeData.findOnePropById(id, RegionalObjectiveSiegeData::passive) ?: false
			val label = if (isPassive) "Passive Income - $objectiveName" else "Capture of $objectiveName"
			entries.add(RewardEntry.RegionalObjectiveRewardEntry(id, label, rewards))
		}

		return entries
	}

	inner class IndividualRewardsGui(
		viewer: Player,
		val entry: RewardEntry
	) : ListInvUIWindow<Map.Entry<String, Int>>(viewer) {

		override val listingsPerPage: Int = 18

		override fun buildTitle(): Component = GuiText("${entry.name} Rewards")
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)
			.build()

		override fun buildWindow(): Window? {
			val gui = PagedGui.items()
				.setStructure(
					"b . . . . . . . .",
					"# # # # # # # # #",
					"# # # # # # # # #",
					". l . . . . . r ."
				)
				.addIngredient('#', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
				.addIngredient('r', GuiItems.PageRightItem())
				.addIngredient('l', GuiItems.PageLeftItem())
				.addIngredient('b', GuiItem.CANCEL.makeItem(text("Go Back"))
					.makeGuiButton { _, _ -> SiegeRewardsGui(viewer, solarSiegeIds, regionalObjectiveSiegeIds).openGui() })
				.setContent(items)
				.build()

			return normalWindow(gui)
		}

		override fun createItem(item: Map.Entry<String, Int>): Item {
			return fromItemString(item.key)
				.updateLore(listOf(
					ofChildren(text("Quantity", HEColorScheme.HE_MEDIUM_GRAY), text(": ", HEColorScheme.HE_DARK_GRAY), text(item.value, HEColorScheme.HE_LIGHT_GRAY)),
					text("Hold Shift to Withdraw All", HEColorScheme.HE_MEDIUM_GRAY)
				))
				.makeGuiButton { type, _ ->
					withdrawAndReopen(item.key, item.value, if (type.isShiftClick) item.value else 64)
				}
		}

		override fun generateEntries(): List<Map.Entry<String, Int>> = entry.rewards.entries.toList()

		fun withdrawAndReopen(item: String, amount: Int, limit: Int) {
			val itemStack = fromItemString(item)

			Tasks.async {
				val current = entry.rewards[item] ?: amount
				val withdrawAmount = minOf(current, limit)
				val newAmount = current - withdrawAmount

				entry.rewards[item] = newAmount
				if (newAmount <= 0) entry.rewards.remove(item)

				// Update the correct DB collection based on entry type
				when (entry) {
					is RewardEntry.SolarRewardEntry ->
						SolarSiegeData.updateById(entry.id, setValue(SolarSiegeData::availableRewards, entry.rewards))
					is RewardEntry.RegionalObjectiveRewardEntry ->
						RegionalObjectiveSiegeData.updateById(entry.id, setValue(RegionalObjectiveSiegeData::availableRewards, entry.rewards))
				}

				Tasks.sync {
					Bazaars.giveOrDropItems(itemStack, withdrawAmount, viewer)
					IonServer.slF4JLogger.info("${viewer.name} withdrew $withdrawAmount $item from siege rewards.")

					if (entry.rewards.isEmpty()) {
						parentWindow?.openGui()
						return@sync
					}

					openGui()
				}
			}
		}
	}
}
