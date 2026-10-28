package com.therapist.assistant.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.therapist.assistant.ui.screens.*
import com.therapist.assistant.viewmodel.AppointmentViewModel

object Routes {
    const val TODAY = "today"
    const val HOME = "home"
    const val CLIENTS = "clients"
    const val CLIENT_DETAIL = "client_detail/{clientId}"
    const val ADD_EDIT_CLIENT = "add_edit_client?clientId={clientId}"
    const val ADD_EDIT_APPOINTMENT = "add_edit_appointment?appointmentId={appointmentId}&clientId={clientId}"
    const val CALENDAR = "calendar"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val SESSION_DETAIL = "session_detail/{appointmentId}"
    const val DEBTS = "debts"
    const val SEARCH = "search"
    const val SELF_CARE = "self_care"
    const val DOCUMENTS = "documents?clientId={clientId}"
    const val BUSINESS_CARD = "business_card"
    const val ONBOARDING = "onboarding"
    const val PIN = "pin"
    const val MORE = "more"
    const val PRIVACY = "privacy"
    const val UPGRADE = "upgrade"
    const val PROTOCOLS = "protocols"
    const val WORKSHOPS = "workshops"
    const val ASSESSMENTS = "assessments"
    const val TRENDS = "trends"

    fun clientDetail(id: Long) = "client_detail/$id"
    fun phq9(id: Long) = "phq9/$id"
    fun sessionDetail(id: Long) = "session_detail/$id"
    fun documents(clientId: Long? = null) = "documents?clientId=${clientId ?: -1L}"
    fun dass21(id: Long) = "dass21/$id"
    fun workshopEdit(id: Long? = null) = "workshop_edit?workshopId=${id ?: 0L}"
    fun assessmentRun(id: String, clientId: Long? = null) =
        "assessment_run/$id?clientId=${clientId ?: -1L}"
    fun addEditClient(id: Long? = null) = "add_edit_client?clientId=${id ?: -1L}"
    fun addEditAppointment(appointmentId: Long? = null, clientId: Long? = null) =
        "add_edit_appointment?appointmentId=${appointmentId ?: -1L}&clientId=${clientId ?: -1L}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: AppointmentViewModel,
    sharedBackupText: String? = null,
    onSharedBackupConsumed: () -> Unit = {}
) {
    val onboardingDone by viewModel.onboardingDone.collectAsState()
    val pinHash by viewModel.pinHash.collectAsState()
    val sessionUnlocked by viewModel.sessionUnlocked.collectAsState()

    // startDestination ثابت — تغییر پویا باعث کرش/لوپ ناوبری می‌شود
    val startDestination = Routes.TODAY

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val mainTabs = setOf(Routes.TODAY, Routes.CLIENTS, Routes.HOME, Routes.MORE)
    val showBottomBar = currentRoute in mainTabs && currentRoute != Routes.PIN && currentRoute != Routes.ONBOARDING

    // هدایت امن بر اساس وضعیت، بدون عوض کردن startDestination (جلوگیری از کرش/لوپ)
    LaunchedEffect(onboardingDone, pinHash, sessionUnlocked) {
        try {
            val dest = when {
                !onboardingDone -> Routes.ONBOARDING
                pinHash != null && !sessionUnlocked -> Routes.PIN
                else -> return@LaunchedEffect
            }
            val now = navController.currentDestination?.route
            if (now != dest) {
                navController.navigate(dest) {
                    launchSingleTop = true
                    restoreState = true
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e("NavGraph", "route guard failed", t)
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.TODAY,
                        onClick = {
                            navController.navigate(Routes.TODAY) {
                                popUpTo(Routes.TODAY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "امروز") },
                        label = { Text("امروز") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.CLIENTS,
                        onClick = {
                            navController.navigate(Routes.CLIENTS) {
                                popUpTo(Routes.TODAY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.People, contentDescription = "مراجعین") },
                        label = { Text("مراجعین") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.TODAY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.EventNote, contentDescription = "جلسات") },
                        label = { Text("جلسات") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.MORE,
                        onClick = {
                            navController.navigate(Routes.MORE) {
                                popUpTo(Routes.TODAY) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "بیشتر") },
                        label = { Text("بیشتر") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(viewModel) {
                    navController.navigate(Routes.TODAY) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            }
            composable(Routes.PIN) {
                PinLockScreen(viewModel) {
                    viewModel.markSessionUnlocked()
                    navController.navigate(Routes.TODAY) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                }
            }
            composable(Routes.TODAY) {
                TodayScreen(
                    viewModel = viewModel,
                    onAddClick = { navController.navigate(Routes.addEditAppointment()) },
                    onEditClick = { id -> navController.navigate(Routes.sessionDetail(id)) },
                    onClientsClick = { navController.navigate(Routes.CLIENTS) },
                    onCalendarClick = { navController.navigate(Routes.CALENDAR) },
                    onReportsClick = { navController.navigate(Routes.REPORTS) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                    onAllAppointments = { navController.navigate(Routes.HOME) },
                    onDebtsClick = { navController.navigate(Routes.DEBTS) },
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                    onSelfCareClick = { navController.navigate(Routes.SELF_CARE) },
                    onDocumentsClick = { navController.navigate(Routes.documents()) },
                    onBusinessCardClick = { navController.navigate(Routes.BUSINESS_CARD) },
                    onMoreClick = { navController.navigate(Routes.MORE) }
                )
            }
            composable(Routes.HOME) {
                SessionsDashboardScreen(
                    viewModel = viewModel,
                    onBack = null,
                    onAddClick = { navController.navigate(Routes.addEditAppointment()) },
                    onEditClick = { id ->
                        navController.navigate(Routes.sessionDetail(id)) {
                            launchSingleTop = true
                        }
                    },
                    onClientsClick = { navController.navigate(Routes.CLIENTS) },
                    onCalendarClick = { navController.navigate(Routes.CALENDAR) },
                    onReportsClick = { navController.navigate(Routes.REPORTS) },
                    onDebtsClick = { navController.navigate(Routes.DEBTS) }
                )
            }
            composable(Routes.CLIENTS) {
                ClientsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onClientClick = { id -> navController.navigate(Routes.clientDetail(id)) },
                    onAddClient = { navController.navigate(Routes.addEditClient()) }
                )
            }
            composable(
                Routes.CLIENT_DETAIL,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("clientId") ?: return@composable
                ClientDetailScreen(
                    viewModel = viewModel,
                    clientId = id,
                    onBack = { navController.popBackStack() },
                    onEditClient = { navController.navigate(Routes.addEditClient(id)) },
                    onAddAppointment = { navController.navigate(Routes.addEditAppointment(clientId = id)) },
                    onEditAppointment = { aid -> navController.navigate(Routes.sessionDetail(aid)) },
                    onPhq9 = { navController.navigate(Routes.phq9(id)) },
                    onDass21 = { navController.navigate(Routes.dass21(id)) },
                    onDocuments = { navController.navigate(Routes.documents(id)) }
                )
            }
            composable(
                Routes.ADD_EDIT_CLIENT,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val id = entry.arguments?.getLong("clientId") ?: -1L
                AddEditClientScreen(
                    viewModel = viewModel,
                    clientId = if (id == -1L) null else id,
                    onBack = { navController.popBackStack() },
                    onUpgrade = { navController.navigate(Routes.UPGRADE) }
                )
            }
            composable(
                Routes.ADD_EDIT_APPOINTMENT,
                arguments = listOf(
                    navArgument("appointmentId") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("clientId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val aid = entry.arguments?.getLong("appointmentId") ?: -1L
                val cid = entry.arguments?.getLong("clientId") ?: -1L
                AddEditAppointmentScreen(
                    viewModel = viewModel,
                    appointmentId = if (aid == -1L) null else aid,
                    preselectedClientId = if (cid == -1L) null else cid,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAppointmentClick = { id -> navController.navigate(Routes.sessionDetail(id)) }
                )
            }
            composable(Routes.REPORTS) {
                ReportsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    sharedBackupText = sharedBackupText,
                    onSharedBackupConsumed = onSharedBackupConsumed,
                    onBack = { navController.popBackStack() },
                    onAbout = { navController.navigate(Routes.ABOUT) },
                    onPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onUpgrade = { navController.navigate(Routes.UPGRADE) }
                )
            }
            composable(
                "phq9/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val cid = entry.arguments?.getLong("clientId") ?: return@composable
                Phq9Screen(viewModel = viewModel, clientId = cid, onBack = { navController.popBackStack() })
            }
            composable(
                "dass21/{clientId}",
                arguments = listOf(navArgument("clientId") { type = NavType.LongType })
            ) { entry ->
                val cid = entry.arguments?.getLong("clientId") ?: return@composable
                Dass21Screen(viewModel = viewModel, clientId = cid, onBack = { navController.popBackStack() })
            }
            composable(
                "session_detail/{appointmentId}",
                arguments = listOf(navArgument("appointmentId") { type = NavType.LongType })
            ) { entry ->
                val aid = entry.arguments?.getLong("appointmentId") ?: return@composable
                SessionDetailScreen(
                    viewModel = viewModel,
                    appointmentId = aid,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Routes.addEditAppointment(appointmentId = aid)) },
                    onClientClick = { cid -> navController.navigate(Routes.clientDetail(cid)) }
                )
            }
            composable(Routes.DEBTS) {
                DebtsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSessionClick = { id -> navController.navigate(Routes.sessionDetail(id)) },
                    onClientClick = { id -> navController.navigate(Routes.clientDetail(id)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onClient = { id -> navController.navigate(Routes.clientDetail(id)) },
                    onSession = { id -> navController.navigate(Routes.sessionDetail(id)) }
                )
            }
            composable(Routes.SELF_CARE) {
                SelfCareScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(
                Routes.DOCUMENTS,
                arguments = listOf(navArgument("clientId") { type = NavType.LongType; defaultValue = -1L })
            ) { entry ->
                val cid = entry.arguments?.getLong("clientId") ?: -1L
                DocumentsScreen(
                    viewModel = viewModel,
                    clientId = if (cid == -1L) null else cid,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BUSINESS_CARD) {
                BusinessCardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.MORE) {
                MoreScreen(
                    viewModel = viewModel,
                    onSearch = { navController.navigate(Routes.SEARCH) },
                    onCalendar = { navController.navigate(Routes.CALENDAR) },
                    onReports = { navController.navigate(Routes.REPORTS) },
                    onDebts = { navController.navigate(Routes.DEBTS) },
                    onDocuments = { navController.navigate(Routes.documents()) },
                    onSelfCare = { navController.navigate(Routes.SELF_CARE) },
                    onBusinessCard = { navController.navigate(Routes.BUSINESS_CARD) },
                    onProtocols = { navController.navigate(Routes.PROTOCOLS) },
                    onTrends = { navController.navigate(Routes.TRENDS) },
                    onWorkshops = { navController.navigate(Routes.WORKSHOPS) },
                    onAssessments = { navController.navigate(Routes.ASSESSMENTS) },
                    onUpgrade = { navController.navigate(Routes.UPGRADE) },
                    onPrivacy = { navController.navigate(Routes.PRIVACY) },
                    onSettings = { navController.navigate(Routes.SETTINGS) },
                    onAbout = { navController.navigate(Routes.ABOUT) }
                )
            }
            composable(Routes.PRIVACY) {
                PrivacyScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.TRENDS) {
                TrendsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onClient = { id -> navController.navigate(Routes.clientDetail(id)) }
                )
            }
            composable(Routes.UPGRADE) {
                UpgradeScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.WORKSHOPS) {
                WorkshopListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpen = { id -> navController.navigate(Routes.workshopEdit(id)) },
                    onCreate = { navController.navigate(Routes.workshopEdit()) }
                )
            }
            composable(
                "workshop_edit?workshopId={workshopId}",
                arguments = listOf(navArgument("workshopId") { type = NavType.LongType; defaultValue = 0L })
            ) { entry ->
                val id = entry.arguments?.getLong("workshopId") ?: 0L
                WorkshopEditorScreen(
                    viewModel = viewModel,
                    workshopId = if (id == 0L) null else id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ASSESSMENTS) {
                AssessmentListScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSelect = { id -> navController.navigate(Routes.assessmentRun(id)) },
                    onUpgrade = { navController.navigate(Routes.UPGRADE) }
                )
            }
            composable(
                "assessment_run/{assessmentId}?clientId={clientId}",
                arguments = listOf(
                    navArgument("assessmentId") { type = NavType.StringType },
                    navArgument("clientId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) { entry ->
                val aid = entry.arguments?.getString("assessmentId") ?: return@composable
                val cid = entry.arguments?.getLong("clientId") ?: -1L
                AssessmentRunScreen(
                    viewModel = viewModel,
                    assessmentId = aid,
                    preselectedClientId = if (cid == -1L) null else cid,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROTOCOLS) {
                ProtocolsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
