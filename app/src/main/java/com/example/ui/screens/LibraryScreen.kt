package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfBook
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun LibraryScreen(
    books: List<PdfBook>,
    isAdminMode: Boolean,
    onToggleLock: (String) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ই-বুক ও স্টাডি ম্যাটেরিয়াল", "শিডিউলড পিডিএফ টেস্ট")

    val displayedBooks = if (selectedTab == 0) {
        books.filter { !it.isScheduledExamPdf }
    } else {
        books.filter { it.isScheduledExamPdf }
    }

    var showUploadModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("library_screen_container")
    ) {
        // Tab row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.testTag("library_tab_$index")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scheduled PDF explanation banner if in tab 1
            if (selectedTab == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LockClock,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "টাইমড ও লকড পিডিএফ টেস্ট",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ইনস্টিটিউট যখন প্রশ্নপত্র আপলোড করে, তা নির্দিষ্ট সময় পর্যন্ত লক থাকে। পরীক্ষার নির্ধারিত সময়ে আনলক হলে শিক্ষার্থীরা প্রশ্ন দেখে সাথে সাথে উত্তর সাবমিট করতে পারে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Admin Upload Simulator Button
            if (isAdminMode) {
                item {
                    Button(
                        onClick = { showUploadModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_pdf_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নতুন বই বা শিডিউলড টেস্ট পিডিএফ আপলোড করুন (Admin)")
                    }
                }
            }

            items(displayedBooks) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_item_${book.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (book.isLocked) CrimsonRed.copy(alpha = 0.12f) else EmeraldGreen.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (book.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (book.isLocked) CrimsonRed else EmeraldGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (book.isLocked) "লক করা (Locked)" else "উন্মুক্ত (Available)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (book.isLocked) CrimsonRed else EmeraldGreen
                                    )
                                }
                            }

                            Text(
                                text = "${book.totalPages} পৃষ্ঠা • ${book.fileSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "লেখক/প্রকাশক: ${book.author} • বিভাগ: ${book.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = book.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (book.isLocked) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentAmber.copy(alpha = 0.1f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "সময়সূচী: ${book.scheduledUnlockTime}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentAmber
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Admin toggle lock button
                            if (isAdminMode && book.isScheduledExamPdf) {
                                TextButton(
                                    onClick = { onToggleLock(book.id) },
                                    modifier = Modifier.testTag("admin_toggle_lock_${book.id}")
                                ) {
                                    Icon(
                                        imageVector = if (book.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (book.isLocked) "এখনই আনলক করুন" else "পুনরায় লক করুন")
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = { onOpenPdf(book.id) },
                                enabled = !book.isLocked || isAdminMode,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("open_pdf_btn_${book.id}")
                            ) {
                                Icon(
                                    imageVector = if (book.isScheduledExamPdf) Icons.Default.Assignment else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        book.isLocked && !isAdminMode -> "লকড (সময় বাকি)"
                                        book.isScheduledExamPdf -> "পিডিএফ টেস্ট দিন"
                                        else -> "পিডিএফ পড়ুন"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadModal) {
        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = { Text("নতুন বই / শিডিউলড টেস্ট আপলোড") },
            text = {
                Text(
                    "ইনস্টিটিউটের অ্যাডমিন পোর্টাল থেকে যেকোনো PDF ফাইল আপলোড করে নির্দিষ্ট তারিখে ও সময়ে অটো-আনলক সেট করা যাবে। শিক্ষার্থী ও শিক্ষকদের সাথে এটি সাথে সাথে সিঙ্ক হবে।"
                )
            },
            confirmButton = {
                Button(onClick = { showUploadModal = false }) {
                    Text("ফাইল নির্বাচন করুন (OK)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadModal = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
