package com.droidkaigi.quiz.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.components.StaffHorizontalDivider
import com.droidkaigi.quiz.feature.staff.components.StaffTextButton

@Composable
fun StaffTopBar(
    sitePublished: Boolean,
    onToggleSitePublished: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(QuizTokens.staffTopBarHeight)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = QuizTokens.spacingLarge),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Keeps the wordmark clear of the navigation rail that starts below this bar.
                Spacer(modifier = Modifier.width(QuizTokens.staffNavRailWidth - QuizTokens.spacingLarge))
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(QuizTokens.spacingLarge),
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
                Text(
                    text = "Droid26クイズ管理アプリ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (sitePublished) "サイト: 公開中" else "サイト: 非公開",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sitePublished) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
                StaffTextButton(
                    text = if (sitePublished) "サイトを非公開にする" else "サイトを公開",
                    icon = null,
                    onClick = onToggleSitePublished,
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingMedium))
                StaffTextButton(
                    text = "Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = onSignOut,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StaffHorizontalDivider()
    }
}
