package com.example.ui

import android.content.ClipboardManager
import android.content.Context
import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SpamRecord
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class Screen {
    Analyze, History, About
}

@Composable
fun SpamShieldApp(
    viewModel: SpamViewModel,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.Analyze) }
    val predictState by viewModel.predictUiState.collectAsStateWithLifecycle()
    val historyList by viewModel.historyRecords.collectAsStateWithLifecycle()

    // Interactive popups for history details
    var selectedRecordForDialog by remember { mutableStateOf<SpamRecord?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        bottomBar = {
            GlassBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { screen ->
                    currentScreen = screen
                    if (screen == Screen.Analyze) {
                        viewModel.resetPredictionState()
                    }
                }
            )
        }
    ) { innerPadding ->
        // Premium particle/gradient background directly drawn behind content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawBehind {
                    if (isDarkTheme) {
                        // Accent purple ambient light source at the top
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricPurple.copy(alpha = 0.12f), Color.Transparent),
                                center = Offset(size.width * 0.2f, 0f),
                                radius = size.width * 0.8f
                            ),
                            center = Offset(size.width * 0.2f, 0f),
                            radius = size.width * 0.8f
                        )
                        // Accent cyan light source at bottom right
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(size.width * 0.8f, size.height),
                                radius = size.width * 0.9f
                            ),
                            center = Offset(size.width * 0.8f, size.height),
                            radius = size.width * 0.9f
                        )
                    }
                }
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 250),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Analyze -> AnalyzeScreen(
                        predictState = predictState,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onAnalyze = { emailText ->
                            viewModel.classifyEmail(emailText)
                        },
                        onReset = {
                            viewModel.resetPredictionState()
                        }
                    )
                    Screen.History -> HistoryScreen(
                        records = historyList,
                        onClearAll = {
                            viewModel.clearHistory()
                            Toast.makeText(context, "History logs cleared successfully", Toast.LENGTH_SHORT).show()
                        },
                        onRecordClick = { record ->
                            selectedRecordForDialog = record
                        }
                    )
                    Screen.About -> AboutScreen()
                }
            }

            // Record Details Popup Dialog
            selectedRecordForDialog?.let { record ->
                HistoryDetailDialog(
                    record = record,
                    onDismiss = { selectedRecordForDialog = null }
                )
            }
        }
    }
}

// ==========================================
// 1. HOME / ANALYZE SCREEN
// ==========================================
@Composable
fun AnalyzeScreen(
    predictState: PredictUiState,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onAnalyze: (String) -> Unit,
    onReset: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Identity Brand Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(ElectricPurple, NeonCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = "Shield Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SpamShield",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Dark / Light Mode Toggle icon button
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "DEEP LEARNING AI V2.0",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = NeonCyan
            )
        }

        // Conditionally render input area and actions or result page
        AnimatedContent(
            targetState = predictState,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "StateTransition"
        ) { state ->
            when (state) {
                is PredictUiState.Idle, is PredictUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (state is PredictUiState.Error) {
                            GlassCard(
                                borderColor = SpamRed.copy(alpha = 0.5f),
                                backgroundColor = DarkSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = "Error icon",
                                        tint = SpamRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SpamRedGlow
                                    )
                                }
                            }
                        }

                        // Input Box Header with Clipboard functionality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Email Content",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            // Paste pill button
                            Card(
                                shape = RoundedCornerShape(50.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, TextMuted.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clickable {
                                        val pasteText = clipboardManager.getText()?.text
                                        if (!pasteText.isNullOrEmpty()) {
                                            emailInput = pasteText
                                            Toast.makeText(context, "Pasted text from clipboard", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .testTag("paste_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentPaste,
                                        contentDescription = "Paste Icon",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paste From Clipboard",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        // Glassmorphic Input Text Area with Ambient Glow Backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // Glowing soft background backdrop (asymmetry glow feel)
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(4.dp)
                                    .offset(y = 2.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                ElectricPurple.copy(alpha = 0.12f),
                                                NeonCyan.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
                            )

                            GlassCard(
                                borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                backgroundColor = if (isDarkTheme) Color(0xCC1A1A1A) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    placeholder = {
                                        Text(
                                            text = "Paste header details or email body context to evaluate threat potential...",
                                            color = TextMuted,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp)
                                        .testTag("email_input_field"),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = ElectricPurple,
                                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                            }
                        }

                        // Helper buttons for generating Random Spam and Random Ham
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = {
                                emailInput = "URGENT: Your account has been suspended due to suspicious activities. Please verify your identity immediately by clicking the link to restore access and win a $500 Walmart Gift Card."
                            }) {
                                Icon(Icons.Rounded.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Random Spam", style = MaterialTheme.typography.labelMedium)
                            }
                            TextButton(onClick = {
                                emailInput = "Hi team, Just a quick reminder that our weekly sync is scheduled for tomorrow at 10 AM. Please make sure to update your status reports before the meeting. Thanks, Sarah"
                            }) {
                                Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Random Ham", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Action Call Button with search icon
                        GradientButton(
                            text = "Analyze Email",
                            icon = Icons.Rounded.Search,
                            onClick = {
                                focusManager.clearFocus()
                                onAnalyze(emailInput)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("analyze_button")
                        )
                    }
                }

                is PredictUiState.Loading -> {
                    NetworkLoaderWidget()
                }

                is PredictUiState.Success -> {
                    VerdictDisplayWidget(
                        successResult = state,
                        onReset = {
                            emailInput = ""
                            onReset()
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. DETAILED SHIMMER / CYBERPUNK LOADING COMPONENT
// ==========================================
@Composable
fun NetworkLoaderWidget() {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    // Gradient slide effect
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // Scientific phrases cycled sequentially using state side-effect
    var currentPhaseText by remember { mutableStateOf("Initializing pipeline parameters...") }
    val phases = listOf(
        "Initializing pipeline parameters...",
        "Applying token preprocessing and cleaning...",
        "Executing TF-IDF vector weight calculations...",
        "Filtering descriptors via Chi-Square (χ²) thresholds...",
        "Routing vector arrays to BiLSTM deep neurons...",
        "Computing Sigmoid categorical projection...",
        "Finalizing classification verdict..."
    )

    LaunchedEffect(Unit) {
        var idx = 0
        while (true) {
            delay(1200)
            idx = (idx + 1) % phases.size
            currentPhaseText = phases[idx]
        }
    }

    GlassCard(
        borderColor = ElectricPurple.copy(alpha = 0.5f),
        backgroundColor = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("loading_skeleton")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spinning cyber loader
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(ElectricPurple, NeonCyan, Color.Transparent)
                            ),
                            startAngle = rotation,
                            sweepAngle = 280f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "HYBRID CLASSIFIER EVALUATION",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentPhaseText,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(48.dp) // Avoid layout jumps during text transition
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                color = ElectricPurple,
                trackColor = DarkSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
        }
    }
}

// ==========================================
// 3. ANALYSIS VERDICT CARD DISPLAY
// ==========================================
@Composable
fun VerdictDisplayWidget(
    successResult: PredictUiState.Success,
    onReset: () -> Unit
) {
    val isSpam = successResult.prediction.lowercase().trim() == "spam"
    val accentColor = if (isSpam) SpamRed else SafeGreen
    val accentGlow = if (isSpam) SpamRedGlow else SafeGreenGlow
    val headerVerdict = if (isSpam) "SPAM DETECTED" else "SAFE SECURE (HAM)"
    val statusIcon = if (isSpam) Icons.Rounded.Error else Icons.Rounded.CheckCircle
    
    // Confidence percentage representation (0.0 to 1.0 converts to %)
    val percentage = (successResult.confidence * 100).toInt()

    // Smooth animation on progress bar
    var progressAnimValue by remember { mutableStateOf(0f) }
    LaunchedEffect(successResult) {
        animate(
            initialValue = 0f,
            targetValue = successResult.confidence.toFloat(),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { valValue, _ ->
            progressAnimValue = valValue
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("verdict_screen_card"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Verdict Card main
        GlassCard(
            borderColor = accentColor.copy(alpha = 0.6f),
            backgroundColor = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Threat Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.5.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Threat Icon",
                        tint = accentColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = headerVerdict,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = accentColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Percentage Pill
                Card(
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Confidence Level: $percentage%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = accentGlow,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confidence Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Model Confidence",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Text(
                            text = "${(progressAnimValue * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressAnimValue },
                        color = accentColor,
                        trackColor = DarkSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Text preview
                Text(
                    text = "Analyzed Snippet:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Text(
                        text = successResult.emailText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Button
        GradientButton(
            text = "Back to Analyzer",
            icon = Icons.Rounded.ArrowBack,
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .testTag("reset_after_verdict_button")
        )
    }
}

// ==========================================
// 4. HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(
    records: List<SpamRecord>,
    onClearAll: () -> Unit,
    onRecordClick: (SpamRecord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("history_screen")
    ) {
        // History Page Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Analysis History",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Last 10 audits cached locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (records.isNotEmpty()) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier
                        .background(SpamRed.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, SpamRed.copy(alpha = 0.3f), CircleShape)
                        .testTag("clear_history_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Clear analysis history logs",
                        tint = SpamRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = "No Records found",
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Clean Threat Registry",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste or type sample payloads into the checker flow to stream offline records here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("records_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryItemRow(
                        record = record,
                        onClick = { onRecordClick(record) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    record: SpamRecord,
    onClick: () -> Unit
) {
    val isSpam = record.prediction.lowercase().trim() == "spam"
    val accentColor = if (isSpam) SpamRed else SafeGreen
    val verdictLabel = if (isSpam) "SPAM" else "SAFE"
    val timeLabel = DateUtils.getRelativeTimeSpanString(
        record.timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )

    GlassCard(
        borderColor = accentColor.copy(alpha = 0.25f),
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Small Indicator Box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verdictLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = accentColor,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text snippets
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.emailText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence: ${(record.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "• $timeLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = "View detail",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ==========================================
// 5. DETAILED HISTORY RECORD POPUP DIALOG
// ==========================================
@Composable
fun HistoryDetailDialog(
    record: SpamRecord,
    onDismiss: () -> Unit
) {
    val isSpam = record.prediction.lowercase().trim() == "spam"
    val accentColor = if (isSpam) SpamRed else SafeGreen
    val verdictLabel = if (isSpam) "SPAM DETECTED" else "SAFE SECURE (HAM)"
    val dateText = DateUtils.formatDateTime(
        LocalContext.current,
        record.timestamp,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_YEAR
    )

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            borderColor = accentColor.copy(alpha = 0.5f),
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .testTag("history_item_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Threat Audit Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close detailed log popup",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Tag
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSpam) Icons.Rounded.Error else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$verdictLabel [${(record.confidence * 100).toInt()}%]",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date label
                Text(
                    text = "TIMESTOP RECORD",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Full text
                Text(
                    text = "AUDIT BODY PAYLOAD",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = record.emailText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Audit File", color = TextPrimary)
                }
            }
        }
    }
}

// ==========================================
// 6. ABOUT / CS ACADEMIC ARCHITECTURE SCREEN
// ==========================================
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
            .testTag("about_screen")
    ) {
        Text(
            text = "Model Architecture",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Computer Science Computerized Classifier Details",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Project Info Block
        GlassCard(
            borderColor = NeonCyan.copy(alpha = 0.3f),
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FINAL YEAR PROJECT TITLED:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"A Hybrid Feature Selection for Email Spam Classification Using Deep Learning\"",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HYBRID MACHINE LEARNING PIPELINE",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = TextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Phase Cards
        AboutInfographicCard(
            stepNumber = "01",
            title = "TF-IDF Weighted Extraction",
            description = "The input textual payload is normalized via lowercasing, stop-word elimination, and tokenization. It's then projected into matrix fields mapping Term Frequency-Inverse Document Frequency weight vectors.",
            accentColor = ElectricPurple
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutInfographicCard(
            stepNumber = "02",
            title = "Chi-Square (χ²) Filtering",
            description = "To solve high-dimensionality noise, a mathematical Chi-Square statistical validation test filter ranks the term vectors. Only terminologies with solid mathematical dependencies on the target spam metadata are preserved, speeding up execution and boosting classifier accuracy.",
            accentColor = NeonCyan
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutInfographicCard(
            stepNumber = "03",
            title = "BiLSTM Deep Categorizer",
            description = "The filtered features feed into a recurrent Bidirectional Long Short-Term Memory deep network. Capturing forward and backward semantic sequence context, the BiLSTM layers projects arrays into a Sigmoid dense layer to map predictions cleanly to 'Spam' or 'Ham'.",
            accentColor = PurplePrimary
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AboutInfographicCard(
    stepNumber: String,
    title: String,
    description: String,
    accentColor: Color
) {
    GlassCard(
        borderColor = accentColor.copy(alpha = 0.25f),
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Circular badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}


// ==========================================
// Generic UI / REUSABLE STYLED BLOCKS
// ==========================================

// GORGEOUS GLASSMORPHIC CARD CONTAINER
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = backgroundColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        content()
    }
}

// DYNAMIC ACCENT GRADIENT BUTTON
@Composable
fun GradientButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(ElectricPurple, NeonCyan))),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

// GLASS BOTTOM NAVIGATION MODULE (respects safe navigation bars)
@Composable
fun GlassBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // Respect Android Safe gesture bar insets
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("app_navigation_bar"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Rounded.Shield,
                label = "Scan",
                selected = currentScreen == Screen.Analyze,
                onClick = { onScreenSelected(Screen.Analyze) },
                testTag = "nav_item_analyze"
            )
            BottomNavItem(
                icon = Icons.Rounded.History,
                label = "History",
                selected = currentScreen == Screen.History,
                onClick = { onScreenSelected(Screen.History) },
                testTag = "nav_item_history"
            )
            BottomNavItem(
                icon = Icons.Rounded.Info,
                label = "About",
                selected = currentScreen == Screen.About,
                onClick = { onScreenSelected(Screen.About) },
                testTag = "nav_item_about"
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val textColor = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val iconColor = if (selected) NeonCyan else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                ),
                color = textColor,
                maxLines = 1
            )
            
            // Subtle neon cyan line under selected tab
            if (selected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(NeonCyan)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
