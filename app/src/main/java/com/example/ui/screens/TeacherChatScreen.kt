package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.ChatMessage
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherChatScreen(
    messages: List<ChatMessage>,
    isAdminMode: Boolean,
    onSendMessage: (String, String?, String?) -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showAttachmentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "শিক্ষক-শিক্ষার্থী ডাউট ক্লিয়ারিং চ্যাট",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(shape = CircleShape, color = EmeraldGreen, modifier = Modifier.size(8.dp)) {}
                                Text(
                                    text = if (isAdminMode) "আপনি শিক্ষক হিসেবে উত্তর দিচ্ছেন" else "ফ্যাকাল্টি অনলাইনে রয়েছেন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showAttachmentDialog = true },
                        modifier = Modifier.testTag("chat_attach_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "ফাইল ও ছবি শেয়ার",
                            tint = PrimaryBlue
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("আপনার প্রশ্ন বা বার্তা লিখুন...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText, null, null)
                                inputText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "পাঠান",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 সরাসরি ইনস্টিটিউট শিক্ষকদের সাথে আলোচনা। যেকোনো জটিল প্রশ্ন, গণিতের হিসাব বা বিষয়ের ফটো তুলে এখানে পাঠাতে পারেন।",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            items(messages) { msg ->
                val isMyMessage = if (isAdminMode) msg.isTeacher else !msg.isTeacher

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMyMessage) 16.dp else 4.dp,
                            bottomEnd = if (isMyMessage) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMyMessage) PrimaryBlue else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (msg.isTeacher) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = if (isMyMessage) Color.White else PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMyMessage) Color.White.copy(alpha = 0.9f) else PrimaryBlue
                                )
                            }

                            Text(
                                text = msg.messageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurface
                            )

                            // Attachment if present
                            if (msg.attachmentName != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isMyMessage) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (msg.attachmentType == "IMAGE") Icons.Default.Image else Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = if (isMyMessage) Color.White else PrimaryBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = msg.attachmentName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (msg.attachmentType == "IMAGE") "সংযুক্ত ছবি" else "সংযুক্ত পিডিএফ নোটস",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = if (isMyMessage) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            title = { Text("ফাইল বা ছবি যুক্ত করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSendMessage(
                                    if (inputText.isNotBlank()) inputText else "প্রশ্নপত্রের গণিতের ছবির সমাধান চেয়েছি",
                                    "Math_Problem_Snapshot.jpg",
                                    "IMAGE"
                                )
                                inputText = ""
                                showAttachmentDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = PrimaryBlue)
                            Column {
                                Text("প্রশ্নপত্রের ছবি আপলোড (Photo)", fontWeight = FontWeight.Bold)
                                Text("ক্যামেরা বা গ্যালারি থেকে ছবির ফাইল সংযুক্ত করুন", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSendMessage(
                                    if (inputText.isNotBlank()) inputText else "এই পিডিএফ নোটসটি শেয়ার করলাম",
                                    "Student_Handwritten_Notes.pdf",
                                    "PDF"
                                )
                                inputText = ""
                                showAttachmentDialog = false
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = null, tint = EmeraldGreen)
                            Column {
                                Text("স্টাডি নোটস বা পিডিএফ শেয়ার (Document)", fontWeight = FontWeight.Bold)
                                Text("PDF বা ডকুমেন্ট ফাইল পাঠিয়ে প্রশ্ন আলোচনা করুন", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
