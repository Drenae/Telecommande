package com.telecommande.ui.settings.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.telecommande.core.discovery.DiscoveredTv
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.TvManagementSpecs

@Composable
fun PinEntryDialog(
    tvToPair: DiscoveredTv,
    currentPin: String,
    onPinChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var pinValue by remember(currentPin) { mutableStateOf(currentPin) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(22.dp),
        containerColor = AppColors.surfaceElevated,
        titleContentColor = AppColors.appWhite,
        textContentColor = AppColors.textSecondary,
        title = {
            Column {
                Text(
                    text = "Appairer la TV",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tvToPair.friendlyName ?: tvToPair.ipAddress ?: "TV Android",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.accent
                )
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.Start) {
                Text("Saisissez le code affiché sur votre TV (lettres et chiffres).")
                Spacer(modifier = Modifier.height(TvManagementSpecs.PinDialogVerticalSpacer))
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = { rawValue ->
                        val normalizedValue = rawValue
                            .filter { it.isLetterOrDigit() }
                            .uppercase()
                        pinValue = normalizedValue
                        onPinChange(normalizedValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text("Code d'appairage") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (!isLoading && pinValue.isNotBlank()) {
                            keyboardController?.hide()
                            onConfirm()
                        }
                    }),
                    singleLine = true,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyboardController?.hide()
                    onConfirm()
                },
                enabled = !isLoading && pinValue.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.accent,
                    contentColor = AppColors.appBlack
                )
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AppColors.appBlack
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Vérification")
                    }
                } else {
                    Text("Confirmer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Annuler")
            }
        },
        modifier = Modifier.padding(TvManagementSpecs.PinDialogOverallPadding)
    )
}
