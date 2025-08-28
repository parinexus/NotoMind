package com.parinexus.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.designsystem.R as Rd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet2(
    currentColor: Int? = null,
    currentImage: Int? = null,
    saveImage: (String) -> Unit = {},
    getPhotoUri: () -> String = { "" },
    onDismiss: () -> Unit = {},
    show: Boolean,
) {
    val background = when {
        currentImage != null && currentImage != -1 -> NoteIcon.background[currentImage].fgColor
        currentColor != null && currentColor != -1 -> NoteIcon.noteColors[currentColor]
        else -> MaterialTheme.colorScheme.surface
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                saveImage(it.toString())
            }
        },
    )
    val snapPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {
            if (it) {
                saveImage(getPhotoUri())
            }
        },
    )

    if (show) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = background,

            ) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = NoteIcon.PhotoCamera,
                        contentDescription = "",
                    )
                },
                label = { Text(text = stringResource(Rd.string.modules_designsystem_take_photo)) },
                selected = false,
                onClick = {
                    snapPictureLauncher.launch(Uri.parse(getPhotoUri()))
                    onDismiss()
                },
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = background),
                modifier = Modifier.testTag("detail:take_photo"),
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = NoteIcon.Image,
                        contentDescription = "",
                    )
                },
                label = { Text(text = stringResource(Rd.string.modules_designsystem_add_image)) },
                selected = false,
                onClick = {
                    imageLauncher.launch(
                        PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                    onDismiss()
                },
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = background),
                modifier = Modifier.testTag("detail:add_image"),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddBottomSheet2Preview() {
    AddBottomSheet2(
        currentColor = 2,
        currentImage = 2,
        show = true,
    )
}
