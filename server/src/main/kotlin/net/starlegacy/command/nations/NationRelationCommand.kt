package net.starlegacy.command.nations

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType.USER_ERROR
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.NationRole
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.litote.kmongo.eq

@Suppress("Unused")
@CommandAlias("nation|n")
internal object NationRelationCommand : SLCommand() {
	@Subcommand("ally")
	@CommandCompletion("@nations")
	fun onAlly(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.ALLY)

	@Subcommand("friendly")
	@CommandCompletion("@nations")
	fun onFriendly(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.FRIENDLY)

	@Subcommand("neutral")
	@CommandCompletion("@nations")
	fun onNeutral(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.NEUTRAL)

	@Subcommand("unfriendly")
	@CommandCompletion("@nations")
	fun onUnfriendly(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.UNFRIENDLY)

	@Subcommand("enemy")
	@CommandCompletion("@nations")
	fun onEnemy(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.ENEMY)

	@Subcommand("nation")
	@CommandCompletion("@nations")
	fun onNation(sender: Player, nation: String) = setRelationWish(sender, nation, NationRelation.Level.NATION)

	private fun setRelationWish(sender: Player, nation: String, wish: NationRelation.Level) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationPermission(sender, senderNation, NationRole.Permission.MANAGE_RELATIONS)
		val otherNation = resolveNation(nation)

		if (senderNation == otherNation && wish != NationRelation.Level.NATION) {
			sender.sendFeedbackMessage(USER_ERROR, "Error: Cannot {0} your own nation", wish.name)
			return@asyncCommand
		}

		if (wish == NationRelation.Level.NATION && senderNation != otherNation) {
			sender.sendFeedbackMessage(USER_ERROR, "Error: Cannot nation another nation")
			return@asyncCommand
		}

		val otherWish = NationRelation.getRelationWish(otherNation, senderNation)

		val actual = NationRelation.changeRelationWish(senderNation, otherNation, wish)

		Bukkit.getOnlinePlayers().forEach { player ->
			player.sendRichMessage(
				"<yellow>${sender.name} of ${getNationName(senderNation)} " +
						"has made the relation wish <reset>${wish.coloredName}<yellow> " +
						"with the nation ${getNationName(otherNation)}. " +
						"Their wish is <reset>${otherWish.coloredName}<yellow>, " +
						"so their relation is &r${actual.coloredName}<yellow>!"
			)
		}
	}

	@Subcommand("relations")
	fun onRelations(sender: Player) = asyncCommand(sender) {
		val nation = requireNationIn(sender)

		for (relation in NationRelation.find(NationRelation::nation eq nation)) {
			val other = relation.other
			val otherName = getNationName(other)
			val otherWish = NationRelation.getRelationWish(other, nation)
			sender.sendRichMessage(
				"<yellow>$otherName<dark_gray>: ${relation.actual.coloredName} " +
					"<dark_gray>(<gray>Your wish: ${relation.wish.coloredName}<gray>, their wish: ${otherWish.coloredName}<dark_gray>)"
			)
		}
	}
}
