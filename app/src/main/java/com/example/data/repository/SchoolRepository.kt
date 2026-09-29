package com.example.data.repository

import com.example.data.models.ChatMessage
import com.example.data.models.MessageSenderRole
import com.example.data.models.MessageStatus
import com.example.data.models.School

class SchoolRepository {

    val schools: List<School> = listOf(
        // 1. Australia
        School(
            id = "ilsc_melbourne",
            name = "ILSC Language Schools Melbourne",
            city = "Melbourne",
            country = "Australia",
            flagEmoji = "🇦🇺",
            accreditation = "NEAS Quality Endorsed & English Australia",
            rating = 4.9,
            reviewsCount = 428,
            priceWeeklyUsd = 310.0,
            representativeName = "Liam O'Connor",
            representativeRole = "Admissions Director - LatAm & Perú",
            representativeOnline = true,
            representativeTimezone = "Melbourne (AEST, UTC+10)",
            popularPrograms = listOf("General English (24h/sem)", "Cambridge Mastery (FCE/CAE)", "English for Hospitality"),
            visaAllowsWork = true,
            workDetails = "Permiso de trabajo legal hasta 48 horas por quincena en Australia con visa de estudiante Subclass 500.",
            description = "Campus moderno en el CBD de Melbourne con estación Flinders Street a 3 minutos. Amplia comunidad de estudiantes peruanos y asesoría para empleo local.",
            latitude = -37.8180,
            longitude = 144.9568,
            address = "120 Spencer St, Melbourne VIC 3000, Australia"
        ),
        School(
            id = "greenwich_sydney",
            name = "Greenwich English College Sydney",
            city = "Sídney",
            country = "Australia",
            flagEmoji = "🇦🇺",
            accreditation = "NEAS Quality & IALC Accredited",
            rating = 4.88,
            reviewsCount = 375,
            priceWeeklyUsd = 325.0,
            representativeName = "Chloe Bennett",
            representativeRole = "Student Enrolment Specialist",
            representativeOnline = true,
            representativeTimezone = "Sydney (AEST, UTC+10)",
            popularPrograms = listOf("English for Business", "IELTS Preparation Exam", "Pronunciation in Context"),
            visaAllowsWork = true,
            workDetails = "Trabajo legal hasta 48 horas quincenales durante período lectivo.",
            description = "Ubicada en Pitt Street en pleno corazón de Sídney, a pasos del puerto y de la Opera House.",
            latitude = -33.8749,
            longitude = 151.2069,
            address = "396 Pitt St, Haymarket NSW 2000, Australia"
        ),

        // 2. Canadá
        School(
            id = "ilac_toronto",
            name = "ILAC - International Language Academy of Canada",
            city = "Toronto",
            country = "Canadá",
            flagEmoji = "🇨🇦",
            accreditation = "Languages Canada & ALTO",
            rating = 4.85,
            reviewsCount = 590,
            priceWeeklyUsd = 295.0,
            representativeName = "Sophie Tremblay",
            representativeRole = "Senior Student Advisor - Latin America",
            representativeOnline = true,
            representativeTimezone = "Toronto (EDT, UTC-4)",
            popularPrograms = listOf("University Pathway Program", "General Intensive English", "Power English 38 lecciones"),
            visaAllowsWork = false,
            workDetails = "Convenios con más de 90 colleges canadienses para postulación directa sin examen IELTS/TOEFL.",
            description = "Una de las academias más galardonadas del mundo con sedes boutique en Bloor-Yorkville. Asesoría para transición a colleges públicos canadienses.",
            latitude = 43.6702,
            longitude = -79.3868,
            address = "920 Yonge St, 4th Floor, Toronto, ON M4W 3C7, Canada"
        ),
        School(
            id = "oxford_vancouver",
            name = "Oxford International English School Gastown",
            city = "Vancouver",
            country = "Canadá",
            flagEmoji = "🇨🇦",
            accreditation = "Languages Canada & BC EQA",
            rating = 4.78,
            reviewsCount = 264,
            priceWeeklyUsd = 285.0,
            representativeName = "David Miller",
            representativeRole = "Admissions Manager - South America",
            representativeOnline = true,
            representativeTimezone = "Vancouver (PDT, UTC-7)",
            popularPrograms = listOf("General English Fluency", "Morning Intensive 30", "Canadian College Pathway"),
            visaAllowsWork = false,
            workDetails = "Apoyo en búsqueda de alojamiento en homestays seleccionados con familias canadienses.",
            description = "Ubicada en Gastown, Vancouver, rodeada de montañas y el océano Pacífico. Clases dinámicas con un promedio de 12 alumnos por aula.",
            latitude = 49.2849,
            longitude = -123.1118,
            address = "250-815 W Hastings St, Vancouver, BC V6C 1B4, Canada"
        ),

        // 3. USA
        School(
            id = "ec_new_york",
            name = "EC English Language Centres Times Square",
            city = "New York",
            country = "USA",
            flagEmoji = "🇺🇸",
            accreditation = "ACCET & English USA",
            rating = 4.8,
            reviewsCount = 312,
            priceWeeklyUsd = 390.0,
            representativeName = "Marcus Vance",
            representativeRole = "International Admissions Coordinator",
            representativeOnline = true,
            representativeTimezone = "New York (EDT, UTC-4)",
            popularPrograms = listOf("English for Global Careers", "Academic English I-20", "TOEFL Prep Intensive"),
            visaAllowsWork = false,
            workDetails = "Emisión expedita de formulario I-20 para visa de estudiante F-1 en embajada de EE.UU. en Lima.",
            description = "Ubicación inmejorable en pleno Broadway & Times Square con vista a la Gran Manzana. Programa de inmersión cultural y talleres de networking profesional.",
            latitude = 40.7589,
            longitude = -73.9851,
            address = "1450 Broadway 27th Floor, New York, NY 10018, USA"
        ),
        School(
            id = "kings_los_angeles",
            name = "Kings Education Hollywood Los Angeles",
            city = "Los Ángeles",
            country = "USA",
            flagEmoji = "🇺🇸",
            accreditation = "ACCET & English USA",
            rating = 4.82,
            reviewsCount = 245,
            priceWeeklyUsd = 375.0,
            representativeName = "Jessica Taylor",
            representativeRole = "Admissions Director California",
            representativeOnline = true,
            representativeTimezone = "Los Angeles (PDT, UTC-7)",
            popularPrograms = listOf("English Plus Film & Arts", "Intensive Course 28 lessons", "Vacation Plus"),
            visaAllowsWork = false,
            workDetails = "Soporte completo con visa F-1 y convenios con universidades estadounidenses.",
            description = "En el corazón de Hollywood, a cuadras del Paseo de la Fama y estudios de cine. Excelente ambiente californiano.",
            latitude = 34.0989,
            longitude = -118.3267,
            address = "1555 Cassil Pl, Los Angeles, CA 90028, USA"
        ),

        // 4. England
        School(
            id = "kaplan_london",
            name = "Kaplan International Languages Covent Garden",
            city = "Londres",
            country = "England",
            flagEmoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            accreditation = "British Council & English UK",
            rating = 4.9,
            reviewsCount = 475,
            priceWeeklyUsd = 360.0,
            representativeName = "Emma Harrington",
            representativeRole = "Head of Global Enrolments",
            representativeOnline = true,
            representativeTimezone = "London (BST, UTC+1)",
            popularPrograms = listOf("Intensive Academic Semester", "IELTS Exam Preparation", "Business English London"),
            visaAllowsWork = false,
            workDetails = "Sin necesidad de visa previa para peruanos en estancias menores a 6 meses (Standard Visitor).",
            description = "Elegante edificio histórico del siglo XVIII en el vibrante barrio de Covent Garden. Metodología K+ blended learning con certificación internacional.",
            latitude = 51.5129,
            longitude = -0.1243,
            address = "3-4 Southampton Pl, London WC1A 2DA, UK"
        ),
        School(
            id = "bell_cambridge",
            name = "Bell Cambridge English Academy",
            city = "Cambridge",
            country = "England",
            flagEmoji = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
            accreditation = "British Council, EAQUALS & English UK",
            rating = 4.93,
            reviewsCount = 310,
            priceWeeklyUsd = 385.0,
            representativeName = "Oliver Ward",
            representativeRole = "Academic Director of Studies",
            representativeOnline = true,
            representativeTimezone = "Cambridge (BST, UTC+1)",
            popularPrograms = listOf("Cambridge English Fluency", "University Foundation Programme", "IELTS Elite"),
            visaAllowsWork = false,
            workDetails = "Estancias de hasta 6 meses sin visa consular para ciudadanos de Perú.",
            description = "Tradición académica en campus privado con jardines victorianos, laboratorios multimedia y excelencia pedagógica británica.",
            latitude = 52.1812,
            longitude = 0.1416,
            address = "Red Cross Ln, Cambridge CB2 0QU, UK"
        ),

        // 5. New Zealand
        School(
            id = "nzlc_auckland",
            name = "NZLC - New Zealand Language Centres Auckland",
            city = "Auckland",
            country = "New Zealand",
            flagEmoji = "🇳🇿",
            accreditation = "NZQA Category 1 & English New Zealand",
            rating = 4.91,
            reviewsCount = 330,
            priceWeeklyUsd = 290.0,
            representativeName = "Kiri Te Kanawa",
            representativeRole = "International Student Manager",
            representativeOnline = true,
            representativeTimezone = "Auckland (NZST, UTC+12)",
            popularPrograms = listOf("General English (Full-time)", "IELTS / Cambridge Exam Prep", "Business English"),
            visaAllowsWork = true,
            workDetails = "Permiso de trabajo legal 20 horas por semana en Nueva Zelanda con visa de estudiante en escuelas Categoría 1.",
            description = "Sede premiada 5 veces consecutivas con el STM Star Award en el Viaduct Harbour de Auckland. Alta calidad de vida y seguridad garantizada.",
            latitude = -36.8456,
            longitude = 174.7645,
            address = "Level 2, 104 Customs St West, Auckland 1010, New Zealand"
        ),
        School(
            id = "southern_lakes_queenstown",
            name = "Southern Lakes English College Queenstown",
            city = "Queenstown",
            country = "New Zealand",
            flagEmoji = "🇳🇿",
            accreditation = "NZQA Category 1 Quality Assured",
            rating = 4.86,
            reviewsCount = 195,
            priceWeeklyUsd = 305.0,
            representativeName = "Hamish Fraser",
            representativeRole = "Admissions & Activities Lead",
            representativeOnline = true,
            representativeTimezone = "Queenstown (NZST, UTC+12)",
            popularPrograms = listOf("English + Ski / Snowboard", "General English 25h", "Working Holiday Support"),
            visaAllowsWork = true,
            workDetails = "Permiso de trabajo legal 20 horas/semana y oportunidades abundantes en hotelería y turismo.",
            description = "Situada en la capital mundial de los deportes de aventura entre el lago Wakatipu y Los Remarcables.",
            latitude = -45.0312,
            longitude = 168.6626,
            address = "57 Shotover St, Queenstown 9300, New Zealand"
        ),

        // 6. Malta
        School(
            id = "ec_malta",
            name = "EC English Language Centres St. Julian's",
            city = "St. Julian's",
            country = "Malta",
            flagEmoji = "🇲🇹",
            accreditation = "FELTOM & Ministry of Education Malta",
            rating = 4.75,
            reviewsCount = 340,
            priceWeeklyUsd = 240.0,
            representativeName = "Maria Borg",
            representativeRole = "International Representative",
            representativeOnline = true,
            representativeTimezone = "Malta (CEST, UTC+2)",
            popularPrograms = listOf("Intensive English + Summer Club", "General English 20", "English for Career"),
            visaAllowsWork = true,
            workDetails = "Permiso de trabajo legal tras la semana 13 de estudios en territorio Schengen maltés.",
            description = "Destino paradisíaco en el Mediterráneo con clima soleado todo el año. Costos accesibles de vida y excelente ambiente multicultural.",
            latitude = 35.9221,
            longitude = 14.4883,
            address = "Marguerite May Alfieri Street, St Julian's STJ 3042, Malta"
        ),
        School(
            id = "iels_sliema",
            name = "IELS Institute of English Language Studies Sliema",
            city = "Sliema",
            country = "Malta",
            flagEmoji = "🇲🇹",
            accreditation = "FELTOM Accredited & ALTO",
            rating = 4.81,
            reviewsCount = 280,
            priceWeeklyUsd = 230.0,
            representativeName = "Gianluca Camilleri",
            representativeRole = "Admissions Coordinator Latin America",
            representativeOnline = true,
            representativeTimezone = "Malta (CEST, UTC+2)",
            popularPrograms = listOf("General English Intensive 30", "Business English", "Cambridge Exam Prep"),
            visaAllowsWork = true,
            workDetails = "Permiso para trabajar 20 horas por semana a partir de los 90 días de curso.",
            description = "A metros del paseo marítimo de Sliema, con residencia estudiantil propia y terrazas al sol.",
            latitude = 35.9122,
            longitude = 14.5042,
            address = "Mattew Pulis Street, Sliema SLM 3052, Malta"
        ),

        // 7. Irlanda
        School(
            id = "ces_dublin",
            name = "Centre of English Studies (CES) Dublin",
            city = "Dublín",
            country = "Irlanda",
            flagEmoji = "🇮🇪",
            accreditation = "ACELS & EAQUALS Member",
            rating = 4.88,
            reviewsCount = 380,
            priceWeeklyUsd = 270.0,
            representativeName = "Sean Murphy",
            representativeRole = "Student Services & Visa Specialist",
            representativeOnline = true,
            representativeTimezone = "Dublin (IST, UTC+1)",
            popularPrograms = listOf("25-Week Work & Study Ireland", "Standard General English", "IELTS Express"),
            visaAllowsWork = true,
            workDetails = "Permiso de trabajo legal 20h/sem (40h/sem en vacaciones) con programa ILEP 25 semanas.",
            description = "Campus frente al Trinity College en Dame Street, Dublín. Ideal para estudiantes peruanos que buscan trabajar legalmente en Europa mientras perfeccionan su inglés.",
            latitude = 53.3441,
            longitude = -6.2647,
            address = "31 Dame St, Dublin 2, D02 EE86, Ireland"
        ),
        School(
            id = "atlantic_galway",
            name = "Atlantic Language Galway",
            city = "Galway",
            country = "Irlanda",
            flagEmoji = "🇮🇪",
            accreditation = "EAQUALS, ACELS & IALC Member",
            rating = 4.84,
            reviewsCount = 260,
            priceWeeklyUsd = 260.0,
            representativeName = "Ciara Kelly",
            representativeRole = "Work & Study Advisor",
            representativeOnline = true,
            representativeTimezone = "Galway (IST, UTC+1)",
            popularPrograms = listOf("Work and Study 25 weeks", "General English 20", "Cambridge CAE"),
            visaAllowsWork = true,
            workDetails = "Permiso oficial de trabajo irlandés de 20h/semana en la ciudad cultural de Galway.",
            description = "Campus moderno galardonado como mejor escuela de idiomas de Europa, cerca de la costa atlántica de Irlanda.",
            latitude = 53.2743,
            longitude = -9.0490,
            address = "Fairgreen House, Fairgreen Rd, Galway, H91 AXK8, Ireland"
        ),

        // 8. Sudáfrica
        School(
            id = "good_hope_capetown",
            name = "Good Hope Studies City Centre",
            city = "Ciudad del Cabo",
            country = "Sudáfrica",
            flagEmoji = "🇿🇦",
            accreditation = "IALC & Education South Africa (EduSA)",
            rating = 4.82,
            reviewsCount = 210,
            priceWeeklyUsd = 220.0,
            representativeName = "Zola Ndlovu",
            representativeRole = "Admissions & Volunteer Coordinator",
            representativeOnline = true,
            representativeTimezone = "Cape Town (SAST, UTC+2)",
            popularPrograms = listOf("English + Safari & Volunteer", "General English Morning", "Business Fluency"),
            visaAllowsWork = false,
            workDetails = "Costos de vida sumamente económicos para estudiantes de América Latina.",
            description = "Experiencia transformadora a los pies de Table Mountain en Sudáfrica. Combina clases de inglés intensivo con safaris y proyectos ecológicos.",
            latitude = -33.9249,
            longitude = 18.4241,
            address = "5 St Georges Mall, Cape Town City Centre, Cape Town, 8001, South Africa"
        ),
        School(
            id = "ih_cape_town",
            name = "International House Cape Town Sea Point",
            city = "Ciudad del Cabo",
            country = "Sudáfrica",
            flagEmoji = "🇿🇦",
            accreditation = "International House World Organisation & EduSA",
            rating = 4.79,
            reviewsCount = 185,
            priceWeeklyUsd = 215.0,
            representativeName = "Thabo Mbeki",
            representativeRole = "Student Enrolment Lead",
            representativeOnline = true,
            representativeTimezone = "Cape Town (SAST, UTC+2)",
            popularPrograms = listOf("Standard English 20", "English for Safari Guide", "Intensive Fluency"),
            visaAllowsWork = false,
            workDetails = "Sin requerimiento de visa consular para estadías turísticas de hasta 90 días.",
            description = "Frente al paseo costero de Sea Point con vistas espectaculares al Atlántico y actividades al aire libre.",
            latitude = -33.9144,
            longitude = 18.3888,
            address = "225 Main Rd, Sea Point, Cape Town, 8005, South Africa"
        )
    )

    fun getInitialChatMessages(school: School): List<ChatMessage> {
        val now = System.currentTimeMillis()
        val fifteenMinAgo = now - (15 * 60 * 1000)
        val tenMinAgo = now - (10 * 60 * 1000)
        val fiveMinAgo = now - (5 * 60 * 1000)

        return listOf(
            ChatMessage(
                id = "msg_${school.id}_1",
                channelId = "chat_${school.id}",
                schoolId = school.id,
                senderId = "rep_${school.id}",
                senderName = school.representativeName,
                senderRole = MessageSenderRole.REPRESENTATIVE,
                text = "¡Hola! Soy ${school.representativeName}, ${school.representativeRole} en ${school.name}. Me alegra mucho saludarte desde ${school.city}, ${school.country}. ¿Tienes alguna meta específica en mente para tu curso de inglés en 2026?",
                timestamp = fifteenMinAgo,
                status = MessageStatus.READ
            ),
            ChatMessage(
                id = "msg_${school.id}_2",
                channelId = "chat_${school.id}",
                schoolId = school.id,
                senderId = "rep_${school.id}",
                senderName = school.representativeName,
                senderRole = MessageSenderRole.REPRESENTATIVE,
                text = "Te comparto el brochure oficial con la lista de fechas de inicio y promociones exclusivas para estudiantes de Perú 🇵🇪:",
                timestamp = tenMinAgo,
                status = MessageStatus.READ,
                attachmentTitle = "📄 Brochure Oficial 2026 - ${school.name}.pdf",
                attachmentType = "PDF_BROCHURE"
            ),
            ChatMessage(
                id = "msg_${school.id}_3",
                channelId = "chat_${school.id}",
                schoolId = school.id,
                senderId = "rep_${school.id}",
                senderName = school.representativeName,
                senderRole = MessageSenderRole.REPRESENTATIVE,
                text = "Estamos ubicados en ${school.address}. Puedes consultarme cualquier duda sobre visado, alojamiento o programas de estudio.",
                timestamp = fiveMinAgo,
                status = MessageStatus.READ
            )
        )
    }

    fun generateRepresentativeReply(school: School, studentQuery: String): ChatMessage {
        val lower = studentQuery.lowercase()
        val replyText = when {
            lower.contains("visa") || lower.contains("visado") -> {
                if (school.visaAllowsWork) {
                    "Para ${school.country}, los estudiantes peruanos cuentan con excelentes facilidades de visa. ${school.workDetails} Nosotros emitimos la carta oficial de aceptación para tu trámite consular."
                } else {
                    "Para estudiar en ${school.country} (${school.city}), te emitimos toda la documentación oficial requerida por la embajada. ${school.workDetails} Te brindamos asesoría paso a paso."
                }
            }
            lower.contains("precio") || lower.contains("costo") || lower.contains("cotizar") || lower.contains("cuanto") -> {
                "Nuestra tarifa preferencial para estudiantes peruanos es de $${school.priceWeeklyUsd.toInt()} USD por semana (aprox. S/. ${(school.priceWeeklyUsd * 3.75).toInt()}). Incluye prueba de nivel, material digital y certificado de culminación."
            }
            lower.contains("trabaj") || lower.contains("chamba") || lower.contains("empleo") -> {
                if (school.visaAllowsWork) {
                    "¡Sí! ${school.workDetails} Nuestro campus cuenta con un Job Club gratuito que te ayuda a redactar tu CV local y prepararte para entrevistas."
                } else {
                    "En ${school.country}, los cursos de inglés estándar están enfocados al 100% en el avance académico rápido. Muchos estudiantes combinan esto con programas Pathway para ingresar a colleges donde sí obtienen permiso de trabajo."
                }
            }
            lower.contains("curso") || lower.contains("programa") || lower.contains("clase") -> {
                "Ofrecemos ${school.popularPrograms.joinToString(", ")}. Contamos con inicios todos los lunes del año y niveles desde principiante (A1) hasta avanzado (C2)."
            }
            lower.contains("donde") || lower.contains("ubicac") || lower.contains("mapa") || lower.contains("direccion") -> {
                "Nuestra sede principal está en ${school.address}. Estamos muy bien comunicados con transporte público, residencias estudiantiles y cafeterías."
            }
            else -> {
                "¡Excelente consulta! En ${school.name} (${school.city}) estaremos encantados de recibirte. Nuestro equipo de admisiones te acompaña con el alojamiento, seguro médico y bienvenida en el aeropuerto."
            }
        }

        return ChatMessage(
            id = "rep_reply_${System.currentTimeMillis()}",
            channelId = "chat_${school.id}",
            schoolId = school.id,
            senderId = "rep_${school.id}",
            senderName = school.representativeName,
            senderRole = MessageSenderRole.REPRESENTATIVE,
            text = replyText,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
    }
}
