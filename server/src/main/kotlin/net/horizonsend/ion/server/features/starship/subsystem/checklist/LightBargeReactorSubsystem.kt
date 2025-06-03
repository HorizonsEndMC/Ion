package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.LightBargeReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class LightBargeReactorSubsystem (starship: ActiveStarship, sign: Sign, multiblock: LightBargeReactorMultiblock) :
	SupercapitalReactorSubsystem<LightBargeReactorMultiblock>(starship, sign, multiblock)

