package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.drills.DrillMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class PlanetDrillSubsystem(starship: ActiveStarship, val sign: Sign, multiblock: DrillMultiblock) :
	AbstractMultiblockSubsystem<DrillMultiblock>(starship, sign, multiblock)

