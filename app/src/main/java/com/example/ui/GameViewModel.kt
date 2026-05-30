package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameDatabase
import com.example.data.model.CarConfig
import com.example.data.model.ScoreRecord
import com.example.data.model.PlayerRank
import com.example.data.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

enum class GameStatus {
    MENU, GAMEPLAY, BUSTED, ESCAPED
}

enum class ObstacleType {
    BUILDING, WATER, TREE
}

data class Obstacle(
    val rect: Rect,
    val type: ObstacleType,
    // Specifying height or texture info for the 3D projection appearance
    val name: String = "Панелька"
)

enum class ItemType {
    COIN, REPAIR, NITRO
}

data class GameItem(
    val id: Int,
    val x: Double,
    val y: Double,
    val type: ItemType,
    var isCollected: Boolean = false
)

data class CopCar(
    val id: Int,
    var x: Double,
    var y: Double,
    var vx: Double = 0.0,
    var vy: Double = 0.0,
    var speed: Double = 0.0,
    var angle: Double = 0.0,
    var health: Double = 100.0,
    var isStruck: Boolean = false,
    var isStuckFrames: Int = 0
)

data class Bullet(
    val id: Int,
    var x: Double,
    var y: Double,
    val vx: Double,
    val vy: Double,
    val damage: Double,
    var isPlayerOwned: Boolean = true
)

data class DriftPoint(
    val x: Float,
    val y: Float,
    val alpha: Float = 0.6f
)

// Particle effect simulation for rain/snow rendering in Canvas
data class WeatherParticle(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val angle: Float
)

data class GameState(
    val playerX: Double = 2000.0,
    val playerY: Double = 2000.0,
    val playerAngle: Double = 0.0, // in radians
    val playerSpeed: Double = 0.0,
    val playerHealth: Double = 100.0,
    val nitroAmount: Double = 100.0,
    val cashEarned: Int = 0,
    val score: Int = 0,
    val heatLevel: Int = 1,
    val heatProgress: Float = 0.0f,
    val copCars: List<CopCar> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val items: List<GameItem> = emptyList(),
    val status: GameStatus = GameStatus.MENU,
    val escapeCountdown: Int = 5,
    val bustedProgress: Float = 0.0f, // 0 to 1
    val driftTrails: List<DriftPoint> = emptyList(),
    val copsActive: Boolean = false,
    val weatherParticles: List<WeatherParticle> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository: GameRepository

    val carConfigState: StateFlow<CarConfig>
    val scoreHistoryState: StateFlow<List<ScoreRecord>>

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Map properties
    val mapSize = 4000.0
    val obstacles = mutableListOf<Obstacle>()
    private var random = Random()

    // Physics parameters (based on upgrades)
    var maxSpeed = 12.0
    var acceleration = 0.25
    var gripFactor = 0.15 // lower means more slide / drift
    var brakingForce = 0.35
    var maxNitro = 100.0
    var currentCarColor = 0xFF18181C.toInt()

    // User input controls
    var steerInput = 0.0  // -1.0 left to 1.0 right
    var accelInput = 0.0  // 0.0 (idle) to 1.0 (accelerate)
    var brakeInput = 0.0  // 0.0 to 1.0
    var nitroActive = false

    private var gameLoopJob: kotlinx.coroutines.Job? = null
    private var copIdCounter = 0
    private var itemIdCounter = 0
    private var bulletIdCounter = 0

    // Weapons / Fighting state variables
    private var shootCooldownTicks = 0
    
    // MODERN DEVELOPER ADMIN CONSOLE CHEAT MODIFIERS
    var adminSpeedMultiplier = 1.0
    var copsDumbMode = false
    var copsTurboMode = false
    var infiniteNitro = false
    var infiniteAmmo = false
    private val shootMaxCooldown = 15 // ticks between shots

    init {
        val db = GameDatabase.getDatabase(application)
        repository = GameRepository(db.carConfigDao(), db.scoreRecordDao())

        carConfigState = repository.carConfig.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CarConfig()
        )

        scoreHistoryState = repository.allScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Generate city obstacles procedurally with Soviet panel houses (панельки) and garages (Тюнинг-Про)
        generateMap()

        // Apply tuning physics whenever configuration changes
        viewModelScope.launch {
            carConfigState.collect { config ->
                applyPhysicsUpgrades(config)
            }
        }
    }

    private fun generateMap() {
        // Map division is 5x5 sectors centered around coordinates
        // Avenues are located at multiples of 800.
        obstacles.clear()
        for (i in 0..4) {
            for (j in 0..4) {
                val blockCenterX = i * 800.0 + 400.0
                val blockCenterY = j * 800.0 + 400.0

                // Do not put obstacles directly at the player's initial spawning area (2000, 2000)
                if (abs(blockCenterX - 2000.0) < 100.0 && abs(blockCenterY - 2000.0) < 100.0) {
                    continue
                }

                val pattern = (i + j) % 3
                when (pattern) {
                    0 -> {
                        // Two high concrete block buildings (панельки)
                        obstacles.add(
                            Obstacle(
                                Rect(
                                    (blockCenterX - 240.0).toFloat(),
                                    (blockCenterY - 240.0).toFloat(),
                                    (blockCenterX - 60.0).toFloat(),
                                    (blockCenterY + 240.0).toFloat()
                                ), ObstacleType.BUILDING, "Панелька Хрущевка"
                            )
                        )
                        // Add a Car Service Workshop target zone: "Тюнинг-Про" at (blockCenterX + 160.0, blockCenterY - 40.0)
                        obstacles.add(
                            Obstacle(
                                Rect(
                                    (blockCenterX + 80.0).toFloat(),
                                    (blockCenterY - 140.0).toFloat(),
                                    (blockCenterX + 240.0).toFloat(),
                                    (blockCenterY + 140.0).toFloat()
                                ), ObstacleType.BUILDING, if (i == 1 && j == 2) "Тюнинг-Про Автосервис" else "Гаражный Кооператив"
                            )
                        )
                    }
                    1 -> {
                        // Massive 9-story apartment complex
                        obstacles.add(
                            Obstacle(
                                Rect(
                                    (blockCenterX - 180.0).toFloat(),
                                    (blockCenterY - 180.0).toFloat(),
                                    (blockCenterX + 180.0).toFloat(),
                                    (blockCenterY + 180.0).toFloat()
                                ), ObstacleType.BUILDING, "9-Этажка Панельная"
                            )
                        )
                    }
                    2 -> {
                        // Dark frozen pond / lake (Solid water)
                        obstacles.add(
                            Obstacle(
                                Rect(
                                    (blockCenterX - 150.0).toFloat(),
                                    (blockCenterY - 150.0).toFloat(),
                                    (blockCenterX + 150.0).toFloat(),
                                    (blockCenterY + 150.0).toFloat()
                                ), ObstacleType.WATER, "Замерзшее Озеро"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun applyPhysicsUpgrades(config: CarConfig) {
        // Normal tuning physics
        maxSpeed = 10.0 + (config.engineLevel - 1) * 1.5 // 10.0 to 16.0 px/frame
        acceleration = 0.18 + (config.engineLevel - 1) * 0.05 // 0.18 to 0.38
        gripFactor = 0.08 + (config.tyresLevel - 1) * 0.04 // 0.08 to 0.24
        brakingForce = 0.25 + (config.brakesLevel - 1) * 0.08
        maxNitro = 100.0 + (config.nitroLevel - 1) * 20.0
        currentCarColor = config.carColor

        // Apply weather sliding influence directly
        when (config.targetWeather) {
            "RAIN" -> {
                gripFactor *= 0.70 // slippery road surface
            }
            "SNOW" -> {
                gripFactor *= 0.40 // extremely slippery ice & snow surface
                acceleration *= 0.90
            }
        }
    }

    fun startNewGame() {
        steerInput = 0.0
        accelInput = 0.0
        brakeInput = 0.0
        nitroActive = false
        copIdCounter = 0
        itemIdCounter = 0
        bulletIdCounter = 0
        shootCooldownTicks = 0

        // Create customized visual elements depending on active weather
        val activeWeather = carConfigState.value.targetWeather
        val initParticles = createWeatherParticles(activeWeather)

        _gameState.value = GameState(
            playerX = 2000.0,
            playerY = 2000.0,
            playerAngle = -Math.PI / 2, // North
            playerSpeed = 0.0,
            playerHealth = 100.0,
            nitroAmount = maxNitro,
            cashEarned = 0,
            score = 0,
            heatLevel = 1,
            heatProgress = 0.0f,
            copCars = emptyList(),
            bullets = emptyList(),
            items = createInitialItems(),
            status = GameStatus.GAMEPLAY,
            escapeCountdown = 5,
            bustedProgress = 0.0f,
            driftTrails = emptyList(),
            copsActive = false,
            weatherParticles = initParticles
        )

        gameLoopJob?.cancel()

        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTick = System.currentTimeMillis()
            var secondAccumulator = 0L
            var gameSeconds = 0

            while (_gameState.value.status == GameStatus.GAMEPLAY) {
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now

                updateGamePhysics()

                secondAccumulator += delta
                if (secondAccumulator >= 1000L) {
                    secondAccumulator -= 1000L
                    gameSeconds++
                    handlePerSecondTimer()
                    // Passive cash from family crew level!
                    earnPassiveFamilyCash()
                }

                delay(16)
            }
        }
    }

    private fun createWeatherParticles(weatherType: String): List<WeatherParticle> {
        val count = if (weatherType == "OVERCAST") 10 else 60
        val plist = mutableListOf<WeatherParticle>()
        for (i in 0 until count) {
            plist.add(
                WeatherParticle(
                    x = random.nextFloat() * 1200f - 200f,
                    y = random.nextFloat() * 2000f - 200f,
                    speed = if (weatherType == "RAIN") 18f + random.nextFloat() * 8f else 4f + random.nextFloat() * 3f,
                    size = if (weatherType == "RAIN") 1f + random.nextFloat() * 2f else 3f + random.nextFloat() * 4f,
                    angle = if (weatherType == "RAIN") 1.4f else 0.8f + random.nextFloat() * 0.4f
                )
            )
        }
        return plist
    }

    // Passive cash from crew (familyLevel)
    private fun earnPassiveFamilyCash() {
        val fLvl = carConfigState.value.familyLevel
        if (fLvl > 0) {
            val amount = fLvl * 15 // 15₽/30₽/45₽ cash per second from the lads running districts
            _gameState.value = _gameState.value.copy(
                cashEarned = _gameState.value.cashEarned + amount
            )
        }
    }

    private fun createInitialItems(): List<GameItem> {
        val list = mutableListOf<GameItem>()
        for (i in 1..35) {
            val isRoad = random.nextBoolean()
            var x = 0.0
            var y = 0.0
            if (isRoad) {
                val avenueIdx = random.nextInt(5)
                val avenueVal = avenueIdx * 800.0 + 400.0
                val alongAvenue = random.nextDouble() * mapSize
                if (random.nextBoolean()) {
                    x = avenueVal + (random.nextDouble() * 120.0 - 60.0)
                    y = alongAvenue
                } else {
                    x = alongAvenue
                    y = avenueVal + (random.nextDouble() * 120.0 - 60.0)
                }
            } else {
                x = 100.0 + random.nextDouble() * 3800.0
                y = 100.0 + random.nextDouble() * 3800.0
            }

            x = x.coerceIn(50.0, mapSize - 50.0)
            y = y.coerceIn(50.0, mapSize - 50.0)

            if (isCollidingWithObstacle(x, y, 10.0)) {
                x = 2000.0 + (random.nextDouble() * 1000.0 - 500.0)
                y = 2000.0 + (random.nextDouble() * 1000.0 - 500.0)
            }

            val itemType = when (random.nextInt(100)) {
                in 0..70 -> ItemType.COIN
                in 71..85 -> ItemType.REPAIR
                else -> ItemType.NITRO
            }

            list.add(GameItem(itemIdCounter++, x, y, itemType))
        }
        return list
    }

    private fun isCollidingWithObstacle(tx: Double, ty: Double, radius: Double): Boolean {
        for (obs in obstacles) {
            if (obs.type == ObstacleType.BUILDING || obs.type == ObstacleType.WATER) {
                val paddedLeft = obs.rect.left - radius
                val paddedRight = obs.rect.right + radius
                val paddedTop = obs.rect.top - radius
                val paddedBottom = obs.rect.bottom + radius

                if (tx >= paddedLeft && tx <= paddedRight && ty >= paddedTop && ty <= paddedBottom) {
                    return true
                }
            }
        }
        return false
    }

    private fun handlePerSecondTimer() {
        val current = _gameState.value
        if (current.status != GameStatus.GAMEPLAY) return

        // Level up check by keeping survive score
        val addScore = current.heatLevel * 20
        val earnedExp = current.heatLevel * 2 // Exp per second survived

        _gameState.value = current.copy(
            score = current.score + addScore
        )

        // Award EXP to persistent profile
        awardPlayerExperience(earnedExp)

        // Active pursuit heat scaling
        var heatRewardProgress = current.heatProgress
        if (current.copsActive) {
            heatRewardProgress += 0.05f
        } else {
            heatRewardProgress += 0.015f
        }

        var newHeatLevel = current.heatLevel
        if (heatRewardProgress >= 1.0f) {
            heatRewardProgress = 0.0f
            if (newHeatLevel < 5) {
                newHeatLevel++
                viewModelScope.launch(Dispatchers.Main) {
                    SoundManager.playSirenSound()
                }
            }
        }

        // Escape checks
        var remainingEscape = current.escapeCountdown
        val nearbyCops = current.copCars.any { getDistance(it.x, it.y, current.playerX, current.playerY) < 650.0 }
        
        if (current.copsActive && !nearbyCops) {
            if (remainingEscape > 0) {
                remainingEscape--
            } else {
                triggerEscapeSuccess()
                return
            }
        } else if (nearbyCops) {
            remainingEscape = 5
        }

        _gameState.value = _gameState.value.copy(
            heatLevel = newHeatLevel,
            heatProgress = heatRewardProgress,
            escapeCountdown = remainingEscape
        )
    }

    private fun awardPlayerExperience(amount: Int) {
        val config = carConfigState.value
        val newExp = config.experience + amount
        val reqExp = 100 // Level boundaries
        var finalLevel = config.level
        var finalExp = newExp

        if (finalExp >= reqExp) {
            finalLevel += finalExp / reqExp
            finalExp %= reqExp
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(
                config.copy(
                    level = finalLevel,
                    experience = finalExp
                )
            )
        }
    }

    private fun triggerEscapeSuccess() {
        val current = _gameState.value
        _gameState.value = current.copy(status = GameStatus.ESCAPED)
        
        viewModelScope.launch(Dispatchers.IO) {
            // Give 3x payouts + bonus for high ranks
            val pRankFactor = 1.0 + (carConfigState.value.level * 0.05)
            val basePayout = current.cashEarned + (current.score / 2)
            val payout = (basePayout * pRankFactor).toInt()
            repository.addCash(payout)

            // Huge exp bonus on escaping
            awardPlayerExperience(current.score / 15)

            val durationSec = (System.currentTimeMillis() - gameIdTimestamp) / 1000L
            val record = ScoreRecord(
                score = current.score,
                durationSeconds = durationSec.toInt(),
                heatLevel = current.heatLevel,
                escaped = true
            )
            repository.insertScore(record)
        }
    }

    private var gameIdTimestamp = 0L

    private fun triggerBustedState() {
        val current = _gameState.value
        _gameState.value = current.copy(status = GameStatus.BUSTED)

        viewModelScope.launch(Dispatchers.IO) {
            // Keep portion of earnings
            val savings = (current.cashEarned * 0.80).toInt()
            repository.addCash(savings)
            
            // Deduct slightly smaller exp on capture
            awardPlayerExperience(current.score / 40)

            val durationSec = (System.currentTimeMillis() - gameIdTimestamp) / 1000L
            val record = ScoreRecord(
                score = current.score,
                durationSeconds = durationSec.toInt(),
                heatLevel = current.heatLevel,
                escaped = false
            )
            repository.insertScore(record)
        }
    }

    fun initiateGameBoot() {
        gameIdTimestamp = System.currentTimeMillis()
        startNewGame()
    }

    // Shooting command
    fun fireActiveWeapon() {
        val pconfig = carConfigState.value
        val weaponLvl = pconfig.weaponLevel
        if (weaponLvl <= 0) return // No weapon owned
        if (shootCooldownTicks > 0) return // Cooling down

        val current = _gameState.value
        if (current.status != GameStatus.GAMEPLAY) return

        // Deduct/Initiate fire cooldown
        shootCooldownTicks = if (infiniteAmmo) 3 else shootMaxCooldown

        viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() } // Simple bullet fire sound simulation

        val updatedBullets = current.bullets.toMutableList()
        val headingAngle = current.playerAngle

        // Fire different projectile configurations based on Weapon level!
        when (weaponLvl) {
            1 -> {
                // Makarov PM pistol single bullet
                val speed = 24.0
                updatedBullets.add(
                    Bullet(
                        id = bulletIdCounter++,
                        x = current.playerX + cos(headingAngle) * 20.0,
                        y = current.playerY + sin(headingAngle) * 20.0,
                        vx = cos(headingAngle) * speed + current.playerSpeed * cos(headingAngle),
                        vy = sin(headingAngle) * speed + current.playerSpeed * sin(headingAngle),
                        damage = 35.0
                    )
                )
            }
            2 -> {
                // AK-74u Kalashnikov double burst shots!
                val speed = 28.0
                val angleOffset = 0.08
                updatedBullets.add(
                    Bullet(
                        id = bulletIdCounter++,
                        x = current.playerX + cos(headingAngle) * 22.0,
                        y = current.playerY + sin(headingAngle) * 22.0,
                        vx = cos(headingAngle - angleOffset) * speed,
                        vy = sin(headingAngle - angleOffset) * speed,
                        damage = 50.0
                    )
                )
                updatedBullets.add(
                    Bullet(
                        id = bulletIdCounter++,
                        x = current.playerX + cos(headingAngle) * 22.0,
                        y = current.playerY + sin(headingAngle) * 22.0,
                        vx = cos(headingAngle + angleOffset) * speed,
                        vy = sin(headingAngle + angleOffset) * speed,
                        damage = 50.0
                    )
                )
            }
            3 -> {
                // RPG-7 Rocket heavy projectile
                val speed = 19.0
                updatedBullets.add(
                    Bullet(
                        id = bulletIdCounter++,
                        x = current.playerX + cos(headingAngle) * 22.0,
                        y = current.playerY + sin(headingAngle) * 22.0,
                        vx = cos(headingAngle) * speed,
                        vy = sin(headingAngle) * speed,
                        damage = 100.0 // instant kill
                    )
                )
            }
        }

        _gameState.value = current.copy(bullets = updatedBullets)
    }

    private fun updateGamePhysics() {
        val current = _gameState.value
        if (current.status != GameStatus.GAMEPLAY) return

        val pconfig = carConfigState.value

        var px = current.playerX
        var py = current.playerY
        var pAngle = current.playerAngle
        var pSpeed = current.playerSpeed
        var pHealth = current.playerHealth
        var pNitro = current.nitroAmount
        var earnedCash = current.cashEarned
        var runScore = current.score

        if (shootCooldownTicks > 0) {
            shootCooldownTicks--
        }

        // Apply Weather particles progression
        val weatherType = pconfig.targetWeather
        val updatedParticles = current.weatherParticles.map { p ->
            p.y += p.speed
            p.x += if (weatherType == "SNOW") 1.2f else 0.5f // slide fall direction
            if (p.y > 1100f) {
                p.y = -50f
                p.x = random.nextFloat() * 1200f - 200f
            }
            p
        }

        // Tile friction checks - Snowy/Wet roads vs standard roads
        val roadMargin = 95.0
        val isLocalRoadX = abs((px % 800) - 400.0) < roadMargin
        val isLocalRoadY = abs((py % 800) - 400.0) < roadMargin
        val isRoad = isLocalRoadX || isLocalRoadY

        var surfaceFriction = if (isRoad) 0.985 else 0.925
        if (weatherType == "SNOW") {
            surfaceFriction = if (isRoad) 0.994 else 0.965 // very low drag, vehicle glides/slides forever like a sled!
        } else if (weatherType == "RAIN") {
            surfaceFriction = if (isRoad) 0.990 else 0.940
        }

        // Accelerator boost calculation
        val nitroCapFactor = if (nitroActive && pNitro > 0.0) 1.5 else 1.0
        val activeMaxSpeed = maxSpeed * nitroCapFactor * adminSpeedMultiplier
        val activeAcceleration = acceleration * (if (nitroActive && pNitro > 0.0) 2.0 else 1.0) * adminSpeedMultiplier

        if (accelInput > 0.0 && pHealth > 0.0) {
            pSpeed += accelInput * activeAcceleration
        } else if (brakeInput > 0.0) {
            pSpeed -= brakeInput * brakingForce
        } else {
            pSpeed *= surfaceFriction
        }

        pSpeed = pSpeed.coerceIn(-activeMaxSpeed * 0.3, activeMaxSpeed)

        // Steering logic
        if (abs(pSpeed) > 0.5) {
            // In Snow, you have significantly reduced yaw steering control while speeding, simulating authentic loss of grip!
            val snowSteerReduce = if (weatherType == "SNOW") 0.65 else 1.0
            val steerAngleModifier = 0.065 * (1.5 - min(abs(pSpeed) / activeMaxSpeed, 0.8)) * snowSteerReduce
            val direction = if (pSpeed >= 0.0) 1.0 else -1.0
            pAngle += steerInput * steerAngleModifier * direction
            if (pAngle > Math.PI) pAngle -= 2 * Math.PI
            if (pAngle < -Math.PI) pAngle += 2 * Math.PI
        }

        // Nitro
        if (infiniteNitro) {
            pNitro = maxNitro
        } else if (nitroActive && pNitro > 0.0 && accelInput > 0.1) {
            pNitro = (pNitro - 1.2).coerceAtLeast(0.0)
            if (random.nextInt(6) == 0) {
                viewModelScope.launch(Dispatchers.Main) { SoundManager.playNitroSound() }
            }
        } else if (!nitroActive && pNitro < maxNitro) {
            pNitro = (pNitro + 0.15).coerceAtMost(maxNitro)
        }

        // Momentum calculation
        val headingX = cos(pAngle)
        val headingY = sin(pAngle)

        // Lower grip on snow makes your sideways slide highly prominent
        var grip = gripFactor * (if (isRoad) 1.0 else 0.6)
        if (weatherType == "SNOW") {
            grip *= 0.45 // sliding easily
        }

        val moveVX = (headingX * pSpeed) * grip + (cos(pAngle) * pSpeed) * (1 - grip)
        val moveVY = (headingY * pSpeed) * grip + (sin(pAngle) * pSpeed) * (1 - grip)

        px += moveVX
        py += moveVY

        // Outer map boundaries
        if (px < 40.0 || px > mapSize - 40.0) {
            pSpeed = -pSpeed * 0.3
            px = px.coerceIn(40.0, mapSize - 40.0)
            if (!pconfig.godMode) {
                pHealth = (pHealth - abs(pSpeed) * 3.0).coerceAtLeast(0.0)
            }
            viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() }
        }
        if (py < 40.0 || py > mapSize - 40.0) {
            pSpeed = -pSpeed * 0.3
            py = py.coerceIn(40.0, mapSize - 40.0)
            if (!pconfig.godMode) {
                pHealth = (pHealth - abs(pSpeed) * 3.0).coerceAtLeast(0.0)
            }
            viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() }
        }

        // Obstacles collision check
        val carCollisionRadius = 32.0
        for (obs in obstacles) {
            if (obs.type == ObstacleType.BUILDING || obs.type == ObstacleType.WATER) {
                val closestX = px.coerceIn(obs.rect.left.toDouble(), obs.rect.right.toDouble())
                val closestY = tyCheck(py, obs.rect.top.toDouble(), obs.rect.bottom.toDouble())

                val distance = getDistance(px, py, closestX, closestY)
                if (distance < carCollisionRadius) {
                    val normalX = (px - closestX) / (distance + 0.001)
                    val normalY = (py - closestY) / (distance + 0.001)

                    px = closestX + normalX * carCollisionRadius
                    py = closestY + normalY * carCollisionRadius

                    val impactVelocity = abs(pSpeed)
                    if (impactVelocity > 1.5) {
                        if (!pconfig.godMode) {
                            pHealth = (pHealth - impactVelocity * 2.8).coerceAtLeast(0.0)
                        }
                        viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() }
                    }

                    pSpeed = -pSpeed * 0.38
                }
            }
        }

        // Collect drift points
        val isSlidingDegrees = abs(acos(headingX * cos(pAngle) + headingY * sin(pAngle))) > 0.05
        val updatedDriftTrails = current.driftTrails.toMutableList()
        if (isSlidingDegrees && abs(pSpeed) > 3.0) {
            updatedDriftTrails.add(DriftPoint(px.toFloat(), py.toFloat()))
            if (updatedDriftTrails.size > 140) {
                updatedDriftTrails.removeAt(0)
            }
            // Add drift reward score!
            runScore += 2
        } else {
            if (updatedDriftTrails.isNotEmpty() && random.nextInt(4) == 0) {
                updatedDriftTrails.removeAt(0)
            }
        }

        // Collect item logic
        val currentItems = current.items.map { item ->
            val dist = getDistance(px, py, item.x, item.y)
            if (dist < 42.0 && !item.isCollected) {
                item.isCollected = true
                when (item.type) {
                    ItemType.COIN -> {
                        earnedCash += 50
                        runScore += 100
                        viewModelScope.launch(Dispatchers.Main) { SoundManager.playCoinSound() }
                    }
                    ItemType.REPAIR -> {
                        pHealth = (pHealth + 25.0).coerceAtMost(100.0)
                        viewModelScope.launch(Dispatchers.Main) { SoundManager.playCoinSound() }
                    }
                    ItemType.NITRO -> {
                        pNitro = (pNitro + 40.0).coerceAtMost(maxNitro)
                        viewModelScope.launch(Dispatchers.Main) { SoundManager.playCoinSound() }
                    }
                }
                respawnCollectedItem(item)
            }
            item
        }

        // Bullets Physics & collision tracking with Cop units!
        val activeBullets = current.bullets.toMutableList()
        val copList = current.copCars.toMutableList()

        // Move bullets
        for (bIdx in activeBullets.indices.reversed()) {
            val b = activeBullets[bIdx]
            b.x += b.vx
            b.y += b.vy

            // Check walls or bounds
            if (b.x < 0 || b.x > mapSize || b.y < 0 || b.y > mapSize || isCollidingWithObstacle(b.x, b.y, 10.0)) {
                activeBullets.removeAt(bIdx)
                continue
            }

            // Check collision with cops
            for (cIdx in copList.indices.reversed()) {
                val cop = copList[cIdx]
                val dist = getDistance(b.x, b.y, cop.x, cop.y)
                if (dist < 35.0) {
                    // Damaged!
                    cop.health -= b.damage
                    cop.isStruck = true
                    viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() }
                    activeBullets.removeAt(bIdx)

                    if (cop.health <= 0) {
                        copList.removeAt(cIdx)
                        runScore += 600
                        earnedCash += 200 // Bonus authority cash
                    }
                    break
                }
            }
        }

        // Manage standard cops spawning
        val optimalCopsCount = current.heatLevel
        val copsActive = current.heatLevel >= 1

        if (copList.size < optimalCopsCount && random.nextInt(85) == 0) {
            val spawnAngle = random.nextDouble() * 2.0 * Math.PI
            val spawnDist = 700.0 + random.nextDouble() * 200.0
            val spawnCX = (px + cos(spawnAngle) * spawnDist).coerceIn(100.0, mapSize - 100.0)
            val spawnCY = (py + sin(spawnAngle) * spawnDist).coerceIn(100.0, mapSize - 100.0)

            if (!isCollidingWithObstacle(spawnCX, spawnCY, 20.0)) {
                copList.add(
                    CopCar(
                        id = copIdCounter++,
                        x = spawnCX,
                        y = spawnCY,
                        speed = 0.0,
                        angle = spawnAngle + Math.PI
                    )
                )
            }
        }

        // Cop pursuit loop
        val playerRadius = 32.0
        var isPlayerArrestProgressing = false

        for (idx in copList.indices) {
            val cop = copList[idx]
            val dx = px - cop.x
            val dy = py - cop.y
            val distToPlayer = getDistance(px, py, cop.x, cop.y)

            val targetAngle = atan2(dy, dx)
            cop.angle = steerTowards(cop.angle, targetAngle, 0.055)

            val copMaxSpeed = (7.5 + current.heatLevel * 0.75) * (if (copsTurboMode) 2.5 else 1.0)
            val copAccel = (0.11 + current.heatLevel * 0.015) * (if (copsTurboMode) 2.5 else 1.0)

            val driveToPlayerSpeed = if (copsDumbMode) {
                0.0
            } else if (distToPlayer > 80.0) {
                (cop.speed + copAccel).coerceAtMost(copMaxSpeed)
            } else {
                (cop.speed + copAccel * 0.5).coerceAtMost(copMaxSpeed * 0.65)
            }
            cop.speed = if (copsDumbMode) 0.0 else driveToPlayerSpeed

            cop.vx = cos(cop.angle) * cop.speed
            cop.vy = sin(cop.angle) * cop.speed

            cop.x += cop.vx
            cop.y += cop.vy

            // Avoid obstacles simple checks
            for (obs in obstacles) {
                if (obs.type == ObstacleType.BUILDING || obs.type == ObstacleType.WATER) {
                    val obsCX = cop.x.coerceIn(obs.rect.left.toDouble(), obs.rect.right.toDouble())
                    val obsCY = tyCheck(cop.y, obs.rect.top.toDouble(), obs.rect.bottom.toDouble())
                    val obsDist = getDistance(cop.x, cop.y, obsCX, obsCY)
                    if (obsDist < 30.0) {
                        val pushX = (cop.x - obsCX) / (obsDist + 0.001)
                        val pushY = (cop.y - obsCY) / (obsDist + 0.001)
                        cop.x = obsCX + pushX * 30.0
                        cop.y = obsCY + pushY * 30.0
                        cop.angle += Math.PI * 0.4
                        cop.speed *= 0.5
                    }
                }
            }

            cop.x = cop.x.coerceIn(50.0, mapSize - 50.0)
            cop.y = cop.y.coerceIn(50.0, mapSize - 50.0)

            // Collision ramming
            if (distToPlayer < (playerRadius + 22.0)) {
                val relSpeed = abs(pSpeed - cop.speed)
                val pushNormalX = (px - cop.x) / (distToPlayer + 0.001)
                val pushNormalY = (py - cop.y) / (distToPlayer + 0.001)

                px += pushNormalX * 8.0
                py += pushNormalY * 8.0
                cop.x -= pushNormalX * 8.0
                cop.y -= pushNormalY * 8.0

                if (relSpeed > 2.0) {
                    viewModelScope.launch(Dispatchers.Main) { SoundManager.playCrashSound() }
                    
                    if (pSpeed > cop.speed && pSpeed > 4.0) {
                        cop.health -= (pSpeed * 18.0)
                        pSpeed = pSpeed * 0.4
                        cop.speed = -cop.speed * 0.4
                        
                        if (cop.health <= 0.0) {
                            runScore += 500
                            earnedCash += 150
                        }
                    } else {
                        if (!pconfig.godMode) {
                            pHealth = (pHealth - (cop.speed * 4.2)).coerceAtLeast(0.0)
                        }
                        cop.speed = -cop.speed * 0.3
                        pSpeed = pSpeed * 0.5
                    }
                } else {
                    if (abs(pSpeed) < 0.8 && abs(cop.speed) < 0.8 && pHealth > 0.0) {
                        isPlayerArrestProgressing = true
                    }
                }
            }
        }

        val activeCopsRemaining = copList.filter { it.health > 0.0 }

        var bProgress = current.bustedProgress
        if (isPlayerArrestProgressing) {
            bProgress = (bProgress + 0.015f).coerceAtMost(1.0f)
            if (bProgress >= 1.0f) {
                triggerBustedState()
                return
            }
        } else {
            bProgress = (bProgress - 0.02f).coerceAtLeast(0.0f)
        }

        _gameState.value = current.copy(
            playerX = px,
            playerY = py,
            playerAngle = pAngle,
            playerSpeed = pSpeed,
            playerHealth = pHealth,
            nitroAmount = pNitro,
            cashEarned = earnedCash,
            score = runScore,
            copCars = activeCopsRemaining,
            bullets = activeBullets,
            items = currentItems,
            bustedProgress = bProgress,
            driftTrails = updatedDriftTrails,
            copsActive = copsActive,
            weatherParticles = updatedParticles
        )
    }

    private fun respawnCollectedItem(collected: GameItem) {
        viewModelScope.launch(Dispatchers.Default) {
            val px = _gameState.value.playerX
            val py = _gameState.value.playerY
            
            var rx = 0.0
            var ry = 0.0
            var valid = false
            var attempts = 0

            while (!valid && attempts < 10) {
                attempts++
                val angle = random.nextDouble() * 2.0 * Math.PI
                val dist = 800.0 + random.nextDouble() * 1200.0
                rx = (px + cos(angle) * dist).coerceIn(100.0, mapSize - 100.0)
                ry = (py + sin(angle) * dist).coerceIn(100.0, mapSize - 100.0)

                if (!isCollidingWithObstacle(rx, ry, 20.0)) {
                    valid = true
                }
            }

            val newItem = collected.copy(
                x = rx,
                y = ry,
                isCollected = false
            )

            val current = _gameState.value
            val updated = current.items.map { item ->
                if (item.id == collected.id) newItem else item
            }
            _gameState.value = current.copy(items = updated)
        }
    }

    private fun steerTowards(currentAngle: Double, targetAngle: Double, step: Double): Double {
        var difference = targetAngle - currentAngle
        while (difference < -Math.PI) difference += 2 * Math.PI
        while (difference > Math.PI) difference -= 2 * Math.PI

        return currentAngle + difference.coerceIn(-step, step)
    }

    private fun getDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }

    private fun tyCheck(valIn: Double, minV: Double, maxV: Double): Double {
        return valIn.coerceIn(minV, maxV)
    }

    // Tuning Screen Upgrade Functions
    fun upgradeEngine() {
        val config = carConfigState.value
        if (config.engineLevel < 5) {
            val cost = config.engineLevel * 500
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(engineLevel = config.engineLevel + 1))
                }
            }
        }
    }

    fun upgradeTyres() {
        val config = carConfigState.value
        if (config.tyresLevel < 5) {
            val cost = config.tyresLevel * 450
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(tyresLevel = config.tyresLevel + 1))
                }
            }
        }
    }

    fun upgradeBrakes() {
        val config = carConfigState.value
        if (config.brakesLevel < 5) {
            val cost = config.brakesLevel * 350
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(brakesLevel = config.brakesLevel + 1))
                }
            }
        }
    }

    fun upgradeNitro() {
        val config = carConfigState.value
        if (config.nitroLevel < 5) {
            val cost = config.nitroLevel * 400
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(nitroLevel = config.nitroLevel + 1))
                }
            }
        }
    }

    fun customizeColor(colorInt: Int) {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(carColor = colorInt))
        }
    }

    fun exitToMenu() {
        gameLoopJob?.cancel()
        _gameState.value = _gameState.value.copy(status = GameStatus.MENU)
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
    }

    // ==========================================
    // SLAVIC RPG MECHANICS / DONAT & PURCHASE STATE
    // ==========================================
    
    fun purchaseWeapon() {
        val config = carConfigState.value
        val nextLevel = config.weaponLevel + 1
        if (nextLevel <= 3) {
            val cost = when(nextLevel) {
                1 -> 2000  // Makarov PM
                2 -> 6500  // AK-74u
                else -> 15000 // RPG Rocket
            }
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(weaponLevel = nextLevel))
                }
            }
        }
    }

    fun purchaseFamilyCrew() {
        val config = carConfigState.value
        val nextLvl = config.familyLevel + 1
        if (nextLvl <= 3) {
            val cost = nextLvl * 4000
            viewModelScope.launch(Dispatchers.IO) {
                if (repository.spendCash(cost)) {
                    repository.saveCarConfig(config.copy(familyLevel = nextLvl))
                }
            }
        }
    }

    fun toggleSpoiler() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(bigSpoiler = !config.bigSpoiler))
        }
    }

    fun toggleNeonUnderglow() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(neonUnderglow = !config.neonUnderglow))
        }
    }

    fun updateNickname(newName: String) {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(nickname = newName.trim()))
        }
    }

    // ==========================================
    // ADMIN PANEL ACTIONS (mikha_q SPECIAL POWERS!)
    // ==========================================
    
    fun adminGiveMoney() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCash(10000000) // +10,000,000 ₽
        }
    }

    fun adminGiveExperience() {
        viewModelScope.launch(Dispatchers.Default) {
            awardPlayerExperience(1000) // +1000 EXP
        }
    }

    fun adminToggleGodMode() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(godMode = !config.godMode))
        }
    }

    fun adminFullTuning() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(
                config.copy(
                    engineLevel = 5,
                    tyresLevel = 5,
                    brakesLevel = 5,
                    nitroLevel = 5,
                    bigSpoiler = true,
                    neonUnderglow = true,
                    carColor = 0xFF121212.toInt() // Sleek midnight black Lada VAZ
                )
            )
        }
    }

    fun adminSetWeather(wType: String) {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(targetWeather = wType))
        }
        // If mid-session, update weather parameters
        _gameState.value = _gameState.value.copy(
            weatherParticles = createWeatherParticles(wType)
        )
    }

    fun adminTeleport(sector: Int) {
        // Safe coordinates around map Landmarks
        val targetCoords = when (sector) {
            1 -> Pair(1200.0, 1200.0) // "Тюнинг-Про" Auto Service
            2 -> Pair(2800.0, 1200.0) // Crime Center district
            3 -> Pair(1200.0, 2800.0) // Cozy panel sector "Хрущевки"
            else -> Pair(2000.0, 2000.0) // Safe spawn avenue
        }
        _gameState.value = _gameState.value.copy(
            playerX = targetCoords.first,
            playerY = targetCoords.second,
            playerSpeed = 0.0 // Instant calm brake on teleporting
        )
    }

    fun adminSpawnPolicePattern() {
        val current = _gameState.value
        val list = current.copCars.toMutableList()
        val px = current.playerX
        val py = current.playerY

        // Instantly spawn 4 surrounding cops
        val angles = listOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)
        for (ang in angles) {
            val cx = (px + cos(ang) * 450.0).coerceIn(100.0, mapSize - 100.0)
            val cy = (py + sin(ang) * 450.0).coerceIn(100.0, mapSize - 100.0)
            list.add(
                CopCar(
                    id = copIdCounter++,
                    x = cx,
                    y = cy,
                    speed = 2.0,
                    angle = ang + Math.PI
                )
            )
        }
        _gameState.value = current.copy(copCars = list)
    }

    fun adminKillAllPolice() {
        val current = _gameState.value
        _gameState.value = current.copy(
            copCars = emptyList(),
            score = current.score + (current.copCars.size * 300)
        )
    }

    fun adminSetMaxRank() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(
                config.copy(
                    level = 50,
                    experience = 0
                )
            )
        }
    }

    fun adminDemoteRank() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(
                config.copy(
                    level = 1,
                    experience = 0
                )
            )
        }
    }

    fun adminPromoteRank() {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(
                config.copy(
                    level = (config.level + 5).coerceAtMost(50)
                )
            )
        }
    }

    fun addCheatCash() {
        viewModelScope.launch {
            repository.addCash(5000)
        }
    }

    fun adminInjectCustomCash(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCash(amount)
        }
    }

    fun adminSetWeaponLevel(lvl: Int) {
        val config = carConfigState.value
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCarConfig(config.copy(weaponLevel = lvl))
        }
    }

    fun adminSpawnPickupWave(typeStr: String) {
        val current = _gameState.value
        if (current.status != GameStatus.GAMEPLAY) return
        val px = current.playerX
        val py = current.playerY
        val type = when (typeStr.uppercase()) {
            "COIN" -> ItemType.COIN
            "REPAIR" -> ItemType.REPAIR
            else -> ItemType.NITRO
        }
        val spawnedList = mutableListOf<GameItem>()
        for (i in 0 until 12) {
            val angle = random.nextDouble() * 2.0 * Math.PI
            val dist = 80.0 + random.nextDouble() * 250.0
            val ix = (px + cos(angle) * dist).coerceIn(100.0, mapSize - 100.0)
            val iy = (py + sin(angle) * dist).coerceIn(100.0, mapSize - 100.0)
            spawnedList.add(
                GameItem(
                    id = itemIdCounter++,
                    x = ix,
                    y = iy,
                    type = type,
                    isCollected = false
                )
            )
        }
        val newList = current.items + spawnedList
        _gameState.value = current.copy(items = newList)
    }
}
