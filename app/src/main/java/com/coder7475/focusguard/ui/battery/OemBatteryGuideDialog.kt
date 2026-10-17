package com.coder7475.focusguard.ui.battery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class OemBatteryGuide(
    val manufacturerName: String,
    val title: String,
    val steps: List<String>
)

object OemDetector {
    fun getGuideForDevice(): OemBatteryGuide {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                OemBatteryGuide(
                    manufacturerName = "Xiaomi / Redmi (MIUI & HyperOS)",
                    title = "Keep FocusGuard Active on Xiaomi",
                    steps = listOf(
                        "1. Open App Info for FocusGuard and enable 'Autostart'.",
                        "2. Tap 'Battery saver' and select 'No restrictions'.",
                        "3. In Recent Apps, lock FocusGuard with the padlock icon."
                    )
                )
            }
            manufacturer.contains("samsung") -> {
                OemBatteryGuide(
                    manufacturerName = "Samsung (One UI)",
                    title = "Keep FocusGuard Active on Samsung",
                    steps = listOf(
                        "1. Go to Settings > Device care > Battery > Background usage limits.",
                        "2. Add FocusGuard to 'Never sleeping apps'.",
                        "3. In App Info > Battery, set to 'Unrestricted'."
                    )
                )
            }
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                OemBatteryGuide(
                    manufacturerName = "OnePlus / Oppo (OxygenOS & ColorOS)",
                    title = "Keep FocusGuard Active on OnePlus",
                    steps = listOf(
                        "1. Go to App Info > Battery usage.",
                        "2. Enable 'Allow background activity' and 'Allow auto-launch'.",
                        "3. Turn off battery optimization for FocusGuard."
                    )
                )
            }
            else -> {
                OemBatteryGuide(
                    manufacturerName = "Android",
                    title = "Unrestricted Battery Background Setting",
                    steps = listOf(
                        "1. Open App Info for FocusGuard.",
                        "2. Tap 'App battery usage'.",
                        "3. Select 'Unrestricted' so background timers remain accurate."
                    )
                )
            }
        }
    }
}

@Composable
fun OemBatteryGuideDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val guide = OemDetector.getGuideForDevice()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = guide.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Aggressive background battery savers on ${guide.manufacturerName} can terminate focus timers prematurely. Follow these steps:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                guide.steps.forEach { step ->
                    Text(
                        text = step,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    openAppInfoSettings(context)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open App Info", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Dismiss", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    )
}

private fun openAppInfoSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
