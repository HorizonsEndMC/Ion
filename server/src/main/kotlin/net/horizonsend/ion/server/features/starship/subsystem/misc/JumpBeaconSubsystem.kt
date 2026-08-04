package net.horizonsend.ion.server.features.starship.subsystem.misc

import net.horizonsend.ion.server.features.multiblock.type.starship.navigationcomputer.JumpBeaconMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.AbstractMultiblockSubsystem
import org.bukkit.block.Sign

class JumpBeaconSubsystem(starship: ActiveStarship, sign: Sign, multiblock: JumpBeaconMultiblock) :
	AbstractMultiblockSubsystem<JumpBeaconMultiblock>(starship, sign, multiblock)
