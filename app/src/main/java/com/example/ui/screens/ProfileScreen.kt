package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    savedSchoolsCount: Int,
    onOpenSavedSchools: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Header
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Navy800,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "JC",
                                color = Gold500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Juan Carlos Mendoza",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🇵🇪", fontSize = 16.sp)
                        }
                        Text(
                            text = "juansintierra76@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald100,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Estudiante Internacional Activo",
                                color = Emerald700,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Saved Schools & Firestore Sync Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Nube & Mensajería Firestore", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Emerald700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronización en Tiempo Real:", fontSize = 12.sp)
                        }
                        Text("Activa ✓", fontWeight = FontWeight.Bold, color = Emerald700, fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = Navy800)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escuelas Guardadas:", fontSize = 12.sp)
                        }
                        TextButton(onClick = onOpenSavedSchools) {
                            Text("$savedSchoolsCount guardadas • Ver", fontWeight = FontWeight.Bold, color = Navy800)
                        }
                    }
                }
            }
        }

        // Peruvian Student Preferences
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Preferencias de Estudio en el Exterior", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Text("• Ciudad de residencia en Perú: Lima Metropolitana", fontSize = 12.sp, color = Slate700)
                    Text("• Destino de mayor interés: Australia & Canadá", fontSize = 12.sp, color = Slate700)
                    Text("• Meta académica: Preparación IELTS & University Pathway", fontSize = 12.sp, color = Slate700)
                    Text("• Pasaporte peruano: Vigente hasta 2029", fontSize = 12.sp, color = Slate700)
                }
            }
        }
    }
}
