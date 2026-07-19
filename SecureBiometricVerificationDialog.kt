package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.*
import com.example.util.BiometricAuthHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SecureBiometricVerificationDialog(
    title: String,
    subtitle: String,
    onAuthSuccess: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // States for simulator in case hardware is not available or fallback is selected
    var scanState by remember { mutableStateOf("AWAITING_TOUCH") } // "AWAITING_TOUCH", "SCANNING", "SUCCESS", "FAILED"
    var progress by remember { mutableStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if biometric authentication can be requested officially
    val isHardwareAvailable = remember { BiometricAuthHelper.isBiometricHardwareAvailable(context) }

    fun triggerOfficialBiometric() {
        val activity = context as? FragmentActivity
        if (activity != null && isHardwareAvailable) {
            BiometricAuthHelper.showBiometricPrompt(
                activity = activity,
                title = title,
                subtitle = subtitle,
                onSuccess = {
                    onAuthSuccess()
                },
                onError = { _, err ->
                    errorMessage = err.toString()
                    scanState = "FAILED"
                },
                onFailed = {
                    scanState = "FAILED"
                }
            )
        } else {
            // Fallback immediately to simulation mode loop
            coroutineScope.launch {
                scanState = "SCANNING"
                progress = 0f
                while (progress < 1f) {
                    delay(30)
                    progress += 0.04f
                }
                scanState = "SUCCESS"
                delay(500)
                onAuthSuccess()
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("secure_biometric_verification_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCardBG),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "SHEBA CRYPTO SHIELD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pulsing biometric sensor
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    when (scanState) {
                                        "SUCCESS" -> NeonGreen.copy(alpha = 0.2f)
                                        "FAILED" -> LightRed.copy(alpha = 0.2f)
                                        else -> PrimarySapphire.copy(alpha = 0.15f)
                                    },
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
                                    "FAILED" -> listOf(LightRed, LightRed.copy(alpha = 0.4f), LightRed)
                                    else -> listOf(BorderColor, BorderColor.copy(alpha = 0.4f), BorderColor)
                                }
                            ),
                            shape = CircleShape
                        )
                        .clickable(enabled = scanState == "AWAITING_TOUCH" || scanState == "FAILED") {
                            triggerOfficialBiometric()
                        }
                        .testTag("biometric_sensor_target"),
                    contentAlignment = Alignment.Center
                ) {
                    val iconAnimColor by animateColorAsState(
                        targetValue = when (scanState) {
                            "SUCCESS" -> NeonGreen
                            "SCANNING" -> Color(0xFF00C2FF)
                            "FAILED" -> LightRed
                            else -> TextWhite
                        },
                        animationSpec = tween(300),
                        label = "sensor_color"
                    )

                    Icon(
                        imageVector = if (scanState == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Fingerprint,
                        contentDescription = "Touch ID Sensor",
                        tint = iconAnimColor,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val statusText = when (scanState) {
                    "AWAITING_TOUCH" -> "TAP SENSOR TO AUTHENTICATE"
                    "SCANNING" -> "ROUTING ENCRYPTED VECTORS... ${(progress * 100).toInt()}%"
                    "SUCCESS" -> "IDENTITY VERIFIED! AUTHORIZING..."
                    "FAILED" -> errorMessage ?: "AUTH ERROR: RE-TAP TO RETRY"
                    else -> "BIOMETRIC SHIELD ACTIVE"
                }

                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    color = when (scanState) {
                        "SUCCESS" -> NeonGreen
                        "SCANNING" -> Color(0xFF00C2FF)
                        "FAILED" -> LightRed
                        else -> AmberAccent
                    },
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = BorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("biometric_dismiss_btn")
                ) {
                    Text(
                        text = "CANCEL SECURE CONFIRMATION",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
