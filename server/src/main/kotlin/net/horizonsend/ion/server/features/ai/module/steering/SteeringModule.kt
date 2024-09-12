import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.util.Vector

abstract class SteeringModule(var controler: AIController) : AIModule(controler){
    abstract fun steer() : SteeringOutput
    data class SteeringOutput(val newHeading : Vector, val newThrust : Vector)
}
