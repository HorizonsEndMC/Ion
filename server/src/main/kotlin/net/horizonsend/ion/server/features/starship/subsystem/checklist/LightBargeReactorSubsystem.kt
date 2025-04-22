package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.LightBargeReactorMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class LightBargeReactorSubsystem (starship: ActiveStarship, sign: Sign, multiblock: LightBargeReactorMultiblock) :
        AbstractMultiblockSubsystem<LightBargeReactorMultiblock>(starship, sign, multiblock)
