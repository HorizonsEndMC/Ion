package net.horizonsend.ion.server.command.nations

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
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
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.event.ClickEvent
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

		Discord.sendEmbed(
			ConfigurationFiles.discordSettings().globalChannel, Embed(
			description = message.plainText(),
			color = wish.color.value()
		))

		Notify.notifyOnlineAction(message)
	}

	@Subcommand("relations")
	fun onRelations(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val nation = requireNationIn(sender)

		handleRelations(sender, nation, page)
	}

	@Subcommand("relations")
	@CommandCompletion("@nations")
	fun onRelations(sender: Player, nationName: String, @Optional page: Int?) = asyncCommand(sender) {
		if (nationName.length < 3 && nationName.all { it.isDigit() }) {
			val nation = requireNationIn(sender)

			handleRelations(sender, nation, nationName.toInt())
			return@asyncCommand
		}

		val nation: Oid<Nation> = resolveNation(nationName)

		handleRelations(sender, nation, page)
	}

	private fun handleRelations(sender: Audience, nation: Oid<Nation>, page: Int?) {
		val relations = NationRelation.find(NationRelation::nation eq nation).toList()
		val name = getNationName(nation)

		sender.sendMessage(ofChildren(lineBreakWithCenterText(text("$name's Relations", YELLOW)), newline()))

		val body = formatPaginatedMenu(
			entries = relations.count(),
			command = "/nation relations $name",
			currentPage = page ?: 1,
		) {
			val relation = relations[it]
			val other = relation.other

			val otherName = getNationName(other)
			val otherNameFormatted = text((otherName), YELLOW)
				.hoverEvent(text("/nation info $otherName"))
				.clickEvent(ClickEvent.runCommand("/nation info $otherName"))

			val otherWish = NationRelation.getRelationWish(other, nation)

			val relationText = bracketed(template(text("Your wish: {0}, their wish: {1}", GRAY), relation.wish.component, otherWish.component))

			ofChildren(otherNameFormatted, text(": ", DARK_GRAY), relation.actual.component, text(" "), relationText)
		}

		sender.sendMessage(body)
	}
}
