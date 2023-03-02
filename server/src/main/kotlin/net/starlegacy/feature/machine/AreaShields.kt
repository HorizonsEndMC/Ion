package net.starlegacy.feature.machine

import net.horizonsend.ion.server.IonServer
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.areashield.AreaShield
import net.starlegacy.util.Tasks
import net.starlegacy.util.isInRange
import net.starlegacy.util.isWallSign
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object AreaShields : SLComponent() {
	val bypassShieldEvents = ConcurrentHashMap.newKeySet<BlockExplodeEvent>()

	override fun onEnable() {
		loadData()

		Tasks.asyncRepeat(0L, 20L * 60L) {
			saveData()
		}
	}

	var configuration: YamlConfiguration? = null

	//region File data
	fun loadData() {
		val areaShieldFile = File(IonServer.dataFolder, "areashields.yml")
		if (!areaShieldFile.exists()) {
			return
		}

		configuration = YamlConfiguration.loadConfiguration(areaShieldFile)
		for (worldName in configuration!!.getKeys(false)) {
			val world = Bukkit.getWorld(worldName)

			if (world == null) {
				log.warn("WORLD NOT FOUND $world! FAILED TO LOAD AREA SHIELD!")
				continue
			}

			configuration!!.getConfigurationSection(worldName)?.getKeys(false)?.forEach { vector ->
				val split = vector.split(",")
				val location = Location(world, split[0].toDouble(), split[1].toDouble(), split[2].toDouble())
				val radius = configuration!!.getInt("$worldName.$vector")
				locationRadiusMap[location] = radius
			}
		}
	}

	private fun saveData() {
		val file = File(IonServer.dataFolder, "areashields.yml")

		if (configuration == null) configuration = YamlConfiguration()

		for (world in Bukkit.getWorlds()) {
			configuration!!.createSection(world.name)
		}

		for (location in locationRadiusMap.keys) {
			val worldName = location.world.name
			val text = "${location.blockX},${location.blockY},${location.blockZ}"
			val radius = locationRadiusMap[location]
			configuration!!.getConfigurationSection(worldName)!!.set(text, radius)
		}

		configuration!!.save(file)
	}
	//endregion

	private var locationRadiusMap = ConcurrentHashMap<Location, Int>()

	fun register(location: Location, radius: Int) {
		locationRadiusMap[location] = radius
	}

	fun removeAreaShield(location: Location): Boolean {
		val removed = locationRadiusMap.remove(location)
		return removed != null
	}

	fun getNearbyAreaShields(
		location: Location,
		explosionSize: Double
	): Map<Location, Int> = locationRadiusMap.filter { (shieldLoc: Location, shieldRadius: Int) ->
		val radius = shieldRadius + explosionSize
		return@filter shieldLoc.world == location.world && shieldLoc.isInRange(location, radius)
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	fun onBlockExplode(event: BlockExplodeEvent) {
		if (bypassShieldEvents.remove(event)) return
		if (event.yield == 0.123f) return
		var distance = 0.0
		val location = event.block.location

		for (block in event.blockList()) {
			distance = max(distance, block.location.distanceSquared(location))
		}

		distance = ceil(sqrt(distance))
		onShieldImpact(
			location,
			event.blockList(),
			distance,
			event.yield != 0.10203f
		)
		if (event.blockList().isEmpty()) event.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		if (event.yield == 0.123f) return
		var distance = 0.0
		val location = event.location
		for (block in event.blockList())
			distance = max(distance, block.location.distanceSquared(location))
		distance = ceil(sqrt(distance))
		onShieldImpact(
			event.location,
			event.blockList(),
			distance,
			event.yield != 0.10203f
		)
		if (event.blockList().isEmpty()) event.isCancelled = true
	}

	private fun onShieldImpact(
		location: Location,
		blockList: MutableList<Block>,
		explosionSize: Double,
		usePower: Boolean
	) {
		val areaShields = getNearbyAreaShields(location, explosionSize)
		var shielded = false
		for (shieldLocation in areaShields.keys) {
			val block = shieldLocation.block
			if (!block.type.isWallSign) {
				continue
			}
			val sign = block.getState(false) as Sign
			val multiblock = Multiblocks[sign] as? AreaShield ?: continue
			if (multiblock.radius != areaShields.get(shieldLocation)) continue
			var power = PowerMachines.getPower(sign)
			if (power <= 0) continue
			power -= blockList.size * 2
			val percent = power.toFloat() / multiblock.maxPower.toFloat()
			if (usePower) PowerMachines.setPower(sign, power)
			val color = Color.fromRGB(
				min(255f, 255 - max(0f, 255 * percent)).toInt(),
				0,
				min(255f, max(0f, 255 * percent)).toInt()
			)

			val center = location.toCenterLocation()

			val particle = Particle.REDSTONE
			val dustOptions = Particle.DustOptions(color, 100f)
			center.world.spawnParticle(particle, location, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
			shielded = true
		}
		if (shielded) blockList.clear()
	}

	@EventHandler
	fun onEntityDamage(event: EntityDamageEvent) {
		if (
			event.cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION &&
			event.cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
		) {
			return
		}

		val areaShields = getNearbyAreaShields(event.entity.location, 1.0)
		for (shieldLocation in areaShields.keys) {
			val block = shieldLocation.block
			if (!block.type.isWallSign) continue
			val sign = block.getState(false) as Sign
			val multiblock = Multiblocks[sign] as? AreaShield ?: continue
			if (multiblock.radius != areaShields[shieldLocation]) continue
			if (PowerMachines.getPower(sign) > 0) event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBreak(event: BlockBreakEvent) {
		if (removeAreaShield(event.block.location)) {
			event.player.sendActionBar("Removed area shield")
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun onBlockBurn(event: BlockBurnEvent) {
		removeAreaShield(event.block.location)
	}
}
