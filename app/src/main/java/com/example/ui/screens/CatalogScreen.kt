package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CurrencyMode
import com.example.data.models.School
import com.example.ui.theme.*

@Composable
fun CatalogScreen(
    schools: List<School>,
    currencyMode: CurrencyMode,
    bookmarkedSchoolIds: Set<String>,
    onToggleBookmark: (School) -> Unit,
    onDirectChatClick: (School) -> Unit,
    onPreEnrollClick: (School) -> Unit,
    onOpenMapClick: (School?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("Todos") }
    val countries = listOf("Todos", "England", "USA", "Canadá", "Australia", "New Zealand", "Malta", "Irlanda", "Sudáfrica")

    val filteredSchools = remember(schools, searchQuery, selectedCountry) {
        schools.filter { school ->
            val matchesCountry = selectedCountry == "Todos" || school.country.equals(selectedCountry, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    school.name.contains(searchQuery, true) ||
                    school.city.contains(searchQuery, true) ||
                    school.representativeName.contains(searchQuery, true)
            matchesCountry && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar & Filter Chips
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar escuela, ciudad o sede...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("catalog_search_bar")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Navy800,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenMapClick(null) }
                            .testTag("open_map_view_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Ver Mapa de Escuelas",
                                    tint = PureWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Mapa", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Country Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(countries) { country ->
                        val isSelected = selectedCountry == country
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCountry = country },
                            label = { Text(country, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy800,
                                selectedLabelColor = PureWhite
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        // Schools List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            items(filteredSchools, key = { it.id }) { school ->
                SchoolCardItem(
                    school = school,
                    currencyMode = currencyMode,
                    isBookmarked = bookmarkedSchoolIds.contains(school.id),
                    onToggleBookmark = { onToggleBookmark(school) },
                    onDirectChat = { onDirectChatClick(school) },
                    onPreEnroll = { onPreEnrollClick(school) },
                    onOpenMap = { onOpenMapClick(school) }
                )
            }
        }
    }
}

@Composable
private fun SchoolCardItem(
    school: School,
    currencyMode: CurrencyMode,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onDirectChat: () -> Unit,
    onPreEnroll: () -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier
            .fillMaxWidth()
            .testTag("school_card_${school.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Country flag & City & Map & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Navy100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = school.flagEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${school.city}, ${school.country}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Navy800
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Gold100
                    ) {
                        Text(
                            text = "★ ${school.rating} (${school.reviewsCount})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold600,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onOpenMap,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("view_on_map_${school.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Ver en Google Maps",
                            tint = PeruRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("bookmark_button_${school.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Guardar",
                            tint = if (isBookmarked) Navy800 else Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // School Name & Accreditation
            Text(
                text = school.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = school.accreditation,
                fontSize = 11.sp,
                color = Slate600
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Address
            if (school.address.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = Slate500, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = school.address,
                        fontSize = 10.sp,
                        color = Slate600,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Description
            Text(
                text = school.description,
                fontSize = 12.sp,
                color = Slate700,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Programs Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(school.popularPrograms) { program ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate100
                    ) {
                        Text(
                            text = program,
                            fontSize = 10.sp,
                            color = Slate800,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Official Representative Card Inside Item
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Slate50,
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDirectChat() }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = Navy800,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = school.representativeName.take(1),
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (school.representativeOnline) {
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald600,
                                    border = BorderStroke(1.5.dp, PureWhite),
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.BottomEnd)
                                ) {}
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = school.representativeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Navy800
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "✓ Oficial", fontSize = 10.sp, color = Emerald700, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = school.representativeRole,
                                fontSize = 10.sp,
                                color = Slate600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Direct Chat Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PureWhite,
                        border = BorderStroke(1.dp, Navy800)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Navy800, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chatear", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy800)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Visa details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CurrencyMode.formatPrice(school.priceWeeklyUsd, currencyMode) + " / sem",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Navy800
                    )
                    Text(
                        text = CurrencyMode.formatDual(school.priceWeeklyUsd),
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }

                if (school.visaAllowsWork) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald100
                    ) {
                        Text(
                            text = "💼 Permite trabajar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Emerald700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Chat & Prematrícula
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDirectChat,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Navy800),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("chat_action_${school.id}")
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(15.dp), tint = Navy800)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat Representante", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy800)
                }

                Button(
                    onClick = onPreEnroll,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("pre_enroll_action_${school.id}")
                ) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Prematrícula", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
