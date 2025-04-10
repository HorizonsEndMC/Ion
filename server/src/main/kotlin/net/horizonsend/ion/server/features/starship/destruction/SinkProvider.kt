package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.BlockWrapper.CustomBlockWrapper
import net.horizonsend.ion.server.features.starship.destruction.RemoveBlockSink.Companion.BlockWrapper.MaterialWrapper
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
					MaterialWrapper(Material.IRON_BLOCK) to 0.25,
					MaterialWrapper(Material.GOLD_BLOCK) to 0.25,
					MaterialWrapper(Material.DIAMOND_BLOCK) to 0.25,
					MaterialWrapper(Material.EMERALD_BLOCK) to 0.25,
					MaterialWrapper(Material.REDSTONE_BLOCK) to 0.25,
					MaterialWrapper(Material.NETHERITE_BLOCK) to 0.25,
					MaterialWrapper(Material.COPPER_BLOCK) to 0.25,
					MaterialWrapper(Material.EXPOSED_COPPER) to 0.25,
					MaterialWrapper(Material.WEATHERED_COPPER) to 0.25,
					MaterialWrapper(Material.OXIDIZED_COPPER) to 0.25,
					MaterialWrapper(Material.WAXED_COPPER_BLOCK) to 0.25,
					MaterialWrapper(Material.WAXED_EXPOSED_COPPER) to 0.25,
					MaterialWrapper(Material.WAXED_WEATHERED_COPPER) to 0.25,
					MaterialWrapper(Material.WAXED_OXIDIZED_COPPER) to 0.25,
					CustomBlockWrapper(CustomBlocks.TITANIUM_BLOCK) to 0.25,
					CustomBlockWrapper(CustomBlocks.ALUMINUM_BLOCK) to 0.25,
					CustomBlockWrapper(CustomBlocks.URANIUM_BLOCK) to 0.25,
					CustomBlockWrapper(CustomBlocks.ENRICHED_URANIUM_BLOCK) to 0.25,
					CustomBlockWrapper(CustomBlocks.BARGE_REACTOR_CORE) to 1.0,
					CustomBlockWrapper(CustomBlocks.CRUISER_REACTOR_CORE) to 1.0,
					CustomBlockWrapper(CustomBlocks.BATTLECRUISER_REACTOR_CORE) to 1.0,
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
					MaterialWrapper(Material.IRON_BLOCK) to 1.0,
					MaterialWrapper(Material.GOLD_BLOCK) to 1.0,
					MaterialWrapper(Material.DIAMOND_BLOCK) to 1.0,
					MaterialWrapper(Material.EMERALD_BLOCK) to 1.0,
					MaterialWrapper(Material.REDSTONE_BLOCK) to 1.0,
					MaterialWrapper(Material.NETHERITE_BLOCK) to 1.0,
					MaterialWrapper(Material.COPPER_BLOCK) to 1.0,
					MaterialWrapper(Material.EXPOSED_COPPER) to 1.0,
					MaterialWrapper(Material.WEATHERED_COPPER) to 1.0,
					MaterialWrapper(Material.OXIDIZED_COPPER) to 1.0,
					MaterialWrapper(Material.WAXED_COPPER_BLOCK) to 1.0,
					MaterialWrapper(Material.WAXED_EXPOSED_COPPER) to 1.0,
					MaterialWrapper(Material.WAXED_WEATHERED_COPPER) to 1.0,
					MaterialWrapper(Material.WAXED_OXIDIZED_COPPER) to 1.0,
					CustomBlockWrapper(CustomBlocks.TITANIUM_BLOCK) to 1.0,
					CustomBlockWrapper(CustomBlocks.ALUMINUM_BLOCK) to 1.0,
					CustomBlockWrapper(CustomBlocks.URANIUM_BLOCK) to 1.0,
					CustomBlockWrapper(CustomBlocks.ENRICHED_URANIUM_BLOCK) to 1.0,
					CustomBlockWrapper(CustomBlocks.BARGE_REACTOR_CORE) to 1.0,
					CustomBlockWrapper(CustomBlocks.CRUISER_REACTOR_CORE) to 1.0,
					CustomBlockWrapper(CustomBlocks.BATTLECRUISER_REACTOR_CORE) to 1.0,
				))
			}
		};

		abstract fun getSinkProvider(starship: ActiveStarship): SinkProvider
	}
}
