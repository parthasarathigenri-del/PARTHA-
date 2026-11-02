package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.repository.JobPrepRepository
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppDarkGreen = Color(0xFF075E54)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    currentStudent: Student,
    allStudents: List<Student>,
    onRegister: (name: String, phone: String, targetExam: String) -> Unit,
    onAdminLogin: (phone: String, pass: String) -> Boolean,
    onContinueToApp: () -> Unit
) {
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    var studentNameInput by remember { mutableStateOf("") }
    var studentTargetExamInput by remember { mutableStateOf("") }
    var isRegistrationSubmitted by remember { mutableStateOf(!currentStudent.isApproved && currentStudent.phone.isNotBlank() && !currentStudent.isAdmin) }

    // Detect if entered phone matches the Institute Admin number (8902720907)
    val cleanDigits = phoneInput.filter { it.isDigit() }
    val isAdminNumber = cleanDigits == JobPrepRepository.ADMIN_PHONE || cleanDigits.endsWith(JobPrepRepository.ADMIN_PHONE)

    LaunchedEffect(currentStudent) {
        if (!currentStudent.isApproved && currentStudent.phone.isNotBlank() && !currentStudent.isAdmin) {
            isRegistrationSubmitted = true
        } else if (currentStudent.isApproved || currentStudent.isAdmin) {
            isRegistrationSubmitted = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = WhatsAppGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = WhatsAppGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "কম্পিটিটিভ পাঠশালা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isAdminNumber) "👑 ইনস্টিটিউট প্রধান লগইন গেটওয়ে" else "শিক্ষার্থী রেজিস্ট্রেশন ও লগইন",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAdminNumber) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
            if (isRegistrationSubmitted && !currentStudent.isApproved && !currentStudent.isAdmin) {
                // Pending Admin Approval Screen for Regular Students
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
                                text = "নমস্কার ${currentStudent.name},\nআপনার আবেদনটি সফলভাবে প্রধান অ্যাডমিন প্যানেলে জমা হয়েছে।\nইনস্টিটিউট অ্যাডমিন অনুমোদন দিলেই আপনার জন্য মক টেস্ট ও পাঠ্য বিষয় উন্মুক্ত হবে।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )

                            HorizontalDivider()

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
                                Text(currentStudent.phone.ifBlank { phoneInput }, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

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
                        }
                    }
                }
            } else {
                // Primary Authentication Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_auth_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header Banner
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAdminNumber) PrimaryBlue.copy(alpha = 0.15f) else WhatsAppGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isAdminNumber) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isAdminNumber) PrimaryBlue else WhatsAppGreen,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isAdminNumber) "👑 ইনস্টিটিউট প্রধান / অ্যাডমিন লগইন" else "মোবাইল নম্বর লিখুন",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isAdminNumber) "নিরাপদ অ্যাডমিন অ্যাক্সেসের জন্য পাসওয়ার্ড দিন" else "ছাত্রছাত্রীদের কোনো পাসওয়ার্ডের প্রয়োজন নেই",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider()

                            // Phone Number Input
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = {
                                    if (it.length <= 13 && it.all { ch -> ch.isDigit() || ch == '+' || ch == ' ' }) {
                                        phoneInput = it
                                        passwordError = null
                                    }
                                },
                                label = { Text("মোবাইল নম্বর (Mobile Number)") },
                                placeholder = { Text("8902720907 অথবা আপনার নম্বর") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    if (isAdminNumber) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PrimaryBlue
                                        ) {
                                            Text(
                                                text = "ADMIN",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // ROLE 1: ADMIN LOGIN MODE (Number is 8902720907)
                            AnimatedVisibility(visible = isAdminNumber, enter = fadeIn(), exit = fadeOut()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "অ্যাডমিন নম্বর শনাক্ত হয়েছে। অ্যাডমিন প্যানেল খুলতে নির্দিষ্ট পাসওয়ার্ড লিখুন।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PrimaryBlue,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Password Input
                                    OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = {
                                            passwordInput = it
                                            passwordError = null
                                        },
                                        label = { Text("অ্যাডমিন পাসওয়ার্ড (Admin Password)") },
                                        placeholder = { Text("পাসওয়ার্ড লিখুন") },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = PrimaryBlue)
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                                Icon(
                                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        isError = passwordError != null,
                                        supportingText = {
                                            if (passwordError != null) {
                                                Text(passwordError!!, color = CrimsonRed, fontSize = 11.sp)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_password_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = {
                                            val success = onAdminLogin(phoneInput, passwordInput)
                                            if (success) {
                                                onContinueToApp()
                                            } else {
                                                passwordError = "ভুল পাসওয়ার্ড! সঠিক অ্যাডমিন পাসওয়ার্ড লিখুন।"
                                            }
                                        },
                                        enabled = passwordInput.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_login_submit_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("👑 অ্যাডমিন হিসেবে প্যানেলে প্রবেশ করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // ROLE 2: REGULAR STUDENT REGISTRATION (No password needed)
                            AnimatedVisibility(visible = !isAdminNumber && cleanDigits.length >= 10, enter = fadeIn(), exit = fadeOut()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "ছাত্রছাত্রীদের কোনো পাসওয়ার্ডের প্রয়োজন নেই। শুধু নাম ও টার্গেট পরীক্ষা দিয়ে আবেদন করুন।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WhatsAppDarkGreen,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Student Name Input
                                    OutlinedTextField(
                                        value = studentNameInput,
                                        onValueChange = { studentNameInput = it },
                                        label = { Text("আপনার সম্পূর্ণ নাম (Student Name)") },
                                        placeholder = { Text("যেমন: রাহুল সেন / অনিতা দাস") },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("student_name_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    // Target Exam Input
                                    OutlinedTextField(
                                        value = studentTargetExamInput,
                                        onValueChange = { studentTargetExamInput = it },
                                        label = { Text("টার্গেট পরীক্ষা (Target Exam)") },
                                        placeholder = { Text("যেমন: WBP Constable, WBCS, Primary TET") },
                                        singleLine = true,
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.School, contentDescription = null)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("student_exam_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Button(
                                        onClick = {
                                            if (studentNameInput.isNotBlank()) {
                                                onRegister(studentNameInput, phoneInput, studentTargetExamInput.ifBlank { "চাকরির প্রস্তুতি" })
                                                isRegistrationSubmitted = true
                                            }
                                        },
                                        enabled = studentNameInput.isNotBlank(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("student_register_submit_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("শিক্ষার্থী হিসেবে রেজিস্ট্রেশন করুন", fontWeight = FontWeight.Bold)
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
