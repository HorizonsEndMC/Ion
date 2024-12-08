package net.horizonsend.ion.server.data.migrator.types

import net.horizonsend.ion.server.features.world.IonWorld
import org.bukkit.World

abstract class WorldDataMigrator(dataVersion: Int) : DataMigrator<World, IonWorld>(dataVersion)
