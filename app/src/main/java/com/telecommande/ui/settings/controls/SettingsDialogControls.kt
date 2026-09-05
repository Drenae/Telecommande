package com.telecommande.ui.settings.controls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telecommande.data.model.PairedTvInfo
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.SettingsDialogDimensions
import com.telecommande.util.technicalName

@Composable
fun ErrorDialogControl(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.settingsErrorDialogContainer,
        titleContentColor = AppColors.settingsErrorDialogTitle,
        textContentColor = AppColors.settingsErrorDialogText,
        title = { Text("Erreur") },
        text = { Text(errorMessage) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = AppColors.settingsErrorDialogConfirm)
            }
        }
    )
}

@Composable
fun RenameTvControl(
    tvInfo: PairedTvInfo,
    renameValue: String,
    hasCustomDisplayName: Boolean,
    onRenameValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onRestoreOriginalName: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.settingsRenameDialogContainer,
        titleContentColor = AppColors.settingsRenameDialogTitle,
        textContentColor = AppColors.settingsRenameDialogText,
        title = { Text("Renommer la TV") },
        text = {
            Column {
                Text(
                    text = "Nom réel : ${tvInfo.technicalName()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.settingsRenameTechnicalName
                )
                Spacer(Modifier.height(SettingsDialogDimensions.renameTechnicalNameSpacing))
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { onRenameValueChange(it.take(32)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nom affiché") },
                    placeholder = { Text("Salon, Chambre…") },
                    singleLine = true
                )
                Spacer(Modifier.height(SettingsDialogDimensions.renameHelpTextSpacing))
                Text(
                    text = "Ce nom est uniquement visuel. Le nom réel de la TV reste inchangé pour la connexion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.settingsRenameHelpText
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.settingsRenameConfirmBackground,
                    contentColor = AppColors.settingsRenameConfirmContent
                )
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            Row {
                if (hasCustomDisplayName) {
                    TextButton(onClick = onRestoreOriginalName) {
                        Text(
                            "Nom d'origine",
                            color = AppColors.settingsRenameDismissText
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Annuler", color = AppColors.settingsRenameDismissText)
                }
            }
        }
    )
}
