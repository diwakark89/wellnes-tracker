package com.thewalkersoft.tracker.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewalkersoft.tracker.domain.model.CycleStatus
import com.thewalkersoft.tracker.domain.model.PredictionResult
import com.thewalkersoft.tracker.ui.theme.*

private data class PhaseInsightContent(
    val phaseTitle: String,
    val phaseColor: Color,
    val phaseIcon: ImageVector,
    val conceptionLikelihood: String,
    val conceptionColor: Color,
    val hormoneSummary: String,
    val energyLevel: String,
    val tips: List<Pair<ImageVector, String>>
)

@Composable
fun PhaseInsightsCard(
    prediction: PredictionResult?,
    modifier: Modifier = Modifier
) {
    if (prediction == null) return

    val content = when (prediction.status) {
        CycleStatus.FOLLICULAR -> PhaseInsightContent(
            phaseTitle = "Follicular Phase",
            phaseColor = FollicularColor,
            phaseIcon = Icons.Default.Spa,
            conceptionLikelihood = "Low Chance",
            conceptionColor = FollicularColor,
            hormoneSummary = "Estrogen is steadily rising as your body prepares a follicle. You may feel heightened optimism and sharper mental clarity.",
            energyLevel = "Rising & Vibrant",
            tips = listOf(
                Icons.Default.FitnessCenter to "Great time for high-intensity training, strength workouts, and new projects.",
                Icons.Default.Restaurant to "Fuel with complex carbohydrates, fermented foods, and lean proteins.",
                Icons.Default.WbSunny to "Take advantage of increased energy for social connections and creative thinking."
            )
        )
        CycleStatus.OVULATION_WINDOW -> PhaseInsightContent(
            phaseTitle = "Fertile Window & Ovulation",
            phaseColor = OvulationColor,
            phaseIcon = Icons.Default.AutoAwesome,
            conceptionLikelihood = "Peak Fertility",
            conceptionColor = OvulationColor,
            hormoneSummary = "Luteinizing Hormone (LH) and estrogen peak to trigger egg release. Cervical fluid becomes clear and stretchy.",
            energyLevel = "Peak Energy & Libido",
            tips = listOf(
                Icons.Default.Favorite to "Optimal window for conception or natural family planning awareness.",
                Icons.Default.FitnessCenter to "Energy is at its highest—enjoy brisk cardio or group workouts.",
                Icons.Default.LocalDrink to "Stay well hydrated and include antioxidant-rich leafy greens and berries."
            )
        )
        CycleStatus.LUTEAL -> PhaseInsightContent(
            phaseTitle = "Luteal Phase",
            phaseColor = LutealColor,
            phaseIcon = Icons.Default.Bedtime,
            conceptionLikelihood = "Low Chance",
            conceptionColor = LutealColor,
            hormoneSummary = "Progesterone rises to support the uterine lining. Body temperature is elevated and metabolic rate slightly increases.",
            energyLevel = "Gentle & Reflective",
            tips = listOf(
                Icons.Default.SelfImprovement to "Shift toward restorative movement like yoga, walking, and mobility.",
                Icons.Default.LocalCafe to "Add magnesium-rich snacks (nuts, dark chocolate) to soothe PMS cravings.",
                Icons.Default.Nightlight to "Prioritize consistent 8+ hours of sleep to support hormonal equilibrium."
            )
        )
        CycleStatus.PREDICTION_WINDOW_ACTIVE -> PhaseInsightContent(
            phaseTitle = "Due Soon / Arrival Window",
            phaseColor = WindowActiveColor,
            phaseIcon = Icons.Default.NotificationsActive,
            conceptionLikelihood = "Very Low Chance",
            conceptionColor = WindowActiveColor,
            hormoneSummary = "Your adaptive prediction window is active. Menstruation is expected in the next 1–3 days.",
            energyLevel = "Rest & Reset",
            tips = listOf(
                Icons.Default.WaterDrop to "Keep your menstrual care supplies accessible and ready.",
                Icons.Default.Thermostat to "A warm heating pad or herbal tea (chamomile, peppermint) eases pre-flow cramps.",
                Icons.Default.Spa to "Listen to your body's need for lower pace and gentle downtime."
            )
        )
        CycleStatus.OVERDUE -> PhaseInsightContent(
            phaseTitle = "Cycle Overdue",
            phaseColor = OverdueColor,
            phaseIcon = Icons.Default.Warning,
            conceptionLikelihood = "Variable",
            conceptionColor = OverdueColor,
            hormoneSummary = "Your cycle has exceeded the dynamic confidence buffer. Natural variations can happen with travel, stress, or postpartum changes.",
            energyLevel = "Listen to Your Body",
            tips = listOf(
                Icons.Default.Checklist to "Consider taking a pregnancy test if you were sexually active during the fertile window.",
                Icons.Default.RestartAlt to "If recovering postpartum or breastfeeding, enable the Postpartum Reset switch in logs.",
                Icons.Default.HealthAndSafety to "Export your doctor PDF report if irregularity persists across multiple cycles."
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = content.phaseColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = content.phaseIcon,
                                contentDescription = null,
                                tint = content.phaseColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Body & Phase Signals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = content.phaseTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = content.phaseColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Conception Likelihood Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = content.conceptionColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = content.conceptionLikelihood,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = content.conceptionColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hormone & Physiological Summary
            Text(
                text = content.hormoneSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Energy Bar Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = content.phaseColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Energy Outlook:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = content.energyLevel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = content.phaseColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actionable Tips List
            Text(
                text = "Daily Wellness Guidance",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            content.tips.forEach { (icon, tipText) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tipText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
