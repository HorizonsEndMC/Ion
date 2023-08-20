package net.horizonsend.ion.server.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.BukkitCommandCompletionContext
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.HelpCommand
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.NPCTerritoryOwner
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.progression.Levels
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.msg
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Bukkit
import org.bukkit.command.CommandException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.litote.kmongo.and
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("MemberVisibilityCanBePrivate")
abstract class SLCommand : BaseCommand() {
	protected val log: Logger = LoggerFactory.getLogger(javaClass)

	companion object {
		val ASYNC_COMMAND_THREAD: ExecutorService =
			Executors.newSingleThreadExecutor(Tasks.namedThreadFactory("sl-async-commands"))
	}

	open fun onEnable(manager: PaperCommandManager) {}
	open fun onDisable() {}

	protected fun registerStaticCompletion(manager: PaperCommandManager, key: String, value: String) {
		manager.commandCompletions.registerStaticCompletion(key, value)
	}

	protected fun registerAsyncCompletion(
		manager: PaperCommandManager,
		key: String,
		value: (BukkitCommandCompletionContext) -> List<String>
	) {
		manager.commandCompletions.registerAsyncCompletion(key, value)
	}

	/**
	 * Run this block of code async. Also, no two blocks passed to this method will run at the same time,
	 * because it runs them all on a single thread. This prevents exploits from multiple people running
	 * one command at the same time or in the same place or in rapid succession.
	 *
	 * HOWEVER, this does not help when people are on different servers, which should be kept in mind.
	 */
	protected open fun asyncCommand(sender: CommandSender, block: () -> Unit) {
		ASYNC_COMMAND_THREAD.submit {
			try {
				block()
			} catch (e: Exception) {
				if (e is CommandException || e is InvalidCommandArgument) {
					sender.userError("Error: ${e.message}")
					return@submit
				}

				val cause = e.cause
				if (cause is CommandException || cause is InvalidCommandArgument) {
					sender.userError("Error: ${cause.message}")
					return@submit
				}

				val uuid = UUID.randomUUID()
				log.error("Command Error for ${sender.name}, id: $uuid", e)
				sender.serverError("Something went wrong with that command, please tell staff.\nError ID: $uuid")
			}
		}
	}

	//region Utilities
	/**
	 * Returns the UUID linked to the given name from the SLPlayers table if available,
	 * else throws InvalidCommandArgument
	 *
	 * @throws InvalidCommandArgument if no UUID could be found for that name
	 */
	protected fun resolveOfflinePlayer(name: String): UUID = SLPlayer.findIdByName(name)?.uuid
		?: fail { "Player $name not found. Have they joined the server?" }
	//endregion

	@HelpCommand
	fun onHelp(help: CommandHelp) = help.showHelp()

	protected val linesPerPage = 8

	protected fun getPlayerName(id: SLPlayerId): String {
		return Bukkit.getPlayer(id.uuid)?.name ?: SLPlayer.getName(id) ?: error("No such player $id")
	}

	protected fun getSettlementName(id: Oid<Settlement>): String {
		return SettlementCache[id].name
	}

	protected fun getNationName(id: Oid<Nation>): String {
		return NationCache[id].name
	}

	protected fun getNPCOwnerName(id: Oid<NPCTerritoryOwner>): String? {
		return NPCTerritoryOwner.getName(id)
	}

	protected fun getSettlementTag(id: SLPlayerId, name: String, color: SLTextStyle = SLTextStyle.RESET): String {
		val tag = PlayerCache.getColoredTag(SettlementRole.getTag(id))?.plus(" ") ?: ""
		return "$tag$color$name"
	}

	protected fun getNationTag(id: SLPlayerId, name: String, color: SLTextStyle = SLTextStyle.RESET): String {
		val tag = PlayerCache.getColoredTag(NationRole.getTag(id))?.plus(" ") ?: ""
		return "$tag$color$name"
	}

	protected fun getRelation(sender: CommandSender, nation: Oid<Nation>): NationRelation.Level = when (sender) {
		is Player -> PlayerCache[sender].nationOid?.let { RelationCache[it, nation] }
		else -> null
	} ?: NationRelation.Level.NONE

	protected fun sendBreak(sender: CommandSender, color: SLTextStyle) = sender msg lineBreak(color)

	protected fun lineBreak(color: SLTextStyle = SLTextStyle.DARK_GRAY): String =
		"$color============================================="

	protected fun failIf(boolean: Boolean, message: () -> String) {
		if (boolean) {
			fail(message)
		}
	}

	protected fun fail(message: () -> String): Nothing {
		throw InvalidCommandArgument(message.invoke())
	}

	protected fun <T> T.fail(message: (T) -> String) {
		throw InvalidCommandArgument(message.invoke(this))
	}

	protected fun resolveSettlement(name: String): Oid<Settlement> = SettlementCache.getByName(name)
		?: fail { "Settlement $name not found" }

	protected fun resolveNation(name: String): Oid<Nation> = NationCache.getByName(name)
		?: fail { "Nation $name not found" }

	protected fun requireMinLevel(sender: Player, level: Int) =
		failIf(Levels[sender] < level) { "You need to be at least level $level to do that" }

	protected fun requireTerritoryIn(sender: Player): RegionTerritory = Regions.findFirstOf(sender.location)
		?: fail { "You're not in a territory on a planet" }

	protected fun requireTerritoryUnclaimed(territory: RegionTerritory) {
		territory.settlement?.fail { "${territory.name} is claimed by ${getSettlementName(it)}" }

		territory.nation?.fail { "${territory.name} is an outpost of ${getNationName(it)}" }

		territory.npcOwner?.fail { "${territory.name} is the NPC territory ${getNPCOwnerName(it)}" }
	}

	protected fun requireSettlementIn(sender: Player): Oid<Settlement> = PlayerCache[sender].settlementOid
		?: fail { "You need to be in a settlement to do that" }

	protected fun requireNationIn(sender: Player): Oid<Nation> = PlayerCache[sender].nationOid
		?: fail { "You need to be in a nation to do that" }

	protected fun isSettlementLeader(player: Player, settlementId: Oid<Settlement>): Boolean =
		SettlementCache[settlementId].leader == player.slPlayerId

	protected fun isNationLeader(player: Player, nationId: Oid<Nation>): Boolean =
		SettlementCache[NationCache[nationId].capital].leader == player.slPlayerId

	protected fun requireSettlementLeader(sender: Player, settlementId: Oid<Settlement>) =
		failIf(!isSettlementLeader(sender, settlementId)) { "Only the settlement leader can do that" }

	protected fun requireNationLeader(sender: Player, nationId: Oid<Nation>) =
		failIf(!isNationLeader(sender, nationId)) { "Only the nation leader can do that" }

	protected fun requireIsMemberOf(slPlayerId: SLPlayerId, settlementId: Oid<Settlement>, name: String? = null) {
		failIf(
			!SLPlayer.isMemberOfSettlement(
				slPlayerId,
				settlementId
			)
		) { "${name ?: "That player"} is not a member of the settlement" }
	}

	protected fun requireNotSettlementLeader(sender: Player, settlementId: Oid<Settlement>) {
		failIf(isSettlementLeader(sender, settlementId)) {
			"You can't do that while the leader of a settlement! " +
				"Hint: To disband a settlement, use /s disband, " +
				"or to change the leader, use /s set leader"
		}
	}

	protected fun requireNotInSettlement(sender: Player) =
		failIf(PlayerCache[sender].settlementOid != null) { "You can't do that while in a settlement" }

	protected fun requireNotInNation(sender: Player) =
		failIf(PlayerCache[sender].nationOid != null) { "You can't do that while in a nation. Hint: To leave the nation, use /n leave" }

	protected fun requireNotCapital(settlementId: Oid<Settlement>, action: String = "do that") =
		failIf(SettlementCache[settlementId].nation?.let(NationCache::get)?.capital == settlementId) { "The capital settlement can't $action!" }

	protected fun requireMoney(sender: Player, amount: Number, text: String = "do that") {
		failIf(!VAULT_ECO.has(sender, amount.toDouble())) {
			"You don't have enough money to $text! It requires ${amount.toCreditsString()}, " +
				"but you only have ${VAULT_ECO.getBalance(sender).toCreditsString()}"
		}
	}

	protected fun requireSettlementPermission(
		sender: Player,
		settlementId: Oid<Settlement>,
		permission: SettlementRole.Permission
	) {
		if (isSettlementLeader(sender, settlementId)) {
			return // leaders have all perms
		}

		val query = and(
			SettlementRole::parent eq settlementId, // just in case, but should never have a role from another settlement
			SettlementRole::members contains sender.slPlayerId,
			SettlementRole::permissions contains permission
		)

		failIf(SettlementRole.none(query)) { "You need the settlement permission $permission to do that" }
	}

	protected fun requireNationPermission(
		sender: Player,
		nationId: Oid<Nation>,
		permission: NationRole.Permission
	) {
		if (isNationLeader(sender, nationId)) {
			return // leaders have all perms
		}

		val query = and(
			NationRole::parent eq nationId, // just in case, but should never have a role from another nation
			NationRole::members contains sender.slPlayerId,
			NationRole::permissions contains permission
		)

		failIf(NationRole.none(query)) { "You need the nation permission $permission to do that" }
	}

	protected fun getSettlementTerritory(settlementId: Oid<Settlement>): RegionTerritory {
		val territoryId = Settlement.findPropById(settlementId, Settlement::territory)
			?: error("Failed to get territory for settlement $settlementId")
		return Regions[territoryId]
	}

	protected fun getStarshipRiding(sender: Player) = ActiveStarships.findByPassenger(sender)
		?: fail { "You must be riding a starship" }

	protected fun getStarshipPiloting(sender: Player) = ActiveStarships.findByPilot(sender)
		?: fail { "You must be piloting a starship" }

	open fun supportsVanilla(): Boolean = false
}
