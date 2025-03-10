package net.horizonsend.ion.server.features.starship.factory

import net.horizonsend.ion.common.database.schema.starships.Blueprint
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactorySettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.cube
import org.bukkit.Particle
import org.bukkit.entity.Player

class BoundingBoxTask(
	blueprint: Blueprint,
	settings: ShipFactorySettings,
	entity: ShipFactoryEntity,
	val player: Player
) : ShipFactoryBlockProcessor(blueprint, settings, entity) {
	override var clipboardNormalizationOffset: Vec3i = getClipboardOffset()
	override var target = calculateTarget()

	fun getBlueprintId() = blueprint._id

	fun tick() {
		val minimumPoint = Vec3i(clipboard.minimumPoint.x(), clipboard.minimumPoint.y(), clipboard.minimumPoint.z())
		val maximumPoint = Vec3i(clipboard.maximumPoint.x(), clipboard.maximumPoint.y(), clipboard.maximumPoint.z())

		val minLoc = minimumPoint.toCenterVector().toLocation(entity.world)
		val maxLoc = maximumPoint.toCenterVector().toLocation(entity.world)

		val points = cube(minLoc, maxLoc)

		for (point in points) {
			val adjusted = toWorldCoordinates(Vec3i(point)).toLocation(entity.world).toCenterLocation()
			// I can't think of a better way to do this
			player.spawnParticle(Particle.SOUL_FIRE_FLAME, adjusted, 1, 0.0, 0.0, 0.0, 0.0)
		}
	}

	fun recalculate() {
		clipboardNormalizationOffset = getClipboardOffset()
		target = calculateTarget()
	}
}
