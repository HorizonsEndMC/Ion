package net.starlegacy.feature.starship.subsystem

import net.horizonsend.ion.common.database.schema.Cryopod
import net.horizonsend.ion.server.features.cryopods.CryoPodMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.block.Sign

class CryoSubsystem(starship: ActiveStarship, sign: Sign, multiblock: CryoPodMultiblock, var pod: Cryopod) :
	AbstractMultiblockSubsystem<CryoPodMultiblock>(starship, sign, multiblock)
