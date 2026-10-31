package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesPaymentScreen(
    student: Student,
    fees: List<FeePayment>,
    selectedFeeToPay: FeePayment?,
    latestReceipt: FeePayment?,
    onOpenPayDialog: (FeePayment) -> Unit,
    onClosePayDialog: () -> Unit,
    onProcessPayment: (String) -> Unit,
    onCloseReceipt: () -> Unit,
    onBack: () -> Unit
) {
    var selectedUpiApp by remember { mutableStateOf("Google Pay (UPI)") }
    val upiOptions = listOf("Google Pay (UPI)", "PhonePe (UPI)", "Paytm (UPI)", "BHIM UPI")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ইনস্টিটিউট অনলাইন ফি পেমেন্ট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "রোল: ${student.rollNo} • ${student.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("fees_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("fees_list_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "সংকল্প একাডেমি পেমেন্ট পোর্টাল",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "সকল প্রকার মাসিক টিউশন ফি, স্পেশাল মক টেস্ট প্যাক ও অনলাইন স্টাডি ম্যাটেরিয়াল চার্জ নিরাপদ UPI গেটওয়ের মাধ্যমে পরিশোধ করে সাথে সাথে ডিজিটাল রসিদ সংগ্রহ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "ফি বিবরণী ও হিসেবসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(fees) { fee ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fee_item_${fee.id}"),
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
                            Text(
                                text = fee.billingMonth,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (fee.isPaid) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (fee.isPaid) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                        contentDescription = null,
                                        tint = if (fee.isPaid) EmeraldGreen else CrimsonRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (fee.isPaid) "পরিশোধিত (PAID)" else "বকেয়া (PENDING)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (fee.isPaid) EmeraldGreen else CrimsonRed
                                    )
                                }
                            }
                        }

                        Text(
                            text = fee.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "পরিমাণ (Amount)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${fee.amount.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (fee.isPaid) "পরিশোধের তারিখ" else "জমার শেষ দিন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (fee.isPaid) (fee.paymentDate ?: "") else fee.dueDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        if (fee.isPaid) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Txn ID: ${fee.transactionId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { onOpenPayDialog(fee) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("view_receipt_btn_${fee.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রসিদ দেখুন", fontSize = 11.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = { onOpenPayDialog(fee) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pay_now_btn_${fee.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("অনলাইনে ফি জমা দিন (Pay ₹${fee.amount.toInt()})")
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Gateway Simulator Dialog
    if (selectedFeeToPay != null && !selectedFeeToPay.isPaid) {
        AlertDialog(
            onDismissRequest = onClosePayDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryBlue)
                    Text("অনলাইন পেমেন্ট গেটওয়ে")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = selectedFeeToPay.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "মোট প্রদেয় ফি: ₹${selectedFeeToPay.amount.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlue
                    )

                    Divider()

                    Text(
                        text = "পেমেন্টের মাধ্যম নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    upiOptions.forEach { method ->
                        val isSelected = selectedUpiApp == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) BorderStroke(1.5.dp, PrimaryBlue) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedUpiApp = method }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    Text(text = method, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                }
                                RadioButton(selected = isSelected, onClick = { selectedUpiApp = method })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onProcessPayment(selectedUpiApp) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("confirm_payment_btn")
                ) {
                    Text("পেমেন্ট সম্পন্ন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = onClosePayDialog) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Digital Receipt Modal
    if (latestReceipt != null || (selectedFeeToPay != null && selectedFeeToPay.isPaid)) {
        val receipt = latestReceipt ?: selectedFeeToPay!!
        AlertDialog(
            onDismissRequest = onCloseReceipt,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                    Text("ডিজিটাল পেমেন্ট রসিদ")
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "সংকল্প একাডেমি - চাকরির প্রস্তুতি",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Divider()
                        Text("শিক্ষার্থীর নাম: ${student.name}", style = MaterialTheme.typography.bodySmall)
                        Text("রোল নম্বর: ${student.rollNo}", style = MaterialTheme.typography.bodySmall)
                        Text("বিষয়: ${receipt.title}", style = MaterialTheme.typography.bodySmall)
                        Text("প্রদেয় অর্থ: ₹${receipt.amount.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Text("পেমেন্ট মেথড: ${receipt.paymentMethod ?: "UPI"}", style = MaterialTheme.typography.bodySmall)
                        Text("ট্রানজ্যাকশন আইডি: ${receipt.transactionId}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("তারিখ: ${receipt.paymentDate}", style = MaterialTheme.typography.bodySmall)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EmeraldGreen.copy(alpha = 0.2f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "✓ ফি সফলভাবে গৃহীত হয়েছে",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onCloseReceipt) {
                    Text("রসিদ সেভ / ক্লোজ করুন")
                }
            }
        )
    }
}
