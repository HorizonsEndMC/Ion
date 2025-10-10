package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.nations.SiegeCommand
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.sieges.SiegeRewardsGui.SiegeRewardData
import net.horizonsend.ion.server.gui.invui.ListInvUIWindow
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.litote.kmongo.setValue
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

class SiegeRewardsGui(viewer: Player, val rewardSiegeIDs: List<Oid<SolarSiegeData>>) : ListInvUIWindow<SiegeRewardData>(viewer, async = true) {
	override val listingsPerPage: Int = 7

	override fun buildTitle(): Component {
		return GuiText("Siege Rewards")
			.setSlotOverlay(
				"# # # # # # # # #",
				"# . . . . . . . #",
				"# # # # # # # # #",
			)
			.build()
	}

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

	override fun createItem(entry: SiegeRewardData): Item {
		return GuiItem.STAR
			.makeItem(Component.text(entry.name))
			.updateLore(entry.rewards.entries.map { (reward, amount) ->
				val rewardName = fromItemString(reward).displayNameComponent
				ofChildren(rewardName, Component.text(": ", HEColorScheme.HE_DARK_GRAY), Component.text(amount, HEColorScheme.HE_LIGHT_GRAY))
			})
			.makeGuiButton { type, player -> openIndividualSiegeRewards(entry) }
	}

	override fun generateEntries(): List<SiegeRewardData> {
		return rewardSiegeIDs.mapNotNull { entry ->
			val siegerName = runCatching { NationCache[SolarSiegeData.findOnePropById(entry, SolarSiegeData::attacker)!!].name }.getOrNull()
			val name = "${siegerName}'s siege of ${SiegeCommand.getSiegeRegionName(entry)}"

			val rewards = SolarSiegeData.findOnePropById(entry, SolarSiegeData::availableRewards) ?: mutableMapOf()

			if (rewards.isEmpty()) return@mapNotNull null

			SiegeRewardData(entry, name, rewards)
		}
	}

	data class SiegeRewardData(
		val id: Oid<SolarSiegeData>,
		val name: String,
		val rewards: MutableMap<String, Int>
	)

	fun openIndividualSiegeRewards(data: SiegeRewardData) {
		IndividualSiegeGui(viewer, data).openGui(this)
	}

	inner class IndividualSiegeGui(viewer: Player, val data: SiegeRewardData) : ListInvUIWindow<Map.Entry<String, Int>>(viewer) {
		override val listingsPerPage: Int = 18

		override fun buildTitle(): Component {
			return GuiText("${data.name} Rewards")
				.setSlotOverlay(
					"# # # # # # # # #",
					". . . . . . . . .",
					". . . . . . . . .",
					"# # # # # # # # #",
				)
				.build()
		}

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
				.addIngredient('b', GuiItem.CANCEL.makeItem(Component.text("Go Back")).makeGuiButton { type, player -> SiegeRewardsGui(viewer, rewardSiegeIDs).openGui() })
				.setContent(items)
				.build()

			return normalWindow(gui)
		}

		override fun createItem(entry: Map.Entry<String, Int>): Item {
			return fromItemString(entry.key)
				.updateLore(listOf(
					ofChildren(Component.text("Quanity", HEColorScheme.HE_MEDIUM_GRAY), Component.text(": ", HEColorScheme.HE_DARK_GRAY), Component.text(entry.value, HEColorScheme.HE_LIGHT_GRAY)),
					Component.text("Hold Shift to Withdraw All", HEColorScheme.HE_MEDIUM_GRAY)
				))
				.makeGuiButton { type, player ->
					withdrawAndReOpen(entry.key, entry.value, if (type.isShiftClick) entry.value else 64)
				}
		}

		override fun generateEntries(): List<Map.Entry<String, Int>> {
			return data.rewards.entries.toList()
		}

		fun withdrawAndReOpen(item: String, amount: Int, limit: Int) {
			val itemStack = fromItemString(item)

			Tasks.async {
				val amount = data.rewards[item] ?: amount

				val withdrawAmount = minOf(amount, limit)

				val newAmount = amount - withdrawAmount

				data.rewards[item] = newAmount
				if (newAmount <= 0) data.rewards.remove(item)

				SolarSiegeData.updateById(data.id, setValue(SolarSiegeData::availableRewards, data.rewards))

				Tasks.sync {
					Bazaars.giveOrDropItems(itemStack, withdrawAmount, viewer)
					IonServer.slF4JLogger.info("${viewer.name} withdrew $withdrawAmount $item from siege rewards.")

					if (data.rewards.isEmpty()) {
						parentWindow?.openGui()
						return@sync
					}

					openGui()
				}
			}
		}
	}
}
