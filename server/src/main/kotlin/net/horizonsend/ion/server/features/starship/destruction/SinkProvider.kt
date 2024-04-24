package net.horizonsend.ion.server.features.starship.destruction

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
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
		STANDARD {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return StandardSinkProvider(starship)
			}
		},

		CRUISER {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return CruiserSink(starship)
			}
		},

		BATTLECRUISER {
			override fun getSinkProvider(starship: ActiveStarship): SinkProvider {
				return BattlecruiserSink(starship)
			}
		};

		abstract fun getSinkProvider(starship: ActiveStarship): SinkProvider
	}
}
