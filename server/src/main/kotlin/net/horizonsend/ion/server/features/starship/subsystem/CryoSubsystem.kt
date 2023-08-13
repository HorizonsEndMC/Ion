package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.server.features.multiblock.misc.CryoPodMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import org.bukkit.block.Sign

class CryoSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CryoPodMultiblock, var pod: Cryopod) :
	AbstractMultiblockSubsystem<CryoPodMultiblock>(starship, sign, multiblock)
