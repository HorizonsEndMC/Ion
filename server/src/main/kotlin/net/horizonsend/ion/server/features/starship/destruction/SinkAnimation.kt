package net.horizonsend.ion.server.features.starship.destruction

import io.netty.util.internal.ThreadLocalRandom
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getEnumSetting
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.modular.DisplayWrapper
import net.horizonsend.ion.server.features.client.display.modular.ItemDisplayContainer
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.transport.items.util.DYEABLE_CUBE_MONO
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.orthogonalVectors
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.minecraft.util.Brightness
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cbrt
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.random.Random

class SinkAnimation(
	val starship: Starship,
	val size: Int,
	val world: World,
	val origin: Vec3i,
) : BukkitRunnable() {
	val scale: Double = 0.5 * (cbrt(size.toDouble()) / cbrt(739.0))
	private val baseDuration = (60 * scale).roundToLong()

	private val blockWrappers = ObjectOpenHashSet<SinkAnimationBlock>()

	private var iterations = 0

	init {
		addExplosion()
		addShockwave()
		playRandomBlocks()
	}

	fun addExplosion() {
		val referenceCenter = origin.toCenterVector()

		val explosionBlockCount = (sqrt(size.toDouble()) * 5 * scale).roundToInt()

		repeat(explosionBlockCount) {
			val origin = Vec3i(starship.blocks.random()).toCenterVector()
			var vector = origin.clone().subtract(referenceCenter)

			if (vector.isZero) vector = Vector.getRandom()

			vector.normalize()
			vector.y = vector.y.coerceIn(-0.25..0.25)

			val initColor = Color.ORANGE

			val item = DYEABLE_CUBE_MONO.construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(initColor, false)) }

			val originHeading = Vector(Random.nextDouble(-1.0, 1.0), Random.nextDouble(-1.0, 1.0), Random.nextDouble(-1.0, 1.0))
			val (_, _) = originHeading.orthogonalVectors()

			val displayContainer = ItemDisplayContainer(world = world, initScale = 1.0f, initPosition = origin, initHeading = originHeading, item = item, playerFilter = playerFilter)
			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			blockWrappers.add(ColoredSinkAnimationBlock(
				duration = Random.nextLong(baseDuration, (baseDuration * 1.5).roundToLong()),
				wrapper = displayContainer,
				direction = vector.multiply(scale).multiply(Random.nextDouble(0.9, 1.1)),
				initialScale = 2.0 * scale,
				finalScale = 8.0 * scale,
				rotationAxis = Vector.getRandom(),
				rotationDegrees = 360.0 / 20.0,
				colors = mapOf(
					Color.fromRGB(255, 219, 1) to 2,
					Color.ORANGE to 2,
					Color.fromRGB(235, 64, 52) to 2,
					Color.GRAY to 1,
					Color.BLACK to 1,
				)
			))
		}
	}

	fun addShockwave() {
		val shockwavePoints = (90 * scale).roundToInt()

		repeat(shockwavePoints) { iteration ->
			val degrees = (shockwavePoints / iteration.toDouble()) * 360.0
			val vector = BlockFace.NORTH.direction.rotateAroundY(Math.toRadians(degrees)).normalize().multiply(3).multiply(scale)

			val item = DYEABLE_CUBE_MONO.construct { t -> t.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.GRAY, false)) }
			val displayContainer = ItemDisplayContainer(world, 1.0f, origin.toCenterVector(), Vector.getRandom(), item, playerFilter = playerFilter)
			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			blockWrappers.add(ColoredSinkAnimationBlock(
				duration = (baseDuration * 1.5).roundToLong(),
				wrapper = displayContainer,
				direction = vector,
				initialScale = 1.0 * scale,
				finalScale = 3.0 * scale,
				rotationAxis = Vector(
					Random.nextDouble(-0.05, 0.05),
					Random.nextDouble(-0.05, 0.05),
					Random.nextDouble(-0.05, 0.05)
				),
				rotationDegrees = 360.0 / 20.0,
				colors = mapOf(
					Color.WHITE to 4,
					Color.SILVER to 3,
					Color.GRAY to 2,
					Color.BLACK to 1,
				)
			))
		}
	}

	fun playRandomBlocks() {
		val blockCount = sqrt(sqrt(min(size, 20000).toDouble()) * scale).roundToInt() * 10

		val bockPositions = starship.blocks.shuffled().take(blockCount)

		val random = ThreadLocalRandom.current()

		for (position in bockPositions) {
			val blockData = starship.world.getBlockAtKey(position).blockData
			if (!blockData.material.isItem) continue

			val position = Vec3i(position).toVector()

			val direction = position.clone().subtract(origin.toCenterVector()).normalize().multiply(6 * scale).multiply(Random.nextDouble(0.75, 1.25))

			val displayContainer = ItemDisplayContainer(
				world = world,
				initPosition = position,
				initHeading = direction,
				initScale = 1.0f,
				item = ItemStack(blockData.material),
				playerFilter = playerFilter
			)

			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			val d = random.nextLong(baseDuration, (baseDuration / 2) + baseDuration)

			blockWrappers.add(SinkAnimationBlock(
				duration = d,
				wrapper = displayContainer,
				direction = direction,
				initialScale = 1.0,
				finalScale = 1.0,
				rotationAxis = Vector(
					Random.nextDouble(-0.025, 0.025),
					Random.nextDouble(-0.025, 0.025),
					Random.nextDouble(-0.025, 0.025)
				),
				rotationDegrees = 360.0 / 20.0,
				motionAdjuster = {
					if (world.ion.hasFlag(WorldFlag.SPACE_WORLD)) return@SinkAnimationBlock

					direction.y -= 9.81f / 20.0f
				}
			))
		}
	}

	override fun run() {
		iterations++

		@Suppress("SYNTHETIC_PROPERTY_WITHOUT_JAVA_ORIGIN")
		if (blockWrappers.isEmpty) {
			cancel()
			return
		}

		blockWrappers.forEach(SinkAnimationBlock::update)
		blockWrappers.removeAll(SinkAnimationBlock::checkDead)
	}

	override fun cancel() {
		blockWrappers.forEach { it.wrapper.remove() }
	}

	fun schedule() = runTaskTimerAsynchronously(IonServer, 2L, 2L)

	companion object {
		fun blend(original: Number, final: Number, phase: Double): Double {
			return original.toDouble() + phase * (final.toDouble() - original.toDouble())
		}

		private val playerFilter = { player: Player ->
			val playerSetting = player.getEnumSetting<ClientDisplayEntities.Visibility>(PlayerSettings::displayEntityVisibility)

			when (playerSetting) {
				ClientDisplayEntities.Visibility.ON -> true
				ClientDisplayEntities.Visibility.REDUCED -> Random.nextBoolean()
				ClientDisplayEntities.Visibility.OFF -> false
			}
		}
	}

	open class SinkAnimationBlock(
		val duration: Long,
		open val wrapper: DisplayWrapper,
		var direction: Vector,
		val initialScale: Double,
		val finalScale: Double,
		val rotationAxis: Vector,
		val rotationDegrees: Double,
		val motionAdjuster: SinkAnimationBlock.() -> Unit = {}
	) {
		private var iterations: Int = 0

		protected val phase get() = iterations.toDouble() / duration.toDouble()

		open fun update() {
			iterations++

			updatePosition()
			wrapper.update()
		}

		private fun updatePosition() {
			motionAdjuster.invoke(this)
			wrapper.offset = wrapper.offset.add(direction)

			val scale = blend(initialScale, finalScale, phase).toFloat()
			wrapper.scale = Vector(scale, scale, scale)

			val rotated = wrapper.heading.clone().rotateAroundNonUnitAxis(rotationAxis, Math.toRadians(rotationDegrees))
			wrapper.heading = rotated
		}

		fun checkDead(): Boolean {
			val dead = iterations >= duration
			if (dead) wrapper.remove()
			return dead
		}
	}

	inner class ColoredSinkAnimationBlock(
		duration: Long,
		override val wrapper: ItemDisplayContainer,
		direction: Vector,
		initialScale: Double,
		finalScale: Double,
		rotationAxis: Vector,
		rotationDegrees: Double,
		colors: Map<Color, Int>, // Color to weight
	) : SinkAnimationBlock(duration, wrapper, direction, initialScale, finalScale, rotationAxis, rotationDegrees) {
		val colors = mutableListOf<Color>().apply {
			for ((color, weight) in colors) {
				repeat(weight) { add(color) }
			}
		}

		override fun update() {
			updateColor()
			super.update()
		}

		private fun updateColor() {
			val item = wrapper.itemStack

			val colorPhase = colors.lastIndex * phase
			val colorIndex = colorPhase.toInt()

			val color = colors[colorIndex]
			val nextColor = colors[(colorIndex + 1).coerceAtMost(colors.lastIndex)]

			val blendedR = blend(color.red, nextColor.red, colorPhase - colorIndex).roundToInt().coerceAtMost(255)
			val blendedG = blend(color.green, nextColor.green, colorPhase - colorIndex).roundToInt().coerceAtMost(255)
			val blendedB = blend(color.blue, nextColor.blue, colorPhase - colorIndex).roundToInt().coerceAtMost(255)

			val newColor = Color.fromRGB(blendedR, blendedG, blendedB)

			wrapper.itemStack = item.clone().updateData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(newColor, false))
		}
	}
}
