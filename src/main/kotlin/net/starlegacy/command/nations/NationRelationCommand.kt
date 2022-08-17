package net.starlegacy.command.nations

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.nations.NationRelation
import net.starlegacy.database.schema.nations.NationRole
import net.starlegacy.util.Notify
import net.starlegacy.util.msg
import org.bukkit.entity.Player
import org.litote.kmongo.eq

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

	private fun setRelationWish(sender: Player, nation: String, wish: NationRelation.Level) = asyncCommand(sender) {
		val senderNation = requireNationIn(sender)
		requireNationPermission(sender, senderNation, NationRole.Permission.MANAGE_RELATIONS)
		val otherNation = resolveNation(nation)

		val otherWish = NationRelation.getRelationWish(otherNation, senderNation)

		val actual = NationRelation.changeRelationWish(senderNation, otherNation, wish)

		Notify.online(
			"&e${sender.name} of ${getNationName(senderNation)} " +
				"has made the relation wish &r${wish.coloredName}&e " +
				"with the nation ${getNationName(otherNation)}. " +
				"Their wish is &r${otherWish.coloredName}&e, " +
				"so their relation is &r${actual.coloredName}&e!"
		)
	}

	@Subcommand("relations")
	fun onRelations(sender: Player) = asyncCommand(sender) {
		val nation = requireNationIn(sender)

		for (relation in NationRelation.find(NationRelation::nation eq nation)) {
			val other = relation.other
			val otherName = getNationName(other)
			val otherWish = NationRelation.getRelationWish(other, nation)
			sender msg "&e$otherName&8: ${relation.actual.coloredName} " +
				"&8(&7Your wish: ${relation.wish.coloredName}&7, their wish: ${otherWish.coloredName}&8)"
		}
	}
}
