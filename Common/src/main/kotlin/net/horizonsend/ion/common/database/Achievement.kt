package net.horizonsend.ion.common.database

enum class Achievement(
	val title: String,
	val description: String,
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int
) {
	PLANET_CHIMGARA   ("Dont get tripophobia"  , "Visit: Chimgara"               ,   750,  125, 0), // Gutin
	PLANET_CHANDRA    ("One small step"        , "Visit: Chandra"                ,   750,  125, 0), // Wither
	PLANET_DAMKOTH    ("The cheth must flow"   , "Visit: Damkoth"                ,   750,  125, 0), // Wither
	PLANET_VASK       ("Ooh! A penny!"         , "Visit: Vask"                   ,   750,  125, 0), // Vandrayk
	PLANET_GAHARA     ("Ice Ice baby"          , "Visit: Gahara"                 ,   750,  125, 0), // Sciath
	PLANET_ISIK       ("Ashes to Ashes.."      , "Visit: Isik"                   ,   750,  125, 0), // Kwazedilla
	PLANET_KRIO       ("Snowball fight"        , "Visit: Krio"                   ,   750,  125, 0), // Vandrayk
	PLANET_HERDOLI    ("The red planet"        , "Visit: Herdoli"                ,   750,  125, 0), // Wither
	PLANET_ILIUS      ("Welcome to HE!"        , "Visit: Ilius"                  ,   750,  125, 0), // Vandrayk
	PLANET_AERACH     ("Aerach really rocks!"  , "Visit: Aerach"                 ,   750,  125, 0), // Kwazedilla
	PLANET_RUBACIEA   ("Icy-Hot"               , "Visit: Rubaciea"               ,   750,  125, 0), // Liluzivirt
	PLANET_ARET       ("I hate sand"           , "Visit: Aret"                   ,   750,  125, 0), // Wither
	PLANET_LUXITERNA  ("Supercritical"         , "Visit: Luxiterna"              ,   750,  125, 0), // Gutin
	PLANET_TURMS      ("Turms and Conditions"  , "Visit: Turms"                  ,   750,  125, 0), // Kwazedilla
	PLANET_LIODA      ("Caves and Cliffs"      , "Visit: Lioda"                  ,   750,  125, 0), // Vandrayk
	PLANET_QATRA      ("What happened here"    , "Visit: Qatra"                  ,   750,  125, 0), // GenBukkit
	PLANET_KOVFEFE    ("It's not chocolate"    , "Visit: Kovfefe"                ,   750,  125, 0), // Peter
	SYSTEM_ASTERI     ("New beginnings"        , "Visit: Asteri System"          ,  2500,  400, 0), // Gutin
	SYSTEM_REGULUS    ("Middle Ground"         , "Visit: Regulus System"         ,  2500,  400, 0), // Sciath
	SYSTEM_SIRIUS     ("Why So Sirius?"        , "Visit: Sirius System"          ,  2500,  400, 0), // Sciath
	SYSTEM_ILIOS      ("Not the planet"        , "Visit: Ilios System"           ,  2500,  400, 0), // Kwazedilla
	CREATE_SETTLEMENT ("Breaking ground"       , "Found a settlement"            ,  1000,  250, 0), // Peter
	CREATE_NATION     ("Galactic Power"        , "Found a nation"                ,  5000,  500, 0), // Vandrayk
	CREATE_NATIONCLAIM("Manifest Destiny"      , "Create a nation claim"         ,  2500,  250, 0), // Vandrayk
	USE_HYPERSPACE    ("Ludicrous Speed!"      , "Perform a hyperspace jump"     ,   250,   75, 8), // Liluzivirt
	DETECT_SHIP       ("All in working order"  , "Detect a starship"             ,   100,   50, 0), // Vandrayk
	KILL_SHIP         ("Tango Down"            , "Shoot down a starship"         ,  1000,  250, 0), // Vandrayk
	KILL_PLAYER       ("Don't get carried away", "Kill a player"                 ,   250,  100, 0), // Peter
	SIEGE_STATION     ("Poking the bear"       , "Participate in a station siege",   500,  125, 0), // Gutin
	CAPTURE_STATION   ("Bear eliminated"       , "Capture a station in a siege"  ,  5000,  750, 0), // Liluzivirt
	LEVEL_10          ("What do we do now?"    , "Reach level 10"                ,  1000,  250, 0), // Vandrayk
	LEVEL_20          ("Where the fun begins"  , "Reach level 20"                ,  2000,  350, 0), // Kwazedilla
	LEVEL_40          ("Sorry for the pain"    , "Reach level 40"                ,  5000,  500, 0), // Sciath
	LEVEL_80          ("Overwhelming firepower", "Reach level 80"                , 10000, 1000, 0), // Peter
	DETECT_MULTIBLOCK ("Revolutionary industry", "Detect a multiblock"           ,   500,  125, 0), // Wither
	COMPLETE_CARGO_RUN("Space trucking"        , "Complete a cargo run"          ,   500,  125, 0), // Peter
	ACQUIRE_CHETHERITE("Unleaded"              , "Obtain Chetherite"             ,   250,  100, 0), // Gutin
	ACQUIRE_URANIUM   ("Split the atom"        , "Obtain Uranium"                ,   250,  100, 0), // Peter
	ACQUIRE_ALUMINIUM ("Pronounced Aluminium"  , "Obtain Aluminium"              ,   250,  100, 0), // Gutin
	ACQUIRE_TITANIUM  ("Material of the future", "Obtain Titanium"               ,   250,  100, 0), // Gutin
	COMPLETE_TUTORIAL ("Space Cadet"           , "Complete the Tutorial"         ,  1000,  250, 0), // Wither
	BUY_SPAWN_SHUTTLE ("To space and beyond"   , "Buy a spawn shuttle"           ,   250,  100, 0)  // Sciath
}