package com.coder7475.focusguard.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coder7475.focusguard.data.local.FocusGuardDatabase
import com.coder7475.focusguard.data.repository.RoomSessionRepository
import com.coder7475.focusguard.domain.engine.SessionEngine
import com.coder7475.focusguard.domain.model.FocusSession
import com.coder7475.focusguard.domain.model.SessionStatus
import com.coder7475.focusguard.platform.alarm.AlarmCoordinator
import com.coder7475.focusguard.service.foreground.FocusGuardService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
//@Preview(showBackground = true)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { FocusGuardDatabase.getInstance(context) }
    val sessionRepo = remember { RoomSessionRepository(db.sessionDao()) }
    val sessionEngine = remember { SessionEngine(sessionRepo) }
    val alarmCoordinator = remember { AlarmCoordinator(context) }

    val activeSession by sessionRepo.observeActiveSession().collectAsState(initial = null)
    val completedCount by sessionRepo.observeCompletedSessionCount().collectAsState(initial = 0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B132B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FocusGuard",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Un-bypassable Discipline Lock",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$completedCount",
                            color = Color(0xFF38BDF8),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completed",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val current = activeSession
            if (current != null && current.status == SessionStatus.ACTIVE) {
                ActiveSessionCard(
                    session = current,
                    onCancelClick = {
                        coroutineScope.launch {
                            sessionEngine.completeActiveSession(current.id)
                            alarmCoordinator.cancelSessionCompletion(current.id)
                            FocusGuardService.stop(context)
                        }
                    }
                )
            } else {
                Text(
                    text = "Quick Start Focus Session",
                    color = Color(0xFFE2E8F0),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val presets = listOf(
                    15 to "Quick Sprint",
                    25 to "Pomodoro",
                    45 to "Deep Focus",
                    60 to "Ultra Discipline"
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets.size) { index ->
                        val (minutes, label) = presets[index]
                        QuickStartCard(
                            minutes = minutes,
                            label = label,
                            onStart = {
                                coroutineScope.launch {
                                    val durationMs = minutes * 60 * 1000L
                                    val result = sessionEngine.startQuickSession(durationMs)
                                    val session = result.getOrNull()
                                    if (session != null) {
                                        alarmCoordinator.scheduleSessionCompletion(
                                            session.endsAtEpochMs,
                                            session.id
                                        )
                                        FocusGuardService.start(context, session.endsAtEpochMs)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(
    session: FocusSession,
    onCancelClick: () -> Unit
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(500L)
        }
    }

    val remainingMs = max(0L, session.endsAtEpochMs - currentTimeMs)
    val seconds = remainingMs / 1000L
    val m = (seconds % 3600L) / 60L
    val s = seconds % 60L
    val formatted = String.format(Locale.US, "%02d:%02d", m, s)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ACTIVE SESSION IN PROGRESS",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatted,
                color = Color.White,
                fontSize = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Distracting apps are locked. Emergency & dialer permitted.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onCancelClick,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "End Session",
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun QuickStartCard(
    minutes: Int,
    label: String,
    onStart: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "$minutes Minutes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Start", fontWeight = FontWeight.Bold)
            }
        }
    }
}
