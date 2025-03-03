package net.horizonsend.ion.server.features.ai

import SteeringModule
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.ai.module.combat.AimingModule
import net.horizonsend.ion.server.features.ai.module.combat.CombatModule
import net.horizonsend.ion.server.features.ai.module.combat.DefensiveCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.FrigateCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.MultiTargetFrigateCombatModule
import net.horizonsend.ion.server.features.ai.module.combat.StarfighterCombatModule
import net.horizonsend.ion.server.features.ai.module.debug.AIDebugModule
import net.horizonsend.ion.server.features.ai.module.misc.ContactsJammerModule
import net.horizonsend.ion.server.features.ai.module.misc.DifficultyModule
import net.horizonsend.ion.server.features.ai.module.misc.GravityWellModule
import net.horizonsend.ion.server.features.ai.module.misc.NavigationModule
import net.horizonsend.ion.server.features.ai.module.steering.BasicSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.CapitalSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.DistancePositioningModule
import net.horizonsend.ion.server.features.ai.module.steering.GunshipSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.StarfighterSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.SteeringSolverModule
import net.horizonsend.ion.server.features.ai.module.steering.TravelSteeringModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestLargeStarshipTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestPlayerTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestSmallStarshipTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.ClosestTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.EmityModule
import net.horizonsend.ion.server.features.ai.module.targeting.HighestDamagerTargetingModule
import net.horizonsend.ion.server.features.ai.module.targeting.TargetingModule
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceToVector
import net.horizonsend.ion.server.miscellaneous.utils.map
import net.horizonsend.ion.server.miscellaneous.utils.orNull
import java.util.Optional
import java.util.function.Supplier
import kotlin.random.Random

@Suppress("unused") // Entry points
object AIControllerFactories : IonServerComponent() {
	val presetControllers = mutableMapOf<String, AIControllerFactory>()
	val aiSteeringConfig get() = ConfigurationFiles.aiSteeringConfiguration()

	val starfighter = registerFactory("STARFIGHTER") {
		setCoreModuleBuilder { controller: AIController, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.starfighterDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, StarfighterSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)
			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }


		build()
	}

	val gunship = registerFactory("GUNSHIP") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.gunshipDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
    }

	val goonship = registerFactory("GOONSHIP") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))

			val steering = builder.addModule(SteeringModule::class, BasicSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget
			)
			)

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val jammingGunship = registerFactory("JAMMING_GUNSHIP") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))


			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.gunshipDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder.addModule(ContactsJammerModule::class, ContactsJammerModule(controller, 300.0, controller.getCoreModuleSupplier<TargetingModule>(TargetingModule::class).map { it.findTargets() }))
			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val corvette = registerFactory("CORVETTE") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.corvetteDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::corvetteBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val interdictionCorvette = registerFactory("INTERDICTION_CORVETTE") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, StarfighterCombatModule(controller, difficultyManager,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.interdictionCorvetteDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::corvetteBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }
		addUtilModule { controller -> GravityWellModule(controller, 1800.0, true, controller.getCoreModuleSupplier<TargetingModule>(TargetingModule::class).map { it.findTarget() }) }

		build()
	}

	val logisticCorvette = registerFactory("LOGISTIC_CORVETTE") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				true))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, MultiTargetFrigateCombatModule(controller, difficultyManager,toggleRandomTargeting = false, aiming) { targeting.findTargets() })

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.logisticCorvetteDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::corvetteBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}


			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val miniFrigate = registerFactory("MINI_FRIGATE") {//for 4.9k ships
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, FrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.miniFrigateDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, GunshipSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::miniFrigateBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val frigate = registerFactory("FRIGATE") {
        setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, FrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.capitalDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, CapitalSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,difficultyManager,
				targeting::findTarget,
				SteeringSolverModule.MovementType.CRUISE
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}


			builder
        }

		addUtilModule { AIDebugModule(it) }

        build()
    }

	val advancedFrigate = registerFactory("ADVANCED_FRIGATE") {
        setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, MultiTargetFrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTargets))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.advancedCapitalDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, CapitalSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,difficultyManager,
				targeting::findTarget,
				SteeringSolverModule.MovementType.CRUISE
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}


			builder
        }

		addUtilModule { AIDebugModule(it) }
		addUtilModule { controller -> GravityWellModule(controller, 1800.0, true, controller.getCoreModuleSupplier<TargetingModule>(TargetingModule::class).map { it.findTarget() }) }

        build()
	}

	val destroyer = registerFactory("DESTROYER") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, FrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.capitalDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, CapitalSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::destroyerBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,difficultyManager,
				targeting::findTarget,
				SteeringSolverModule.MovementType.CRUISE
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}


			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val advancedDestroyer = registerFactory("ADVANCED_DESTROYER") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, MultiTargetFrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTargets))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.advancedCapitalDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, CapitalSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::destroyerBasicSteeringConfiguration)
			))

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,difficultyManager,
				targeting::findTarget,
				SteeringSolverModule.MovementType.CRUISE
			)
			)

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}


			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val battlecruiser = registerFactory("BATTLECRUISER") {
		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, FrigateCombatModule(controller, difficultyManager, toggleRandomTargeting = true,aiming, targeting::findTarget))

			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.battlecruiserDistanceConfiguration))

			val steering = builder.addModule(SteeringModule::class, CapitalSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				configSupplier = Supplier(ConfigurationFiles.aiSteeringConfiguration()::battlecruiserBasicSteeringConfiguration)
			))

			builder.addModule(SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,difficultyManager,
				targeting::findTarget,
				SteeringSolverModule.MovementType.CRUISE
			))

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

		build()
	}

	val passive_cruise = registerFactory("EXPLORER_CRUISE") {
		val cruiseEndpoint: (AIController) -> Optional<Vec3i> = lambda@{ controller ->
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

				val planets = Space.getAllPlanets().filter { it.spaceWorld == world }.map { it.location.toVector() }

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

		setCoreModuleBuilder { controller, difficulty, targetAI ->
			val builder = AIControllerFactory.Builder.ModuleBuilder()

			// Combat handling
			val difficultyManager = builder.addModule(DifficultyModule::class, DifficultyModule(controller, internalDifficulty = difficulty))

			val targeting = builder.addModule(EmityModule::class, EmityModule(
				controller,
				difficultyManager,
				targetAI))

			val aiming = builder.addModule(AimingModule::class, AimingModule(controller,difficultyManager))
			builder.addModule(CombatModule::class, DefensiveCombatModule(controller ,difficultyManager,aiming, targeting::findTarget))

			// Movement handling
			val distance = builder.addModule(DistancePositioningModule::class, DistancePositioningModule(
				controller, difficultyManager,targeting::findTarget, aiSteeringConfig.starfighterDistanceConfiguration))
			val steering = builder.addModule(SteeringModule::class, TravelSteeringModule(
				controller,
				difficultyManager,
				targeting::findTarget,
				distance::calcDistance,
				cruiseEndpoint.invoke(controller).orNull() ?: Vec3i(0, 0, 0)
			)
			)

			builder.addModule(
				SteeringSolverModule::class, SteeringSolverModule(
				controller,
				steering,
				difficultyManager,
				targeting::findTarget ,
				SteeringSolverModule.MovementType.DC
			))

			if (difficultyManager.doNavigation) {
				builder.addModule(NavigationModule::class, NavigationModule(
					controller,
					targeting,
					difficultyManager
				))
			}

			builder
		}

		addUtilModule { AIDebugModule(it) }

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
