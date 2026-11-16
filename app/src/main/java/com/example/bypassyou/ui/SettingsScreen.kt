package com.example.bypassyou.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bypassyou.R
import com.example.bypassyou.ui.components.*
import com.example.bypassyou.ui.theme.screenBackgroundColor
import com.example.bypassyou.ui.tokens.SpacingTokens
import com.example.bypassyou.ui.tokens.SystemLayoutTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val isBypassEnabled by viewModel.isBypassEnabled.collectAsState()
    val targetVolumePercent by viewModel.targetVolumePercent.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val isIgnoringBatteryOpt by viewModel.isIgnoringBatteryOpt.collectAsState()
    val isDndAccessGranted by viewModel.isDndAccessGranted.collectAsState()
    val isCallScreeningRoleHeld by viewModel.isCallScreeningRoleHeld.collectAsState()
    val hasPhonePermissions by viewModel.hasPhonePermissions.collectAsState()
    val hasContactsPermission by viewModel.hasContactsPermission.collectAsState()
    val isStarredContactsEnabled by viewModel.isStarredContactsEnabled.collectAsState()
    val starredContactsCount by viewModel.starredContactsCount.collectAsState()
    val isAscendingVolumeEnabled by viewModel.isAscendingVolumeEnabled.collectAsState()
    val ascendingDurationSec by viewModel.ascendingDurationSec.collectAsState()
    val isScheduleEnabled by viewModel.isScheduleEnabled.collectAsState()
    val scheduleStartHour by viewModel.scheduleStartHour.collectAsState()
    val scheduleStartMinute by viewModel.scheduleStartMinute.collectAsState()
    val scheduleEndHour by viewModel.scheduleEndHour.collectAsState()
    val scheduleEndMinute by viewModel.scheduleEndMinute.collectAsState()
    val scheduleDays by viewModel.scheduleDays.collectAsState()

    val requiredPermissions = remember {
        mutableStateListOf<String>().apply {
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.READ_CALL_LOG)
            add(Manifest.permission.READ_CONTACTS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var pendingStarredEnable by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val contactsGranted = results[Manifest.permission.READ_CONTACTS] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (pendingStarredEnable) {
            if (contactsGranted) {
                viewModel.toggleStarredBypass(true)
            }
            pendingStarredEnable = false
        }
        viewModel.refreshSystemIntegrity()
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshSystemIntegrity()
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.screenBackgroundColor,
        topBar = {
            CanonicalLargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    ExpressiveBackButton(onClick = onNavigateBack)
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.screenBackgroundColor,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + SpacingTokens.ExtraLarge
            ),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.Small)
        ) {
            // 1. Theme Selector
            item {
                ThemeSelectorCard(
                    currentTheme = themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            // 2. System Health & Diagnostics
            item {
                SystemHealthCard(
                    isServiceActive = isBypassEnabled,
                    isIgnoringBatteryOpt = isIgnoringBatteryOpt,
                    isDndAccessGranted = isDndAccessGranted,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    onRequestBatteryOpt = { viewModel.requestIgnoreBatteryOptimizations() },
                    onRequestDndAccess = { viewModel.openDndSettings() },
                    onRequestCallScreeningRole = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val roleManager = context.getSystemService(RoleManager::class.java)
                            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            if (intent != null) {
                                roleLauncher.launch(intent)
                            }
                        }
                    },
                    hasPhonePermissions = hasPhonePermissions,
                    onRequestPhonePermissions = {
                        permissionLauncher.launch(requiredPermissions.toTypedArray())
                    }
                )
            }

            // 3. Volume Setting
            item {
                VolumeSettingCard(
                    volumePercent = targetVolumePercent,
                    maxSteps = viewModel.maxRingVolumeIndex,
                    minSteps = viewModel.minRingVolumeIndex,
                    onVolumeChange = { viewModel.setTargetVolumePercent(it) },
                    onTestSound = { viewModel.playSoundPreview() }
                )
            }

            // 4. Rules & Schedule
            item {
                RulesAndScheduleCard(
                    isStarredEnabled = isStarredContactsEnabled,
                    starredCount = starredContactsCount,
                    onToggleStarred = { enabled ->
                        if (enabled) {
                            val hasContactsPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasContactsPermission) {
                                pendingStarredEnable = true
                                permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                            } else {
                                viewModel.toggleStarredBypass(true)
                            }
                        } else {
                            pendingStarredEnable = false
                            viewModel.toggleStarredBypass(false)
                        }
                    },
                    isAscendingEnabled = isAscendingVolumeEnabled,
                    ascendingDurationSec = ascendingDurationSec,
                    onToggleAscending = { viewModel.toggleAscendingVolume(it) },
                    onSetAscendingDuration = { viewModel.setAscendingDuration(it) },
                    isScheduleEnabled = isScheduleEnabled,
                    scheduleStartHour = scheduleStartHour,
                    scheduleStartMinute = scheduleStartMinute,
                    scheduleEndHour = scheduleEndHour,
                    scheduleEndMinute = scheduleEndMinute,
                    scheduleDays = scheduleDays,
                    onToggleSchedule = { viewModel.toggleSchedule(it) },
                    onSetScheduleTime = { sh, sm, eh, em -> viewModel.setScheduleTime(sh, sm, eh, em) },
                    onToggleScheduleDay = { viewModel.toggleScheduleDay(it) }
                )
            }
        }
    }
}
