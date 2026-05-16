package com.aksharadeepa.tutor

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Data models shared by the UI, local database, and the future Firebase sync layer. */
data class SubjectInfo(
    val name: String,
    val storageKey: String,
    val color: Int,
    val lightColor: Int,
    val totalChapters: Int,
)

data class Chapter(
    val id: Int,
    val name: String,
    val subject: String,
)

data class Question(
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

data class User(
    val id: String,
    val fullName: String,
    val phone: String,
    val gender: String,
    val password: String,
)

data class QuizResult(
    val subject: String,
    val chapterId: Int,
    val chapterName: String,
    val score: Int,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
    val total: Int,
    val timeTakenMs: Long,
    val timestamp: Long,
)

object Palette {
    const val PRIMARY = 0xFF1A237E.toInt()
    const val PRIMARY_LIGHT = 0xFF283593.toInt()
    const val ACCENT = 0xFF4A148C.toInt()
    const val BACKGROUND = 0xFFF5F5F5.toInt()
    const val CARD = 0xFFFFFFFF.toInt()
    const val TEXT = 0xFF212121.toInt()
    const val MUTED = 0xFF757575.toInt()
    const val DIVIDER = 0xFFE0E0E0.toInt()
    const val SCIENCE = 0xFF4CAF50.toInt()
    const val SCIENCE_LIGHT = 0xFFE8F5E9.toInt()
    const val MATH = 0xFF2196F3.toInt()
    const val MATH_LIGHT = 0xFFE3F2FD.toInt()
    const val SOCIAL = 0xFFFF9800.toInt()
    const val SOCIAL_LIGHT = 0xFFFFF3E0.toInt()
    const val RED = 0xFFE53935.toInt()
    const val GREEN = 0xFF43A047.toInt()
}

object SyllabusRepository {
    val subjects = listOf(
        SubjectInfo("Science", "Science", Palette.SCIENCE, Palette.SCIENCE_LIGHT, 16),
        SubjectInfo("Mathematics", "Math", Palette.MATH, Palette.MATH_LIGHT, 15),
        SubjectInfo("Social Studies", "SocialStudies", Palette.SOCIAL, Palette.SOCIAL_LIGHT, 20),
    )

    private val science = listOf(
        "Chemical Reactions and Equations",
        "Acids, Bases and Salts",
        "Metals and Non-metals",
        "Carbon and its Compounds",
        "Life Processes",
        "Control and Coordination",
        "How do Organisms Reproduce",
        "Heredity and Evolution",
        "Light - Reflection and Refraction",
        "The Human Eye and the Colourful World",
        "Electricity",
        "Magnetic Effects of Electric Current",
        "Our Environment",
        "Sustainable Management of Natural Resources",
        "Sources of Energy",
        "Periodic Classification of Elements",
    )

    private val mathematics = listOf(
        "Real Numbers",
        "Polynomials",
        "Pair of Linear Equations in Two Variables",
        "Quadratic Equations",
        "Arithmetic Progressions",
        "Triangles",
        "Coordinate Geometry",
        "Introduction to Trigonometry",
        "Some Applications of Trigonometry",
        "Circles",
        "Constructions",
        "Areas Related to Circles",
        "Surface Areas and Volumes",
        "Statistics",
        "Probability",
    )

    private val socialStudies = listOf(
        "The Rise of Nationalism in Europe",
        "Nationalism in India",
        "The Making of a Global World",
        "The Age of Industrialisation",
        "Print Culture and the Modern World",
        "Resources and Development",
        "Forest and Wildlife Resources",
        "Water Resources",
        "Agriculture",
        "Minerals and Energy Resources",
        "Manufacturing Industries",
        "Lifelines of National Economy",
        "Power Sharing",
        "Federalism",
        "Democracy and Diversity",
        "Gender, Religion and Caste",
        "Political Parties",
        "Outcomes of Democracy",
        "Development",
        "Sectors of the Indian Economy",
    )

    fun subjectByName(name: String): SubjectInfo =
        subjects.firstOrNull { (it.name == name) || (it.storageKey == name) } ?: subjects.first()

    fun chapters(subject: SubjectInfo): List<Chapter> {
        val names = when (subject.storageKey) {
            "Science" -> science
            "Math" -> mathematics
            else -> socialStudies
        }
        return names.mapIndexed { index, title -> Chapter(index + 1, title, subject.name) }
    }

    fun questionsFor(chapter: Chapter): List<Question> {
        val profile = conceptProfileFor(chapter)
        return conceptQuestions(chapter.name, profile)
    }

    private data class ConceptProfile(
        val concept: String,
        val idea: String,
        val process: String,
        val example: String,
        val application: String,
        val rule: String,
        val tool: String,
        val contrast: String,
        val misconception: String,
        val marker: String,
    )

    private fun conceptQuestions(topic: String, p: ConceptProfile): List<Question> = listOf(
        Question(
            "$topic: Which option best explains ${p.concept}?",
            listOf(p.idea, p.contrast, p.misconception, "Only the title of the chapter without meaning"),
            0,
            p.idea,
        ),
        Question(
            "$topic: Which process is most directly connected with ${p.concept}?",
            listOf("Copying definitions without applying them", p.process, p.contrast, "Ignoring the condition given in the question"),
            1,
            p.process,
        ),
        Question(
            "$topic: Which example correctly belongs to this concept?",
            listOf(p.misconception, "A point unrelated to $topic", p.example, p.contrast),
            2,
            p.example,
        ),
        Question(
            "$topic: Where is this idea commonly applied?",
            listOf(p.contrast, p.misconception, "Only in decorative diagrams", p.application),
            3,
            p.application,
        ),
        Question(
            "$topic: Which rule or principle should be remembered?",
            listOf(p.rule, "Choose any answer that looks longest", p.contrast, p.misconception),
            0,
            p.rule,
        ),
        Question(
            "$topic: Which tool, representation, or method helps solve questions from this chapter?",
            listOf(p.misconception, p.tool, "Skipping all worked examples", p.contrast),
            1,
            p.tool,
        ),
        Question(
            "$topic: Which option is a common misconception to avoid?",
            listOf(p.idea, p.process, p.misconception, p.application),
            2,
            p.misconception,
        ),
        Question(
            "$topic: Which contrast helps separate this chapter from a similar idea?",
            listOf(p.example, p.rule, p.tool, p.contrast),
            3,
            p.contrast,
        ),
        Question(
            "$topic: Which marker tells you the question is testing ${p.concept}?",
            listOf(p.marker, p.misconception, "A random page number", p.contrast),
            0,
            p.marker,
        ),
        Question(
            "$topic: Which answer pair is correctly matched?",
            listOf("Misconception - ${p.idea}", "Concept - ${p.concept}", "Wrong method - ${p.tool}", "Unrelated fact - ${p.process}"),
            1,
            "${p.concept} is the central concept being tested.",
        ),
        Question(
            "$topic: If a question asks for reasoning, which point is most useful?",
            listOf(p.tool, p.example, p.process, "Writing only yes or no"),
            2,
            p.process,
        ),
        Question(
            "$topic: Which option should be rejected because it does not fit the chapter concept?",
            listOf(p.idea, p.example, p.application, p.misconception),
            3,
            p.misconception,
        ),
        Question(
            "$topic: Which statement connects the concept to a real situation?",
            listOf(p.application, p.contrast, "The question can be answered without reading it", p.misconception),
            0,
            p.application,
        ),
        Question(
            "$topic: What should you focus on while revising this chapter?",
            listOf("Only the spelling of headings", "${p.concept}, ${p.process}, and ${p.example}", "Only the page layout", "Answers from a different subject"),
            1,
            "The concept, process, and example together build usable understanding.",
        ),
        Question(
            "$topic: Which option gives the strongest exam answer?",
            listOf("A vague sentence with no concept", p.misconception, "${p.idea}; for example, ${p.example}", p.contrast),
            2,
            "A strong answer combines the correct idea with a relevant example.",
        ),
    )

    private fun conceptProfileFor(chapter: Chapter): ConceptProfile = when (chapter.name) {
        "Chemical Reactions and Equations" -> cp("chemical reaction", "Reactants change into new substances called products", "balancing equations using conservation of mass", "magnesium ribbon burning to form magnesium oxide", "explaining rusting, respiration, and combustion", "atoms are neither created nor destroyed in a balanced equation", "balanced chemical equation", "physical change where no new substance forms", "colour change alone always proves a reaction", "reactants, products, gas, precipitate, heat, or colour change")
        "Acids, Bases and Salts" -> cp("acid-base behaviour", "Acids release hydrogen ions and bases release hydroxide ions in water", "neutralisation producing salt and water", "hydrochloric acid reacting with sodium hydroxide", "using antacids and treating acidic soil", "pH below 7 is acidic and pH above 7 is basic", "pH scale and indicators", "neutral salt solution with pH near 7", "all acids are dangerous only when concentrated", "pH, indicator colour, neutralisation, salt, or water")
        "Metals and Non-metals" -> cp("metal and non-metal properties", "Metals are generally malleable conductors while non-metals often are not", "reactivity series and displacement reactions", "zinc displacing copper from copper sulphate solution", "choosing metals for wires, utensils, and alloys", "more reactive metals displace less reactive metals from compounds", "reactivity series", "non-metallic oxide forming acidic solution", "all metals react with water at the same rate", "malleability, ductility, conductivity, displacement, or ore")
        "Carbon and its Compounds" -> cp("carbon bonding", "Carbon forms covalent bonds and long chains due to tetravalency and catenation", "formation of homologous series and functional groups", "ethanol oxidising to ethanoic acid", "fuels, soaps, detergents, and organic materials", "same functional group gives similar chemical properties", "structural formula", "ionic bonding by complete electron transfer", "all carbon compounds are simple conductors", "covalent bond, tetravalency, functional group, or homologous series")
        "Life Processes" -> cp("life processes", "Nutrition, respiration, transport, and excretion keep organisms alive", "movement of materials through body systems", "photosynthesis making glucose in green leaves", "understanding digestion, breathing, and circulation", "energy is released from food during respiration", "flow chart of body systems", "non-living chemical change", "breathing and respiration mean exactly the same thing", "nutrition, respiration, transport, excretion, enzyme, or chlorophyll")
        "Control and Coordination" -> cp("control and coordination", "Organisms respond to stimuli through nervous and hormonal control", "reflex action through a reflex arc", "withdrawing a hand from a hot object", "explaining quick responses and plant movements", "nerve impulses travel through neurons to effectors", "reflex arc diagram", "digestion of food in the stomach", "plants have no coordination because they lack nerves", "stimulus, receptor, neuron, hormone, reflex, or synapse")
        "How do Organisms Reproduce" -> cp("reproduction", "Reproduction creates new individuals and transfers genetic information", "gamete formation, fertilisation, and development", "pollen reaching stigma before fertilisation in flowers", "understanding continuity of species", "sexual reproduction creates variation", "flower reproductive parts diagram", "growth of an individual without producing offspring", "asexual reproduction always needs two parents", "gamete, fertilisation, pollination, ovary, or zygote")
        "Heredity and Evolution" -> cp("heredity and evolution", "Traits pass from parents to offspring through genes", "inheritance patterns and natural selection", "pea plant traits used in Mendel's experiments", "explaining variation and adaptation", "dominant traits can appear when one dominant allele is present", "Punnett square", "acquired traits such as muscle size changing DNA directly", "all variations are harmful", "gene, trait, allele, dominant, recessive, or variation")
        "Light - Reflection and Refraction" -> cp("reflection and refraction", "Light changes direction on reflection and bends during refraction", "image formation by mirrors and lenses", "a pencil appearing bent in water", "designing spectacles, mirrors, and optical devices", "angle of incidence equals angle of reflection", "ray diagram", "sound wave reflection", "concave and convex lenses always form the same image", "incident ray, reflected ray, refracted ray, focus, or lens")
        "The Human Eye and the Colourful World" -> cp("human eye and colour", "The eye forms images on the retina and white light can disperse into colours", "accommodation and dispersion", "rainbow formation after sunlight passes through water droplets", "correcting vision defects using lenses", "myopia is corrected using a concave lens", "eye diagram and prism diagram", "magnetic effect of current", "all vision defects use the same lens", "retina, lens, pupil, myopia, hypermetropia, prism, or dispersion")
        "Electricity" -> cp("electric current", "Current is the rate of flow of electric charge", "using Ohm's law and circuit combinations", "a bulb glowing in a closed circuit", "calculating household electrical energy use", "V = IR for an ohmic conductor", "circuit diagram", "magnetic field pattern around a magnet", "current flows in an open circuit", "current, voltage, resistance, circuit, power, or energy")
        "Magnetic Effects of Electric Current" -> cp("magnetic effect of current", "Current-carrying conductors produce magnetic fields", "right-hand thumb rule and electromagnetic induction", "deflection of a compass near a current-carrying wire", "electric motors, generators, and electromagnets", "field direction depends on current direction", "field line diagram", "chemical neutralisation", "magnetic field lines cross each other", "solenoid, magnetic field, current, motor, generator, or Fleming rule")
        "Our Environment" -> cp("ecosystem balance", "Organisms interact through food chains and nutrient cycles", "energy transfer across trophic levels", "grass to deer to tiger food chain", "understanding pollution and ecosystem damage", "only about a small fraction of energy passes to the next trophic level", "food chain or food web", "electric circuit arrangement", "biodegradable and non-biodegradable wastes break down equally fast", "producer, consumer, decomposer, trophic level, or ozone")
        "Sustainable Management of Natural Resources" -> cp("sustainable resource use", "Resources should be used so present and future needs are balanced", "reduce, reuse, recycle, and community management", "rainwater harvesting in a village", "conserving forests, water, coal, and petroleum", "local participation improves resource management", "resource management plan", "unlimited extraction without renewal", "sustainable use means never using resources", "conservation, watershed, fossil fuel, forest, or stakeholder")
        "Sources of Energy" -> cp("energy sources", "Energy sources are chosen by availability, efficiency, cost, and pollution", "conversion of one form of energy into useful work", "solar cells converting sunlight into electricity", "selecting renewable energy for local needs", "a good fuel has high calorific value and low pollution", "energy conversion chart", "heredity pattern in organisms", "all renewable sources are available continuously everywhere", "fuel, calorific value, renewable, non-renewable, turbine, or solar cell")
        "Periodic Classification of Elements" -> cp("periodic classification", "Elements are arranged so properties repeat periodically", "grouping elements by valence electrons and atomic number", "elements in the same group showing similar valency", "predicting properties of unknown elements", "modern periodic law uses atomic number", "periodic table", "classification by colour only", "atomic mass always gives the perfect order", "group, period, valency, atomic number, or periodicity")
        "Real Numbers" -> cp("real numbers", "Real numbers include rational and irrational numbers", "prime factorisation and Euclid's division algorithm", "finding HCF using repeated division", "simplifying ratios and proving irrationality", "every composite number has a unique prime factorisation", "factor tree", "geometric angle construction", "all decimals are terminating", "HCF, LCM, prime factor, rational, irrational, or decimal expansion")
        "Polynomials" -> cp("polynomials", "A polynomial is an expression with variables having whole-number powers", "finding zeroes and relating them to coefficients", "x squared minus 5x plus 6 having zeroes 2 and 3", "modelling area and number patterns", "for ax squared plus bx plus c, sum of zeroes is -b/a", "graph or factor form", "linear equation in two variables only", "every expression with x in denominator is a polynomial", "zero, coefficient, degree, factor, or quadratic polynomial")
        "Pair of Linear Equations in Two Variables" -> cp("linear equation pair", "Two linear equations can have one, none, or infinitely many solutions", "substitution, elimination, or graphical method", "finding two numbers from their sum and difference", "solving age, cost, and mixture problems", "intersecting lines give a unique solution", "coordinate graph", "single-variable quadratic equation", "parallel lines have a common solution", "linear pair, solution, elimination, substitution, or intersection")
        "Quadratic Equations" -> cp("quadratic equation", "A quadratic equation has degree two", "factorisation, completing square, or formula method", "solving x squared - 5x + 6 = 0", "area and projectile-style word problems", "discriminant decides nature of roots", "quadratic formula", "linear equation of degree one", "negative discriminant gives two real roots", "root, discriminant, factorisation, coefficient, or parabola")
        "Arithmetic Progressions" -> cp("arithmetic progression", "An AP is a sequence with a constant difference", "finding nth term and sum of terms", "2, 5, 8, 11 as an AP", "planning savings or seating arrangements", "nth term is a + (n - 1)d", "AP table", "geometric sequence with constant ratio", "common difference changes every term", "term, common difference, nth term, or sum")
        "Triangles" -> cp("similar triangles", "Similar triangles have equal corresponding angles and proportional sides", "using similarity criteria and Pythagoras theorem", "finding height using proportional sides", "surveying and indirect measurement", "corresponding sides of similar triangles are in the same ratio", "labelled triangle diagram", "circles with equal radii", "equal area always means similar triangles", "similarity, congruence, ratio, hypotenuse, or theorem")
        "Coordinate Geometry" -> cp("coordinate geometry", "Points are located using ordered pairs on the Cartesian plane", "distance, section, and area formula use coordinates", "finding distance between (0,0) and (3,4)", "mapping positions and shapes", "distance formula comes from Pythagoras theorem", "coordinate plane", "probability of an event", "x-coordinate and y-coordinate can be swapped without change", "x-axis, y-axis, quadrant, distance, midpoint, or section")
        "Introduction to Trigonometry" -> cp("trigonometric ratios", "Trigonometric ratios compare sides of a right triangle", "using sin, cos, and tan ratios", "tan theta equals opposite side by adjacent side", "finding heights and distances", "sin squared theta plus cos squared theta equals 1", "right triangle diagram", "statistics table of frequency", "trigonometry works for any triangle without conditions", "sine, cosine, tangent, hypotenuse, opposite, or adjacent")
        "Some Applications of Trigonometry" -> cp("height and distance", "Trigonometry can find inaccessible heights and distances", "forming right triangles from angles of elevation or depression", "finding the height of a tower using its shadow angle", "surveying buildings, trees, and hills", "angle of elevation is measured upward from the horizontal", "line-of-sight diagram", "polynomial factor theorem", "angle of elevation and depression are always equal", "line of sight, elevation, depression, height, or distance")
        "Circles" -> cp("circle tangents", "A tangent touches a circle at exactly one point", "using radius perpendicular to tangent", "lengths of tangents from an external point being equal", "wheel, pulley, and circular design problems", "radius is perpendicular to tangent at point of contact", "circle tangent diagram", "arithmetic progression", "a tangent cuts the circle at two points", "radius, tangent, chord, secant, point of contact, or centre")
        "Constructions" -> cp("geometric construction", "Construction uses compass and ruler to create accurate figures", "bisecting lines, angles, and constructing similar triangles", "drawing a tangent to a circle from an external point", "accurate diagrams in geometry", "steps must preserve given measurements and ratios", "compass-ruler construction", "estimation without instruments", "freehand drawing is the same as construction", "arc, bisector, tangent, scale factor, or construction step")
        "Areas Related to Circles" -> cp("circle area", "Area formulas measure regions related to circles", "calculating sector and segment areas", "area of a semicircular garden", "designing circular tracks and fields", "area of circle is pi r squared", "sector diagram", "linear equation solution", "circumference and area have the same unit", "sector, segment, radius, circumference, arc, or area")
        "Surface Areas and Volumes" -> cp("mensuration", "Surface area measures covering and volume measures capacity", "using formulas for solids and combinations", "finding volume of a cylindrical tank", "packing, painting, and container problems", "volume of cylinder is pi r squared h", "solid shape net or formula table", "probability experiment", "surface area and volume are measured in the same units", "cube, cuboid, cylinder, cone, sphere, surface area, or volume")
        "Statistics" -> cp("statistics", "Statistics organises data to find central tendencies", "calculating mean, median, and mode", "finding average marks of a class", "analysing survey and performance data", "mean equals sum of observations divided by number of observations", "frequency table", "angle bisector construction", "mode is always the middle observation", "mean, median, mode, frequency, class interval, or ogive")
        "Probability" -> cp("probability", "Probability measures the chance of an event", "comparing favourable outcomes with total outcomes", "probability of getting a head in a coin toss", "predicting outcomes in games and experiments", "probability lies from 0 to 1", "sample space table", "surface area formula", "probability can be greater than 1", "event, outcome, sample space, favourable outcome, or chance")
        "The Rise of Nationalism in Europe" -> cp("European nationalism", "Nationalism created shared identity among people of a region", "unification movements and revolutions", "unification of Germany under Bismarck", "understanding modern nation-states", "symbols and common history strengthen national identity", "timeline and map of Europe", "local self-government in India", "nationalism means only loyalty to a king", "nation-state, liberalism, revolution, unification, or allegory")
        "Nationalism in India" -> cp("Indian nationalism", "Indian nationalism united people against colonial rule", "mass movements such as non-cooperation and civil disobedience", "Salt March as protest against British salt law", "understanding freedom struggle strategies", "shared struggle can create national unity", "movement timeline", "industrial production in Europe", "all groups joined every movement for the same reason", "satyagraha, boycott, swadeshi, civil disobedience, or colonial rule")
        "The Making of a Global World" -> cp("globalisation history", "Trade, migration, and technology connected distant regions", "movement of goods, people, capital, and ideas", "Silk routes connecting Asia and Europe", "understanding present global economy", "global links can create both opportunity and crisis", "trade route map", "village local government only", "globalisation began only with the internet", "trade, migration, indentured labour, tariff, or global market")
        "The Age of Industrialisation" -> cp("industrialisation", "Industrialisation shifted production from hand labour to machine-based factories", "factory system, labour changes, and market expansion", "textile mills producing cloth in large quantities", "understanding modern industries and labour", "machines increased production but changed worker lives", "factory production chart", "forest conservation policy", "industrialisation happened identically in every country", "factory, labour, textile, machine, or industrial capitalist")
        "Print Culture and the Modern World" -> cp("print culture", "Print spread ideas quickly to large numbers of people", "printing press, newspapers, books, and public debate", "newspapers spreading nationalist ideas", "understanding literacy and reform movements", "print can challenge authority by spreading new ideas", "print timeline", "mineral resource map", "printed books had no effect on society", "manuscript, press, newspaper, censorship, or public sphere")
        "Resources and Development" -> cp("resources", "Resources are useful materials transformed by human needs and technology", "classification, planning, and conservation of resources", "soil used for agriculture", "resource planning for sustainable development", "resources become valuable through utility and access", "resource map", "political party alliance", "all resources are unlimited", "renewable, non-renewable, resource planning, conservation, or stock")
        "Forest and Wildlife Resources" -> cp("biodiversity conservation", "Forests and wildlife maintain ecological balance", "conservation through protected areas and community participation", "sacred groves protecting local species", "preventing habitat loss and extinction", "biodiversity supports ecosystem stability", "biodiversity chart", "bank loan calculation", "conservation means removing people from every forest", "flora, fauna, biodiversity, endangered, or habitat")
        "Water Resources" -> cp("water resource management", "Water must be conserved because usable freshwater is limited", "dams, rainwater harvesting, and watershed management", "rooftop rainwater harvesting", "irrigation, drinking water, and flood control", "local harvesting reduces water scarcity", "river basin map", "language policy in federalism", "large dams solve every water problem without effects", "dam, groundwater, rainwater harvesting, watershed, or scarcity")
        "Agriculture" -> cp("agriculture", "Agriculture is cultivation of crops based on soil, climate, and inputs", "cropping seasons and farming practices", "rice grown as a kharif crop in high rainfall areas", "food security and rural livelihoods", "crop choice depends on climate, soil, and water", "crop season calendar", "court structure", "all crops need the same climate", "kharif, rabi, zaid, irrigation, plantation, or food crop")
        "Minerals and Energy Resources" -> cp("minerals and energy", "Minerals are naturally occurring substances and energy resources power activities", "extraction, distribution, and conservation", "coal used to produce thermal power", "industry, transport, and household energy use", "minerals are unevenly distributed and exhaustible", "mineral map", "gender division of labour", "minerals can be renewed quickly after mining", "ore, mineral, fossil fuel, thermal power, or conservation")
        "Manufacturing Industries" -> cp("manufacturing", "Manufacturing converts raw materials into finished goods", "industrial location and production chains", "sugar industry near sugarcane-growing areas", "employment, economic growth, and exports", "industries locate near raw material, labour, market, and power", "industrial location map", "election manifesto", "manufacturing does not depend on transport", "raw material, industry, market, labour, power, or pollution")
        "Lifelines of National Economy" -> cp("transport and communication", "Transport, trade, and communication connect the economy", "movement of goods, people, and information", "railways carrying bulk goods across India", "national integration and market access", "efficient transport lowers distance barriers", "transport network map", "soil profile diagram", "communication is unrelated to trade", "roadways, railways, ports, trade, communication, or tourism")
        "Power Sharing" -> cp("power sharing", "Power sharing distributes authority among groups and institutions", "horizontal and vertical division of power", "power shared between legislature, executive, and judiciary", "reducing conflict in democracy", "power sharing is desirable for stability and fairness", "power-sharing chart", "crop rotation method", "democracy works best when one group holds all power", "federalism, coalition, checks and balances, or community government")
        "Federalism" -> cp("federalism", "Federalism divides power between central and state governments", "constitutional distribution of subjects", "Union List, State List, and Concurrent List", "governing diverse countries effectively", "different levels of government have their own jurisdiction", "three-list table", "food chain energy flow", "federalism means no central government", "jurisdiction, union, state, concurrent, decentralisation, or local government")
        "Democracy and Diversity" -> cp("social diversity", "Democracy handles social differences through accommodation and equality", "recognising overlapping and cross-cutting differences", "different communities sharing political space", "reducing social conflict", "cross-cutting differences are easier to accommodate", "social division chart", "electric circuit diagram", "all diversity automatically creates conflict", "social division, diversity, accommodation, or identity")
        "Gender, Religion and Caste" -> cp("social equality", "Gender, religion, and caste affect social and political participation", "challenging discrimination and communalism", "reservation improving representation", "building equal democratic participation", "constitutional equality rejects discrimination", "representation table", "trigonometric ratio", "caste has completely disappeared from politics", "patriarchy, communalism, caste, secularism, or representation")
        "Political Parties" -> cp("political parties", "Political parties contest elections and form governments", "candidate selection, campaigns, and policy promises", "a party manifesto before an election", "linking citizens with government", "parties aggregate interests and provide choices", "party system chart", "water harvesting structure", "democracy does not need political parties", "manifesto, coalition, opposition, ruling party, or election")
        "Outcomes of Democracy" -> cp("democratic outcomes", "Democracy is judged by accountability, participation, and dignity", "evaluating political and social results", "citizens questioning elected representatives", "protecting rights and peaceful correction of mistakes", "democracy allows public discussion and correction", "outcome comparison table", "chemical reaction equation", "democracy always gives instant economic equality", "accountability, transparency, dignity, participation, or legitimacy")
        "Development" -> cp("development", "Development means improvement in quality of life, not only income", "comparing income, health, education, and sustainability", "using per capita income and literacy rate together", "planning public facilities and welfare", "different people can have different development goals", "development indicator table", "mirror ray diagram", "higher income alone always means full development", "per capita income, HDI, sustainability, public facilities, or goal")
        "Sectors of the Indian Economy" -> cp("economic sectors", "Economic activities are grouped into primary, secondary, and tertiary sectors", "classification by type of production or service", "farming as primary and banking as tertiary", "understanding employment and GDP contribution", "sectors are interdependent", "sector classification table", "periodic table group", "only factories contribute to the economy", "primary, secondary, tertiary, organised, unorganised, or GDP")
        else -> cp(chapter.name, "The chapter explains the central idea of ${chapter.name}", "applying the main steps from ${chapter.name}", "a textbook example from ${chapter.name}", "solving questions related to ${chapter.name}", "the main rule stated in ${chapter.name}", "chapter notes and diagrams", "an unrelated concept from another chapter", "guessing without using the chapter concept", "keywords from ${chapter.name}")
    }

    private fun cp(
        concept: String,
        idea: String,
        process: String,
        example: String,
        application: String,
        rule: String,
        tool: String,
        contrast: String,
        misconception: String,
        marker: String
    ): ConceptProfile = ConceptProfile(concept, idea, process, example, application, rule, tool, contrast, misconception, marker)
}

class AppDatabase(private val appContext: Context) : SQLiteOpenHelper(appContext, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id TEXT PRIMARY KEY,
                full_name TEXT NOT NULL,
                phone TEXT NOT NULL UNIQUE,
                gender TEXT NOT NULL,
                password TEXT NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE completed_chapters (
                user_id TEXT NOT NULL,
                subject_key TEXT NOT NULL,
                chapter_id INTEGER NOT NULL,
                best_score INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(user_id, subject_key, chapter_id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE quiz_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                subject TEXT NOT NULL,
                chapter_id INTEGER NOT NULL,
                chapter_name TEXT NOT NULL,
                score INTEGER NOT NULL,
                correct INTEGER NOT NULL,
                wrong INTEGER NOT NULL,
                skipped INTEGER NOT NULL,
                total INTEGER NOT NULL,
                time_taken_ms INTEGER NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        createQuestionBankTable(db)
        db.execSQL("CREATE INDEX idx_quiz_results_user ON quiz_results(user_id, timestamp)")
        db.execSQL("CREATE INDEX idx_completed_user_subject ON completed_chapters(user_id, subject_key)")
        seedQuestionBank(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createQuestionBankTable(db)
            seedQuestionBank(db)
        }
        if (oldVersion < 3) {
            seedQuestionBankFromAssets(db)
        }
    }

    private fun createQuestionBankTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS question_bank (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                subject_key TEXT NOT NULL,
                subject_name TEXT NOT NULL,
                chapter_id INTEGER NOT NULL,
                chapter_name TEXT NOT NULL,
                prompt TEXT NOT NULL,
                option_a TEXT NOT NULL,
                option_b TEXT NOT NULL,
                option_c TEXT NOT NULL,
                option_d TEXT NOT NULL,
                correct_index INTEGER NOT NULL,
                explanation TEXT NOT NULL,
                difficulty TEXT NOT NULL DEFAULT 'basic',
                UNIQUE(subject_key, chapter_id, prompt)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_question_bank_chapter ON question_bank(subject_key, chapter_id)")
    }

    private fun seedQuestionBank(db: SQLiteDatabase) {
        val existing = db.rawQuery("SELECT COUNT(*) FROM question_bank", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        if (existing > 0) return

        val importedCount = seedQuestionBankFromAssets(db)
        seedGeneratedQuestionFallbacks(db)
        android.util.Log.i("AksharaDeepa", "Question bank seeded: $importedCount imported rows plus generated fallback rows")
    }

    private fun seedQuestionBankFromAssets(db: SQLiteDatabase): Int {
        val json = runCatching {
            appContext.assets.open(QUESTION_SEED_ASSET).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return 0
        val root = runCatching { JSONObject(json) }.getOrNull() ?: return 0
        val array = root.optJSONArray("questions") ?: return 0
        val curatedChapters = mutableSetOf<Pair<String, Int>>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val subjectKey = item.optString("subjectKey")
            val chapterId = item.optInt("chapterId", -1)
            val options = item.optJSONArray("options")
            val correctIndex = item.optInt("correctIndex", -1)
            val isSubjectValid = subjectKey.isNotBlank()
            val isChapterValid = chapterId > 0
            val isOptionsValid = (options != null) && (options.length() == 4)
            val isCorrectIndexValid = correctIndex >= 0 && correctIndex <= 3
            if (isSubjectValid && isChapterValid && isOptionsValid && isCorrectIndexValid) {
                curatedChapters.add(subjectKey to chapterId)
            }
        }

        curatedChapters.forEach { (subjectKey, chapterId) ->
            db.delete("question_bank", "subject_key = ? AND chapter_id = ?", arrayOf(subjectKey, chapterId.toString()))
        }

        var inserted = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val subjectKey = item.optString("subjectKey")
            val chapterId = item.optInt("chapterId", -1)
            val prompt = item.optString("prompt")
            val options = item.optJSONArray("options")
            val correctIndex = item.optInt("correctIndex", -1)
            val explanation = item.optString("explanation")
            if (subjectKey.isBlank() || chapterId <= 0 || prompt.isBlank() || options == null || options.length() != 4 || correctIndex !in 0..3) continue

            val subject = SyllabusRepository.subjectByName(subjectKey)
            val chapter = SyllabusRepository.chapters(subject).firstOrNull { it.id == chapterId } ?: continue
            val question = Question(
                prompt = prompt,
                options = List(4) { optionIndex -> options.optString(optionIndex) },
                correctIndex = correctIndex,
                explanation = explanation.ifBlank { "Review the concept in ${chapter.name}." }
            )
            if (insertQuestion(db, subject, chapter, question, item.optString("difficulty", "basic"))) inserted++
        }
        return inserted
    }

    private fun seedGeneratedQuestionFallbacks(db: SQLiteDatabase) {
        SyllabusRepository.subjects.forEach { subject ->
            SyllabusRepository.chapters(subject).forEach { chapter ->
                val existing = db.rawQuery(
                    "SELECT COUNT(*) FROM question_bank WHERE subject_key = ? AND chapter_id = ?",
                    arrayOf(subject.storageKey, chapter.id.toString())
                ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
                if (existing == 0) {
                    SyllabusRepository.questionsFor(chapter).forEach { question ->
                        insertQuestion(db, subject, chapter, question, "basic")
                    }
                }
            }
        }
    }

    private fun insertQuestion(
        db: SQLiteDatabase,
        subject: SubjectInfo,
        chapter: Chapter,
        question: Question,
        difficulty: String
    ): Boolean {
        val values = ContentValues().apply {
            put("subject_key", subject.storageKey)
            put("subject_name", subject.name)
            put("chapter_id", chapter.id)
            put("chapter_name", chapter.name)
            put("prompt", question.prompt)
            put("option_a", question.options.getOrElse(0) { "" })
            put("option_b", question.options.getOrElse(1) { "" })
            put("option_c", question.options.getOrElse(2) { "" })
            put("option_d", question.options.getOrElse(3) { "" })
            put("correct_index", question.correctIndex)
            put("explanation", question.explanation)
            put("difficulty", difficulty.ifBlank { "basic" })
        }
        return db.insertWithOnConflict("question_bank", null, values, SQLiteDatabase.CONFLICT_IGNORE) != -1L
    }

    fun questionsFor(chapter: Chapter): List<Question> {
        val subject = SyllabusRepository.subjects.firstOrNull { it.name == chapter.subject }
            ?: SyllabusRepository.subjectByName(chapter.subject)
        val cursor = readableDatabase.rawQuery(
            """
            SELECT prompt, option_a, option_b, option_c, option_d, correct_index, explanation
            FROM question_bank
            WHERE subject_key = ? AND chapter_id = ?
            ORDER BY id ASC
            """.trimIndent(),
            arrayOf(subject.storageKey, chapter.id.toString())
        )
        val questions = cursor.use {
            val rows = mutableListOf<Question>()
            while (it.moveToNext()) {
                rows.add(
                    Question(
                        prompt = it.getString(0),
                        options = listOf(it.getString(1), it.getString(2), it.getString(3), it.getString(4)),
                        correctIndex = it.getInt(5),
                        explanation = it.getString(6)
                    )
                )
            }
            rows
        }
        return questions.ifEmpty { SyllabusRepository.questionsFor(chapter) }
    }
    fun addUser(user: User) {
        val values = ContentValues().apply {
            put("id", user.id)
            put("full_name", user.fullName)
            put("phone", user.phone)
            put("gender", user.gender)
            put("password", user.password)
            put("created_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun findUserByPhone(phone: String): User? = queryUser("phone = ?", arrayOf(phone))

    fun userById(id: String): User? = queryUser("id = ?", arrayOf(id))

    fun markCompleted(userId: String, subject: SubjectInfo, chapterId: Int, bestScore: Int) {
        val previousBest = bestScore(userId, subject, chapterId)
        val values = ContentValues().apply {
            put("user_id", userId)
            put("subject_key", subject.storageKey)
            put("chapter_id", chapterId)
            put("best_score", maxOf(previousBest, bestScore))
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("completed_chapters", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun completed(userId: String, subject: SubjectInfo): Set<String> {
        val cursor = readableDatabase.rawQuery(
            "SELECT chapter_id FROM completed_chapters WHERE user_id = ? AND subject_key = ? ORDER BY chapter_id",
            arrayOf(userId, subject.storageKey)
        )
        return cursor.use {
            val ids = mutableSetOf<String>()
            while (it.moveToNext()) ids.add(it.getInt(0).toString())
            ids
        }
    }

    fun bestScore(userId: String, subject: SubjectInfo, chapterId: Int): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT best_score FROM completed_chapters WHERE user_id = ? AND subject_key = ? AND chapter_id = ? LIMIT 1",
            arrayOf(userId, subject.storageKey, chapterId.toString())
        )
        return cursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    fun addQuizResult(userId: String, result: QuizResult) {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("subject", result.subject)
            put("chapter_id", result.chapterId)
            put("chapter_name", result.chapterName)
            put("score", result.score)
            put("correct", result.correct)
            put("wrong", result.wrong)
            put("skipped", result.skipped)
            put("total", result.total)
            put("time_taken_ms", result.timeTakenMs)
            put("timestamp", result.timestamp)
        }
        writableDatabase.insert("quiz_results", null, values)
    }

    fun quizResults(userId: String): List<QuizResult> {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT subject, chapter_id, chapter_name, score, correct, wrong, skipped, total, time_taken_ms, timestamp
            FROM quiz_results
            WHERE user_id = ?
            ORDER BY timestamp ASC
            """.trimIndent(),
            arrayOf(userId)
        )
        return cursor.use {
            val results = mutableListOf<QuizResult>()
            while (it.moveToNext()) results.add(it.toQuizResult())
            results
        }
    }

    private fun queryUser(whereClause: String, whereArgs: Array<String>): User? {
        val cursor = readableDatabase.query(
            "users",
            arrayOf("id", "full_name", "phone", "gender", "password"),
            whereClause,
            whereArgs,
            null,
            null,
            null,
            "1"
        )
        return cursor.use { if (it.moveToFirst()) it.toUser() else null }
    }

    private fun Cursor.toUser(): User = User(
        getString(column("id")),
        getString(column("full_name")),
        getString(column("phone")),
        getString(column("gender")),
        getString(column("password"))
    )

    private fun Cursor.toQuizResult(): QuizResult = QuizResult(
        getString(0),
        getInt(1),
        getString(2),
        getInt(3),
        getInt(4),
        getInt(5),
        getInt(6),
        getInt(7),
        getLong(8),
        getLong(9)
    )

    private fun Cursor.column(name: String): Int = getColumnIndexOrThrow(name)

    companion object {
        private const val DB_NAME = "akshara_deepa.db"
        private const val DB_VERSION = 4
        private const val QUESTION_SEED_ASSET = "questions_seed.json"
    }
}

class PreferenceStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("akshara_deepa_prefs", Context.MODE_PRIVATE)
    private val db = AppDatabase(context.applicationContext)
    private val firebaseSync = FirebaseSyncService(context.applicationContext)

    init {
        migrateLegacyPreferenceData()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    fun currentUserId(): String? = prefs.getString("user_id", null)

    fun setLoggedIn(user: User) {
        prefs.edit {
            putBoolean("is_logged_in", true)
            putString("user_id", user.id)
            putString("user_name", user.fullName)
        }
    }

    fun logout() {
        val savedPhone = prefs.getString("saved_phone", "")
        val remember = prefs.getBoolean("remember_me", false)
        prefs.edit { clear() }
        if (remember) {
            prefs.edit {
                putBoolean("remember_me", true)
                putString("saved_phone", savedPhone)
                putBoolean("sqlite_migrated", true)
            }
        }
    }

    fun saveCredentials(phone: String, password: String) {
        prefs.edit {
            putBoolean("remember_me", true)
            putString("saved_phone", phone)
        }
    }

    fun clearCredentials() {
        prefs.edit {
            putBoolean("remember_me", false)
            remove("saved_phone")
        }
    }

    fun rememberedCredentials(): Pair<String, String> =
        Pair(prefs.getString("saved_phone", "") ?: "", "")
    fun addUser(fullName: String, phone: String, gender: String, password: String): User {
        val user = User(UUID.randomUUID().toString(), fullName, phone, gender, password)
        db.addUser(user)
        firebaseSync.syncUser(user)
        return user
    }

    fun findUserByPhone(phone: String): User? = db.findUserByPhone(phone)
    fun userById(id: String): User? = db.userById(id)

    fun markCompleted(userId: String, subject: SubjectInfo, chapter: Chapter, bestScore: Int) {
        db.markCompleted(userId, subject, chapter.id, bestScore)
        firebaseSync.syncChapterProgress(userId, subject, chapter, bestScore)
    }

    fun completed(userId: String, subject: SubjectInfo): Set<String> = db.completed(userId, subject)

    fun bestScore(userId: String, subject: SubjectInfo, chapterId: Int): Int = db.bestScore(userId, subject, chapterId)

    fun addQuizResult(userId: String, result: QuizResult) {
        db.addQuizResult(userId, result)
        firebaseSync.syncQuizResult(userId, result)
    }

    fun quizResults(userId: String): List<QuizResult> = db.quizResults(userId)

    fun questionsFor(chapter: Chapter): List<Question> = db.questionsFor(chapter)

    private fun migrateLegacyPreferenceData() {
        if (prefs.getBoolean("sqlite_migrated", false)) return
        val users = runCatching { JSONArray(prefs.getString("users", "[]")) }.getOrDefault(JSONArray())
        for (index in 0 until users.length()) {
            val user = runCatching { userFromJson(users.getJSONObject(index)) }.getOrNull() ?: continue
            db.addUser(user)
            SyllabusRepository.subjects.forEach { subject ->
                val completed = prefs.getStringSet("completed_${user.id}_${subject.storageKey}", emptySet()) ?: emptySet()
                completed.forEach { chapterIdText ->
                    val chapterId = chapterIdText.toIntOrNull() ?: return@forEach
                    val best = prefs.getInt("best_${user.id}_${subject.storageKey}_$chapterId", 0)
                    db.markCompleted(user.id, subject, chapterId, best)
                }
            }
            val results = runCatching { JSONArray(prefs.getString("results_${user.id}", "[]")) }.getOrDefault(JSONArray())
            for (resultIndex in 0 until results.length()) {
                val result = runCatching { quizResultFromJson(results.getJSONObject(resultIndex)) }.getOrNull() ?: continue
                db.addQuizResult(user.id, result)
            }
        }
        prefs.edit { putBoolean("sqlite_migrated", true) }
    }
}

private fun userFromJson(json: JSONObject): User = User(
    json.getString("id"),
    json.getString("fullName"),
    json.getString("phone"),
    json.getString("gender"),
    json.getString("password")
)

private fun quizResultFromJson(json: JSONObject): QuizResult = QuizResult(
    json.getString("subject"),
    json.getInt("chapterId"),
    json.getString("chapterName"),
    json.getInt("score"),
    json.getInt("correct"),
    json.getInt("wrong"),
    json.getInt("skipped"),
    json.getInt("total"),
    json.getLong("timeTakenMs"),
    json.getLong("timestamp")
)
