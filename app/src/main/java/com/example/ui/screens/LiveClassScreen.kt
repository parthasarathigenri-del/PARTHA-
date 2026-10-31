package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.LiveClass
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue

@Composable
fun LiveClassScreen(
    classes: List<LiveClass>,
    onJoinLiveRoom: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("live_class_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner
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
                        color = CrimsonRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VideoCameraFront,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "লাইভ ভিডিও কনফারেন্সিং ক্লাস",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ইনস্টিটিউটের ফ্যাকাল্টির সাথে সরাসরি ইন্টারেক্টিভ ক্লাস, প্রশ্নোত্তরের জন্য হাত তোলা (Raise Hand) ও চ্যাট সুবিধা।",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "আজকের লাইভ ও নির্ধারিত ক্লাস সমূহ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(classes) { liveItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_class_card_${liveItem.id}"),
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
                            color = if (liveItem.isLiveNow) CrimsonRed else PrimaryBlue.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (liveItem.isLiveNow) {
                                    Icon(
                                        imageVector = Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "LIVE NOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "SCHEDULED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (liveItem.isLiveNow) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${liveItem.liveStudentCount} জন শিক্ষার্থী যুক্ত",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = liveItem.scheduledTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = liveItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = liveItem.facultyName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = liveItem.facultyDesignation,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = liveItem.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বিষয়: ${liveItem.subject}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { onJoinLiveRoom(liveItem.id) },
                            colors = if (liveItem.isLiveNow) {
                                ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("join_live_btn_${liveItem.id}")
                        ) {
                            Icon(
                                imageVector = if (liveItem.isLiveNow) Icons.Default.Sensors else Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (liveItem.isLiveNow) "সরাসরি ক্লাসে যোগ দিন" else "রিমাইন্ডার সেট করুন")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveRoomScreen(
    liveClass: LiveClass,
    onLeaveRoom: () -> Unit
) {
    var isMuted by remember { mutableStateOf(true) }
    var isCameraOff by remember { mutableStateOf(true) }
    var isHandRaised by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    val classChats = remember {
        mutableStateListOf(
            "অনন্যা: নমস্কার স্যার!",
            "সুমন ব্যানার্জী: স্যার, আজাদ হিন্দ সরকারের প্রধান কার্যালয় কোথায় ছিল?",
            "প্রফেসর অমিতাভ চ্যাটার্জী (ফ্যাকাল্টি): প্রথমে সিঙ্গাপুর ও পরবর্তীতে রেঙ্গুনে।"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(liveClass.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(liveClass.facultyName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeaveRoom) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonRed,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FiberManualRecord, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
        ) {
            // Video Streaming Box Simulation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "প্রফেসর অমিতাভ চ্যাটার্জী (লাইভ ক্লাস নিচ্ছেন)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "হোয়াইটবোর্ড প্রেজেন্টেশন ও আলোচনা চলছে...",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Top left badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("${liveClass.liveStudentCount + 1} জন সক্রিয়", color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            // Video Conference In-Room Controls
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isMuted) CrimsonRed else Color.DarkGray
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { isCameraOff = !isCameraOff },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isCameraOff) CrimsonRed else Color.DarkGray
                        )
                    ) {
                        Icon(
                            imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Camera",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { isHandRaised = !isHandRaised },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isHandRaised) AccentAmber else Color.DarkGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = "Raise Hand",
                            tint = Color.White
                        )
                    }

                    Button(
                        onClick = onLeaveRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("ত্যাগ করুন")
                    }
                }
            }

            // Live Class Q&A Chat Feed
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "লাইভ প্রশ্নোত্তর চ্যাট",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(classChats) { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = msg,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("প্রশ্ন লিখুন...", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                classChats.add("আপনি (সুমন): $chatInput")
                                chatInput = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
