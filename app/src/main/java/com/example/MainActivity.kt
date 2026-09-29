package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.English4EveryoneBottomBar
import com.example.ui.components.English4EveryoneTopBar
import com.example.ui.components.NavigationTab
import com.example.ui.screens.*
import com.example.ui.theme.English4EveryoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            English4EveryoneTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val activeChatSchool by viewModel.activeChatSchool.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val currencyMode by viewModel.currencyMode.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedSchoolIds.collectAsState()
    val isSavedSchoolsOpen by viewModel.isSavedSchoolsDialogOpen.collectAsState()
    val preEnrollSchool by viewModel.preEnrollSchool.collectAsState()

    val visaApplications by viewModel.visaApplications.collectAsState()
    val activeVisaApplication by viewModel.activeVisaApplication.collectAsState()
    val visaSyncStatus by viewModel.visaSyncStatus.collectAsState()
    val inAppNotifications by viewModel.inAppNotifications.collectAsState()
    val latestNotification by viewModel.latestNotification.collectAsState()

    // Handle back press if not on CATALOG tab
    if (currentTab != NavigationTab.CATALOG) {
        BackHandler {
            viewModel.setTab(NavigationTab.CATALOG)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            English4EveryoneTopBar(
                currencyMode = currencyMode,
                onCurrencyToggle = { viewModel.toggleCurrency() },
                savedSchoolsCount = bookmarkedIds.size,
                onSavedSchoolsClick = { viewModel.openSavedSchools() }
            )
        },
        bottomBar = {
            English4EveryoneBottomBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

        when (currentTab) {
            NavigationTab.CATALOG -> {
                CatalogScreen(
                    schools = viewModel.schools,
                    currencyMode = currencyMode,
                    bookmarkedSchoolIds = bookmarkedIds,
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onDirectChatClick = { viewModel.selectChatSchool(it) },
                    onPreEnrollClick = { viewModel.startPreEnrollment(it) },
                    onOpenMapClick = { _ -> viewModel.setTab(NavigationTab.MAP) },
                    modifier = contentModifier
                )
            }
            NavigationTab.MAP -> {
                SchoolMapScreen(
                    schools = viewModel.schools,
                    currencyMode = currencyMode,
                    onDirectChatClick = { viewModel.selectChatSchool(it) },
                    onPreEnrollClick = { viewModel.startPreEnrollment(it) },
                    modifier = contentModifier
                )
            }
            NavigationTab.CHATS -> {
                RealTimeChatScreen(
                    currentSchool = activeChatSchool,
                    allSchools = viewModel.schools,
                    messages = chatMessages,
                    currencyMode = currencyMode,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onSelectSchool = { viewModel.selectChatSchool(it) },
                    onPreEnrollClick = { viewModel.startPreEnrollment(it) },
                    modifier = contentModifier
                )
            }
            NavigationTab.VISA -> {
                VisaTrackerScreen(
                    applications = visaApplications,
                    activeApplication = activeVisaApplication,
                    inAppNotifications = inAppNotifications,
                    latestNotification = latestNotification,
                    syncStatus = visaSyncStatus,
                    onSelectApplication = { viewModel.selectVisaApplication(it) },
                    onUpdateStatus = { app, newStatus, notes, appointmentDate ->
                        viewModel.updateVisaStatus(context, app, newStatus, notes, appointmentDate)
                    },
                    onToggleChecklistItem = { app, itemId ->
                        viewModel.toggleChecklistItem(app, itemId)
                    },
                    onAddChecklistItem = { app, title, desc ->
                        viewModel.addCustomChecklistItem(app, title, desc)
                    },
                    onRefreshSync = { viewModel.refreshVisaFromFirestore() },
                    onDismissNotificationBanner = { viewModel.dismissLatestNotification() },
                    onChatWithSchool = { schoolId ->
                        val school = viewModel.schools.find { it.id == schoolId } ?: viewModel.schools.first()
                        viewModel.selectChatSchool(school)
                    },
                    modifier = contentModifier
                )
            }
            NavigationTab.APPLICATIONS -> {
                PreEnrollmentScreen(
                    school = preEnrollSchool,
                    currencyMode = currencyMode,
                    onSubmitPreEnrollment = { weeks, studentName, program ->
                        viewModel.submitPreEnrollment(
                            school = preEnrollSchool,
                            weeks = weeks,
                            studentName = studentName,
                            studentEmail = "juansintierra76@gmail.com",
                            studentPhone = "+51 987 654 321",
                            studentRegion = "Lima",
                            program = program
                        )
                        viewModel.selectChatSchool(preEnrollSchool)
                    },
                    modifier = contentModifier
                )
            }
            NavigationTab.PROFILE -> {
                ProfileScreen(
                    savedSchoolsCount = bookmarkedIds.size,
                    onOpenSavedSchools = { viewModel.openSavedSchools() },
                    modifier = contentModifier
                )
            }
        }

        if (isSavedSchoolsOpen) {
            val savedSchools = viewModel.schools.filter { bookmarkedIds.contains(it.id) }
            SavedSchoolsDialog(
                savedSchools = savedSchools,
                currencyMode = currencyMode,
                onDismiss = { viewModel.closeSavedSchools() },
                onRemoveBookmark = { viewModel.toggleBookmark(it) },
                onDirectChat = {
                    viewModel.closeSavedSchools()
                    viewModel.selectChatSchool(it)
                }
            )
        }
    }
}
