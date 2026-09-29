package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.CurrencyMode
import com.example.data.models.School
import com.example.ui.theme.*

data class CountryCoordinate(
    val name: String,
    val flag: String,
    val lat: Double,
    val lng: Double,
    val zoom: Int
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SchoolMapScreen(
    schools: List<School>,
    currencyMode: CurrencyMode,
    onDirectChatClick: (School) -> Unit,
    onPreEnrollClick: (School) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Country centers for quick navigation
    val countryFilters = remember {
        listOf(
            CountryCoordinate("Todos", "🌍", 20.0, 0.0, 2),
            CountryCoordinate("England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", 51.5074, -0.1278, 6),
            CountryCoordinate("USA", "🇺🇸", 38.0, -97.0, 4),
            CountryCoordinate("Canadá", "🇨🇦", 52.0, -100.0, 4),
            CountryCoordinate("Australia", "🇦🇺", -25.2744, 133.7751, 4),
            CountryCoordinate("New Zealand", "🇳🇿", -41.2865, 174.7762, 5),
            CountryCoordinate("Malta", "🇲🇹", 35.9375, 14.3754, 11),
            CountryCoordinate("Irlanda", "🇮🇪", 53.4129, -8.2439, 7),
            CountryCoordinate("Sudáfrica", "🇿🇦", -30.5595, 22.9375, 5)
        )
    }

    var selectedCountry by remember { mutableStateOf("Todos") }
    var selectedSchool by remember { mutableStateOf<School?>(schools.firstOrNull()) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    val filteredSchools = remember(schools, selectedCountry) {
        if (selectedCountry == "Todos") schools
        else schools.filter { it.country.equals(selectedCountry, ignoreCase = true) }
    }

    // Function to pan map to a specific location in WebView
    fun panMap(lat: Double, lng: Double, zoom: Int) {
        webViewRef?.evaluateJavascript(
            "if (window.map) { window.map.flyTo([$lat, $lng], $zoom, { animate: true, duration: 1.5 }); }",
            null
        )
    }

    // Function to focus on a school marker
    fun focusSchoolMarker(school: School) {
        selectedSchool = school
        panMap(school.latitude, school.longitude, 14)
        webViewRef?.evaluateJavascript(
            "if (window.focusMarker) { window.focusMarker('${school.id}'); }",
            null
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhite)
    ) {
        // ============================================================
        // 1. EMBEDDED GOOGLE MAPS INTERACTIVE VIEW (AndroidView)
        // ============================================================
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                        }
                    }

                    // JavaScript Bridge to communicate clicks from map markers to Android Compose
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onSchoolClicked(schoolId: String) {
                            val school = schools.find { it.id == schoolId }
                            if (school != null) {
                                post {
                                    selectedSchool = school
                                }
                            }
                        }
                    }, "AndroidBridge")

                    val mapHtml = generateMapHtml(schools)
                    loadDataWithBaseURL("https://www.google.com/maps", mapHtml, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ============================================================
        // 2. TOP FLOATING COUNTRY SELECTOR & ACTIONS
        // ============================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            // Country Filter Pills
            Surface(
                color = PureWhite.copy(alpha = 0.95f),
                tonalElevation = 4.dp,
                shadowElevation = 3.dp,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PeruRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sedes Oficiales (${filteredSchools.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        // Reset View Action
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Navy50,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedCountry = "Todos"
                                    panMap(20.0, 0.0, 2)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Navy800, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mundo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy800)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(countryFilters) { country ->
                            val isSelected = selectedCountry == country.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Navy800 else Slate100,
                                border = BorderStroke(1.dp, if (isSelected) Navy800 else Slate200),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedCountry = country.name
                                        panMap(country.lat, country.lng, country.zoom)
                                        // Auto-select first school in that country if available
                                        val firstInCountry = schools.firstOrNull {
                                            if (country.name == "Todos") true else it.country.equals(country.name, ignoreCase = true)
                                        }
                                        if (firstInCountry != null) {
                                            selectedSchool = firstInCountry
                                        }
                                    }
                                    .testTag("filter_country_${country.name.lowercase().replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = country.flag, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = country.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PureWhite else Slate800
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ============================================================
        // 3. BOTTOM SHEET / PREVIEW CARD FOR SELECTED SCHOOL
        // ============================================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Horizontal list of schools in selected country
            if (filteredSchools.size > 1) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredSchools) { school ->
                        val isSelected = selectedSchool?.id == school.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Navy900 else PureWhite,
                            tonalElevation = 4.dp,
                            shadowElevation = 3.dp,
                            border = BorderStroke(1.dp, if (isSelected) Navy900 else Slate200),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    focusSchoolMarker(school)
                                }
                                .widthIn(min = 140.dp, max = 220.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(school.flagEmoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = school.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) PureWhite else Slate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${school.city} • ★ ${school.rating}",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Gold500 else Slate500
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Detailed Card of Selected School
            val school = selectedSchool
            if (school != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("school_map_detail_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header: Name, Location, Flag, Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Navy50,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(school.flagEmoji, fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = school.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Slate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${school.city}, ${school.country} • ${school.accreditation}",
                                        fontSize = 11.sp,
                                        color = Slate600,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Price Tag
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald100
                            ) {
                                val priceFormatted = remember(school.priceWeeklyUsd, currencyMode) {
                                    val amount = if (currencyMode == CurrencyMode.USD) school.priceWeeklyUsd else school.priceWeeklyUsd * 3.75
                                    val symbol = if (currencyMode == CurrencyMode.USD) "$" else "S/."
                                    "$symbol${amount.toInt()}/sem"
                                }
                                Text(
                                    text = priceFormatted,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Street Address
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = PeruRed, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = school.address,
                                fontSize = 11.sp,
                                color = Slate700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Work info if allowed
                        if (school.visaAllowsWork) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold100.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = Gold600, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = school.workDetails,
                                        fontSize = 10.sp,
                                        color = Slate800,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Open in Google Maps, Street View, Chat, Pre-enroll
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Open in Google Maps button
                            OutlinedButton(
                                onClick = {
                                    launchGoogleMaps(context, school)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("open_in_google_maps_button")
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, tint = Navy800, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy800)
                            }

                            // Street View Button
                            OutlinedButton(
                                onClick = {
                                    launchStreetView(context, school)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(42.dp)
                                    .testTag("open_street_view_button")
                            ) {
                                Icon(Icons.Default.Streetview, contentDescription = null, tint = PeruRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("360°", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PeruRed)
                            }

                            // Chat Representative Button
                            Button(
                                onClick = { onDirectChatClick(school) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(42.dp)
                                    .testTag("chat_with_school_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chatear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Launches the native Google Maps app or web fallback with exact coordinates & marker
 */
private fun launchGoogleMaps(context: Context, school: School) {
    try {
        val geoUri = Uri.parse("geo:${school.latitude},${school.longitude}?q=${Uri.encode("${school.latitude},${school.longitude}(${school.name})")}")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${school.latitude},${school.longitude}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

/**
 * Launches Google Street View for the school's location
 */
private fun launchStreetView(context: Context, school: School) {
    try {
        val streetViewUri = Uri.parse("google.streetview:cbll=${school.latitude},${school.longitude}")
        val streetViewIntent = Intent(Intent.ACTION_VIEW, streetViewUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (streetViewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(streetViewIntent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=${school.latitude},${school.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=${school.latitude},${school.longitude}")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

/**
 * Generates the HTML5 Leaflet map styled to look identical to Google Maps with interactive pins
 */
private fun generateMapHtml(schools: List<School>): String {
    val markersJson = buildString {
        append("[")
        schools.forEachIndexed { index, school ->
            val escapedName = school.name.replace("'", "\\'")
            val escapedCity = school.city.replace("'", "\\'")
            val escapedCountry = school.country.replace("'", "\\'")
            val escapedAddress = school.address.replace("'", "\\'")
            val escapedFlag = school.flagEmoji
            append("""
                {
                    "id": "${school.id}",
                    "name": "$escapedName",
                    "city": "$escapedCity",
                    "country": "$escapedCountry",
                    "lat": ${school.latitude},
                    "lng": ${school.longitude},
                    "flag": "$escapedFlag",
                    "rating": ${school.rating},
                    "price": ${school.priceWeeklyUsd},
                    "address": "$escapedAddress"
                }
            """.trimIndent())
            if (index < schools.size - 1) append(",")
        }
        append("]")
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background: #f8fafc;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                .custom-pin {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 38px;
                    height: 38px;
                    background: #1e3a8a;
                    border: 2px solid #ffffff;
                    border-radius: 50% 50% 50% 0;
                    transform: rotate(-45deg);
                    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
                    cursor: pointer;
                    transition: transform 0.2s ease, background 0.2s ease;
                }
                .custom-pin:hover, .custom-pin.active {
                    background: #dc2626;
                    transform: rotate(-45deg) scale(1.15);
                }
                .pin-emoji {
                    transform: rotate(45deg);
                    font-size: 16px;
                }
                .leaflet-popup-content-wrapper {
                    border-radius: 14px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                    padding: 4px;
                }
                .popup-title {
                    font-size: 13px;
                    font-weight: 700;
                    color: #0f172a;
                    margin-bottom: 2px;
                }
                .popup-sub {
                    font-size: 11px;
                    color: #475569;
                    margin-bottom: 4px;
                }
                .popup-btn {
                    display: inline-block;
                    background: #1e3a8a;
                    color: #ffffff;
                    font-size: 10px;
                    font-weight: 700;
                    padding: 4px 8px;
                    border-radius: 6px;
                    text-decoration: none;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var schools = $markersJson;
                var map = L.map('map', {
                    center: [20.0, 0.0],
                    zoom: 2,
                    zoomControl: false
                });

                // Google Maps style clean OSM tiles
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap | English4everyone'
                }).addTo(map);

                // Add zoom control at top right
                L.control.zoom({ position: 'topright' }).addTo(map);

                var markersMap = {};

                schools.forEach(function(school) {
                    var customIcon = L.divIcon({
                        className: 'custom-pin-container',
                        html: '<div class="custom-pin" id="pin-' + school.id + '"><span class="pin-emoji">' + school.flag + '</span></div>',
                        iconSize: [38, 38],
                        iconAnchor: [19, 38],
                        popupAnchor: [0, -38]
                    });

                    var marker = L.marker([school.lat, school.lng], { icon: customIcon }).addTo(map);

                    var popupContent = '<div style="min-width: 140px;">' +
                        '<div class="popup-title">' + school.flag + ' ' + school.name + '</div>' +
                        '<div class="popup-sub">' + school.city + ', ' + school.country + ' • ★ ' + school.rating + '</div>' +
                        '<div style="font-weight: bold; color: #047857; font-size: 11px; margin-bottom: 6px;">$' + school.price + ' USD / sem</div>' +
                        '<a href="javascript:void(0)" class="popup-btn" onclick="selectSchool(\'' + school.id + '\')">Ver detalles</a>' +
                        '</div>';

                    marker.bindPopup(popupContent);

                    marker.on('click', function() {
                        selectSchool(school.id);
                    });

                    markersMap[school.id] = marker;
                });

                function selectSchool(schoolId) {
                    if (window.AndroidBridge && window.AndroidBridge.onSchoolClicked) {
                        window.AndroidBridge.onSchoolClicked(schoolId);
                    }
                }

                window.focusMarker = function(schoolId) {
                    var m = markersMap[schoolId];
                    if (m) {
                        m.openPopup();
                    }
                };
            </script>
        </body>
        </html>
    """.trimIndent()
}
