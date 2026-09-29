package com.example.ui.screens.queue

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.PublishReadiness

@Composable
fun FinalVerificationBadge(status: FinalVerificationStatus, modifier: Modifier = Modifier) {
    val (label, bg, fg) = when (status) {
        FinalVerificationStatus.PASSED -> Triple("VERIFIED", Color(0xFFDCFCE7), Color(0xFF166534))
        FinalVerificationStatus.NEEDS_REVIEW -> Triple("REVIEW REQ", Color(0xFFFEF3C7), Color(0xFF92400E))
        FinalVerificationStatus.BLOCKED -> Triple("BLOCKED", Color(0xFFFEE2E2), Color(0xFF991B1B))
        FinalVerificationStatus.FAILED -> Triple("FAILED", Color(0xFFFEE2E2), Color(0xFF991B1B))
        FinalVerificationStatus.EXPIRED -> Triple("EXPIRED", Color(0xFFF3F4F6), Color(0xFF374151))
        FinalVerificationStatus.PENDING -> Triple("PENDING", Color(0xFFE0E7FF), Color(0xFF3730A3))
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg, modifier = modifier) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
fun HumanReviewRequiredBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFFEF3C7),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "HUMAN REVIEW REQUIRED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E)
            )
        }
    }
}

@Composable
fun PublishReadinessBadge(readiness: PublishReadiness, modifier: Modifier = Modifier) {
    val (bg, fg) = when (readiness) {
        PublishReadiness.READY_FOR_PUBLISHER -> Color(0xFFDCFCE7) to Color(0xFF166534)
        PublishReadiness.BLOCKED -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        PublishReadiness.NOT_READY -> Color(0xFFFEF3C7) to Color(0xFF92400E)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg, modifier = modifier) {
        Text(
            text = readiness.displayName,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
