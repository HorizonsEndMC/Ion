package net.horizonsend.ion.server.features.starship.subsystem.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.checklist.BargeReactorMultiBlock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class BargeReactorSubsystem (starship: ActiveStarship, sign: Sign, multiblock: BargeReactorMultiBlock) :
        AbstractMultiblockSubsystem<BargeReactorMultiBlock>(starship, sign, multiblock)
