package com.telecommande.ui.settings.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.telecommande.ui.theme.SettingsPrimaryButtonDimensions

@Composable
fun SettingsPrimaryButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SettingsPrimaryButtonDimensions.cornerRadius)

    Row(
        modifier = modifier
            .height(SettingsPrimaryButtonDimensions.height)
            .background(AppColors.settingsPrimaryButtonBackground, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = SettingsPrimaryButtonDimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsPrimaryButtonDimensions.contentSpacing)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.settingsPrimaryButtonIcon,
            modifier = Modifier.size(SettingsPrimaryButtonDimensions.iconSize)
        )
        Text(
            text = label,
            color = AppColors.settingsPrimaryButtonText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
