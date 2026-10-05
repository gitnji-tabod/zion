package com.example.driveschool.data.db

import com.example.driveschool.data.model.*
import java.util.UUID

object DriveSchoolSeedData {

  // ==========================================
  // 1. BRANCHES (CAMEROON NATIONWIDE & ONLINE)
  // ==========================================
  val branchBamenda = BranchEntity(
    id = "branch-bamenda-01",
    name = "Bamenda Main Branch",
    city = "Bamenda (North West)",
    address = "Commercial Avenue, Opposite Sacred Heart Cathedral",
    phone = "+237 677 123 456",
    isVirtual = false,
    managerId = "user-mgr-01"
  )

  val branchDouala = BranchEntity(
    id = "branch-douala-02",
    name = "Douala Akwa Branch",
    city = "Douala (Littoral)",
    address = "Boulevard de la Liberté, Immeuble Akwa Palace",
    phone = "+237 699 234 567",
    isVirtual = false,
    managerId = "user-mgr-02"
  )

  val branchYaounde = BranchEntity(
    id = "branch-yaounde-03",
    name = "Yaoundé Bastos Branch",
    city = "Yaoundé (Centre)",
    address = "Rue Joseph Mballa, Bastos Carrefour",
    phone = "+237 670 345 678",
    isVirtual = false,
    managerId = "user-mgr-03"
  )

  val branchBafoussam = BranchEntity(
    id = "branch-bafoussam-04",
    name = "Bafoussam Commercial Branch",
    city = "Bafoussam (West)",
    address = "Carrefour Auberge, Centre Commercial",
    phone = "+237 675 789 012",
    isVirtual = false,
    managerId = "user-mgr-04"
  )

  val branchGaroua = BranchEntity(
    id = "branch-garoua-05",
    name = "Garoua Central Branch",
    city = "Garoua (North)",
    address = "Boulevard de la République, Face Gouvernance",
    phone = "+237 691 678 901",
    isVirtual = false,
    managerId = "user-mgr-05"
  )

  val branchOnline = BranchEntity(
    id = "branch-online-00",
    name = "DriveSchool Online Global Campus",
    city = "Global Cloud (Virtual)",
    address = "https://driveschool.cm/online",
    phone = "+237 680 000 111",
    isVirtual = true,
    managerId = "user-admin-01"
  )

  val branches = listOf(
    branchBamenda,
    branchDouala,
    branchYaounde,
    branchBafoussam,
    branchGaroua,
    branchOnline
  )

  // ==========================================
  // 2. USERS & STAFF PERSONAS
  // ==========================================
  val userSuperAdmin = UserEntity(
    id = "user-admin-01",
    name = "Dr. Ernest Tabi (Owner & MD)",
    email = "owner@driveschool.cm",
    phone = "+237 677 000 001",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.SUPER_ADMIN,
    branchId = null
  )

  val userBranchManager = UserEntity(
    id = "user-mgr-01",
    name = "Beatrice Fongang (Manager Bamenda)",
    email = "manager.bamenda@driveschool.cm",
    phone = "+237 677 222 333",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.BRANCH_MANAGER,
    branchId = branchBamenda.id
  )

  val userManagerDouala = UserEntity(
    id = "user-mgr-02",
    name = "Jean-Paul Mbarga (Manager Douala)",
    email = "manager.douala@driveschool.cm",
    phone = "+237 699 111 222",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.BRANCH_MANAGER,
    branchId = branchDouala.id
  )

  val userSecretary = UserEntity(
    id = "user-sec-01",
    name = "Che Roland (Secretary Bamenda)",
    email = "secretary.bamenda@driveschool.cm",
    phone = "+237 675 444 555",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.SECRETARY,
    branchId = branchBamenda.id
  )

  val userSecretaryDouala = UserEntity(
    id = "user-sec-02",
    name = "Chantal Eto'o (Secretary Douala)",
    email = "secretary.douala@driveschool.cm",
    phone = "+237 699 333 444",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.SECRETARY,
    branchId = branchDouala.id
  )

  val userInstructor = UserEntity(
    id = "user-inst-01",
    name = "Peter Ngu (Chief Instructor)",
    email = "instructor.ngu@driveschool.cm",
    phone = "+237 671 888 999",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.INSTRUCTOR,
    branchId = branchBamenda.id
  )

  val userInstructorDouala = UserEntity(
    id = "user-inst-02",
    name = "Emmanuel Fotso (Heavy Vehicle Instructor)",
    email = "instructor.fotso@driveschool.cm",
    phone = "+237 694 555 777",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.INSTRUCTOR,
    branchId = branchDouala.id
  )

  val userStudentOnsite = UserEntity(
    id = "user-stud-onsite-01",
    name = "Brenda Bih (Onsite Student)",
    email = "brenda.bih@gmail.com",
    phone = "+237 677 889 900",
    countryCode = "CM", // Cameroon -> Onsite capable
    preferredLocale = "en",
    role = UserRole.STUDENT,
    branchId = branchBamenda.id
  )

  val userStudentOnline = UserEntity(
    id = "user-stud-online-02",
    name = "Thomas Leroy (International)",
    email = "thomas.leroy@yahoo.fr",
    phone = "+33 6 12 34 56 78",
    countryCode = "FR", // France -> Online virtual branch
    preferredLocale = "fr",
    role = UserRole.STUDENT,
    branchId = branchOnline.id
  )

  val userStudentLucas = UserEntity(
    id = "user-stud-onsite-03",
    name = "Lucas Mbome (Heavy Truck Student)",
    email = "lucas.mbome@gmail.com",
    phone = "+237 699 556 677",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.STUDENT,
    branchId = branchDouala.id
  )

  // Yaoundé Bastos Branch Staff & Student
  val userManagerYaounde = UserEntity(
    id = "user-mgr-03",
    name = "Gervais Ondoa (Manager Yaoundé)",
    email = "manager.yaounde@driveschool.cm",
    phone = "+237 670 111 333",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.BRANCH_MANAGER,
    branchId = branchYaounde.id
  )

  val userSecretaryYaounde = UserEntity(
    id = "user-sec-03",
    name = "Marie-Claire Ngo (Secretary Yaoundé)",
    email = "secretary.yaounde@driveschool.cm",
    phone = "+237 670 222 444",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.SECRETARY,
    branchId = branchYaounde.id
  )

  val userStudentYaounde = UserEntity(
    id = "user-stud-yaounde-04",
    name = "Alain Mbida (Student Yaoundé)",
    email = "alain.mbida@gmail.com",
    phone = "+237 677 334 455",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.STUDENT,
    branchId = branchYaounde.id
  )

  // Bafoussam Commercial Branch Staff & Student
  val userManagerBafoussam = UserEntity(
    id = "user-mgr-04",
    name = "Sylvain Kamga (Manager Bafoussam)",
    email = "manager.bafoussam@driveschool.cm",
    phone = "+237 675 333 555",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.BRANCH_MANAGER,
    branchId = branchBafoussam.id
  )

  val userSecretaryBafoussam = UserEntity(
    id = "user-sec-04",
    name = "Nadine Tchuente (Secretary Bafoussam)",
    email = "secretary.bafoussam@driveschool.cm",
    phone = "+237 675 444 666",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.SECRETARY,
    branchId = branchBafoussam.id
  )

  val userStudentBafoussam = UserEntity(
    id = "user-stud-bafoussam-05",
    name = "Patrick Fotsing (Student Bafoussam)",
    email = "patrick.fotsing@gmail.com",
    phone = "+237 675 889 911",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.STUDENT,
    branchId = branchBafoussam.id
  )

  // Garoua Central Branch Staff & Student
  val userManagerGaroua = UserEntity(
    id = "user-mgr-05",
    name = "Hamadou Bello (Manager Garoua)",
    email = "manager.garoua@driveschool.cm",
    phone = "+237 691 222 444",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.BRANCH_MANAGER,
    branchId = branchGaroua.id
  )

  val userSecretaryGaroua = UserEntity(
    id = "user-sec-05",
    name = "Aissatou Ousmanou (Secretary Garoua)",
    email = "secretary.garoua@driveschool.cm",
    phone = "+237 691 333 555",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.SECRETARY,
    branchId = branchGaroua.id
  )

  val userStudentGaroua = UserEntity(
    id = "user-stud-garoua-06",
    name = "Moussa Daouda (Student Garoua)",
    email = "moussa.daouda@gmail.com",
    phone = "+237 691 778 899",
    countryCode = "CM",
    preferredLocale = "fr",
    role = UserRole.STUDENT,
    branchId = branchGaroua.id
  )

  val userWalkinStudent = UserEntity(
    id = "user-walkin-03",
    name = "Solomon Fon (Walk-in Student)",
    email = "solomon.fon@yahoo.com",
    phone = "+237 678 112 233",
    countryCode = "CM",
    preferredLocale = "en",
    role = UserRole.STUDENT,
    branchId = branchBamenda.id
  )

  val users = listOf(
    userSuperAdmin,
    userBranchManager,
    userManagerDouala,
    userManagerYaounde,
    userManagerBafoussam,
    userManagerGaroua,
    userSecretary,
    userSecretaryDouala,
    userSecretaryYaounde,
    userSecretaryBafoussam,
    userSecretaryGaroua,
    userInstructor,
    userInstructorDouala,
    userStudentOnsite,
    userStudentOnline,
    userStudentLucas,
    userStudentYaounde,
    userStudentBafoussam,
    userStudentGaroua,
    userWalkinStudent
  )

  // ==========================================
  // 3. CAMEROON STANDARD LICENCE CATEGORIES (A–G)
  // ==========================================
  val courseCatA = CourseEntity(
    id = "course-cat-a",
    title = "Category A - Motorcycle & Scooter",
    titleFr = "Catégorie A - Motocyclettes & Scooters",
    licenseCategory = LicenseCategory.A,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 4,
    description = "Two-wheel and three-wheel motorized vehicles training, road safety, defensive riding and maneuvering.",
    descriptionFr = "Permis deux-roues et tricycles, sécurité routière, maîtrise de trajectoire et freinage d'urgence."
  )

  val courseCatB = CourseEntity(
    id = "course-cat-b",
    title = "Category B - Light Motor Vehicle",
    titleFr = "Catégorie B - Véhicule Léger",
    licenseCategory = LicenseCategory.B,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 8,
    description = "Full driving course for private passenger cars (up to 3,500 kg). Theory + 20 practical dual-control driving hours.",
    descriptionFr = "Formation complète au permis automobile (jusqu'à 3,5t). Théorie code de la route + 20h de conduite pratique."
  )

  val courseCatC = CourseEntity(
    id = "course-cat-c",
    title = "Category C - Heavy Commercial Truck",
    titleFr = "Catégorie C - Poids Lourds (> 3.5t)",
    licenseCategory = LicenseCategory.C,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 10,
    description = "Professional goods transport vehicles over 3,500 kg, pneumatic braking systems, load distribution, highway safety.",
    descriptionFr = "Formation chauffeur poids lourd transport de marchandises, freinage pneumatique, arrimage et réglementation CEMAC."
  )

  val courseCatD = CourseEntity(
    id = "course-cat-d",
    title = "Category D - Passenger Transport Bus",
    titleFr = "Catégorie D - Transport en Commun",
    licenseCategory = LicenseCategory.D,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 10,
    description = "Public passenger transport vehicles over 9 seats, passenger safety protocol, first aid and urban bus driving.",
    descriptionFr = "Permis transport public de voyageurs (> 9 places), sécurité des passagers, premiers secours et conduite urbaine."
  )

  val courseCatE = CourseEntity(
    id = "course-cat-e",
    title = "Category E - Articulated & Heavy Trailers",
    titleFr = "Catégorie E - Remorques & Semi-Remorques",
    licenseCategory = LicenseCategory.E,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 12,
    description = "Articulated vehicle combinations with trailers exceeding 750 kg. Reversing, hitching and mountain road safety.",
    descriptionFr = "Ensembles de véhicules attelés d'une remorque > 750 kg. Manœuvres complexes, attelage et routes de montagne."
  )

  val courseCatF = CourseEntity(
    id = "course-cat-f",
    title = "Category F - Specially Adapted Vehicles",
    titleFr = "Catégorie F - Véhicules Aménagés Mobilité Réduite",
    licenseCategory = LicenseCategory.F,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 8,
    description = "Specially adapted vehicles for drivers with reduced mobility or physical handicaps, ergonomic driving training.",
    descriptionFr = "Conduite de véhicules adaptés aux personnes à mobilité réduite ou en situation de handicap."
  )

  val courseCatG = CourseEntity(
    id = "course-cat-g",
    title = "Category G - Agricultural & Construction Machinery",
    titleFr = "Catégorie G - Engins Agricoles & Travaux Publics",
    licenseCategory = LicenseCategory.G,
    delivery = DeliveryType.COMBINED,
    durationWeeks = 12,
    description = "Agricultural tractors, excavators, bulldozers and heavy industrial machinery handling and safety certification.",
    descriptionFr = "Tracteurs agricoles, engins de terrassement et chantiers, sécurité industrielle et manœuvre d'engins spéciaux."
  )

  val courses = listOf(
    courseCatA,
    courseCatB,
    courseCatC,
    courseCatD,
    courseCatE,
    courseCatF,
    courseCatG
  )

  // ==========================================
  // 4. FEE SCHEDULES
  // ==========================================
  val feeSchedules = listOf(
    FeeScheduleEntity("fee-cat-a", courseCatA.id, 65000.0, 25000.0, "XAF"),
    FeeScheduleEntity("fee-cat-b", courseCatB.id, 125000.0, 45000.0, "XAF"),
    FeeScheduleEntity("fee-cat-c", courseCatC.id, 185000.0, 60000.0, "XAF"),
    FeeScheduleEntity("fee-cat-d", courseCatD.id, 210000.0, 70000.0, "XAF"),
    FeeScheduleEntity("fee-cat-e", courseCatE.id, 250000.0, 80000.0, "XAF"),
    FeeScheduleEntity("fee-cat-f", courseCatF.id, 110000.0, 35000.0, "XAF"),
    FeeScheduleEntity("fee-cat-g", courseCatG.id, 280000.0, 90000.0, "XAF")
  )

  // ==========================================
  // 5. EXTENDED LMS CURRICULUM
  // ==========================================
  val lessons = listOf(
    // Free Onboarding Lesson 1
    LessonEntity(
      id = "lesson-01",
      courseId = courseCatB.id,
      moduleTitle = "Module 1: General Road Fundamentals",
      moduleTitleFr = "Module 1: Fondamentaux du Code de la Route",
      orderIndex = 1,
      title = "Cameroon Road Safety Code & Traffic Signs",
      titleFr = "Code de la Route Camerounais et Signalisation",
      type = LessonType.VIDEO,
      isOnboarding = true,
      durationMinutes = 15,
      videoUrl = "https://cdn.driveschool.cm/stream/module1-intro.mp4",
      contentBodyEn = """
        Welcome to DriveSchool Cameroon!
        In this introductory lesson, you will master the fundamental traffic sign categories recognized under the Cameroon Highway Code:
        1. Danger Signs (Triangular with red border): Signal curves, road narrowing, and pedestrian crossings.
        2. Regulatory Signs (Circular): Mandatory actions (blue circle) and prohibitions (white circle with red border).
        3. Priority Signs: Stop octagonal sign, Give Way upside-down triangle, and Main Road diamond.
        4. Information Signs (Rectangular): Directions, distances, parking, and public facilities.
        Remember: Defensive driving protects yourself and all other road users across Cameroon highways.
      """.trimIndent(),
      contentBodyFr = """
        Bienvenue à DriveSchool Cameroun !
        Dans cette leçon d'introduction, découvrez les catégories fondamentales de panneaux routiers :
        1. Panneaux de danger (Triangulaires à bord rouge).
        2. Panneaux d'obligation (Cercle bleu) et d'interdiction (Cercle blanc à bord rouge).
        3. Panneaux de priorité (Stop octogonal, Cédez le passage, Route prioritaire).
        4. Panneaux d'indication (Rectangulaires).
        La conduite préventive est la clé de la sécurité sur les routes camerounaises.
      """.trimIndent()
    ),
    // Free Onboarding Lesson 2
    LessonEntity(
      id = "lesson-02",
      courseId = courseCatB.id,
      moduleTitle = "Module 1: General Road Fundamentals",
      moduleTitleFr = "Module 1: Fondamentaux du Code de la Route",
      orderIndex = 2,
      title = "Cockpit Drill & Vehicle Instrumentation",
      titleFr = "Installation au Poste de Conduite et Tableau de Bord",
      type = LessonType.DOCUMENT,
      isOnboarding = true,
      durationMinutes = 20,
      contentBodyEn = """
        The DSSSM Cockpit Drill:
        • Doors: Verify all doors and boot are securely latched before engine start.
        • Seat: Adjust cushion distance so knees maintain a 120-degree bend with clutch fully depressed.
        • Steering wheel: Grip position at 9 and 3 o'clock, with wrist resting naturally on top of wheel.
        • Seatbelt: Fasten across collarbone and pelvis snugly. Mandatory for driver and passengers.
        • Mirrors: Rear-view mirror centered on rear windscreen; side mirrors showing just a sliver of car body.
        
        Dashboard warning lights:
        - Red: Immediate danger (oil pressure, brake fluid, battery charge). Stop safely immediately.
        - Amber/Yellow: Advisory caution (check engine, ABS, low fuel).
        - Green/Blue: Active indicators (headlights, high beams, indicators).
      """.trimIndent(),
      contentBodyFr = """
        L'installation méthodique au poste de conduite (Règle des 5 réglages) :
        • Portières : Vérifier le verrouillage complet.
        • Siège : Distance pédales, hauteur d'assise et inclinaison du dossier.
        • Volant : Position des mains à 9h15 ou 10h10.
        • Ceinture de sécurité : Bouclage obligatoire pour tous les passagers.
        • Rétroviseurs : Intérieur et extérieurs gauche/droite.
      """.trimIndent()
    ),
    // Locked Lesson 3 (Payment Gate Active)
    LessonEntity(
      id = "lesson-03",
      courseId = courseCatB.id,
      moduleTitle = "Module 2: Road Rules & Priority",
      moduleTitleFr = "Module 2: Règles de Priorité et Circulation",
      orderIndex = 3,
      title = "Roundabouts, Intersections & Right-of-Way in Cameroon",
      titleFr = "Priorité à Droite, Ronds-points et Carrefours",
      type = LessonType.VIDEO,
      isOnboarding = false,
      durationMinutes = 25,
      videoUrl = "https://cdn.driveschool.cm/stream/module2-priority.mp4",
      contentBodyEn = """
        Navigating Cameroon Junctions & Roundabouts:
        • Standard Rule: Priority to the right applies in the absence of road signs or traffic lights.
        • Modern Roundabouts (Giratoires): Vehicles already circulating inside have absolute right-of-way over entering traffic.
        • Gendarmerie & Police Direction: Traffic officer arm signals override all signs and traffic signals.
        • Emergency Vehicles: Give way immediately to ambulances, fire trucks, and police escorts displaying sirens and flashing beacons.
      """.trimIndent(),
      contentBodyFr = """
        Règles d'intersection au Cameroun :
        • Priorité à droite en l'absence de signalisation.
        • Carrefours à sens giratoire : priorité aux véhicules déjà engagés dans l'anneau.
        • Injonctions des agents de police et de gendarmerie prévalent sur toute signalisation.
      """.trimIndent()
    ),
    // Locked Lesson 4
    LessonEntity(
      id = "lesson-04",
      courseId = courseCatB.id,
      moduleTitle = "Module 2: Road Rules & Priority",
      moduleTitleFr = "Module 2: Règles de Priorité et Circulation",
      orderIndex = 4,
      title = "Speed Limits, CEMAC Regulations & Overtaking",
      titleFr = "Vitesses Limites, Dépassement et Normes CEMAC",
      type = LessonType.DOCUMENT,
      isOnboarding = false,
      durationMinutes = 20,
      contentBodyEn = """
        Speed Regulations in Cameroon:
        • Urban agglomerations (Douala, Yaoundé, Bamenda): Maximum 60 km/h (reduced to 30 km/h in school and hospital zones).
        • Open National Highways (RN3, RN1): Maximum 110 km/h for light passenger vehicles, 80 km/h for heavy transport.
        • Rain and Heavy Downpours: Speed must be reduced by at least 20 km/h; maintain triple following distance due to aquaplaning risks.
        • Prohibited Overtaking: Solid continuous white line, curves with restricted visibility, hills, intersections, and railway crossings.
      """.trimIndent(),
      contentBodyFr = """
        Limitations de vitesse au Cameroun :
        • En agglomération : 60 km/h max (30 km/h zones écoles et hôpitaux).
        • Hors agglomération / Routes nationales : 110 km/h pour véhicules légers, 80 km/h pour poids lourds.
        • En cas de forte pluie : réduire impérativement la vitesse de 20 km/h minimum.
      """.trimIndent()
    ),
    // Locked Lesson 5: Defensive Driving
    LessonEntity(
      id = "lesson-05",
      courseId = courseCatB.id,
      moduleTitle = "Module 3: Defensive Driving & Road Safety",
      moduleTitleFr = "Module 3: Conduite Défensive & Sécurité",
      orderIndex = 5,
      title = "Defensive Driving, Night Driving & Adverse Conditions",
      titleFr = "Conduite Défensive, Conduite de Nuit et Intempéries",
      type = LessonType.DOCUMENT,
      isOnboarding = false,
      durationMinutes = 25,
      contentBodyEn = """
        Core Principles of Defensive Driving:
        1. Anticipation: Scan 12-15 seconds ahead along the roadway. Watch for moto-taxis (benskins), pedestrians, and potholes.
        2. Space Cushion: Maintain at least a 3-second gap behind preceding vehicles in dry weather, and 5 seconds during tropical downpours.
        3. Night Driving: Headlights must be switched from high beam to dipped beam when approaching oncoming vehicles within 150 metres.
        4. Braking in Emergencies: On ABS-equipped vehicles, press brake firmly without pumping. Steer while braking.
      """.trimIndent(),
      contentBodyFr = """
        Principes clés de la conduite défensive :
        1. Anticipation : Regard porté au loin (12 à 15 secondes d'avance). Attention accrue aux motos-taxis et piétons.
        2. Distance de sécurité : Règle des 3 secondes par temps sec, 5 secondes sous fortes pluies tropicales.
        3. Conduite nocturne : Passer en feux de croisement dès 150 mètres en croisant un véhicule venant en sens inverse.
      """.trimIndent()
    ),
    // Locked Lesson 6: Comprehensive Practice Quiz
    LessonEntity(
      id = "lesson-06",
      courseId = courseCatB.id,
      moduleTitle = "Module 4: Official Theory Examination Prep",
      moduleTitleFr = "Module 4: Évaluation et Examen Blanc",
      orderIndex = 6,
      title = "Official Highway Code Practice Quiz",
      titleFr = "QCM Officiel de Préparation à l'Examen",
      type = LessonType.QUIZ,
      isOnboarding = false,
      durationMinutes = 20,
      quizQuestionsJson = """[
        {"q":"When approaching an unmarked 4-way intersection in Cameroon, which vehicle has right of way?","options":["Vehicle driving fastest","Vehicle arriving from your right","Vehicle on the wider road","First vehicle to honk"],"correct":1,"exp":"Under general highway code, priority to the right applies when no signs, lights or police officers are present."},
        {"q":"What is the legal speed limit in urban zones (cities/towns) in Cameroon?","options":["80 km/h","60 km/h","50 km/h","100 km/h"],"correct":1,"exp":"The statutory speed limit in urban agglomerations in Cameroon is 60 km/h unless otherwise posted."},
        {"q":"What does a triangular road sign with a red border indicate?","options":["Mandatory action","Prohibition","Hazard / Danger ahead","Informational tip"],"correct":2,"exp":"Triangular signs with red borders signify danger or roadway hazard ahead."},
        {"q":"When is overtaking strictly forbidden?","options":["Across a solid white continuous line","In broad daylight on straight road","When using your indicators","Inside designated passing lanes"],"correct":0,"exp":"Crossing or straddling a continuous solid white line is strictly prohibited under traffic law."},
        {"q":"What does a circular blue sign with a white directional arrow represent?","options":["Recommendation","Mandatory obligation in specified direction","No entry","Rest stop"],"correct":1,"exp":"Blue circular signs indicate mandatory requirements that drivers must obey."}
      ]"""
    )
  )

  // ==========================================
  // 6. REALISTIC FLEET OF VEHICLES
  // ==========================================
  val vehicles = listOf(
    VehicleEntity(
      id = "veh-01",
      branchId = branchBamenda.id,
      plateNo = "NW-241-AA",
      makeModel = "Toyota Yaris (Dual Controls Manual)",
      odometer = 45200,
      nextServiceKm = 46000,
      nextServiceDate = System.currentTimeMillis() + 25L * 24 * 3600 * 1000,
      status = VehicleStatus.ACTIVE
    ),
    VehicleEntity(
      id = "veh-02",
      branchId = branchBamenda.id,
      plateNo = "NW-819-BC",
      makeModel = "Hyundai i10 (Dual Controls Auto)",
      odometer = 38950,
      nextServiceKm = 39000, // Due in 50 km! Overdue check BR-08
      nextServiceDate = System.currentTimeMillis() + 3L * 24 * 3600 * 1000,
      status = VehicleStatus.SERVICE_DUE
    ),
    VehicleEntity(
      id = "veh-03",
      branchId = branchDouala.id,
      plateNo = "LT-442-DD",
      makeModel = "Isuzu Elf Heavy Truck (Cat C)",
      odometer = 82100,
      nextServiceKm = 85000,
      nextServiceDate = System.currentTimeMillis() + 45L * 24 * 3600 * 1000,
      status = VehicleStatus.ACTIVE
    ),
    VehicleEntity(
      id = "veh-04",
      branchId = branchDouala.id,
      plateNo = "LT-902-EE",
      makeModel = "Toyota Coaster Passenger Bus (Cat D)",
      odometer = 64300,
      nextServiceKm = 68000,
      nextServiceDate = System.currentTimeMillis() + 35L * 24 * 3600 * 1000,
      status = VehicleStatus.ACTIVE
    ),
    VehicleEntity(
      id = "veh-05",
      branchId = branchYaounde.id,
      plateNo = "CE-108-FF",
      makeModel = "Peugeot 208 (Dual Controls Manual)",
      odometer = 29800,
      nextServiceKm = 32000,
      nextServiceDate = System.currentTimeMillis() + 60L * 24 * 3600 * 1000,
      status = VehicleStatus.ACTIVE
    ),
    VehicleEntity(
      id = "veh-06",
      branchId = branchGaroua.id,
      plateNo = "OU-553-GG",
      makeModel = "Suzuki Hayate 125cc Training Bike (Cat A)",
      odometer = 12400,
      nextServiceKm = 14000,
      nextServiceDate = System.currentTimeMillis() + 50L * 24 * 3600 * 1000,
      status = VehicleStatus.ACTIVE
    )
  )

  // ==========================================
  // 7. ENROLLMENTS ACROSS MODES & STATUSES
  // ==========================================
  val enrollmentOnsiteBrenda = EnrollmentEntity(
    id = "enr-brenda-01",
    studentId = userStudentOnsite.id,
    courseId = courseCatB.id,
    branchId = branchBamenda.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.ACTIVE,
    negotiatedAmount = 115000.0,
    negotiatedBy = userSecretary.id,
    approvedBy = userBranchManager.id,
    discountPercent = 8.0,
    isDiscountApproved = true,
    termEndsAt = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
  )

  val enrollmentOnlineThomas = EnrollmentEntity(
    id = "enr-thomas-02",
    studentId = userStudentOnline.id,
    courseId = courseCatB.id,
    branchId = branchOnline.id,
    mode = EnrollmentMode.ONLINE,
    status = EnrollmentStatus.ONBOARDING, // Free onboarding active! Payment gate enforced
    negotiatedAmount = null, // BR-03 Standard
    negotiatedBy = null,
    approvedBy = null,
    discountPercent = 0.0,
    isDiscountApproved = false,
    termEndsAt = System.currentTimeMillis() + 90L * 24 * 3600 * 1000
  )

  val enrollmentPendingDiscount = EnrollmentEntity(
    id = "enr-walkin-pending",
    studentId = "user-walkin-03",
    courseId = courseCatB.id,
    branchId = branchBamenda.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.AWAITING_PAYMENT,
    negotiatedAmount = 95000.0, // 24% discount -> requires Branch Manager approval
    negotiatedBy = userSecretary.id,
    approvedBy = null,
    discountPercent = 24.0,
    isDiscountApproved = false,
    termEndsAt = System.currentTimeMillis() + 90L * 24 * 3600 * 1000
  )

  val enrollmentLucasTruck = EnrollmentEntity(
    id = "enr-lucas-04",
    studentId = userStudentLucas.id,
    courseId = courseCatC.id,
    branchId = branchDouala.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.ACTIVE,
    negotiatedAmount = 185000.0,
    negotiatedBy = userSecretaryDouala.id,
    approvedBy = userManagerDouala.id,
    discountPercent = 0.0,
    isDiscountApproved = true,
    termEndsAt = System.currentTimeMillis() + 75L * 24 * 3600 * 1000
  )

  val enrollmentYaounde = EnrollmentEntity(
    id = "enr-yaounde-05",
    studentId = userStudentYaounde.id,
    courseId = courseCatB.id,
    branchId = branchYaounde.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.ACTIVE,
    negotiatedAmount = 125000.0,
    negotiatedBy = userSecretaryYaounde.id,
    approvedBy = userManagerYaounde.id,
    discountPercent = 0.0,
    isDiscountApproved = true,
    termEndsAt = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
  )

  val enrollmentBafoussam = EnrollmentEntity(
    id = "enr-bafoussam-06",
    studentId = userStudentBafoussam.id,
    courseId = courseCatB.id,
    branchId = branchBafoussam.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.ACTIVE,
    negotiatedAmount = 120000.0,
    negotiatedBy = userSecretaryBafoussam.id,
    approvedBy = userManagerBafoussam.id,
    discountPercent = 4.0,
    isDiscountApproved = true,
    termEndsAt = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
  )

  val enrollmentGaroua = EnrollmentEntity(
    id = "enr-garoua-07",
    studentId = userStudentGaroua.id,
    courseId = courseCatA.id,
    branchId = branchGaroua.id,
    mode = EnrollmentMode.ONSITE,
    status = EnrollmentStatus.ACTIVE,
    negotiatedAmount = 60000.0,
    negotiatedBy = userSecretaryGaroua.id,
    approvedBy = userManagerGaroua.id,
    discountPercent = 0.0,
    isDiscountApproved = true,
    termEndsAt = System.currentTimeMillis() + 45L * 24 * 3600 * 1000
  )

  // ==========================================
  // 8. EXAM SESSIONS & 5-STAGE CANDIDATE CHAIN
  // ==========================================
  val examSessionBamenda = ExamSessionEntity(
    id = "exam-session-oct2026",
    sessionName = "National Driving Exam - Bamenda Centre Session A",
    branchId = branchBamenda.id,
    scheduledDate = System.currentTimeMillis() + 14L * 24 * 3600 * 1000,
    category = LicenseCategory.B,
    type = DeliveryType.COMBINED
  )

  val examSessionDouala = ExamSessionEntity(
    id = "exam-session-nov2026",
    sessionName = "National Heavy Goods Exam - Douala Maritime Centre",
    branchId = branchDouala.id,
    scheduledDate = System.currentTimeMillis() + 28L * 24 * 3600 * 1000,
    category = LicenseCategory.C,
    type = DeliveryType.COMBINED
  )

  // Candidate status stages (FR-21 & BR-06)
  val candidate1 = ExamCandidateEntity(
    id = "cand-01",
    examSessionId = examSessionBamenda.id,
    enrollmentId = "enr-sample-01",
    studentId = "user-student-c1",
    status = CandidateStatus.RECOMMENDED, // Stage 1: Recommended by instructor
    recommendedBy = userInstructor.id,
    approvedBy = null,
    appliedBy = null,
    score = null
  )

  val candidate2 = ExamCandidateEntity(
    id = "cand-02",
    examSessionId = examSessionBamenda.id,
    enrollmentId = "enr-sample-02",
    studentId = "user-student-c2",
    status = CandidateStatus.APPROVED, // Stage 2: Approved by Branch Manager
    recommendedBy = userInstructor.id,
    approvedBy = userBranchManager.id,
    appliedBy = null,
    score = null
  )

  val candidate3 = ExamCandidateEntity(
    id = "cand-03",
    examSessionId = examSessionBamenda.id,
    enrollmentId = "enr-sample-03",
    studentId = "user-student-c3",
    status = CandidateStatus.APPLIED, // Stage 3: Applied by Secretary
    recommendedBy = userInstructor.id,
    approvedBy = userBranchManager.id,
    appliedBy = userSecretary.id,
    score = null
  )

  val candidatePassedBrenda = ExamCandidateEntity(
    id = "cand-brenda",
    examSessionId = examSessionBamenda.id,
    enrollmentId = enrollmentOnsiteBrenda.id,
    studentId = userStudentOnsite.id,
    status = CandidateStatus.PASSED, // Stage 5: Passed with 94%
    recommendedBy = userInstructor.id,
    approvedBy = userBranchManager.id,
    appliedBy = userSecretary.id,
    score = 94.0,
    resultEnteredAt = System.currentTimeMillis() - 2L * 24 * 3600 * 1000
  )

  // ==========================================
  // 9. VERIFIABLE CERTIFICATES (QR & UUID)
  // ==========================================
  val certificateBrenda = CertificateEntity(
    id = "cert-brenda-01",
    enrollmentId = enrollmentOnsiteBrenda.id,
    studentId = userStudentOnsite.id,
    studentName = userStudentOnsite.name,
    verificationUuid = "d9b4f2a1-7c3e-4b28-98e1-5f60bca43210",
    type = CertificateType.FULL,
    category = LicenseCategory.B,
    branchName = branchBamenda.name,
    issuedAt = System.currentTimeMillis() - 2L * 24 * 3600 * 1000,
    qrPayload = "https://driveschool.cm/verify/d9b4f2a1-7c3e-4b28-98e1-5f60bca43210"
  )

  // ==========================================
  // 10. INSURANCE POLICIES & EXPIRY PIPELINE
  // ==========================================
  val insurancePolicies = listOf(
    InsurancePolicyEntity(
      id = "ins-pol-01",
      holderId = userStudentOnsite.id,
      holderName = "Brenda Bih",
      tariffName = "Third-Party Liability + Passenger Cover",
      tariffNameFr = "Responsabilité Civile + Protection Passagers",
      vehiclePlate = "NW-241-AA",
      issuedBy = userSecretary.id,
      startsAt = System.currentTimeMillis() - 355L * 24 * 3600 * 1000,
      expiresAt = System.currentTimeMillis() + 6L * 24 * 3600 * 1000, // Due in 6 days (triggers 7-day alert)
      premiumAmount = 45000.0,
      status = "EXPIRING_SOON"
    ),
    InsurancePolicyEntity(
      id = "ins-pol-02",
      holderId = userStudentLucas.id,
      holderName = "Lucas Mbome",
      tariffName = "Comprehensive Auto All-Risks",
      tariffNameFr = "Tous Risques Automobile Intégral",
      vehiclePlate = "LT-442-DD",
      issuedBy = userSecretaryDouala.id,
      startsAt = System.currentTimeMillis() - 335L * 24 * 3600 * 1000,
      expiresAt = System.currentTimeMillis() + 28L * 24 * 3600 * 1000, // Due in 28 days (triggers 30-day alert)
      premiumAmount = 120000.0,
      status = "ACTIVE"
    ),
    InsurancePolicyEntity(
      id = "ins-pol-03",
      holderId = "user-fleeting-09",
      holderName = "Express Cargo Logistics SARL",
      tariffName = "Commercial Goods Transport Insurance",
      tariffNameFr = "Assurance Transport Marchandises Pro",
      vehiclePlate = "LT-902-EE",
      issuedBy = userSecretaryDouala.id,
      startsAt = System.currentTimeMillis() - 364L * 24 * 3600 * 1000,
      expiresAt = System.currentTimeMillis() + 1L * 24 * 3600 * 1000, // Due tomorrow! (triggers 1-day alert)
      premiumAmount = 280000.0,
      status = "CRITICAL_RENEWAL"
    )
  )

  // ==========================================
  // 11. EXPEIRY ALERTS (FR-26 & FR-27: 30/7/1 DAYS)
  // ==========================================
  val expiryAlerts = listOf(
    ExpiryAlertEntity(
      id = "alert-01",
      expirableType = "INSURANCE",
      expirableId = "ins-pol-03",
      titleEn = "URGENT (1 Day): Commercial Insurance Expiry",
      titleFr = "URGENT (1 Jour): Expiration Assurance Transport",
      messageEn = "Policy for Express Cargo (LT-902-EE) expires tomorrow. Automated SMS reminder dispatched to owner.",
      messageFr = "La police pour Express Cargo (LT-902-EE) expire demain. Rappel SMS automatique envoyé.",
      leadDays = 1,
      expiryDate = System.currentTimeMillis() + 1L * 24 * 3600 * 1000,
      channels = "IN_APP, SMS (+237 699 234 567), EMAIL",
      isRead = false
    ),
    ExpiryAlertEntity(
      id = "alert-02",
      expirableType = "VEHICLE_SERVICE",
      expirableId = "veh-02",
      titleEn = "Vehicle Service Due (3 Days): Hyundai i10",
      titleFr = "Entretien Véhicule Dû (3 Jours): Hyundai i10",
      messageEn = "Vehicle NW-819-BC at Bamenda is due for maintenance at 39,000 km (current: 38,950 km). Blocked from booking.",
      messageFr = "Véhicule NW-819-BC à Bamenda doit subir sa révision à 39 000 km. Réservations bloquées.",
      leadDays = 7,
      expiryDate = System.currentTimeMillis() + 3L * 24 * 3600 * 1000,
      channels = "IN_APP, SMS (+237 677 222 333)",
      isRead = false
    ),
    ExpiryAlertEntity(
      id = "alert-03",
      expirableType = "INSURANCE",
      expirableId = "ins-pol-01",
      titleEn = "Insurance Renewal (6 Days): Brenda Bih",
      titleFr = "Renouvellement Assurance (6 Jours): Brenda Bih",
      messageEn = "Third-Party policy for NW-241-AA expires in 6 days. Renewal quote sent via WhatsApp & SMS.",
      messageFr = "Police au tiers pour NW-241-AA expire dans 6 jours. Devis de renouvellement envoyé par WhatsApp.",
      leadDays = 7,
      expiryDate = System.currentTimeMillis() + 6L * 24 * 3600 * 1000,
      channels = "IN_APP, SMS (+237 677 889 900)",
      isRead = false
    ),
    ExpiryAlertEntity(
      id = "alert-04",
      expirableType = "ENROLLMENT",
      expirableId = "enr-brenda-01",
      titleEn = "Term Expiry Notice (60 Days): Enrollment Validity",
      titleFr = "Validité Inscription (60 Jours): Terme Proche",
      messageEn = "Brenda Bih Cat B combined enrollment valid until December 2026.",
      messageFr = "Inscription Cat B combinée de Brenda Bih valide jusqu'en décembre 2026.",
      leadDays = 30,
      expiryDate = System.currentTimeMillis() + 60L * 24 * 3600 * 1000,
      channels = "IN_APP, EMAIL",
      isRead = true
    )
  )

  // ==========================================
  // 12. IMMUTABLE FINANCIAL & AUDIT LOGS
  // ==========================================
  val auditLogs = listOf(
    AuditLogEntity(
      id = "log-01",
      action = "FEE_NEGOTIATED",
      actorId = userSecretary.id,
      actorName = userSecretary.name,
      actorRole = "SECRETARY",
      details = "Negotiated fee 115,000 XAF for student Brenda Bih (Standard: 125,000 XAF, 8% discount). Within 15% threshold.",
      timestamp = System.currentTimeMillis() - 7L * 24 * 3600 * 1000
    ),
    AuditLogEntity(
      id = "log-02",
      action = "DISCOUNT_APPROVED",
      actorId = userBranchManager.id,
      actorName = userBranchManager.name,
      actorRole = "BRANCH_MANAGER",
      details = "Approved 8% discount on Cat B Combined enrollment for Brenda Bih.",
      timestamp = System.currentTimeMillis() - 7L * 24 * 3600 * 1000 + 3600000
    ),
    AuditLogEntity(
      id = "log-03",
      action = "CASH_COLLECTED",
      actorId = userSecretary.id,
      actorName = userSecretary.name,
      actorRole = "SECRETARY",
      details = "Collected 50,000 XAF cash (Installment 1) from Brenda Bih. Ref: CSH-BMD-2026-0041. Official receipt issued.",
      timestamp = System.currentTimeMillis() - 6L * 24 * 3600 * 1000
    ),
    AuditLogEntity(
      id = "log-04",
      action = "EXAM_RECOMMENDED",
      actorId = userInstructor.id,
      actorName = userInstructor.name,
      actorRole = "INSTRUCTOR",
      details = "Recommended student Brenda Bih for National Driving Exam Session A. Passed mock driving test.",
      timestamp = System.currentTimeMillis() - 3L * 24 * 3600 * 1000
    ),
    AuditLogEntity(
      id = "log-05",
      action = "EXAM_CANDIDATE_APPROVED",
      actorId = userBranchManager.id,
      actorName = userBranchManager.name,
      actorRole = "BRANCH_MANAGER",
      details = "Branch Manager approved candidate Brenda Bih for national exam application.",
      timestamp = System.currentTimeMillis() - 2L * 24 * 3600 * 1000 - 3600000
    ),
    AuditLogEntity(
      id = "log-06",
      action = "EXAM_CANDIDATE_APPLIED",
      actorId = userSecretary.id,
      actorName = userSecretary.name,
      actorRole = "SECRETARY",
      details = "Secretary registered exam dossier with regional transport delegation for Brenda Bih.",
      timestamp = System.currentTimeMillis() - 2L * 24 * 3600 * 1000 - 1800000
    ),
    AuditLogEntity(
      id = "log-07",
      action = "EXAM_RESULT_RECORDED",
      actorId = "exam-board",
      actorName = "Regional Transport Delegate",
      actorRole = "SUPER_ADMIN",
      details = "Recorded official exam pass for Brenda Bih with 94% score. Verifiable certificate generated.",
      timestamp = System.currentTimeMillis() - 2L * 24 * 3600 * 1000
    )
  )

  // ==========================================
  // 13. PAYMENTS LEDGER
  // ==========================================
  val payments = listOf(
    PaymentEntity(
      id = "pay-01",
      enrollmentId = enrollmentOnsiteBrenda.id,
      studentId = userStudentOnsite.id,
      amount = 50000.0,
      channel = PaymentChannel.CASH,
      reference = "CSH-BMD-2026-0041",
      collectedBy = userSecretary.id,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 6L * 24 * 3600 * 1000,
      notes = "Installment 1 of 2 (Cash at Bamenda Branch Desk)"
    ),
    PaymentEntity(
      id = "pay-02",
      enrollmentId = enrollmentOnsiteBrenda.id,
      studentId = userStudentOnsite.id,
      amount = 65000.0,
      channel = PaymentChannel.MTN_MOMO,
      reference = "MOMO-2026-993812",
      collectedBy = null,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 4L * 24 * 3600 * 1000,
      notes = "Installment 2 of 2 (MTN Mobile Money +237 677 889 900)"
    ),
    PaymentEntity(
      id = "pay-03",
      enrollmentId = enrollmentLucasTruck.id,
      studentId = userStudentLucas.id,
      amount = 185000.0,
      channel = PaymentChannel.ORANGE_MONEY,
      reference = "OM-2026-112233",
      collectedBy = null,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 2L * 24 * 3600 * 1000,
      notes = "Full Payment Category C (Orange Money +237 699 556 677)"
    ),
    PaymentEntity(
      id = "pay-04",
      enrollmentId = enrollmentYaounde.id,
      studentId = userStudentYaounde.id,
      amount = 125000.0,
      channel = PaymentChannel.MTN_MOMO,
      reference = "MOMO-2026-443322",
      collectedBy = null,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 3L * 24 * 3600 * 1000,
      notes = "Full Payment Cat B Bastos (MTN Mobile Money)"
    ),
    PaymentEntity(
      id = "pay-05",
      enrollmentId = enrollmentBafoussam.id,
      studentId = userStudentBafoussam.id,
      amount = 120000.0,
      channel = PaymentChannel.CASH,
      reference = "CSH-BAF-2026-0012",
      collectedBy = userSecretaryBafoussam.id,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 5L * 24 * 3600 * 1000,
      notes = "Full Tuition Cash Receipt Bafoussam Desk"
    ),
    PaymentEntity(
      id = "pay-06",
      enrollmentId = enrollmentGaroua.id,
      studentId = userStudentGaroua.id,
      amount = 60000.0,
      channel = PaymentChannel.CASH,
      reference = "CSH-GAR-2026-0008",
      collectedBy = userSecretaryGaroua.id,
      status = PaymentStatus.CONFIRMED,
      createdAt = System.currentTimeMillis() - 1L * 24 * 3600 * 1000,
      notes = "Cat A Motorcycle Cash Receipt Garoua Desk"
    )
  )

  // ==========================================
  // 14. PRACTICAL SESSIONS
  // ==========================================
  val practicalSessions = listOf(
    PracticalSessionEntity(
      id = "prac-01",
      enrollmentId = enrollmentOnsiteBrenda.id,
      studentId = userStudentOnsite.id,
      instructorId = userInstructor.id,
      vehicleId = "veh-01",
      scheduledAt = System.currentTimeMillis() - 24L * 3600 * 1000,
      durationMinutes = 60,
      attendance = AttendanceStatus.PRESENT,
      odometerStart = 45180,
      odometerEnd = 45200,
      instructorFeedback = "Excellent clutch control, parallel parking mastered on Commercial Ave."
    ),
    PracticalSessionEntity(
      id = "prac-02",
      enrollmentId = enrollmentOnsiteBrenda.id,
      studentId = userStudentOnsite.id,
      instructorId = userInstructor.id,
      vehicleId = "veh-01",
      scheduledAt = System.currentTimeMillis() + 48L * 3600 * 1000,
      durationMinutes = 60,
      attendance = AttendanceStatus.SCHEDULED,
      odometerStart = 45200,
      odometerEnd = null,
      instructorFeedback = null
    ),
    PracticalSessionEntity(
      id = "prac-03",
      enrollmentId = enrollmentLucasTruck.id,
      studentId = userStudentLucas.id,
      instructorId = userInstructorDouala.id,
      vehicleId = "veh-03",
      scheduledAt = System.currentTimeMillis() + 72L * 3600 * 1000,
      durationMinutes = 90,
      attendance = AttendanceStatus.SCHEDULED,
      odometerStart = 82100,
      odometerEnd = null,
      instructorFeedback = null
    )
  )
}
