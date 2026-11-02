package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfBook
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdf: PdfBook,
    omrAnswers: Map<Int, Int>,
    onSelectOmr: (Int, Int) -> Unit,
    onSubmitPdfTest: (String) -> Unit,
    onBack: () -> Unit
) {
    var currentPageIndex by remember { mutableIntStateOf(0) }
    val pages = if (pdf.samplePages.isNotEmpty()) pdf.samplePages else listOf("নথি লোড হচ্ছে...", "পরবর্তী পৃষ্ঠা...")
    var isNightMode by remember { mutableStateOf(false) }
    var showOmrSheet by remember { mutableStateOf(pdf.isScheduledExamPdf) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = pdf.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "পৃষ্ঠা ${currentPageIndex + 1} / ${pages.size} • ${pdf.fileSize}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("pdf_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    if (pdf.fileUri != null) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(android.net.Uri.parse(pdf.fileUri), "application/pdf")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open in PDF App",
                                tint = PrimaryBlue
                            )
                        }
                    }
                    IconButton(onClick = { isNightMode = !isNightMode }) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Theme Toggle"
                        )
                    }
                    if (pdf.isScheduledExamPdf) {
                        IconButton(onClick = { showOmrSheet = !showOmrSheet }) {
                            Icon(
                                imageVector = Icons.Default.FactCheck,
                                contentDescription = "OMR Sheet",
                                tint = PrimaryBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                        enabled = currentPageIndex > 0,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পূর্ববর্তী পৃষ্ঠা")
                    }

                    Text(
                        text = "${currentPageIndex + 1} / ${pages.size}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { if (currentPageIndex < pages.size - 1) currentPageIndex++ },
                        enabled = currentPageIndex < pages.size - 1,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("পরবর্তী পৃষ্ঠা")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isNightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
        ) {
            // PDF Document Viewport
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF0F172A) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Page Header simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "সংকল্প একাডেমি পাবলিকেশন • চাকরির প্রস্তুতি",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNightMode) Color.LightGray else Color.Gray
                        )
                        Text(
                            text = "পৃষ্ঠা ${currentPageIndex + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNightMode) Color.LightGray else Color.Gray
                        )
                    }

                    Divider(color = (if (isNightMode) Color.DarkGray else Color.LightGray).copy(alpha = 0.5f))

                    Text(
                        text = pages.getOrElse(currentPageIndex) { "পৃষ্ঠা খালি" },
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 26.sp,
                        color = if (isNightMode) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            // Interactive OMR Sheet for Scheduled PDF Mock Tests
            if (pdf.isScheduledExamPdf && showOmrSheet) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("pdf_omr_sheet_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ডিজিটাল ওএমআর শিট (উত্তর নির্বাচন করুন)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { onSubmitPdfTest(pdf.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("submit_omr_btn")
                            ) {
                                Text("ওএমআর জমা দিন", fontSize = 11.sp)
                            }
                        }

                        // Question OMR Rows (Questions 1 to 5)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items((1..5).toList()) { qNum ->
                                val selectedOpt = omrAnswers[qNum]
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "প্রশ্ন $qNum",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("A", "B", "C", "D").forEachIndexed { optIdx, optChar ->
                                                val isChosen = selectedOpt == optIdx
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isChosen) PrimaryBlue else Color.White)
                                                        .border(1.dp, PrimaryBlue, CircleShape)
                                                        .clickable { onSelectOmr(qNum, optIdx) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = optChar,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isChosen) Color.White else PrimaryBlue
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
