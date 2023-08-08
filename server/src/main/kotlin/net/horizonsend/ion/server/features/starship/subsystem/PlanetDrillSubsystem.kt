package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.drills.DrillMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class PlanetDrillSubsystem(starship: ActiveStarship, val sign: Sign, multiblock: DrillMultiblock) :
	AbstractMultiblockSubsystem<DrillMultiblock>(starship, sign, multiblock)

