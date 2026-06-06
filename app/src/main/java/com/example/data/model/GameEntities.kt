package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlayerRank(val title: String, val levelRequired: Int) {
    SH_6("Шестёрка", 1),
    STRELOK("Стрелок", 5),
    BOETS("Боец", 10),
    TORPEDA("Торпеда", 15),
    SMOTRYASHY("Смотрящий за Районом", 20),
    AVTORITET("Авторитет", 25),
    BRIGADIR("Бригадир", 30),
    VOR_V_ZAKONE("Вор в Законе", 35),
    CRIMINAL_GENERAL("Криминальный Генерал", 40),
    KOROL_RAYONA("Король Района", 45),
    KHOZYAIN_GORODA("Хозяин Города", 49),
    PAKHAN("Пахан", 50);

    companion object {
        fun fromLevel(level: Int): PlayerRank {
            return values().findLast { level >= it.levelRequired } ?: SH_6
        }
    }
}

@Entity(tableName = "car_config")
data class CarConfig(
    @PrimaryKey val id: Int = 1, // Store single player garage config
    val cash: Int = 55000,       // Starting cash set to $55,000 as requested
    val engineLevel: Int = 1,    // Speed & acceleration, Max 5
    val tyresLevel: Int = 1,     // Handling & drift grip, Max 5
    val brakesLevel: Int = 1,    // Friction & stop power, Max 5
    val nitroLevel: Int = 1,     // Nitro capacity & fill rate, Max 5
    val carColor: Int = 0xFF18181C.toInt(), // Hex color of the car (default black Lada)

    // Slavic RPG Elements
    val nickname: String = "mikha_q",
    val level: Int = 1,
    val experience: Int = 0,
    val weaponLevel: Int = 0,    // 0 = None, 1 = PM Pistol, 2 = AK-74u, 3 = RPG-7
    val familyLevel: Int = 0,    // 0 = Alone, 1 = Gopnik Crew, 2 = Bratva Gang, 3 = Full Syndicate
    val targetWeather: String = "OVERCAST", // OVERCAST, RAIN, SNOW
    val godMode: Boolean = false,
    val bigSpoiler: Boolean = true, // Lada sports spoiler enabled
    val neonUnderglow: Boolean = true, // Neon green glow behind Lada
    val carModelIndex: Int = 0, // Index of the car from CarCatalog
    val graphicsQuality: String = "HIGH", // LOW, MEDIUM, HIGH
    val mapSizeSetting: String = "BIG",   // NORMAL, BIG, ULTRA
    val hasVipStatus: Boolean = false,    // VIP Deluxe status
    val hasFullAdminControl: Boolean = false, // Full Control level access
    val copDensitySetting: Int = 2, // 1 = Low, 2 = Normal, 3 = Heavy! (Жесть!)
    val tintLevel: Int = 0, // 0 = No tint, 1 = Rear 50%, 2 = Full 100% "В бункер"
    val suspensionHeight: Int = 1, // 0 = Slammed (Занижение), 1 = Standard, 2 = High (Вездеход)
    val neonColorHex: Long = 0xFF10B981 // Active neon color hex
)

data class CarModelDetail(
    val name: String,
    val price: Int,
    val description: String,
    val weightFactor: Double,
    val speedFactor: Double,
    val accelerationFactor: Double,
    val interiorType: Int, // 0 = Retro VAZ, 1 = Modern VAZ, 2 = Premium LADA, 3 = Heavy Truck, 4 = Premium Foreign/Sport
    val baseColorHex: Long = 0xFF18181C
)

object CarCatalog {
    val models: List<CarModelDetail> = listOf(
        CarModelDetail("ВАЗ-2106 «Шоха»", 0, "Легендарная советская классика. Задний привод.", 1.0, 0.90, 0.95, 0, 0xFF7F1D1D),
        CarModelDetail("ВАЗ-2114 «Четырка»", 120000, "Дерзкий пацанский хэтчбек. Бодрая передняя тяга.", 1.0, 1.05, 1.10, 1, 0xFF1E293B),
        CarModelDetail("LADA Priora «Сликер»", 350000, "Пневмоподвеска опущенная в пол, ксенон.", 0.95, 1.25, 1.20, 2, 0xFFD1D5DB),
        CarModelDetail("КАМАЗ-54115 «Громобой»", 1800000, "Сверхтяжёлый 3D грузовик! Легко таранит ДПС.", 3.5, 0.70, 0.85, 3, 0xFF1E3A8A),
        CarModelDetail("BMW E34 «Бумер ОПГ»", 4000000, "Бандитский спорт-седан. Максимальная скорость и заносы.", 1.25, 1.60, 1.55, 4, 0xFF020617),
        CarModelDetail("ГАЗ-24 «Волга Баржа»", 150000, "Советская Волга премиум-класса. Плавный и мягкий ход.", 1.4, 0.95, 0.90, 0, 0xFF111827),
        CarModelDetail("ВАЗ-1111 «Ока Смертник»", 80000, "Микро-кар. Забавная и экстремально легкая.", 0.5, 1.10, 1.30, 0, 0xFFCA8A04),
        CarModelDetail("ЗАЗ-968 «Запорожец Зверь»", 50000, "Народный заднемоторный болид со звонким выхлопом.", 0.6, 0.80, 0.85, 0, 0xFFD97706),
        CarModelDetail("ВАЗ-2107 «Семёрка Дрифт»", 180000, "Боевая классика для зимнего дрифта на районе.", 0.95, 1.00, 1.15, 0, 0xFFB91C1C),
        CarModelDetail("ВАЗ-2109 «Девятка Вишнёвая»", 220000, "Та самая вишневая девятка из культовой песни.", 1.0, 1.08, 1.05, 1, 0xFF991B1B),
        CarModelDetail("ГАЗ-31105 «Волга Люкс»", 280000, "Представительский седан. Тихий и престижный.", 1.45, 1.15, 1.00, 1, 0xFF3F3F46),
        CarModelDetail("УАЗ-452 «Буханка Спецназ»", 600000, "Вездеходный вечный фургон для спецмиссий.", 1.8, 0.85, 1.10, 3, 0xFF14532D),
        CarModelDetail("УАЗ Hunter «Козел»", 750000, "Внедорожник для жесткого бездорожья и болот.", 1.7, 0.90, 1.20, 3, 0xFF064E3B),
        CarModelDetail("LADA Niva 4x4 «Тайга»", 450000, "Легендарный советский внедорожник. Не боится грязи.", 1.2, 1.00, 1.25, 1, 0xFF047857),
        CarModelDetail("LADA Vesta Sport «Веста»", 1100000, "Современный гоночный болид LADA. Заниженная подвеска.", 1.15, 1.40, 1.30, 2, 0xFFEA580C),
        CarModelDetail("LADA Granta «Такси Вояж»", 500000, "Надежная рабочая лошадка. Экономичная на дорогах.", 1.0, 1.10, 1.10, 1, 0xFFEAB308),
        CarModelDetail("ГАЗель-3302 «Маршрутка»", 950000, "Передаем за проезд! Высокая посадка, просторный салон.", 2.5, 0.82, 0.90, 3, 0xFFD97706),
        CarModelDetail("ЗИЛ-130 «Кормилец»", 1400000, "Легендарный советский самосвал с ревущим V8 мотором.", 3.2, 0.80, 0.80, 3, 0xFF2563EB),
        CarModelDetail("ПАЗ-3205 «Пазик Ада»", 1600000, "Народный сельский автобус. Огромный таранный урон.", 3.8, 0.75, 0.75, 3, 0xFFD97706),
        CarModelDetail("Toyota Mark II «Самурай»", 2200000, "Правый руль, легендарный мотор 1JZ-GTE. Неуловимый занос.", 1.35, 1.65, 1.60, 4, 0xFFFFFFFF),
        CarModelDetail("Nissan Skyline R34 «Годзилла»", 3800000, "Полноприводный японский зверь из уличных гонок.", 1.4, 1.90, 1.75, 4, 0xFF2563EB),
        CarModelDetail("Subaru Impreza WRX STI", 3200000, "Раллийный оппозитник. Бубнящий выхлоп и синий окрас.", 1.35, 1.75, 1.80, 4, 0xFF1D4ED8),
        CarModelDetail("Mitsubishi Lancer Evolution IX", 3300000, "Символ трека с раллийным гоночным прошлым.", 1.38, 1.78, 1.82, 4, 0xFFDC2626),
        CarModelDetail("Mercedes-Benz W140 «Кабан»", 4500000, "Бронированный шестисотый S-класс прямиком из 90-х.", 2.2, 1.70, 1.50, 4, 0xFF18181B),
        CarModelDetail("Mercedes-Benz G63 AMG «Гелик»", 10000000, "Черный куб авторитетов. Сносит любые препятствия.", 2.6, 1.85, 1.40, 4, 0xFF09090B),
        CarModelDetail("Porsche 911 Turbo S «Порш»", 15000000, "Немецкая инженерная пуля. Запредельное ускорение.", 1.45, 2.30, 2.10, 4, 0xFFE11D48),
        CarModelDetail("Audi RS6 Avant «Злой Универсал»", 12000000, "Семейный монстр на 600 сил с полным приводом Quattro.", 1.9, 2.10, 1.95, 4, 0xFF4B5563),
        CarModelDetail("ВАЗ-2101 «Копейка Ретро»", 90000, "Первая модель Жигулей. Итальянское ретро-изящество.", 0.9, 0.95, 0.95, 0, 0xFF0284C7),
        CarModelDetail("ВАЗ-2105 «Пятерка»", 110000, "Строгий рубленый классический дизайн СССР.", 0.95, 0.98, 0.95, 0, 0xFFF1F5F9),
        CarModelDetail("ВАЗ-2108 «Восьмерка Зубило»", 170000, "Клиновидный хэтчбек. Послушный уличный боец.", 0.96, 1.06, 1.10, 1, 0xFF0891B2),
        CarModelDetail("ВАЗ-2110 «Червонец»", 200000, "Обтекаемый зализанный перед конца девяностых.", 1.05, 1.10, 1.12, 1, 0xFF52525B),
        CarModelDetail("ВАЗ-2112 «Двенашка»", 250000, "Самый популярный гоночный снаряд в Самаре.", 1.04, 1.15, 1.15, 1, 0xFF0F172A),
        CarModelDetail("LADA Kalina Sport «Ягодка»", 380000, "Компактная ракета. Заниженная спортивная подвеска.", 1.0, 1.25, 1.25, 2, 0xFFCA8A04),
        CarModelDetail("LADA Largus «Дачник»", 650000, "Вместительный универсал для любых грузов и рассады.", 1.35, 1.02, 1.10, 1, 0xFFD1D5DB),
        CarModelDetail("ГАЗ-13 «Чайка»", 5000000, "Советский правительственный лимузин. Статус госбезопасности.", 2.1, 1.40, 1.10, 0, 0xFF09090B),
        CarModelDetail("ГАЗ-21 «Волга Олень»", 2500000, "Раритет с хромированным оленем. Настоящее искусство.", 1.45, 1.10, 0.95, 0, 0xFF0284C7),
        CarModelDetail("КрАЗ-255 «Лаптежник»", 3000000, "Военный трехосный гигант. Деревянная кабина, небьющийся кузов.", 4.5, 0.72, 1.10, 3, 0xFF14532D),
        CarModelDetail("Урал-4320 «Штурм»", 3500000, "Тяжелый внедорожный армейский грузовик. Сокрушитель ДПС.", 4.2, 0.78, 1.15, 3, 0xFF166534),
        CarModelDetail("БЕЛАЗ-75710 «Карьерный Монстр»", 50000000, "Гигантский карьерный самосвал весом в 450 тонн. Легенда!", 12.0, 0.65, 0.80, 3, 0xFFEAB308),
        CarModelDetail("Daewoo Nexia «Узбечка»", 150000, "Работящая и неубиваемая легенда всех таксистов.", 1.1, 1.12, 1.05, 1, 0xFFE2E8F0),
        CarModelDetail("Hyundai Solaris «Поло-Убийца»", 900000, "Звезда каршеринга. Юркий, динамичный, маневренный.", 1.15, 1.35, 1.35, 2, 0xFFD1D5DB),
        CarModelDetail("Kia Rio «Рент-а-Кар»", 920000, "Современный городской хэтчбек со стильным салоном.", 1.15, 1.36, 1.37, 2, 0xFF475569),
        CarModelDetail("Ford Focus 2 «Кредитный Фокус»", 600000, "Эталон мягкого комфорта. Самый любимый хэтчбек в РФ.", 1.3, 1.30, 1.30, 4, 0xFF1D4ED8),
        CarModelDetail("Chevrolet Lacetti «Надежный»", 450000, "Корейский американец с тихим мотором и плавным ходом.", 1.25, 1.25, 1.22, 1, 0xFF94A3B8),
        CarModelDetail("Volkswagen Polo Sedan", 880000, "Строгий немецкий седан с великолепным держаком.", 1.2, 1.38, 1.38, 4, 0xFF111827),
        CarModelDetail("Renault Logan «Неубиваемый»", 400000, "Внедорожный седан. Мягкая и вечная подвеска.", 1.1, 1.15, 1.15, 1, 0xFF78350F),
        CarModelDetail("Honda Civic Type R", 2800000, "Спортивный турбо-хэтчбек на 320 сил c VTEC задором.", 1.3, 1.82, 1.70, 4, 0xFFFFFFFF),
        CarModelDetail("Nissan GT-R R35 «Годзилла II»", 9000000, "Уличный истребитель на полном приводе. Бешеный старт.", 1.6, 2.25, 2.15, 4, 0xFFB91C1C),
        CarModelDetail("BMW M5 E60 «Воющий V10»", 6000000, "Легендарный седан с атмосферным мотором V10 под капотом.", 1.75, 2.05, 1.80, 4, 0xFF1E293B),
        CarModelDetail("Mercedes-Benz E500 «Волчок»", 4800000, "Шедевр из 90х построенный на заводе Porsche. Чистый кайф.", 1.7, 1.88, 1.75, 4, 0xFF0F172A),
        CarModelDetail("Lexus LX570 «Император»", 11000000, "Премиальный японский джип-крепость. Гроза барьерных колец.", 2.8, 1.75, 1.50, 4, 0xFFF1F5F9),
        CarModelDetail("ВАЗ-2104 «Боевая Дача»", 140000, "Рабоче-крестьянский дрифт-вагон. Огромный багажник для трофеев.", 1.10, 1.05, 1.15, 0, 0xFF4338CA),
        CarModelDetail("BMW M5 F90 «Гроза Города»", 18500000, "Полноприводная ракета на 600+ л.с. Властелин уличного трафика.", 1.85, 2.50, 2.30, 4, 0xFF111827),
        CarModelDetail("Bugatti Chiron «Гиперкар»", 100000000, "Король скорости и роскоши. 1500 сил, разгон за секунды!", 1.9, 2.80, 2.50, 4, 0xFF1D4ED8)
    )
}

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val durationSeconds: Int,
    val heatLevel: Int,
    val escaped: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
