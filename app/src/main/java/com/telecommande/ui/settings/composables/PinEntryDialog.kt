package com.telecommande.ui.settings.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.telecommande.core.discovery.DiscoveredTv
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
        title = { Text("PIN requis pour ${tvToPair.friendlyName ?: tvToPair.ipAddress}") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Veuillez entrer le code PIN affiché sur votre TV.")
                Spacer(modifier = Modifier.height(TvManagementSpecs.PinDialogVerticalSpacer))
                OutlinedTextField(
                    value = pinValue,
                    onValueChange = {
                        pinValue = it
                        onPinChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text("Code PIN") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (!isLoading && pinValue.isNotBlank()) {
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
                onClick = onConfirm,
                enabled = !isLoading && pinValue.isNotBlank()
            ) {
                Text("Confirmer")
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
