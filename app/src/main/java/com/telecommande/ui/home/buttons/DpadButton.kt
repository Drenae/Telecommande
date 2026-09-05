package com.telecommande.ui.home.buttons

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.telecommande.util.GradientIcon

@Composable
fun DpadButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(size)
    ) {
        GradientIcon(
            icon = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(size * .72f)
        )
    }
}
