package com.example.simonium.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.simonium.ui.viewmodel.SimoniumViewModel

@Composable
fun IntelligenceScreen(
    viewModel: SimoniumViewModel
) {
    val aiQuery by viewModel.aiQuery.collectAsState()
    val aiLanguage by viewModel.aiLanguage.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var customInput by remember { mutableStateOf("") }

    val presetTopics = listOf(
        "High-altitude malaria transmission dynamics",
        "Water quality & turbidity nephelometry in rural catchments",
        "Bio-char pyrolyzed coffee husks for chemical sorption",
        "Community reporting workflows & epidemiological surveillance"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("intelligence_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Header & Disclaimer
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SimoniumNavyDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SimoniumCyanLight
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIMONIUM Scientific Intelligence",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Language Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SimoniumNavyElevated, RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            TextButton(
                                onClick = { viewModel.setAiLanguage("EN") },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (aiLanguage == "EN") SimoniumCyanLight else Color.Gray
                                )
                            ) {
                                Text("English", fontSize = 10.sp, fontWeight = if (aiLanguage == "EN") FontWeight.Bold else FontWeight.Normal)
                            }
                            TextButton(
                                onClick = { viewModel.setAiLanguage("RW") },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (aiLanguage == "RW") SimoniumCyanLight else Color.Gray
                                )
                            ) {
                                Text("Kinyarwanda", fontSize = 10.sp, fontWeight = if (aiLanguage == "RW") FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Assistive inquiry engine: clarifies scientific concepts, suggests testable hypotheses, identifies interdisciplinary linkages, and supports technical concepts in English and Kinyarwanda.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEA580C).copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Suggestion Notice: AI outputs are inquiry aids and never replace required human scientific verification or clinical medical practitioner confirmation.",
                                fontSize = 10.sp,
                                color = Color(0xFFFDBA74),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Query Input Section
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Explore Concept or Research Question:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        placeholder = { Text("Ask to explain a concept or suggest hypotheses...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_query_input"),
                        trailingIcon = {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = SimoniumTeal
                                )
                            } else {
                                IconButton(
                                    onClick = {
                                        if (customInput.isNotBlank()) {
                                            viewModel.setAiQuery(customInput)
                                            viewModel.askAiIntelligence(customInput)
                                        }
                                    },
                                    modifier = Modifier.testTag("ai_ask_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Ask",
                                        tint = SimoniumTeal
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Or pick a common scientific research topic:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presetTopics) { topic ->
                            SuggestionChip(
                                onClick = {
                                    customInput = topic
                                    viewModel.setAiQuery(topic)
                                    viewModel.askAiIntelligence(topic)
                                },
                                label = { Text(topic.take(32) + "...", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // AI Response Card
        if (aiResponse != null) {
            val res = aiResponse!!
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ai_response_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.topic,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SimoniumTeal
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SimoniumTeal.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (aiLanguage == "RW") "Igisobanuro mu Kinyarwanda" else "Scientific Breakdown",
                                    fontSize = 10.sp,
                                    color = SimoniumTeal,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = res.summary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (aiLanguage == "RW") "Ibitekerezo by'Ubushakashatsi (Hypotheses):" else "Suggested Research Hypotheses:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        res.hypothesesSuggested.forEach { hyp ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = hyp,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (aiLanguage == "RW") "Isano hagati y'Amashami y'Ubumenyi:" else "Interdisciplinary Cross-Linkages:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.interdisciplinaryLinks,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SimoniumNavyElevated
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF0D9488).copy(alpha = 0.08f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.kinyarwandaNotes,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
