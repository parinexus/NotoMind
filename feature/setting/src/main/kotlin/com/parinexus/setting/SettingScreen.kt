package com.parinexus.setting

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.model.DarkThemeConfig
import com.parinexus.ui.Waiting
import com.parinexus.designsystem.R as Rd

@Composable
internal fun SettingRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val viewModel = hiltViewModel<SettingViewModel>()
    val settingState = viewModel.settingState.collectAsStateWithLifecycle()

    SettingScreen(
        modifier = modifier.heightIn(min = 300.dp),
        settingState = settingState.value,
        setDarkMode = viewModel::setDarkThemeConfig,
        onBack = onBack,
    )
}

@Composable
internal fun SettingScreen(
    settingState: SettingState,
    modifier: Modifier = Modifier,
    setDarkMode: (DarkThemeConfig) -> Unit = {},
    onBack: () -> Unit = {},
) {
    Card(modifier = modifier.testTag("setting:screen")) {
        AnimatedContent(settingState) {
            when (it) {
                is SettingState.Loading -> Waiting(modifier)
                is SettingState.Success -> MainContent(
                    modifier = modifier,
                    settingState = it,
                    setDarkMode = setDarkMode,
                    onBack = onBack,
                )

                else -> {}
            }
        }
    }
}

@Composable
internal fun MainContent(
    modifier: Modifier = Modifier,
    settingState: SettingState.Success,
    setDarkMode: (DarkThemeConfig) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var dark by remember { mutableStateOf(false) }

    Column(
        modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
            IconButton(
                onClick = onBack,

            ) {
                Icon(imageVector = NoteIcon.Cancel, "cancel")
            }
        }

        Spacer(Modifier.height(8.dp))

        ListItem(
            modifier = Modifier.testTag("setting:mode").clickable { dark = true },
            headlineContent = { Text("DayNight mode") },
            supportingContent = {
                Text(stringArrayResource(Rd.array.modules_designsystem_daynight)[settingState.darkThemeConfig.ordinal])
            },
        )
    }

    AnimatedVisibility(dark) {
        OptionsDialog(
            modifier = Modifier,
            options = stringArrayResource(Rd.array.modules_designsystem_daynight).toList(),
            current = settingState.darkThemeConfig.ordinal,
            onDismiss = { dark = false },
            onSelect = { setDarkMode(DarkThemeConfig.entries[it]) },
        )
    }
}
