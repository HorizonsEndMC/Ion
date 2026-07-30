package net.horizonsend.ion.server.features.explosions.utilities

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.util.RayTraceResult
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.AxisAngle4f
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3i
import kotlin.math.cos
import kotlin.math.sin


fun playSound(location: Location, sound: org.bukkit.Sound, volume: Float, pitch: Float) {
    location.world!!.playSound(location, sound, volume, pitch)
}

fun <T : Entity> spawnEntity(location: Location, clazz: Class<T>, initializer: (T) -> Unit): T {
    return location.world!!.spawn(location, clazz, initializer)
}

fun spawnParticle(particle: org.bukkit.Particle, location: Location, count: Int, offsetX: Double, offsetY: Double, offsetZ: Double, extra: Double) {
    location.world!!.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra)
}

fun <T> spawnParticle(
    particle: org.bukkit.Particle,
    location: Location,
    count: Int,
    offsetX: Double,
    offsetY: Double,
    offsetZ: Double,
    extra: Double,
    data: T
) {
    location.world!!.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data)
}

fun raycastGround(location: Location, direction: Vector, maxDistance: Double): RayTraceResult? {
    return location.world!!.rayTraceBlocks(location, direction, maxDistance, FluidCollisionMode.NEVER, true)
}


fun centredTransform(xSize: Float, ySize: Float, zSize: Float): Transformation {
    return Transformation(
        Vector3f(-xSize / 2, -ySize / 2, -zSize / 2),
        AxisAngle4f(0f, 0f, 0f, 1f),
        Vector3f(xSize, ySize, zSize),
        AxisAngle4f(0f, 0f, 0f, 1f)
    )
}

fun ring(count: Int): List<Pair<Double, Double>> {
    val out = mutableListOf<Pair<Double, Double>>()

    for (i in 0 until count) {
        val angle = i.toDouble() / count * 2 * Math.PI
        out.add(sin(angle) to cos(angle))
    }

    return out
}


fun transformFromMatrix(matrix: Matrix4f): Transformation {
    val translation = matrix.getTranslation(Vector3f())
    val rotation = matrix.getUnnormalizedRotation(Quaternionf())
    val scale = matrix.getScale(Vector3f())

    return Transformation(translation, rotation, scale, Quaternionf())
}

fun applyTransformationWithInterpolation(entity: BlockDisplay, transformation: Transformation) {
    if (entity.transformation != transformation) {
        entity.transformation = transformation
        entity.interpolationDelay = 0
    }
}

fun applyTransformationWithInterpolation(entity: BlockDisplay, matrix: Matrix4f) {
    applyTransformationWithInterpolation(entity, transformFromMatrix(matrix))
}

fun sphereBlockOffsets(radius: Int) = sequence {
    for (x in -radius..radius) for (z in -radius..radius) for (y in -radius..radius) {
        if (x * x + y * y + z * z > radius * radius) continue
        yield(Vector3i(x, y, z))
    }
}
