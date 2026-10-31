package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.theme.CrimsonRed

@Composable
fun AccessRestrictedScreen(
    student: Student,
    onSwitchToAdmin: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("access_restricted_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = CrimsonRed.copy(alpha = 0.1f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "অ্যাক্সেস লক",
                            tint = CrimsonRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "ইনস্টিটিউট অ্যাক্সেস স্থগিত রয়েছে",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "নমস্কার ${student.name},\nসংকল্প একাডেমি অ্যাপটি শুধুমাত্র অনুমোদিত শিক্ষার্থীদের জন্য সংরক্ষিত। আপনার রোল নম্বর (${student.rollNo})-এর জন্য অ্যাডমিন অনুমোদন বর্তমানে নিষ্ক্রিয় রয়েছে।",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "ইনস্টিটিউট হেল্পডেস্ক",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+91 98300 00000 / info@sankalpacademy.org",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onSwitchToAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_admin_switch_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অ্যাডমিন প্যানেলে গিয়ে অনুমোদন প্রদান করুন (Demo)")
                }
            }
        }
    }
}
