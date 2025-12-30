package net.horizonsend.ion.server.features.explosions.utilities

import org.bukkit.Location
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.TextDisplay
import org.bukkit.util.Vector
import org.joml.Matrix4f
import java.io.Closeable

class ModelPart <T : Entity> (
    val clazz : Class<T>,
    val location : Location,
    val init : (T) -> Unit = {},
    val update : (T) -> Unit = {}
)

class Model {
    val parts = mutableMapOf<Any, ModelPart<out Entity>>()

    fun add(id: Any, part: ModelPart<out Entity>) {
        parts[id] = part
    }


    fun add(id: Any, model: Model) {
        for ((subId, part) in model.parts) {
            parts[id to subId] = part
        }
    }
}

fun blockModel(
    location: Location,
    init: (BlockDisplay) -> Unit = {},
    update: (BlockDisplay) -> Unit = {}
) = ModelPart(
    clazz = BlockDisplay::class.java,
    location = location,
    init = init,
    update = update
)

class ModelRenderer: Closeable {
    val rendered = mutableMapOf<Any, Entity>()

    private val used = mutableSetOf<Any>()

    override fun close() {
        for (entity in rendered.values) {
            entity.remove()
        }
        rendered.clear()
        used.clear()
    }

    fun render(model: Model) {
        for ((id, template) in model.parts) {
            renderPart(id, template)
        }

        val toRemove = rendered.keys - used
        for (key in toRemove) {
            val entity = rendered[key]!!
            entity.remove()
            rendered.remove(key)
        }
        used.clear()
    }

    fun <T: Entity>render(part: ModelPart<T>) {
        val model = Model().apply { add(0, part) }
        render(model)
    }

    private fun <T: Entity>renderPart(id: Any, template: ModelPart<T>) {
        used.add(id)

        val oldEntity = rendered[id]
        if (oldEntity != null) {
            // check if the entity is of the same type
            if (oldEntity.type.entityClass == template.clazz) {
                oldEntity.teleport(template.location)
                @Suppress("UNCHECKED_CAST")
                template.update(oldEntity as T)
                return
            }

            oldEntity.remove()
            rendered.remove(id)
        }

        val entity = spawnEntity(template.location, template.clazz) {
            template.init(it)
            template.update(it)
        }
        rendered[id] = entity
    }
}


class MultiModelRenderer: Closeable {
    private val renderer = ModelRenderer()
    private var model = Model()

    fun render(id: Any, model: Model) {
        this.model.add(id, model)
    }

    fun render(id: Any, model: ModelPart<out Entity>) {
        this.model.add(id, model)
    }

    fun flush() {
        renderer.render(model)
        model = Model()
    }

    override fun close() {
        renderer.close()
    }
}
