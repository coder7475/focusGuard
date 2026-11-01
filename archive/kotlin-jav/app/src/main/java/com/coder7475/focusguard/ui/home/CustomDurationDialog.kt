package com.coder7475.focusguard.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.ui.theme.BorderDark
import com.coder7475.focusguard.ui.theme.DangerRed
import com.coder7475.focusguard.ui.theme.DarkGunmetal
import com.coder7475.focusguard.ui.theme.ElectricCyan
import com.coder7475.focusguard.ui.theme.MatteCharcoal
import com.coder7475.focusguard.ui.theme.OffWhite
import com.coder7475.focusguard.ui.theme.SafetyOrange
import com.coder7475.focusguard.ui.theme.SignalAmber
import com.coder7475.focusguard.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDurationDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(initialMinutes.toString()) }
    val parsedMinutes = textValue.toIntOrNull()
    val isValid = parsedMinutes != null && parsedMinutes in 1..1440

    val errorMessage = when {
        textValue.isEmpty() -> "Duration cannot be empty"
        parsedMinutes == null -> "Please enter a valid number"
        parsedMinutes < 1 -> "Minimum duration is 1 minute"
        parsedMinutes > 1440 -> "Maximum duration is 1440 minutes (24 hours)"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        shape = RoundedCornerShape(12.dp),
        title = {
            Column {
                Text(
                    text = "CUSTOM FOCUS DURATION",
                    color = OffWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Enter direct session length in minutes (1–1440)",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(4)
                        textValue = digits
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null && textValue.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        cursorColor = SafetyOrange,
                        focusedBorderColor = SafetyOrange,
                        unfocusedBorderColor = BorderDark,
                        errorBorderColor = DangerRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (errorMessage != null && textValue.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else if (parsedMinutes != null && parsedMinutes in 1..1440) {
                    val hrs = parsedMinutes / 60
                    val mins = parsedMinutes % 60
                    val formatted = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                    Text(
                        text = "Duration: $formatted",
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedMinutes != null) {
                        onConfirm(parsedMinutes)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafetyOrange,
                    disabledContainerColor = BorderDark,
                    contentColor = MatteCharcoal,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "CONFIRM",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Text(
                    text = "CANCEL",
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}

@Composable
fun DisciplineWarningDialog(
    durationMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val hrs = durationMinutes / 60
    val mins = durationMinutes % 60
    val formattedDuration = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkGunmetal,
        shape = RoundedCornerShape(12.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚠️ HIGH DURATION DISCIPLINE",
                    color = SignalAmber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "You are committing to an extended $formattedDuration ($durationMinutes minutes) focus session.",
                    color = OffWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF261D15), RoundedCornerShape(8.dp))
                        .border(1.dp, SignalAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "FocusGuard strictly enforces an un-bypassable lock over restricted apps. Extended sessions require high discipline and cannot be dismissed once engaged.",
                        color = SignalAmber,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "COMMIT TO SESSION",
                    color = MatteCharcoal,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
            ) {
                Text(
                    text = "BACK",
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}
