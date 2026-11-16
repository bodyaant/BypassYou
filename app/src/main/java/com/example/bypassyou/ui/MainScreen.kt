package com.example.bypassyou.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.bypassyou.R
import com.example.bypassyou.data.model.CallEventLog
import com.example.bypassyou.data.model.VipContact
import com.example.bypassyou.ui.components.*
import com.example.bypassyou.ui.theme.screenBackgroundColor
import com.example.bypassyou.ui.tokens.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val vipContacts by viewModel.vipContacts.collectAsState()
    val isBypassEnabled by viewModel.isBypassEnabled.collectAsState()
    val eventLogs by viewModel.eventLogs.collectAsState()

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

    var showManualAddDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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

    // Auto-refresh diagnostics & permissions on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSystemIntegrity()
                viewModel.refreshEventLogs()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshSystemIntegrity()
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshSystemIntegrity()
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let { viewModel.processPickedContactUri(it) }
    }

    val allOptimal = isIgnoringBatteryOpt && isDndAccessGranted && isCallScreeningRoleHeld && hasPhonePermissions

    val requestCallScreeningRoleAction: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            if (intent != null) {
                roleLauncher.launch(intent)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.screenBackgroundColor,
        topBar = {
            CanonicalLargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
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
                top = innerPadding.calculateTopPadding() + SpacingTokens.Small,
                bottom = innerPadding.calculateBottomPadding() + SpacingTokens.ExtraLarge
            )
        ) {
            // 1. Hero Main Service Toggle Pill (AOSP MainSwitchPreference)
            item(key = "hero_service_toggle") {
                ServiceToggleCard(
                    isEnabled = isBypassEnabled,
                    onToggle = { enabled ->
                        if (enabled && !hasPhonePermissions) {
                            permissionLauncher.launch(requiredPermissions.toTypedArray())
                        }
                        viewModel.toggleBypassEnabled(enabled)
                    }
                )
            }

            // 2. Contextual Actionable Permissions Card (only shown at top if setup is incomplete)
            if (!allOptimal) {
                item(key = "urgent_system_health") {
                    SystemHealthCard(
                        isServiceActive = isBypassEnabled,
                        isIgnoringBatteryOpt = isIgnoringBatteryOpt,
                        isDndAccessGranted = isDndAccessGranted,
                        isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                        hasPhonePermissions = hasPhonePermissions,
                        onRequestBatteryOpt = { viewModel.requestIgnoreBatteryOptimizations() },
                        onRequestDndAccess = { viewModel.openDndSettings() },
                        onRequestCallScreeningRole = requestCallScreeningRoleAction,
                        onRequestPhonePermissions = {
                            permissionLauncher.launch(requiredPermissions.toTypedArray())
                        }
                    )
                }
            }

            // 3. Unified "Who can reach you" Section (Starred Contacts + VIP List + Add Contact Two-Target Row)
            item(key = "who_can_reach_section") {
                WhoCanReachSection(
                    isStarredContactsEnabled = isStarredContactsEnabled,
                    hasContactsPermission = hasContactsPermission,
                    starredContactsCount = starredContactsCount,
                    vipContacts = vipContacts,
                    // The switch is disabled until contacts access is granted.
                    onToggleStarredContacts = { enabled -> viewModel.toggleStarredBypass(enabled) },
                    onRemoveVipContact = { viewModel.removeVipContact(it) },
                    onPickFromContacts = {
                        if (!hasContactsPermission) {
                            permissionLauncher.launch(requiredPermissions.toTypedArray())
                        } else {
                            contactPickerLauncher.launch(null)
                        }
                    },
                    onAddManually = { showManualAddDialog = true }
                )
            }

            // 4. Unified "Sound & schedule" Section (Volume + Ramp-Up + Schedule)
            item(key = "sound_and_schedule_section") {
                SoundAndScheduleSection(
                    volumePercent = targetVolumePercent,
                    maxSteps = viewModel.maxRingVolumeIndex,
                    minSteps = viewModel.minRingVolumeIndex,
                    onVolumeChange = { viewModel.setTargetVolumePercent(it) },
                    onTestSound = { viewModel.playSoundPreview() },
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

            // 5. Unified "Activity & system" Section (Call Log + System Permissions + Theme Switch)
            item(key = "activity_and_system_section") {
                ActivityAndSystemSection(
                    eventLogs = eventLogs,
                    onAddVipFromLog = { viewModel.addContactFromLog(it) },
                    onClearLogs = { viewModel.clearEventLogs() },
                    allOptimal = allOptimal,
                    isIgnoringBatteryOpt = isIgnoringBatteryOpt,
                    isDndAccessGranted = isDndAccessGranted,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    hasPhonePermissions = hasPhonePermissions,
                    onRequestBatteryOpt = { viewModel.requestIgnoreBatteryOptimizations() },
                    onRequestDndAccess = { viewModel.openDndSettings() },
                    onRequestCallScreeningRole = requestCallScreeningRoleAction,
                    onRequestPhonePermissions = {
                        permissionLauncher.launch(requiredPermissions.toTypedArray())
                    },
                    themeMode = themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            // 6. Canonical AOSP Footer Info
            item(key = "aosp_footer_info") {
                CanonicalFooterInfo(
                    text = stringResource(R.string.aosp_footer_info)
                )
            }
        }
    }

    if (showManualAddDialog) {
        AddContactDialog(
            onDismiss = { showManualAddDialog = false },
            onConfirm = { name, number ->
                viewModel.addManualContact(name, number)
                showManualAddDialog = false
            }
        )
    }
}

@Composable
fun WhoCanReachSection(
    isStarredContactsEnabled: Boolean,
    hasContactsPermission: Boolean,
    starredContactsCount: Int,
    vipContacts: List<VipContact>,
    onToggleStarredContacts: (Boolean) -> Unit,
    onRemoveVipContact: (String) -> Unit,
    onPickFromContacts: () -> Unit,
    onAddManually: () -> Unit,
    modifier: Modifier = Modifier
) {
    val starredTitle = stringResource(R.string.rules_starred_title)
    val starredSubtitle = when {
        !hasContactsPermission -> stringResource(R.string.rules_starred_desc_no_access)
        starredContactsCount > 0 -> stringResource(R.string.rules_starred_desc_with_count, starredContactsCount)
        else -> stringResource(R.string.rules_starred_desc)
    }

    CanonicalSegmentedColumn(
        title = stringResource(R.string.who_can_reach_title),
        badgeText = if (vipContacts.isNotEmpty()) "${vipContacts.size}" else null,
        modifier = modifier
    ) {
        // Segment 1: Starred Contacts Toggle
        switchItem(
            key = "starred_contacts_rule",
            title = starredTitle,
            subtitle = starredSubtitle,
            // Without contacts access the rule cannot work, so show it as off and greyed out.
            checked = isStarredContactsEnabled && hasContactsPermission,
            onCheckedChange = onToggleStarredContacts,
            enabled = hasContactsPermission
        )

        // Segments 2..N: VIP Contacts
        vipContacts.forEach { contact ->
            cardItem(key = contact.id) {
                VipContactCard(
                    contact = contact,
                    onDelete = { onRemoveVipContact(contact.id) }
                )
            }
        }

        // Bottom Segment: Canonical AOSP Two-Target "Add VIP contact" Row
        item(key = "add_vip_contact_action") { shape ->
            AddVipContactSegment(
                onPickFromContacts = onPickFromContacts,
                onAddManually = onAddManually,
                shape = shape
            )
        }
    }
}

@Composable
fun ActivityAndSystemSection(
    eventLogs: List<CallEventLog>,
    onAddVipFromLog: (CallEventLog) -> Unit,
    onClearLogs: () -> Unit,
    allOptimal: Boolean,
    isIgnoringBatteryOpt: Boolean,
    isDndAccessGranted: Boolean,
    isCallScreeningRoleHeld: Boolean,
    hasPhonePermissions: Boolean,
    onRequestBatteryOpt: () -> Unit,
    onRequestDndAccess: () -> Unit,
    onRequestCallScreeningRole: () -> Unit,
    onRequestPhonePermissions: () -> Unit,
    themeMode: String,
    onThemeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CanonicalSegmentedColumn(
        title = stringResource(R.string.system_and_logs_title),
        modifier = modifier
    ) {
        // Segment 1: Call Activity Log
        item(key = "call_event_log") { shape ->
            EventLogCard(
                eventLogs = eventLogs,
                onAddVipFromLog = onAddVipFromLog,
                onClearLogs = onClearLogs,
                shape = shape
            )
        }

        // Segment 2: System Permissions Summary (calm collapsible row when optimal)
        if (allOptimal) {
            item(key = "system_health_optimal_summary") { shape ->
                SystemHealthCollapsibleSegment(
                    isIgnoringBatteryOpt = isIgnoringBatteryOpt,
                    isDndAccessGranted = isDndAccessGranted,
                    isCallScreeningRoleHeld = isCallScreeningRoleHeld,
                    hasPhonePermissions = hasPhonePermissions,
                    onRequestBatteryOpt = onRequestBatteryOpt,
                    onRequestDndAccess = onRequestDndAccess,
                    onRequestCallScreeningRole = onRequestCallScreeningRole,
                    onRequestPhonePermissions = onRequestPhonePermissions,
                    shape = shape
                )
            }
        }

        // Segment 3: Expressive Theme Switch
        item(key = "app_theme_selector") { shape ->
            ThemeSelectorSegment(
                currentTheme = themeMode,
                onThemeSelected = onThemeSelected,
                shape = shape
            )
        }
    }
}
