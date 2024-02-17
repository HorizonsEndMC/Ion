package net.horizonsend.ion.server.command.nations

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
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

		val message = template(
			text("{0} of {1} has made the relation wish {2} with the nation {3}. Their wish is {4}, so their relation is {5}", YELLOW),
			paramColor = YELLOW,
			useQuotesAroundObjects = false,
			sender.name,
			getNationName(senderNation),
			wish.component,
			getNationName(otherNation),
			otherWish.component,
			actual.component
		)

		Discord.sendEmbed(IonServer.discordSettings.globalChannel, Embed(
			description = message.plainText(),
			color = wish.color.value()
		))

		Notify.notifyOnlineAction(message)
	}

	@Subcommand("relations")
	fun onRelations(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val nation = requireNationIn(sender)

		val relations = NationRelation.find(NationRelation::nation eq nation).toList()

		val body = formatPaginatedMenu(
			entries = relations.count(),
			command = "/nation relations",
			currentPage = page ?: 1,
		) {
			val relation = relations[it]
			val other = relation.other
			val otherName = getNationName(other)
			val otherWish = NationRelation.getRelationWish(other, nation)

			val relationText = bracketed(template(text("Your wish: {0}, their wish: {1}", GRAY), relation.wish.component, otherWish.component))

			ofChildren(text(otherName, YELLOW), text(": ", DARK_GRAY), relation.actual.component, text(" "), relationText)
		}

		sender.sendMessage(ofChildren(
			lineBreakWithCenterText(text("Relations", YELLOW)), newline(),
			body
		))
	}
}
