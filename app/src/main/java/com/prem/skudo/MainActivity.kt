package com.prem.skudo

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.prem.skudo.database.GameStats
import com.prem.skudo.model.Difficulty
import com.prem.skudo.ui.*
import com.prem.skudo.ui.theme.*
import com.prem.skudo.viewmodel.HomeViewModel
import com.prem.skudo.viewmodel.SudokuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val settingsViewModel: com.prem.skudo.viewmodel.SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()
            
            val darkTheme = when(settingsState.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            SudokuTheme(
                darkTheme = darkTheme,
                accentColorName = settingsState.accentColor
            ) {
                val navController = rememberNavController()
                val gameViewModel: SudokuViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(220))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                animationSpec = tween(180, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(180))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(220, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(220))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.End,
                                animationSpec = tween(180, easing = FastOutSlowInEasing)
                            ) + fadeOut(animationSpec = tween(180))
                        }
                    ) {
                        composable(
                            "splash",
                            enterTransition = { fadeIn(tween(150)) },
                            exitTransition = { fadeOut(tween(150)) }
                        ) {
                            LoadingScreen(
                                onLoadingComplete = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(
                            "home",
                            exitTransition = {
                                if (targetState.destination.route == "shop") {
                                    fadeOut(animationSpec = tween(250))
                                } else {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                                    ) + fadeOut(animationSpec = tween(180))
                                }
                            },
                            popEnterTransition = {
                                if (initialState.destination.route == "shop") {
                                    fadeIn(animationSpec = tween(250))
                                } else {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                                    ) + fadeIn(animationSpec = tween(220))
                                }
                            }
                        ) {
                            val homeState by homeViewModel.uiState.collectAsState()
                            
                            SudokuHomeScreen(
                                onStartGame = { difficulty, isDaily ->
                                    gameViewModel.dismissAllDialogs()
                                    navController.navigate("game/${difficulty.name}?isDaily=$isDaily&resume=false")
                                },
                                onContinueGame = { difficulty, isDaily ->
                                    gameViewModel.dismissAllDialogs()
                                    navController.navigate("game/${difficulty.name}?isDaily=$isDaily&resume=true")
                                },
                                onDailyChallenge = { navController.navigate("daily_challenge") },
                                onViewProfile = { navController.navigate("profile") },
                                onSettings = { navController.navigate("settings") },
                                onShop = { navController.navigate("shop") },
                                viewModel = homeViewModel
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onViewAchievements = { navController.navigate("achievements") },
                                homeViewModel = homeViewModel
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onViewProfile = { navController.navigate("profile") }
                            )
                        }
                        composable(
                            "shop",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(300))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(250))
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(250))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(250))
                            }
                        ) {
                            ShopScreen(onBack = { navController.popBackStack() })
                        }
                        composable(
                            route = "game/{difficulty}?isDaily={isDaily}&resume={resume}",
                            arguments = listOf(
                                navArgument("difficulty") { type = NavType.StringType },
                                navArgument("isDaily") { type = NavType.BoolType; defaultValue = false },
                                navArgument("resume") { type = NavType.BoolType; defaultValue = false }
                            )
                        ) { backStackEntry ->
                            val difficultyName = backStackEntry.arguments?.getString("difficulty") ?: "EASY"
                            val isDaily = backStackEntry.arguments?.getBoolean("isDaily") ?: false
                            val resume = backStackEntry.arguments?.getBoolean("resume") ?: false
                            
                            LaunchedEffect(difficultyName, isDaily, resume) {
                                if (resume) {
                                    gameViewModel.continueGame(Difficulty.valueOf(difficultyName), isDaily)
                                } else {
                                    gameViewModel.startNewGame(Difficulty.valueOf(difficultyName), isDaily)
                                }
                            }

                            SudokuGameScreen(
                                difficulty = Difficulty.valueOf(difficultyName),
                                onBack = { navController.popBackStack() },
                                viewModel = gameViewModel
                            )
                        }
                        composable("achievements") {
                            AchievementsScreen(
                                uiState = homeViewModel.uiState.collectAsState().value,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("daily_challenge") {
                            DailyChallengeScreen(
                                onStartDaily = {
                                    navController.navigate("game/${Difficulty.EXPERT.name}?isDaily=true")
                                },
                                onBack = { navController.popBackStack() },
                                viewModel = homeViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onStartTutorial: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(page)
        }

        // Pager Indicators & Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(if (isSelected) 32.dp else 8.dp)
                    val color by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else TextMuted.copy(alpha = 0.3f))
                    
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Primary Button
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onStartTutorial()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (pagerState.currentPage < 2) "CONTINUE" else "START PLAYING",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            // Skip/Back
            TextButton(
                onClick = onComplete,
                modifier = Modifier.alpha(if (pagerState.currentPage == 2) 1f else 0.7f)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) "EXPLORE HOME" else "SKIP",
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: Int) {
    val (title, description, icon, color) = when (page) {
        0 -> OnboardingData(
            "Welcome to Skudo",
            "Experience Sudoku like never before with premium visuals and satisfying gameplay.",
            Icons.Default.Grid4x4,
            PrimaryCyan
        )
        1 -> OnboardingData(
            "Master the Grid",
            "Track your progress with remaining number counters and smart highlights.",
            Icons.Default.Calculate,
            AccentGold
        )
        else -> OnboardingData(
            "Daily Rewards",
            "Complete daily challenges to earn coins, unlock avatars, and climb the ranks.",
            Icons.Default.EmojiEvents,
            EasyGreen
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(180.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(2.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = color
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

data class OnboardingData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo animation: bounce scale and fade in
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        
        // Text animation starts slightly later
        delay(200L)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        // Brief brand moment
        delay(800)
        onLoadingComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Skudo Logo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        alpha = alpha.value
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
            ) {
                Text(
                    text = "SKUDO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "CLASSIC NUMBER PUZZLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SudokuHomeScreen(
    onStartGame: (Difficulty, Boolean) -> Unit,
    onContinueGame: (Difficulty, Boolean) -> Unit,
    onDailyChallenge: () -> Unit,
    onViewProfile: () -> Unit,
    onSettings: () -> Unit,
    onShop: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            (context as? Activity)?.finish()
        } else {
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadStats()
        viewModel.checkDailyReward()
    }
    
    if (uiState.showDailyReward) {
        DailyRewardDialog(
            streak = uiState.dailyRewardStreak,
            coins = uiState.dailyRewardCoins,
            onDismiss = { viewModel.dismissDailyReward() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Header Section: Profile (Left) - Resources (Center) - Shop (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile Button (Circle)
            val profile = uiState.userProfile ?: com.prem.skudo.database.UserProfile()
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onViewProfile() },
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                UserAvatar(
                    avatarId = profile.avatarId,
                    imageUrl = profile.photoUrl,
                    size = 50.dp
                )
            }

            // Resources (Coins)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, null, Modifier.size(20.dp), AccentGold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = profile.coins.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Shop Button (Circle)
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable { onShop() },
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Shop",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Action Section (Continue Playing)
        if (uiState.hasSavedGame && uiState.latestSavedGame != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "CONTINUE PLAYING",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Start
                )
                ResumeGameCard(
                    savedGame = uiState.latestSavedGame!!,
                    onContinue = {
                        val saved = uiState.latestSavedGame!!
                        val difficulty = if (saved.isDailyChallenge) Difficulty.EXPERT else Difficulty.valueOf(saved.difficulty)
                        onContinueGame(difficulty, saved.isDailyChallenge)
                    }
                )
            }
        }

        // Logo & Title Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                "SKUDO",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp
            )
            Text(
                "CLASSIC NUMBER PUZZLE",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Difficulty Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "SELECT DIFFICULTY",
                color = TextMuted,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            DifficultyGrid { difficulty ->
                onStartGame(difficulty, false)
            }
        }

        // Action Section (Stats & Bottom Buttons)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val displayStats = uiState.bestOverallStats ?: uiState.mediumStats ?: GameStats("MEDIUM")
            StatsCard(displayStats)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    HomeActionButton("DAILY CHALLENGE", Icons.Default.CalendarToday, PrimaryCyan, onDailyChallenge)
                }
            }
            HomeActionButton("SETTINGS", Icons.Default.Settings, TextMuted, onSettings)
        }
    }
}

@Composable
fun AchievementsScreen(
    uiState: com.prem.skudo.viewmodel.HomeState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .statusBarsPadding()
                    .height(56.dp)
                    .fillMaxWidth()
                    .shadow(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = "ACHIEVEMENTS",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val bestStats = uiState.bestOverallStats
            if (bestStats != null) {
                Icon(Icons.Default.EmojiEvents, null, Modifier.size(80.dp), AccentGold)
                Spacer(Modifier.height(24.dp))
                Text(
                    "HIGHEST SCORE",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Time: ${formatTime(bestStats.bestTimeSeconds)}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Difficulty: ${bestStats.difficulty}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (bestStats.bestTimeTimestamp > 0) {
                    val date = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(bestStats.bestTimeTimestamp))
                    Text(
                        "Achieved on: $date",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                Icon(Icons.Default.Lock, null, Modifier.size(80.dp), TextMuted)
                Spacer(Modifier.height(24.dp))
                Text(
                    "No Achievements Yet",
                    color = TextDark,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Win a game to see your highest score!",
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

@Composable
fun DailyChallengeScreen(
    onStartDaily: () -> Unit,
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date()).toInt()
    val isCompletedToday = uiState.lastChallengeDate == today

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .statusBarsPadding()
                    .height(56.dp)
                    .fillMaxWidth()
                    .shadow(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = "DAILY CHALLENGE",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.CalendarToday, null, Modifier.size(80.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            
            Text(
                "TODAY'S CHALLENGE",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .background(HardPurple.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("EXPERT DIFFICULTY", color = HardPurple, fontWeight = FontWeight.ExtraBold)
            }

            Text(
                "Daily Streak: ${uiState.dailyStreak} 🔥",
                color = AccentGold,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            if (isCompletedToday) {
                Button(
                    onClick = { },
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(containerColor = EasyGreen)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(Modifier.width(8.dp))
                    Text("COMPLETED TODAY")
                }
            } else {
                Button(
                    onClick = onStartDaily,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(56.dp).fillMaxWidth(0.7f)
                ) {
                    Text("PLAY CHALLENGE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun ResumeGameCard(
    savedGame: com.prem.skudo.database.SavedGame,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContinue() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (savedGame.isDailyChallenge) "Daily Challenge" else savedGame.difficulty.lowercase().replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatTime(savedGame.timerSeconds),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Button(
                onClick = onContinue,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SudokuHomeScreenPreview() {
    SudokuTheme {
        SudokuHomeScreen(
            onStartGame = { _, _ -> },
            onContinueGame = { _, _ -> },
            onDailyChallenge = {},
            onViewProfile = {},
            onSettings = {},
            onShop = {}
        )
    }
}
