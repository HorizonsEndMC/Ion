package net.horizonsend.ion.server.features.starship.destruction

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.DyedItemColor
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ItemDisplayContainer
import net.horizonsend.ion.server.features.transport.items.util.DYEABLE_CUBE_MONO
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.minecraft.util.Brightness
import org.bukkit.Color
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.joml.Vector3f
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class SinkAnimation(
	val starship: Starship,
	val size: Int,
	val world: World,
	val origin: Vec3i,
	val scale: Double = 1.5,
) : BukkitRunnable() {
	private val duration = (60 * scale).roundToInt()

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

			val displayContainer = ItemDisplayContainer(world, 1.0f, origin, Vector.getRandom(), item)
			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			blockWrappers.add(ColoredSinkAnimationBlock(
				wrapper = displayContainer,
				direction = vector.multiply(scale),
				initialScale = 1.0 * scale,
				finalScale = 7.0 * scale,
				rotationVector = Vector(
					Random.nextDouble(-0.0015, 0.0015),
					Random.nextDouble(-0.0015, 0.0015),
					Random.nextDouble(-0.0015, 0.0015)
				),
				colors = mapOf(
					Color.fromRGB(HE_LIGHT_ORANGE.value()) to 1,
					Color.ORANGE to 1,
					Color.fromRGB(235, 64, 52) to 1,
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
			val displayContainer = ItemDisplayContainer(world, 1.0f, origin.toCenterVector(), Vector.getRandom(), item)
			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			blockWrappers.add(ColoredSinkAnimationBlock(
				wrapper = displayContainer,
				direction = vector,
				initialScale = 1.0 * scale,
				finalScale = 3.0 * scale,
				rotationVector = Vector(
					Random.nextDouble(-0.05, 0.05),
					Random.nextDouble(-0.05, 0.05),
					Random.nextDouble(-0.05, 0.05)
				),
				colors = mapOf(
					Color.WHITE to 1,
					Color.SILVER to 1,
					Color.GRAY to 1,
					Color.BLACK to 1,
				)
			))
		}
	}

	fun playRandomBlocks() {
		val blockCount = sqrt(sqrt(size.toDouble()) * scale).roundToInt() * 10

		val bockPositions = starship.blocks.shuffled().take(blockCount)

		for (position in bockPositions) {
			val blockData = starship.world.getBlockAtKey(position).blockData
			if (!blockData.material.isItem) continue

			val direction = Vector(
				Random.nextDouble(-6.0, 6.0),
				Random.nextDouble(-6.0, 6.0),
				Random.nextDouble(-6.0, 6.0)
			).multiply(scale)

			val displayContainer = ItemDisplayContainer(world, 1.0f, origin.toCenterVector(), direction, ItemStack(blockData.material))
			displayContainer.getEntity().brightnessOverride = Brightness.FULL_BRIGHT

			blockWrappers.add(SinkAnimationBlock(
				wrapper = displayContainer,
				direction = direction,
				initialScale = 3.0,
				finalScale = 3.0,
				rotationVector = Vector(
					Random.nextDouble(-0.25, 0.25),
					Random.nextDouble(-0.25, 0.25),
					Random.nextDouble(-0.25, 0.25)
				),
				motionAdjuster = {
					if (world.ion.hasFlag(WorldFlag.SPACE_WORLD)) return@SinkAnimationBlock

					direction.y -= 9.81f / 20.0f
				}
			))
		}
	}

	override fun run() {
		iterations++

		if (iterations >= duration) {
			cancel()
			return
		}

		blockWrappers.forEach(SinkAnimationBlock::update)
	}

	override fun cancel() {
		blockWrappers.forEach { it.wrapper.remove() }
	}

	fun schedule() = runTaskTimerAsynchronously(IonServer, 2L, 2L)

	companion object {
		fun blend(original: Number, final: Number, phase: Double): Double {
			return original.toDouble() + phase * (final.toDouble() - original.toDouble())
		}
	}

	open inner class SinkAnimationBlock(
		val wrapper: ItemDisplayContainer,
		val direction: Vector,
		val initialScale: Double,
		val finalScale: Double,
		val rotationVector: Vector,
		val motionAdjuster: SinkAnimationBlock.() -> Unit = {}
	) {
		private val durationOffset = Random.nextInt(duration, (duration / 2) + duration)
		protected val phase get() = iterations.toDouble() / durationOffset.toDouble()

		open fun update() {
			updatePosition()
			wrapper.update()
		}

		private fun updatePosition() {
			motionAdjuster.invoke(this)
			wrapper.offset = wrapper.offset.add(direction.toVector3f())
			wrapper.scale = Vector3f(blend(initialScale, finalScale, phase).toFloat())
			wrapper.heading = wrapper.heading.clone().rotateAroundAxis(rotationVector.normalize(), rotationVector.length())
		}
	}

	inner class ColoredSinkAnimationBlock(
		wrapper: ItemDisplayContainer,
		direction: Vector,
		initialScale: Double,
		finalScale: Double,
		rotationVector: Vector,
		colors: Map<Color, Int>, // Color to weight
	) : SinkAnimationBlock(wrapper, direction, initialScale, finalScale, rotationVector) {
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
