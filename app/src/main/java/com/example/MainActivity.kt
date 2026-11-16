package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ExerciseScreen
import com.example.ui.screens.WaterScreen
import com.example.ui.theme.DeepOceanInk
import com.example.ui.theme.HydraFitTheme
import com.example.ui.theme.MutedSlate
import com.example.ui.theme.OceanicPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SkyBackground
import com.example.ui.theme.SkySoftAccent
import com.example.ui.viewmodel.HydraFitTab
import com.example.ui.viewmodel.HydraFitViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = SkyBackground.toArgb(),
                darkScrim = SkyBackground.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = PureWhite.toArgb(),
                darkScrim = PureWhite.toArgb(),
            ),
        )
        setContent {
            HydraFitTheme {
                HydraFitApp(
                    viewModel = viewModel(
                        factory = HydraFitViewModel.Factory(application),
                    ),
                )
            }
        }
    }
}

@Composable
fun HydraFitApp(viewModel: HydraFitViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(HydraFitTab.WATER) }
    val waterState by viewModel.waterUiState.collectAsStateWithLifecycle()
    val exerciseState by viewModel.exerciseUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDayAndWindowStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = selectedTab == HydraFitTab.EXERCISE) {
        selectedTab = HydraFitTab.WATER
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(SkyBackground, SkySoftAccent.copy(alpha = 0.45f), SkyBackground),
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        if (useNavigationRail) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars),
            ) {
                NavigationRail(
                    containerColor = PureWhite,
                    contentColor = DeepOceanInk,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    NavigationRailItem(
                        selected = selectedTab == HydraFitTab.WATER,
                        onClick = { selectedTab = HydraFitTab.WATER },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == HydraFitTab.WATER) {
                                    Icons.Filled.WaterDrop
                                } else {
                                    Icons.Outlined.WaterDrop
                                },
                                contentDescription = "Water tab",
                            )
                        },
                        label = { Text("Water") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = OceanicPrimary,
                            selectedTextColor = OceanicPrimary,
                            indicatorColor = SkyBackground,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                        ),
                        modifier = Modifier.testTag("nav_tab_water"),
                    )
                    NavigationRailItem(
                        selected = selectedTab == HydraFitTab.EXERCISE,
                        onClick = { selectedTab = HydraFitTab.EXERCISE },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == HydraFitTab.EXERCISE) {
                                    Icons.Filled.FitnessCenter
                                } else {
                                    Icons.Outlined.FitnessCenter
                                },
                                contentDescription = "Exercise tab",
                            )
                        },
                        label = { Text("Exercise") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = OceanicPrimary,
                            selectedTextColor = OceanicPrimary,
                            indicatorColor = SkyBackground,
                            unselectedIconColor = MutedSlate,
                            unselectedTextColor = MutedSlate,
                        ),
                        modifier = Modifier.testTag("nav_tab_exercise"),
                    )
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.weight(1f),
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        HydraFitTabContent(
                            selectedTab = selectedTab,
                            waterState = waterState,
                            exerciseState = exerciseState,
                            viewModel = viewModel,
                            onShowMessage = { msg ->
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(msg)
                                }
                            },
                            modifier = Modifier.widthIn(max = 640.dp),
                        )
                    }
                }
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 4.dp,
                        windowInsets = WindowInsets.navigationBars,
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == HydraFitTab.WATER,
                            onClick = { selectedTab = HydraFitTab.WATER },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == HydraFitTab.WATER) {
                                        Icons.Filled.WaterDrop
                                    } else {
                                        Icons.Outlined.WaterDrop
                                    },
                                    contentDescription = "Water tab",
                                )
                            },
                            label = {
                                Text(
                                    text = "Water",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OceanicPrimary,
                                selectedTextColor = OceanicPrimary,
                                indicatorColor = SkyBackground,
                                unselectedIconColor = MutedSlate,
                                unselectedTextColor = MutedSlate,
                            ),
                            modifier = Modifier.testTag("nav_tab_water"),
                        )
                        NavigationBarItem(
                            selected = selectedTab == HydraFitTab.EXERCISE,
                            onClick = { selectedTab = HydraFitTab.EXERCISE },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == HydraFitTab.EXERCISE) {
                                        Icons.Filled.FitnessCenter
                                    } else {
                                        Icons.Outlined.FitnessCenter
                                    },
                                    contentDescription = "Exercise tab",
                                )
                            },
                            label = {
                                Text(
                                    text = "Exercise",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OceanicPrimary,
                                selectedTextColor = OceanicPrimary,
                                indicatorColor = SkyBackground,
                                unselectedIconColor = MutedSlate,
                                unselectedTextColor = MutedSlate,
                            ),
                            modifier = Modifier.testTag("nav_tab_exercise"),
                        )
                    }
                },
            ) { innerPadding ->
                HydraFitTabContent(
                    selectedTab = selectedTab,
                    waterState = waterState,
                    exerciseState = exerciseState,
                    viewModel = viewModel,
                    onShowMessage = { msg ->
                        coroutineScope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(msg)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun HydraFitTabContent(
    selectedTab: HydraFitTab,
    waterState: com.example.ui.viewmodel.WaterUiState,
    exerciseState: com.example.ui.viewmodel.ExerciseUiState,
    viewModel: HydraFitViewModel,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = selectedTab,
        label = "tab_crossfade",
        modifier = modifier,
    ) { tab ->
        when (tab) {
            HydraFitTab.WATER -> {
                WaterScreen(
                    state = waterState,
                    onAddWater = { ml -> viewModel.addWater(ml) },
                    onDeleteWaterLog = { id -> viewModel.deleteWaterLog(id) },
                    onUpdateGoal = { goal -> viewModel.updateWaterGoal(goal) },
                    onToggleSilentReminder = { enabled ->
                        viewModel.toggleSilentReminder(enabled)
                        onShowMessage(
                            if (enabled) {
                                "Silent vibration reminders enabled (10:00 AM – 2:00 AM)"
                            } else {
                                "Silent reminders turned off"
                            },
                        )
                    },
                    onTestVibration = {
                        val vibrated = viewModel.testSilentVibration()
                        onShowMessage(
                            if (vibrated) {
                                "Silent pulse triggered [300, 100, 300] ms"
                            } else {
                                "Device vibrator not available on this hardware"
                            },
                        )
                    },
                )
            }
            HydraFitTab.EXERCISE -> {
                ExerciseScreen(
                    state = exerciseState,
                    onAddExercise = { mins -> viewModel.addExercise(mins) },
                    onDeleteExerciseLog = { id -> viewModel.deleteExerciseLog(id) },
                    onUpdateGoal = { goal -> viewModel.updateExerciseGoal(goal) },
                )
            }
        }
    }
}
