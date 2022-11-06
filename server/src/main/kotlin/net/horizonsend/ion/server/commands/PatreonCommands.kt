package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import java.awt.Color
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Particle
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.utilities.feedback.FeedbackType
import net.horizonsend.ion.server.utilities.feedback.sendFeedbackMessage
import org.bukkit.entity.Player

@CommandAlias("patreon")
@Suppress("unused")
class PatreonCommands : BaseCommand(){

	@Subcommand("chooseParticle")
	@CommandCompletion("@particles")
	fun onChooseParticle(sender: Player, particle: Particle){
		if (PlayerData[sender.uniqueId].patreonMoney >= particle.patreonMoneyNeeded || sender.hasPermission(("patreon.changeParticle"))){
			PlayerData[sender.uniqueId].update {
				chosenParticle = particle
			}
			sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Chosen Particle now equals ${particle.name}")
		}else sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Error: Need <gray>${particle.patreonMoneyNeeded}$ donared<gray><reset> to use this particle")
	}

	@Subcommand("chooseColour")
	fun onChooseColour(sender: Player, r: Int, g: Int, b: Int){
		if (PlayerData[sender.uniqueId].patreonMoney >= IonServer.Ion.configuration.ParticleColourChoosingMoneyRequirement!! || sender.hasPermission("patreon.chooseParticleColour")){
			PlayerData[sender.uniqueId].update {
				chosenColour = Color(r, g, b)
			}
			sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Chosen Particle Colour change to ${PlayerData[sender.uniqueId].chosenColour}")
		}else sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "Error: Need <gray>${IonServer.Ion.configuration.ParticleColourChoosingMoneyRequirement}$ donated<gray><reset> to change particle colour")
	}

	@Subcommand("clearColour")
	fun onClearColour(sender: Player){
		PlayerData[sender.uniqueId].update {
			chosenColour = null
		}
		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Successfully cleared chosen colour")
	}
}