package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.viewmodel.Screen

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SankalpTopAppBar(
    title: String,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isAdminMode: Boolean,
    showAdminOption: Boolean = false,
    onAdminToggle: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenAiChat: () -> Unit,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (isAdminMode) "অ্যাডমিন প্যানেল" else when (language) {
                        AppLanguage.BENGALI -> "কম্পিটিটিভ পাঠশালা - চাকরির প্রস্তুতি"
                        AppLanguage.ENGLISH -> "Competitive Pathshala - Job Prep"
                        AppLanguage.HINDI -> "कम्पेटिटिव पाठशाला - सरकारी नौकरी तैयारी"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = if (isAdminMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBackClick, modifier = Modifier.testTag("nav_back_button")) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "ফিরে যান"
                    )
                }
            } else {
                IconButton(onClick = {}, modifier = Modifier.testTag("app_logo_button")) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Institute Logo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            // Language selector button
            Box {
                OutlinedButton(
                    onClick = { showLanguageMenu = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.testTag("language_switch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "ভাষা / Language",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> "বাং"
                            AppLanguage.ENGLISH -> "ENG"
                            AppLanguage.HINDI -> "हि"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("বাংলা (Bengali)") },
                        onClick = {
                            onLanguageChange(AppLanguage.BENGALI)
                            showLanguageMenu = false
                        },
                        leadingIcon = {
                            if (language == AppLanguage.BENGALI) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            onLanguageChange(AppLanguage.ENGLISH)
                            showLanguageMenu = false
                        },
                        leadingIcon = {
                            if (language == AppLanguage.ENGLISH) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("हिन्दी (Hindi)") },
                        onClick = {
                            onLanguageChange(AppLanguage.HINDI)
                            showLanguageMenu = false
                        },
                        leadingIcon = {
                            if (language == AppLanguage.HINDI) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }

            // AI Chatbot button
            IconButton(onClick = onOpenAiChat, modifier = Modifier.testTag("top_ai_chatbot_button")) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI চ্যাটবট সহকারী",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Chat button
            IconButton(onClick = onOpenChat, modifier = Modifier.testTag("top_chat_button")) {
                BadgedBox(badge = { Badge { Text("২") } }) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "শিক্ষক-শিক্ষার্থী চ্যাট"
                    )
                }
            }

            // Admin switch button - only visible if authorized as Admin
            if (showAdminOption || isAdminMode) {
                FilterChip(
                    selected = isAdminMode,
                    onClick = onAdminToggle,
                    label = {
                        Text(
                            text = if (isAdminMode) "অ্যাডমিন" else "ছাত্র মোড",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .testTag("admin_role_chip")
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun SankalpBottomNavigation(
    currentScreen: Screen,
    language: AppLanguage,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        NavItem(
            when (language) {
                AppLanguage.BENGALI -> "হোম"
                AppLanguage.ENGLISH -> "Home"
                AppLanguage.HINDI -> "होम"
            },
            Icons.Filled.Home,
            Icons.Outlined.Home,
            Screen.Home
        ),
        NavItem(
            when (language) {
                AppLanguage.BENGALI -> "মক টেস্ট"
                AppLanguage.ENGLISH -> "Mock Tests"
                AppLanguage.HINDI -> "मॉक टेस्ट"
            },
            Icons.Filled.Quiz,
            Icons.Outlined.Quiz,
            Screen.MockTests
        ),
        NavItem(
            when (language) {
                AppLanguage.BENGALI -> "পিডিএফ"
                AppLanguage.ENGLISH -> "PDF Library"
                AppLanguage.HINDI -> "पीडीएफ"
            },
            Icons.Filled.PictureAsPdf,
            Icons.Outlined.PictureAsPdf,
            Screen.Library
        ),
        NavItem(
            when (language) {
                AppLanguage.BENGALI -> "রুটিন"
                AppLanguage.ENGLISH -> "Routine"
                AppLanguage.HINDI -> "रूटीन"
            },
            Icons.Filled.CalendarMonth,
            Icons.Outlined.CalendarMonth,
            Screen.Routine
        ),
        NavItem(
            when (language) {
                AppLanguage.BENGALI -> "অ্যানালাইটিক্স"
                AppLanguage.ENGLISH -> "Analytics"
                AppLanguage.HINDI -> "एनालिटिक्स"
            },
            Icons.Filled.Leaderboard,
            Icons.Outlined.Leaderboard,
            Screen.Analytics
        )
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { item ->
            val isSelected = when (item.screen) {
                is Screen.Home -> currentScreen is Screen.Home
                is Screen.MockTests -> currentScreen is Screen.MockTests || currentScreen is Screen.ActiveExam || currentScreen is Screen.ExamResult || currentScreen is Screen.ExamReview
                is Screen.Library -> currentScreen is Screen.Library || currentScreen is Screen.PdfViewer
                is Screen.Routine -> currentScreen is Screen.Routine
                is Screen.Analytics -> currentScreen is Screen.Analytics
                else -> false
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("nav_${item.title}")
            )
        }
    }
}
