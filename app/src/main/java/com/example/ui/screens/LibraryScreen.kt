package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfBook
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.PrimaryBlue

data class SubjectCategory(
    val key: String,
    val name: String,
    val icon: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    books: List<PdfBook>,
    isAdminMode: Boolean,
    onToggleLock: (String) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    val subjectCategories = listOf(
        SubjectCategory("all", "সব বিষয়", "📚", PrimaryBlue),
        SubjectCategory("ইতিহাস", "ইতিহাস", "🏛️", Color(0xFFD97706)),
        SubjectCategory("ভূগোল", "ভূগোল", "🌍", Color(0xFF059669)),
        SubjectCategory("সায়েন্স", "সায়েন্স", "🔬", Color(0xFF2563EB)),
        SubjectCategory("রিজনিং", "রিজনিং", "🧩", Color(0xFF7C3AED)),
        SubjectCategory("অঙ্ক", "অঙ্ক", "📐", Color(0xFFDC2626)),
        SubjectCategory("সংবিধান", "সংবিধান", "⚖️", Color(0xFF0D9488)),
        SubjectCategory("শিডিউলড টেস্ট", "শিডিউলড টেস্ট", "🔒", CrimsonRed)
    )

    var selectedCategoryKey by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var showUploadModal by remember { mutableStateOf(false) }

    val filteredBooks = books.filter { book ->
        val matchesCategory = if (selectedCategoryKey == "all") {
            true
        } else {
            book.category.contains(selectedCategoryKey, ignoreCase = true)
        }
        val matchesSearch = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.description.contains(searchQuery, ignoreCase = true) ||
                book.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("library_screen_container")
    ) {
        // Top Search & Header Bar
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ডিজিটাল পিডিএফ লাইব্রেরি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বিষয়ভিত্তিক স্টাডি ম্যাটেরিয়াল ও পরীক্ষা পেপার",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isAdminMode) {
                        Button(
                            onClick = { showUploadModal = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("admin_upload_pdf_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF আপলোড", fontSize = 12.sp)
                        }
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("বই, নোটস বা বিষয় অনুসন্ধান করুন...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pdf_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Horizontal Subject Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjectCategories.forEach { cat ->
                        val isSelected = selectedCategoryKey == cat.key
                        val count = if (cat.key == "all") {
                            books.size
                        } else {
                            books.count { it.category.contains(cat.key, ignoreCase = true) }
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryKey = cat.key },
                            label = {
                                Text(
                                    text = "${cat.icon} ${cat.name} ($count)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.color.copy(alpha = 0.18f),
                                selectedLabelColor = cat.color
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("pdf_category_${cat.key}")
                        )
                    }
                }
            }
        }

        // PDF List / Subject Sections
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // If "all" is selected, we group by subject sections
            if (selectedCategoryKey == "all" && searchQuery.isBlank()) {
                val activeSections = subjectCategories.filter { it.key != "all" }
                activeSections.forEach { category ->
                    val sectionBooks = books.filter { it.category.contains(category.key, ignoreCase = true) }
                    if (sectionBooks.isNotEmpty()) {
                        item(key = "header_${category.key}") {
                            SubjectSectionHeader(category = category, count = sectionBooks.size)
                        }

                        items(sectionBooks, key = { it.id }) { book ->
                            PdfBookCard(
                                book = book,
                                isAdminMode = isAdminMode,
                                onToggleLock = onToggleLock,
                                onOpenPdf = onOpenPdf
                            )
                        }
                    }
                }
            } else {
                // Filtered by specific subject or search
                if (filteredBooks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "এই বিষয়ে কোনো পিডিএফ পাওয়া যায়নি",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    item {
                        val currentCategory = subjectCategories.find { it.key == selectedCategoryKey }
                        if (currentCategory != null && selectedCategoryKey != "all") {
                            SubjectSectionHeader(category = currentCategory, count = filteredBooks.size)
                        }
                    }

                    items(filteredBooks, key = { it.id }) { book ->
                        PdfBookCard(
                            book = book,
                            isAdminMode = isAdminMode,
                            onToggleLock = onToggleLock,
                            onOpenPdf = onOpenPdf
                        )
                    }
                }
            }
        }
    }

    if (showUploadModal) {
        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = PrimaryBlue)
                    Text("পিডিএফ আপলোড প্যানেল")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "শিক্ষক ও অ্যাডমিনরা সরাসরি অ্যাপ থেকে যেকোনো বিষয় (ইতিহাস, ভূগোল, সায়েন্স, রিজনিং, অঙ্ক, সংবিধান) অথবা শিডিউলড মক টেস্ট প্রশ্নপত্র আপলোড করতে পারেন।"
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 টিপস: আপনি অ্যাডমিন প্যানেল থেকে টেক্সট কপি করে সরাসরি মক টেস্ট প্রশ্নমালাতেও রূপান্তর করতে পারেন।",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showUploadModal = false }) {
                    Text("ফাইল নির্বাচন সম্পন্ন (OK)")
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

@Composable
fun SubjectSectionHeader(
    category: SubjectCategory,
    count: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = category.color.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_header_${category.key}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = category.icon,
                    fontSize = 18.sp
                )
                Text(
                    text = "${category.name} সেকশন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
            }
            Surface(
                shape = CircleShape,
                color = category.color.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$count টি পিডিএফ",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
            }
        }
    }
}

@Composable
fun PdfBookCard(
    book: PdfBook,
    isAdminMode: Boolean,
    onToggleLock: (String) -> Unit,
    onOpenPdf: (String) -> Unit
) {
    val subjectIcon = when {
        book.category.contains("ইতিহাস") -> "🏛️"
        book.category.contains("ভূগোল") -> "🌍"
        book.category.contains("সায়েন্স") -> "🔬"
        book.category.contains("রিজনিং") -> "🧩"
        book.category.contains("অঙ্ক") -> "📐"
        book.category.contains("সংবিধান") -> "⚖️"
        else -> "📄"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pdf_item_${book.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = subjectIcon, fontSize = 12.sp)
                        Text(
                            text = book.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (book.isLocked) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonRed.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(12.dp))
                                Text("লকড", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.12f)
                        ) {
                            Text("উন্মুক্ত", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }

                    Text(
                        text = "${book.totalPages} পৃষ্ঠা • ${book.fileSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = book.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
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

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        Text(if (book.isLocked) "এখনই আনলক করুন" else "পুনরায় লক করুন", fontSize = 12.sp)
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
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
