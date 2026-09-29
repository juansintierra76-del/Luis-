package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CurrencyMode
import com.example.ui.theme.*

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CATALOG("Explorar", Icons.Default.School),
    MAP("Mapa", Icons.Default.Map),
    CHATS("Chat Escuelas", Icons.Default.Forum),
    VISA("Mi Visa", Icons.Default.AirplaneTicket),
    APPLICATIONS("Prematrícula", Icons.Default.AssignmentTurnedIn),
    PROFILE("Perfil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun English4EveryoneTopBar(
    currencyMode: CurrencyMode,
    onCurrencyToggle: () -> Unit,
    savedSchoolsCount: Int,
    onSavedSchoolsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Navy800,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "E4E",
                                color = Gold500,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "English4everyone",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "🇵🇪", fontSize = 14.sp)
                        }
                        Text(
                            text = "Representantes Oficiales en Vivo",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate500
                            )
                        )
                    }
                }

                // Actions: Currency Pill & Bookmark Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Currency Pill Toggle
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (currencyMode == CurrencyMode.PEN) PeruRedLight else Navy50,
                        border = BorderStroke(1.dp, if (currencyMode == CurrencyMode.PEN) PeruRed.copy(alpha = 0.5f) else Navy100),
                        modifier = Modifier
                            .testTag("currency_toggle_button")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onCurrencyToggle() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = "Cambiar divisa",
                                tint = if (currencyMode == CurrencyMode.PEN) PeruRed else Navy800,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currencyMode == CurrencyMode.USD) "$ USD" else "S/. PEN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (currencyMode == CurrencyMode.PEN) PeruRed else Navy800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Saved schools bookmark button
                    IconButton(
                        onClick = onSavedSchoolsClick,
                        modifier = Modifier.testTag("topbar_saved_schools_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (savedSchoolsCount > 0) {
                                    Badge(
                                        containerColor = Navy800,
                                        contentColor = PureWhite
                                    ) {
                                        Text(savedSchoolsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (savedSchoolsCount > 0) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Escuelas Guardadas",
                                tint = if (savedSchoolsCount > 0) Navy800 else Slate600
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = Slate200, thickness = 1.dp)
        }
    }
}

@Composable
fun English4EveryoneBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    unreadChatCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        color = PureWhite,
        modifier = modifier
    ) {
        Column {
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 0.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            if (tab == NavigationTab.CHATS && unreadChatCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = PeruRed,
                                            contentColor = PureWhite
                                        ) {
                                            Text(unreadChatCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(imageVector = tab.icon, contentDescription = tab.title)
                                }
                            } else {
                                Icon(imageVector = tab.icon, contentDescription = tab.title)
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PureWhite,
                            indicatorColor = Navy800,
                            selectedTextColor = Navy800,
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    }
}
