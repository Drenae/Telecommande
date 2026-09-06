package com.telecommande.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.telecommande.R
import com.telecommande.navigation.Screen
import com.telecommande.ui.home.layout.ContentSection
import com.telecommande.ui.home.layout.FooterSection
import com.telecommande.ui.home.layout.HeaderSection
import com.telecommande.ui.theme.AppColors
import com.telecommande.ui.theme.HomeScreenDimensions

@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val remoteInputFocusRequester = remember { FocusRequester() }
    var remoteInputValue by remember { mutableStateOf(TextFieldValue()) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
            viewModel.clearSnackbarMessage()
        }
    }

    LaunchedEffect(uiState.pairingRequiredEvent) {
        if (uiState.pairingRequiredEvent) {
            snackbarHostState.showSnackbar(
                message = "Appairage requis pour ${uiState.activeTvName ?: "la TV"}.",
                duration = SnackbarDuration.Short
            )
            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
            viewModel.consumePairingRequiredEvent()
        }
    }

    LaunchedEffect(uiState.textInputRequestId) {
        val request = uiState.textInputRequest ?: return@LaunchedEffect
        val textLength = request.value.length
        val selectionStart = request.selectionStart.coerceIn(0, textLength)
        val selectionEnd = request.selectionEnd.coerceIn(0, textLength)
        remoteInputValue = TextFieldValue(
            text = request.value,
            selection = TextRange(selectionStart, selectionEnd)
        )
        remoteInputFocusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(uiState.textInputRequest) {
        if (uiState.textInputRequest == null) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        containerColor = AppColors.homeScreenScaffoldBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(R.drawable.remote_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.32f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = HomeScreenDimensions.horizontalPadding,
                        vertical = HomeScreenDimensions.verticalPadding
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HeaderSection(
                    title = uiState.activeTvName ?: "Télécommande",
                    onPowerClick = viewModel::sendPowerCommand,
                    isConnected = uiState.isConnected,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    onStatusIndicatorClick = { navController.navigate(Screen.Settings.route) }
                )

                ContentSection(
                    modifier = Modifier.fillMaxWidth(),
                    onOkClick = viewModel::sendDpadCenterCommand,
                    onUpClick = viewModel::sendDpadUpCommand,
                    onDownClick = viewModel::sendDpadDownCommand,
                    onLeftClick = viewModel::sendDpadLeftCommand,
                    onRightClick = viewModel::sendDpadRightCommand,
                    onBackClick = viewModel::sendBackCommand,
                    onHomeClick = viewModel::sendHomeCommand,
                    volumeLevel = uiState.volumeLevel,
                    volumeMax = uiState.volumeMax,
                    isMuted = uiState.isMuted,
                    onVolumeUpClick = viewModel::sendVolumeUpCommand,
                    onVolumeDownClick = viewModel::sendVolumeDownCommand,
                    onMuteClick = viewModel::sendMuteCommand,
                    onRewindClick = viewModel::sendMediaRewindCommand,
                    onPlayPauseClick = viewModel::sendMediaPlayPauseCommand,
                    onStopClick = viewModel::sendMediaStopCommand,
                    onFastForwardClick = viewModel::sendMediaFastForwardCommand
                )

                FooterSection(
                    modifier = Modifier.fillMaxWidth(),
                    onLaunchNetflix = { viewModel.launchAppByLink("netflix://") },
                    onLaunchYouTube = { viewModel.launchAppByLink("vnd.youtube://") },
                    onLaunchPlex = { viewModel.launchAppByLink("plex://") },
                    onLaunchCrunchyroll = { viewModel.launchAppByLink("crunchyroll://") }
                )
            }

            BasicTextField(
                value = remoteInputValue,
                onValueChange = { newValue ->
                    val previousText = remoteInputValue.text
                    remoteInputValue = newValue
                    viewModel.onTextInputChanged(previousText, newValue.text)
                },
                modifier = Modifier
                    .size(1.dp)
                    .alpha(0f)
                    .focusRequester(remoteInputFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.submitTextInput()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    onDone = {
                        viewModel.submitTextInput()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    onGo = {
                        viewModel.submitTextInput()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    onSend = {
                        viewModel.submitTextInput()
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true
            )
        }
    }
}
