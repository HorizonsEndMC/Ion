package net.horizonsend.ion.common.database

enum class Achievement(
	val title: String,
	val description: String,
	val creditReward: Int,
	val experienceReward: Int,
	val chetheriteReward: Int
) {
	PLANET_CHIMGARA   ("PLANET_CHIMGARA"       , "Visit Chimgara."                      , 0, 0, 0),
	PLANET_CHANDRA    ("One small step"        , "Visit Chandra."                       , 0, 0, 0), // Wither
	PLANET_DAMKOTH    ("The cheth must flow"   , "Visit Damkoth."                       , 0, 0, 0), // Wither
	PLANET_VASK       ("PLANET_VASK"           , "Visit Vask."                          , 0, 0, 0),
	PLANET_GAHARA     ("PLANET_GAHARA"         , "Visit Gahara."                        , 0, 0, 0),
	PLANET_ISIK       ("PLANET_ISIK"           , "Visit Isik."                          , 0, 0, 0),
	PLANET_KRIO       ("PLANET_KRIO"           , "Visit Krio."                          , 0, 0, 0),
	PLANET_HERDOLI    ("The red planet"        , "Visit Herdoli."                       , 0, 0, 0), // Wither
	PLANET_ILIUS      ("PLANET_ILIUS"          , "Visit Ilius."                         , 0, 0, 0),
	PLANET_AERACH     ("PLANET_AERACH"         , "Visit Aerach."                        , 0, 0, 0),
	PLANET_RUBACIEA   ("PLANET_RUBACIEA"       , "Visit Rubaciea."                      , 0, 0, 0),
	PLANET_ARET       ("I hate sand"           , "Visit Aret."                          , 0, 0, 0), // Wither
	PLANET_LUXITERNA  ("PLANET_LUXITERNA"      , "Visit Luxiterna."                     , 0, 0, 0),
	PLANET_TURMS      ("PLANET_TURMS"          , "Visit Turms."                         , 0, 0, 0),
	PLANET_LIODA      ("PLANET_LIODA"          , "Visit Lioda."                         , 0, 0, 0),
	PLANET_QATRA      ("What happened here "   , "Visit Qatra."                         , 0, 0, 0), // GenBukkit
	PLANET_KOVFEFE    ("It's not chocolate"    , "Visit Kovfefe."                       , 0, 0, 0), // Peter
	SYSTEM_ASTERI     ("SYSTEM_ASTERI"         , "Visit the Asteri system."             , 0, 0, 0),
	SYSTEM_REGULUS    ("SYSTEM_REGULUS"        , "Visit the Regulus system."            , 0, 0, 0),
	SYSTEM_SIRIUS     ("SYSTEM_SIRIUS"         , "Visit the Sirius system."             , 0, 0, 0),
	SYSTEM_ILIOS      ("SYSTEM_ILIOS"          , "Visit the Ilios system."              , 0, 0, 0),
	CREATE_SETTLEMENT ("Breaking ground"       , "Found a settlement."                  , 0, 0, 0), // Peter
	CREATE_NATION     ("CREATE_NATION"         , "Found a nation."                      , 0, 0, 0),
	USE_HYPERSPACE    ("A powerful engine"     , "Use hyperspace to jump to a location.", 0, 0, 0), // Peter
	DETECT_SHIP       ("DETECT_SHIP"           , "Detect a starship."                   , 0, 0, 0),
	KILL_SHIP         ("KILL_SHIP"             , "Shoot down a starship."               , 0, 0, 0),
	KILL_PLAYER       ("Don't get carried away", "Kill a player."                       , 0, 0, 0), // Peter
	SIEGE_STATION     ("SIEGE_STATION"         , "Participate in a station siege."      , 0, 0, 0),
	CAPTURE_STATION   ("CAPTURE_STATION"       , "Capture a station in a siege."        , 0, 0, 0),
	LEVEL_10          ("LEVEL_10"              , "Reach level 10."                      , 0, 0, 0),
	LEVEL_20          ("LEVEL_20"              , "Reach level 20."                      , 0, 0, 0),
	LEVEL_40          ("LEVEL_40"              , "Reach level 40."                      , 0, 0, 0),
	LEVEL_80          ("Overwhelming firepower", "Reach level 80."                      , 0, 0, 0), // Peter
	DETECT_MULTIBLOCK ("Revolutionary industry", "Detect a multiblock."                 , 0, 0, 0), // Wither
	COMPLETE_CARGO_RUN("Space trucking"        , "Complete a cargo run."                , 0, 0, 0), // Peter
	ACQUIRE_CHETHERITE("ACQUIRE_CHETHERITE"    , "Obtain Chetherite."                   , 0, 0, 0),
	ACQUIRE_URANIUM   ("Split the atom"        , "Obtain Uranium."                      , 0, 0, 0), // Peter
	ACQUIRE_ALUMINIUM ("ACQUIRE_ALUMINIUM"     , "Obtain Aluminium."                    , 0, 0, 0),
	ACQUIRE_TITANIUM  ("ACQUIRE_TITANIUM"      , "Obtain Titanium."                     , 0, 0, 0),
	COMPLETE_TUTORIAL ("Space Cadet"           , "Complete the Tutorial."               , 0, 0, 0), // Wither
	BUY_SPAWN_SHUTTLE ("BUY_SPAWN_SHUTTLE"     , "Buy a spawn shuttle from the dealer." , 0, 0, 0)
}