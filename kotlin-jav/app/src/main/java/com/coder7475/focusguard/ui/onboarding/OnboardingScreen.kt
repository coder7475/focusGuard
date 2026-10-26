package com.coder7475.focusguard.ui.onboarding

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.coder7475.focusguard.data.preferences.FocusPreferences
import com.coder7475.focusguard.data.preferences.focusDataStore
import com.coder7475.focusguard.platform.permission.FocusPermission
import com.coder7475.focusguard.platform.permission.PermissionChecker
import com.coder7475.focusguard.platform.permission.PermissionTier
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.ElectricCyan
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.SignalAmber
import com.coder7475.focusguard.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val permissionChecker = remember { PermissionChecker(context) }
    val focusPreferences = remember { FocusPreferences(context.focusDataStore) }

    val permissionStates = remember {
        mutableStateMapOf<FocusPermission, Boolean>().apply {
            FocusPermission.entries.forEach { put(it, permissionChecker.isGranted(it)) }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                FocusPermission.entries.forEach { perm ->
                    permissionStates[perm] = permissionChecker.isGranted(perm)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mandatoryPermissions = remember { FocusPermission.entries.filter { it.isMandatory } }
    val mandatoryGrantedCount = mandatoryPermissions.count { permissionStates[it] == true }
    val allMandatoryGranted = mandatoryGrantedCount == mandatoryPermissions.size

    val progressFraction = if (mandatoryPermissions.isNotEmpty()) {
        (mandatoryGrantedCount.toFloat() / mandatoryPermissions.size.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 400),
        label = "onboardingProgressFraction"
    )

    // Sequential resolution: determine next ungranted mandatory permission
    val nextUngranted = remember(permissionStates.toMap()) {
        permissionChecker.getNextUngrantedMandatoryPermission()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatteCharcoal)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Branding Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "FOCUSGUARD",
                    color = OffWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(SafetyOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SETUP",
                        color = MatteCharcoal,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Establish an un-bypassable hardware discipline lock over distracting applications while preserving phone and emergency capabilities.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Progress Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SECURITY ENFORCEMENT SETUP",
                            color = OffWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}% READY",
                            color = if (allMandatoryGranted) ElectricCyan else SafetyOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = if (allMandatoryGranted) ElectricCyan else SafetyOrange,
                        trackColor = MatteCharcoal,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$mandatoryGrantedCount of ${mandatoryPermissions.size} Core Enforcers active",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tiered Permission List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val tiers = listOf(PermissionTier.CORE_ENFORCERS, PermissionTier.BACKGROUND_DURABILITY)

                tiers.forEach { tier ->
                    item(key = "header_${tier.name}") {
                        TierHeader(tier = tier)
                    }

                    val tierPermissions = FocusPermission.entries.filter { it.tier == tier }
                    items(
                        items = tierPermissions,
                        key = { it.name }
                    ) { permission ->
                        val isGranted = permissionStates[permission] == true
                        val isNextAction = permission == nextUngranted

                        PermissionStepCard(
                            permission = permission,
                            isGranted = isGranted,
                            isNextAction = isNextAction,
                            onGrantClick = {
                                val intent = permissionChecker.createIntentForPermission(permission)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Smart Action Button
            if (!allMandatoryGranted && nextUngranted != null) {
                Button(
                    onClick = {
                        val intent = permissionChecker.createIntentForPermission(nextUngranted)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "GRANT NEXT: ${nextUngranted.title.uppercase()}",
                        color = MatteCharcoal,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            focusPreferences.setOnboardingCompleted(true)
                            onOnboardingComplete()
                        }
                    },
                    enabled = allMandatoryGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafetyOrange,
                        disabledContainerColor = DarkGunmetal,
                        contentColor = MatteCharcoal,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "ENTER FOCUSGUARD",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TierHeader(
    tier: PermissionTier,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = tier.title.uppercase(),
                color = OffWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .background(
                        if (tier == PermissionTier.CORE_ENFORCERS) SafetyOrange.copy(alpha = 0.2f) else DarkGunmetal,
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        1.dp,
                        if (tier == PermissionTier.CORE_ENFORCERS) SafetyOrange else BorderDark,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (tier == PermissionTier.CORE_ENFORCERS) "MANDATORY" else "DURABILITY",
                    color = if (tier == PermissionTier.CORE_ENFORCERS) SafetyOrange else TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = tier.description,
            color = TextMuted,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun PermissionStepCard(
    permission: FocusPermission,
    isGranted: Boolean,
    isNextAction: Boolean,
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isGranted -> BorderDark
        isNextAction -> SafetyOrange
        else -> BorderDark
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkGunmetal),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor),
            width = if (isNextAction && !isGranted) 2.dp else 1.dp
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                color = if (isGranted) ElectricCyan else BorderDark,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isGranted) "✓" else "!",
                            color = MatteCharcoal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = permission.title,
                        color = OffWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (permission.isMandatory) {
                    Text(
                        text = "REQUIRED",
                        color = if (isGranted) ElectricCyan else SafetyOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = permission.description,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Android 13+ Restricted Settings Guidance for Accessibility
            if (!isGranted && permission == FocusPermission.ACCESSIBILITY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261D15)),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(SignalAmber.copy(alpha = 0.5f))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚠️ ANDROID 13+ RESTRICTED SETTINGS NOTICE",
                                color = SignalAmber,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If the system greys out Accessibility with 'Restricted setting':\n1. Tap 'Open App Info' below\n2. Tap the top-right 3 dots (⋮)\n3. Tap 'Allow restricted settings'\n4. Return here and tap 'Grant Permission'.",
                            color = OffWhite.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                val appInfoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = "package:${context.packageName}".toUri()
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(appInfoIntent)
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SignalAmber.copy(alpha = 0.6f)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "OPEN APP INFO",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isGranted) {
                OutlinedButton(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isNextAction) SafetyOrange else TextMuted
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNextAction) SafetyOrange else BorderDark
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (isNextAction) "GRANT PERMISSION →" else "GRANT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
