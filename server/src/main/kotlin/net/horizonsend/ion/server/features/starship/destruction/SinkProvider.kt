package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.BlockWrapper.CustomBlockWrapper
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.BlockWrapper.MaterialWrapper
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.RemovalState.Always
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.RemovalState.IfPlayerSink
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

/**
 * When executed at the time the starship sinks, it will handle the mechanics there of
 **/
abstract class SinkProvider(
	val starship: ActiveStarship
) : BukkitRunnable() {
	private var sinkTime: Long = 0

	/**
	 * Start the sink process
	 **/
	fun execute() {
		sinkTime = System.currentTimeMillis()

		setup()

		runTaskTimerAsynchronously(IonServer, 20L, 20L)
	}

	final override fun run() {
		if (sinkTime + MAX_LIFE <= System.currentTimeMillis()) {
			cancel()
			return
		}

		tick()
	}

	/**
	 * Set up any mechanics at the start of sinking
	 **/
	protected abstract fun setup()

	/**
	 * Tick the sink provider
	 *
	 * Called once per second upon sinking
	 **/
	abstract fun tick()

	companion object {
		/**
		 * Starship sinking can run at most 60 seconds
		 **/
		private const val MAX_LIFE = 60_000L
	}

	enum class SinkProviders {
		AI_LARGE {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return RemoveBlockSink.withChance(starship, mapOf(
					MaterialWrapper(Material.IRON_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.GOLD_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.DIAMOND_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.EMERALD_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.REDSTONE_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.NETHERITE_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.COPPER_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.EXPOSED_COPPER) to Pair(0.0, Always),
					MaterialWrapper(Material.WEATHERED_COPPER) to Pair(0.0, Always),
					MaterialWrapper(Material.OXIDIZED_COPPER) to Pair(0.0, Always),
					MaterialWrapper(Material.WAXED_COPPER_BLOCK) to Pair(0.0, Always),
					MaterialWrapper(Material.WAXED_EXPOSED_COPPER) to Pair(0.0, Always),
					MaterialWrapper(Material.WAXED_WEATHERED_COPPER) to Pair(0.0, Always),
					MaterialWrapper(Material.WAXED_OXIDIZED_COPPER) to Pair(0.0, Always),
					CustomBlockWrapper(CustomBlockKeys.TITANIUM_BLOCK) to Pair(0.0, Always),
					CustomBlockWrapper(CustomBlockKeys.ALUMINUM_BLOCK) to Pair(0.0, Always),
					CustomBlockWrapper(CustomBlockKeys.URANIUM_BLOCK) to Pair(0.0, Always),
					CustomBlockWrapper(CustomBlockKeys.ENRICHED_URANIUM_BLOCK) to Pair(0.5, Always),
					CustomBlockWrapper(CustomBlockKeys.NETHERITE_CASING) to Pair(0.5, Always),
					CustomBlockWrapper(CustomBlockKeys.CHETHERITE_BLOCK) to Pair(0.0, Always),
					CustomBlockWrapper(CustomBlockKeys.STEEL_BLOCK) to Pair(0.5, Always),
					CustomBlockWrapper(CustomBlockKeys.BARGE_REACTOR_CORE) to Pair(1.0, Always),
					CustomBlockWrapper(CustomBlockKeys.CRUISER_REACTOR_CORE) to Pair(1.0, Always),
					CustomBlockWrapper(CustomBlockKeys.BATTLECRUISER_REACTOR_CORE) to Pair(1.0, Always),
				))
			}
		},

		NO_REMOVAL {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return AdvancedSinkProvider(starship)
			}
		},

		PLAYER {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return RemoveBlockSink.withChance(starship, mapOf(
					MaterialWrapper(Material.IRON_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.GOLD_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.DIAMOND_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.EMERALD_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.REDSTONE_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.NETHERITE_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.COPPER_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.EXPOSED_COPPER) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.WEATHERED_COPPER) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.OXIDIZED_COPPER) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.WAXED_COPPER_BLOCK) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.WAXED_EXPOSED_COPPER) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.WAXED_WEATHERED_COPPER) to Pair(0.5, IfPlayerSink),
					MaterialWrapper(Material.WAXED_OXIDIZED_COPPER) to Pair(0.5, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.TITANIUM_BLOCK) to Pair(0.5, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.ALUMINUM_BLOCK) to Pair(0.5, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.URANIUM_BLOCK) to Pair(0.5, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.ENRICHED_URANIUM_BLOCK) to Pair(0.75, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.NETHERITE_CASING) to Pair(0.75, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.CHETHERITE_BLOCK) to Pair(0.5, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.STEEL_BLOCK) to Pair(0.75, IfPlayerSink),
					CustomBlockWrapper(CustomBlockKeys.BARGE_REACTOR_CORE) to Pair(1.0, Always),
					CustomBlockWrapper(CustomBlockKeys.CRUISER_REACTOR_CORE) to Pair(1.0, Always),
					CustomBlockWrapper(CustomBlockKeys.BATTLECRUISER_REACTOR_CORE) to Pair(1.0, Always),
				))
			}
		};

		abstract fun getSinkProvider(starship: ActiveStarship): SinkProvider
	}
}
