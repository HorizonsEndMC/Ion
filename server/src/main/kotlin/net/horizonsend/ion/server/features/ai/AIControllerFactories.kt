package net.horizonsend.ion.server.features.ai

import BasicSteeringModule
import TravelSteeringModule
import net.horizonsend.ion.server.IonServer.aiSteeringConfig
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.module.combat.DefensiveCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.GoonCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.MultiTargetFrigateCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.ai.module.debug.AIDebugModule
import net.horizonsend.ion.server.features.ai.module.misc.ContactsJammerModule
import net.horizonsend.ion.server.features.ai.module.misc.GravityWellModule
import net.horizonsend.ion.server.features.ai.module.misc.TrackingModule
import net.horizonsend.ion.server.features.ai.module.movement.SteeringSolverModule
import net.horizonsend.ion.server.features.ai.module.positioning.DistancePositioningModule
import net.horizonsend.ion.server.features.ai.module.steering.CapitalSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.GunshipSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.StarfighterSteeringModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestLargeStarshipTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestPlayerTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestSmallStarshipTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.HighestDamagerTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import java.util.Optional
import kotlin.random.Random

@Suppress("unused") // Entry points
object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()

	val starfighter = registerFactory("STARFIGHTER") {
		setControllerTypeName("Starfighter")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val targetingOriginal = builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })
			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.starfighterDistanceConfiguration))
			val steering = builder.addModule("steering", StarfighterSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
	}

	val gunship = registerFactory("GUNSHIP") {
		setControllerTypeName("Gunship")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
    }

	val gunship_pulse = registerFactory("GUNSHIP_PULSE") {
		setControllerTypeName("PulseGunship")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
	}

	val goonship = registerFactory("GOONSHIP") {
		setControllerTypeName("GoonShip")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", GoonCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })
			val steering = builder.addModule("steering", BasicSteeringModule(it) {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()})
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))
			builder
		}
		build()
	}

	val jammingGunship = registerFactory("JAMMING_GUNSHIP") {
		setControllerTypeName("JammingGunship")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()
			builder.addModule("targeting", ClosestPlayerTargetingModule(it, 5000.0))
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))

			builder.addModule("jamming", ContactsJammerModule(it, 300.0) { builder.suppliedModule<TargetingModule>("targeting").get().findTargets() })
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
	}

	val corvette = registerFactory("CORVETTE") {
		setControllerTypeName("Corvette")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 500.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.corvetteBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))
			builder
		}
		build()
	}

	val interdictionCorvette = registerFactory("INTERDICTION_CORVETTE") {
		setControllerTypeName("Interdiction Corvette")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 2000.0, null).apply { sticky = false })
			builder.addModule("combat", StarfighterCombatModule(it) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })
			builder.addModule("gravityWell", GravityWellModule(it, 1800.0, true) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.interdictionCorvetteDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.corvetteBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
	}

	val logisticCorvette = registerFactory("LOGISTIC_CORVETTE") {
		setControllerTypeName("Logistic Corvette")
		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestLargeStarshipTargetingModule(it, 2000.0, null, true).apply { sticky = false })
			builder.addModule("combat", MultiTargetFrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TargetingModule>("targeting").get().findTargets() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.logisticCorvetteDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.corvetteBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}
		build()
	}

	val miniFrigate = registerFactory("MINI_FRIGATE") {//for 4.9k ships
		setControllerTypeName("Frigate")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.miniFrigateDistanceConfiguration))
			val steering = builder.addModule("steering", GunshipSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.miniFrigateBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}

		build()
	}

	val frigate = registerFactory("FRIGATE") {
        setControllerTypeName("Frigate")

        setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 1500.0, null).apply { sticky = true })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.capitalDistanceConfiguration))
			val steering = builder.addModule("steering", CapitalSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.CRUISE))
			builder.addModule("debug", AIDebugModule(it))

			builder
        }

        build()
    }

	val advancedFrigate = registerFactory("ADVANCED_FRIGATE") {
        setControllerTypeName("Advanced Frigate")

        setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestSmallStarshipTargetingModule(it, 700.0, null).apply { sticky = true })
			builder.addModule("tracking", TrackingModule(it, 5, 1800.0, 15.0) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TrackingModule>("tracking").get().findTarget() })
			builder.addModule("gravityWell", GravityWellModule(it, 2400.0, true) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.advancedCapitalDistanceConfiguration))
			val steering = builder.addModule("steering", CapitalSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()}) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.CRUISE))
			builder.addModule("debug", AIDebugModule(it))

			builder
        }

        build()
	}

	val destroyer = registerFactory("DESTROYER") {
		setControllerTypeName("Destroyer")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestTargetingModule(it, 5000.0, null).apply { sticky = true })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.capitalDistanceConfiguration))
			val steering = builder.addModule("steering", CapitalSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.destroyerBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.CRUISE))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}

		build()
	}

	val advancedDestroyer = registerFactory("ADVANCED_DESTROYER") {
		setControllerTypeName("Advanced Destroyer")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestLargeStarshipTargetingModule(it, 5000.0, null).apply { sticky = true })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = false) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.advancedCapitalDistanceConfiguration))
			val steering = builder.addModule("steering", CapitalSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.destroyerBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.CRUISE))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}

		build()
	}

	val battlecruiser = registerFactory("BATTLECRUISER") {
		setControllerTypeName("Battlecruiser")

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			builder.addModule("targeting", ClosestLargeStarshipTargetingModule(it, 5000.0, null, focusRange = 200.0).apply { sticky = true })
			builder.addModule("tracking", TrackingModule(it, 5, 87.5, 35.0) { builder.suppliedModule<TargetingModule>("targeting").get().findTarget() })
			builder.addModule("combat", FrigateCombatModule(it, toggleRandomTargeting = true) { builder.suppliedModule<TrackingModule>("tracking").get().findTarget() })

			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.advancedCapitalDistanceConfiguration))
			val steering = builder.addModule("steering", CapitalSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				config = aiSteeringConfig.battlecruiserBasicSteeringConfiguration) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.CRUISE))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}

		build()
	}

	val passive_cruise = registerFactory("EXPLORER_CRUISE") {
		setControllerTypeName("Starfighter")

		val cruiseEndpoint: (AIController) -> Optional<Vec3i> = lambda@{ controller: AIController ->
			var iterations = 0
			val origin = controller.getCenter()

			val world = controller.getWorld()
			val border = world.worldBorder

			val minX = (border.center.x - border.size).toInt()
			val minZ = (border.center.z - border.size).toInt()
			val maxX = (border.center.x + border.size).toInt()
			val maxZ = (border.center.z + border.size).toInt()

			while (iterations < 15) {
				iterations++

				val endPointX = Random.nextInt(minX, maxX)
				val endPointZ = Random.nextInt(minZ, maxZ)
				val endPoint = Vec3i(endPointX, origin.y, endPointZ)

				val planets = Space.getPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

				val minDistance = planets.minOfOrNull {
					val direction = endPoint.minus(origin)

					distanceToVector(origin.toVector(), direction.toVector(), it)
				}

				// If there are planets, and the distance to any of them along the path of travel is less than 500, discard
				if (minDistance != null && minDistance <= 500.0) continue

				return@lambda Optional.of(endPoint)
			}

			Optional.empty()
		}

		setModuleBuilder {
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			// Combat handling
			val targeting = builder.addModule("targeting", HighestDamagerTargetingModule(it))
			builder.addModule("combat", DefensiveCombatModule(it, targeting::findTarget))

			// Movement handling
			val distance = builder.addModule("distance", DistancePositioningModule(it, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule("steering", TravelSteeringModule(
				it, {builder.suppliedModule<TargetingModule>("targeting").get().findTarget()},
				{builder.suppliedModule<DistancePositioningModule>("distance").get().calcDistance()},
				cruiseEndpoint.invoke(it).orNull() ?: Vec3i(0, 0, 0)) )
			builder.addModule("movement", SteeringSolverModule(it, steering,
				{builder.suppliedModule<TargetingModule>("targeting").get().findTarget()}, SteeringSolverModule.MovementType.DC))
			builder.addModule("debug", AIDebugModule(it))

			builder
		}

		build()
	}

	operator fun get(identifier: String) = presetControllers[identifier] ?: throw NoSuchElementException("Controller factory $identifier does not exist!")

	fun registerFactory(
		identifier: String,
		factory: AIControllerFactory.Builder.() -> AIControllerFactory
	): AIControllerFactory {
		val built = factory(AIControllerFactory.Builder(identifier))

		presetControllers[identifier] = built
		return built
	}
}
