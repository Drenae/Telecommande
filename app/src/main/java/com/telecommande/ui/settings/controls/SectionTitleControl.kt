package com.telecommande.ui.settings.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.SettingsSectionTitleDimensions

@Composable
fun SectionTitleControl(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SettingsSectionTitleDimensions.horizontalPadding,
                vertical = SettingsSectionTitleDimensions.verticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(SettingsSectionTitleDimensions.iconContainerSize)
                .background(
                    AppColors.settingsSectionIconBackground,
                    RoundedCornerShape(SettingsSectionTitleDimensions.iconContainerCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.settingsSectionIcon,
                modifier = Modifier.size(SettingsSectionTitleDimensions.iconSize)
            )
        }

        Spacer(Modifier.width(SettingsSectionTitleDimensions.contentSpacing))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.settingsSectionTitle
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.settingsSectionSubtitle
            )
        }
    }
}
