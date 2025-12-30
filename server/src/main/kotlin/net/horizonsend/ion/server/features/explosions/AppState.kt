package net.horizonsend.ion.server.features.explosions

import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.server.features.explosions.utilities.MultiModelRenderer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Location

object GameObjectsTicker : IonComponent() {
	override fun onEnable() {
		Tasks.syncRepeat(1,1){tick()}
	}
	fun tick(){
		for (gameObject in GameObject.live) gameObject.update()
		for (gameObject in GameObject.live) gameObject.render()
		AppState.renderer.flush()
	}
}

object AppState {
    val renderer = MultiModelRenderer()
    var target: Location? = null

    fun close() {
        renderer.close()
    }
}

/**
 * Game object
 *	Game Objects are used for custom effects, they update every tick and render themselves as something initially
 * @constructor Create empty Game object
 */
abstract class GameObject {
    companion object {
        private val liveGameObjects = mutableListOf<GameObject>()
        val live: List<GameObject> get() = liveGameObjects.toList()
    }

    init {
        @Suppress("LeakingThis")
        liveGameObjects += this
    }

    open fun remove() {
        liveGameObjects.remove(this)
    }

    abstract fun update()
    abstract fun render()
}
