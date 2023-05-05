package net.starlegacy.feature.starship.subsystem.shield

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.SLComponent
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.particleshield.BoxShieldMultiblock
import net.starlegacy.feature.multiblock.particleshield.ShieldMultiblock
import net.starlegacy.feature.multiblock.particleshield.SphereShieldMultiblock
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipActivatedEvent
import net.starlegacy.feature.starship.event.StarshipDeactivatedEvent
import net.starlegacy.util.PerWorld
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.getFacing
import net.starlegacy.util.getSphereBlocks
import net.starlegacy.util.nms
import net.starlegacy.util.rightFace
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.boss.BarColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

object StarshipShields : SLComponent() {
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
		val worldID = starship.serverLevel.world.uid

		for (shield in starship.shields) {
			val shieldPos = ShieldPos(worldID, shield.pos)
			shield.power = shields.remove(shieldPos) ?: continue
		}
	}

	@EventHandler
	fun onDeactivate(event: StarshipDeactivatedEvent) {
		val starship = event.starship
		val worldID = starship.serverLevel.world.uid

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

		val blockList = event.blockList()
		val power = explosionPowerOverride ?: getExplosionPower(block, blockList)
		onShieldImpact(block.location.toCenterLocation(), blockList, power)
		if (LAST_EXPLOSION_ABSORBED) {
			event.isCancelled = true
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	fun onEntityExplode(event: EntityExplodeEvent) {
		val location = event.location
		val block = location.block
		val blockList = event.blockList()
		val power = explosionPowerOverride ?: getExplosionPower(block, blockList)
		onShieldImpact(location.toCenterLocation(), blockList, power)
		if (LAST_EXPLOSION_ABSORBED) {
			event.isCancelled = true
		}
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
			if (ship is ActivePlayerStarship) {
				updateShieldBars(ship)
			}
		}
	}

	@Synchronized
	fun updateShieldBars(ship: ActivePlayerStarship) {
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

	private val flaringBlocks = PerWorld { LongOpenHashSet() }
	private val flaringChunks = PerWorld { LongOpenHashSet() }

	private fun onShieldImpact(location: Location, blockList: MutableList<Block>, power: Double) {
		LAST_EXPLOSION_ABSORBED = false

		val world: World = location.world
		val nmsWorld = world.minecraft
		val chunkKey: Long = location.chunk.chunkKey

		val time: Long = world.time

		val size: Int = blockList.size

		if (blockList.isEmpty()) {
			return
		}

		val flaringBlocks: LongOpenHashSet = flaringBlocks[world]
		val flaringChunks: LongOpenHashSet = flaringChunks[world]

		val canFlare = !flaringChunks.contains(chunkKey)

		val flaredBlocks = LongOpenHashSet()

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
				canFlare,
				flaringBlocks,
				flaredBlocks,
				nmsWorld
			)
		}

		scheduleUnflare(canFlare, flaredBlocks, flaringChunks, chunkKey, flaringBlocks, world, nmsWorld)

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
		canFlare: Boolean,
		flaringBlocks: LongOpenHashSet,
		flaredBlocks: LongOpenHashSet,
		nmsLevel: Level
	) {
		// ignore if it's over 500 blocks away
		if (starship.centerOfMassVec3i.toLocation(world).distanceSquared(location) > 250_000) {
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
				canFlare,
				flaringBlocks,
				flaredBlocks,
				nmsLevel,
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
		canFlare: Boolean,
		flaringBlocks: LongOpenHashSet,
		flaredBlocks: LongOpenHashSet,
		nmsLevel: Level,
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

		protectedBlocks.addAll(containedBlocks)

		var usage: Int = (shield.getPowerUsage(power) * damagedPercent).toInt()

		if (shield.isReinforcementActive()) {
			usage = (usage * 0.1f).toInt()
		}

		if (canFlare && protectedBlocks.isNotEmpty() && percent > 0.01f) {
			addFlare(containedBlocks, shield, flaringBlocks, flaredBlocks, nmsLevel)
		}

		shield.power = shield.power - usage

		if (usage > 0) {
			updatedStarships.add(starship)
		}

		return true
	}

	private fun addFlare(
		containedBlocks: List<Block>,
		shield: ShieldSubsystem,
		flaringBlocks: LongOpenHashSet,
		flaredBlocks: LongOpenHashSet,
		nmsLevel: Level
	) {
		val percent = shield.powerRatio

		val flare: BlockState = when {
			shield.isReinforcementActive() -> Material.MAGENTA_STAINED_GLASS
			percent <= 0.05 -> Material.RED_STAINED_GLASS
			percent <= 0.10 -> Material.ORANGE_STAINED_GLASS
			percent <= 0.25 -> Material.YELLOW_STAINED_GLASS
			percent <= 0.40 -> Material.LIME_STAINED_GLASS
			percent <= 0.55 -> Material.GREEN_STAINED_GLASS
			percent <= 0.70 -> Material.CYAN_STAINED_GLASS
			percent <= 0.85 -> Material.LIGHT_BLUE_STAINED_GLASS
			else -> Material.BLUE_STAINED_GLASS
		}.createBlockData().nms

		for (block in containedBlocks) {
			val bx = block.x
			val by = block.y
			val bz = block.z

			val blockKey: Long = block.blockKey

			if (!flaringBlocks.add(blockKey) || !flaredBlocks.add(blockKey)) {
				continue
			}

			val pos = BlockPos(bx, by, bz)
			val packet = ClientboundBlockUpdatePacket(pos, flare)
			nmsLevel.getChunkAt(pos).playerChunk?.broadcast(packet, false)
		}
	}

	private fun scheduleUnflare(
		canFlare: Boolean,
		flaredBlocks: LongOpenHashSet,
		flaringChunks: LongOpenHashSet,
		chunkKey: Long,
		flaringBlocks: LongOpenHashSet,
		world: World,
		nmsLevel: Level
	) {
		if (!canFlare || flaredBlocks.isEmpty()) {
			return
		}

		flaringChunks.add(chunkKey)
		flaringBlocks.addAll(flaredBlocks)

		Tasks.syncDelay(3) {
			flaringChunks.remove(chunkKey)

			for (key: Long in flaredBlocks.iterator()) {
				flaringBlocks.remove(key)

				val data = world.getBlockAtKey(key).blockData.nms

				if (data.block is BaseEntityBlock) {
					world.getBlockAtKey(key).state.update(false, false)
					continue
				}

				val pos = BlockPos(blockKeyX(key), blockKeyY(key), blockKeyZ(key))
				val packet = ClientboundBlockUpdatePacket(pos, data)
				nmsLevel.getChunkAt(pos).playerChunk?.broadcast(packet, false)
			}
		}
	}

	@EventHandler
	fun onClickShield(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) {
			return
		}

		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = Multiblocks[sign] as? ShieldMultiblock ?: return

		val blocks: List<Vec3i> = when (multiblock) {
			is SphereShieldMultiblock -> getSphereBlocks(multiblock.maxRange)
			is BoxShieldMultiblock -> getBoxShieldBlocks(sign)
			else -> return
		}

		val world = sign.world
		val (x0, y0, z0) = Vec3i(sign.location)

		val start = System.nanoTime()
		Tasks.bukkitRunnable {
			for ((dx, dy, dz) in blocks) {
				val x = x0 + dx + 0.5
				val y = y0 + dy + 0.5
				val z = z0 + dz + 0.5
				world.spawnParticle(
					Particle.BLOCK_MARKER,
					x,
					y,
					z,
					1,
					0.0,
					0.0,
					0.0,
					0.0,
					Material.BARRIER.createBlockData()
				)
			}

			if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(10L)) {
				cancel()
			}
		}.runTaskTimer(IonServer, 20, 20)
	}

	private fun getBoxShieldBlocks(sign: Sign): List<Vec3i> {
		val dimensions = sign.getLine(3)
			.replace(",", " ")
			.split(" ")
			.map { it.toInt() }
		val width = dimensions[0]
		val height = dimensions[1]
		val length = dimensions[2]

		val inward = sign.getFacing().oppositeFace
		val right = inward.rightFace

		val dw = width / 2
		val dl = length / 2

		val dx = abs(dw * right.modX + dl * inward.modX)
		val dy = height / 2
		val dz = abs(dw * right.modZ + dl * inward.modZ)

		val blocks = mutableListOf<Vec3i>()

		for (x in (-dx)..(dx)) {
			for (y in (-dy)..(dy)) {
				for (z in (-dz)..(dz)) {
					if (abs(x) != dx && abs(y) != dy && abs(z) != dz) {
						continue
					}

					blocks.add(Vec3i(x, y, z))
				}
			}
		}

		return blocks
	}

	fun cartesianProduct(a: Set<*>, b: Set<*>, vararg sets: Set<*>): Set<List<*>> =
		(setOf(a, b).plus(sets))
			.fold(listOf(listOf<Any?>())) { acc, set ->
				acc.flatMap { list -> set.map { element -> list + element } }
			}
			.toSet()
}
