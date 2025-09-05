package net.horizonsend.ion.server.command.admin

import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.sequences.Sequence
import net.horizonsend.ion.server.features.sequences.SequenceKeys
import net.horizonsend.ion.server.features.sequences.SequenceManager
import net.horizonsend.ion.server.features.sequences.phases.SequencePhase
import net.horizonsend.ion.server.features.sequences.phases.SequencePhaseKeys
import org.bukkit.entity.Player

@CommandAlias("sequenceadmin")
object SequenceAdminCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		manager.commandCompletions.registerCompletion("sequencePhases") { SequencePhaseKeys.allStrings() }
		manager.commandContexts.registerContext(SequencePhase::class.java) { it.popFirstArg().let {  arg -> SequencePhaseKeys[arg]?.getValue() ?: fail { "Unknown key $arg" } } }
		manager.commandCompletions.setDefaultCompletion("sequencePhases", SequencePhase::class.java)

		manager.commandCompletions.registerCompletion("sequences") { SequenceKeys.allStrings() }
		manager.commandContexts.registerContext(Sequence::class.java) { it.popFirstArg().let { arg -> SequenceKeys[arg]?.getValue() ?: fail { "Unknown key $arg" } } }
		manager.commandCompletions.setDefaultCompletion("sequences", Sequence::class.java)
	}

	@Subcommand("sequence start")
	fun start(sender: Player, sequence: Sequence) {
		sender.information("Starting ${sequence.key}")
		SequenceManager.startPhase(sender, sequence.key, sequence.firstPhase)
	}

	@Subcommand("sequence end")
	fun end(sender: Player, sequence: Sequence) {
		sender.information("Ending ${sequence.key}")
		SequenceManager.endSequence(sender, sequence)
		SequenceManager.clearSequenceData(sender)
	}

	@Subcommand("phase start")
	fun start(sender: Player, phase: SequencePhase) {
		sender.information("Starting ${phase.phaseKey.key}")
		SequenceManager.startPhase(sender, phase.sequenceKey, phase.phaseKey)
	}

	@Subcommand("phase info")
	fun getSequencePhases(sender: Player, sequence: Sequence) {
		val phase = SequenceManager.getCurrentPhase(sender, sequence.key) ?: fail { "You don't have ${sequence.key} active!" }
		sender.information("Current phase: ${phase.key}")
	}

	@Subcommand("sequence info")
	fun getSequenceInfo(sender: Player, sequence: Sequence) {
		val data = SequenceManager.getSequenceData(sender, sequence.key)
		sender.information(data.keyedData.entries.joinToString(separator = "\n") { "${it.key} = ${it.value}" })
	}
}
