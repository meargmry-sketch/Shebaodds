package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.PredictionResult
import com.example.data.model.Bet
import com.example.data.model.SportMatch
import com.example.data.model.TransactionRecord
import com.example.data.model.UserWallet
import com.example.data.model.leagueName
import com.example.ui.theme.*
import com.example.viewmodel.BetViewModel
import com.example.viewmodel.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

// ==========================================
// 1. DASHBOARD HUB SCREEN
// ==========================================
// 1. DASHBOARD HUB SCREEN
// ==========================================

@Composable
fun DashboardSkeletonView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBG)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shimmering simulated header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(24.dp)
                    .background(SlateSurfaceL2, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SlateSurfaceL2, CircleShape)
            )
        }
        
        // Wallet skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SlateCardBG.copy(alpha = 0.6f))
                .border(BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
        )
        
        // Trending header
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(20.dp)
                .background(SlateSurfaceL2, RoundedCornerShape(4.dp))
        )
        
        // Horizontal cards skeleton
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(135.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SlateCardBG.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                )
            }
        }
        
        // Categories list
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(38.dp)
                        .background(SlateSurfaceL2, RoundedCornerShape(100.dp))
                )
            }
        }
        
        // Standard match skeleton
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateCardBG.copy(alpha = 0.6f))
                    .border(BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun MatchOddsTrendSparkline(
    matchId: Int,
    currentOdds: Double,
    modifier: Modifier = Modifier
) {
    // Generate deterministic history using matchId as seed to prevent jitter
    val history = remember(matchId, currentOdds) {
        val random = java.util.Random(matchId.toLong() * 41L)
        val list = mutableListOf<Float>()
        var lastValue = currentOdds
        for (i in 0 until 5) {
            val drift = (random.nextDouble() - 0.5) * 0.16 // up to 16% potential path fluctuation
            lastValue = lastValue * (1.1 + drift).coerceAtLeast(0.1)
            list.add(0, lastValue.toFloat().coerceAtLeast(1.05f))
        }
        list.add(currentOdds.toFloat())
        list
    }

    val initial = history.first()
    val final = history.last()
    val change = ((final - initial) / initial) * 100
    // If odds decreased, it means probability increased -> Green. If odds increased -> Red.
    val trendColor = if (change < -0.3) NeonGreen else if (change > 0.3) LightRed else TextMuted
    val trendIcon = if (change < -0.3) Icons.Default.TrendingDown else if (change > 0.3) Icons.Default.TrendingUp else Icons.Default.TrendingFlat

    Row(
        modifier = modifier.testTag("match_trend_sparkline_${matchId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val minVal = history.minOrNull() ?: 1.0f
        val maxVal = history.maxOrNull() ?: 10.0f
        val range = (maxVal - minVal).coerceAtLeast(0.01f)

        Box(
            modifier = Modifier
                .width(55.dp)
                .height(28.dp)
                .padding(vertical = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val points = history.mapIndexed { idx, value ->
                    val x = idx * (width / (history.size - 1))
                    val y = height - ((value - minVal) / range) * height
                    Offset(x, y)
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val previous = points[i - 1]
                            val current = points[i]
                            val controlX1 = previous.x + (current.x - previous.x) / 2f
                            val controlY1 = previous.y
                            val controlX2 = previous.x + (current.x - previous.x) / 2f
                            val controlY2 = current.y
                            cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
                        }
                    }
                }

                val areaPath = Path().apply {
                    addPath(path)
                    if (points.isNotEmpty()) {
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(trendColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = path,
                    color = trendColor,
                    style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round)
                )

                drawPath(
                    path = path,
                    color = trendColor.copy(alpha = 0.2f),
                    style = Stroke(width = 3.6f.dp.toPx(), cap = StrokeCap.Round)
                )

                if (points.isNotEmpty()) {
                    drawCircle(
                        color = trendColor,
                        radius = 2.2f.dp.toPx(),
                        center = points.last()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%+.1f%%", change),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }
            Text(
                text = "60m Odds",
                fontSize = 7.5.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TrendingMatchCard(
    match: SportMatch,
    selectedSelection: String? = null,
    onOddsClick: (String, Double) -> Unit,
    onAnalyzeClick: () -> Unit,
    isTracked: Boolean = false,
    onTrackClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .testTag("trending_match_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header Row: Sport tag + Dynamic Live-Pulse Ticker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTrackClick,
                        modifier = Modifier.size(24.dp).testTag("track_match_${match.id}")
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Track Match Odds",
                            tint = if (isTracked) AmberAccent else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    val sportIcon = when (match.sport) {
                        "Football" -> Icons.Default.SportsSoccer
                        "Basketball" -> Icons.Default.SportsBasketball
                        "Tennis" -> Icons.Default.SportsBaseball
                        "Esports" -> Icons.Default.SportsEsports
                        else -> Icons.Default.Sports
                    }
                    Icon(
                        imageVector = sportIcon,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.sport.uppercase(),
                        fontSize = 9.s_p,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent
                    )
                }
                
                // Hot Tag
                Box(
                    modifier = Modifier
                        .background(NeonGreen.copy(0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Trending Match Icon",
                            tint = NeonGreen,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "HOT TICKET",
                            fontSize = 8.s_p,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Match Teams + Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.teamA,
                        fontSize = 12.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = match.teamB,
                        fontSize = 12.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Score block or Live time display
                if (match.isLive) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${match.scoreA} - ${match.scoreB}",
                            fontSize = 14.s_p,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = match.timeString,
                                fontSize = 9.s_p,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(SlateSurfaceL2, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UPCOMING",
                            fontSize = 8.s_p,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Realtime Trend",
                        fontSize = 8.5.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                MatchOddsTrendSparkline(
                    matchId = match.id,
                    currentOdds = match.odds1
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            val is1Selected = selectedSelection == "1"
            val isXSelected = selectedSelection == "X"
            val is2Selected = selectedSelection == "2"

            // 1X2 Rapid Odds Buttons directly inside the Trending Card!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Team A Win Odds (Selection 1)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (is1Selected) AmberAccent.copy(alpha = 0.15f) else SlateSurfaceL2, RoundedCornerShape(6.dp))
                        .border(1.dp, if (is1Selected) AmberAccent else BorderColor.copy(0.4f), RoundedCornerShape(6.dp))
                        .clickable { onOddsClick("1", match.odds1) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "1", fontSize = 8.s_p, color = if (is1Selected) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%.2f", match.odds1),
                            fontSize = 10.s_p,
                            color = if (is1Selected) AmberAccent else TextWhite,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                // Draw Odds (Selection X) - if relevant (football has oddsX > 1.0)
                if (match.oddsX > 1.0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (isXSelected) AmberAccent.copy(alpha = 0.15f) else SlateSurfaceL2, RoundedCornerShape(6.dp))
                            .border(1.dp, if (isXSelected) AmberAccent else BorderColor.copy(0.4f), RoundedCornerShape(6.dp))
                            .clickable { onOddsClick("X", match.oddsX) }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "X", fontSize = 8.s_p, color = if (isXSelected) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%.2f", match.oddsX),
                                fontSize = 10.s_p,
                                color = if (isXSelected) AmberAccent else TextWhite,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                // Team B Win Odds (Selection 2)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (is2Selected) AmberAccent.copy(alpha = 0.15f) else SlateSurfaceL2, RoundedCornerShape(6.dp))
                        .border(1.dp, if (is2Selected) AmberAccent else BorderColor.copy(0.4f), RoundedCornerShape(6.dp))
                        .clickable { onOddsClick("2", match.odds2) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "2", fontSize = 8.s_p, color = if (is2Selected) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%.2f", match.odds2),
                            fontSize = 10.s_p,
                            color = if (is2Selected) AmberAccent else TextWhite,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeaturedMatchCard(
    match: SportMatch,
    selectedSelection: String? = null,
    onOddsClick: (String, Double) -> Unit,
    onAnalyzeClick: () -> Unit,
    isTracked: Boolean = false,
    onTrackClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .testTag("featured_match_card_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.2f.dp, if (match.isLive) AmberAccent else BorderColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Badges row: Sport name + Ticker badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTrackClick,
                        modifier = Modifier.size(24.dp).testTag("track_match_featured_${match.id}")
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Track Match Odds",
                            tint = if (isTracked) AmberAccent else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    val sportIcon = when (match.sport) {
                        "Football" -> Icons.Default.SportsSoccer
                        "Basketball" -> Icons.Default.SportsBasketball
                        "Tennis" -> Icons.Default.SportsBaseball
                        "Esports" -> Icons.Default.SportsEsports
                        else -> Icons.Default.Sports
                    }
                    Icon(
                        imageVector = sportIcon,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.sport.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                if (match.isLive) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF4444).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LIVE: " + match.timeString,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF00C2FF).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FEATURED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00C2FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Showdown Match Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.teamA,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = match.teamB,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (match.isLive) {
                    Box(
                        modifier = Modifier
                            .background(SlateSurfaceL2, RoundedCornerShape(6.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${match.scoreA} - ${match.scoreB}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Upcoming Scheduled",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (match.isLive) "Real-time feeds active" else match.dateTimeString,
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Real-time Trend",
                        fontSize = 8.5.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                MatchOddsTrendSparkline(
                    matchId = match.id,
                    currentOdds = match.odds1
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Rapid Odds Selection + AI prediction click
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val is1Sel = selectedSelection == "1"
                val isXSel = selectedSelection == "X"
                val is2Sel = selectedSelection == "2"

                // 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (match.isLocked) SlateDarkBG.copy(alpha = 0.5f) 
                            else if (is1Sel) AmberAccent.copy(alpha = 0.12f) 
                            else SlateSurfaceL2, 
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (match.isLocked) SlateSurfaceL2 else if (is1Sel) AmberAccent else BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !match.isLocked) { onOddsClick("1", match.odds1) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("1", fontSize = 8.sp, color = if (match.isLocked) TextMuted else if (is1Sel) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                        if (match.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = LightRed,
                                modifier = Modifier.size(10.dp)
                            )
                        } else {
                            Text(String.format("%.2f", match.odds1), fontSize = 10.sp, color = if (is1Sel) AmberAccent else TextWhite, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // X Draw
                if (match.oddsX > 1.0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (match.isLocked) SlateDarkBG.copy(alpha = 0.5f) 
                                else if (isXSel) AmberAccent.copy(alpha = 0.12f) 
                                else SlateSurfaceL2, 
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (match.isLocked) SlateSurfaceL2 else if (isXSel) AmberAccent else BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable(enabled = !match.isLocked) { onOddsClick("X", match.oddsX) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Draw", fontSize = 8.sp, color = if (match.isLocked) TextMuted else if (isXSel) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                            if (match.isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = LightRed,
                                    modifier = Modifier.size(10.dp)
                                )
                            } else {
                                Text(String.format("%.2f", match.oddsX), fontSize = 10.sp, color = if (isXSel) AmberAccent else TextWhite, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // 2
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (match.isLocked) SlateDarkBG.copy(alpha = 0.5f) 
                            else if (is2Sel) AmberAccent.copy(alpha = 0.12f) 
                            else SlateSurfaceL2, 
                            RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, if (match.isLocked) SlateSurfaceL2 else if (is2Sel) AmberAccent else BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !match.isLocked) { onOddsClick("2", match.odds2) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("2", fontSize = 8.sp, color = if (match.isLocked) TextMuted else if (is2Sel) AmberAccent else TextMuted, fontWeight = FontWeight.Bold)
                        if (match.isLocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = LightRed,
                                modifier = Modifier.size(10.dp)
                            )
                        } else {
                            Text(String.format("%.2f", match.odds2), fontSize = 10.sp, color = if (is2Sel) AmberAccent else TextWhite, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // AI Expert Analytics
                IconButton(
                    onClick = onAnalyzeClick,
                    modifier = Modifier
                        .size(34.dp)
                        .background(SlateSurfaceL2, CircleShape)
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Expert Insight",
                        tint = Color(0xFF00C2FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("dashboard_search_bar_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Matches",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search matches, leagues, team names...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_search_input_field"),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextLight,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = PrimarySapphire
                )
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp).testTag("dashboard_search_clear_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserBalanceWidget(
    wallet: UserWallet,
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    val bioEnabled by viewModel.biometricQuickBetEnabled.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_balance_widget"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Icon",
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AVAILABLE FUNDS",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NeonGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "REAL-TIME",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format("%,.2f", wallet.balance),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        modifier = Modifier.testTag("user_balance_value")
                    )
                    Text(
                        text = wallet.currency,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = if (bioEnabled) NeonGreen else TextMuted,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (bioEnabled) "TOUCHID ACTIVE" else "TOUCHID STANDBY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bioEnabled) NeonGreen else TextMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "SECURE LEDGER V2",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }
            }

            Button(
                onClick = { showDepositDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimarySapphire,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .testTag("user_balance_widget_deposit_btn")
                    .border(
                        width = 1.dp,
                        color = NeonGreen.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCard,
                        contentDescription = "Deposit Icon",
                        tint = TextWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "DEPOSIT FUNDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (showDepositDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = { showDepositDialog = false }
        )
    }
}

@Composable
fun DepositFundsDialog(
    viewModel: BetViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialAmount: Double? = null
) {
    var phoneNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(initialAmount?.toInt()?.toString() ?: "") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    var stepState by remember { mutableStateOf("INPUT") }
    var progressMessage by remember { mutableStateOf("") }
    var outTradeNo by remember { mutableStateOf("") }
    var signaturePayload by remember { mutableStateOf("") }
    var simPinCode by remember { mutableStateOf("") }
    var cancelOptionEnabled by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val availablePresets = listOf(100.0, 500.0, 1000.0, 5000.0)

    Dialog(
        onDismissRequest = {
            if (cancelOptionEnabled && (stepState == "INPUT" || stepState == "SUCCESS" || stepState == "FAILED")) {
                onDismissRequest()
            }
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("deposit_funds_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Crossfade(targetState = stepState, label = "deposit_steps") { currentStep ->
                when (currentStep) {
                    "INPUT" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = "Telebirr Logo",
                                        tint = Color(0xFF00C2FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "TELEBIRR GATEWAY",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TextWhite,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Automated secure checkout platform",
                                            fontSize = 8.5.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Divider(color = BorderColor, thickness = 1.dp)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF00C2FF).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF00C2FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF00C2FF),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "By submitting, you authorize a real-time secure USSD push bill. Settle seamlessly with RSA cryptographic handshake signature.",
                                        fontSize = 9.sp,
                                        color = TextLight,
                                        lineHeight = 13.sp
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "SENDER TELEBIRR PHONE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = {
                                        phoneNumber = it
                                        phoneError = null
                                    },
                                    placeholder = { Text("e.g. 0912345678", color = TextGrey, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🇪🇹 +251",
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Divider(
                                                color = BorderColor,
                                                modifier = Modifier
                                                    .height(16.dp)
                                                    .width(1.dp)
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("telebirr_phone_input"),
                                    isError = phoneError != null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedContainerColor = SlateSurfaceL2,
                                        unfocusedContainerColor = SlateSurfaceL2,
                                        focusedBorderColor = Color(0xFF00C2FF),
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                )
                                if (phoneError != null) {
                                    Text(
                                        text = phoneError!!,
                                        color = LightRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "DEPOSIT AMOUNT (ETB)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = {
                                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                            amountText = it
                                            amountError = null
                                        }
                                    },
                                    placeholder = { Text("0.00 ETB", color = TextGrey, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Paid,
                                            contentDescription = null,
                                            tint = NeonGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("telebirr_amount_input"),
                                    isError = amountError != null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedContainerColor = SlateSurfaceL2,
                                        unfocusedContainerColor = SlateSurfaceL2,
                                        focusedBorderColor = NeonGreen,
                                        unfocusedBorderColor = BorderColor
                                    ),
                                    textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                )
                                if (amountError != null) {
                                    Text(
                                        text = amountError!!,
                                        color = LightRed,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    availablePresets.forEach { preset ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    amountText = preset.toInt().toString()
                                                    amountError = null
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+${preset.toInt()} ETB",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00C2FF)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismissRequest,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "CANCEL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        val cleanPhone = phoneNumber.trim()
                                        if (cleanPhone.length < 9) {
                                            phoneError = "Please enter a valid phone number (9+ digits)"
                                            return@Button
                                        }

                                        val amount = amountText.toDoubleOrNull() ?: 0.0
                                        if (amount < 1.0) {
                                            amountError = "Minimum deposit amount is 1.00 ETB"
                                            return@Button
                                        }

                                        val depositLimitError = viewModel.checkDepositLimits(amount)
                                        if (depositLimitError != null) {
                                            amountError = depositLimitError
                                            return@Button
                                        }

                                        scope.launch {
                                            cancelOptionEnabled = false
                                            stepState = "PROCESSING_RSA"
                                            progressMessage = "Synthesizing dynamic check-out payload vectors..."
                                            delay(1000)

                                            outTradeNo = "TX_" + (100000..999999).random().toString()
                                            progressMessage = "Acquiring secure SHA256withRSA cryptosignature..."
                                            
                                            val fields = mapOf(
                                                "appId" to com.example.BuildConfig.TELEBIRR_APP_ID,
                                                "merchCode" to com.example.BuildConfig.TELEBIRR_MCH_SHORT_CODE,
                                                "outTradeNo" to outTradeNo,
                                                "totalAmount" to amount.toString(),
                                                "timestamp" to System.currentTimeMillis().toString()
                                            )
                                            val signStr = com.example.util.TelebirrUtil.createSignString(fields)
                                            signaturePayload = com.example.util.TelebirrUtil.signPayload(
                                                signStr,
                                                com.example.BuildConfig.MERCHANT_PRIVATE_KEY
                                            )
                                            
                                            viewModel.logConsole("🔑 [MOBILE SDK] Initializing checkout merchant outTradeNo: $outTradeNo")
                                            viewModel.logConsole("📦 RSA-signed Payload checksum payload successfully generated.")
                                            
                                            delay(1200)

                                            stepState = "USSD_PUSH"
                                            progressMessage = "Pushing carrier transport signal to +251 $cleanPhone..."
                                            delay(1500)

                                            stepState = "PIN_INPUT"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C2FF), contentColor = SlateDarkBG),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .testTag("deposit_proceed_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = SlateDarkBG,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(text = "PROCEED SECURELY", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    "PROCESSING_RSA" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00C2FF),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "CRYPTOGRAPHIC HANDSHAKE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressMessage,
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    "USSD_PUSH" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "MOBILE TRANSPORTER BROADCASTING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressMessage,
                                fontSize = 10.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    "PIN_INPUT" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dialpad,
                                contentDescription = null,
                                tint = Color(0xFF00C2FF),
                                modifier = Modifier.size(36.dp)
                            )
                            
                            Text(
                                text = "CARRIER SECURITY SHEET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "Telebirr Carrier USSD Push: Please confirm the deposit billing of ${amountText} ETB by typing your secret 5-digit account PIN:",
                                fontSize = 9.5.sp,
                                color = TextLight,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )

                            OutlinedTextField(
                                value = simPinCode,
                                onValueChange = {
                                    if (it.length <= 5 && (it.isEmpty() || it.all { char -> char.isDigit() })) {
                                        simPinCode = it
                                    }
                                },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .width(140.dp)
                                    .testTag("telebirr_sim_pin_input"),
                                singleLine = true,
                                placeholder = { Text("•••••", color = TextGrey, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedContainerColor = SlateSurfaceL2,
                                    unfocusedContainerColor = SlateSurfaceL2,
                                    focusedBorderColor = Color(0xFF00C2FF),
                                    unfocusedBorderColor = BorderColor
                                ),
                                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp, textAlign = TextAlign.Center)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (simPinCode.length < 5) {
                                        return@Button
                                    }
                                    
                                    scope.launch {
                                        stepState = "VERIFYING_CALLBACK"
                                        progressMessage = "Callback webhook dispatch sent to Node API..."
                                        
                                        val amt = amountText.toDoubleOrNull() ?: 100.0
                                        viewModel.depositFundsPending(amt) { customTx ->
                                            scope.launch {
                                                delay(1200)
                                                progressMessage = "Synchronizing SQLite ledger state... cryptographically signing with RSA..."
                                                
                                                viewModel.executeTelebirrCallback(
                                                    outTradeNo = customTx,
                                                    baseAmount = amt,
                                                    simulateTamper = false
                                                )
                                                
                                                delay(1500)
                                                stepState = "SUCCESS"
                                                progressMessage = "Deposit transaction of ${String.format("%,.2f", amt)} ETB successfully verified and settled. Merchant outTradeNo: $customTx."
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = SlateDarkBG),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("telebirr_pin_submit_btn"),
                                enabled = simPinCode.length == 5
                            ) {
                                Text(text = "AUTHORIZE PAYMENT", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }

                            TextButton(
                                onClick = {
                                    stepState = "FAILED"
                                    progressMessage = "Transaction aborted by user pin bypass."
                                }
                            ) {
                                Text(text = "DECLINE / CANCEL", color = LightRed, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    "VERIFYING_CALLBACK" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = NeonGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "RESOLVING CRYPTO WEBHOOK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressMessage,
                                fontSize = 9.5.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    "SUCCESS" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = NeonGreen,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "PAYMENT APPROVED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressMessage,
                                fontSize = 9.5.sp,
                                color = TextLight,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    cancelOptionEnabled = true
                                    onDismissRequest()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire, contentColor = TextWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "COMPLETED", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    "FAILED" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Failed",
                                tint = LightRed,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "TRANSACTION TERMINATED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressMessage,
                                fontSize = 9.5.sp,
                                color = TextLight,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    cancelOptionEnabled = true
                                    onDismissRequest()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2, contentColor = TextMuted),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "DISMISS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// REAL-TIME SCROLLING TICKER COMPONENT
// ==========================================
@Composable
fun LiveScrollingTicker(viewModel: BetViewModel) {
    val matches by viewModel.allMatches.collectAsState()
    val liveMatches = remember(matches) { matches.filter { it.isLive } }
    
    if (liveMatches.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "ticker_anim")
    val tickerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(0.dp))
            .padding(vertical = 6.dp)
            .testTag("live_scrolling_ticker_container")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(LightRed)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .zIndex(2f)
            ) {
                Text(
                    text = "LIVE TICKER",
                    color = TextWhite,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            
            // Scrolling marquee content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(0.dp))
                    .graphicsLayer { clip = true }
            ) {
                Row(
                    modifier = Modifier.graphicsLayer { translationX = tickerOffset },
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat items to keep ticker full
                    repeat(3) {
                        liveMatches.forEach { match ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚽ ${match.teamA} ",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "${match.scoreA} - ${match.scoreB}",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " ${match.teamB} ",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(AmberAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "1X2: @${String.format("%.2f", match.odds1)} / @${String.format("%.2f", match.oddsX)} / @${String.format("%.2f", match.odds2)}",
                                        color = AmberAccent,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
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

// ==========================================
// RECHARTS-PARITY INTERACTIVE BET PERFORMANCE CHART
// ==========================================
@Composable
fun BetPerformanceSummaryCard(modifier: Modifier = Modifier) {
    val localBorderColor = BorderColor
    val localNeonGreen = NeonGreen
    val localSlateDarkBG = SlateDarkBG
    val localTextWhite = TextWhite

    // Generate 30 days of data tracking net profit/loss
    val dataPoints = remember {
        listOf(
            0.0, 150.0, -100.0, 200.0, 450.0, 300.0, 120.0, 500.0, 850.0, 720.0,
            600.0, 950.0, 1400.0, 1100.0, 980.0, 1500.0, 1850.0, 1600.0, 2100.0, 2450.0,
            2300.0, 2200.0, 2800.0, 3400.0, 3200.0, 3900.0, 4500.0, 4200.0, 4800.0, 5250.0
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf(-1f) }
    var touchY by remember { mutableStateOf(-1f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bet_performance_summary_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "30-DAY PERFORMANCE INDEX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Realtime aggregate profit/loss analysis based on settled slips",
                        fontSize = 9.5.sp,
                        color = TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonGreen.copy(0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+5250.00 ETB",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart with touch interaction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val canvasWidth = size.width
                                    val segmentWidth = canvasWidth / (dataPoints.size - 1)
                                    val index = (offset.x / segmentWidth).roundToInt().coerceIn(0, dataPoints.size - 1)
                                    selectedIndex = index
                                    touchX = offset.x
                                    touchY = offset.y
                                    tryAwaitRelease()
                                    selectedIndex = null
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val maxVal = dataPoints.maxOrNull() ?: 1.0
                    val minVal = dataPoints.minOrNull() ?: 0.0
                    val range = (maxVal - minVal).coerceAtLeast(1.0)
                    
                    val segmentWidth = width / (dataPoints.size - 1)

                    // Helper lambda to calculate y coordinates
                    val getY: (Double) -> Float = { value ->
                        (height - ((value - minVal) / range * height * 0.8f) - (height * 0.1f)).toFloat()
                    }

                    // Draw Horizontal Gridlines
                    val gridLinesCount = 4
                    for (i in 0..gridLinesCount) {
                        val gridY = height * 0.1f + (height * 0.8f / gridLinesCount) * i
                        drawLine(
                            color = localBorderColor.copy(alpha = 0.25f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Build path for curve & gradient fill
                    val curvePath = Path()
                    val fillPath = Path()

                    curvePath.moveTo(0f, getY(dataPoints[0]))
                    fillPath.moveTo(0f, height)
                    fillPath.lineTo(0f, getY(dataPoints[0]))

                    for (i in 1 until dataPoints.size) {
                        val prevX = (i - 1) * segmentWidth
                        val prevY = getY(dataPoints[i - 1])
                        val currX = i * segmentWidth
                        val currY = getY(dataPoints[i])

                        // Smooth Bezier Curve points
                        val controlX1 = prevX + (currX - prevX) / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + (currX - prevX) / 2f
                        val controlY2 = currY

                        curvePath.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, currX, currY)
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // 1. Draw area gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                localNeonGreen.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 2. Draw curve line
                    drawPath(
                        path = curvePath,
                        color = localNeonGreen,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 3. Draw crosshair & details if touched
                    val activeIndex = selectedIndex
                    if (activeIndex != null && activeIndex in dataPoints.indices) {
                        val activeX = activeIndex * segmentWidth
                        val activeY = getY(dataPoints[activeIndex])

                        // Vertical guide line
                        drawLine(
                            color = localTextWhite.copy(alpha = 0.4f),
                            start = Offset(activeX, 0f),
                            end = Offset(activeX, height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Glowing point
                        drawCircle(
                            color = localSlateDarkBG,
                            radius = 6.dp.toPx(),
                            center = Offset(activeX, activeY)
                        )
                        drawCircle(
                            color = localNeonGreen,
                            radius = 4.dp.toPx(),
                            center = Offset(activeX, activeY)
                        )
                    }
                }

                // Interactive Tooltip Overlay HTML-Parity
                val activeIndex = selectedIndex
                if (activeIndex != null && activeIndex in dataPoints.indices) {
                    val valAtIdx = dataPoints[activeIndex]
                    val isProfit = valAtIdx >= 0.0
                    val cardXOffset = if (touchX > 180f) (touchX / 3f) else touchX / 1.5f

                    Box(
                        modifier = Modifier
                            .offset(x = cardXOffset.dp, y = 10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF020617).copy(alpha = 0.92f))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "Day ${activeIndex + 1}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Text(
                                text = if (isProfit) "Net Profit: +${String.format("%.2f", valAtIdx)} ETB" else "Net Loss: ${String.format("%.2f", valAtIdx)} ETB",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isProfit) NeonGreen else LightRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            // X-Axis Scale labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day 1 (Start)", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(text = "Day 15", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(text = "Day 30 (Today)", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// COMPACT QUICK DEPOSIT SHORTCUT CARD
// ==========================================
@Composable
fun QuickDepositShortcutCard(
    onTriggerDeposit: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(100.0, 250.0, 500.0, 1000.0, 2500.0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_deposit_shortcut_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TELEBIRR QUICK TOP UP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tap any preset below to trigger secure automated Telebirr checkout instantly.",
                fontSize = 9.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { amt ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateSurfaceL2)
                            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            .clickable { onTriggerDeposit(amt) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "+${amt.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                            Text(
                                text = "ETB",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CUSTOM PRICE ALERTS CONFIGURATION DIALOG
// ==========================================
@Composable
fun PriceAlertSettingDialog(
    match: SportMatch,
    onDismiss: () -> Unit,
    onSetAlert: (market: String, targetValue: Double, condition: String) -> Unit
) {
    var selectedMarket by remember { mutableStateOf("1") } // "1", "X", "2"
    var thresholdValue by remember { mutableStateOf("") }
    var alertCondition by remember { mutableStateOf("ABOVE") } // "ABOVE", "BELOW"

    LaunchedEffect(selectedMarket) {
        val currentOdds = when (selectedMarket) {
            "1" -> match.odds1
            "X" -> match.oddsX
            "2" -> match.odds2
            else -> 1.0
        }
        thresholdValue = String.format(java.util.Locale.US, "%.2f", currentOdds)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("price_alert_setting_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SET ODDS PRICE ALERT",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${match.teamA} vs ${match.teamB}",
                    fontSize = 11.sp,
                    color = TextLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Select Option segment
                Text(
                    text = "SELECT MARKET SELECTION",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1", "X", "2").forEach { mkt ->
                        val label = when (mkt) {
                            "1" -> "Home Win"
                            "X" -> "Draw"
                            else -> "Away Win"
                        }
                        val isSelected = selectedMarket == mkt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) PrimarySapphire.copy(0.15f) else SlateSurfaceL2)
                                .border(1.dp, if (isSelected) PrimarySapphire else BorderColor, RoundedCornerShape(6.dp))
                                .clickable { selectedMarket = mkt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextWhite else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Alert condition
                Text(
                    text = "TRIGGER CONDITION",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ABOVE" to "Odds Rise Above (>=)", "BELOW" to "Odds Drop Below (<=)").forEach { (condKey, condLabel) ->
                        val isSelected = alertCondition == condKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) PrimarySapphire.copy(0.15f) else SlateSurfaceL2)
                                .border(1.dp, if (isSelected) PrimarySapphire else BorderColor, RoundedCornerShape(6.dp))
                                .clickable { alertCondition = condKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = condLabel,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextWhite else TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Odds Textfield
                OutlinedTextField(
                    value = thresholdValue,
                    onValueChange = { thresholdValue = it },
                    label = { Text("TARGET ODDS THRESHOLD", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("alert_target_odds_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = PrimarySapphire,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = SlateSurfaceL2,
                        unfocusedContainerColor = SlateSurfaceL2
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "CANCEL", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val targetDbl = thresholdValue.toDoubleOrNull() ?: 1.0
                            onSetAlert(selectedMarket, targetDbl, alertCondition)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).testTag("set_alert_confirm_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "SET SHIELD ALERT", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// RESPONSIBLE GAMBLING SAFETY ADVICE MODAL
// ==========================================
@Composable
fun ResponsibleGamblingModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var limitInput by remember { mutableStateOf("1500") }
    var limitUpdateMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(20.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("responsible_gambling_modal_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header icon & text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEAB308).copy(alpha = 0.12f))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "RESPONSIBLE PLAY LAYER",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Automated proactive frequency monitoring",
                            fontSize = 9.5.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Frequency Warning Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEAB308).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFEAB308).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "⚠️ HIGH-FREQUENCY BETTING DETECTED",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEAB308)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "You have placed a rapid sequence of tickets in a short time. We highly encourage utilizing pacing settings to secure your balance.",
                            fontSize = 9.sp,
                            color = TextLight,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Limit setting
                Text(
                    text = "SET DAILY SPENDING LIMIT (ETB)",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { limitInput = it },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("spending_limit_input_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PrimarySapphire,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SlateSurfaceL2,
                            unfocusedContainerColor = SlateSurfaceL2
                        )
                    )
                    Button(
                        onClick = {
                            limitUpdateMessage = "Daily limit configured to ${limitInput} ETB!"
                        },
                        colors = ButtonColors(containerColor = PrimarySapphire, contentColor = TextWhite, disabledContainerColor = Color.Gray, disabledContentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("apply_spending_limit_btn")
                    ) {
                        Text(text = "APPLY PACE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (limitUpdateMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = limitUpdateMessage!!,
                        color = NeonGreen,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Resources links list
                Text(
                    text = "SUPPORT RESOURCES & ADVICE",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val helpHelplines = listOf(
                        "📞 ShebaOdds Responsible Support Hotline: 8092 (Toll Free)",
                        "🤝 Local Pacing and Balance Setting Hub",
                        "🛡️ Self-Exclusion Safety Gate"
                    )
                    helpHelplines.forEach { line ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(SlateSurfaceL2)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = line,
                                fontSize = 9.sp,
                                color = TextLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Exit
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("dismiss_responsible_gambling_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "I AGREE & UNDERSTAND", color = TextWhite, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SportsCategoryTabs(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSport by viewModel.selectedSport.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()

    val sports = listOf("All", "Soccer", "Basketball", "Tennis", "Esports")
    val selectedIndex = when (selectedSport) {
        "All" -> 0
        "Football" -> 1
        "Soccer" -> 1
        "Basketball" -> 2
        "Tennis" -> 3
        "Esports" -> 4
        else -> 0
    }

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = SlateCardBG,
        contentColor = TextWhite,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = AmberAccent,
                    height = 3.dp
                )
            }
        },
        divider = { Divider(color = BorderColor.copy(alpha = 0.5f)) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("sports_category_tab_row")
    ) {
        sports.forEachIndexed { index, name ->
            val isSelected = selectedIndex == index
            val liveCount = if (name == "All") {
                allMatches.filter { it.isLive }.size
            } else if (name == "Soccer") {
                allMatches.filter { (it.sport.equals("Football", ignoreCase = true) || it.sport.equals("Soccer", ignoreCase = true)) && it.isLive }.size
            } else {
                allMatches.filter { it.sport.equals(name, ignoreCase = true) && it.isLive }.size
            }

            val icon = when (name) {
                "Soccer" -> Icons.Default.SportsSoccer
                "Basketball" -> Icons.Default.SportsBasketball
                "Tennis" -> Icons.Default.SportsBaseball
                "Esports" -> Icons.Default.SportsEsports
                else -> Icons.Default.Sports
            }

            val originalName = if (name == "Soccer") "Football" else name

            Tab(
                selected = isSelected,
                onClick = {
                    val targetSport = if (name == "Soccer") "Football" else name
                    viewModel.selectSport(targetSport)
                },
                modifier = Modifier.testTag("sport_filter_chip_$originalName")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = if (isSelected) AmberAccent else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = name.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isSelected) TextWhite else TextMuted
                    )
                    if (liveCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(LightRed, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = TextWhite,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: BetViewModel,
    onOddsSelected: (SportMatch, String, Double) -> Unit,
    onAnalyzeSelected: (SportMatch) -> Unit,
    onViewMultiBetSlipSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.wallet.collectAsState()
    val matches by viewModel.filteredMatches.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val trendingMatches by viewModel.trendingMatches.collectAsState()
    val selectedSport by viewModel.selectedSport.collectAsState()
    val isApiFetching by viewModel.isApiFetching.collectAsState()
    val activeSlipItems by viewModel.activeSlipSelectedItems.collectAsState()
    val trackedMatchIds by viewModel.trackedMatchIds.collectAsState()

    var showQuickDepositDialog by remember { mutableStateOf(false) }
    var quickDepositInitialAmount by remember { mutableStateOf<Double?>(null) }
    var selectedAlertMatch by remember { mutableStateOf<SportMatch?>(null) }
    val showRespWarning by viewModel.showResponsiblePlayWarning.collectAsState()
    
    val sports = listOf("All", "Football", "Basketball", "Tennis", "Esports")

    // Animated simulated API overlay skeleton loader
    Crossfade(
        targetState = isApiFetching,
        animationSpec = tween(durationMillis = 350),
        label = "api_shimmer_cross"
    ) { fetching ->
        if (fetching) {
            DashboardSkeletonView()
        } else {
            BoxWithConstraints(modifier = modifier.fillMaxSize().background(SlateDarkBG)) {
                val isPortrait = maxHeight > maxWidth
                val isTablet = !isPortrait && maxWidth >= 600.dp && maxHeight >= 480.dp
                val searchQuery by viewModel.searchQuery.collectAsState()
                
                Column(modifier = Modifier.fillMaxSize()) {
                    DashboardSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) }
                    )
                    
                    wallet?.let {
                        UserBalanceWidget(
                            wallet = it,
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    SportsCategoryTabs(viewModel = viewModel, modifier = Modifier.padding(bottom = 8.dp))

                    // LIVE REAL-TIME TICKER
                    LiveScrollingTicker(viewModel = viewModel)
                    
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (isTablet) {
                    // ==========================================
                    // RESPONSIVE CANONICAL TAB/SPLIT LAYOUT (Tablet / Foldables)
                    // ==========================================
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Pane: Core Betting Lines & Markets (Weight 1.25)
                        LazyColumn(
                            modifier = Modifier
                                .weight(1.25f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                        ) {
                            if (wallet?.role == "super_admin") {
                                item {
                                    AdminOddsAndLocksController(viewModel = viewModel)
                                }
                            }

                            // FEATURED SHOWDOWNS CAROUSEL AT TOP OF LEFT PANEL
                            if (trendingMatches.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = AmberAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "FEATURED SHOWDOWNS",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = TextWhite,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(trendingMatches, key = { it.id }) { match ->
                                            FeaturedMatchCard(
                                                match = match,
                                                selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                                onOddsClick = { selection, odds -> 
                                                    viewModel.toggleSlipSelection(match, selection, odds)
                                                },
                                                onAnalyzeClick = { onAnalyzeSelected(match) },
                                                isTracked = trackedMatchIds.contains(match.id),
                                                onTrackClick = { viewModel.toggleTrackedMatch(match.id) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Active Live Event Categories Section
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "LIVE EVENTS HUB",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite,
                                        letterSpacing = 0.5.sp
                                    )
                                    
                                    // Manual API Synchronization Trigger
                                    IconButton(
                                        onClick = { viewModel.refreshDashboardData() },
                                        modifier = Modifier.testTag("api_sync_tablet_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Simulated API Sync",
                                            tint = PrimarySapphire,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // LIVE IN-PLAY MATCHES
                            val liveMatches = matches.filter { it.isLive }
                            if (liveMatches.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Live Matches", isLive = true)
                                }
                                 items(liveMatches, key = { it.id }) { match ->
                                    MatchBettingCard(
                                        match = match,
                                        selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                        onOddsClick = { selection, odds -> 
                                            viewModel.toggleSlipSelection(match, selection, odds)
                                        },
                                        onAnalyzeClick = { onAnalyzeSelected(match) },
                                        onSetAlertClick = { selectedAlertMatch = match }
                                    )
                                }
                            }

                            // UPCOMING FIXTURES
                            val upcomingMatches = matches.filter { !it.isLive }
                            if (upcomingMatches.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Upcoming Matches", isLive = false)
                                }
                                items(upcomingMatches, key = { it.id }) { match ->
                                    MatchBettingCard(
                                        match = match,
                                        selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                        onOddsClick = { selection, odds -> 
                                            viewModel.toggleSlipSelection(match, selection, odds)
                                        },
                                        onAnalyzeClick = { onAnalyzeSelected(match) },
                                        onSetAlertClick = { selectedAlertMatch = match }
                                    )
                                }
                            } else if (liveMatches.isEmpty()) {
                                item {
                                    EmptyStateCard(message = "No matching athletic lines found in database.")
                                }
                            }
                        }

                        // Right Pane: Supporting Balance, Trending Widgets & Spotlight (Weight 0.75)
                        Column(
                            modifier = Modifier
                                .weight(0.75f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Dynamic Interactive Live Bet Slip (Mockup parity!)
                            SidebarLiveBetSlip(viewModel = viewModel)

                            // Wallet Cards
                            wallet?.let {
                                WalletSummaryCard(wallet = it)
                            }

                            // Quick deposit presets trigger
                            QuickDepositShortcutCard(
                                onTriggerDeposit = { amt ->
                                    quickDepositInitialAmount = amt
                                    showQuickDepositDialog = true
                                }
                            )

                            // Bet performance analytics
                            BetPerformanceSummaryCard()

                            // Promo Banner
                            PromoHeroBanner()

                            // Active Global Partners / sponsors tier List
                            SponsorHubSection()

                            // TRENDING TICKER SPOTLIGHT
                            Text(
                                text = "TRENDING ODDS SPOTLIGHT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(trendingMatches, key = { it.id }) { match ->
                                    TrendingMatchCard(
                                        match = match,
                                        selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                        onOddsClick = { selection, odds -> 
                                            viewModel.toggleSlipSelection(match, selection, odds)
                                        },
                                        onAnalyzeClick = { onAnalyzeSelected(match) },
                                        isTracked = trackedMatchIds.contains(match.id),
                                        onTrackClick = { viewModel.toggleTrackedMatch(match.id) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // COMPACT PORTRAIT LAYOUT (Mobile Handset)
                    // ==========================================
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
                        ) {
                            if (wallet?.role == "super_admin") {
                                item {
                                    AdminOddsAndLocksController(viewModel = viewModel)
                                }
                            }

                            // 1. FEATURED SHOWDOWNS CAROUSEL FOR MOBILE VIEWPORT (Immediate visual attention)
                            if (trendingMatches.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = null,
                                                tint = AmberAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Featured Showdowns",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Black,
                                                color = TextWhite
                                            )
                                        }
                                        
                                        // Simulated Sync button
                                        IconButton(
                                            onClick = { viewModel.refreshDashboardData() },
                                            modifier = Modifier.size(24.dp).testTag("api_sync_mobile_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Simulated API Sync",
                                                tint = PrimarySapphire,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(trendingMatches, key = { it.id }) { match ->
                                            FeaturedMatchCard(
                                                match = match,
                                                selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                                onOddsClick = { selection, odds -> 
                                                    viewModel.toggleSlipSelection(match, selection, odds)
                                                },
                                                onAnalyzeClick = { onAnalyzeSelected(match) },
                                                isTracked = trackedMatchIds.contains(match.id),
                                                onTrackClick = { viewModel.toggleTrackedMatch(match.id) }
                                            )
                                        }
                                    }
                                }
                            }

                            // QUICK DEPOSIT & PERFORMANCE CARDS IN MOBILE FEED
                            item {
                                QuickDepositShortcutCard(
                                    onTriggerDeposit = { amt ->
                                        quickDepositInitialAmount = amt
                                        showQuickDepositDialog = true
                                    }
                                )
                            }

                            item {
                                BetPerformanceSummaryCard()
                            }

                            // 3. ACTIVE LIVE MATCHES SECTION (Highest priority in-play)
                            val liveMatches = matches.filter { it.isLive }
                            if (liveMatches.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Live Matches", isLive = true)
                                }
                                items(liveMatches, key = { it.id }) { match ->
                                    MatchBettingCard(
                                        match = match,
                                        selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                        onOddsClick = { selection, odds -> 
                                            viewModel.toggleSlipSelection(match, selection, odds)
                                        },
                                        onAnalyzeClick = { onAnalyzeSelected(match) },
                                        isTracked = trackedMatchIds.contains(match.id),
                                        onTrackClick = { viewModel.toggleTrackedMatch(match.id) },
                                        onSetAlertClick = { selectedAlertMatch = match }
                                    )
                                }
                            }

                            // 4. PREMIUM PROMO HERO BANNER (Inline ad placement to break content beautifully)
                            item {
                                PromoHeroBanner()
                            }

                            // 5. UPCOMING FIXTURES
                            val upcomingMatches = matches.filter { !it.isLive }
                            if (upcomingMatches.isNotEmpty()) {
                                item {
                                    SectionHeader(title = "Upcoming Fixtures", isLive = false)
                                }
                                items(upcomingMatches, key = { it.id }) { match ->
                                    MatchBettingCard(
                                        match = match,
                                        selectedSelection = activeSlipItems.find { it.matchId == match.id }?.selection,
                                        onOddsClick = { selection, odds -> 
                                            viewModel.toggleSlipSelection(match, selection, odds)
                                        },
                                        onAnalyzeClick = { onAnalyzeSelected(match) },
                                        isTracked = trackedMatchIds.contains(match.id),
                                        onTrackClick = { viewModel.toggleTrackedMatch(match.id) },
                                        onSetAlertClick = { selectedAlertMatch = match }
                                    )
                                }
                            } else if (liveMatches.isEmpty()) {
                                item {
                                    EmptyStateCard(message = "No sports matching your category selections found.")
                                }
                            }

                            // 6. DETAILS WALLET AND SECURITY BRIEF (Lower priority description details)
                            item {
                                wallet?.let {
                                    WalletSummaryCard(wallet = it)
                                }
                            }

                            // 7. INSTITUTIONAL SPONSOHIP GATEWAYS (Bottom page informational footer)
                            item {
                                SponsorHubSection()
                            }
                        }
                    }
                        }
                    }
                }

                if (activeSlipItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 16.dp)
                            .testTag("floating_accumulator_bar")
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable { onViewMultiBetSlipSelected() },
                            shape = RoundedCornerShape(26.dp),
                            color = PrimarySapphire,
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, AmberAccent)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = "Accumulator",
                                        tint = AmberAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${activeSlipItems.size} ${if (activeSlipItems.size == 1) "LEG" else "LEGS"} SELECTED",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                val totalOddsStr = String.format("%.2f", activeSlipItems.fold(1.0) { acc, item -> acc * item.odds })
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "@$totalOddsStr",
                                        color = NeonGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "VIEW TICKET",
                                        color = AmberAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AmberAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (showQuickDepositDialog) {
                    DepositFundsDialog(
                        viewModel = viewModel,
                        initialAmount = quickDepositInitialAmount,
                        onDismissRequest = { showQuickDepositDialog = false }
                    )
                }

                selectedAlertMatch?.let { match ->
                    PriceAlertSettingDialog(
                        match = match,
                        onDismiss = { selectedAlertMatch = null },
                        onSetAlert = { market, target, cond ->
                            viewModel.addCustomPriceAlert(
                                matchId = match.id,
                                teamA = match.teamA,
                                teamB = match.teamB,
                                marketName = market,
                                targetOdds = target,
                                condition = cond
                            )
                        }
                    )
                }

                if (showRespWarning) {
                    ResponsibleGamblingModal(
                        onDismiss = { viewModel.dismissResponsiblePlayWarning() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 1A. COGNATE DASHBOARD SUBVIEWS
// ==========================================

@Composable
fun WalletSummaryCard(wallet: UserWallet) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Balance",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOUR TOTAL BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
                
                // VIP Tier indicator
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(listOf(AmberAccent, Color(0xFFFFDB7D))),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "GOLD PREMIUM",
                        fontSize = 9.s_p,
                        fontWeight = FontWeight.Black,
                        color = SlateDarkBG
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "${String.format("%,.2f", wallet.balance)} ${wallet.currency}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                modifier = Modifier.testTag("wallet_balance_label")
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "AVAILABLE BONUS", fontSize = 11.s_p, color = TextMuted)
                    Text(
                        text = "${String.format("%,.2f", wallet.bonusBalance)} ${wallet.currency}",
                        fontSize = 14.s_p,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "SECURITY STATUS", fontSize = 11.s_p, color = TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Verified Status",
                            tint = NeonGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% SECURE",
                            fontSize = 12.s_p,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }
        }
    }
}

private val Int.s_p: androidx.compose.ui.unit.TextUnit get() = this.sp

@Composable
fun PromoHeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PrimarySapphire.copy(alpha = 0.85f), SlateCardBG),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .fillMaxWidth(0.65f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(AmberAccent, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HOT BOOST",
                    fontSize = 9.s_p,
                    fontWeight = FontWeight.Bold,
                    color = SlateDarkBG
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "UEFA Finals Match Boost",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Get +15% enhanced returns on Football match slips today!",
                fontSize = 11.s_p,
                color = TextLight.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Beautiful abstract design vector drawn dynamically
        val localTextWhite = TextWhite
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(110.dp)
                .drawBehind {
                    val pathWidth = size.width
                    val pathHeight = size.height
                    drawCircle(
                        color = NeonGreen.copy(alpha = 0.15f),
                        radius = pathHeight * 0.7f,
                        center = Offset(pathWidth * 0.8f, pathHeight * 0.5f)
                    )
                    drawCircle(
                        color = PrimarySapphire.copy(alpha = 0.3f),
                        radius = pathHeight * 0.4f,
                        center = Offset(pathWidth * 0.8f, pathHeight * 0.5f)
                    )
                    // Draw mini soccer ball outline
                    drawCircle(
                        color = localTextWhite.copy(alpha = 0.08f),
                        radius = pathHeight * 0.2f,
                        center = Offset(pathWidth * 0.8f, pathHeight * 0.5f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )
    }
}

data class AppSponsor(
    val name: String,
    val role: String,
    val tier: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val activePerk: String
)

@Composable
fun SponsorHubSection() {
    var selectedSponsorIndex by remember { mutableStateOf<Int?>(null) }
    
    val sponsors = listOf(
        AppSponsor(
            name = "Telebirr Pay",
            role = "Lead Financial Gateway",
            tier = "TITANIUM PARTNER",
            description = "National unified mobile money ecosystem, providing safe instant funds routing.",
            icon = Icons.Default.Payments,
            color = Color(0xFF00C2FF),
            activePerk = "🚀 5% Deposit Bonus applied instantly on transaction confirmation."
        ),
        AppSponsor(
            name = "Commercial Bank of Ethiopia (CBE)",
            role = "Primary Settlement Vault",
            tier = "GOLD SPONSOR",
            description = "The premier state-governed financial house powering secure withdrawals.",
            icon = Icons.Default.AccountBalance,
            color = Color(0xFFBD00FF),
            activePerk = "⚡ Real-time ledger direct settlement with 0% extra gateway fees."
        ),
        AppSponsor(
            name = "Ethiopian Premier League",
            role = "Lead Athletic Partner",
            tier = "PLATINUM EVENT CO-HOST",
            description = "Official sporting federation of premier professional football competition.",
            icon = Icons.Default.SportsSoccer,
            color = Color(0xFFFFB000),
            activePerk = "🏆 Double Odds multiplier options on all live Ethiopian Derby selections."
        ),
        AppSponsor(
            name = "Sheba Telecom Limited",
            role = "Official Broadcast Net",
            tier = "GOLD PARTNER",
            description = "Elite digital telecommunications and high-speed sports broadcast server.",
            icon = Icons.Default.Tv,
            color = Color(0xFF00E676),
            activePerk = "📡 Free HD Live-Stream access directly in Admin & Telebirr Sync panels."
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sponsors_hub_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHEBAODDS GLOBAL SPONSORS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(AmberAccent.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PARTNERS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tap a premium sponsor card below to view custom active contract benefits and bonus rewards!",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Carousel list / Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(sponsors) { index, sponsor ->
                    val isSelected = selectedSponsorIndex == index
                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable {
                                selectedSponsorIndex = if (isSelected) null else index
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) sponsor.color.copy(alpha = 0.12f) else SlateSurfaceL2
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) sponsor.color else BorderColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(sponsor.color.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = sponsor.icon,
                                        contentDescription = sponsor.name,
                                        tint = sponsor.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(sponsor.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = sponsor.tier,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = sponsor.color
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sponsor.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = sponsor.role,
                                fontSize = 9.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = sponsor.description,
                                fontSize = 8.sp,
                                color = TextLight,
                                lineHeight = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Expanding Drawer Section to show active Perk status in absolute fidelity
            selectedSponsorIndex?.let { index ->
                val sponsor = sponsors[index]
                Spacer(modifier = Modifier.height(12.dp))
                
                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F162A), RoundedCornerShape(8.dp))
                            .border(1.dp, sponsor.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.OfflineBolt,
                                    contentDescription = "Reward active",
                                    tint = sponsor.color,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${sponsor.name.uppercase()} ACTIVE CONTRACT BENEFIT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sponsor.activePerk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = sponsor.color,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SportFilterChip(
    name: String,
    isSelected: Boolean,
    liveCount: Int = 0,
    onClick: () -> Unit
) {
    val icon = when (name) {
        "Football" -> Icons.Default.SportsSoccer
        "Basketball" -> Icons.Default.SportsBasketball
        "Tennis" -> Icons.Default.SportsBaseball
        "Esports" -> Icons.Default.SportsEsports
        else -> Icons.Default.Sports
    }

    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) PrimarySapphire else SlateCardBG,
                shape = RoundedCornerShape(100.dp)
            )
            .border(
                1.dp,
                if (isSelected) PrimarySapphire else BorderColor,
                RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .testTag("sport_filter_chip_$name")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected) TextWhite else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = if (isSelected) TextWhite else TextLight,
                fontSize = 12.s_p,
                fontWeight = FontWeight.SemiBold
            )
            if (liveCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) TextWhite.copy(0.25f) else NeonGreen.copy(0.12f),
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$liveCount",
                        color = if (isSelected) TextWhite else NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, isLive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = TextWhite
            )
            if (isLive) {
                Spacer(modifier = Modifier.width(8.dp))
                
                // Blinking Pulse Live Indicator
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                
                Box(
                    modifier = Modifier
                        .background(
                            color = NeonGreen.copy(alpha = alpha),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 9.s_p,
                        fontWeight = FontWeight.Black,
                        color = SlateDarkBG
                    )
                }
            }
        }
    }
}

@Composable
fun MatchBettingCard(
    match: SportMatch,
    selectedSelection: String? = null,
    onOddsClick: (String, Double) -> Unit,
    onAnalyzeClick: () -> Unit,
    isTracked: Boolean = false,
    onTrackClick: () -> Unit = {},
    onSetAlertClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Sport Category and Time label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTrackClick,
                        modifier = Modifier.size(24.dp).testTag("track_match_betting_${match.id}")
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Track Match Odds",
                            tint = if (isTracked) AmberAccent else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (onSetAlertClick != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        IconButton(
                            onClick = onSetAlertClick,
                            modifier = Modifier.size(24.dp).testTag("set_alert_match_betting_${match.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAlert,
                                contentDescription = "Set Custom Odds Alert",
                                tint = AmberAccent,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    val sportIcon = when (match.sport) {
                        "Football" -> Icons.Default.SportsSoccer
                        "Basketball" -> Icons.Default.SportsBasketball
                        "Tennis" -> Icons.Default.SportsBaseball
                        "Esports" -> Icons.Default.SportsEsports
                        else -> Icons.Default.Sports
                    }
                    Icon(
                        imageVector = sportIcon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.sport.uppercase(),
                        fontSize = 10.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 10.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = match.leagueName,
                        fontSize = 10.s_p,
                        fontWeight = FontWeight.Medium,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (match.isLocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LightRed.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = LightRed,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "LOCKED",
                                    fontSize = 8.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = LightRed
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = if (match.isLive) match.timeString else match.dateTimeString,
                    fontSize = 11.s_p,
                    fontWeight = FontWeight.Bold,
                    color = if (match.isLive) NeonGreen else TextMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            // Team Names and Live Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = match.teamA,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = match.teamB,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (match.isLive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${match.scoreA} : ${match.scoreB}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live Odds 60m Trend",
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                }
                MatchOddsTrendSparkline(
                    matchId = match.id,
                    currentOdds = match.odds1
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            
            // Betting Markets Quick Buttons (1 X 2 odds)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home (1) button
                OddsButton(
                    label = "1",
                    oddsVal = match.odds1,
                    isSelected = selectedSelection == "1",
                    isLocked = match.isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onOddsClick("1", match.odds1) }
                )
                
                // Draw (X) button
                if (match.sport != "Tennis" && match.sport != "Esports") {
                    OddsButton(
                        label = "X",
                        oddsVal = match.oddsX,
                        isSelected = selectedSelection == "X",
                        isLocked = match.isLocked,
                        modifier = Modifier.weight(1f),
                        onClick = { onOddsClick("X", match.oddsX) }
                    )
                }

                // Away (2) button
                OddsButton(
                    label = "2",
                    oddsVal = match.odds2,
                    isSelected = selectedSelection == "2",
                    isLocked = match.isLocked,
                    modifier = Modifier.weight(1f),
                    onClick = { onOddsClick("2", match.odds2) }
                )

                // AI Analyst Button
                IconButton(
                    onClick = onAnalyzeClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(SlateSurfaceL2, CircleShape)
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI Prediction",
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OddsButton(
    label: String,
    oddsVal: Double,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = { if (!isLocked) onClick() },
        modifier = modifier
            .height(38.dp)
            .testTag("odds_btn_${label}_$oddsVal"),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isLocked) SlateDarkBG.copy(alpha = 0.5f) else if (isSelected) AmberAccent.copy(alpha = 0.15f) else SlateSurfaceL2,
            contentColor = if (isLocked) TextMuted else if (isSelected) AmberAccent else TextLight
        ),
        elevation = null,
        contentPadding = PaddingValues(0.dp),
        border = BorderStroke(1.dp, if (isLocked) SlateSurfaceL2 else if (isSelected) AmberAccent else BorderColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.s_p,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) TextMuted else if (isSelected) AmberAccent else TextMuted
            )
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = LightRed,
                    modifier = Modifier.size(11.dp)
                )
            } else {
                Text(
                    text = String.format("%.2f", oddsVal),
                    fontSize = 12.s_p,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) AmberAccent else TextWhite
                )
            }
        }
    }
}

// ==========================================
// 2. BET HISTORY & SLIPS SCREEN
// ==========================================

data class UnifiedTx(
    val id: String,
    val type: String,        // "DEPOSIT", "WITHDRAWAL", "WAGER", "PAYOUT"
    val amount: Double,
    val status: String,      // "APPROVED", "PENDING", "REJECTED", "WON", "LOST", "CASHOUT"
    val method: String,
    val timeLabel: String,
    val timestamp: Long,
    val description: String
)

@Composable
fun CustomFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (selected) PrimarySapphire else SlateCardBG, RoundedCornerShape(8.dp))
            .border(1.dp, if (selected) PrimarySapphire else BorderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) TextWhite else TextMuted
        )
    }
}

@Composable
fun BettingPerformanceDashboard(
    settledBets: List<Bet>,
    modifier: Modifier = Modifier
) {
    val sortedBets = remember(settledBets) {
        settledBets.sortedBy { it.timestamp }
    }

    if (sortedBets.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = TextGrey,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PERFORMANCE ANALYTICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No settled slips yet. Your performance charts and live Yield ROI analysis will automatically draw here.",
                    fontSize = 11.sp,
                    color = TextGrey,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Toggle: true -> Cumulative Growth Trend (Recharts AreaChart style), false -> Individual Tickets comparison (Recharts BarChart style)
    var isCumulativeView by remember { mutableStateOf(true) }

    // Structure for plotting
    val chartPoints = remember(sortedBets) {
        var runningStake = 0.0
        var runningWinnings = 0.0
        val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
        sortedBets.mapIndexed { index, bet ->
            val winAmt = when (bet.status) {
                "WON" -> bet.potentialReturn
                "CASHOUT" -> bet.potentialReturn
                else -> 0.0
            }
            runningStake += bet.stake
            runningWinnings += winAmt
            object {
                val index = index
                val dateLabel = sdf.format(java.util.Date(bet.timestamp))
                val ticketNumber = "#" + ((bet.id * 313 + 7041) % 10000)
                val selection = bet.selection
                val stake = bet.stake
                val winnings = winAmt
                val cumulativeStake = runningStake
                val cumulativeWinnings = runningWinnings
                val status = bet.status
            }
        }
    }

    val totalStakes = remember(chartPoints) { chartPoints.sumOf { it.stake } }
    val totalWinnings = remember(chartPoints) { chartPoints.sumOf { it.winnings } }
    val netProfit = totalWinnings - totalStakes
    val roiPercent = if (totalStakes > 0) (netProfit / totalStakes) * 100 else 0.0
    val winCount = sortedBets.count { it.status == "WON" || it.status == "CASHOUT" }
    val winRatePercent = if (sortedBets.isNotEmpty()) (winCount.toDouble() / sortedBets.size) * 100 else 0.0

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Auto-select the last point by default to show tooltip overview
    LaunchedEffect(chartPoints) {
        if (chartPoints.isNotEmpty() && selectedIndex == null) {
            selectedIndex = chartPoints.lastIndex
        }
    }

    val activeSelectedPoint = selectedIndex?.let { if (it in chartPoints.indices) chartPoints[it] else null }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("betting_performance_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERFORMANCE LEDGER GRAPH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-Time Winnings vs. Stakes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                // Interactive Chart Mode Toggle Switch Buttons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateSurfaceL2)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCumulativeView) PrimarySapphire else Color.Transparent)
                            .clickable { 
                                isCumulativeView = true 
                                if (chartPoints.isNotEmpty()) selectedIndex = chartPoints.lastIndex
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TREND",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCumulativeView) TextWhite else TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!isCumulativeView) PrimarySapphire else Color.Transparent)
                            .clickable { 
                                isCumulativeView = false 
                                if (chartPoints.isNotEmpty()) selectedIndex = chartPoints.lastIndex
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMPARE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (!isCumulativeView) TextWhite else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Analytical Key Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Return stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "NET YIELD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (netProfit >= 0) "+" else ""}${String.format("%,.1f", netProfit)} ETB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (netProfit >= 0) NeonGreen else LightRed
                    )
                }

                // ROI stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "TOTAL ROI", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (roiPercent >= 0) "+" else ""}${String.format("%.1f", roiPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (roiPercent >= 0) NeonGreen else LightRed
                    )
                }

                // Win rate stat
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(text = "WIN RATE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format("%.1f", winRatePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts-Style Canvas Plot Box
            val currentBorderColor = BorderColor
            val currentTextWhite = TextWhite
            val currentPrimarySapphire = PrimarySapphire
            val currentNeonGreen = NeonGreen
            val currentLightRed = LightRed
            val currentTextGrey = TextGrey

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                    .padding(top = 12.dp, bottom = 4.dp, start = 8.dp, end = 12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(chartPoints, isCumulativeView) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val leftPaddingPx = 36.dp.toPx()
                                    val rightPaddingPx = 8.dp.toPx()
                                    val usableWidth = size.width - leftPaddingPx - rightPaddingPx
                                    val numPoints = chartPoints.size
                                    
                                    if (numPoints > 1) {
                                        if (isCumulativeView) {
                                            val spacing = usableWidth / (numPoints - 1)
                                            var bestIndex = 0
                                            var bestDist = Float.MAX_VALUE
                                            for (i in 0 until numPoints) {
                                                val ptX = leftPaddingPx + i * spacing
                                                val dist = kotlin.math.abs(offset.x - ptX)
                                                if (dist < bestDist) {
                                                    bestDist = dist
                                                    bestIndex = i
                                                }
                                            }
                                            if (bestDist < spacing * 0.75f) {
                                                selectedIndex = bestIndex
                                            }
                                        } else {
                                            // Bars mode selection
                                            val spacing = usableWidth / numPoints
                                            val selectedCol = ((offset.x - leftPaddingPx) / spacing).toInt()
                                            if (selectedCol in 0 until numPoints) {
                                                selectedIndex = selectedCol
                                            }
                                        }
                                    } else if (numPoints == 1) {
                                        selectedIndex = 0
                                    }
                                }
                            )
                        }
                ) {
                    val leftPadding = 36.dp.toPx()
                    val rightPadding = 8.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 14.dp.toPx()
                    val usableHeight = size.height - topPadding - bottomPadding
                    val usableWidth = size.width - leftPadding - rightPadding

                    if (chartPoints.isEmpty()) return@Canvas

                    // Maximum value selection
                    val maxValue = if (isCumulativeView) {
                        maxOf(
                            chartPoints.maxOf { it.cumulativeStake },
                            chartPoints.maxOf { it.cumulativeWinnings },
                            200.0
                        )
                    } else {
                        maxOf(
                            chartPoints.maxOf { it.stake },
                            chartPoints.maxOf { it.winnings },
                            200.0
                        )
                    }
                    val scaleY = maxValue * 1.15 // 15% padding on top

                    // Draw GRIDLINES (Recharts aesthetics)
                    val gridLinesCount = 3
                    for (i in 0..gridLinesCount) {
                        val frac = i.toFloat() / gridLinesCount
                        val y = topPadding + usableHeight * (1.0f - frac)
                        drawLine(
                            color = currentBorderColor.copy(alpha = 0.35f),
                            start = Offset(leftPadding, y),
                            end = Offset(size.width - rightPadding, y),
                            strokeWidth = 0.5.dp.toPx()
                        )
                    }

                    if (isCumulativeView) {
                        // AREA & LINE TREND PLOT
                        val numPoints = chartPoints.size
                        if (numPoints > 1) {
                            val spacing = usableWidth / (numPoints - 1)

                            // Helper function to create Paths for Stakes and Winnings
                            fun makePaths(isWinnings: Boolean): Pair<Path, Path> {
                                val linePath = Path()
                                val areaPath = Path()
                                
                                val firstYVal = if (isWinnings) chartPoints[0].cumulativeWinnings else chartPoints[0].cumulativeStake
                                val firstY = (size.height - bottomPadding - (firstYVal / scaleY) * usableHeight).toFloat()
                                val firstX = leftPadding

                                linePath.moveTo(firstX, firstY)
                                areaPath.moveTo(firstX, (size.height - bottomPadding).toFloat())
                                areaPath.lineTo(firstX, firstY)

                                for (i in 1 until numPoints) {
                                    val valY = if (isWinnings) chartPoints[i].cumulativeWinnings else chartPoints[i].cumulativeStake
                                    val currentY = (size.height - bottomPadding - (valY / scaleY) * usableHeight).toFloat()
                                    val currentX = leftPadding + i * spacing
                                    linePath.lineTo(currentX, currentY)
                                    areaPath.lineTo(currentX, currentY)
                                }

                                val finalX = leftPadding + (numPoints - 1) * spacing
                                areaPath.lineTo(finalX, (size.height - bottomPadding).toFloat())
                                areaPath.close()

                                return Pair(linePath, areaPath)
                            }

                            // 1. Draw Stakes Area & Line (Sapphire color)
                            val (stakeLine, stakeArea) = makePaths(false)
                            drawPath(
                                path = stakeArea,
                                brush = Brush.verticalGradient(
                                    colors = listOf(currentPrimarySapphire.copy(alpha = 0.16f), Color.Transparent),
                                    startY = topPadding,
                                    endY = size.height - bottomPadding
                                )
                            )
                            drawPath(
                                path = stakeLine,
                                color = currentPrimarySapphire,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // 2. Draw Winnings Area & Line (NeonGreen color)
                            val (winLine, winArea) = makePaths(true)
                            drawPath(
                                path = winArea,
                                brush = Brush.verticalGradient(
                                    colors = listOf(currentNeonGreen.copy(alpha = 0.2f), Color.Transparent),
                                    startY = topPadding,
                                    endY = size.height - bottomPadding
                                )
                            )
                            drawPath(
                                path = winLine,
                                color = currentNeonGreen,
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Highlight the selected snap node dot
                            selectedIndex?.let { index ->
                                if (index in chartPoints.indices) {
                                    val ptX = leftPadding + index * spacing
                                    val ptStkY = (size.height - bottomPadding - (chartPoints[index].cumulativeStake / scaleY) * usableHeight).toFloat()
                                    val ptWinY = (size.height - bottomPadding - (chartPoints[index].cumulativeWinnings / scaleY) * usableHeight).toFloat()

                                    // Stakes Snap Dot
                                    drawCircle(
                                        color = currentTextWhite,
                                        radius = 4.dp.toPx(),
                                        center = Offset(ptX, ptStkY)
                                    )
                                    drawCircle(
                                        color = currentPrimarySapphire,
                                        radius = 2.5.dp.toPx(),
                                        center = Offset(ptX, ptStkY)
                                    )

                                    // Winnings Snap Dot
                                    drawCircle(
                                        color = currentTextWhite,
                                        radius = 4.dp.toPx(),
                                        center = Offset(ptX, ptWinY)
                                    )
                                    drawCircle(
                                        color = currentNeonGreen,
                                        radius = 2.5.dp.toPx(),
                                        center = Offset(ptX, ptWinY)
                                    )
                                }
                            }
                        } else if (numPoints == 1) {
                            // Draw horizontal flat plots
                            val ptStkY = (size.height - bottomPadding - (chartPoints[0].cumulativeStake / scaleY) * usableHeight).toFloat()
                            val ptWinY = (size.height - bottomPadding - (chartPoints[0].cumulativeWinnings / scaleY) * usableHeight).toFloat()

                            drawLine(color = currentPrimarySapphire, start = Offset(leftPadding, ptStkY), end = Offset(size.width - rightPadding, ptStkY), strokeWidth = 2.dp.toPx())
                            drawLine(color = currentNeonGreen, start = Offset(leftPadding, ptWinY), end = Offset(size.width - rightPadding, ptWinY), strokeWidth = 2.dp.toPx())
                        }
                    } else {
                        // DOUBLE SIDE-BY-SIDE COLUMN BAR CHART
                        val numPoints = chartPoints.size
                        val spacing = usableWidth / numPoints
                        val barWidth = (spacing / 3f).coerceIn(4.dp.toPx(), 18.dp.toPx())

                        chartPoints.forEachIndexed { i, pt ->
                            val ptX = leftPadding + i * spacing + (spacing / 2f)

                            val yStkVal = pt.stake
                            val yWinVal = pt.winnings

                            val hStk = ((yStkVal / scaleY) * usableHeight).toFloat()
                            val hWin = ((yWinVal / scaleY) * usableHeight).toFloat()

                            val yStkStart = (size.height - bottomPadding - hStk).toFloat()
                            val yWinStart = (size.height - bottomPadding - hWin).toFloat()

                            // Highlight selected Column Background bar
                            if (selectedIndex == i) {
                                drawRoundRect(
                                    color = currentBorderColor.copy(alpha = 0.25f),
                                    topLeft = Offset(leftPadding + i * spacing, topPadding),
                                    size = Size(spacing, usableHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx())
                                )
                            }

                            // Stake Bar (Left Column of Pair)
                            drawRoundRect(
                                color = currentPrimarySapphire,
                                topLeft = Offset(ptX - barWidth - 1.dp.toPx(), yStkStart),
                                size = Size(barWidth, hStk.coerceAtLeast(1f)),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )

                            // Winnings Bar (Right Column of Pair)
                            val winBarColor = when (pt.status) {
                                "WON" -> currentNeonGreen
                                "CASHOUT" -> currentNeonGreen.copy(alpha = 0.75f)
                                "LOST" -> currentLightRed.copy(alpha = 0.35f)
                                else -> currentTextGrey
                            }
                            drawRoundRect(
                                color = winBarColor,
                                topLeft = Offset(ptX + 1.dp.toPx(), yWinStart),
                                size = Size(barWidth, hWin.coerceAtLeast(1f)),
                                cornerRadius = CornerRadius(2.dp.toPx())
                            )
                        }
                    }

                    // DRAW BOTTOM CHRONOLOGICAL DATES (X AXIS)
                    if (chartPoints.isNotEmpty()) {
                        val paintText = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 7.dp.toPx()
                            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        if (isCumulativeView) {
                            val numIndices = chartPoints.size
                            if (numIndices > 1) {
                                val spacing = usableWidth / (numIndices - 1)
                                val stride = (numIndices / 4).coerceAtLeast(1)
                                for (i in 0 until numIndices step stride) {
                                    val date = chartPoints[i].dateLabel
                                    val x = leftPadding + i * spacing
                                    drawContext.canvas.nativeCanvas.drawText(
                                        date,
                                        x,
                                        size.height - 2.dp.toPx(),
                                        paintText
                                    )
                                }
                                // Ensure last index is printed
                                if ((numIndices - 1) % stride != 0) {
                                    drawContext.canvas.nativeCanvas.drawText(
                                        chartPoints.last().dateLabel,
                                        leftPadding + (numIndices - 1) * spacing,
                                        size.height - 2.dp.toPx(),
                                        paintText
                                    )
                                }
                            } else {
                                drawContext.canvas.nativeCanvas.drawText(
                                    chartPoints[0].dateLabel,
                                    leftPadding + usableWidth / 2f,
                                    size.height - 2.dp.toPx(),
                                    paintText
                                )
                            }
                        } else {
                            // Bars Mode Labels
                            val numIndices = chartPoints.size
                            val spacing = usableWidth / numIndices
                            val labelStride = (numIndices / 5).coerceAtLeast(1)
                            for (i in 0 until numIndices step labelStride) {
                                val label = chartPoints[i].dateLabel
                                val x = leftPadding + i * spacing + spacing / 2f
                                drawContext.canvas.nativeCanvas.drawText(
                                    label,
                                    x,
                                    size.height - 2.dp.toPx(),
                                    paintText
                                )
                            }
                        }
                    }

                    // DRAW Y AXIS LABELS ON THE LEFT
                    val paintYText = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#64748B")
                        textSize = 6.5.dp.toPx()
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                    val labelYValues = listOf(0.0, maxValue * 0.5, maxValue)
                    labelYValues.forEach { valY ->
                        val y = (size.height - bottomPadding - (valY / scaleY) * usableHeight).toFloat()
                        val textLabel = if (valY >= 1000) String.format("%.1fk", valY / 1000) else String.format("%.0f", valY)
                        drawContext.canvas.nativeCanvas.drawText(
                            textLabel,
                            leftPadding - 4.dp.toPx(),
                            y + 2.5.dp.toPx(),
                            paintYText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color legends indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(currentPrimarySapphire, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCumulativeView) "Cum. Stakes" else "Wager Stake",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(currentNeonGreen, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCumulativeView) "Cum. Returns" else "Ticket Return",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
            }

            // SNAP SHOT HIGH-TECH RECHARTS TOOLTIP INFO SCREEN
            activeSelectedPoint?.let { pt ->
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .border(1.dp, currentBorderColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(currentPrimarySapphire.copy(0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Slip ${pt.ticketNumber}",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = currentPrimarySapphire
                                )
                            }
                            Text(text = "(${pt.dateLabel})", fontSize = 9.sp, color = TextMuted)
                        }

                        // Selection label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when (pt.status) {
                                        "WON" -> currentNeonGreen.copy(0.15f)
                                        "CASHOUT" -> AmberAccent.copy(0.15f)
                                        else -> currentLightRed.copy(0.15f)
                                    }
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pt.status,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = when (pt.status) {
                                    "WON" -> currentNeonGreen
                                    "CASHOUT" -> AmberAccent
                                    else -> currentLightRed
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isCumulativeView) "CUMULATIVE STAKE" else "STAKE PLACED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = currentTextGrey)
                            Text(
                                text = "${String.format("%,.1f", if (isCumulativeView) pt.cumulativeStake else pt.stake)} ETB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentTextWhite
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = if (isCumulativeView) "CUMULATIVE RETURN" else "PAYOUT RETURN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = currentTextGrey)
                            Text(
                                text = "${String.format("%,.1f", if (isCumulativeView) pt.cumulativeWinnings else pt.winnings)} ETB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentNeonGreen
                            )
                        }
                    }

                    // Net profit diff
                    val pointProfit = if (isCumulativeView) pt.cumulativeWinnings - pt.cumulativeStake else pt.winnings - pt.stake
                    Divider(color = currentBorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isCumulativeView) "NET BALANCE DELTA" else "NET RETURN RESULT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "${if (pointProfit >= 0) "PROFIT +" else "LOSS "}${String.format("%,.1f", pointProfit)} ETB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (pointProfit >= 0) currentNeonGreen else currentLightRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BetsHistoryScreen(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    val bets by viewModel.allBets.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = listOf("ACTIVE SLIPS", "SETTLE HISTORY", "TRANSACTIONS")
    val activeBets = bets.filter { it.status == "PENDING" }
    val settledBets = bets.filter { it.status != "PENDING" }

    // Combined unified transactions sorted chronologically (Newest first)
    val unifiedItems = remember(bets, transactions) {
        val list = mutableListOf<UnifiedTx>()
        
        // 1. Map all bets
        bets.forEach { bet ->
            // Wager placed
            list.add(
                UnifiedTx(
                    id = "WGR-${bet.id + 52890}",
                    type = "WAGER",
                    amount = bet.stake,
                    status = bet.status, // "PENDING", "WON", "LOST", "CASHOUT"
                    method = "Sports Wallet",
                    timeLabel = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bet.timestamp)),
                    timestamp = bet.timestamp,
                    description = "Wager: ${bet.teamA} vs ${bet.teamB} (${bet.selection})"
                )
            )
            
            // Payout for Won/Cashout
            if (bet.status == "WON") {
                list.add(
                    UnifiedTx(
                        id = "PAY-${bet.id + 52890}",
                        type = "PAYOUT",
                        amount = bet.potentialReturn,
                        status = "WON", // Won bet payout status is WON
                        method = "Sports Wallet",
                        timeLabel = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bet.timestamp + 7200000)),
                        timestamp = bet.timestamp + 7200000,
                        description = "Payout: Slip #${bet.id + 52890} Won"
                    )
                )
            } else if (bet.status == "CASHOUT") {
                list.add(
                    UnifiedTx(
                        id = "PAY-${bet.id + 52890}",
                        type = "PAYOUT",
                        amount = bet.stake * 0.85,
                        status = "CASHOUT",
                        method = "Sports Wallet",
                        timeLabel = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(bet.timestamp + 3600000)),
                        timestamp = bet.timestamp + 3600000,
                        description = "Payout: Slip #${bet.id + 52890} Cashout"
                    )
                )
            }
        }
        
        // 2. Map deposits and withdrawals
        transactions.forEach { tx ->
            list.add(
                UnifiedTx(
                    id = tx.id,
                    type = tx.type, // "DEPOSIT", "WITHDRAWAL"
                    amount = tx.amount,
                    status = tx.status, // "APPROVED", "PENDING", "REJECTED"
                    method = tx.method,
                    timeLabel = tx.timeLabel,
                    timestamp = tx.timestamp,
                    description = if (tx.type == "DEPOSIT") "Fund Deposit via ${tx.method}" else "Withdrawal via ${tx.method}"
                )
            )
        }
        
        list.sortedByDescending { it.timestamp }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }
    val filters = listOf("ALL", "BETS", "DEPOSITS", "WITHDRAWALS")

    val filteredUnifiedItems = remember(unifiedItems, selectedFilter) {
        when (selectedFilter) {
            "BETS" -> unifiedItems.filter { it.type == "WAGER" || it.type == "PAYOUT" }
            "DEPOSITS" -> unifiedItems.filter { it.type == "DEPOSIT" }
            "WITHDRAWALS" -> unifiedItems.filter { it.type == "WITHDRAWAL" }
            else -> unifiedItems
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBG)
    ) {
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateCardBG,
            contentColor = TextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimarySapphire,
                    height = 3.dp
                )
            },
            divider = { Divider(color = BorderColor) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("bet_tab_$index")
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.s_p,
                            color = if (selectedTab == index) TextWhite else TextMuted
                        )
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab < 2) {
                    val currentList = if (selectedTab == 0) activeBets else settledBets

                    if (currentList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Outlined.ContentPaste else Icons.Outlined.AssignmentTurnedIn,
                                    contentDescription = null,
                                    tint = TextGrey,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedTab == 0) "No Active Betting Slips" else "No Settled Betting History",
                                    fontSize = 14.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = "Place fresh bets from the Sportsbook Dashboard to start.",
                                    fontSize = 11.s_p,
                                    color = TextGrey,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedTab == 1) {
                                item {
                                    BettingPerformanceDashboard(settledBets = currentList)
                                }
                            }
                            items(currentList, key = { it.id }) { bet ->
                                BetRecordCard(
                                    bet = bet,
                                    onCashout = { viewModel.cashoutBet(bet) },
                                    onSimulateWin = { viewModel.resolveBet(bet, true) },
                                    onSimulateLoss = { viewModel.resolveBet(bet, false) },
                                    onApprove = { viewModel.approveBet(bet) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                } else {
                    // TRANSACTIONS LEDGER TAB
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Analytics counter overview cards
                        val approvedDeposits = remember(transactions) {
                            transactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
                        }
                        val approvedWithdrawals = remember(transactions) {
                            transactions.filter { it.type == "WITHDRAWAL" && it.status == "APPROVED" }.sumOf { it.amount }
                        }
                        val activeWagers = remember(bets) {
                            bets.filter { it.status == "PENDING" }.sumOf { it.stake }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Deposits Stats Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "DEPOSITED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format("%,.0f", approvedDeposits)} ETB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                }
                            }

                            // Withdrawals Stats Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "WITHDRAWN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format("%,.0f", approvedWithdrawals)} ETB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LightRed
                                    )
                                }
                            }

                            // Active Wagers Card
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "ACTIVE STAKES", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format("%,.0f", activeWagers)} ETB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filters) { f ->
                                CustomFilterChip(
                                    selected = selectedFilter == f,
                                    text = f,
                                    onClick = { selectedFilter = f },
                                    modifier = Modifier.testTag("tx_filter_$f")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // List of unified ledger items
                        if (filteredUnifiedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = TextGrey,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Transaction Logs Matching '$selectedFilter'",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Funding your wallet or placing sportsbook tickets generates instant records here.",
                                        fontSize = 11.sp,
                                        color = TextGrey,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(filteredUnifiedItems, key = { it.id }) { tx ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("unified_tx_card_${tx.id}"),
                                        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                                        border = BorderStroke(1.dp, BorderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val (iconColor, bgColor, iconVec) = when (tx.type) {
                                                    "DEPOSIT" -> Triple(NeonGreen, NeonGreen.copy(0.12f), Icons.Default.ArrowDownward)
                                                    "WITHDRAWAL" -> Triple(LightRed, LightRed.copy(0.12f), Icons.Default.ArrowUpward)
                                                    "WAGER" -> Triple(Color(0xFF38BDF8), Color(0xFF38BDF8).copy(0.12f), Icons.Default.SportsBasketball)
                                                    "PAYOUT" -> Triple(AmberAccent, AmberAccent.copy(0.12f), Icons.Default.LocalFireDepartment)
                                                    else -> Triple(TextWhite, BorderColor, Icons.Default.ReceiptLong)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(bgColor, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = iconVec,
                                                        contentDescription = null,
                                                        tint = iconColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = when (tx.type) {
                                                            "WAGER" -> "WAGER PLACED"
                                                            "PAYOUT" -> "BET PAYOUT WIN"
                                                            else -> tx.type
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = TextWhite
                                                    )
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                    Text(
                                                        text = tx.description,
                                                        fontSize = 10.sp,
                                                        color = TextLight,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "Ref: ${tx.id}",
                                                            fontSize = 8.sp,
                                                            color = TextMuted,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Box(modifier = Modifier.size(2.dp).background(TextMuted, CircleShape))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = tx.timeLabel,
                                                            fontSize = 9.sp,
                                                            color = TextMuted
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Column(
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                val amountSign = when (tx.type) {
                                                    "DEPOSIT", "PAYOUT" -> "+"
                                                    "WITHDRAWAL", "WAGER" -> "-"
                                                    else -> ""
                                                }
                                                val amountColor = when (tx.type) {
                                                    "DEPOSIT", "PAYOUT" -> NeonGreen
                                                    "WITHDRAWAL", "WAGER" -> TextWhite
                                                    else -> TextWhite
                                                }
                                                
                                                Text(
                                                    text = "$amountSign${String.format("%,.2f", tx.amount)} ETB",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = amountColor
                                                )
                                                
                                                Spacer(modifier = Modifier.height(3.dp))
                                                
                                                val (statusColor, statusBg) = when (tx.status) {
                                                    "APPROVED", "WON", "SUCCESS" -> Pair(NeonGreen, NeonGreen.copy(0.12f))
                                                    "PENDING" -> Pair(AmberAccent, AmberAccent.copy(0.12f))
                                                    "LOST" -> Pair(LightRed, LightRed.copy(0.12f))
                                                    "CASHOUT" -> Pair(TextMuted, SlateSurfaceL2)
                                                    else -> Pair(TextMuted, BorderColor)
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .background(statusBg, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = when (tx.status) {
                                                            "APPROVED" -> "SUCCESS"
                                                            else -> tx.status
                                                        },
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = statusColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketQrCode(text: String, sizeDp: androidx.compose.ui.unit.Dp = 72.dp) {
    val hash = text.hashCode()
    Canvas(
        modifier = Modifier
            .size(sizeDp)
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(4.dp)
            .testTag("ticket_qr_${text}")
    ) {
        val sizePx = size.width
        val modules = 15 // grid dimensions
        val moduleSize = sizePx / modules
        
        // Solid white background
        drawRect(color = Color.White)

        // Helper function for finder anchor squares in standard 2D codes
        fun drawFinderPattern(col: Int, row: Int) {
            drawRect(
                color = Color.Black,
                topLeft = Offset(col * moduleSize, row * moduleSize),
                size = Size(5 * moduleSize, 5 * moduleSize)
            )
            drawRect(
                color = Color.White,
                topLeft = Offset((col + 1) * moduleSize, (row + 1) * moduleSize),
                size = Size(3 * moduleSize, 3 * moduleSize)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset((col + 2) * moduleSize, (row + 2) * moduleSize),
                size = Size(moduleSize, moduleSize)
            )
        }
        
        // Render anchors at top-left, top-right, and bottom-left bounds
        drawFinderPattern(0, 0)
        drawFinderPattern(modules - 5, 0)
        drawFinderPattern(0, modules - 5)
        
        // Fill other modules procedurally with a seed from the ticket Id hash
        val random = java.util.Random(hash.toLong())
        for (r in 0 until modules) {
            for (c in 0 until modules) {
                val isNearTopLeft = r < 5 && c < 5
                val isNearTopRight = r < 5 && c >= modules - 5
                val isNearBottomLeft = r >= modules - 5 && c < 5
                
                if (!isNearTopLeft && !isNearTopRight && !isNearBottomLeft) {
                    if (random.nextBoolean()) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(c * moduleSize, r * moduleSize),
                            size = Size(moduleSize + 0.3f, moduleSize + 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BetRecordCard(
    bet: Bet,
    onCashout: () -> Unit,
    onSimulateWin: () -> Unit,
    onSimulateLoss: () -> Unit,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bet_record_card_${bet.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(
            width = 1.dp,
            color = if (bet.isApproved) NeonGreen.copy(0.4f) else BorderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Category, Date/Time and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(PrimarySapphire.copy(0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bet.sport.uppercase(),
                            fontSize = 8.s_p,
                            fontWeight = FontWeight.Bold,
                            color = PrimarySapphire
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SLIP #${bet.id + 52890}",
                        fontSize = 10.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }
                
                // Payout status stamp
                val (statusColor, statusBg) = when (bet.status) {
                    "WON" -> Pair(NeonGreen, NeonGreen.copy(0.12f))
                    "LOST" -> Pair(LightRed, LightRed.copy(0.12f))
                    "CASHOUT" -> Pair(AmberAccent, AmberAccent.copy(0.12f))
                    else -> Pair(PrimarySapphire, PrimarySapphire.copy(0.12f))
                }
                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bet.status,
                        fontSize = 10.s_p,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Visual Coupon Content holding details and the vector QR code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${bet.teamA} vs ${bet.teamB}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "Pick: ${bet.selection}",
                        fontSize = 11.sp,
                        color = TextLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Market: ${bet.marketType}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Odds multiplier: @${String.format("%.2f", bet.odds)}",
                        fontSize = 11.sp,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Visual QR Code
                Box(
                    modifier = Modifier
                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                        .background(SlateSurfaceL2)
                        .padding(4.dp)
                ) {
                    TicketQrCode(text = bet.ticketNumber, sizeDp = 72.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Dashed Separator / Coupon cut line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BorderColor, Color.Transparent, BorderColor),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 0f)
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(10.dp))

            // Money details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "TICKET STAKE", fontSize = 8.s_p, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${String.format("%,.2f", bet.stake)} ETB",
                        fontSize = 13.s_p,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (bet.status == "PENDING") "POTENTIAL PAYOUT" else "TOTAL RETURNED",
                        fontSize = 8.s_p,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format("%,.2f", bet.potentialReturn)} ETB",
                        fontSize = 13.s_p,
                        fontWeight = FontWeight.Black,
                        color = if (bet.status == "WON" || bet.status == "CASHOUT") NeonGreen else TextWhite
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Linear digital Retail style Barcode block
            val localTextLight = TextLight
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(SlateSurfaceL2, RoundedCornerShape(4.dp))
                    .padding(vertical = 3.dp, horizontal = 12.dp)
            ) {
                val barCount = 48
                val step = size.width / barCount
                val random = java.util.Random(bet.ticketNumber.hashCode().toLong())
                for (i in 0 until barCount) {
                    val barWidth = if (random.nextBoolean()) step * 0.7f else step * 0.3f
                    if (random.nextBoolean()) {
                        drawRect(
                            color = localTextLight.copy(alpha = 0.5f),
                            topLeft = Offset(i * step, 0f),
                            size = Size(barWidth, size.height)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Centered printed ticket sequence number
            val rawNum = bet.ticketNumber
            val formattedTicketNum = if (rawNum.length >= 12) {
                "${rawNum.substring(0,3)}-${rawNum.substring(3,7)}-${rawNum.substring(7,11)}-${rawNum.substring(11)}"
            } else {
                rawNum
            }
            Text(
                text = "TICKET SEQUENCE: $formattedTicketNum",
                fontSize = 9.s_p,
                color = TextGrey,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))
            
            // Dynamic Approval Area (State dependent layouts)
            if (bet.isApproved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreen.copy(0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified Ticket",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TICKET OFFICIALLY REGISTERED & APPROVED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                            Text(
                                text = "Authorized for automatic secure wallet transactions.",
                                fontSize = 8.sp,
                                color = TextLight
                            )
                        }
                    }
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AmberAccent.copy(0.1f), RoundedCornerShape(6.dp))
                            .border(1.dp, AmberAccent.copy(0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrivacyTip,
                            contentDescription = "Pending secure approval",
                            tint = AmberAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Awaiting official block approval sequence...",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Approval action button
                    Button(
                        onClick = onApprove,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("btn_approve_ticket_${bet.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = SlateDarkBG
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "APPROVE WAGER TICKET NOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Normal bet resolution controllers (WINS / LOSSES / CASHOUTS)
            if (bet.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val estCashout = bet.potentialReturn * 0.8
                    Button(
                        onClick = onCashout,
                        modifier = Modifier.weight(1.2f).height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateSurfaceL2,
                            contentColor = AmberAccent
                        ),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "CASHOUT: ${Math.round(estCashout)} ETB",
                                fontSize = 9.s_p,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Button(
                        onClick = onSimulateWin,
                        modifier = Modifier.weight(0.7f).height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(0.15f), contentColor = NeonGreen),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "SIM WIN", fontSize = 9.s_p, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSimulateLoss,
                        modifier = Modifier.weight(0.7f).height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.15f), contentColor = LightRed),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = "SIM LOSS", fontSize = 9.s_p, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. SECURE FINANCES & STATEMENT SCREEN
// ==========================================

@Composable
fun FinancesScreen(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.wallet.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    
    var subTab by remember { mutableStateOf(0) } // 0: Deposit, 1: Withdraw
    var textAmount by remember { mutableStateOf("") }
    var selectedGateway by remember { mutableStateOf("TeleBirr") }
    
    // Security dialog triggers
    var securityTriggerCode by remember { mutableStateOf<String?>(null) }
    var securityProgressMessage by remember { mutableStateOf("") }
    var isVerifyingToken by remember { mutableStateOf(false) }
    var currentTelebirrDetails by remember { mutableStateOf<String?>(null) }
    
    // Biometric payment states
    var showBiometricPaymentAuthDialog by remember { mutableStateOf(false) }
    var pendingPaymentAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    val bioPaymentsEnabled by viewModel.biometricPaymentsEnabled.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    val creditSecuredGateways = listOf("TeleBirr", "CBE Birr", "Credit Card", "USDT Crypto")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBG),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
        item { Spacer(modifier = Modifier.height(10.dp)) }

        // Balance top card card
        item {
            wallet?.let {
                WalletSummaryCard(wallet = it)
            }
        }

        // Sub categories deposit vs withdrawal selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { subTab = 0 },
                        modifier = Modifier.weight(1f).testTag("select_deposit_tab"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == 0) PrimarySapphire else Color.Transparent,
                            contentColor = if (subTab == 0) TextWhite else TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "DEPOSIT", fontWeight = FontWeight.Bold, fontSize = 12.s_p)
                    }
                    
                    Button(
                        onClick = { subTab = 1 },
                        modifier = Modifier.weight(1f).testTag("select_withdraw_tab"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == 1) PrimarySapphire else Color.Transparent,
                            contentColor = if (subTab == 1) TextWhite else TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "WITHDRAW", fontWeight = FontWeight.Bold, fontSize = 12.s_p)
                    }
                }
            }
        }

        // Gateway select channels
        item {
            Text(
                text = "Select Payment Channel",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                creditSecuredGateways.forEach { gateway ->
                    val isChanSelected = selectedGateway == gateway
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isChanSelected) PrimarySapphire.copy(0.12f) else SlateCardBG,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isChanSelected) PrimarySapphire else BorderColor,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedGateway = gateway }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val logoIcon = when (gateway) {
                                "TeleBirr" -> Icons.Default.PhoneAndroid
                                "CBE Birr" -> Icons.Default.AccountBalance
                                "Credit Card" -> Icons.Default.CreditCard
                                else -> Icons.Default.CurrencyBitcoin
                            }
                            Icon(
                                imageVector = logoIcon,
                                contentDescription = null,
                                tint = if (isChanSelected) PrimarySapphire else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gateway,
                                fontSize = 9.s_p,
                                color = if (isChanSelected) TextWhite else TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Amount Input text tag
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ENTER TRANSACTION VALUE",
                        fontSize = 10.s_p,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = textAmount,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) textAmount = it },
                        modifier = Modifier.fillMaxWidth().testTag("finance_amount_input"),
                        placeholder = { Text("0.00 ETB", color = TextGrey) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = PrimarySapphire,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SlateSurfaceL2,
                            unfocusedContainerColor = SlateSurfaceL2
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Secure Submit Button
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val amount = textAmount.toDoubleOrNull() ?: 0.0
                            if (amount <= 0.0) {
                                return@Button
                            }
                            
                            val executePayment: () -> Unit = {
                                scope.launch {
                                    isVerifyingToken = true
                                    securityTriggerCode = "PROCESSING"
                                    securityProgressMessage = "Initializing secured gateway handshake..."
                                    
                                    if (selectedGateway == "TeleBirr") {
                                        val fields = mapOf(
                                            "appId" to com.example.BuildConfig.TELEBIRR_APP_ID,
                                            "merchCode" to com.example.BuildConfig.TELEBIRR_MCH_SHORT_CODE,
                                            "outTradeNo" to "TX_" + (100000..999999).random().toString(),
                                            "notifyUrl" to com.example.BuildConfig.TELEBIRR_NOTIFY_URL,
                                            "returnUrl" to com.example.BuildConfig.TELEBIRR_RETURN_URL,
                                            "receiverName" to com.example.BuildConfig.TELEBIRR_RECEIVE_NAME,
                                            "totalAmount" to amount.toString(),
                                            "timeoutExpress" to "120",
                                            "timestamp" to System.currentTimeMillis().toString(),
                                            "nonce" to java.util.UUID.randomUUID().toString()
                                        )
                                        val signStr = com.example.util.TelebirrUtil.createSignString(fields)
                                        val signature = com.example.util.TelebirrUtil.signPayload(signStr, com.example.BuildConfig.MERCHANT_PRIVATE_KEY)
                                        val encrypted = com.example.util.TelebirrUtil.encryptUssdPlanet(signStr)
                                        
                                        currentTelebirrDetails = """
                                            📌 ALPHABETIC SORTED QUERY STRING
                                            $signStr
                                            
                                            🔑 RSA SIGNATURE (SHA256withRSA)
                                            $signature
                                            
                                            📦 ENCRYPTED BASE64 USSD PAYLOAD
                                            $encrypted
                                        """.trimIndent()
                                    } else {
                                        currentTelebirrDetails = null
                                    }

                                    delay(1200)
                                    securityProgressMessage = "Encrypting packet vectors with SEC-RSA-4096..."
                                    delay(1000)
                                    securityProgressMessage = "Securing socket confirmation from $selectedGateway API..."
                                    delay(1200)
                                    
                                    if (subTab == 0) {
                                        val limitError = viewModel.checkDepositLimits(amount)
                                        if (limitError != null) {
                                            securityProgressMessage = "Transaction Denied: $limitError"
                                            securityTriggerCode = "FAILED_ERR"
                                        } else if (selectedGateway == "TeleBirr") {
                                            viewModel.depositFundsPending(amount) { tx ->
                                                securityProgressMessage = "Payment initiated! Telebirr billing has been created as PENDING. Go to Admin Panel to simulate the secure callback webhook."
                                                securityTriggerCode = "SUCCESS_TX: $tx"
                                            }
                                        } else {
                                            viewModel.depositFunds(amount, selectedGateway) { tx ->
                                                securityProgressMessage = "Payment Approved! Wallet credited securely."
                                                securityTriggerCode = "SUCCESS_TX: $tx"
                                            }
                                        }
                                    } else {
                                        viewModel.withdrawFunds(
                                            amount = amount,
                                            gateway = selectedGateway,
                                            onSuccess = { tx ->
                                                securityProgressMessage = "Withdrawal request authorized! Processing settlement."
                                                securityTriggerCode = "SUCCESS_TX: $tx"
                                            },
                                            onError = { message ->
                                                securityProgressMessage = "Transaction Denied: $message"
                                                securityTriggerCode = "FAILED_ERR"
                                            }
                                        )
                                    }
                                    textAmount = ""
                                }
                            }

                            if (bioPaymentsEnabled) {
                                pendingPaymentAction = executePayment
                                showBiometricPaymentAuthDialog = true
                            } else {
                                executePayment()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("secure_pay_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subTab == 0) NeonGreen else PrimarySapphire,
                            contentColor = SlateDarkBG
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = SlateDarkBG
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (subTab == 0) "SECURE TOP UP" else "SECURE WITHDRAW",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.s_p,
                                color = SlateDarkBG
                            )
                        }
                    }
                }
            }
        }

        // Biometric Security Guard Preference card
        item {
            val bioEnabled by viewModel.biometricQuickBetEnabled.collectAsState()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_security_card"),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimarySapphire.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = PrimarySapphire,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Biometric Quick Bet Layer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Secure instantly using fingerprint TouchID",
                                fontSize = 9.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = bioEnabled,
                        onCheckedChange = { viewModel.setBiometricQuickBetEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SlateSurfaceL2
                        ),
                        modifier = Modifier.testTag("biometric_quick_bet_switch")
                    )
                }
            }
        }

        // Biometric Payments preference card
        item {
            val bioPaymentsActive by viewModel.biometricPaymentsEnabled.collectAsState()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_payments_card"),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Biometric Payments Lock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Secure deposits & withdrawals with TouchID",
                                fontSize = 9.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = bioPaymentsActive,
                        onCheckedChange = { viewModel.setBiometricPaymentsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SlateSurfaceL2
                        ),
                        modifier = Modifier.testTag("biometric_payments_switch")
                    )
                }
            }
        }

        // STATEMENT SUMMARY (Recent transactions tables corresponding to user screenshot admin data!)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (transactions.isEmpty()) {
            item {
                EmptyStateCard(message = "No financial transaction logs found in records.")
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionListItem(transaction = tx)
            }
        }
    }
}

    // High Fidelity SECURE GATEWAY ENCRYPTION OVERLAY DIALOG DRAWING
    if (isVerifyingToken) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val trxCode = securityTriggerCode ?: "PROCESSING"
                    
                    if (trxCode == "PROCESSING") {
                        CircularProgressIndicator(
                            color = PrimarySapphire,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "SECURE BANKING ENCRYPTION",
                            fontSize = 11.s_p,
                            fontWeight = FontWeight.Bold,
                            color = PrimarySapphire,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = securityProgressMessage,
                            textAlign = TextAlign.Center,
                            color = TextLight,
                            fontSize = 13.s_p
                        )

                        currentTelebirrDetails?.let { details ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "LIVE TELEBIRR CRYPTO ENGINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Text(
                                            text = details,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = TextGrey
                                        )
                                    }
                                }
                            }
                        }
                    } else if (trxCode.startsWith("SUCCESS_TX")) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NeonGreen,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "VERIFICATION APPROVED",
                            fontSize = 14.s_p,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = securityProgressMessage,
                            textAlign = TextAlign.Center,
                            color = TextMuted,
                            fontSize = 12.s_p
                        )
                        Text(
                            text = "Log Ref ID: ${trxCode.substringAfter("SUCCESS_TX: ")}",
                            fontSize = 10.s_p,
                            color = TextGrey,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { isVerifyingToken = false },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire, contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "DONE", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Failed State
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Failed",
                            tint = LightRed,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "TRANSACTION DENIED",
                            fontSize = 14.s_p,
                            fontWeight = FontWeight.Black,
                            color = LightRed
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = securityProgressMessage,
                            textAlign = TextAlign.Center,
                            color = TextMuted,
                            fontSize = 12.s_p
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { isVerifyingToken = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2, contentColor = TextWhite),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Text(text = "CANCEL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // OVERLAY: SECURE BIOMETRIC PAYMENT AUTHENTICATION DIALOG
    if (showBiometricPaymentAuthDialog) {
        val payAmt = textAmount.toDoubleOrNull() ?: 0.0
        SecureBiometricVerificationDialog(
            title = if (subTab == 0) "AUTHORIZE TOP-UP DEPOSIT" else "AUTHORIZE CASH WITHDRAWAL",
            subtitle = "Crypto shield layer is verifying ${String.format("%,.2f", payAmt)} ETB transaction via $selectedGateway gateway.",
            onAuthSuccess = {
                showBiometricPaymentAuthDialog = false
                pendingPaymentAction?.invoke()
                pendingPaymentAction = null
            },
            onDismissRequest = {
                showBiometricPaymentAuthDialog = false
                pendingPaymentAction = null
            }
        )
    }
}

@Composable
fun TransactionListItem(transaction: TransactionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Direction Arrow circle
                val isDeposit = transaction.type == "DEPOSIT"
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            if (isDeposit) NeonGreen.copy(0.12f) else LightRed.copy(0.12f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isDeposit) NeonGreen else LightRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${transaction.type.uppercase()} [${transaction.method}]",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${transaction.id} • ${transaction.timeLabel}",
                        fontSize = 10.s_p,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == "DEPOSIT") "+" else "-"}${String.format("%,.2f", transaction.amount)} ETB",
                    fontSize = 12.s_p,
                    fontWeight = FontWeight.Black,
                    color = if (transaction.type == "DEPOSIT") NeonGreen else TextLight
                )
                
                // Active status code badge
                val stColor = when (transaction.status) {
                    "APPROVED" -> NeonGreen
                    "PENDING" -> AmberAccent
                    else -> LightRed
                }
                Text(
                    text = transaction.status,
                    fontSize = 9.s_p,
                    fontWeight = FontWeight.Black,
                    color = stColor
                )
            }
        }
    }
}

// ==========================================
// 4. OVERLAY: AI PREDICTION DIALOG GAUGE (CANVAS DRAWINGS!)
// ==========================================

@Composable
fun AIPredictionDialog(
    match: SportMatch,
    viewModel: BetViewModel,
    onDismissRequest: () -> Unit
) {
    val prediction by viewModel.currentPrediction.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(match.id) {
        viewModel.fetchMatchPrediction(match)
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("ai_prediction_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Head
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BETMASTER AI PRO ANALYST",
                            fontSize = 11.s_p,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            viewModel.clearPrediction()
                            onDismissRequest()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextGrey)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                if (isAnalyzing || prediction == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        // Advanced animated loading circle
                        val infiniteTransition = rememberInfiniteTransition(label = "ai_rot")
                        val rotAngle by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing)
                            ),
                            label = "angle"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(SlateSurfaceL2, CircleShape)
                                .border(1.dp, BorderColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = PrimarySapphire,
                                modifier = Modifier
                                    .size(36.dp)
                                    .graphicsLayer(rotationZ = rotAngle)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "GENERATING TACTICAL INTEL...",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.s_p,
                            color = PrimarySapphire
                        )
                        Text(
                            text = "Gemini AI is examining team stats, historic form curves, and sports market volatility metrics...",
                            fontSize = 11.s_p,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp).padding(top = 4.dp)
                        )
                    }
                } else {
                    val predData = prediction!!
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 460.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Canvas semicircular Gauge drawing for Safety Score!
                        val animProgress = remember { Animatable(0f) }
                        LaunchedEffect(predData.safetyScore) {
                            animProgress.animateTo(
                                targetValue = predData.safetyScore.toFloat() / 100f,
                                animationSpec = tween(1000, easing = FastOutSlowInEasing)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val localBorderColor = BorderColor
                            Canvas(modifier = Modifier.size(150.dp, 100.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Color dial brush
                                val sweepBrush = Brush.linearGradient(
                                    colors = listOf(LightRed, AmberAccent, NeonGreen),
                                    start = Offset(0f, canvasHeight),
                                    end = Offset(canvasWidth, canvasHeight)
                                )
                                
                                // Background Arc
                                drawArc(
                                    color = localBorderColor,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                                    size = Size(canvasWidth - 20.dp.toPx(), (canvasHeight * 2) - 20.dp.toPx()),
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                
                                // Active Dial Progress arc based on animProgress
                                drawArc(
                                    brush = sweepBrush,
                                    startAngle = 180f,
                                    sweepAngle = 180f * animProgress.value,
                                    useCenter = false,
                                    topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                                    size = Size(canvasWidth - 20.dp.toPx(), (canvasHeight * 2) - 20.dp.toPx()),
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            
                            Column(
                                modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${predData.safetyScore}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (predData.safetyScore >= 75) NeonGreen else if (predData.safetyScore >= 55) AmberAccent else LightRed
                                )
                                Text(
                                    text = "SAFETY COEFFICIENT",
                                    fontSize = 8.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            }
                        }
                        
                        // Suggested outcome pick card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "RECOMMENDED VALUE PICK",
                                    fontSize = 9.s_p,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = predData.prediction,
                                    fontSize = 15.s_p,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 📈 ODDS FLUCTUATIONS CHART COMPONENT
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurfaceL2)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timeline,
                                        contentDescription = "Timeline",
                                        tint = Color(0xFF00C2FF),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "📈 ODDS FLUCTUATIONS CHART",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                                Text(
                                    text = "Last 20m Trend",
                                    fontSize = 7.5.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Interactive Legend
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF00C2FF), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Home (1) @ ${match.odds1}", fontSize = 7.5.sp, color = TextLight)
                                }
                                if (match.oddsX > 1.0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(Color(0xFFFFB300), CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Draw (X) @ ${match.oddsX}", fontSize = 7.5.sp, color = TextLight)
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFE040FB), CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Away (2) @ ${match.odds2}", fontSize = 7.5.sp, color = TextLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // High precision vector charting canvas
                            val points1 = remember(match.id, match.odds1) { listOf(match.odds1 * 1.15, match.odds1 * 1.05, match.odds1 * 0.95, match.odds1 * 1.08, match.odds1) }
                            val pointsX = remember(match.id, match.oddsX) { listOf(match.oddsX * 0.90, match.oddsX * 1.10, match.oddsX * 1.05, match.oddsX * 0.98, match.oddsX) }
                            val points2 = remember(match.id, match.odds2) { listOf(match.odds2 * 0.85, match.odds2 * 0.95, match.odds2 * 1.12, match.odds2 * 0.92, match.odds2) }

                            val localBorderColorChart = BorderColor
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                            ) {
                                val width = size.width
                                val height = size.height
                                val paddingLeft = 12.dp.toPx()
                                val paddingBottom = 12.dp.toPx()
                                val chartWidth = width - paddingLeft
                                val chartHeight = height - paddingBottom

                                // Gridline guides
                                for (i in 0..3) {
                                    val y = (chartHeight / 3) * i
                                    drawLine(
                                        color = localBorderColorChart.copy(0.2f),
                                        start = Offset(paddingLeft, y),
                                        end = Offset(width, y),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                val allVals = points1 + pointsX + points2
                                val maxOddsVal = (allVals.maxOrNull() ?: 5.0).toFloat() * 1.1f
                                val minOddsVal = ((allVals.minOrNull() ?: 1.0).toFloat() * 0.9f).coerceAtLeast(0.5f)
                                val delta = (maxOddsVal - minOddsVal).coerceAtLeast(0.1f)

                                fun drawTrendPath(points: List<Double>, teamColor: Color) {
                                    val drawPath = androidx.compose.ui.graphics.Path()
                                    val stepX = chartWidth / (points.size - 1)
                                    for (idx in points.indices) {
                                        val xPos = paddingLeft + idx * stepX
                                        val normY = ((points[idx].toFloat() - minOddsVal) / delta).coerceIn(0f, 1f)
                                        val yPos = chartHeight - (normY * chartHeight)
                                        if (idx == 0) drawPath.moveTo(xPos, yPos) else drawPath.lineTo(xPos, yPos)
                                        drawCircle(color = teamColor, radius = 7f, center = Offset(xPos, yPos))
                                    }
                                    drawPath(path = drawPath, color = teamColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
                                }

                                drawTrendPath(points1, Color(0xFF00C2FF))
                                if (match.oddsX > 1.0) drawTrendPath(pointsX, Color(0xFFFFB300))
                                drawTrendPath(points2, Color(0xFFE040FB))

                                drawLine(color = localBorderColorChart, start = Offset(paddingLeft, chartHeight), end = Offset(width, chartHeight), strokeWidth = 1.dp.toPx())
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("T-20m", fontSize = 7.5.sp, color = TextMuted)
                                Text("T-10m", fontSize = 7.5.sp, color = TextMuted)
                                Text("LIVE", fontSize = 7.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }

                        // 🧮 SMART RETURN BET CALCULATOR COMPONENT
                        var calcStake by remember { mutableStateOf("100") }
                        var calcOddsType by remember { mutableStateOf("1") }
                        var customOddsAmt by remember { mutableStateOf("2.50") }

                        val activeCalcOdds = when (calcOddsType) {
                            "1" -> match.odds1
                            "X" -> match.oddsX
                            "2" -> match.odds2
                            else -> customOddsAmt.toDoubleOrNull() ?: 1.0
                        }

                        val numericStake = calcStake.toDoubleOrNull() ?: 0.0
                        val grossReturnAmt = numericStake * activeCalcOdds
                        val netGain = (numericStake * (activeCalcOdds - 1.0)).coerceAtLeast(0.0)
                        val taxDeduction = netGain * 0.15
                        val realProfitNet = netGain - taxDeduction

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateSurfaceL2)
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = Color(0xFFFF6B00),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🧮 SMART RETURN CALCULATOR",
                                    style = TextStyle(color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "Formulate payouts instantly based on current market coefficients.",
                                fontSize = 7.5.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("STAKE AMOUNT (ETB)", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    OutlinedTextField(
                                        value = calcStake,
                                        onValueChange = { calcStake = it },
                                        textStyle = TextStyle(color = TextWhite, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.height(48.dp).fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedContainerColor = Color(0xFF030508),
                                            unfocusedContainerColor = Color(0xFF030508),
                                            focusedBorderColor = Color(0xFF00C2FF),
                                            unfocusedBorderColor = BorderColor
                                        ),
                                        singleLine = true
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("COEFFICIENT BASE", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(48.dp),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        listOf("1", "X", "2", "C").forEach { type ->
                                            val isSelected = calcOddsType == type
                                            val label = when(type) {
                                                "1" -> "1"
                                                "X" -> "X"
                                                "2" -> "2"
                                                else -> "Cust"
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSelected) PrimarySapphire else Color(0xFF030508))
                                                    .border(0.5.dp, if (isSelected) Color(0xFF00C2FF) else BorderColor, RoundedCornerShape(4.dp))
                                                    .clickable { calcOddsType = type }
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) TextWhite else TextLight,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (calcOddsType == "C") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ADJUST POTENTIAL ODDS:", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Text(customOddsAmt, color = Color(0xFF00C2FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = customOddsAmt.toFloatOrNull() ?: 2.5f,
                                    onValueChange = { customOddsAmt = String.format(java.util.Locale.US, "%.2f", it) },
                                    valueRange = 1.01f..15.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00C2FF), activeTrackColor = Color(0xFF00C2FF), inactiveTrackColor = BorderColor)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderColor, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("GROSS RETURN", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${String.format(java.util.Locale.US, "%.2f", grossReturnAmt)} ETB", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("15% INCOME TAX", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${String.format(java.util.Locale.US, "%.2f", taxDeduction)} ETB", fontSize = 11.sp, color = LightRed, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("EST. NET REVENUE", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("${String.format(java.util.Locale.US, "%.2f", realProfitNet)} ETB", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        // Technical analysis text block
                        Column(modifier = Modifier.align(Alignment.Start)) {
                            Text(
                                text = "MATCH ANALYTICAL INTEL:",
                                fontSize = 10.s_p,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = predData.fullAnalysis,
                                fontSize = 12.s_p,
                                color = TextLight.copy(alpha = 0.9f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricPromptSimulated(
    stakeAmount: Double,
    onAuthSuccess: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scanState by remember { mutableStateOf("AWAITING_TOUCH") } // "AWAITING_TOUCH", "SCANNING", "SUCCESS"
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(scanState) {
        if (scanState == "SCANNING") {
            progress = 0f
            while (progress < 1f) {
                delay(40)
                progress += 0.05f
            }
            scanState = "SUCCESS"
            delay(600)
            onAuthSuccess()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("biometric_auth_card_simulated"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = PrimarySapphire,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SECURE QUICK-BET SHIELD",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Confirm Stake: ${String.format("%,.2f", stakeAmount)} ETB",
                    fontSize = 14.s_p,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Biometric Authentication is active for real-time payment gateway authorization.",
                    fontSize = 10.s_p,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing biometric touch target sensor
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    if (scanState == "SUCCESS") NeonGreen.copy(alpha = 0.2f) else PrimarySapphire.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = when (scanState) {
                                    "SUCCESS" -> listOf(NeonGreen, NeonGreen.copy(alpha = 0.3f), NeonGreen)
                                    "SCANNING" -> listOf(PrimarySapphire, Color(0xFF00C2FF), PrimarySapphire)
                                    else -> listOf(BorderColor, BorderColor.copy(alpha = 0.4f), BorderColor)
                                }
                            ),
                            shape = CircleShape
                        )
                        .clickable(enabled = scanState == "AWAITING_TOUCH") {
                            scanState = "SCANNING"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val iconAnimColor by animateColorAsState(
                        targetValue = when (scanState) {
                            "SUCCESS" -> NeonGreen
                            "SCANNING" -> Color(0xFF00C2FF)
                            else -> TextWhite
                        },
                        animationSpec = tween(300),
                        label = "icon_color"
                    )

                    Icon(
                        imageVector = if (scanState == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                        contentDescription = "Touch Sensor",
                        tint = iconAnimColor,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val statusText = when (scanState) {
                    "AWAITING_TOUCH" -> "TAP SENSOR TO SECURE QUICK-BET"
                    "SCANNING" -> "SWEEPING BIO-IDENTITY CORES... ${(progress * 100).toInt()}%"
                    "SUCCESS" -> "IDENTITITY VERIFIED. DEPOSITING LEDGER..."
                    else -> "BIOMETRIC TIMEOUT ERROR"
                }

                Text(
                    text = statusText,
                    fontSize = 10.s_p,
                    color = when (scanState) {
                        "SUCCESS" -> NeonGreen
                        "SCANNING" -> Color(0xFF00C2FF)
                        else -> AmberAccent
                    },
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("biometric_cancel_btn")
                    ) {
                        Text(
                            text = "USE CONVENTIONAL PIN / CANCEL",
                            color = TextMuted,
                            fontSize = 10.s_p,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. OVERLAY: BET SLIP BOTTOM WORKSPACE DIALOG
// ==========================================

@Composable
fun PlaceBetSlipDialog(
    match: SportMatch,
    selection: String,
    odds: Double,
    viewModel: BetViewModel,
    onDismissRequest: () -> Unit,
    onBetPlacedSuccess: () -> Unit
) {
    var stakeInput by remember { mutableStateOf("100") }
    val wallet by viewModel.wallet.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var maxSlippageAllowed by remember { mutableStateOf(0.05) }
    
    val biometricEnabled by viewModel.biometricQuickBetEnabled.collectAsState()
    var showBiometricAuthSim by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    
    val currentBalance = wallet?.balance ?: 0.0
    val selectedOdds = odds

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("bet_slip_workspace"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Bet Slip",
                            tint = PrimarySapphire,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STAKE BET RECEIPT",
                            fontSize = 11.s_p,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    IconButton(onClick = onDismissRequest, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextGrey)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Match details
                Text(
                     text = "${match.teamA} vs ${match.teamB}",
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     color = TextWhite
                )
                Text(
                     text = match.sport.uppercase(),
                     fontSize = 10.s_p,
                     color = TextMuted,
                     fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selection Box details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "SELECTED SELECTION", fontSize = 9.s_p, color = TextMuted)
                        Text(
                            text = "Option Outcome: $selection",
                            fontSize = 13.s_p,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "ODDS", fontSize = 9.s_p, color = TextMuted)
                        Text(
                            text = "@${String.format("%.2f", selectedOdds)}",
                            fontSize = 14.s_p,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Wallet indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Available Wallet Balance:", fontSize = 11.s_p, color = TextMuted)
                    Text(
                        text = "${String.format("%,.2f", currentBalance)} ETB",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Numeric stake input text field
                OutlinedTextField(
                    value = stakeInput,
                    onValueChange = {
                        errorMessage = null
                        if (it.isEmpty() || it.toDoubleOrNull() != null) stakeInput = it
                    },
                    modifier = Modifier.fillMaxWidth().testTag("stake_input_field"),
                    label = { Text("STAKE VALUE (ETB)", fontSize = 10.s_p, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = PrimarySapphire,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = SlateSurfaceL2,
                        unfocusedContainerColor = SlateSurfaceL2
                    ),
                    singleLine = true
                )
                
                // Rapid amount chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf("100", "500", "1000", "5000")
                    presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(SlateSurfaceL2, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    errorMessage = null
                                    stakeInput = preset
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "+$preset", fontSize = 11.s_p, color = TextLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Subtle Low Balance indicator/warning
                val minRequiredStake = 50.0
                val enteredStake = stakeInput.toDoubleOrNull() ?: minRequiredStake
                val hasLowBalance = currentBalance < enteredStake || currentBalance < minRequiredStake
                if (hasLowBalance) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEAB308).copy(alpha = 0.12f))
                            .border(BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.32f)), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Balance Caution",
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(14.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ LOW WALLET FUNDS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEAB308),
                                letterSpacing = 0.4.sp
                            )
                            Text(
                                text = "Funds (${String.format("%,.2f", currentBalance)} ETB) are below required ${String.format("%,.0f", enteredStake)} ETB.",
                                fontSize = 9.sp,
                                color = TextMuted,
                                lineHeight = 12.sp
                            )
                        }
                        TextButton(
                            onClick = { showDepositDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text(
                                text = "TOP UP",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEAB308)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SLIPPAGE TOLERANCE",
                    fontSize = 10.s_p,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val slippageOptions = listOf(0.01, 0.03, 0.05, 0.10)
                    slippageOptions.forEach { opt ->
                        val isSelected = maxSlippageAllowed == opt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) PrimarySapphire.copy(alpha = 0.25f) else SlateSurfaceL2,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) PrimarySapphire else BorderColor,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    errorMessage = null
                                    maxSlippageAllowed = opt
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(opt * 100).toInt()}%",
                                fontSize = 11.s_p,
                                color = if (isSelected) TextWhite else TextLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.setBiometricQuickBetEnabled(!biometricEnabled)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Quick Bet TouchID",
                            tint = if (biometricEnabled) NeonGreen else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Frictionless Biometric Quick-Bet",
                            fontSize = 11.s_p,
                            fontWeight = FontWeight.Bold,
                            color = if (biometricEnabled) TextWhite else TextMuted
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { viewModel.setBiometricQuickBetEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = SlateSurfaceL2.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .testTag("slip_biometric_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Profit returns calculation
                val stakeDbl = stakeInput.toDoubleOrNull() ?: 0.0
                val estReturn = stakeDbl * selectedOdds
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "POTENTIAL RETURN", fontSize = 11.s_p, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${String.format("%,.2f", estReturn)} ETB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen
                    )
                }
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = LightRed,
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Submit Placement
                Button(
                    onClick = {
                        val finalStake = stakeInput.toDoubleOrNull() ?: 0.0
                        if (finalStake <= 0.0) {
                            errorMessage = "Please enter a valid stake amount."
                            return@Button
                        }
                        if (finalStake > currentBalance) {
                            errorMessage = "Insufficient Balance. Please top up funds."
                            return@Button
                        }
                        
                        if (biometricEnabled) {
                            showBiometricAuthSim = true
                        } else {
                            viewModel.placeBet(
                                matchId = match.id,
                                selection = selection,
                                odds = selectedOdds,
                                stake = finalStake,
                                maxSlippageAllowed = maxSlippageAllowed,
                                onSuccess = {
                                    onBetPlacedSuccess()
                                },
                                onError = { err ->
                                    errorMessage = err
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("place_bet_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySapphire, contentColor = TextWhite),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (biometricEnabled) "TOUCH TO PLACE QUICK BET" else "PLACE SECURED SINGLE BET",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.s_p,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.toggleSlipSelection(match, selection, selectedOdds)
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("add_to_accumulator_btn"),
                    border = BorderStroke(1.dp, AmberAccent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "ADD TO ACCUMULATOR TICKET",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.s_p,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (showBiometricAuthSim) {
        val finalStake = stakeInput.toDoubleOrNull() ?: 100.0
        BiometricPromptSimulated(
            stakeAmount = finalStake,
            onAuthSuccess = {
                showBiometricAuthSim = false
                viewModel.placeBet(
                    matchId = match.id,
                    selection = selection,
                    odds = selectedOdds,
                    stake = finalStake,
                    maxSlippageAllowed = maxSlippageAllowed,
                    onSuccess = {
                        onBetPlacedSuccess()
                    },
                    onError = { err ->
                        errorMessage = err
                    }
                )
            },
            onDismissRequest = {
                showBiometricAuthSim = false
            }
        )
    }

    if (showDepositDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = { showDepositDialog = false }
        )
    }
}

@Composable
fun SidebarLiveBetSlip(
    viewModel: com.example.viewmodel.BetViewModel,
    modifier: Modifier = Modifier
) {
    val activeItems by viewModel.activeSlipSelectedItems.collectAsState()
    val wallet by viewModel.wallet.collectAsState()
    val currentBalance = wallet?.balance ?: 0.0

    var stakeInput by remember { mutableStateOf("") }
    var txMessageText by remember { mutableStateOf("") }
    var txMessageType by remember { mutableStateOf("") } // "success" or "error"
    var isSubmitting by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showConfirmWagerDialog by remember { mutableStateOf(false) }

    val totalOdds = activeItems.fold(1.0) { acc, item -> acc * item.odds }
    val computedWin = if (stakeInput.isNotEmpty()) {
        (stakeInput.toDoubleOrNull() ?: 0.0) * totalOdds
    } else {
        0.0
    }

    // Reset feedback on item change
    LaunchedEffect(activeItems.size) {
        txMessageText = ""
        txMessageType = ""
    }

    Card(
        modifier = modifier.fillMaxWidth().testTag("sidebar_bet_slip_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = PrimarySapphire,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE BET SLIP",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                }
                if (activeItems.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimarySapphire.copy(0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (activeItems.size == 1) "Single" else "Accumulator (${activeItems.size})",
                            fontSize = 8.s_p,
                            color = PrimarySapphire,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Divider(color = BorderColor.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (activeItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Your bet slip is empty. Click on any shifting live odds on the left to initialize a wager ticket.",
                        fontSize = 10.s_p,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.s_p,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // List of leg items
                    activeItems.forEach { leg ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${leg.teamA} vs ${leg.teamB}",
                                    fontSize = 11.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.removeSelectionFromSlip(leg.matchId) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove selection",
                                        tint = LightRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Market Result Pick: " + leg.selection,
                                    fontSize = 10.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimarySapphire
                                )
                                Text(
                                    text = "@" + String.format("%.2f", leg.odds),
                                    fontSize = 12.s_p,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stake numeric entry field
                    Column {
                        Text(
                            text = "STAKE VALUE AMOUNT (ETB)",
                            fontSize = 9.s_p,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = stakeInput,
                            onValueChange = {
                                txMessageText = ""
                                txMessageType = ""
                                if (it.isEmpty() || it.toDoubleOrNull() != null) stakeInput = it
                            },
                            modifier = Modifier.fillMaxWidth().testTag("sidebar_stake_input"),
                            placeholder = { Text("Enter amount", fontSize = 11.s_p, color = TextGrey) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextLight,
                                focusedBorderColor = PrimarySapphire,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = SlateSurfaceL2,
                                unfocusedContainerColor = SlateSurfaceL2
                            ),
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = "ETB",
                                    fontSize = 10.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        )
                        
                        // Subtle Low Balance indicator/warning
                        val minRequiredStake = 50.0
                        val enteredStake = stakeInput.toDoubleOrNull() ?: minRequiredStake
                        val hasLowBalance = currentBalance < enteredStake || currentBalance < minRequiredStake
                        if (hasLowBalance) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEAB308).copy(alpha = 0.12f))
                                    .border(BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.32f)), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Low Balance Caution",
                                    tint = Color(0xFFEAB308),
                                    modifier = Modifier.size(14.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "⚠️ LOW WALLET FUNDS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEAB308),
                                        letterSpacing = 0.4.sp
                                    )
                                    Text(
                                        text = "Funds (${String.format("%,.2f", currentBalance)} ETB) are below required ${String.format("%,.0f", enteredStake)} ETB.",
                                        fontSize = 9.sp,
                                        color = TextMuted,
                                        lineHeight = 12.sp
                                    )
                                }
                                TextButton(
                                    onClick = { showDepositDialog = true },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    Text(
                                        text = "TOP UP",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEAB308)
                                    )
                                }
                            }
                        }
                    }

                    // Ledger pricing calculations
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceL2, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Odds Line", fontSize = 11.s_p, color = TextMuted)
                            Text(
                                text = "@" + String.format("%.2f", totalOdds),
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Gross Estimation Return", fontSize = 11.s_p, color = TextMuted)
                            Text(
                                text = String.format("%,.2f", computedWin) + " ETB",
                                fontSize = 12.s_p,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }

                    // Place Wager button
                    val hasStake = stakeInput.isNotEmpty() && (stakeInput.toDoubleOrNull() ?: 0.0) > 0.0
                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            val finalStake = stakeInput.toDoubleOrNull() ?: 0.0
                            if (finalStake > currentBalance) {
                                txMessageType = "error"
                                txMessageText = "Insufficient balance for this stake."
                                return@Button
                            }
                            showConfirmWagerDialog = true
                        },
                        enabled = hasStake && !isSubmitting,
                        modifier = Modifier.fillMaxWidth().testTag("sidebar_place_wager_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEAB308),
                            contentColor = Color.Black,
                            disabledContainerColor = SlateSurfaceL2,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isSubmitting) "Locking Ticket Slip..." else "Place Multi-Market Wager",
                            fontSize = 11.s_p,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Notification/Alert box
                    if (txMessageText.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (txMessageType == "success") NeonGreen.copy(0.12f)
                                    else LightRed.copy(0.12f)
                                )
                                .border(
                                    1.dp,
                                    if (txMessageType == "success") NeonGreen.copy(0.35f)
                                    else LightRed.copy(0.35f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (txMessageType == "success") Icons.Default.CheckCircle
                                              else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (txMessageType == "success") NeonGreen else LightRed,
                                modifier = Modifier.size(15.dp).padding(top = 1.dp)
                            )
                            Text(
                                text = txMessageText,
                                fontSize = 10.s_p,
                                fontWeight = FontWeight.Bold,
                                color = if (txMessageType == "success") NeonGreen else LightRed,
                                lineHeight = 13.s_p
                            )
                        }
                    }
                }
            }
        }
    }
    if (showDepositDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = { showDepositDialog = false }
        )
    }

    if (showConfirmWagerDialog) {
        Dialog(onDismissRequest = { showConfirmWagerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("bet_confirmation_dialog"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONFIRM WAGER TICKET",
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                        IconButton(
                            onClick = { showConfirmWagerDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextGrey)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Wager Selection Summary:",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Legends list inside confirmation
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(weight = 1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        activeItems.forEach { leg ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "${leg.teamA} vs ${leg.teamB}",
                                    fontSize = 11.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Selection: ${leg.selection}",
                                        fontSize = 10.s_p,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimarySapphire
                                    )
                                    Text(
                                        text = "@" + String.format("%.2f", leg.odds),
                                        fontSize = 11.s_p,
                                        fontWeight = FontWeight.Black,
                                        color = AmberAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Ledger pricing calculations
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Combined Total Odds", fontSize = 11.s_p, color = TextMuted)
                            Text(
                                text = "@" + String.format("%.2f", totalOdds),
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Risk Stake", fontSize = 11.s_p, color = TextMuted)
                            Text(
                                text = String.format("%,.2f", stakeInput.toDoubleOrNull() ?: 0.0) + " ETB",
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Divider(color = BorderColor.copy(alpha = 0.3f), thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Gross Potential Returns", fontSize = 11.s_p, color = TextMuted)
                            Text(
                                text = String.format("%,.2f", computedWin) + " ETB",
                                fontSize = 12.s_p,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showConfirmWagerDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_bet_btn"),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                showConfirmWagerDialog = false
                                if (isSubmitting) return@Button
                                val finalStake = stakeInput.toDoubleOrNull() ?: 0.0
                                if (finalStake > currentBalance) {
                                    txMessageType = "error"
                                    txMessageText = "Insufficient balance for this stake."
                                    return@Button
                                }

                                isSubmitting = true
                                txMessageText = ""
                                txMessageType = ""

                                if (activeItems.size == 1) {
                                    val singleLeg = activeItems.first()
                                    viewModel.placeBet(
                                        matchId = singleLeg.matchId,
                                        selection = singleLeg.selection,
                                        odds = singleLeg.odds,
                                        stake = finalStake,
                                        maxSlippageAllowed = 0.05,
                                        onSuccess = {
                                            isSubmitting = false
                                            txMessageType = "success"
                                            txMessageText = "Bet Accepted successfully! Slip ID: #SGP" + (1000 + (Math.random() * 9000).toInt())
                                            viewModel.clearSlip()
                                            stakeInput = ""
                                        },
                                        onError = { err ->
                                            isSubmitting = false
                                            txMessageType = "error"
                                            txMessageText = err
                                        }
                                    )
                                } else {
                                    viewModel.placeMultiLegBet(
                                        stake = finalStake,
                                        onSuccess = {
                                            isSubmitting = false
                                            txMessageType = "success"
                                            txMessageText = "Bet Accepted successfully! Slip ID: #TICKET" + (1000 + (Math.random() * 9000).toInt())
                                            viewModel.clearSlip()
                                            stakeInput = ""
                                        },
                                        onError = { err ->
                                            isSubmitting = false
                                            txMessageType = "error"
                                            txMessageText = err
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("confirm_finalize_bet_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEAB308),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Lock Wager",
                                fontSize = 11.s_p,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceMultiBetSlipDialog(
    viewModel: com.example.viewmodel.BetViewModel,
    onDismissRequest: () -> Unit,
    onBetPlacedSuccess: () -> Unit
) {
    var stakeInput by remember { mutableStateOf("100") }
    val wallet by viewModel.wallet.collectAsState()
    val activeItems by viewModel.activeSlipSelectedItems.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDepositDialog by remember { mutableStateOf(false) }

    val currentBalance = wallet?.balance ?: 0.0
    val totalOdds = activeItems.fold(1.0) { acc, item -> acc * item.odds }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val coroutineScope = rememberCoroutineScope()
    fun handleDismiss() {
        coroutineScope.launch {
            isVisible = false
            delay(250) // Duration of slide/fade exit
            onDismissRequest()
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { handleDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        // Entire Dialog Canvas
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f * (if (isVisible) 1f else 0f))) // Fade transition for scrim background
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { handleDismiss() }
        ) {
            val isTablet = maxWidth >= 600.dp
            
            // Drawer Panel Box
            AnimatedVisibility(
                visible = isVisible,
                enter = if (isTablet) {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250))
                } else {
                    slideInVertically(initialOffsetY = { it }, animationSpec = tween(250)) + fadeIn(animationSpec = tween(250))
                },
                exit = if (isTablet) {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                } else {
                    slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                },
                modifier = if (isTablet) {
                    Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(enabled = true, onClick = {}, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) // Prevent clicks from going through scrim
                        .testTag("multi_bet_slip_workspace"),
                    shape = if (isTablet) {
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    } else {
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    },
                    colors = CardDefaults.cardColors(containerColor = SlateDarkBG),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // Drag Handle / Indicator for mobile drawer
                        if (!isTablet) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(BorderColor, RoundedCornerShape(2.dp))
                                    .align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Header Block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Bet Slip",
                                    tint = PrimarySapphire,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACCUMULATOR TICKET",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite,
                                    letterSpacing = 1.sp
                                )
                            }

                            IconButton(
                                onClick = { handleDismiss() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SlateSurfaceL2, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected match legs
                        Text(
                            text = "SELECTION TRACKER (${activeItems.size} ${if (activeItems.size == 1) "LEG" else "LEGS"})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = TextMuted,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // allow list to take available space
                        ) {
                            if (activeItems.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LayersClear,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Your accumulator contains no selections. Tap on some game matches live odds to begin.",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(activeItems) { leg ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SlateSurfaceL2, RoundedCornerShape(10.dp))
                                                .border(1.dp, BorderColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${leg.teamA} vs ${leg.teamB}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = TextWhite,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${leg.sport.uppercase()} • Market: ${leg.marketType}",
                                                        fontSize = 9.sp,
                                                        color = TextMuted
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Pick: ${leg.selection}",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = AmberAccent
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "@${String.format("%.2f", leg.odds)}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = NeonGreen
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = { viewModel.removeSelectionFromSlip(leg.matchId) },
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(SlateCardBG, CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Remove",
                                                        tint = LightRed,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderColor.copy(alpha = 0.5f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Pricing Calculations & Inputs
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Total Aggregate Odds & Balance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMBINED ACCUMULATOR ODDS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "@${String.format("%.2f", totalOdds)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AmberAccent
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Available Balance",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${String.format("%,.2f", currentBalance)} ETB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextWhite
                                )
                            }

                            // Stake Input Field
                            OutlinedTextField(
                                value = stakeInput,
                                onValueChange = {
                                    errorMessage = null
                                    if (it.isEmpty() || it.toDoubleOrNull() != null) stakeInput = it
                                },
                                modifier = Modifier.fillMaxWidth().testTag("multi_stake_input_field"),
                                label = { Text("STAKE AMOUNT (ETB)", fontSize = 9.sp, fontWeight = FontWeight.Black, color = TextMuted, letterSpacing = 0.5.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextLight,
                                    focusedBorderColor = PrimarySapphire,
                                    unfocusedBorderColor = BorderColor,
                                    focusedContainerColor = SlateSurfaceL2,
                                    unfocusedContainerColor = SlateSurfaceL2
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            // Quick Amount Preset Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf("100", "500", "1000", "5000")
                                presets.forEach { preset ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(SlateSurfaceL2, RoundedCornerShape(6.dp))
                                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                            .clickable {
                                                errorMessage = null
                                                stakeInput = preset
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+$preset",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                            
                            // Subtle Low Balance indicator/warning
                            val minRequiredStake = 50.0
                            val enteredStake = stakeInput.toDoubleOrNull() ?: minRequiredStake
                            val hasLowBalance = currentBalance < enteredStake || currentBalance < minRequiredStake
                            if (hasLowBalance) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEAB308).copy(alpha = 0.12f))
                                        .border(BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.32f)), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Low Balance Caution",
                                        tint = Color(0xFFEAB308),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "⚠️ LOW WALLET FUNDS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFEAB308),
                                            letterSpacing = 0.4.sp
                                        )
                                        Text(
                                            text = "Funds (${String.format("%,.2f", currentBalance)} ETB) are below required ${String.format("%,.0f", enteredStake)} ETB.",
                                            fontSize = 9.5.sp,
                                            color = TextMuted,
                                            lineHeight = 13.sp
                                        )
                                    }
                                    TextButton(
                                        onClick = { showDepositDialog = true },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                        modifier = Modifier.height(22.dp)
                                    ) {
                                        Text(
                                            text = "TOP UP",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFEAB308)
                                        )
                                    }
                                }
                            }

                            // Potential Winnings
                            val stakeDbl = stakeInput.toDoubleOrNull() ?: 0.0
                            val estReturn = stakeDbl * totalOdds

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ESTIMATED PAYOUT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${String.format("%,.2f", estReturn)} ETB",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }

                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = LightRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Place Bet submit button
                            Button(
                                onClick = {
                                    val finalStake = stakeInput.toDoubleOrNull() ?: 0.0
                                    if (finalStake <= 0.0) {
                                        errorMessage = "Please enter a valid stake amount."
                                        return@Button
                                    }
                                    if (finalStake > currentBalance) {
                                        errorMessage = "Insufficient Balance. Please top up funds."
                                        return@Button
                                    }
                                    if (activeItems.isEmpty()) {
                                        errorMessage = "Your slip contains no active selections."
                                        return@Button
                                    }

                                    viewModel.placeMultiBet(
                                        stake = finalStake,
                                        onSuccess = {
                                            onBetPlacedSuccess()
                                        },
                                        onError = { err ->
                                            errorMessage = err
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("place_multi_bet_submit_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEAB308),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "LOCK TICKET & PLACE BET",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDepositDialog) {
        DepositFundsDialog(
            viewModel = viewModel,
            onDismissRequest = { showDepositDialog = false }
        )
    }
}

// ==========================================
// CENTRALIZED EMPTY STATE VIEW
// ==========================================

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(
                text = message,
                fontSize = 12.s_p,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==========================================
// REALTIME SECURE ADMIN PANEL SCREEN
// ==========================================

data class RiskAlert(
    val id: String,
    val timestamp: String,
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val title: String,
    val matchId: String,
    val marketName: String,
    val totalPool: Double,
    val currentExposure: Double,
    val reason: String,
    val status: String, // "ACTIVE_MONITOR", "SUSPENDED", "CLEARED", "BLOCKED", "APPROVED"
    val poolDistribution: List<Float>
)

data class AuditLogEntry(
    val id: Long,
    val adminId: Int,
    val actionType: String,
    val targetEntityId: String,
    val previousState: String,
    val updatedState: String,
    val ipOrigin: String,
    val executionTime: String
)

@Composable
fun AdminDashboardScreen(
    viewModel: BetViewModel
) {
    val wallet by viewModel.wallet.collectAsState()
    val allBets by viewModel.allBets.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var expandedSimTrxId by remember { mutableStateOf<String?>(null) }
    var simWebhookResult by remember { mutableStateOf<String?>(null) }
    var feedUrlInput by remember { mutableStateOf("ws://127.0.0.1:9090") }
    var apiKeyInput by remember { mutableStateOf("shebaodds_api_demo") }

    // Base mock stats matching specified requirements
    val baseUsers = 12458
    val baseBalance = 1257850.00
    val baseBetsToday = 8564
    val baseDeposits = 523600.00
    val baseWithdrawals = 186250.00

    // Stateful live computation based on Room DB updates!
    val currentBalanceChange = if (wallet != null) wallet!!.balance - 523600.00 else 0.0
    val displayBalanceRaw = baseBalance + currentBalanceChange
    val displayBetsToday = baseBetsToday + allBets.size
    
    val dynamicDeposits = allTransactions.filter { it.type == "DEPOSIT" && it.status == "APPROVED" }.sumOf { it.amount }
    val displayDepositsRaw = baseDeposits + dynamicDeposits

    val dynamicWithdrawals = allTransactions.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
    val displayWithdrawalsRaw = baseWithdrawals + dynamicWithdrawals

    val liveMetrics by com.example.util.AdminSocketHubInstance.financialMetrics.collectAsState()
    
    val displayBalance = liveMetrics.totalBalance.toDoubleOrNull() ?: displayBalanceRaw
    val displayDeposits = liveMetrics.totalDeposits.toDoubleOrNull() ?: displayDepositsRaw
    val displayWithdrawals = liveMetrics.totalWithdrawals.toDoubleOrNull() ?: displayWithdrawalsRaw

    // KPI Custom Palette Colors
    val NeonBlue = Color(0xFF00C2FF)
    val NeonPurple = Color(0xFFBD00FF)
    val OrangeAccent = Color(0xFFFF6B00)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBG),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // TOP HEADER DESCRIPTION INFO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏆 SHEBAODDS ADMIN",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberAccent,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SlateSurfaceL2)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE SYNCED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Realtime centralized oversight of all placed tickets, secure bank wallets, balances, and deposits.",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }

        // 5 COLUMN KPI METRICS SECTOR
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REALTIME FINANCIAL & USER KPIS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                
                // Beautifully designed dynamic metrics list
                val kpiList = listOf(
                    KpiData("TOTAL USERS", String.format("%,d", baseUsers), "+5.4%", true, Icons.Default.People, NeonBlue),
                    KpiData("TOTAL BALANCE", String.format("%,.2f ETB", displayBalance), "+8.7%", true, Icons.Default.AccountBalanceWallet, NeonGreen),
                    KpiData("TOTAL BETS TODAY", String.format("%,d", displayBetsToday), "+12.5%", true, Icons.Default.EmojiEvents, NeonPurple),
                    KpiData("TOTAL DEPOSITS", String.format("%,.2f ETB", displayDeposits), "+7.2%", true, Icons.Default.ArrowDownward, OrangeAccent),
                    KpiData("TOTAL WITHDRAWALS", String.format("%,.2f ETB", displayWithdrawals), "-3.1%", false, Icons.Default.ArrowUpward, LightRed)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(kpiList) { kpi ->
                        Card(
                            modifier = Modifier
                                .width(175.dp)
                                .height(105.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = kpi.title,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = kpi.icon,
                                        contentDescription = null,
                                        tint = kpi.color,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = kpi.value,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (kpi.up) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                            contentDescription = null,
                                            tint = if (kpi.up) NeonGreen else LightRed,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = kpi.change,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (kpi.up) NeonGreen else LightRed
                                        )
                                        Text(
                                            text = "from yesterday",
                                            fontSize = 8.sp,
                                            color = TextGrey
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 📡 REAL-TIME WEBSOCKET ADMIN HUB CHANNEL
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_websocket_hub_card"),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, NeonBlue.copy(0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📡 WEBSOCKET BROADCASTER HUB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonGreen.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "adminSocketHubInstance.broadcastNewBet()",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Real-time stream reflecting the underlying Express web-socket event triggers. Whenever tickets bypass validation and clear database COMMIT steps, events emit to active supervisor clients asynchronously.",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    var preferredBroadcasterTab by remember { mutableStateOf("BETS") } // "BETS", "FINANCE", "CLIENT", "JWT", "RISK", "AUDIT"
                    var showBettingEngineSnippet by remember { mutableStateOf(false) }
                    var jwtHeaderInput by remember { mutableStateOf("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJzdXBlcl9hZG1pbiIsInJvbGUiOiJzdXBlcl9hZG1pbiJ9.verified_signature") }
                    var jwtStatusLog by remember { mutableStateOf("Ready Status: Under active Express.js middleware protection.") }
                    var isJwtVerified by remember { mutableStateOf(true) }
                    var requiredClearance by remember { mutableStateOf("super_admin") } // "user", "admin", "super_admin"
                    var selectedExpressRoute by remember { mutableStateOf("GET /matches/live-odds") }

                    var riskStake by remember { mutableStateOf("45000.00") }
                    var riskOdds by remember { mutableStateOf("12.5") }
                    var riskMarketPool by remember { mutableStateOf("1850000.00") }
                    var lastRiskBetTime by remember { mutableStateOf(0L) }
                    var riskStatusLog by remember { mutableStateOf("Ready Status: Under active RiskManagementService surveillance.") }
                    var isRiskApproved by remember { mutableStateOf(true) }
                    var maxPayoutLimit by remember { mutableStateOf(500000.00) }
                    var antiSpamWindowMs by remember { mutableStateOf(5000L) }
                    var hasGlobalLimitsOverrideApplied by remember { mutableStateOf(false) }

                    var inspectedRiskAlertId by remember { mutableStateOf<String?>(null) }
                    var riskAlerts by remember {
                        mutableStateOf(
                            listOf(
                                RiskAlert(
                                    id = "ALT-8921",
                                    timestamp = "07:44:12",
                                    severity = "CRITICAL",
                                    title = "Syndicate Pattern Blocked",
                                    matchId = "482",
                                    marketName = "Ethiopian Coffee vs St. George",
                                    totalPool = 3450000.0,
                                    currentExposure = 850000.0,
                                    reason = "14 distinct high-stake wagers placed within 45 seconds from identical subnet.",
                                    status = "ACTIVE_MONITOR",
                                    poolDistribution = listOf(0.65f, 0.15f, 0.20f)
                                ),
                                RiskAlert(
                                    id = "ALT-5012",
                                    timestamp = "07:42:01",
                                    severity = "WARNING",
                                    title = "Exposure Limit Warning",
                                    matchId = "501",
                                    marketName = "Bahir Dar Kenema vs Hawassa Kenema",
                                    totalPool = 2100000.0,
                                    currentExposure = 420000.0,
                                    reason = "Single payout potential (550,000 ETB) exceeds 500k ETB platform cap.",
                                    status = "ACTIVE_MONITOR",
                                    poolDistribution = listOf(0.75f, 0.25f)
                                ),
                                RiskAlert(
                                    id = "ALT-3104",
                                    timestamp = "07:38:50",
                                    severity = "CRITICAL",
                                    title = "Rate Limit Spam Flagged",
                                    matchId = "311",
                                    marketName = "Fasil Kenema vs Adama City",
                                    totalPool = 1800000.0,
                                    currentExposure = 780000.0,
                                    reason = "Multiple high-frequency bot wagers submitted in < 5s window.",
                                    status = "SUSPENDED",
                                    poolDistribution = listOf(0.40f, 0.35f, 0.25f)
                                ),
                                RiskAlert(
                                    id = "ALT-9527",
                                    timestamp = "07:30:15",
                                    severity = "WARNING",
                                    title = "Odd-Shift Discrepancy",
                                    matchId = "527",
                                    marketName = "Hambericho Durame vs Shashemene City",
                                    totalPool = 2300000.0,
                                    currentExposure = 512000.0,
                                    reason = "Suspicious odd-shifting volume discrepancy aligned with syndicated betting pattern.",
                                    status = "ACTIVE_MONITOR",
                                    poolDistribution = listOf(0.55f, 0.45f)
                                )
                            )
                        )
                    }

                    var auditAdminId by remember { mutableStateOf("1") }
                    var auditAction by remember { mutableStateOf("MANUAL_BALANCE_CREDIT") }
                    var auditTargetId by remember { mutableStateOf("wallet_user_482") }
                    var auditOldValue by remember { mutableStateOf("{\"balance\": 500.00}") }
                    var auditNewValue by remember { mutableStateOf("{\"balance\": 5500.00}") }
                    var auditIpAddress by remember { mutableStateOf("192.168.1.104") }
                    var auditConsoleLog by remember { mutableStateOf("Ready Status: Under pg-AuditLogService supervision. Operations produce unalterable logs inside system_audit_logs tables.") }
                    var auditLoggedHistory by remember {
                        mutableStateOf(
                            listOf(
                                "[AUDIT SECURE] ACTION: 'MATCH_SUSPENDED' on 'match_8391' logged (IP: 192.168.1.99)\n  └ Old: '{\"active\": true}'\n  └ New: '{\"active\": false}'",
                                "[AUDIT SECURE] ACTION: 'MANUAL_BALANCE_CREDIT' on 'wallet_103' logged (IP: 10.0.0.4)\n  └ Old: '{\"balance\": 1500.00}'\n  └ New: '{\"balance\": 25000.00}'"
                            )
                        )
                    }
                    var showSchemaDdl by remember { mutableStateOf(false) }
                    var showAuditServiceCode by remember { mutableStateOf(false) }
                    var auditTableEntries by remember {
                        mutableStateOf(
                            listOf(
                                AuditLogEntry(
                                    id = 1004L,
                                    adminId = 3,
                                    actionType = "MATCH_SUSPENDED",
                                    targetEntityId = "match_8391",
                                    previousState = "{\"active\": true, \"scores\": \"0:0\"}",
                                    updatedState = "{\"active\": false, \"scores\": \"0:0\", \"suspended_by\": \"RiskHeuristics\"}",
                                    ipOrigin = "192.168.1.99",
                                    executionTime = "07:44:12"
                                ),
                                AuditLogEntry(
                                    id = 1003L,
                                    adminId = 1,
                                    actionType = "MANUAL_BALANCE_CREDIT",
                                    targetEntityId = "wallet_user_103",
                                    previousState = "{\"balance\": 1500.00}",
                                    updatedState = "{\"balance\": 25000.00, \"approved_by\": \"AuditOffice\"}",
                                    ipOrigin = "10.0.0.4",
                                    executionTime = "07:42:01"
                                ),
                                AuditLogEntry(
                                    id = 1002L,
                                    adminId = 4,
                                    actionType = "TICKET_VOID",
                                    targetEntityId = "ticket_90112",
                                    previousState = "{\"status\": \"Pending\", \"potential_payout\": 125000.0}",
                                    updatedState = "{\"status\": \"Voided_Manual\", \"potential_payout\": 0.0}",
                                    ipOrigin = "172.16.2.24",
                                    executionTime = "07:38:50"
                                ),
                                AuditLogEntry(
                                    id = 1001L,
                                    adminId = 1,
                                    actionType = "GATEWAY_UPDATE",
                                    targetEntityId = "telebirr_config",
                                    previousState = "{\"merchant_id\": \"921021\", \"api_status\": \"LIVE\"}",
                                    updatedState = "{\"merchant_id\": \"921021\", \"api_status\": \"MAINTENANCE\"}",
                                    ipOrigin = "192.168.1.104",
                                    executionTime = "07:30:15"
                                )
                            )
                        )
                    }
                    var searchAuditQuery by remember { mutableStateOf("") }
                    var filterAuditActionType by remember { mutableStateOf("ALL") }
                    var filterAuditAdminId by remember { mutableStateOf("ALL") }
                    var selectedAuditEntryId by remember { mutableStateOf<Long?>(null) }
                    var showEngineRiskCode by remember { mutableStateOf(false) }
                    var showRiskServiceCode by remember { mutableStateOf(false) }
                    var showJwtExpressTypes by remember { mutableStateOf(false) }
                    var showExpressMiddleware by remember { mutableStateOf(false) }
                    var showExpressRouterCode by remember { mutableStateOf(false) }
                    
                    var showNpmInstaller by remember { mutableStateOf(true) }
                    var npmInstallProgress by remember { mutableStateOf(0f) }
                    var isNpmInstalling by remember { mutableStateOf(false) }
                    var npmConsoleLog by remember { mutableStateOf<List<String>>(listOf("Ready Status: Dependency manager active in workspace root. Standing by...")) }
                    var installedPackages by remember { mutableStateOf(setOf<String>()) }

                     Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BETS", "FINANCE", "CLIENT", "JWT", "RISK", "AUDIT").forEach { tab ->
                            val isSelected = preferredBroadcasterTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeonBlue.copy(0.12f) else SlateSurfaceL2)
                                    .border(1.dp, if (isSelected) NeonBlue else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { preferredBroadcasterTab = tab }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when(tab) {
                                        "BETS" -> "🎯 BETS"
                                        "FINANCE" -> "💼 FINANCE"
                                        "CLIENT" -> "🔌 CLIENT HOOK"
                                        "JWT" -> "🔑 JWT"
                                        "RISK" -> "🛡️ RISK"
                                        else -> "📝 AUDIT"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NeonBlue else TextLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val socketBroadcasts by com.example.util.AdminSocketHubInstance.newBetBroadcasts.collectAsState()
                    val txBroadcasts by com.example.util.AdminSocketHubInstance.transactionBroadcasts.collectAsState()

                    if (preferredBroadcasterTab == "BETS") {
                        // Collapsible Betting Engine trigger block
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F1622))
                                .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                                .clickable { showBettingEngineSnippet = !showBettingEngineSnippet }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Code icon",
                                    tint = NeonBlue,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "bettingEngine.ts Websocket Broadcaster Trigger",
                                    color = TextWhite,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = if (showBettingEngineSnippet) "HIDE TRIGGER ▲" else "VIEW CONTROLLER ▼",
                                color = NeonBlue,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (showBettingEngineSnippet) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = """
                                        // Inside bettingEngine.ts after client.query('COMMIT');
                                        if (adminSocketHubInstance) {
                                          adminSocketHubInstance.broadcastNewBet({
                                            id: `#${'$'}{insertBet.rows[0].id}`,
                                            user: `User${'$'}{userId}`,
                                            match: `${'$'}{matchId} Context Details`,
                                            market: `${'$'}{marketId} - ${'$'}{selection}`,
                                            stake: parseFloat(stake).toFixed(2),
                                            possibleWin: (stake * liveOddsNum).toFixed(2)
                                          });
                                        }
                                    """.trimIndent(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = TextLight,
                                    lineHeight = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (socketBroadcasts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "STREAM IDLE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ready to receive live websocket events. Place a bet...",
                                        fontSize = 8.sp,
                                        color = TextGrey
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                    .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(socketBroadcasts) { broadcast ->
                                        val timeStr = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date(broadcast.timestamp))
                                        Text(
                                            text = "[$timeStr] 🎯 [NEW_BET]: ID ${broadcast.id} for ${broadcast.user}\n  └ Match: ${broadcast.match}\n  └ Market: ${broadcast.market} | Stake: ${broadcast.stake} ETB | Win: ${broadcast.possibleWin} ETB",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 8.5.sp,
                                            color = NeonBlue,
                                            lineHeight = 13.sp,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (preferredBroadcasterTab == "FINANCE") {
                        TelebirrFrameworkConsole(
                            txBroadcasts = txBroadcasts,
                            wallet = wallet,
                            allTransactions = allTransactions,
                            allBets = allBets
                        )
                    } else if (preferredBroadcasterTab == "CLIENT") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = NeonBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🔌 useAdminLiveStream client-side",
                                        color = NeonBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonBlue.copy(0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "REACT / SOCKET.IO",
                                        color = NeonBlue,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Text(
                                text = "Secure, real-time React hook integrating directly with the live transaction feed and wagering platform events.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF030508), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = """
                                        import { useEffect, useState } from 'react';
                                        import { io } from 'socket.io-client';

                                        export function useAdminLiveStream(adminJwtToken) {
                                          const [liveBetsList, setLiveBetsList] = useState([]);
                                          const [recentTransactions, setRecentTransactions] = useState([]);
                                          const [topBarMetrics, setTopBarMetrics] = useState(null);

                                          useEffect(() => {
                                            // Connect to the secure real-time notification engine gateway
                                            const socket = io(process.env.REACT_APP_BACKEND_WS_URL || "https://api.yourbetplatform.com", {
                                              auth: { token: adminJwtToken }
                                            });

                                            socket.on('connect', () => {
                                              console.log('📡 Connected directly to live transactional feed layer.');
                                            });

                                            // Intercept and prepended new active wagers to the dashboard view row list
                                            socket.on('NEW_BET_PLACED', (newBet) => {
                                              setLiveBetsList((prevBets) => [newBet, ...prevBets.slice(0, 4)]); // Keep top 5 latest active bets
                                            });

                                            // Intercept and prepend inbound transaction actions to the activity list tracker
                                            socket.on('NEW_TRANSACTION_ALERT', (newTx) => {
                                              setRecentTransactions((prevTx) => [newTx, ...prevTx.slice(0, 4)]);
                                            });

                                            // Intercept and rewrite financial balance matrix headers in real-time
                                            socket.on('METRICS_UPDATED', (updatedMetrics) => {
                                              setTopBarMetrics(updatedMetrics);
                                            });

                                            socket.on('connect_error', (err) => {
                                              console.error('WebSocket Stream Access Rejected:', err.message);
                                            });

                                            return () => {
                                              socket.disconnect();
                                            };
                                          }, [adminJwtToken]);

                                          return { liveBetsList, recentTransactions, topBarMetrics };
                                        }
                                    """.trimIndent(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = TextLight,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    } else if (preferredBroadcasterTab == "JWT") {
                        ExpressJwtTabPanel()
                    } else if (preferredBroadcasterTab == "RISK") {
                        // "RISK"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🛡️ pg-RiskManagementService",
                                    color = NeonBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isRiskApproved) NeonGreen.copy(0.12f) else AmberAccent.copy(0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isRiskApproved) "MONITORING ACTIVE" else "RISK BLOCKED",
                                        color = if (isRiskApproved) NeonGreen else AmberAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Evaluates exposure thresholds (500k ETB Max Return Limit), temporal frequency checks (5s anti-spam window), and syndicated betting pool limits.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive bettingEngine.ts riskAssessment check viewer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0C1322))
                                    .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                                    .clickable { showEngineRiskCode = !showEngineRiskCode }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "Code icon",
                                        tint = NeonBlue,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "bettingEngine.ts Wager Risk Pipeline Check",
                                        color = TextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (showEngineRiskCode) "HIDE TRIGGER ▲" else "VIEW CONTROLLER ▼",
                                    color = NeonBlue,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (showEngineRiskCode) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = """
                                            // Inside bettingEngine.ts prior to executing 'BEGIN' on the database client pool:
                                            const riskAssessment = await this.riskService.assessWagerRisk({
                                              userId,
                                              matchId,
                                              marketId,
                                              stake,
                                              odds: expectedOdds
                                            });

                                            if (!riskAssessment.approved) {
                                              return { success: false, message: riskAssessment.reason || "Wager rejected by security risk rules." };
                                            }

                                            // Proceed to database wallet deduction and placement if approved safely...
                                        """.trimIndent(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = TextLight,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive RiskManagementService.ts Core Rules expander
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0E1A1A))
                                    .border(0.5.dp, NeonGreen.copy(0.4f), RoundedCornerShape(4.dp))
                                    .clickable { showRiskServiceCode = !showRiskServiceCode }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield Icon",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RiskManagementService.ts (Postgres Rules Core)",
                                        color = TextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (showRiskServiceCode) "HIDE RULES ▲" else "VIEW SECURITY SERVICE ▼",
                                    color = NeonGreen,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (showRiskServiceCode) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = """
                                            import { Pool } from 'pg';

                                            interface IRiskCheckRequest {
                                              userId: number;
                                              matchId: number;
                                              marketId: number;
                                              stake: number;
                                              odds: number;
                                            }

                                            export class RiskManagementService {
                                              // Configurable limits per user tier
                                              private readonly globalMaxPayout = ${String.format(java.util.Locale.US, "%.2f", maxPayoutLimit)}; // Max single slip return potential
                                              private readonly consecutiveBetWindowMs = ${antiSpamWindowMs}; // Consecutive bet time interval

                                              constructor(private readonly dbPool: Pool) {}

                                              /**
                                               * Evaluates if a bet should be rejected or flagged by the risk department
                                               */
                                              public async assessWagerRisk(request: IRiskCheckRequest): Promise<{ approved: boolean; reason?: string }> {
                                                const { userId, matchId, stake, odds } = request;
                                                const potentialPayout = stake * odds;

                                                // 1. Exposure Limit Validation
                                                if (potentialPayout > this.globalMaxPayout) {
                                                  return { 
                                                    approved: false, 
                                                    reason: `Potential payout (${'$'}{potentialPayout} ETB) exceeds the platform threshold of ${'$'}{this.globalMaxPayout} ETB.` 
                                                  };
                                                }

                                                const client = await this.dbPool.connect();
                                                try {
                                                  // 2. Anti-Spam / High-Frequency Bot Detection
                                                  const recentBets = await client.query(`
                                                    SELECT created_at FROM bets 
                                                    WHERE user_id = $1 AND created_at > NOW() - INTERVAL '5 seconds'
                                                  `, [userId]);

                                                  if (recentBets.rows.length > 0) {
                                                    return { 
                                                      approved: false, 
                                                      reason: "Rate limit exceeded: Multiple wagers submitted too quickly. Potential bot pattern detected." 
                                                    };
                                                  }

                                                  // 3. Syndicated Betting Check (Heuristic tracking if massive volume hits one specific outcome)
                                                  const marketVolume = await client.query(`
                                                    SELECT SUM(stake) as total_pool FROM bets
                                                    WHERE match_id = $1 AND status = 'Pending'
                                                  `, [matchId]);

                                                  const poolTotal = parseFloat(marketVolume.rows[0].total_pool || '0');
                                                  if (poolTotal > 2000000.00 && stake > 50000.00) {
                                                    // Automatically flag but do not reject outright if it's a high-roller market
                                                    console.warn(`⚠️ [RISK ALERT] High liabilities building on Match #${'$'}{matchId}. Current pool: ${'$'}{poolTotal} ETB`);
                                                  }

                                                  return { approved: true };

                                                } finally {
                                                  client.release();
                                                }
                                              }
                                            }
                                        """.trimIndent(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = TextLight,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "1. CHOOSE RISK TEST PRESETS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val riskPresets = listOf(
                                "STANDARD" to "Passes all limits smoothly",
                                "OVER_LIMIT" to "Exceeds 500k ETB single limit",
                                "SYNDICATED" to "High pool + Large stake",
                                "SPAM" to "Wagers sent in < 5s delta"
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                riskPresets.forEach { (presetName, presetInfo) ->
                                    val isPresetSelected = when (presetName) {
                                        "STANDARD" -> riskStake == "45000.00" && riskOdds == "8.0" && riskMarketPool == "100000.00"
                                        "OVER_LIMIT" -> riskStake == "60000.00" && riskOdds == "10.0" && riskMarketPool == "500000.00"
                                        "SYNDICATED" -> riskStake == "55000.00" && riskOdds == "2.5" && riskMarketPool == "2200000.00"
                                        else -> false
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isPresetSelected) Color(0xFF0F1622) else SlateSurfaceL2)
                                            .border(0.5.dp, if (isPresetSelected) NeonBlue.copy(0.8f) else BorderColor.copy(0.5f), RoundedCornerShape(4.dp))
                                            .clickable {
                                                when (presetName) {
                                                    "STANDARD" -> {
                                                        riskStake = "45000.00"
                                                        riskOdds = "8.0"
                                                        riskMarketPool = "100000.00"
                                                        riskStatusLog = "Preset Loaded: Standard Wager. Prepared for pg-Risk check."
                                                    }
                                                    "OVER_LIMIT" -> {
                                                        riskStake = "60000.00"
                                                        riskOdds = "10.0"
                                                        riskMarketPool = "500000.00"
                                                        riskStatusLog = "Preset Loaded: Excessive single return potential limit (>500,000 ETB)."
                                                    }
                                                    "SYNDICATED" -> {
                                                        riskStake = "55000.00"
                                                        riskOdds = "2.5"
                                                        riskMarketPool = "2200000.00"
                                                        riskStatusLog = "Preset Loaded: High pool volume syndicated risk warning."
                                                    }
                                                    "SPAM" -> {
                                                        riskStatusLog = "Click \"Assess Wager Risk\" button twice within 5 seconds to trigger anti-spam lock rules."
                                                    }
                                                }
                                                isRiskApproved = true
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(NeonBlue.copy(0.15f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = presetName,
                                                color = NeonBlue,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = presetInfo,
                                            color = if (isPresetSelected) TextWhite else TextLight,
                                            fontSize = 9.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("STAKE (ETB)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = riskStake,
                                        onValueChange = { riskStake = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                Column(modifier = Modifier.weight(0.7f)) {
                                    Text("ODDS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = riskOdds,
                                        onValueChange = { riskOdds = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text("MARKET POOL (ETB)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = riskMarketPool,
                                        onValueChange = { riskMarketPool = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val stakeVal = riskStake.toDoubleOrNull() ?: 0.0
                                        val oddsVal = riskOdds.toDoubleOrNull() ?: 1.0
                                        val poolVal = riskMarketPool.toDoubleOrNull() ?: 0.0
                                        val currentTime = System.currentTimeMillis()
                                        
                                        val resLog = StringBuilder()
                                        resLog.append("🛡️ RiskManagementService.assessWagerRisk()\n")
                                        resLog.append("├ Eval: stake=$stakeVal | odds=$oddsVal | potentialPayout=${stakeVal * oddsVal} ETB\n\n")

                                        // 1. Exposure Limit Validation (500k global cap)
                                        val potentialPayout = stakeVal * oddsVal
                                        if (potentialPayout > maxPayoutLimit) {
                                            isRiskApproved = false
                                            resLog.append("❌ TRANSACTION BLOCKED!\n")
                                            resLog.append("└ Reason: Potential payout (${potentialPayout} ETB) exceeds the global platform threshold of 500000.00 ETB.\n")
                                            resLog.append("└ Response: HTTP 403 Forbidden [EXPOSURE_BREACH]")
                                            riskStatusLog = resLog.toString()

                                            val tStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                            val newAlert = RiskAlert(
                                                id = "ALT-${(1000..9999).random()}",
                                                timestamp = tStr,
                                                severity = "CRITICAL",
                                                title = "Exposure Breach Attempt",
                                                matchId = "999",
                                                marketName = "Simulated Match Winner [Custom]",
                                                totalPool = poolVal,
                                                currentExposure = potentialPayout,
                                                reason = "Potential payout (${potentialPayout} ETB) exceeds the platform threshold of 500000.00 ETB.",
                                                status = "BLOCKED",
                                                poolDistribution = listOf(0.70f, 0.30f)
                                            )
                                            riskAlerts = listOf(newAlert) + riskAlerts
                                            return@Button
                                        }

                                        // 2. Temporal Spam check (5 seconds anti-bot)
                                        val deltaMs = currentTime - lastRiskBetTime
                                        if (lastRiskBetTime > 0L && antiSpamWindowMs > 0L && deltaMs < antiSpamWindowMs) {
                                            isRiskApproved = false
                                            resLog.append("❌ TRANSACTION BLOCKED! [ANTI-SPAM ACTIVE]\n")
                                            resLog.append("└ Reason: Multiple wagers submitted too quickly ($deltaMs ms < 5000 ms). Bot check triggered.\n")
                                            resLog.append("└ Response: HTTP 429 Too Many Requests [SPAM]")
                                            riskStatusLog = resLog.toString()

                                            val tStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                            val newAlert = RiskAlert(
                                                id = "ALT-${(1000..9999).random()}",
                                                timestamp = tStr,
                                                severity = "CRITICAL",
                                                title = "Bot Rate Limit Breach",
                                                matchId = "999",
                                                marketName = "Simulated Match Winner [Custom]",
                                                totalPool = poolVal,
                                                currentExposure = potentialPayout,
                                                reason = "Multiple wagers submitted too quickly ($deltaMs ms < 5000 ms). Potential bot pattern detected.",
                                                status = "BLOCKED",
                                                poolDistribution = listOf(0.50f, 0.50f)
                                            )
                                            riskAlerts = listOf(newAlert) + riskAlerts
                                            return@Button
                                        }

                                        // 3. Syndicated check
                                        if (poolVal > 2000000.00 && stakeVal > 50000.00) {
                                            isRiskApproved = true
                                            lastRiskBetTime = currentTime
                                            resLog.append("⚠️ TRANSACTION APPRVD (WITH RISK DEPT ALERT)\n")
                                            resLog.append("└ Warning: High liabilities building on Match. Current pool: $poolVal ETB.\n")
                                            resLog.append("└ Response: HTTP 200 OK [SYS_WARN_TRIGGERED]")
                                            riskStatusLog = resLog.toString()

                                            val tStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                            val newAlert = RiskAlert(
                                                id = "ALT-${(1000..9999).random()}",
                                                timestamp = tStr,
                                                severity = "WARNING",
                                                title = "High Liability Syndicate",
                                                matchId = "999",
                                                marketName = "Simulated Match Winner [Custom]",
                                                totalPool = poolVal,
                                                currentExposure = potentialPayout,
                                                reason = "High pool liability ($poolVal ETB) flagged with heavy custom stake ($stakeVal ETB).",
                                                status = "ACTIVE_MONITOR",
                                                poolDistribution = listOf(0.80f, 0.20f)
                                            )
                                            riskAlerts = listOf(newAlert) + riskAlerts
                                            return@Button
                                        }

                                        // Normal approval
                                        isRiskApproved = true
                                        lastRiskBetTime = currentTime
                                        resLog.append("✅ TRANSACTION CLEAN & CLEARED!\n")
                                        resLog.append("└ Audit check passed. No threshold breaches detected.\n")
                                        resLog.append("└ Response: HTTP 200 OK [RISK_VERIFIED]")
                                        riskStatusLog = resLog.toString()

                                        val tStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                        val newAlert = RiskAlert(
                                            id = "ALT-${(1000..9999).random()}",
                                            timestamp = tStr,
                                            severity = "INFO",
                                            title = "Operational Footprint Verified",
                                            matchId = "999",
                                            marketName = "Simulated Match Winner [Custom]",
                                            totalPool = poolVal,
                                            currentExposure = potentialPayout,
                                            reason = "Normal clean wager assessment completed successfully. Thresholds conform strictly.",
                                            status = "APPROVED",
                                            poolDistribution = listOf(0.40f, 0.60f)
                                        )
                                        riskAlerts = listOf(newAlert) + riskAlerts
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Assess Wager Risk", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        riskStake = "45000.00"
                                        riskOdds = "12.5"
                                        riskMarketPool = "1850000.00"
                                        riskStatusLog = "Restored default configuration presets. Ready."
                                        isRiskApproved = true
                                        lastRiskBetTime = 0L
                                    },
                                    modifier = Modifier.weight(0.7f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Reset Spec", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "pg-RISK ANALYSIS PIPELINE ENGINE SYSTEM LOG:",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp)
                                    .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = riskStatusLog,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    color = if (isRiskApproved) NeonGreen else AmberAccent,
                                    lineHeight = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 📡 LIVE RISK ALERTS FEED (pg-RiskService)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4136),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "📡 LIVE RISK ALERTS FEED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                                ) {
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            hasGlobalLimitsOverrideApplied = true
                                            maxPayoutLimit = 999999999.00
                                            antiSpamWindowMs = 0L
                                            
                                            riskAlerts = riskAlerts.map { alert ->
                                                if (alert.status != "CLEARED") {
                                                    alert.copy(
                                                        status = "CLEARED",
                                                        reason = "Mitigated: Admin cleared all limits. Limit cap raised to ¥1B ETB."
                                                    )
                                                } else {
                                                    alert
                                                }
                                            }
                                            
                                            riskStatusLog = "🛠️ BATCH SECURITY LIMIT MITIGATION INITIATED!\n├ Raising Platform Exposure Limit: 500,000.00 ETB ➔ 999,999,999.00 ETB [CLEARED]\n├ Disabling Bot/Spam Rate-Limiter: 5s ➔ 0s [BYPASSED]\n├ Custom Stake thresholds: Expanded to Unlimited [APPROVED]\n├ Batch clearing all active threshold alerts...\n└ SUCCESS: All platform-wide exceeded limit exclusions resolved successfully!"
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = NeonGreen.copy(0.15f),
                                            contentColor = NeonGreen
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                                        modifier = androidx.compose.ui.Modifier.height(24.dp),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = if (hasGlobalLimitsOverrideApplied) "✅ LIMITS OVERRIDDEN" else "🛠️ FIX ALL LIMITS EXCEEDED",
                                            fontSize = 7.5.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }

                                    Box(
                                        modifier = androidx.compose.ui.Modifier
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                                            .background(Color(0xFFFF4136).copy(0.15f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = "${riskAlerts.filter { it.status != "CLEARED" }.size} ACTIVE ALERTS",
                                            color = Color(0xFFFF4136),
                                            fontSize = 7.5.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instantly inspect high-liability sports betting pools triggering syndicate algorithms, excessive payout cap violations (>500k ETB), or sub-5s bot temporal spam.",
                                fontSize = 9.sp,
                                color = TextMuted,
                                lineHeight = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (riskAlerts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF06090D), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No security alerts generated by RiskManagementService.", fontSize = 9.sp, color = TextMuted)
                                    }
                                } else {
                                    riskAlerts.forEach { alert ->
                                        val isSelected = inspectedRiskAlertId == alert.id
                                        
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(if (isSelected) Color(0xFF0D1424) else Color(0xFF04060A))
                                                .border(
                                                    0.5.dp, 
                                                    if (isSelected) NeonBlue else when(alert.severity) {
                                                        "CRITICAL" -> Color(0xFFFF4136).copy(0.4f)
                                                        "WARNING" -> AmberAccent.copy(0.4f)
                                                        else -> NeonGreen.copy(0.4f)
                                                    }, 
                                                    RoundedCornerShape(5.dp)
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                                            .background(
                                                                when(alert.status) {
                                                                    "SUSPENDED" -> Color(0xFFFF4136)
                                                                    "CLEARED" -> NeonGreen
                                                                    "BLOCKED" -> Color(0xFFFF851B)
                                                                    else -> if (alert.severity == "CRITICAL") Color(0xFFFF4136) else AmberAccent
                                                                }
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = alert.title,
                                                        color = TextWhite,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(Color(0xFF0A1118))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = alert.id,
                                                            color = TextLight,
                                                            fontSize = 7.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(
                                                            when(alert.severity) {
                                                                "CRITICAL" -> Color(0xFFFF4136).copy(0.12f)
                                                                "WARNING" -> AmberAccent.copy(0.12f)
                                                                else -> NeonGreen.copy(0.12f)
                                                            }
                                                        )
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = alert.severity,
                                                        color = when(alert.severity) {
                                                            "CRITICAL" -> Color(0xFFFF4136)
                                                            "WARNING" -> AmberAccent
                                                            else -> NeonGreen
                                                        },
                                                        fontSize = 6.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Matched Event: ${alert.marketName} (ID: #${alert.matchId})",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 8.5.sp,
                                                color = TextLight
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "├ Reason: ${alert.reason}",
                                                fontSize = 8.sp,
                                                color = TextMuted,
                                                lineHeight = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "└ Financial Pool Breakdown: Pool: ${String.format("%,.0f", alert.totalPool)} ETB | Max payout exposure: ${String.format("%,.0f", alert.currentExposure)} ETB",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 7.5.sp,
                                                color = NeonBlue
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("STATUS: ", fontSize = 7.5.sp, color = TextMuted)
                                                    Text(
                                                        text = alert.status,
                                                        color = when(alert.status) {
                                                            "SUSPENDED" -> Color(0xFFFF4136)
                                                            "CLEARED" -> NeonGreen
                                                            "BLOCKED" -> Color(0xFFFF851B)
                                                            "APPROVED" -> NeonGreen
                                                            else -> AmberAccent
                                                        },
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isSelected) NeonBlue else SlateSurfaceL2)
                                                        .border(0.5.dp, if (isSelected) Color.White else BorderColor, RoundedCornerShape(4.dp))
                                                        .clickable { 
                                                            inspectedRiskAlertId = if (isSelected) null else alert.id 
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = if (isSelected) "Collapse Inspection ▲" else "🔍 Inspect Liability ▼",
                                                        color = if (isSelected) Color.Black else TextWhite,
                                                        fontSize = 7.5.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (isSelected) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Divider(color = BorderColor.copy(0.5f), thickness = 0.5.dp)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Text(
                                                    text = "🛡️ REAL-TIME LIABILITY EXPOSURE & HEAT DETECTOR",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonBlue,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Text(
                                                    text = "Outcome Vol distribution across pool coefficients:",
                                                    fontSize = 7.5.sp,
                                                    color = TextMuted
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                ) {
                                                    if (alert.poolDistribution.isNotEmpty()) {
                                                        if (alert.poolDistribution.size >= 3) {
                                                            val wHome = alert.poolDistribution[0]
                                                            val wDraw = alert.poolDistribution[1]
                                                            val wAway = alert.poolDistribution[2]
                                                            Box(modifier = Modifier.weight(if (wHome > 0) wHome else 0.01f).fillMaxHeight().background(NeonBlue))
                                                            Box(modifier = Modifier.weight(if (wDraw > 0) wDraw else 0.01f).fillMaxHeight().background(TextLight))
                                                            Box(modifier = Modifier.weight(if (wAway > 0) wAway else 0.01f).fillMaxHeight().background(Color(0xFFFF4136)))
                                                        } else if (alert.poolDistribution.size >= 2) {
                                                            val w1 = alert.poolDistribution[0]
                                                            val w2 = alert.poolDistribution[1]
                                                            Box(modifier = Modifier.weight(if (w1 > 0) w1 else 0.01f).fillMaxHeight().background(NeonGreen))
                                                            Box(modifier = Modifier.weight(if (w2 > 0) w2 else 0.01f).fillMaxHeight().background(AmberAccent))
                                                        } else {
                                                            Box(modifier = Modifier.fillMaxSize().background(NeonBlue))
                                                        }
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize().background(NeonBlue))
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    if (alert.poolDistribution.size >= 3) {
                                                        Text(text = "Home Stake (${(alert.poolDistribution[0]*100).toInt()}%)", fontSize = 7.sp, color = NeonBlue)
                                                        Text(text = "Draw Stake (${(alert.poolDistribution[1]*100).toInt()}%)", fontSize = 7.sp, color = TextLight)
                                                        Text(text = "Away Stake (${(alert.poolDistribution[2]*100).toInt()}%)", fontSize = 7.sp, color = Color(0xFFFF4136))
                                                    } else if (alert.poolDistribution.size >= 2) {
                                                        Text(text = "Over Stake (${(alert.poolDistribution[0]*100).toInt()}%)", fontSize = 7.sp, color = NeonGreen)
                                                        Text(text = "Under Stake (${(alert.poolDistribution[1]*100).toInt()}%)", fontSize = 7.sp, color = AmberAccent)
                                                    } else {
                                                        Text(text = "Unified Single Stake Pool (100%)", fontSize = 7.sp, color = NeonBlue)
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            riskAlerts = riskAlerts.map { a ->
                                                                if (a.id == alert.id) a.copy(status = "SUSPENDED") else a
                                                            }
                                                            riskStatusLog = "[ADMIN LOCKDOWN] Manual market suspension issued on Match #${alert.matchId}. Bets halted successfully."
                                                        },
                                                        enabled = alert.status != "SUSPENDED",
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4136)),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("🚫 Suspend Betting", fontSize = 7.sp, color = Color.White, fontWeight = FontWeight.Black)
                                                    }
                                                    
                                                    Button(
                                                        onClick = {
                                                            riskAlerts = riskAlerts.map { a ->
                                                                if (a.id == alert.id) a.copy(status = "CLEARED", reason = "Liability capped at 150k limit. Risk mitigated.") else a
                                                            }
                                                            riskStatusLog = "[ADMIN OVERRIDE] Capped max return pool liability on Match #${alert.matchId}. Risk status updated."
                                                        },
                                                        enabled = alert.status != "CLEARED" && alert.status != "BLOCKED",
                                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("⚖️ Cap Liability", fontSize = 7.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                                    }
                                                    
                                                    Button(
                                                        onClick = {
                                                            riskAlerts = riskAlerts.map { a ->
                                                                if (a.id == alert.id) a.copy(status = "CLEARED", reason = "Administrative inspection passed. Alert dismissed.") else a
                                                            }
                                                            riskStatusLog = "[ADMIN APPROVAL] Inspected Match #${alert.matchId} pool distribution. Deemed standard syndication. Dismissed."
                                                        },
                                                        enabled = alert.status != "CLEARED",
                                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1.3f),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("✅ Clear & Dismiss", fontSize = 7.sp, color = Color.Black, fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (preferredBroadcasterTab == "AUDIT") {
                        // "AUDIT"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📝 pg-SecurityAuditLogger",
                                    color = NeonBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NeonGreen.copy(0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LEDGER APPEND ACTIVE",
                                        color = NeonGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Creates an unalterable operational footprint in the DB ledger (system_audit_logs) for security tracing of manual balance credits, score alterations, or user suspensions.",
                                fontSize = 10.sp,
                                color = TextMuted,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive PostgreSQL DDL Schema Viewer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0C1322))
                                    .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                                    .clickable { showSchemaDdl = !showSchemaDdl }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "Code icon",
                                        tint = NeonBlue,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PostgreSQL Table Schema (DDL Spec)",
                                        color = TextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (showSchemaDdl) "HIDE CODE ▲" else "VIEW SCHEMA ▼",
                                    color = NeonBlue,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (showSchemaDdl) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = """
                                            -- Security Audit Table
                                            CREATE TABLE system_audit_logs (
                                                id BIGSERIAL PRIMARY KEY,
                                                admin_id INT NOT NULL REFERENCES users(id),
                                                action_type VARCHAR(100) NOT NULL,
                                                target_entity_id VARCHAR(100) NOT NULL,
                                                previous_state JSONB,
                                                updated_state JSONB,
                                                ip_origin VARCHAR(45) NOT NULL, -- Supports IPv4 and IPv6 string formats
                                                execution_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
                                            );

                                            -- Fast tracking indexes for high-level management inspections
                                            CREATE INDEX idx_audit_admin_actions ON system_audit_logs (admin_id, execution_time DESC);
                                            CREATE INDEX idx_audit_action_type ON system_audit_logs (action_type);
                                        """.trimIndent(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = TextLight,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive AuditLogService.ts Core Code expander
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0F141F))
                                    .border(0.5.dp, NeonBlue.copy(0.4f), RoundedCornerShape(4.dp))
                                    .clickable { showAuditServiceCode = !showAuditServiceCode }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Shield Icon",
                                        tint = NeonBlue,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AuditLogService.ts (Postgres Ledger Core)",
                                        color = TextWhite,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (showAuditServiceCode) "HIDE SERVICE ▲" else "VIEW SECURITY SERVICE ▼",
                                    color = NeonBlue,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (showAuditServiceCode) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = """
                                            import { Pool } from 'pg';

                                            export interface IAuditLogEntry {
                                              adminId: number;
                                              action: string;       // e.g., 'MANUAL_BALANCE_CREDIT', 'MATCH_SUSPENDED'
                                              targetId: string;     // ID of the altered item
                                              oldValue: string;     // JSON string representing former state
                                              newValue: string;     // JSON string representing updated state
                                              ipAddress: string;
                                            }

                                            export class AuditLogService {
                                              constructor(private readonly dbPool: Pool) {}

                                              /**
                                               * Creates an unalterable operational footprint in the DB ledger
                                               */
                                              public async writeLog(entry: IAuditLogEntry): Promise<void> {
                                                const { adminId, action, targetId, oldValue, newValue, ipAddress } = entry;

                                                const queryText = `
                                                  INSERT INTO system_audit_logs (
                                                    admin_id, action_type, target_entity_id, previous_state, updated_state, ip_origin, execution_time
                                                  ) VALUES ($1, $2, $3, $4, $5, $6, NOW())
                                                `;

                                                try {
                                                  await this.dbPool.query(queryText, [
                                                    adminId, 
                                                    action, 
                                                    targetId, 
                                                    oldValue, 
                                                    newValue, 
                                                    ipAddress
                                                  ]);
                                                  console.log(`[AUDIT SECURE] Action '${'$'}{action}' by Admin #${'$'}{adminId} logged successfully.`);
                                                } catch (error: any) {
                                                  // Fail loudly to prevent untracked backend alterations
                                                  console.error('❌ [AUDIT LOG CRITICAL FAILURE]: Could not write security trail:', error.message);
                                                }
                                              }
                                            }
                                        """.trimIndent(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        color = TextLight,
                                        lineHeight = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "1. SELECT SECURITY OVERRIDE PRESET",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val auditPresets = listOf(
                                "MANUAL_CREDIT" to "Credit user wallet key (High risk)",
                                "MATCH_SUSPENDED" to "Immediate match score override suspension",
                                "TICKET_VOID" to "Emergency void strike against transaction"
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                auditPresets.forEach { (presetName, presetInfo) ->
                                    val isSelected = auditAction == presetName

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color(0xFF0F1622) else SlateSurfaceL2)
                                            .border(0.5.dp, if (isSelected) NeonBlue.copy(0.8f) else BorderColor.copy(0.5f), RoundedCornerShape(4.dp))
                                            .clickable {
                                                auditAction = presetName
                                                when (presetName) {
                                                    "MANUAL_CREDIT" -> {
                                                        auditAdminId = "1"
                                                        auditTargetId = "wallet_user_482"
                                                        auditOldValue = "{\"balance\": 500.00}"
                                                        auditNewValue = "{\"balance\": 25000.00}"
                                                        auditIpAddress = "192.168.1.104"
                                                    }
                                                    "MATCH_SUSPENDED" -> {
                                                        auditAdminId = "3"
                                                        auditTargetId = "match_83901"
                                                        auditOldValue = "{\"active\": true}"
                                                        auditNewValue = "{\"active\": false}"
                                                        auditIpAddress = "10.0.0.12"
                                                    }
                                                    "TICKET_VOID" -> {
                                                        auditAdminId = "4"
                                                        auditTargetId = "ticket_90112"
                                                        auditOldValue = "{\"status\": \"Pending\"}"
                                                        auditNewValue = "{\"status\": \"Voided_Manual\"}"
                                                        auditIpAddress = "172.16.2.24"
                                                    }
                                                }
                                                auditConsoleLog = "Loaded preset '$presetName'. Ready to submit ledger transaction footprint."
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(NeonBlue.copy(0.15f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = presetName,
                                                color = NeonBlue,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = presetInfo,
                                            color = if (isSelected) TextWhite else TextLight,
                                            fontSize = 9.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ADMIN ID", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditAdminId,
                                        onValueChange = { auditAdminId = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text("ACTION NAME", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditAction,
                                        onValueChange = { auditAction = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text("IP ORIGIN", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditIpAddress,
                                        onValueChange = { auditIpAddress = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TARGET ENTITY ID", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditTargetId,
                                        onValueChange = { auditTargetId = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PREVIOUS STATE (JSON)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditOldValue,
                                        onValueChange = { auditOldValue = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }

                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text("UPDATED STATE (JSON)", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = auditNewValue,
                                        onValueChange = { auditNewValue = it },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextLight,
                                            focusedBorderColor = NeonBlue,
                                            unfocusedBorderColor = BorderColor,
                                            focusedContainerColor = Color(0xFF090D13),
                                            unfocusedContainerColor = Color(0xFF090D13)
                                        ),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (auditAdminId.isBlank() || auditAction.isBlank() || auditTargetId.isBlank()) {
                                            auditConsoleLog = "❌ [AUDIT ENTRY REJECTED] Missing mandatory identifier properties on ledger submit validation call."
                                            return@Button
                                        }

                                        val systemHeader = "[AUDIT SECURE] ACTION: '${auditAction.uppercase()}' on '${auditTargetId}' logged (IP: $auditIpAddress)\n" +
                                                "  └ Old: '${auditOldValue}'\n" +
                                                "  └ New: '${auditNewValue}' by Admin #${auditAdminId}"

                                        // Append log history
                                        auditLoggedHistory = listOf(systemHeader) + auditLoggedHistory

                                        val adminIntVal = auditAdminId.toIntOrNull() ?: 1
                                        val nextId = (auditTableEntries.maxOfOrNull { it.id } ?: 1000L) + 1
                                        val tStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                                        
                                        val newEntry = AuditLogEntry(
                                            id = nextId,
                                            adminId = adminIntVal,
                                            actionType = auditAction.uppercase(),
                                            targetEntityId = auditTargetId,
                                            previousState = auditOldValue,
                                            updatedState = auditNewValue,
                                            ipOrigin = auditIpAddress,
                                            executionTime = tStr
                                        )
                                        auditTableEntries = listOf(newEntry) + auditTableEntries

                                        auditConsoleLog = "[AUDIT SECURE] Action '${auditAction}' by Admin #${auditAdminId} logged successfully.\n" +
                                                "└ DB Execution Query: INSERT INTO system_audit_logs (admin_id, action_type, target_entity_id, previous_state, updated_state, ip_origin, execution_time) VALUES ($auditAdminId, '${auditAction}', '${auditTargetId}', '${auditOldValue}', '${auditNewValue}', '${auditIpAddress}', NOW())\n" +
                                                "└ Consolidated unalterable transaction footprint stored in standard append-only ledger security track tables."
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Write Audit Trail Log", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        auditAdminId = "1"
                                        auditAction = "MANUAL_BALANCE_CREDIT"
                                        auditTargetId = "wallet_user_482"
                                        auditOldValue = "{\"balance\": 500.00}"
                                        auditNewValue = "{\"balance\": 5500.00}"
                                        auditIpAddress = "192.168.1.104"
                                        auditConsoleLog = "Restored default credit state properties. Ledger active."
                                    },
                                    modifier = Modifier.weight(0.7f),
                                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceL2),
                                    border = BorderStroke(1.dp, BorderColor),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Text("Reset Spec", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLight)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "pg-AUDIT SECURITY TRAIL SYSTEM LOG:",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp)
                                    .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = auditConsoleLog,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    color = if (auditConsoleLog.contains("❌")) AmberAccent else NeonGreen,
                                    lineHeight = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "IMMUTABLE DB APPEND-ONLY AUDIT HISTORIC LEDGER:",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                auditLoggedHistory.forEach { logItem ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF04060A), RoundedCornerShape(4.dp))
                                            .border(0.5.dp, BorderColor.copy(0.4f), RoundedCornerShape(4.dp))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = logItem,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.5.sp,
                                            color = TextLight,
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONTROL TAB SUBHEADER: DYNAMIC BET RECORDS LIST
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LIVE ACTIVE USER BETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(LightRed.copy(0.12f), RoundedCornerShape(4.dp))
                            .border(1.dp, LightRed.copy(0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(LightRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "MONITORING",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightRed
                            )
                        }
                    }
                }
            }
        }

        if (allBets.isEmpty()) {
            item {
                EmptyStateCard(message = "No bets logged yet. New tickets placed inside the Sportsbook will show up here instantly.")
            }
        } else {
            items(allBets) { bet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                    border = BorderStroke(1.dp, if (bet.status == "PENDING") AmberAccent.copy(0.3f) else BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimarySapphire.copy(0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = bet.sport.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Wager ID: #${73200 + bet.id}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val rawNum = bet.ticketNumber
                                val fmtNum = if (rawNum.length >= 12) "${rawNum.substring(0,3)}-${rawNum.substring(3,7)}-${rawNum.substring(7,11)}" else rawNum
                                Text(
                                    text = "Ticket: $fmtNum",
                                    fontSize = 10.sp,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (bet.status) {
                                            "PENDING" -> AmberAccent.copy(0.12f)
                                            "WON" -> NeonGreen.copy(0.15f)
                                            "LOST" -> LightRed.copy(0.15f)
                                            else -> TextGrey.copy(0.15f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = bet.status,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when (bet.status) {
                                        "PENDING" -> AmberAccent
                                        "WON" -> NeonGreen
                                        "LOST" -> LightRed
                                        else -> TextLight
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val legs = com.example.data.model.deserializeBetItems(bet.subItemsJson)
                        if (legs.isNotEmpty()) {
                            Text(
                                text = "ACCUMULATOR MULTIBET TICKET (${legs.size} Legs)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                legs.forEach { leg ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${leg.teamA} vs ${leg.teamB}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "${leg.sport} • Market: ${leg.marketType} • Pick: ${leg.selection}",
                                                fontSize = 9.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Text(
                                            text = "@${String.format("%.2f", leg.odds)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NeonGreen
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Text(
                                text = "${bet.teamA} vs ${bet.teamB}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                if (legs.isEmpty()) {
                                    Text(
                                        text = "Market: ${bet.marketType} • Selection [${bet.selection}]",
                                        fontSize = 11.sp,
                                        color = TextLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Odds: ", fontSize = 11.sp, color = TextMuted)
                                        Text(
                                            text = String.format("%.2f", bet.odds),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AmberAccent
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = "Stake: ", fontSize = 11.sp, color = TextMuted)
                                        Text(
                                            text = String.format("%,.0f ETB", bet.stake),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (bet.status == "PENDING") "POSSIBLE WIN" else "PROCESSED PAY",
                                    fontSize = 8.sp,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("%,.2f ETB", bet.potentialReturn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (bet.status == "WON") NeonGreen else TextWhite
                                )
                            }
                        }

                        // Settlement control block
                        if (bet.status == "PENDING") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = BorderColor, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (bet.isApproved) Icons.Default.VerifiedUser else Icons.Default.PrivacyTip,
                                        contentDescription = null,
                                        tint = if (bet.isApproved) NeonGreen else AmberAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (bet.isApproved) "APPROVED & REGISTERED" else "PENDING APPROVAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (bet.isApproved) NeonGreen else AmberAccent
                                    )
                                }
                                
                                if (!bet.isApproved) {
                                    Button(
                                        onClick = { viewModel.approveBet(bet) },
                                        modifier = Modifier.height(24.dp).testTag("btn_admin_approve_${bet.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = SlateDarkBG),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(10.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("APPROVE NOW", fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.resolveBet(bet, true) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(0.12f), contentColor = NeonGreen),
                                    border = BorderStroke(1.dp, NeonGreen.copy(0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESOLVE WIN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.resolveBet(bet, false) },
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.12f), contentColor = LightRed),
                                    border = BorderStroke(1.dp, LightRed.copy(0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESOLVE LOSS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ⚙️ PROGRAMMATIC WAGER SETTLER ENGINE CONTROL SUITE
        item {
            var settlerAlertMessage by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, AmberAccent.copy(0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚙️ WAGER SETTLER ENGINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonGreen.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "OPERATIONAL",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Directly operates the transaction-isolated multi-rule outcome evaluation matrix (1X2, Over/Under 2.5, BTTS). Scans for pending user wagers matching concluded matches and executes payouts atomically into user banks with complete transaction trail ledger syncing.",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )

                    settlerAlertMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeonGreen.copy(0.12f), RoundedCornerShape(6.dp))
                                .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = msg,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.runSettlementEngine { count ->
                                    val logMsg = if (count > 0) {
                                        "🎯 Settlement complete! Successfully processed $count pending wager tickets into the Ledger and updated user bank balances."
                                    } else {
                                        "🔍 Settlement scan run: No pending tickets found matching concluded matches."
                                    }
                                    settlerAlertMessage = logMsg
                                    coroutineScope.launch {
                                        delay(5000)
                                        if (settlerAlertMessage == logMsg) {
                                            settlerAlertMessage = null
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent.copy(0.15f), contentColor = AmberAccent),
                            border = BorderStroke(1.dp, AmberAccent.copy(0.40f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RUN SETTLER SCAN", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⛳ TEST ENVIRONMENT MATCH COMPLETION SIMULATORS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.concludeMatchSimulated(matchId = 1, homeScore = 3, awayScore = 1)
                                val logMsg = "🏁 Simulated Match Concluding: Chelsea vs Man City has ended FT 1-3. Run Settler Scan to resolve outstanding bets!"
                                settlerAlertMessage = logMsg
                                coroutineScope.launch {
                                    delay(6000)
                                    if (settlerAlertMessage == logMsg) settlerAlertMessage = null
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(0.12f), contentColor = NeonBlue),
                            border = BorderStroke(1.dp, NeonBlue.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("END MAN CITY (3-1)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.concludeMatchSimulated(matchId = 2, homeScore = 1, awayScore = 2)
                                val logMsg = "🏁 Simulated Match Concluding: Arsenal vs Liverpool has ended FT 1-2. Run Settler Scan to resolve outstanding bets!"
                                settlerAlertMessage = logMsg
                                coroutineScope.launch {
                                    delay(6000)
                                    if (settlerAlertMessage == logMsg) settlerAlertMessage = null
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(0.12f), contentColor = NeonPurple),
                            border = BorderStroke(1.dp, NeonPurple.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SportsBasketball, contentDescription = null, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("END LIVERPOOL (1-2)", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ⚡ ATOMIC WAGER ROUTER EMULATOR CARD (POST /api/v1/bets/place & GET /market/active-sheets)
        item {
            var activeApiMethod by remember { mutableStateOf("POST_PLACE") } // "POST_PLACE", "GET_ACTIVE_SHEETS"
            var jsonRequestInput by remember {
                mutableStateOf(
                    """
                    {
                      "userId": 1,
                      "matchId": 1,
                      "selection": "1",
                      "expectedOdds": 2.10,
                      "stake": 150.0,
                      "maxSlippageAllowed": 0.05
                    }
                    """.trimIndent()
                )
            }
            var jsonResponseOutput by remember {
                mutableStateOf(
                    """
                    HTTP/1.1 100 Continue
                    Content-Type: text/plain
                    
                    Waiting for client triggers...
                    Select a preset or switch API methods.
                    """.trimIndent()
                )
            }
            var isPosting by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_router_emulator_card"),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, NeonBlue.copy(0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = NeonBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ ATOMIC WAGER ROUTER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = TextWhite,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Pill selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(0.3f), RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val methods = listOf(
                            "POST_PLACE" to "POST /v1/bets/place",
                            "GET_ACTIVE_SHEETS" to "GET /market/active-sheets"
                        )
                        methods.forEach { (methodId, label) ->
                            val isSelected = activeApiMethod == methodId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) NeonBlue.copy(0.15f) else Color.Transparent)
                                    .clickable { 
                                        activeApiMethod = methodId 
                                        jsonResponseOutput = """
                                        HTTP/1.1 100 Continue
                                        Content-Type: text/plain
                                        
                                        Waiting for client triggers...
                                        Click EMULATE below to call the active router endpoint.
                                        """.trimIndent()
                                    }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) NeonBlue else TextLight.copy(0.7f),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (activeApiMethod == "POST_PLACE") {
                            "Submits simulated wager tickets using the original Node.js Express router handler format. Features concurrent race protection using transaction isolation layers and automated odds slippage margin assessment."
                        } else {
                            "Serves current active matches and live odds sheets aggregated directly from the local Room database, emulating a low-latency Redis cache layer (5s TTL) with automated cache rebuilds."
                        },
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )

                    if (activeApiMethod == "POST_PLACE") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "REQUEST BODY (JSON)",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        TextField(
                            value = jsonRequestInput,
                            onValueChange = { jsonRequestInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .testTag("api_json_request_field"),
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextWhite
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SlateSurfaceL2,
                                unfocusedContainerColor = SlateSurfaceL2,
                                disabledContainerColor = SlateSurfaceL2,
                                focusedIndicatorColor = NeonBlue,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = false,
                            shape = RoundedCornerShape(6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val requestPresets = listOf(
                                Triple("👍 Standard", "150.0", "2.10"),
                                Triple("⚖️ Slippage Error", "100.0", "15.50"),
                                Triple("💰 Insufficient", "999999.0", "2.10")
                            )
                            requestPresets.forEach { (label, presetStake, presetOdds) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(SlateSurfaceL2, RoundedCornerShape(4.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .clickable {
                                            jsonRequestInput = """
                                            {
                                              "userId": 1,
                                              "matchId": 1,
                                              "selection": "1",
                                              "expectedOdds": $presetOdds,
                                              "stake": $presetStake,
                                              "maxSlippageAllowed": 0.05
                                            }
                                            """.trimIndent()
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextLight
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isPosting = true
                            if (activeApiMethod == "POST_PLACE") {
                                jsonResponseOutput = """
                                HTTP/1.1 100 Continue
                                Content-Type: text/plain
                                
                                Parsing body stream buffers...
                                Executing transactional lock threads...
                                """.trimIndent()
                                viewModel.emulateRestPost(jsonRequestInput) { res ->
                                    coroutineScope.launch {
                                        delay(900) // Simulated backend execution lag and network latency
                                        jsonResponseOutput = res
                                        isPosting = false
                                    }
                                }
                            } else {
                                jsonResponseOutput = """
                                HTTP/1.1 100 Continue
                                Content-Type: text/plain
                                
                                Reading Redis cluster memory pages...
                                Aggregating matching market nodes...
                                """.trimIndent()
                                viewModel.emulateRestGetActiveSheets { res ->
                                    coroutineScope.launch {
                                        delay(700) // Low-latency execution lag
                                        jsonResponseOutput = res
                                        isPosting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("api_post_emulate_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isPosting
                    ) {
                        if (isPosting) {
                            Text("EXECUTING PIPELINE ENGINE...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeApiMethod == "POST_PLACE") "EMULATE POST /v1/bets/place" else "EMULATE GET /market/active-sheets",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "RESPONSE HEADERS & BODY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceL2, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        val isSuccessResponse = jsonResponseOutput.contains("200 OK")
                        val responseColor = if (isSuccessResponse) NeonGreen else if (jsonResponseOutput.contains("Continue")) AmberAccent else LightRed

                        Text(
                            text = jsonResponseOutput,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = responseColor,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // 📡 SPORTS ODDS FEED BROKER (FEED NORMALIZATION ENGINE)
        item {
            var showFeedBlueprints by remember { mutableStateOf(false) }
            var activeFeedBlueprintTab by remember { mutableStateOf("RAW") }
            var webhookResponseText by remember { mutableStateOf<String?>(null) }
            val NeonOrange = Color(0xFFFF9F0A)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val streamStatus by viewModel.liveFeedStatus.collectAsState()
                    val badgeColor = when (streamStatus) {
                        "Connected" -> NeonGreen
                        "Connecting", "Reconnecting" -> AmberAccent
                        "Error/Reconnecting" -> LightRed
                        else -> TextMuted
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📡 ODDS FEED BROKER & NORMALIZER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "WS STATUS: ${streamStatus.uppercase()}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = badgeColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Consumes high-concurrency broker payloads (IRawOddsPayload) via WebSocket and translates them into normalized structures (INormalizedOddsUpdate) to sync active matches and odds lines inside Room DB.",
                        fontSize = 10.sp,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // INPUT CONTROLS for Live Stream
                    Text(
                        text = "WEB_STREAM URL",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = feedUrlInput,
                        onValueChange = { feedUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("wss://...", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color(0xFF090D13),
                            unfocusedContainerColor = Color(0xFF090D13)
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "CORE BROKER TOKEN / API KEY",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("secret_token...", color = TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextLight,
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color(0xFF090D13),
                            unfocusedContainerColor = Color(0xFF090D13)
                        ),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.startLiveFeedClient(feedUrlInput, apiKeyInput)
                            },
                            modifier = Modifier.weight(1.0f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(0.12f), contentColor = NeonGreen),
                            border = BorderStroke(1.dp, NeonGreen.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🔌 CONNECT WS STREAM", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.stopLiveFeedClient()
                            },
                            modifier = Modifier.weight(1.0f).height(36.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.12f), contentColor = LightRed),
                            border = BorderStroke(1.dp, LightRed.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("❌ DISCONNECT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🚀 DIRECT STREAM PAYLOAD SIMULATION BUTTONS:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val jsonFootballStr = """
                                {
                                  "eventId": "sr:match:1",
                                  "timestamp": 1781792618000,
                                  "sport": "Football",
                                  "status": "Live",
                                  "score": {
                                    "home": 2,
                                    "away": 1,
                                    "elapsed": "72'"
                                  },
                                  "markets": [
                                    {
                                      "marketId": "1X2",
                                      "status": "active",
                                      "odds": [
                                        { "outcome": "1", "price": 1.45 },
                                        { "outcome": "X", "price": 3.85 },
                                        { "outcome": "2", "price": 6.80 }
                                      ]
                                    },
                                    {
                                      "marketId": "Over/Under",
                                      "status": "active",
                                      "odds": [
                                        { "outcome": "Over 2.5", "price": 1.15 },
                                        { "outcome": "Under 2.5", "price": 4.50 }
                                      ]
                                    }
                                  ]
                                }
                                """.trimIndent()
                                viewModel.processRawProviderFeed(jsonFootballStr)
                            },
                            modifier = Modifier.weight(1.0f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue.copy(0.12f), contentColor = NeonBlue),
                            border = BorderStroke(1.dp, NeonBlue.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("⚽ SIM COV: MAN CITY", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val jsonBasketballStr = """
                                {
                                  "eventId": "sr:match:2",
                                  "timestamp": 1781792619000,
                                  "sport": "Football",
                                  "status": "Live",
                                  "score": {
                                    "home": 1,
                                    "away": 2,
                                    "elapsed": "55'"
                                  },
                                  "markets": [
                                    {
                                      "marketId": "1X2",
                                      "status": "active",
                                      "odds": [
                                        { "outcome": "1", "price": 5.20 },
                                        { "outcome": "X", "price": 4.10 },
                                        { "outcome": "2", "price": 1.55 }
                                      ]
                                    },
                                    {
                                      "marketId": "BTTS",
                                      "status": "suspended",
                                      "odds": [
                                        { "outcome": "BTTS Yes", "price": 1.50 },
                                        { "outcome": "BTTS No", "price": 2.40 }
                                      ]
                                    }
                                  ]
                                }
                                """.trimIndent()
                                viewModel.processRawProviderFeed(jsonBasketballStr)
                            },
                            modifier = Modifier.weight(1.0f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(0.12f), contentColor = NeonPurple),
                            border = BorderStroke(1.dp, NeonPurple.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🏀 SIM ARS: LIVERPOOL", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🔌 SECURE SPORTS WEBHOOK ENDPOINT SIMULATION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Post raw HTTP webhook update to match telemetry or odds matrix endpoint, simulating live data provider ingest.",
                        fontSize = 8.sp,
                        color = TextMuted,
                        lineHeight = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val payload = """
                                {
                                  "fixture_id": 849201,
                                  "status": "Live",
                                  "home_score": 2,
                                  "away_score": 1,
                                  "elapsed_time": 68
                                }
                                """.trimIndent()
                                viewModel.processSportsWebhook(payload) { resp ->
                                    webhookResponseText = resp
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(0.12f), contentColor = NeonPurple),
                            border = BorderStroke(1.dp, NeonPurple.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("⚽ POST /match PAYLOAD", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }

                        Button(
                            onClick = {
                                val payload = """
                                {
                                  "fixture_id": 849201,
                                  "market_type": "1X2",
                                  "odds_book": [
                                    { "selection": "1", "price": 1.45, "is_suspended": false },
                                    { "selection": "X", "price": 3.80, "is_suspended": false },
                                    { "selection": "2", "price": 6.20, "is_suspended": true }
                                  ]
                                }
                                """.trimIndent()
                                viewModel.processOddsWebhook(payload) { resp ->
                                    webhookResponseText = resp
                                }
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonOrange.copy(0.12f), contentColor = NeonOrange),
                            border = BorderStroke(1.dp, NeonOrange.copy(0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("📈 POST /odds PAYLOAD", fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    webhookResponseText?.let { resp ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "HTTP/1.1 202 Accepted",
                                    fontSize = 8.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = resp,
                                    fontSize = 8.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = TextLight,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💻 BROKER CHANNEL STREAM OUTPUT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "CLEAR STREAM LOGS",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            modifier = Modifier.clickable { viewModel.clearBrokerLogs() }
                        )
                    }

                    val brokerLogs by viewModel.feedBrokerConsoleLogs.collectAsState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(brokerLogs) { log ->
                                val logColor = when {
                                    log.contains("✅") || log.contains("[PARSING SUCCESS]") || log.contains("[DB SYNCED]") -> NeonGreen
                                    log.contains("❌") || log.contains("[PARSING ERROR]") -> LightRed
                                    log.contains("⚠️") || log.contains("[DATABASE SKIP]") || log.contains("exception") -> AmberAccent
                                    log.contains("📡") || log.contains("[INCOMING FEED]") || log.contains("[CLIENT START]") -> NeonBlue
                                    log.contains("🌀") || log.contains("[NORMALIZING]") || log.contains("[WEB_STREAM UPDATE]") -> Color(0xFFBD00FF)
                                    else -> TextGrey
                                }
                                Text(
                                    text = log,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = logColor,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F162A))
                            .border(0.5.dp, Color(0xFFE2E8F0).copy(0.3f), RoundedCornerShape(4.dp))
                            .clickable { showFeedBlueprints = !showFeedBlueprints }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Code icon",
                                tint = NeonPurple,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRODUCTION FEED TYPE SCHEMAS",
                                color = TextWhite,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = if (showFeedBlueprints) "HIDE BLUEPRINTS ▲" else "VIEW BLUEPRINTS ▼",
                            color = NeonPurple,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (showFeedBlueprints) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070B11), RoundedCornerShape(6.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Strict Type definitions mapping the inbound multi-source WebSocket stream payloads to the SQLite transactional schema:",
                                fontSize = 8.sp,
                                color = TextMuted,
                                lineHeight = 11.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val feedTabs = listOf(
                                    "RAW" to "IRawOddsPayload",
                                    "NORMALIZED" to "INormalizedOddsUpdate",
                                    "SERVICE" to "LiveOddsFeedService.ts",
                                    "PROCESSOR" to "feedProcessor.ts"
                                )
                                feedTabs.forEach { (tabId, tabTitle) ->
                                    val isTabSelected = activeFeedBlueprintTab == tabId
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isTabSelected) NeonPurple.copy(0.12f) else Color.Black)
                                            .border(0.5.dp, if (isTabSelected) NeonPurple else BorderColor, RoundedCornerShape(4.dp))
                                            .clickable { activeFeedBlueprintTab = tabId }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tabTitle,
                                            color = if (isTabSelected) NeonPurple else TextLight,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            val modelContent = if (activeFeedBlueprintTab == "RAW") {
                                """
                                export type ProviderMarketStatus = 'active' | 'suspended' | 'deactivated';

                                // Raw event payload directly from the feed broker
                                export interface IRawOddsPayload {
                                  eventId: string;              // e.g., "sr:match:548912"
                                  timestamp: number;
                                  sport: string;
                                  status: 'Live' | 'PreMatch' | 'Ended';
                                  score?: {
                                    home: number;
                                    away: number;
                                    elapsed: string;           // e.g., "65'"
                                  };
                                  markets: Array<{
                                    marketId: string;           // e.g., "1X2" or "total_goals"
                                    status: ProviderMarketStatus;
                                    odds: Array<{
                                      outcome: string;          // e.g., "1", "X", "2", "Over 2.5"
                                      price: number;            // Decimal format odds line (e.g., 1.85)
                                    }>;
                                  }>;
                                }
                                """.trimIndent()
                            } else if (activeFeedBlueprintTab == "NORMALIZED") {
                                """
                                // Normalized schema matching your PostgreSQL transactional layer
                                export interface INormalizedOddsUpdate {
                                  matchId: number;
                                  homeScore: number;
                                  awayScore: number;
                                  matchTime: string;
                                  marketType: string;
                                  selectionName: string;
                                  oddsValue: number;
                                  isSuspended: boolean;
                                }
                                """.trimIndent()
                            } else if (activeFeedBlueprintTab == "SERVICE") {
                                """
                                import WebSocket from 'ws';
                                import { IRawOddsPayload, INormalizedOddsUpdate } from './interfaces';

                                export class LiveOddsFeedService {
                                  private wsClient: WebSocket | null = null;
                                  private reconnectAttempts = 0;
                                  private readonly maxReconnectAttempts = 10;
                                  private readonly baseReconnectDelayMs = 2000;
                                  private heartbeatInterval: NodeJS.Timeout | null = null;

                                  constructor(
                                    private readonly feedUrl: string,
                                    private readonly apiKey: string,
                                    private readonly onValidatedUpdate: (update: INormalizedOddsUpdate) => Promise<void>
                                  ) {}

                                  /**
                                   * Initializes connection to the high-concurrency stream broker
                                   */
                                  public connect(): void {
                                    const authenticatedUrl = `${'$'}{this.feedUrl}?token=${'$'}{this.apiKey}`;
                                    console.log(`[FEED ENGINE] Initializing secure channel to stream broker...`);

                                    this.wsClient = new WebSocket(authenticatedUrl);

                                    this.wsClient.on('open', () => {
                                      console.log('✅ [FEED ENGINE] Secure stream connection established with broker.');
                                      this.reconnectAttempts = 0;
                                      this.startHeartbeatCheck();
                                    });

                                    this.wsClient.on('message', async (data: WebSocket.Data) => {
                                      await this.handleIncomingMessage(data);
                                    });

                                    this.wsClient.on('close', (code, reason) => {
                                      console.warn(`⚠️ [FEED ENGINE] Connection severed (Code: ${'$'}{code}). Reason: ${'$'}{reason.toString() || 'None'}`);
                                      this.cleanupAndReconnect();
                                    });

                                    this.wsClient.on('error', (error) => {
                                      console.error('❌ [FEED ENGINE] WebSocket operational exception caught:', error.message);
                                    });
                                  }

                                  /**
                                   * Maintains active network connections using heartbeat rules
                                   */
                                  private startHeartbeatCheck(): void {
                                    if (this.heartbeatInterval) clearInterval(this.heartbeatInterval);
                                    
                                    this.heartbeatInterval = setInterval(() => {
                                      if (this.wsClient && this.wsClient.readyState === WebSocket.OPEN) {
                                        this.wsClient.ping(); // Standard WebSocket protocol keep-alive check
                                      }
                                    }, 30000); // 30 seconds interval checks
                                  }

                                  /**
                                   * Process raw payload safely without blocking the event loop
                                   */
                                  private async handleIncomingMessage(rawData: WebSocket.Data): Promise<void> {
                                    try {
                                      const parsedJson = JSON.parse(rawData.toString());
                                      
                                      // Heartbeat or provider confirmation interceptors
                                      if (parsedJson.type === 'ping' || parsedJson.type === 'alive') return;

                                      const payload = parsedJson as IRawOddsPayload;
                                      
                                      // Strict structural runtime defensive validation
                                      if (!payload.eventId || !payload.markets) return;

                                      await this.parseAndRoutePayload(payload);
                                    } catch (err) {
                                      console.error('❌ [FEED ENGINE] Serialization parsing error on incoming frame packet:', err);
                                    }
                                  }

                                  /**
                                   * Normalizes raw structural formats into flat ledger update metrics
                                   */
                                  private async parseAndRoutePayload(payload: IRawOddsPayload): Promise<void> {
                                    // Map alphanumeric IDs (e.g., "sr:match:1234") to strict integer formats
                                    const cleanedMatchId = parseInt(payload.eventId.replace(/^\D+/g, ''), 10);
                                    if (isNaN(cleanedMatchId)) return;

                                    const currentHomeScore = payload.score?.home ?? 0;
                                    const currentAwayScore = payload.score?.away ?? 0;
                                    const elapsedMinutes = payload.score?.elapsed ?? "0'";

                                    for (const market of payload.markets) {
                                      const isMarketSuspended = market.status !== 'active';

                                      for (const selection of market.odds) {
                                        // Build normalized internal object mapping to the database update parameters
                                        const normalizedUpdate: INormalizedOddsUpdate = {
                                          matchId: cleanedMatchId,
                                          homeScore: currentHomeScore,
                                          awayScore: currentAwayScore,
                                          matchTime: elapsedMinutes,
                                          marketType: market.marketId,
                                          selectionName: selection.outcome,
                                          oddsValue: selection.price,
                                          isSuspended: isMarketSuspended
                                        };

                                        // Forward to the non-blocking transactional engine update method
                                        await this.onValidatedUpdate(normalizedUpdate).catch((dbErr) => {
                                          console.error(`[DB ROUTE ERROR] Failed processing match target update ${'$'}{cleanedMatchId}:`, dbErr.message);
                                        });
                                      }
                                    }
                                  }

                                  /**
                                   * Triggers an exponential back-off reconnection routine
                                   */
                                  private cleanupAndReconnect(): void {
                                    if (this.heartbeatInterval) clearInterval(this.heartbeatInterval);
                                    
                                    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
                                      console.error('❌ [CRITICAL ALERT] Maximum odds feed reconnection attempts reached. Operational intervention required.');
                                      return;
                                    }

                                    this.reconnectAttempts++;
                                    // Exponential fallback logic calculation: 2s, 4s, 8s, 16s... Maxing out out standard scales
                                    const delay = Math.min(this.baseReconnectDelayMs * Math.pow(2, this.reconnectAttempts), 30000);
                                    
                                    console.log(`⏳ [FEED ENGINE] Attempting connection cycle (${'$'}{this.reconnectAttempts}/${'$'}{this.maxReconnectAttempts}) in ${'$'}{delay}ms...`);
                                    setTimeout(() => this.connect(), delay);
                                  }

                                  /**
                                   * Graceful decommissioning method
                                   */
                                  public disconnect(): void {
                                    if (this.heartbeatInterval) clearInterval(this.heartbeatInterval);
                                    if (this.wsClient) {
                                      this.wsClient.terminate();
                                      console.log('[FEED ENGINE] Clean service disconnect finalized.');
                                    }
                                  }
                                }
                                """.trimIndent()
                            } else {
                                """
                                import { LiveOddsFeedService } from './LiveOddsFeedService';
                                import { INormalizedOddsUpdate } from './interfaces';
                                import { Pool } from 'pg'; // PostgreSQL database pool instance

                                const pgPool = new Pool({ connectionString: process.env.DATABASE_URL });

                                /**
                                 * High-performance processing pool callback
                                 * Executes atomic adjustments on matches and live lines sequentially
                                 */
                                async function processLiveDbUpdate(update: INormalizedOddsUpdate): Promise<void> {
                                  // Use a database transaction to ensure atomicity
                                  const client = await pgPool.connect();
                                  try {
                                    await client.query('BEGIN');

                                    // 1. Update the live match scoreline state metrics
                                    await client.query(`
                                      UPDATE matches 
                                      SET home_score = ${'$'}1, away_score = ${'$'}2, minute_elapsed = ${'$'}3, status = 'Live', updated_at = NOW()
                                      WHERE id = ${'$'}4`,
                                      [update.homeScore, update.awayScore, update.matchTime, update.matchId]
                                    );

                                    // 2. Fetch or insert the correct market relation 
                                    const marketRes = await client.query(`
                                      INSERT INTO markets (match_id, market_type) 
                                      VALUES (${'$'}1, ${'$'}2)
                                      ON CONFLICT (match_id, market_type) DO UPDATE SET updated_at = NOW()
                                      RETURNING id`,
                                      [update.matchId, update.marketType]
                                    );
                                    const marketId = marketRes.rows[0].id;

                                    // 3. Atomically overwrite active outcome prices and suspend states
                                    await client.query(`
                                      INSERT INTO market_odds (market_id, selection, odds, is_suspended, updated_at)
                                      VALUES (${'$'}1, ${'$'}2, ${'$'}3, ${'$'}4, NOW())
                                      ON CONFLICT (market_id, selection) 
                                      DO UPDATE SET odds = EXCLUDED.odds, is_suspended = EXCLUDED.is_suspended, updated_at = NOW()`,
                                      [marketId, update.selectionName, update.oddsValue, update.isSuspended]
                                    );

                                    await client.query('COMMIT');
                                  } catch (error) {
                                    await client.query('ROLLBACK');
                                    throw error;
                                  } finally {
                                    client.release();
                                  }
                                }

                                // Boot the live processing engine service
                                const enterpriseOddsFeed = new LiveOddsFeedService(
                                  "wss://api.provider-stream.bet/v4/sports", 
                                  process.env.SPORTS_DATA_API_KEY || "TEST_KEY",
                                  processLiveDbUpdate
                                );

                                enterpriseOddsFeed.connect();
                                """.trimIndent()
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF030508), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                    .padding(6.dp)
                            ) {
                                val modelScrollState = rememberScrollState()
                                Text(
                                    text = modelContent,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    color = TextLight,
                                    lineHeight = 11.sp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(modelScrollState)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🔌 SECURE SPORTS WEBHOOK INGESTION MONITOR (Translated from React)
        item {
            WebhookMonitor(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🏁 RABBITMQ AMQP RAPID WAGER QUEUE MONITOR (Translated from Node.js)
        item {
            RapidBetQueueMonitor(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🎰 CASINO AGGREGATOR SECURE HANDSHAKE BRIDGE (Translated from Node.js)
        item {
            CasinoAggregatorBridgeMonitor(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 🛡️ ANTI-FRAUD DEVICE INTEGRITY ENGINE (Translated from Node.js & Postgres)
        item {
            AntiFraudDeviceEngineMonitor(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // TELEBIRR GATEWAY CONFIG AUDIT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔌 TELEBIRR GATEWAY INTEGRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonGreen.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val appId = com.example.BuildConfig.TELEBIRR_APP_ID
                    val mchCode = com.example.BuildConfig.TELEBIRR_MCH_SHORT_CODE
                    val apiUrl = com.example.BuildConfig.TELEBIRR_API_URL
                    val notifyUrl = com.example.BuildConfig.TELEBIRR_NOTIFY_URL
                    val hasPrivateKey = com.example.BuildConfig.MERCHANT_PRIVATE_KEY.isNotEmpty() && com.example.BuildConfig.MERCHANT_PRIVATE_KEY != "YOUR_MERCHANT_PRIVATE_KEY" && com.example.BuildConfig.MERCHANT_PRIVATE_KEY != "YOUR_RAW_PRIVATE_KEY"

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GatewayRow("APP ID", appId)
                        GatewayRow("MERCHANT CODE", mchCode)
                        GatewayRow("API URL", apiUrl)
                        GatewayRow("NOTIFY URL", notifyUrl)
                        GatewayRow("RSA SIGNING KEY STATUS", if (hasPrivateKey) "ACTIVE RSA KEY LOADED" else "USING API SIMULATION (DEFAULT)")
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = BorderColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "🎯 WEBHOOK PORTAL WEB TERMINAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val pendingTelebirr = allTransactions.filter { it.method == "TeleBirr" && it.status == "PENDING" }
                    if (pendingTelebirr.isEmpty()) {
                        Text(
                            text = "Status: Listening... No pending Telebirr transactions in queue. Make a deposit using Telebirr first.",
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    } else {
                        var selectedTrx by remember { mutableStateOf<TransactionRecord?>(null) }
                        val currentMatch = pendingTelebirr.find { it.id == selectedTrx?.id }
                        val activeTrx = currentMatch ?: pendingTelebirr.first()
                        selectedTrx = activeTrx

                        Text(
                            text = "Found ${pendingTelebirr.size} pending order(s). Target:",
                            fontSize = 9.sp,
                            color = TextLight,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            items(pendingTelebirr) { tx ->
                                val isSelected = tx.id == activeTrx.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) NeonGreen.copy(0.12f) else SlateSurfaceL2)
                                        .border(1.dp, if (isSelected) NeonGreen else BorderColor, RoundedCornerShape(6.dp))
                                        .clickable { selectedTrx = tx }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "${tx.id} (${tx.amount} ETB)",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NeonGreen else TextLight
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    viewModel.executeTelebirrCallback(
                                        outTradeNo = activeTrx.id,
                                        baseAmount = activeTrx.amount,
                                        simulateTamper = false
                                    )
                                },
                                modifier = Modifier.weight(1f).height(30.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(0.15f), contentColor = NeonGreen),
                                border = BorderStroke(1.dp, NeonGreen.copy(0.4f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🟢 SEND SAFE CALLBACK", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { 
                                    viewModel.executeTelebirrCallback(
                                        outTradeNo = activeTrx.id,
                                        baseAmount = activeTrx.amount,
                                        simulateTamper = true
                                    )
                                },
                                modifier = Modifier.weight(1f).height(30.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.15f), contentColor = LightRed),
                                border = BorderStroke(1.dp, LightRed.copy(0.4f)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🔴 SIMULATE TAMPER", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💻 SHELL STREAM OUTPUT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = "CLEAR CACHE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimarySapphire,
                            modifier = Modifier.clickable { viewModel.clearTelebirrLogs() }
                        )
                    }

                    val consoleLogs by viewModel.telebirrConsoleLogs.collectAsState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFF070A0E), RoundedCornerShape(6.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(consoleLogs) { log ->
                                val logColor = when {
                                    log.contains("✅") || log.contains("[DB SUCCESS]") || log.contains("[WALLET ADDED]") -> NeonGreen
                                    log.contains("❌") || log.contains("[CRITICAL") || log.contains("⛔") -> LightRed
                                    log.contains("⚠️") || log.contains("[SANDBOX") -> AmberAccent
                                    log.contains("📥") -> Color(0xFF00C2FF)
                                    else -> TextGrey
                                }
                                Text(
                                    text = log,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    color = logColor,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // BANK SECURE DIRECT TRANSACTION FLOWS
        item {
            Text(
                text = "RECENT TRANSACTION AUDIT LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 0.5.sp
            )
        }

        if (allTransactions.isEmpty()) {
            item {
                EmptyStateCard(message = "No recent system transfers logged.")
            }
        } else {
            items(allTransactions) { trx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBG),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (trx.type == "DEPOSIT") NeonGreen.copy(0.10f) else LightRed.copy(0.10f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (trx.type == "DEPOSIT") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = if (trx.type == "DEPOSIT") NeonGreen else LightRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${trx.type} [${trx.method}]",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "${trx.id} • ${trx.timeLabel}",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%,.2f ETB", trx.amount),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (trx.type == "DEPOSIT") NeonGreen else LightRed
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (trx.status) {
                                                "APPROVED" -> NeonGreen.copy(0.15f)
                                                "PENDING" -> AmberAccent.copy(0.15f)
                                                else -> LightRed.copy(0.15f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = trx.status,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (trx.status) {
                                            "APPROVED" -> NeonGreen
                                            "PENDING" -> AmberAccent
                                            else -> LightRed
                                        }
                                    )
                                }
                            }
                        }

                        // Pending approvals
                        if (trx.status == "PENDING" && trx.type == "WITHDRAWAL") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = BorderColor, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.updateTransactionStatus(trx, "APPROVED") },
                                    modifier = Modifier.weight(1.0f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(0.15f), contentColor = NeonGreen),
                                    border = BorderStroke(1.dp, NeonGreen.copy(0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("APPROVE TRANSFER", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.updateTransactionStatus(trx, "REJECTED") },
                                    modifier = Modifier.weight(1.0f).height(32.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LightRed.copy(0.15f), contentColor = LightRed),
                                    border = BorderStroke(1.dp, LightRed.copy(0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("REJECT", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Telebirr pending deposits - Webhook simulator
                        if (trx.status == "PENDING" && trx.type == "DEPOSIT" && trx.method == "TeleBirr") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = BorderColor, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (expandedSimTrxId != trx.id) {
                                Button(
                                    onClick = { 
                                        expandedSimTrxId = trx.id
                                        simWebhookResult = null
                                    },
                                    modifier = Modifier.fillMaxWidth().height(34.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent.copy(0.12f), contentColor = AmberAccent),
                                    border = BorderStroke(1.dp, AmberAccent.copy(0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🔌 SIMULATE INCOMING WEBHOOK CALLBACK", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💻 EXPRESS WEBHOOK HANDLER",
                                            fontSize = 9.sp,
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Black
                                        )
                                        IconButton(
                                            onClick = { expandedSimTrxId = null },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = TextMuted,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val mockTradeNo = "T_MOCK_" + trx.id.filter { it.isDigit() }
                                    val dataFields = mapOf(
                                        "appId" to com.example.BuildConfig.TELEBIRR_APP_ID,
                                        "mchShortCode" to com.example.BuildConfig.TELEBIRR_MCH_SHORT_CODE,
                                        "outTradeNo" to trx.id,
                                        "tradeNo" to mockTradeNo,
                                        "transactionStatus" to "SUCCESS",
                                        "totalAmount" to String.format(java.util.Locale.US, "%.2f", trx.amount)
                                    )

                                    val signString = com.example.util.TelebirrUtil.createSignString(dataFields)
                                    val genSignature = com.example.util.TelebirrUtil.signPayload(signString, com.example.BuildConfig.MERCHANT_PRIVATE_KEY)

                                    Text("IPN CALLBACK SPEC FIELDS:", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text("JSON: { appId: \"${dataFields["appId"]}\", outTradeNo: \"${trx.id}\", totalAmount: \"${dataFields["totalAmount"]}\", transactionStatus: \"SUCCESS\" }", fontSize = 8.sp, color = TextLight, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("SORTED SIGN QUERY STRING:", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(signString, fontSize = 8.sp, color = TextLight, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text("GENERATED SHA256withRSA SIGNATURE:", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(genSignature, fontSize = 8.sp, color = NeonBlue, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val verified = com.example.util.TelebirrUtil.verifySignature(
                                                signString,
                                                genSignature,
                                                com.example.BuildConfig.TELEBIRR_PUBLIC_KEY
                                            )
                                            if (verified) {
                                                viewModel.updateTransactionStatus(trx, "APPROVED")
                                                simWebhookResult = "SUCCESS: Signature verified successfully using RSA public key! Transaction with ID ${trx.id} is APPROVED & user received +${trx.amount} ETB."
                                            } else {
                                                simWebhookResult = "ERROR: Signature verification failed. Cryptographic key mismatch!"
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(28.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("EXECUTE CALLBACK VERIFICATION VERDICT", fontSize = 8.sp, fontWeight = FontWeight.Black, color = SlateDarkBG)
                                    }

                                    simWebhookResult?.let { msg ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = msg,
                                            fontSize = 8.sp,
                                            color = if (msg.contains("SUCCESS")) NeonGreen else LightRed,
                                            fontWeight = FontWeight.Bold
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
}

data class WebhookLog(
    val id: Long,
    val type: String,
    val status: String,
    val fixtureId: Int,
    val latency: Int,
    val timestamp: String,
    val msg: String? = null
)

@Composable
fun WebhookMonitor(viewModel: BetViewModel) {
    val NeonBlue = Color(0xFF00C2FF)
    var logs by remember { mutableStateOf(listOf(
        WebhookLog(1, "odds", "SUCCESS", 849201, 42, "10:50:52 PM"),
        WebhookLog(2, "match", "SUCCESS", 849201, 115, "10:50:48 PM"),
        WebhookLog(3, "odds", "ERROR", 721304, 198, "10:49:12 PM", "Foreign key constraint violation on market_id"),
        WebhookLog(4, "match", "SUCCESS", 910443, 56, "10:48:33 PM"),
        WebhookLog(5, "odds", "SUCCESS", 644112, 38, "10:47:15 PM")
    )) }
    var streamActive by remember { mutableStateOf(true) }
    var filter by remember { mutableStateOf("ALL") }

    LaunchedEffect(streamActive) {
        if (!streamActive) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(3000)
            val types = listOf("odds", "match")
            val statuses = listOf("SUCCESS", "SUCCESS", "SUCCESS", "ERROR")
            val pickedType = types.random()
            val pickedStatus = statuses.random()
            val randomFixture = (100000..999999).random()
            val randomLatency = (25..200).random()
            
            val cal = java.util.Calendar.getInstance()
            val timeStr = java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.US).format(cal.time)
            
            val newLog = WebhookLog(
                id = System.currentTimeMillis(),
                type = pickedType,
                status = pickedStatus,
                fixtureId = randomFixture,
                latency = randomLatency,
                timestamp = timeStr,
                msg = if (pickedStatus == "ERROR") "Redis cache eviction timeout / database lock contention retry" else null
            )
            logs = (listOf(newLog) + logs).take(20)
        }
    }

    val totalProcessed = logs.size
    val errorCount = logs.count { it.status == "ERROR" }
    val successCount = logs.count { it.status == "SUCCESS" }
    val successRate = if (totalProcessed > 0) {
        String.format(java.util.Locale.US, "%.1f", (successCount.toFloat() / totalProcessed.toFloat()) * 100)
    } else {
        "100.0"
    }
    val avgLatency = if (totalProcessed > 0) {
        logs.map { it.latency }.average().toInt()
    } else {
        0
    }

    val filteredLogs = logs.filter { log ->
        when (filter) {
            "SUCCESS" -> log.status == "SUCCESS"
            "ERROR" -> log.status == "ERROR"
            else -> true
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Ingestion Stream Monitor",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (streamActive) NeonGreen.copy(0.12f) else SlateSurfaceL2)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (streamActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(NeonGreen)
                                    )
                                }
                                Text(
                                    text = if (streamActive) "LIVE LOGGING" else "STREAM PAUSED",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (streamActive) NeonGreen else TextMuted
                                )
                            }
                        }
                    }
                    Text(
                        text = "Real-time telemetry diagnostics for incoming sports odds webhooks.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { streamActive = !streamActive },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (streamActive) Color(0xFFD97706).copy(0.12f) else NeonBlue.copy(0.12f),
                        contentColor = if (streamActive) Color(0xFFF59E0B) else NeonBlue
                    ),
                    border = BorderStroke(1.dp, if (streamActive) Color(0xFFF59E0B).copy(0.3f) else NeonBlue.copy(0.3f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (streamActive) "Pause Stream" else "Resume Stream",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aggregates Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Card 1: Success Rate
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUCCESS RATE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "SLA",
                            tint = NeonGreen,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$successRate%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "SLA: 99.5%",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Card 2: Active Frame Pool
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FRAME POOL",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Pool",
                            tint = NeonBlue,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalProcessed f",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Size limit: 20",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Card 3: Avg Latency
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATENCY",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Latency",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$avgLatency ms",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Index window TTL",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Card 4: Stream Faults
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FAULTS",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Faults",
                            tint = LightRed,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$errorCount err",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = LightRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Failover enabled",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Table / Event logs list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            ) {
                // Table Sub-header Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C101B))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Broker",
                            tint = NeonBlue,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "Ingestion Event Broker Stream",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    // Filters
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black)
                            .padding(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("ALL", "SUCCESS", "ERROR").forEach { f ->
                            val isSel = filter == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSel) NeonBlue.copy(0.15f) else Color.Transparent)
                                    .border(0.5.dp, if (isSel) NeonBlue else Color.Transparent, RoundedCornerShape(3.dp))
                                    .clickable { filter = f }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = f,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonBlue else TextMuted
                                )
                            }
                        }
                    }
                }

                // Table Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF060910))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "TIMESTAMP", modifier = Modifier.weight(1.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "CATEGORY", modifier = Modifier.weight(1f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "MAPPING ID", modifier = Modifier.weight(1.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "LATENCY", modifier = Modifier.weight(1f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "TRANSACTION EXECUTION TRACE", modifier = Modifier.weight(3.5f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Divider(color = BorderColor, thickness = 0.5.dp)

                // List Items
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (filteredLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No webhook traffic frames matching the chosen classification filters were intercepted.",
                                fontSize = 8.5.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredLogs) { log ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.timestamp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = TextMuted,
                                            modifier = Modifier.weight(1.2f)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (log.type == "odds") Color(0xFF6366F1).copy(0.12f) else Color(0xFF06B6D4).copy(0.12f))
                                                .border(0.5.dp, if (log.type == "odds") Color(0xFF6366F1).copy(0.3f) else Color(0xFF06B6D4).copy(0.3f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = log.type.uppercase(),
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (log.type == "odds") Color(0xFF818CF8) else Color(0xFF22D3EE)
                                            )
                                        }

                                        Text(
                                            text = "FIX_#${log.fixtureId}",
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            color = TextLight,
                                            modifier = Modifier.weight(1.2f)
                                        )

                                        Text(
                                            text = "${log.latency} ms",
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (log.latency > 150) Color(0xFFF59E0B) else TextLight,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Row(
                                            modifier = Modifier.weight(3.5f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (log.status == "SUCCESS") {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "OK",
                                                    tint = NeonGreen,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                                Text(
                                                    text = "Upsert complete. Public cache cleared.",
                                                    fontSize = 8.sp,
                                                    color = NeonGreen
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Error",
                                                    tint = LightRed,
                                                    modifier = Modifier.size(9.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = "HTTP 500: Ingestion Aborted",
                                                        fontSize = 8.sp,
                                                        color = LightRed,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = log.msg ?: "",
                                                        fontSize = 7.sp,
                                                        color = TextMuted,
                                                        lineHeight = 9.sp,
                                                        maxLines = 1,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Divider(color = BorderColor.copy(0.15f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Footer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF060910))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Security handshaking signature validated by standard broker key mappings.",
                        fontSize = 7.5.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Sample window buffer pool: Auto-flushing old arrays",
                        fontSize = 7.5.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun RapidBetQueueMonitor(viewModel: BetViewModel) {
    val NeonBlue = Color(0xFF00C2FF)
    val isConsumerActive by viewModel.isQueueConsumerActive.collectAsState()
    val queueLogs by viewModel.queueLogs.collectAsState()
    val isDeadlockActive by viewModel.simulatedDbDeadlock.collectAsState()
    val prefetchLimit by viewModel.queuePrefetchLimit.collectAsState()
    val activeQueueSize by viewModel.activeQueueSize.collectAsState()
    val totalQueuedCount by viewModel.totalBetsQueuedCount.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "RabbitMQ Wager Processing Cluster",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isConsumerActive) NeonGreen.copy(0.12f) else Color(0xFFEA580C).copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isConsumerActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(NeonGreen)
                                    )
                                }
                                Text(
                                    text = if (isConsumerActive) "CONSUMING ACTIVE" else "CONSUMER PAUSED",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isConsumerActive) NeonGreen else Color(0xFFF97316)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Visualizes Node.js BetQueueEngine state with amqp/RabbitMQ sportsbook_bets_queue.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { viewModel.injectRapidWagers(10) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonBlue.copy(0.12f),
                            contentColor = NeonBlue
                        ),
                        border = BorderStroke(1.dp, NeonBlue.copy(0.3f)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(text = "Burst 10 Bets", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aggregates Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Buffer Queue size
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "BUFFER QUEUE",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$activeQueueSize msgs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (activeQueueSize > 0) Color(0xFF38BDF8) else TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "sportsbook_bets_queue",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Prefetch Limit
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "PREFETCH RATE",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$prefetchLimit msgs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Limit per worker node",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Total Placed
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "TOTAL PROCESSED",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalQueuedCount wagers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Total cumulative wagers",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }

                // Database Lock Mode
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .background(Color(0xFF0F172A).copy(0.4f), RoundedCornerShape(8.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "TRANSACTION ISOLATION",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isDeadlockActive) "CONFLICT DEADLOCK" else "ISOLATED READ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDeadlockActive) LightRed else NeonGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isDeadlockActive) "Auto NACK & Requeue" else "Commit successful",
                        fontSize = 6.5.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Control Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle queue Consumer
                Button(
                    onClick = { viewModel.toggleQueueConsumer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConsumerActive) Color(0xFFD97706).copy(0.12f) else NeonGreen.copy(0.12f),
                        contentColor = if (isConsumerActive) Color(0xFFF59E0B) else NeonGreen
                    ),
                    border = BorderStroke(1.dp, if (isConsumerActive) Color(0xFFF59E0B).copy(0.3f) else NeonGreen.copy(0.3f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                ) {
                    Text(
                        text = if (isConsumerActive) "🔌 Pause Consumer" else "⚡ Resume Consumer",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Toggle DB deadlocks simulation
                Button(
                    onClick = { viewModel.toggleSimulatedDbDeadlock() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeadlockActive) Color.Black.copy(0.3f) else LightRed.copy(0.12f),
                        contentColor = if (isDeadlockActive) TextWhite else LightRed
                    ),
                    border = BorderStroke(1.dp, if (isDeadlockActive) BorderColor else LightRed.copy(0.3f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(30.dp)
                ) {
                    Text(
                        text = if (isDeadlockActive) "Disable Deadlocks" else "⚠️ Simulate Deadlocks",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Prefetch Limit options
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                        .padding(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(50, 100, 200).forEach { limit ->
                        val isSel = prefetchLimit == limit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) NeonBlue.copy(0.15f) else Color.Transparent)
                                .border(0.5.dp, if (isSel) NeonBlue else Color.Transparent, RoundedCornerShape(4.dp))
                                .clickable { viewModel.updatePrefetchLimit(limit) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$limit",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) NeonBlue else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Event stream logs list (AMQP Terminal Interface)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            ) {
                // Table Sub-header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0C101B))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Broker",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "AMQP Broker Client Connection Terminal (prefetch=$prefetchLimit)",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    Text(
                        text = "sportsbook_bets_queue",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9F0A)
                    )
                }

                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF060910))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "TIMESTAMP", modifier = Modifier.weight(1.1f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "USER_ID", modifier = Modifier.weight(0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "SELECTION", modifier = Modifier.weight(1.5f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "STAKE (ETB)", modifier = Modifier.weight(1.1f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "RISK SERVICE", modifier = Modifier.weight(1.6f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "DB TRANSACTION", modifier = Modifier.weight(1.8f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "AMQP ACTION", modifier = Modifier.weight(1.4f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Divider(color = BorderColor, thickness = 0.5.dp)

                // List Items
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (queueLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active wager frames processed through RabbitMQ Bet Queue Engine yet.\nClick 'Burst 10 Bets' to populate simulated high-concurrency traffic.",
                                fontSize = 8.5.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(queueLogs) { log ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = log.timestamp,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.5.sp,
                                            color = TextMuted,
                                            modifier = Modifier.weight(1.1f)
                                        )

                                        Text(
                                            text = "#${log.userId}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.5.sp,
                                            color = TextLight,
                                            modifier = Modifier.weight(0.7f)
                                        )

                                        Column(modifier = Modifier.weight(1.5f)) {
                                            Text(
                                                text = log.teams,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Selection: ${log.selection} @ ${log.odds}",
                                                fontSize = 7.sp,
                                                color = TextMuted
                                            )
                                        }

                                        Text(
                                            text = String.format(java.util.Locale.US, "%,.1f", log.stake),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            modifier = Modifier.weight(1.1f)
                                        )

                                        // Risk Status
                                        Box(
                                            modifier = Modifier
                                                .weight(1.6f)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(
                                                    if (log.riskStatus == "APPROVED") NeonGreen.copy(0.1f)
                                                    else if (log.riskStatus.startsWith("PENDING")) Color(0xFFF59E0B).copy(0.1f)
                                                    else LightRed.copy(0.1f)
                                                )
                                                .border(
                                                    0.5.dp,
                                                    if (log.riskStatus == "APPROVED") NeonGreen.copy(0.3f)
                                                    else if (log.riskStatus.startsWith("PENDING")) Color(0xFFF59E0B).copy(0.3f)
                                                    else LightRed.copy(0.3f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = log.riskStatus,
                                                fontSize = 6.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (log.riskStatus == "APPROVED") NeonGreen
                                                else if (log.riskStatus.startsWith("PENDING")) Color(0xFFF59E0B)
                                                else LightRed
                                            )
                                        }

                                        // DB Transaction Status
                                        Row(
                                            modifier = Modifier.weight(1.8f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dbColor = if (log.dbStatus.contains("COMMITTED")) NeonGreen
                                            else if (log.dbStatus == "PENDING" || log.dbStatus.contains("Queue")) Color(0xFF94A3B8)
                                            else LightRed

                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(RoundedCornerShape(50.dp))
                                                    .background(dbColor)
                                            )
                                            Text(
                                                text = log.dbStatus,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 7.sp,
                                                color = dbColor,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        // RabbitMQ action
                                        Row(
                                            modifier = Modifier.weight(1.4f),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val actionColor = if (log.rabbitMqAction.startsWith("ACK")) NeonGreen
                                            else if (log.rabbitMqAction.startsWith("NACK")) LightRed
                                            else Color(0xFFF59E0B)

                                            Text(
                                                text = log.rabbitMqAction,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Black,
                                                color = actionColor,
                                                fontFamily = FontFamily.Monospace
                                            )

                                            if (log.latency > 0) {
                                                Text(
                                                    text = "(${log.latency}ms)",
                                                    fontSize = 6.5.sp,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }
                                    Divider(color = BorderColor.copy(0.15f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // Footer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF060910))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RabbitMQ cluster queue durable and synchronized with local Wallets & Bets schema.",
                        fontSize = 7.5.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Buffered Message Prefetch Cap: 100",
                        fontSize = 7.5.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun CasinoAggregatorBridgeMonitor(viewModel: BetViewModel) {
    val NeonBlue = Color(0xFF00C2FF)
    val NeonPurple = Color(0xFF9D4EDD)
    val isOffline by viewModel.casinoAggregatorOffline.collectAsState()
    val secretKey by viewModel.casinoSecretKey.collectAsState()
    val handshakeResult by viewModel.casinoHandshakeResult.collectAsState()
    val aviatorMultiplier by viewModel.aviatorMultiplier.collectAsState()

    var customUserId by remember { mutableStateOf("1") }
    var customUserIp by remember { mutableStateOf("197.156.12.84") }
    var selectedGameSlug by remember { mutableStateOf("book-of-ra") }
    var showSecretEdit by remember { mutableStateOf(false) }
    var tempSecret by remember { mutableStateOf(secretKey) }

    val gamesList = listOf(
        "book-of-ra" to "Book of Ra 🏺",
        "crazy-time" to "Crazy Time 🎡",
        "sweet-bonanza" to "Sweet Bonanza 🍭",
        "aviator" to "Aviator ✈️ (Native Fallback)"
    )

    Card(
        modifier = Modifier.fillMaxWidth().testTag("casino_aggregator_bridge_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, NeonPurple.copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🔌 Casino Aggregator Handshake Bridge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (!isOffline) NeonGreen.copy(0.12f) else LightRed.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(if (!isOffline) NeonGreen else LightRed)
                                )
                                Text(
                                    text = if (!isOffline) "AGGREGATOR ONLINE" else "OFFLINE (FORCE FALLBACK)",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOffline) NeonGreen else LightRed
                                )
                            }
                        }
                    }
                    Text(
                        text = "Visualizes Node.js CasinoAggregatorBridge backend service & HMAC signatures with 3000+ slots",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Config Form Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "HANDSHAKE SUITE INPUT CONFIGURATION",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // User ID and IP Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "USER ID", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = customUserId,
                            onValueChange = { customUserId = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(2.3f)) {
                        Text(text = "PLAYER IP ADDRESS", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = customUserIp,
                            onValueChange = { customUserIp = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secret Key Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "AGGREGATOR_SECRET_KEY (HMAC)", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (showSecretEdit) "Save Key" else "Change Key",
                            fontSize = 8.sp,
                            color = NeonBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                if (showSecretEdit) {
                                    viewModel.updateCasinoSecretKey(tempSecret)
                                } else {
                                    tempSecret = secretKey
                                }
                                showSecretEdit = !showSecretEdit
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    if (showSecretEdit) {
                        OutlinedTextField(
                            value = tempSecret,
                            onValueChange = { tempSecret = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "••••••••••••••••" + secretKey.takeLast(4),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Game selection chips
                Text(
                    text = "TARGET CASINO GAME SLUG",
                    fontSize = 7.5.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    gamesList.forEach { (slug, name) ->
                        val isSel = selectedGameSlug == slug
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) NeonPurple.copy(0.18f) else Color.Transparent)
                                .border(0.5.dp, if (isSel) NeonPurple else BorderColor, RoundedCornerShape(6.dp))
                                .clickable { selectedGameSlug = slug }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) NeonPurple else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Online/Offline switch button
                Button(
                    onClick = { viewModel.toggleCasinoAggregatorOffline() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOffline) LightRed.copy(0.12f) else SlateSurfaceL2,
                        contentColor = if (isOffline) LightRed else TextLight
                    ),
                    border = BorderStroke(1.dp, if (isOffline) LightRed.copy(0.4f) else BorderColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1.2f).height(32.dp)
                ) {
                    Text(
                        text = if (isOffline) "🔌 FORCE OFFLINE MODE" else "🟢 NORMAL HANDSHAKE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Launch Trigger Button
                Button(
                    onClick = {
                        val parsedId = customUserId.toIntOrNull() ?: 1
                        viewModel.executeCasinoHandshake(parsedId, customUserIp, selectedGameSlug)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple.copy(0.2f),
                        contentColor = NeonPurple
                    ),
                    border = BorderStroke(1.dp, NeonPurple.copy(0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.weight(1.5f).height(32.dp)
                ) {
                    Text(
                        text = "⚡ HANDSHAKE & LAUNCH",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Results terminal / Visual simulation
            handshakeResult?.let { res ->
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left Column: The terminal connection trace logs
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "CONSOLE TELEMETRY OUTPUT LOGS",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val scrollState = rememberScrollState()
                        Box(modifier = Modifier.height(150.dp).fillMaxWidth().verticalScroll(scrollState)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                res.logs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 7.sp,
                                        lineHeight = 10.sp,
                                        color = if (log.contains("❌")) LightRed else if (log.contains("✅")) NeonGreen else if (log.contains("🕒")) TextMuted else TextLight
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: The Visual Embedded Screen Iframe simulation!
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .border(0.5.dp, NeonPurple.copy(0.35f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "EMBED IFRAME SIMULATION",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (res.isFallback) {
                                // Fallback aviator graphics!
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "✈️ AVIATOR (FALLBACK)",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LightRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${aviatorMultiplier}x",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                    Text(
                                        text = "Ticking live virtual game loop",
                                        fontSize = 6.sp,
                                        color = TextMuted
                                    )
                                }
                            } else {
                                // Aggregator remote loaded screen!
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = null,
                                        tint = NeonPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = selectedGameSlug.replace("-", " ").uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextWhite
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(NeonGreen.copy(0.12f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "HTTP 200 IFRAME RUNNING",
                                            fontSize = 6.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "LAUNCH PATH:",
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = res.finalLaunchUrl,
                            fontSize = 6.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (res.isFallback) LightRed else NeonBlue,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Request/Response Code Block inspect
                var inspectTab by remember { mutableStateOf("REQUEST") } // "REQUEST", "RESPONSE"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030508), RoundedCornerShape(6.dp))
                        .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C101B)),
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        listOf("REQUEST", "RESPONSE").forEach { tab ->
                            val isSel = inspectTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) Color(0xFF030508) else Color(0xFF090D18))
                                    .clickable { inspectTab = tab }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tab == "REQUEST") "📡 POST REQUEST BODY" else "📝 RESPONSE PAYLOAD",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) NeonBlue else TextMuted
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (inspectTab == "REQUEST") res.requestBody else res.responseBody,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            lineHeight = 10.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }
    }
}

data class KpiData(
    val title: String,
    val value: String,
    val change: String,
    val up: Boolean,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun GatewayRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Text(
            text = value, 
            fontSize = 9.sp, 
            color = if (value.contains("LOADED") || value.contains("ACTIVE")) NeonGreen else TextLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false).padding(start = 14.dp)
        )
    }
}

@Composable
fun SupportChatScreen(viewModel: BetViewModel, modifier: Modifier = Modifier) {
    val messages by viewModel.supportMessages.collectAsState()
    val isAgentTyping by viewModel.isAgentTyping.collectAsState()
    var typedMessage by remember { mutableStateOf("") }
    
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to latest messages on list size change
    LaunchedEffect(messages.size, isAgentTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDarkBG)
    ) {
        // Chat Header Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(38.dp).background(Color(0xFF00C2FF).copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Support Agent",
                            tint = Color(0xFF00C2FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BETMASTER SUPPORT CONCIERGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Text(
                            text = "Sara & Marcus ● Premium Desk Active",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SECURED (AES-256)",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // Quick Assistant Recommendation Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Verify payment callbackStatus",
                "How are Multi-Bet odds calculated?",
                "Slippage guidelines for sport wagers",
                "Active betting slip dispute settlement"
            ).forEach { query ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlateSurfaceL2)
                        .border(0.5.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.sendSupportMessage(query)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color(0xFF00C2FF), modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = query, color = TextLight, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Messages Box Scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    val isMyMessage = message.sender == "USER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = message.senderName,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMyMessage) 12.dp else 2.dp,
                                    bottomEnd = if (isMyMessage) 2.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMyMessage) PrimarySapphire else SlateSurfaceL2
                                ),
                                border = if (isMyMessage) null else BorderStroke(0.5.dp, BorderColor)
                            ) {
                                Text(
                                    text = message.text,
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
                
                if (isAgentTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text("Sara is composing...", fontSize = 8.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SlateSurfaceL2)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        repeat(3) {
                                            Box(modifier = Modifier.size(5.dp).background(Color(0xFF00C2FF), CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Message Input Footer
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SlateCardBG,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = typedMessage,
                    onValueChange = { typedMessage = it },
                    placeholder = { Text("Ask about betting rules or callback status...", fontSize = 10.sp, color = TextMuted) },
                    textStyle = TextStyle(color = TextWhite, fontSize = 11.sp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = Color(0xFF030508),
                        unfocusedContainerColor = Color(0xFF030508),
                        focusedBorderColor = Color(0xFF00C2FF),
                        unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PrimarySapphire)
                        .clickable {
                            if (typedMessage.trim().isNotEmpty()) {
                                viewModel.sendSupportMessage(typedMessage)
                                typedMessage = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminOddsAndLocksController(
    viewModel: BetViewModel,
    modifier: Modifier = Modifier
) {
    val allMatches by viewModel.allMatches.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🏆 MASTER ODDS & LOCKS CONTROLLER",
                        fontSize = 11.s_p,
                        fontWeight = FontWeight.Black,
                        color = AmberAccent,
                        letterSpacing = 0.5.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "As Super-Admin, you can manually lock match odds or adjust them. Changing the odds of a locked match will automatically release its lock!",
                    fontSize = 10.s_p,
                    color = TextMuted,
                    lineHeight = 13.s_p
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allMatches.forEach { match ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SlateSurfaceL2, RoundedCornerShape(8.dp))
                                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${match.teamA} vs ${match.teamB}",
                                    fontSize = 11.s_p,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${match.sport.uppercase()}  •  Home: ${match.odds1} | Draw: ${match.oddsX} | Away: ${match.odds2}",
                                        fontSize = 9.s_p,
                                        color = TextMuted
                                    )
                                    if (match.isLocked) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "LOCKED",
                                            fontSize = 9.s_p,
                                            fontWeight = FontWeight.Bold,
                                            color = LightRed
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Manual Lock/Unlock button
                                Button(
                                    onClick = {
                                        val updated = match.copy(isLocked = !match.isLocked)
                                        viewModel.updateMatchDirectly(updated)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (match.isLocked) Color(0xFFEF4444).copy(alpha = 0.15f) else SlateDarkBG,
                                        contentColor = if (match.isLocked) Color(0xFFEF4444) else TextLight
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, if (match.isLocked) Color(0xFFEF4444) else BorderColor),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (match.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                            contentDescription = null,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = if (match.isLocked) "Unlock" else "Lock", fontSize = 9.s_p, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Odds change button: e.g. Home Odds + 0.10
                                Button(
                                    onClick = {
                                        // Update odds and RELEASE LOCK automatically!
                                        val updatedOdds = match.odds1 + 0.10
                                        val updated = match.copy(
                                            odds1 = Math.round(updatedOdds * 100.0) / 100.0,
                                            isLocked = false // automatically unlock!
                                        )
                                        viewModel.updateMatchDirectly(updated)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimarySapphire.copy(alpha = 0.15f),
                                        contentColor = PrimarySapphire
                                    ),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.5.dp, PrimarySapphire),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(text = "Odd1 +0.1", fontSize = 9.s_p, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AntiFraudDeviceEngineMonitor(viewModel: BetViewModel) {
    val NeonBlue = Color(0xFF00C2FF)
    val NeonPurple = Color(0xFF9D4EDD)
    
    val deviceMappings by viewModel.deviceMappings.collectAsState()
    val auditResult by viewModel.fraudAuditResult.collectAsState()

    var userIdInput by remember { mutableStateOf("555") }
    var usernameInput by remember { mutableStateOf("Sintayehu_BonusAbuser") }
    var deviceFingerprintInput by remember { mutableStateOf("HW_HASH_SHA256_A7B9") }
    var ipAddressInput by remember { mutableStateOf("197.156.12.80") }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("anti_fraud_device_engine_card"),
        colors = CardDefaults.cardColors(containerColor = SlateCardBG),
        border = BorderStroke(1.dp, NeonPurple.copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🛡️ Anti-Fraud Device Integrity Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonPurple.copy(0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "POSTGRES PG.POOL ACTIVE",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple
                            )
                        }
                    }
                    Text(
                        text = "Prevents multi-accounting syndicate bonus abuse by inspecting shared hardware environment footprints.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Presets and Simulator configuration form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "SECURITY FINGERPRINT TESTING CONFIG",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Presets Quick Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            userIdInput = "305"
                            usernameInput = "Kidist_Tesfaye"
                            deviceFingerprintInput = "HW_HASH_SHA256_F923"
                            ipAddressInput = "197.156.45.12"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen.copy(0.12f),
                            contentColor = NeonGreen
                        ),
                        border = BorderStroke(0.5.dp, NeonGreen.copy(0.3f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1f).height(24.dp)
                    ) {
                        Text("🟢 Preset: Clean Profile", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            userIdInput = "555"
                            usernameInput = "Sintayehu_BonusAbuser"
                            deviceFingerprintInput = "HW_HASH_SHA256_A7B9"
                            ipAddressInput = "197.156.12.80"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightRed.copy(0.12f),
                            contentColor = LightRed
                        ),
                        border = BorderStroke(0.5.dp, LightRed.copy(0.3f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.weight(1.2f).height(24.dp)
                    ) {
                        Text("🚨 Preset: Syndicate Attack", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // User ID and Username inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "USER ID", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = userIdInput,
                            onValueChange = { userIdInput = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(2f)) {
                        Text(text = "PROFILE USERNAME", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.5.sp),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fingerprint and IP Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(text = "DEVICE HARDWARE FINGERPRINT (HASH)", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = deviceFingerprintInput,
                            onValueChange = { deviceFingerprintInput = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(text = "IP ADDRESS", fontSize = 7.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(3.dp))
                        OutlinedTextField(
                            value = ipAddressInput,
                            onValueChange = { ipAddressInput = it },
                            textStyle = TextStyle(color = TextWhite, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = Color.Black,
                                unfocusedContainerColor = Color.Black,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Run Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Database Button
                Button(
                    onClick = { viewModel.resetFraudDatabase() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = TextLight
                    ),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text(text = "RESET DB", fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Verify Action Button
                Button(
                    onClick = {
                        val pId = userIdInput.toIntOrNull() ?: 555
                        viewModel.verifyDeviceIntegrity(pId, deviceFingerprintInput, ipAddressInput, usernameInput)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple.copy(0.2f),
                        contentColor = NeonPurple
                    ),
                    border = BorderStroke(1.dp, NeonPurple.copy(0.5f)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.weight(1f).height(32.dp)
                ) {
                    Text(
                        text = "🛡️ RUN HARDWARE AUDIT VERIFY",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Results Display block
            auditResult?.let { res ->
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (res.clean) NeonGreen.copy(0.04f) else LightRed.copy(0.04f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (res.clean) NeonGreen.copy(0.35f) else LightRed.copy(0.35f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    // Result Header Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(if (res.clean) NeonGreen else LightRed)
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (res.clean) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (res.clean) "INTEGRITY CLEAR: DEVICE COMPLIANT" else "SECURITY ALERT: MULTI-ACCOUNT DETECTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (res.clean) NeonGreen else LightRed
                                )
                                Text(
                                    text = "AntiFraudDeviceEngine results for target user handshake audit.",
                                    fontSize = 8.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    if (!res.clean) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.4f), RoundedCornerShape(6.dp))
                                .border(0.5.dp, LightRed.copy(0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "REJECTION REASON:",
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LightRed
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = res.reason ?: "",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    // Syndicate accounts grid list
                    if (res.matchedDuplicateUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🔗 SHARING HARDWARE FOOTPRINT (30-DAY WINDOW):",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (res.clean) TextMuted else LightRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            res.matchedDuplicateUsers.forEach { other ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(0.15f), RoundedCornerShape(4.dp))
                                        .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(if (other.isFlagged) LightRed else NeonBlue)
                                        )
                                        Column {
                                            Text(
                                                text = "${other.username} (User #${other.userId})",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "IP: ${other.ipAddress} | Hash: ${other.deviceFingerprint}",
                                                fontSize = 7.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${other.createdAtDaysAgo} days ago",
                                        fontSize = 7.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Raw query trace inspect
                    Text(
                        text = "💻 EXECUTED POSTGRESQL MULTI-ACCOUNTING STATEMENT",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF030508), RoundedCornerShape(6.dp))
                            .border(0.5.dp, BorderColor, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = res.queryTrace,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.5.sp,
                            lineHeight = 11.sp,
                            color = Color(0xFFA5B4FC)
                        )
                    }
                }
            }

            // Database Terminal Logs / Connections
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030508), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🖥️ POSTGRES SYNDICATE DATABASE CONSOLE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF59E0B)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val logsList = auditResult?.logs ?: listOf(
                    "🕒 [AntiFraud Core] Database listener ready. Waiting for hardware device integrity test checks...",
                    "📊 Benchmark threshold: Duplicate account registrations count matching identical hardware hash >= 3."
                )

                Box(modifier = Modifier.height(80.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        logsList.forEach { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 7.sp,
                                lineHeight = 9.5.sp,
                                color = if (log.contains("🚨") || log.contains("UPDATE")) LightRed
                                        else if (log.contains("🟢") || log.contains("✅") || log.contains("OK")) NeonGreen
                                        else if (log.contains("🕒")) TextMuted
                                        else TextLight
                            )
                        }
                    }
                }
            }

            // Live Postgres Device Registry Table
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C101B), RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
            ) {
                // Directory Title Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF060910))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📁 LIVE DEVICE_HARDWARE_HASH DB INDEX",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${deviceMappings.size} REGISTRATIONS",
                        fontSize = 7.sp,
                        color = NeonPurple,
                        fontWeight = FontWeight.Black
                    )
                }

                // Table Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030508))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "USER ID", modifier = Modifier.weight(0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "USERNAME", modifier = Modifier.weight(1.3f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "HARDWARE HASH", modifier = Modifier.weight(1.8f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "IP ADDRESS", modifier = Modifier.weight(1.2f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "AGE", modifier = Modifier.weight(0.7f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = "STATUS", modifier = Modifier.weight(1f), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = TextMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }

                Divider(color = BorderColor.copy(0.15f), thickness = 0.5.dp)

                // Table rows list
                Box(modifier = Modifier.height(130.dp).fillMaxWidth()) {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(deviceMappings) { row ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "#${row.userId}", modifier = Modifier.weight(0.7f), fontSize = 7.5.sp, fontFamily = FontFamily.Monospace, color = TextLight)
                                    Text(
                                        text = row.username,
                                        modifier = Modifier.weight(1.3f),
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = row.deviceFingerprint,
                                        modifier = Modifier.weight(1.8f),
                                        fontSize = 7.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeonBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = row.ipAddress, modifier = Modifier.weight(1.2f), fontSize = 7.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                    Text(text = "${row.createdAtDaysAgo}d ago", modifier = Modifier.weight(0.7f), fontSize = 7.sp, color = TextMuted)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (row.isFlagged) LightRed.copy(0.12f) else NeonGreen.copy(0.12f)
                                            )
                                            .border(
                                                0.5.dp,
                                                if (row.isFlagged) LightRed.copy(0.4f) else NeonGreen.copy(0.4f),
                                                RoundedCornerShape(3.dp)
                                            )
                                            .padding(vertical = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (row.isFlagged) "FLAGGED" else "COMPLIANT",
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (row.isFlagged) LightRed else NeonGreen
                                        )
                                    }
                                }
                                Divider(color = BorderColor.copy(0.1f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
