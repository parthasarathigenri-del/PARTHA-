package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AiMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatbotScreen(
    language: AppLanguage,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val initialGreeting = when (language) {
        AppLanguage.BENGALI -> "নমস্কার! আমি 'পাঠশালা AI গুরু'। চাকরি পরীক্ষার প্রস্তুতি, ইতিহাস, পাটিগণিত শর্টকাট, সংবিধান, সাধারণ জ্ঞান বা মক টেস্টের যেকোনো ডাউট আমাকে জিজ্ঞেস করতে পারেন।"
        AppLanguage.ENGLISH -> "Hello! I am 'Pathshala AI Tutor'. You can ask me any study doubts, history dates, math shortcuts, polity articles, or exam strategies."
        AppLanguage.HINDI -> "नमस्ते! मैं 'पाठशाला AI गुरु' हूँ। आप मुझसे सरकारी नौकरी परीक्षा, इतिहास, गणित शॉर्टकट, संविधान या किसी भी प्रश्न का संदेह पूछ सकते हैं।"
    }

    val messages = remember {
        mutableStateListOf(
            AiMessage(isUser = false, text = initialGreeting)
        )
    }

    val suggestedQuestions = when (language) {
        AppLanguage.BENGALI -> listOf(
            "WBCS ইতিহাসের জন্য গুরুত্বপূর্ণ আন্দোলনের তালিকা",
            "শতকরা ও লাভ-ক্ষতির দ্রুত শর্টকাট সূত্র",
            "মৌলিক অধিকার ও অনুচ্ছেদ ৩২ ব্যাখ্যা",
            "মক টেস্টে নেগেটিভ মার্কিং কমানোর কৌশল",
            "পশ্চিমবঙ্গের ভূগোল ও নদনদী"
        )
        AppLanguage.ENGLISH -> listOf(
            "Important Indian National Movement timeline",
            "Percentage & Profit-Loss shortcut tricks",
            "Article 32 & Constitutional Remedies explained",
            "Strategies to avoid negative marking in exams",
            "Geography of West Bengal key points"
        )
        AppLanguage.HINDI -> listOf(
            "भारतीय राष्ट्रीय आंदोलन की मुख्य समयरेखा",
            "प्रतिशत और लाभ-हानि के शॉर्टकट ट्रिक्स",
            "अनुच्छेद 32 और मौलिक अधिकारों की व्याख्या",
            "परीक्षा में नेगेटिव मार्किंग से बचने के उपाय",
            "संविधान के प्रमुख अनुच्छेदों की सूची"
        )
    }

    fun handleSend(query: String) {
        if (query.isBlank()) return
        messages.add(AiMessage(isUser = true, text = query))
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            delay(1200L) // natural reasoning delay

            val reply = generateAiAnswer(query, language)
            messages.add(AiMessage(isUser = false, text = reply))
            isThinking = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

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
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = when (language) {
                                        AppLanguage.BENGALI -> "পাঠশালা AI চ্যাটবট"
                                        AppLanguage.ENGLISH -> "Pathshala AI Tutor"
                                        AppLanguage.HINDI -> "पाठशाला AI चैटबॉट"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "24x7 সক্রিয়",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "কম্পিটিটিভ পাঠশালা • ইন্টেলিজেন্ট স্টাডি সহকারী",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ai_chat_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        messages.clear()
                        messages.add(AiMessage(isUser = false, text = initialGreeting))
                    }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "রিসেট চ্যাট")
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Suggested prompt chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestedQuestions) { prompt ->
                            SuggestionChip(
                                onClick = { handleSend(prompt) },
                                label = { Text(prompt, fontSize = 11.sp, maxLines = 1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }

                    // Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    when (language) {
                                        AppLanguage.BENGALI -> "যেকোনো চাকরির প্রস্তুতি প্রশ্ন বা ডাউট লিখুন..."
                                        AppLanguage.ENGLISH -> "Type any exam doubt or math problem..."
                                        AppLanguage.HINDI -> "कोई भी प्रश्न या संदेह पूछें..."
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        IconButton(
                            onClick = { handleSend(inputText) },
                            enabled = inputText.isNotBlank() && !isThinking,
                            colors = IconButtonDefaults.iconButtonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.testTag("ai_chat_send_btn")
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
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("ai_messages_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero info banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Navy900, PrimaryBlue)
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = "AI স্টাডি গাইড ও তাত্ক্ষণিক সমাধান",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ইতিহাসের সাল-তারিখ, জটিল অংকের শর্টকাট সূত্র বা সংবিধান সংক্রান্ত কোনো দ্বিধা থাকলে সরাসরি প্রশ্ন করুন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            items(messages) { msg ->
                val isUser = msg.isUser
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) PrimaryBlue else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = if (isUser) Color.White.copy(alpha = 0.8f) else PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isUser) "আপনি" else "পাঠশালা AI গুরু",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) Color.White.copy(alpha = 0.9f) else PrimaryBlue
                                )
                            }

                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = "AI চিন্তা করছে ও উত্তর প্রস্তুত করছে...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateAiAnswer(query: String, lang: AppLanguage): String {
    val q = query.lowercase()

    return when {
        q.contains("ইতিহাস") || q.contains("আন্দোলন") || q.contains("history") || q.contains("movement") -> {
            when (lang) {
                AppLanguage.BENGALI -> "📚 আধুনিক ভারতের গুরুত্বপূর্ণ আন্দোলন ও সময়রেখা:\n" +
                        "• ১৮৫৭: মহাবিদ্রোহ (লর্ড ক্যানিং)\n" +
                        "• ১৮৮৫: ভারতীয় জাতীয় কংগ্রেস প্রতিষ্ঠা (উমেশচন্দ্র বন্দ্যোপাধ্যায়)\n" +
                        "• ১৯০৫: বঙ্গভঙ্গ আন্দোলন ও স্বদেশী যুগ\n" +
                        "• ১৯১১: বঙ্গভঙ্গ রদ ও রাজধানী কলকাতা থেকে দিল্লিতে স্থানান্তর\n" +
                        "• ১৯১৯: জালিয়ানওয়ালাবাগ হত্যাকাণ্ড ও রাওলাট আইন\n" +
                        "• ১৯২০: অসহযোগ আন্দোলন (মহাত্মা গান্ধী)\n" +
                        "• ১৯৩০: ডান্ডি মার্চ ও আইন অমান্য আন্দোলন\n" +
                        "• ১৯৪২: ভারত ছাড়ো আন্দোলন ('করেঙ্গে ইয়া মরেঙ্গে')\n" +
                        "💡 টিপস: এই বছরগুলোতে গভর্নর জেনারেল কারা ছিলেন তা বিশেষভাবে লক্ষ্য রাখুন।"

                AppLanguage.ENGLISH -> "📚 Key Timeline of Indian National Movement:\n" +
                        "• 1857: Sepoy Mutiny / First War of Independence\n" +
                        "• 1885: Formation of Indian National Congress (W.C. Bonnerjee)\n" +
                        "• 1905: Partition of Bengal & Swadeshi Movement\n" +
                        "• 1911: Annulment of Partition of Bengal\n" +
                        "• 1919: Jallianwala Bagh Massacre & Rowlatt Act\n" +
                        "• 1920: Non-Cooperation Movement\n" +
                        "• 1930: Dandi March & Civil Disobedience Movement\n" +
                        "• 1942: Quit India Movement\n" +
                        "💡 Pro Tip: Frequently questions test the corresponding Viceroy/Governor General for each event."

                AppLanguage.HINDI -> "📚 भारतीय राष्ट्रीय आंदोलन के मुख्य पड़ाव:\n" +
                        "• 1857: प्रथम स्वतंत्रता संग्राम (लॉर्ड कैनिंग)\n" +
                        "• 1885: भारतीय राष्ट्रीय कांग्रेस की स्थापना\n" +
                        "• 1905: बंगाल विभाजन व स्वदेशी आंदोलन\n" +
                        "• 1911: बंगाल विभाजन रद्द\n" +
                        "• 1919: जलियांवाला बाग हत्याकांड\n" +
                        "• 1920: असहयोग आंदोलन\n" +
                        "• 1930: दांडी मार्च व सविनय अवज्ञा आंदोलन\n" +
                        "• 1942: भारत छोड़ो आंदोलन ('करो या मरो')"
            }
        }

        q.contains("শতকরা") || q.contains("লাভ") || q.contains("math") || q.contains("percentage") || q.contains("profit") -> {
            when (lang) {
                AppLanguage.BENGALI -> "📐 পাটিগণিত শর্টকাট ট্রিকস (শতকরা ও লাভ-ক্ষতি):\n" +
                        "১. চিনির বা তেলের দাম r% বাড়লে খরচ অপরিবর্তিত রাখতে ব্যবহার কমাতে হবে = [r / (100 + r)] × 100%\n" +
                        "২. দুটি ধারাবাহিক ছাড় (Discount) x% এবং y% হলে সমতুল্য একক ছাড় = (x + y - xy/100)%\n" +
                        "৩. ক্রয়মূল্য সমান রেখে একটি x% লাভে ও অন্যটি x% ক্ষতিতে বিক্রি করলে মোট লাভ বা ক্ষতি ০%। কিন্তু বিক্রয়মূল্য সমান হলে সর্বদা ক্ষতি হয় = (x/10)² %।\n" +
                        "💡 এই সূত্রগুলো চাকরির পরীক্ষায় গড়ে ৪০ সেকেন্ড সময় সাশ্রয় করে।"

                AppLanguage.ENGLISH -> "📐 Quantitative Aptitude Shortcuts:\n" +
                        "1. If price increases by r%, reduction in consumption to keep expenditure constant = [r / (100 + r)] * 100%.\n" +
                        "2. Successive discounts of x% and y% have a single equivalent discount = (x + y - xy/100)%.\n" +
                        "3. When two items are sold at the same selling price, one at x% gain and another at x% loss, there is an overall loss of (x/10)^2 %."

                AppLanguage.HINDI -> "📐 गणित के महत्वपूर्ण शॉर्टकट सूत्र:\n" +
                        "1. यदि किसी वस्तु के मूल्य में r% वृद्धि हो, तो खर्च समान रखने के लिए खपत में कमी = [r / (100 + r)] × 100%।\n" +
                        "2. दो क्रमिक छूट x% और y% की समतुल्य एकल छूट = (x + y - xy/100)%।\n" +
                        "3. यदि दो वस्तुएं समान विक्रय मूल्य पर बेची जाएं, एक पर x% लाभ और दूसरी पर x% हानि, तो कुल हानि = (x/10)² %।"
            }
        }

        q.contains("সংবিধান") || q.contains("ধারা") || q.contains("polity") || q.contains("article") -> {
            when (lang) {
                AppLanguage.BENGALI -> "🏛️ ভারতীয় সংবিধানের অতি-গুরুত্বপূর্ণ অনুচ্ছেদসমূহ:\n" +
                        "• অনুচ্ছেদ ১৪: আইনের দৃষ্টিতে সমতা\n" +
                        "• অনুচ্ছেদ ১৭: অস্পৃশ্যতা দূরীকরণ\n" +
                        "• অনুচ্ছেদ ২১: জীবন ও ব্যক্তিগত স্বাধীনতার সুরক্ষা\n" +
                        "• অনুচ্ছেদ ২১(A): শিক্ষার মৌলিক অধিকার\n" +
                        "• অনুচ্ছেদ ৩২: সংবিধানের প্রতিকারের অধিকার (সুপ্রিম কোর্টের রিট ক্ষমতা - সংবিধানের 'হৃদয় ও আত্মা')\n" +
                        "• অনুচ্ছেদ ৪৪: অভিন্ন দেওয়ানি বিধি (UCC)\n" +
                        "• অনুচ্ছেদ ৩২৪: নির্বাচন কমিশন\n" +
                        "• অনুচ্ছেদ ৩৫২, ৩৫৬, ৩৬০: জরুরি অবস্থা সংক্রান্ত ক্ষমতা।"

                AppLanguage.ENGLISH -> "🏛️ Most Frequently Tested Articles of the Constitution:\n" +
                        "• Article 14: Equality before law\n" +
                        "• Article 17: Abolition of untouchability\n" +
                        "• Article 21: Protection of life and personal liberty\n" +
                        "• Article 21A: Right to elementary education\n" +
                        "• Article 32: Right to constitutional remedies (Writ jurisdiction)\n" +
                        "• Article 44: Uniform Civil Code\n" +
                        "• Article 324: Election Commission of India\n" +
                        "• Articles 352, 356, 360: National, State & Financial Emergencies."

                AppLanguage.HINDI -> "🏛️ भारतीय संविधान के सबसे महत्वपूर्ण अनुच्छेद:\n" +
                        "• अनुच्छेद 14: विधि के समक्ष समानता\n" +
                        "• अनुच्छेद 17: अस्पृश्यता का अंत\n" +
                        "• अनुच्छेद 21: प्राण व दैहिक स्वतंत्रता का संरक्षण\n" +
                        "• अनुच्छेद 32: संवैधानिक उपचारों का अधिकार (सुप्रीम कोर्ट रिट जारी करने का अधिकार)\n" +
                        "• अनुच्छेद 44: समान नागरिक संहिता (UCC)\n" +
                        "• अनुच्छेद 324: निर्वाचन आयोग।"
            }
        }

        q.contains("নেগেটিভ") || q.contains("negative") || q.contains("কৌশল") -> {
            when (lang) {
                AppLanguage.BENGALI -> "🎯 মক টেস্টে নেগেটিভ মার্কিং এড়ানোর ৪টি গোল্ডেন রুল:\n" +
                        "১. ৫০-৫০ এলিমিনেশন পদ্ধতি: দুটি অপশন বাতিল করতে পারলে তবেই ঝুঁকি নিন; চারটি অপশনের কোনোটি জানা না থাকলে ছেড়ে দিন।\n" +
                        "২. তিন রাউন্ড টেকনিক: রাউন্ড ১-এ ১০০% নিশ্চিত প্রশ্নগুলো দাগান; রাউন্ড ২-এ যেসব প্রশ্নে দুটি অপশনের সংশয় আছে তা বিবেচনা করুন; রাউন্ড ৩-এ সময় মেপে রিভিউ করুন।\n" +
                        "৩. নেগেটিভ মার্কিং হিসাব: ৪টি ভুল হলে ১টি সঠিক প্রশ্নের মান কাটা যায়, যা মেধা তালিকায় হাজার র‍্যাঙ্ক পিছিয়ে দেয়।"

                AppLanguage.ENGLISH -> "🎯 3 Golden Rules to Minimize Negative Marking:\n" +
                        "1. 50-50 Elimination Method: Only guess when you can definitively eliminate at least two choices.\n" +
                        "2. Three-Pass Strategy: Pass 1: Guaranteed correct answers; Pass 2: Calculated questions; Pass 3: OMR review.\n" +
                        "3. Respect the cutoff: Every incorrect answer (-0.25) compounds quickly against your aggregate rank."

                AppLanguage.HINDI -> "🎯 नेगेटिव मार्किंग से बचने के 3 नियम:\n" +
                        "1. 50-50 उन्मूलन तकनीक: यदि आप दो विकल्पों को हटा सकते हैं, तभी उत्तर का चयन करें।\n" +
                        "2. तीन राउंड में प्रश्न हल करें: पहले केवल वे प्रश्न करें जो 100% आते हैं।\n" +
                        "3. कटऑफ का ध्यान रखें: 4 गलत उत्तर एक सही प्रश्न का अंक समाप्त कर देते हैं।"
            }
        }

        else -> {
            when (lang) {
                AppLanguage.BENGALI -> "ধন্যবাদ আপনার প্রশ্নের জন্য! কম্পিটিটিভ পাঠশালার সিলেবাস অনুযায়ী এই বিষয়ের মূল ধারণাটি হল:\n" +
                        "• যেকোনো সরকারি চাকরির পরীক্ষায় প্রাথমিক ধারণা (Basic Concept) পরিষ্কার রাখা সবচেয়ে জরুরি।\n" +
                        "• পূর্ববর্তী বছরের প্রশ্নপত্র (PYQ) নিয়মিত সমাধান করুন এবং অ্যাপের মক টেস্টে নিয়মিত অংশ নিন।\n" +
                        "আপনার কি এই টপিকের নির্দিষ্ট কোনো অধ্যায় বা সূত্র নিয়ে আরো কোনো প্রশ্ন আছে?"

                AppLanguage.ENGLISH -> "Thank you for asking! According to the competitive exam syllabus:\n" +
                        "• Clear fundamentals and regular practice of Previous Year Questions (PYQs) are key.\n" +
                        "• Make sure to review your errors in the Mock Test review section using the 'Explain' button.\n" +
                        "Feel free to specify any topic or question number for a detailed breakdown!"

                AppLanguage.HINDI -> "आपके प्रश्न के लिए धन्यवाद! प्रतियोगी परीक्षाओं की तैयारी के लिए:\n" +
                        "• मूलभूत अवधारणाओं (Fundamentals) को मजबूत रखें और नियमित मॉक टेस्ट दें।\n" +
                        "• आप इस विषय के किसी विशेष सूत्र या अध्याय के बारे में भी विस्तार से पूछ सकते हैं।"
            }
        }
    }
}
