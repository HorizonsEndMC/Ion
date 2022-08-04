package net.horizonsend.ion.common.database

enum class Achievement(
	val title: String,
	val description: String,
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int
) {
	PLANET_CHIMGARA   ("PLANET_CHIMGARA"       , "Visit Chimgara."                      ,   750,  125, 0),
	PLANET_CHANDRA    ("One small step"        , "Visit Chandra."                       ,   750,  125, 0), // Wither
	PLANET_DAMKOTH    ("The cheth must flow"   , "Visit Damkoth."                       ,   750,  125, 0), // Wither
	PLANET_VASK       ("PLANET_VASK"           , "Visit Vask."                          ,   750,  125, 0),
	PLANET_GAHARA     ("PLANET_GAHARA"         , "Visit Gahara."                        ,   750,  125, 0),
	PLANET_ISIK       ("PLANET_ISIK"           , "Visit Isik."                          ,   750,  125, 0),
	PLANET_KRIO       ("PLANET_KRIO"           , "Visit Krio."                          ,   750,  125, 0),
	PLANET_HERDOLI    ("The red planet"        , "Visit Herdoli."                       ,   750,  125, 0), // Wither
	PLANET_ILIUS      ("PLANET_ILIUS"          , "Visit Ilius."                         ,   750,  125, 0),
	PLANET_AERACH     ("PLANET_AERACH"         , "Visit Aerach."                        ,   750,  125, 0),
	PLANET_RUBACIEA   ("PLANET_RUBACIEA"       , "Visit Rubaciea."                      ,   750,  125, 0),
	PLANET_ARET       ("I hate sand"           , "Visit Aret."                          ,   750,  125, 0), // Wither
	PLANET_LUXITERNA  ("PLANET_LUXITERNA"      , "Visit Luxiterna."                     ,   750,  125, 0),
	PLANET_TURMS      ("PLANET_TURMS"          , "Visit Turms."                         ,   750,  125, 0),
	PLANET_LIODA      ("PLANET_LIODA"          , "Visit Lioda."                         ,   750,  125, 0),
	PLANET_QATRA      ("What happened here "   , "Visit Qatra."                         ,   750,  125, 0), // GenBukkit
	PLANET_KOVFEFE    ("It's not chocolate"    , "Visit Kovfefe."                       ,   750,  125, 0), // Peter
	SYSTEM_ASTERI     ("SYSTEM_ASTERI"         , "Visit the Asteri system."             ,  2500,  400, 0),
	SYSTEM_REGULUS    ("SYSTEM_REGULUS"        , "Visit the Regulus system."            ,  2500,  400, 0),
	SYSTEM_SIRIUS     ("SYSTEM_SIRIUS"         , "Visit the Sirius system."             ,  2500,  400, 0),
	SYSTEM_ILIOS      ("SYSTEM_ILIOS"          , "Visit the Ilios system."              ,  2500,  400, 0),
	CREATE_SETTLEMENT ("Breaking ground"       , "Found a settlement."                  ,  1000,  250, 0), // Peter
	CREATE_NATION     ("CREATE_NATION"         , "Found a nation."                      ,  5000,  500, 0),
	USE_HYPERSPACE    ("A powerful engine"     , "Use hyperspace to jump to a location.",   250,   75, 8), // Peter
	DETECT_SHIP       ("DETECT_SHIP"           , "Detect a starship."                   ,   100,   50, 0),
	KILL_SHIP         ("KILL_SHIP"             , "Shoot down a starship."               ,  1000,  250, 0),
	KILL_PLAYER       ("Don't get carried away", "Kill a player."                       ,   250,  100, 0), // Peter
	SIEGE_STATION     ("SIEGE_STATION"         , "Participate in a station siege."      ,   500,  125, 0),
	CAPTURE_STATION   ("CAPTURE_STATION"       , "Capture a station in a siege."        ,  5000,  750, 0),
	LEVEL_10          ("LEVEL_10"              , "Reach level 10."                      ,  1000,  250, 0),
	LEVEL_20          ("LEVEL_20"              , "Reach level 20."                      ,  2000,  350, 0),
	LEVEL_40          ("LEVEL_40"              , "Reach level 40."                      ,  5000,  500, 0),
	LEVEL_80          ("Overwhelming firepower", "Reach level 80."                      , 10000, 1000, 0), // Peter
	DETECT_MULTIBLOCK ("Revolutionary industry", "Detect a multiblock."                 ,   500,  125, 0), // Wither
	COMPLETE_CARGO_RUN("Space trucking"        , "Complete a cargo run."                ,   500,  125, 0), // Peter
	ACQUIRE_CHETHERITE("ACQUIRE_CHETHERITE"    , "Obtain Chetherite."                   ,   250,  100, 0),
	ACQUIRE_URANIUM   ("Split the atom"        , "Obtain Uranium."                      ,   250,  100, 0), // Peter
	ACQUIRE_ALUMINIUM ("ACQUIRE_ALUMINIUM"     , "Obtain Aluminium."                    ,   250,  100, 0),
	ACQUIRE_TITANIUM  ("ACQUIRE_TITANIUM"      , "Obtain Titanium."                     ,   250,  100, 0),
	COMPLETE_TUTORIAL ("Space Cadet"           , "Complete the Tutorial."               ,  1000,  250, 0), // Wither
	BUY_SPAWN_SHUTTLE ("BUY_SPAWN_SHUTTLE"     , "Buy a spawn shuttle from the dealer." ,   250,  100, 0)
}