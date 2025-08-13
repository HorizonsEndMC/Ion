package net.horizonsend.ion.server.features.starship.subsystem.shield

import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.command.admin.debugRed
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.nations.utils.isNPC
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveControlledStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.event.StarshipActivatedEvent
import net.horizonsend.ion.server.features.starship.event.StarshipDeactivatedEvent
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.SLTextStyle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.boss.BarColor
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.math.sqrt

object StarshipShields : IonServerComponent() {
	var LAST_EXPLOSION_ABSORBED = false

	val updatedStarships = ConcurrentHashMap.newKeySet<ActiveStarship>()
	private var explosionPowerOverride: Double? = null

	override fun onEnable() {
		Tasks.syncRepeat(5L, 5L) {
			updateShieldBars()
		}
	}

	private data class ShieldPos(val worldID: UUID, val pos: Vec3i)

	private val shields = mutableMapOf<ShieldPos, Int>()

	@EventHandler
	fun onActivate(event: StarshipActivatedEvent) {
		val starship = event.starship
		val worldID = starship.world.uid

		for (shield in starship.shields) {
			val shieldPos = ShieldPos(worldID, shield.pos)
			shield.power = shields.remove(shieldPos) ?: continue
		}
	}

	@EventHandler
	fun onDeactivate(event: StarshipDeactivatedEvent) {
		val starship = event.starship
		val worldID = starship.world.uid

		for (shield in starship.shields) {
			val shieldPos = ShieldPos(worldID, shield.pos)
			shields[shieldPos] = shield.power
		}
	}

	fun withExplosionPowerOverride(value: Double, block: () -> Unit) {
		try {
			explosionPowerOverride = value
			block()
		} finally {
			explosionPowerOverride = null
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onBlockExplode(event: BlockExplodeEvent) {
		val block = event.block
		handleExplosion(block, event.block.location, event.blockList(), event)
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		val location = event.location
		val block = location.block
		handleExplosion(block, event.location, event.blockList(), event)
	}

	private fun handleExplosion(block: Block, location: Location, blockList: MutableList<Block>, event: Cancellable) {
		if (ProtectionListener.isProtectedCity(block.location)) return
		val power = explosionPowerOverride ?: getExplosionPower(block, blockList)

		onShieldImpact(location.toCenterLocation(), blockList, power)

		if (LAST_EXPLOSION_ABSORBED) event.isCancelled = true
	}

	private fun getExplosionPower(center: Block, blockList: List<Block>): Double {
		val x = center.x.d()
		val y = center.y.d()
		val z = center.z.d()
		val biggestDistance = blockList.maxOfOrNull { distanceSquared(it.x.d(), it.y.d(), it.z.d(), x, y, z) } ?: 0.0
		return sqrt(biggestDistance)
	}

	private fun updateShieldBars() {
		val iterator = updatedStarships.iterator()
		while (iterator.hasNext()) {
			val ship = iterator.next()
			if (ship is ActiveControlledStarship) {
				updateShieldBars(ship)
			}
		}
	}

	@Synchronized
	fun updateShieldBars(ship: ActiveControlledStarship) {
		for ((name, bossBar) in ship.shieldBars) {
			var amount = 0
			var isReinforced = false
			var total = 0.0
			val percents = ArrayList<Double>()

			for (subsystem in ship.shields) {
				if (subsystem.name != name) continue
				amount++
				isReinforced = subsystem.isReinforcementActive()
				val subsystemPercent = subsystem.powerRatio
				total += subsystemPercent
				percents.add(subsystemPercent)
			}

			val percent = total / amount.toDouble()

			bossBar.progress = min(percent, 1.0)
			val titleColor = percentColor(percent, isReinforced)

			val barColor = when {
				isReinforced -> BarColor.PURPLE
				percent <= 0.05 -> BarColor.RED
				percent <= 0.25 -> BarColor.YELLOW
				percent <= 0.55 -> BarColor.GREEN
				else -> BarColor.BLUE
			}

			val extraPercents: String = if (percents.size > 1) {
				" (${
				percents.joinToString(separator = " + ") {
					"${percentColor(it, isReinforced)}${formatPercent(it)}%$titleColor"
				}
				})"
			} else {
				""
			}
			val title = "${SLTextStyle.GRAY}$name$titleColor ${formatPercent(total)}%$extraPercents"
			if (bossBar.title != title) {
				bossBar.setTitle(title)
			}
			if (bossBar.color != barColor) {
				bossBar.color = barColor
			}
		}
	}

	private fun percentColor(percent: Double, reinforced: Boolean): SLTextStyle = when {
		reinforced -> SLTextStyle.LIGHT_PURPLE
		percent <= 0.05 -> SLTextStyle.RED
		percent <= 0.10 -> SLTextStyle.GOLD
		percent <= 0.25 -> SLTextStyle.YELLOW
		percent <= 0.40 -> SLTextStyle.GREEN
		percent <= 0.55 -> SLTextStyle.DARK_GREEN
		percent <= 0.70 -> SLTextStyle.AQUA
		percent <= 0.85 -> SLTextStyle.DARK_AQUA
		else -> SLTextStyle.BLUE
	}

	private fun formatPercent(percent: Double): Double = (percent * 1000).toInt().toDouble() / 10.0

	private fun onShieldImpact(location: Location, blockList: MutableList<Block>, power: Double) {
		LAST_EXPLOSION_ABSORBED = false

		val world: World = location.world
		val size: Int = blockList.size

		if (blockList.isEmpty()) {
			return
		}

		val protectedBlocks = HashSet<Block>()
		for (starship in ActiveStarships.getInWorld(world)) {
			processShip(
				starship,
				world,
				location,
				blockList,
				size,
				power,
				protectedBlocks,
			)
		}

		blockList.removeAll(protectedBlocks)

		if (blockList.isEmpty()) {
			LAST_EXPLOSION_ABSORBED = true
			location.world.playSound(location, Sound.ENTITY_IRON_GOLEM_HURT, 8.0f, 0.5f)
		}
	}

	private fun processShip(
		starship: ActiveStarship,
		world: World,
		location: Location,
		blockList: MutableList<Block>,
		size: Int,
		radius: Double,
		protectedBlocks: HashSet<Block>,
	) {
		// ignore if it's over 500 blocks away
		if (starship.centerOfMass.toLocation(world).distanceSquared(location) > 250_000) {
			return
		}

		val blocks = blockList.filter { b -> starship.contains(b.x, b.y, b.z) }

		if (blocks.isEmpty()) {
			return
		}

		val damagedPercent = blocks.size.toFloat() / size.toFloat()

		shieldLoop@
		for (shield: ShieldSubsystem in starship.shields) {
			processShield(
				shield,
				radius,
				protectedBlocks,
				blocks,
				damagedPercent,
				starship
			)
		}
	}

	private fun processShield(
		shield: ShieldSubsystem,
		power: Double,
		protectedBlocks: HashSet<Block>,
		blocks: List<Block>,
		damagedPercent: Float,
		starship: ActiveStarship
	): Boolean {
		val containedBlocks = blocks.filter { shield.containsBlock(it) }

		if (containedBlocks.isEmpty()) {
			return false
		}

		val percent = shield.powerRatio
		if (percent < 0.01) {
			return false
		}

		if (!shield.isIntact()) {
			return false
		}

		// shield power that is consumed per shot
		var usage: Int = (shield.getPowerUsage(power) * damagedPercent).toInt()

		// If shield is enhanced, reduce shot power to 10%
		if (shield.isReinforcementActive()) {
			usage = (usage * 0.1f).toInt()
		}

		starship.debugRed("shield damage = ${shield.power} - $usage = ${shield.power - usage}")
		shield.power -= usage
		//shield.recentDamage += usage / shield.maxPower

		// do not protect blocks if shield power is lowered to 0
		if (shield.power <= 0) {
			return false
		}

		// protection check passed; add all blocks in shield to list
		protectedBlocks.addAll(containedBlocks)

		if (protectedBlocks.isNotEmpty() && percent > 0.01f) {
			spawnShieldDisplayFlares(
				starship = starship,
				blocks = containedBlocks,
				percent = percent,
				reinforced = shield.isReinforcementActive()
			)
		}

		if (usage > 0) {
			updatedStarships.add(starship)
		}

		return true
	}

	private fun spawnShieldDisplayFlares(
		starship: Starship,
		blocks: List<Block>,
		percent: Double,
		reinforced: Boolean
	) {
		val sample = blocks.firstOrNull()?.location?.toCenterLocation() ?: return

		// Only spawn if at least one nearby player
		val interested = sample.getNearbyPlayers(500.0) { !it.isNPC }
		if (interested.isEmpty()) return

		Tasks.async {
			// Lifetime: use the max preference among nearby players
			val lifetime = interested.maxOf { it.getSetting(PlayerSettings::flareTime).toLong() }

			// Throttle number of displays per hit to keep it light
			val maxFlares = 20
			val chosen = if (blocks.size > maxFlares) blocks.shuffled().take(maxFlares) else blocks

			val mat = shieldMaterialFor(percent, reinforced)

			for (b in chosen) {
				val local = starship.getLocalCoordinate(Vec3i(b.x, b.y, b.z))

				ShieldFlareDisplay(starship = starship, local = local, colorItem = mat, lifetime = lifetime).schedule()
			}
		}
	}

	private fun shieldMaterialFor(percent: Double, reinforced: Boolean): Material = when {
		reinforced          -> Material.MAGENTA_STAINED_GLASS
		percent <= 0.05     -> Material.RED_STAINED_GLASS
		percent <= 0.10     -> Material.ORANGE_STAINED_GLASS
		percent <= 0.25     -> Material.YELLOW_STAINED_GLASS
		percent <= 0.40     -> Material.LIME_STAINED_GLASS
		percent <= 0.55     -> Material.GREEN_STAINED_GLASS
		percent <= 0.70     -> Material.LIGHT_BLUE_STAINED_GLASS // close to CYAN tier
		percent <= 0.85     -> Material.LIGHT_BLUE_STAINED_GLASS
		else                -> Material.BLUE_STAINED_GLASS
	}
}
