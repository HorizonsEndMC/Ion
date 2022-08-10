package net.horizonsend.ion.common.database

enum class Achievement(
	val title: String,
	val description: String,
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int,
	val icon: Int?
) {
	PLANET_AERACH     ("Aerach really rocks!" , "Visit: Aerach"         ,   750,  125, 0, 201 ), // Kwazedilla
	PLANET_ARET       ("I hate sand"          , "Visit: Aret"           ,   750,  125, 0, 202 ), // Wither
	PLANET_CHANDRA    ("One small step"       , "Visit: Chandra"        ,   750,  125, 0, 203 ), // Wither
	PLANET_CHIMGARA   ("Dont get tripophobia" , "Visit: Chimgara"       ,   750,  125, 0, 204 ), // Gutin
	PLANET_DAMKOTH    ("The cheth must flow"  , "Visit: Damkoth"        ,   750,  125, 0, 205 ), // Wither
	PLANET_GAHARA     ("Ice Ice baby"         , "Visit: Gahara"         ,   750,  125, 0, 206 ), // Sciath
	PLANET_HERDOLI    ("The red planet"       , "Visit: Herdoli"        ,   750,  125, 0, 207 ), // Wither
	PLANET_ILIUS      ("Welcome to HE!"       , "Visit: Ilius"          ,   750,  125, 0, 208 ), // Vandrayk
	PLANET_ISIK       ("Ashes to Ashes.."     , "Visit: Isik"           ,   750,  125, 0, 209 ), // Kwazedilla
	PLANET_KOVFEFE    ("It's not chocolate"   , "Visit: Kovfefe"        ,   750,  125, 0, 210 ), // Peter
	PLANET_KRIO       ("Snowball fight"       , "Visit: Krio"           ,   750,  125, 0, 211 ), // Vandrayk
	PLANET_LIODA      ("Caves and Cliffs"     , "Visit: Lioda"          ,   750,  125, 0, 212 ), // Vandrayk
	PLANET_LUXITERNA  ("Supercritical"        , "Visit: Luxiterna"      ,   750,  125, 0, 213 ), // Gutin
	PLANET_QATRA      ("What happened here"   , "Visit: Qatra"          ,   750,  125, 0, 214 ), // GenBukkit
	PLANET_RUBACIEA   ("Icy-Hot"              , "Visit: Rubaciea"       ,   750,  125, 0, 215 ), // Liluzivirt
	PLANET_TURMS      ("Turms and Conditions" , "Visit: Turms"          ,   750,  125, 0, 216 ), // Kwazedilla
	PLANET_VASK       ("Ooh! A penny!"        , "Visit: Vask"           ,   750,  125, 0, 217 ), // Vandrayk
	SYSTEM_ASTERI     ("New beginnings"       , "Visit: Asteri System"  ,  2500,  400, 0, 301 ), // Gutin
	SYSTEM_ILIOS      ("Not the planet"       , "Visit: Ilios System"   ,  2500,  400, 0, 302 ), // Kwazedilla
	SYSTEM_REGULUS    ("Middle Ground"        , "Visit: Regulus System" ,  2500,  400, 0, 303 ), // Sciath
	SYSTEM_SIRIUS     ("Why So Sirius?"       , "Visit: Sirius System"  ,  2500,  400, 0, 304 ), // Sciath
	ACQUIRE_ALUMINIUM ("Pronounced Aluminium" , "Obtain Aluminium"      ,   250,  100, 0, 401 ), // Gutin
	ACQUIRE_CHETHERITE("Unleaded"             , "Obtain Chetherite"     ,   250,  100, 0, 402 ), // Gutin
	ACQUIRE_TITANIUM  ("Future's Material"    , "Obtain Titanium"       ,   250,  100, 0, 403 ), // Gutin + Peter
	ACQUIRE_URANIUM   ("Split the atom"       , "Obtain Uranium"        ,   250,  100, 0, 404 ), // Peter
	BUY_SPAWN_SHUTTLE ("Space and beyond"     , "Buy a spawn shuttle"   ,   250,  100, 0, 305 ), // Sciath + Peter
	CAPTURE_STATION   ("Bear eliminated"      , "Capture a station"     ,  5000,  750, 0, 306 ), // Liluzivirt
	COMPLETE_CARGO_RUN("Space trucking"       , "Complete a shipment"   ,   500,  125, 0, null), // Peter
	COMPLETE_TUTORIAL ("Space Cadet"          , "Complete the Tutorial" ,  1000,  250, 0, 307 ), // Wither
	CREATE_NATION     ("Galactic Power"       , "Found a nation"        ,  5000,  500, 0, 308 ), // Vandrayk
	CREATE_OUTPOST    ("Manifest Destiny"     , "Create a nation claim" ,  2500,  250, 0, 309 ), // Vandrayk
	CREATE_SETTLEMENT ("Breaking ground"      , "Found a settlement"    ,  1000,  250, 0, 310 ), // Peter
	DETECT_MULTIBLOCK ("Industrial Revolution", "Detect a multiblock"   ,   500,  125, 0, null), // Wither + Peter
	DETECT_SHIP       ("All in working order" , "Detect a starship"     ,   100,   50, 0, null), // Vandrayk
	KILL_PLAYER       ("Carried away"         , "Kill a player"         ,   250,  100, 0, 311 ), // Peter
	KILL_SHIP         ("Tango Down"           , "Shoot down a ship"     ,  1000,  250, 0, 312 ), // Vandrayk
	LEVEL_10          ("What do we do now?"   , "Reach level 10"        ,  1000,  250, 0, 313 ), // Vandrayk
	LEVEL_20          ("Where it begins"      , "Reach level 20"        ,  2000,  350, 0, 314 ), // Kwazedilla + Peter
	LEVEL_40          ("Sorry for the pain"   , "Reach level 40"        ,  5000,  500, 0, 315 ), // Sciath
	LEVEL_80          ("Overwhelming power"   , "Reach level 80"        , 10000, 1000, 0, 316 ), // Peter
	SIEGE_STATION     ("Poking the bear"      , "Participate in a siege",   500,  125, 0, 317 ), // Gutin
	USE_HYPERSPACE    ("Ludicrous Speed!"     , "Use hyperspace"        ,   250,   75, 8, 318 ); // Liluzivirt
}