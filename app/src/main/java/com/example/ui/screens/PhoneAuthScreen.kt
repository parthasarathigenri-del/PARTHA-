package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue

private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppDarkGreen = Color(0xFF075E54)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    currentStudent: Student,
    allStudents: List<Student>,
    onRegister: (name: String, phone: String, targetExam: String) -> Unit,
    onRegisterAdmin: (name: String, phone: String) -> Unit = { _, _ -> },
    onSwitchUser: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onContinueToApp: () -> Unit
) {
    var selectedAuthTab by remember { mutableIntStateOf(if (currentStudent.isAdmin) 1 else 0) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var targetExamInput by remember { mutableStateOf("") }
    var adminNameInput by remember { mutableStateOf(if (currentStudent.isAdmin) currentStudent.name else "") }
    var adminPhoneInput by remember { mutableStateOf(if (currentStudent.isAdmin) currentStudent.phone.replace("+91 ", "") else "") }
    var isSubmitted by remember { mutableStateOf(!currentStudent.isApproved && !currentStudent.isAdmin) }
    var showSwitchUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentStudent) {
        if (!currentStudent.isApproved && !currentStudent.isAdmin) {
            isSubmitted = true
        } else if (currentStudent.isAdmin) {
            isSubmitted = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "কম্পিটিটিভ পাঠশালা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "WhatsApp স্টাইল ফোন নম্বর রেজিস্ট্রেশন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showSwitchUserDialog = true },
                        modifier = Modifier.testTag("switch_user_auth_button")
                    ) {
                        Icon(imageVector = Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ইউজার পরিবর্তন", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role Selection Tabs (Student vs Admin)
            item {
                TabRow(
                    selectedTabIndex = selectedAuthTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("auth_role_tab_row")
                ) {
                    Tab(
                        selected = selectedAuthTab == 0,
                        onClick = { selectedAuthTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("শিক্ষার্থী মোড", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedAuthTab == 1,
                        onClick = { selectedAuthTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                Text("👑 অ্যাডমিন লগইন / সেটআপ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                            }
                        }
                    )
                }
            }

            if (selectedAuthTab == 1) {
                // TAB 1: ADMIN LOGIN & PROFILE SETUP
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_setup_login_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "ইনস্টিটিউট অ্যাডমিন সেটআপ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "আপনার নাম ও নম্বর দিয়ে অ্যাডমিন হিসেবে প্রবেশ করুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider()

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryBlue.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "অ্যাডমিন হিসেবে লগইন করলে আপনি সমস্ত মক টেস্ট আপলোড, পিডিএফ ফাইল আপলোড এবং ছাত্রছাত্রীদের কে পরীক্ষা বা পিডিএফ এক্সেস করতে পারবে তা নিয়ন্ত্রণ করার সম্পূর্ণ ক্ষমতা পাবেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlue,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Admin Real Name Input
                            OutlinedTextField(
                                value = adminNameInput,
                                onValueChange = { adminNameInput = it },
                                label = { Text("আপনার নাম (Admin Name)") },
                                placeholder = { Text("যেমন: সৌমেন কুমার / প্রধান শিক্ষক / এক্সাম কন্ট্রোলার") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = PrimaryBlue)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_name_setup_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Admin Phone Input
                            OutlinedTextField(
                                value = adminPhoneInput,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        adminPhoneInput = it
                                    }
                                },
                                label = { Text("অ্যাডমিন মোবাইল নম্বর (10 Digits)") },
                                placeholder = { Text("9876543210") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                    ) {
                                        Text("+91", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Divider(
                                            modifier = Modifier
                                                .height(18.dp)
                                                .width(1.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_phone_setup_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val finalName = adminNameInput.ifBlank { "ইনস্টিটিউট অ্যাডমিন" }
                                    val finalPhone = if (adminPhoneInput.isNotBlank()) "+91 $adminPhoneInput" else "+91 98765 43210"
                                    onRegisterAdmin(finalName, finalPhone)
                                    onOpenAdmin()
                                    onContinueToApp()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_admin_setup_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("👑 আমার নামে অ্যাডমিন হিসেবে অ্যাপে প্রবেশ করুন", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (!currentStudent.isApproved && !currentStudent.isAdmin) {
                // Pending WhatsApp Approval Screen for Students
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pending_approval_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AccentAmber.copy(alpha = 0.15f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Text(
                                text = "অ্যাডমিন অনুমোদনের অপেক্ষায়",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentAmber,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "নমস্কার ${currentStudent.name},\nআপনার ফোন নম্বর (${currentStudent.phone})-এর মাধ্যমে অ্যাক্সেস রিকোয়েস্ট সফলভাবে অ্যাডমিন প্যানেলে পাঠানো হয়েছে।\n\nইনস্টিটিউট অ্যাডমিন যতক্ষণ না আপনার রিকোয়েস্ট গ্রহণ করবেন এবং মক টেস্ট ও পিডিএফ দেখার পারমিশন দেবেন, ততক্ষণ অ্যাপের টেস্ট বা কনটেন্ট উন্মুক্ত হবে না।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )

                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("আবেদনের স্থিতি:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("অনুমোদন প্রক্রিয়াধীন (Pending)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AccentAmber)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("রেজিস্টার্ড নম্বর:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentStudent.phone, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Refresh / check button
                            Button(
                                onClick = {
                                    if (currentStudent.isApproved) {
                                        onContinueToApp()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("refresh_approval_status_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("স্ট্যাটাস চেক করুন (Check Status)", fontWeight = FontWeight.Bold)
                            }

                            // Direct switch to Admin setup
                            OutlinedButton(
                                onClick = { selectedAuthTab = 1 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_admin_tab_from_pending_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("👑 আপনি কি অ্যাডমিন? এখানে লগইন করুন", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // New Student Registration Form
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("registration_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = WhatsAppGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AppRegistration,
                                            contentDescription = null,
                                            tint = WhatsAppGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "নতুন শিক্ষার্থী রেজিস্ট্রেশন",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "হোয়াটসঅ্যাপ নম্বরের মাধ্যমে সরাসরি আবেদন করুন",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Divider()

                            // Full name
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("আপনার সম্পূর্ণ নাম (Student Name)") },
                                placeholder = { Text("যেমন: রাহুল সেন / প্রিয়া দাস") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_name_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Phone Number with +91
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = {
                                    if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                        phoneInput = it
                                    }
                                },
                                label = { Text("হোয়াটসঅ্যাপ / মোবাইল নম্বর (10 Digits)") },
                                placeholder = { Text("9876543210") },
                                leadingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                    ) {
                                        Text("+91", fontWeight = FontWeight.Bold, color = WhatsAppDarkGreen)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Divider(
                                            modifier = Modifier
                                                .height(18.dp)
                                                .width(1.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Target Exam / Course
                            OutlinedTextField(
                                value = targetExamInput,
                                onValueChange = { targetExamInput = it },
                                label = { Text("টার্গেট পরীক্ষা (Target Exam)") },
                                placeholder = { Text("যেমন: WBP Constable, WBCS, Primary TET") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.School, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reg_target_exam_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WhatsAppGreen.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = WhatsAppDarkGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "রেজিস্ট্রেশন বাটনে চাপলে আবেদনটি সরাসরি অ্যাডমিনের কাছে পৌঁছাবে। অ্যাডমিন অনুমোদন দিলে এবং মক টেস্ট ও পিডিএফ পারমিশন দিলে আপনি তা এক্সেস করতে পারবেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WhatsAppDarkGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank() && phoneInput.length == 10) {
                                        onRegister(nameInput, "+91 $phoneInput", targetExamInput)
                                        isSubmitted = true
                                    }
                                },
                                enabled = nameInput.isNotBlank() && phoneInput.length == 10,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_registration_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অনুমোদনের জন্য রিকোয়েস্ট পাঠান", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (showSwitchUserDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchUserDialog = false },
            title = {
                Text("পরীক্ষার সুবিধার্থে ইউজার বাছুন", fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(allStudents) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSwitchUser(student.id)
                                    showSwitchUserDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (student.id == currentStudent.id) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (student.isAdmin) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PrimaryBlue
                                            ) {
                                                Text("অ্যাডমিন", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text("${student.phone} • ${student.targetExam}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = if (student.isApproved) EmeraldGreen.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (student.isApproved) "অনুমোদিত" else "অপেক্ষারত",
                                        color = if (student.isApproved) EmeraldGreen else AccentAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchUserDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
