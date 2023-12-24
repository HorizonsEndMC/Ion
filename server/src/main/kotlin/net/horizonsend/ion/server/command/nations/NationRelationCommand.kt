package net.horizonsend.ion.server.command.nations

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.misc.messaging.ServerDiscordMessaging
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
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
			sender.userError("Error: Cannot ${wish.name} your own nation")
			return@asyncCommand
		}

		if (wish == NationRelation.Level.NATION && senderNation != otherNation) {
			sender.userError("Error: Cannot nation another nation")
			return@asyncCommand
		}

		val otherWish = NationRelation.getRelationWish(otherNation, senderNation)

		val actual = NationRelation.changeRelationWish(senderNation, otherNation, wish)

		Bukkit.getOnlinePlayers().forEach { player ->
			val message = template(
				text("{0} of {1} has made the relation wish {2} with the nation {3}. Their wish is {4}, so their relation is {5}", YELLOW),
				paramColor = YELLOW,
				useQuotesAroundObjects = false,
				sender.name,
				wish.component,
				getNationName(otherNation),
				otherWish.component,
				actual.component
			)

			player.sendMessage(message)

			ServerDiscordMessaging.globalEmbed(Embed(
				description = message.plainText(),
				color = wish.color.value()
			))
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
				"<yellow>$otherName<dark_gray>: ${relation.actual.miniMessage} " +
					"<dark_gray>(<gray>Your wish: ${relation.wish.miniMessage}<gray>, their wish: ${otherWish.miniMessage}<dark_gray>)"
			)
		}
	}
}
