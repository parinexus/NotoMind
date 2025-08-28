package com.parinexus.detail

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.parinexus.designsystem.component.NoteTextField
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.model.NoteCheck
import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import com.parinexus.model.NoteUri
import com.parinexus.ui.DateDialog
import com.parinexus.ui.FlowLayout2
import com.parinexus.ui.LabelCard
import com.parinexus.ui.NotificationDialogNew
import com.parinexus.ui.ReminderCard
import com.parinexus.ui.TimeDialog
import java.io.File
import com.parinexus.designsystem.R as Rd

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
internal fun DetailRoute(
    onShowSnackbar: suspend (String, String?) -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    editViewModel: DetailViewModel = hiltViewModel(),
    navigateToGallery: (Long) -> Unit,
    navigateToSelectLevel: (Set<Long>) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,

    ) {
    val note = editViewModel.note.collectAsStateWithLifecycle().value
    var showModalState by remember {
        mutableStateOf(false)
    }
    var noteModalState by remember {
        mutableStateOf(false)
    }
    var noteficationModalState by remember {
        mutableStateOf(false)
    }
    var colorModalState by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember {
        mutableStateOf(false)
    }
    val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            if (it) {
                noteficationModalState = true
            }
        },
    )
    val context = LocalContext.current

    EditScreen(
        modifier = modifier,
        notoMind = note,
        title = editViewModel.title,
        content = editViewModel.content,
//        onTitleChange = editViewModel::onTitleChange,
//        onSubjectChange = editViewModel::onDetailChange,
        onBackClick = onBack,
        onCheckChange = editViewModel::onCheckChange,
        onCheckDelete = editViewModel::onCheckDelete,
        onCheck = editViewModel::onCheck,
        addItem = editViewModel::addCheck,
        moreOptions = {
//            coroutineScope.launch { modalState.show() }
            showModalState = true
        },
        noteOption = { noteModalState = true },
        unCheckAllItems = editViewModel::unCheckAllItems,
        deleteCheckItems = editViewModel::deleteCheckedItems,
        hideCheckBoxes = editViewModel::hideCheckBoxes,
        pinNote = editViewModel::pinNote,
        onLabel = {
            navigateToSelectLevel(
                setOf(
                    editViewModel.note.value.id,
                ),
            )
        },
        onColorClick = { colorModalState = true },
        onNotification = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                noteficationModalState = true
            }
        },
        showNotificationDialog = {
            showDialog = true
        },
        onArchive = editViewModel::onArchive,
        navigateToGallery = navigateToGallery,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,

        )
    AddBottomSheet2(
        show = showModalState,
        currentColor = note.color,
        currentImage = note.background,
        saveImage = editViewModel::saveImage,
        getPhotoUri = editViewModel::getPhotoUri,
        onDismiss = { showModalState = false },
    )
//
    val images = note.images.map {
        val file = File(it.path)
        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
        uri
    }

    val send = {
        val intent = ShareCompat.IntentBuilder(context)
            .setText(note.title)
            .setSubject(note.detail)
            .setChooserTitle("From NotoMind")

        if (images.isNotEmpty()) intent.setType("image/*") else intent.setType("text/*")
        images.forEach {
            intent.setStream(it)
        }

        context.startActivity(Intent(intent.createChooserIntent()))
    }

    NoteOptionBottomSheet(
        show = noteModalState,
        currentColor = note.color,
        currentImage = note.background,
        onLabel = {
            navigateToSelectLevel(
                setOf(
                    editViewModel.note.value.id,
                ),
            )
        },
        onDelete = editViewModel::onDelete,
        onCopy = editViewModel::copyNote,
        onSendNote = send,
        onDismissRequest = { noteModalState = false },
    )
    ColorAndImageBottomSheet(
        show = colorModalState,
        currentColor = note.color,
        currentImage = note.background,
        onColorClick = editViewModel::onColorChange,
        onImageClick = editViewModel::onImageChange,
        onDismissRequest = { colorModalState = false },
    )
//
    NotificationBottomSheet(
        show = noteficationModalState,
        onAlarm = editViewModel::setAlarm,
        showDialog = { showDialog = true },
        currentColor = note.color,
        currentImage = note.background,

        ) { noteficationModalState = false }
    val dateDialogUiData = editViewModel.dateTimeState.collectAsStateWithLifecycle()
//
    NotificationDialogNew(
        showDialog = showDialog,
        dateDialogUiData = dateDialogUiData.value,
        onDismissRequest = { showDialog = false },
        onSetAlarm = editViewModel::setAlarm,
        onTimeChange = editViewModel::onSetTime,
        onDateChange = editViewModel::onSetDate,
        onIntervalChange = editViewModel::onSetInterval,
        onDeleteAlarm = editViewModel::deleteAlarm,
    )
//
    TimeDialog(
        state = editViewModel.timePicker,
        showDialog = dateDialogUiData.value.showTimeDialog,
        onDismissRequest = editViewModel::hideTime,
        onSetTime = editViewModel::onSetTime,
    )
    DateDialog(
        state = editViewModel.datePicker,
        showDialog = dateDialogUiData.value.showDateDialog,
        onDismissRequest = editViewModel::hideDate,
        onSetDate = editViewModel::onSetDate,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun EditScreen(
    modifier: Modifier = Modifier,
    notoMind: NotoMind,
    title: TextFieldState,
    content: TextFieldState,
//    onTitleChange: (String) -> Unit = {},
//    onSubjectChange: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    onCheckChange: (String, Long) -> Unit = { _, _ -> },
    onCheckDelete: (Long) -> Unit = {},
    onCheck: (Boolean, Long) -> Unit = { _, _ -> },
    addItem: () -> Unit = {},
    moreOptions: () -> Unit = {},
    noteOption: () -> Unit = {},
    unCheckAllItems: () -> Unit = {},
    deleteCheckItems: () -> Unit = {},
    hideCheckBoxes: () -> Unit = {},
    pinNote: () -> Unit = {},
    onLabel: () -> Unit = {},
    onColorClick: () -> Unit = {},
    onNotification: () -> Unit = {},
    showNotificationDialog: () -> Unit = {},
    onArchive: () -> Unit = {},
    navigateToGallery: (Long) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
) {
    var expandCheck by remember {
        mutableStateOf(false)
    }

    val subjectFocus = remember {
        FocusRequester()
    }

    val checkNote by remember(notoMind.checks) {
        derivedStateOf { notoMind.checks.filter { it.isCheck } }
    }
    val notCheckNote by remember(notoMind.checks) {
        derivedStateOf { notoMind.checks.filter { !it.isCheck } }
    }
    var showCheckNote by remember {
        mutableStateOf(true)
    }

    val bg = if (notoMind.background != -1) {
        Color.Transparent
    } else {
        if (notoMind.color != -1) {
            NoteIcon.noteColors[notoMind.color]
        } else {
            MaterialTheme.colorScheme.background
        }
    }
    val color = NoteIcon.noteColors.getOrNull(notoMind.color) ?: Color.Transparent

    val sColor = if (notoMind.background != -1) {
        NoteIcon.background[notoMind.background].fgColor
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val painter = if (notoMind.background != -1) {
        rememberVectorPainter(image = ImageVector.vectorResource(id = NoteIcon.background[notoMind.background].bg))
    } else {
        null
    }

    val images = remember(notoMind.images) {
        notoMind.images.reversed().chunked(3)
    }

    LaunchedEffect(
        key1 = notoMind,
        block = {
            if (notoMind.focus) {
                subjectFocus.requestFocus()
            }
        },
    )

    with(sharedTransitionScope) {
        Scaffold(
            containerColor = bg,
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("note${notoMind.id}"),
                    animatedVisibilityScope = animatedContentScope,
                )
                .drawBehind {
                    if (painter != null) {
                        with(painter) {
                            draw(size)
                        }
                    }
                },
            topBar = {
                TopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(
                            modifier = Modifier.testTag("detail:back"),
                            onClick = { onBackClick() },
                        ) {
                            Icon(
                                imageVector = NoteIcon.ArrowBack,
                                contentDescription = "back",
                            )
                        }
                    },

                    actions = {
                        IconButton(onClick = {
//                            editViewModel.saveNote()
                        }) {
                            Icon(
                                imageVector = NoteIcon.Done,
                                contentDescription = "save",
                            )
                        }
                        IconButton(onClick = { pinNote() }) {
                            Icon(
                                imageVector = if (notoMind.isPin) NoteIcon.PushPinD else NoteIcon.PushPin,
                                contentDescription = "pin",
                            )
                        }
                        IconButton(onClick = { onNotification() }) {
                            Icon(
                                imageVector = NoteIcon.NotificationAdd,
                                contentDescription = "notification",
                            )
                        }
                        IconButton(onClick = { onArchive() }) {
                            Icon(
                                imageVector = if (notoMind.noteType == NoteType.ARCHIVE) NoteIcon.Unarchive else NoteIcon.Archive,
                                contentDescription = "archive",
                            )
                        }
                    },
                )
            },

            ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxHeight(),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail:list"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (notoMind.images.isNotEmpty()) {
                        item(images) {
                            images.forEach { imageList ->
                                Row(
                                    modifier = Modifier
                                        .testTag("detail:images")
                                        .fillMaxWidth()
                                        .height(200.dp),
                                ) {
                                    imageList.forEach {
                                        AsyncImage(
                                            modifier = Modifier
                                                .clickable {
                                                    navigateToGallery(notoMind.id)
                                                }
                                                .weight(1f)
                                                .height(200.dp),
                                            model = it.path,
                                            contentDescription = "note image",
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NoteTextField(
                                state = title,
                                placeholder = stringResource(Rd.string.modules_designsystem_title),
                                imeAction = ImeAction.Next,
                                modifier = Modifier
                                    .padding(0.dp)
                                    .weight(1f)
                                    .testTag("detail:title"),

                                )
                            if (notoMind.isCheck) {
                                Box {
                                    IconButton(
                                        modifier = Modifier.testTag("detail:morecheck"),
                                        onClick = { expandCheck = true },
                                    ) {
                                        Icon(
                                            imageVector = NoteIcon.MoreVert,
                                            contentDescription = "",
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandCheck,
                                        onDismissRequest = { expandCheck = false },
                                    ) {
                                        DropdownMenuItem(
                                            modifier = Modifier.testTag("detail:hidecheck"),
                                            text = { Text(text = stringResource(Rd.string.modules_designsystem_hide_checkboxes)) },
                                            onClick = {
                                                hideCheckBoxes()
                                                expandCheck = false
                                            },
                                        )
                                        if (checkNote.isNotEmpty()) {
                                            DropdownMenuItem(
                                                modifier = Modifier.testTag("detail:uncheckall"),
                                                text = { Text(text = stringResource(Rd.string.modules_designsystem_uncheck_all_items)) },
                                                onClick = {
                                                    unCheckAllItems()
                                                    expandCheck = false
                                                },
                                            )
                                            DropdownMenuItem(
                                                modifier = Modifier.testTag("detail:deletecheck"),
                                                text = { Text(text = stringResource(Rd.string.modules_designsystem_delete_checked_items)) },
                                                onClick = {
                                                    deleteCheckItems()
                                                    expandCheck = false
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!notoMind.isCheck) {
                        item {
                            NoteTextField(
                                state = content,
                                placeholder = stringResource(Rd.string.modules_designsystem_subject),
                                imeAction = ImeAction.Next,
                                keyboardAction = { subjectFocus.freeFocus() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .imePadding()
                                    .focusRequester(subjectFocus)
                                    .testTag("detail:content"),

                                )
                        }
                    }
                    if (notoMind.isCheck) {
                        items(notCheckNote, key = { it.id }) {
                            NoteCheck(
                                noteCheckUiState = it,
                                onCheckChange = onCheckChange,
                                onCheckDelete = onCheckDelete,
                                onCheck = onCheck,
                                onNextCheck = addItem,
                            )
                        }

                        item {
                            TextButton(onClick = addItem) {
                                Icon(imageVector = NoteIcon.Add, contentDescription = "")

                                Text(text = stringResource(Rd.string.modules_designsystem_add_list_item))
                            }
                        }

                        if (checkNote.isNotEmpty()) {
                            item {
                                TextButton(onClick = { showCheckNote = !showCheckNote }) {
                                    Icon(
                                        imageVector = if (showCheckNote) NoteIcon.More else NoteIcon.Less,
                                        contentDescription = "",
                                    )
                                    Text(
                                        text = "${checkNote.size} ${stringResource(Rd.string.modules_designsystem_checked_items)}",
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                        }

                        if (showCheckNote) {
                            items(checkNote, key = { it.id }) {
                                NoteCheck(
                                    noteCheckUiState = it,
                                    onCheckChange = onCheckChange,
                                    onCheckDelete = onCheckDelete,
                                    onCheck = onCheck,
                                    strickText = true,
                                    onNextCheck = addItem,
                                )
                            }
                        }
                    }
                    items(items = notoMind.uris, key = { it.id }) {
                        NoteUri(uriState = it, sColor)
                    }
                    item {
                        FlowLayout2(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalSpacing = 8.dp,
                        ) {
                            if (notoMind.reminder > 0) {
                                ReminderCard(
                                    date = notoMind.reminderString,
                                    interval = notoMind.interval,
                                    color = sColor,
                                    style = MaterialTheme.typography.bodyLarge,
                                    onClick = showNotificationDialog,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            notoMind.labels.forEach {
                                LabelCard(
                                    name = it.label,
                                    color = sColor,
                                    style = MaterialTheme.typography.bodyLarge,
                                    onClick = onLabel,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (notoMind.background > -1 && notoMind.color > -1) {
                                Box(
                                    modifier = Modifier
                                        .clickable { onColorClick() }
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(1.dp, Color.Gray, CircleShape)
                                        .size(30.dp),

                                    )
                            }
                        }
                    }
//                item {
//                    AsyncImage(modifier = Modifier.size(200.dp), model = "https://icon.horse/icon/fb.com", contentDescription = "")
//                }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        modifier = Modifier.testTag("detail:more"),
                        onClick = { moreOptions() },
                    ) {
                        Icon(
                            imageVector = NoteIcon.AddBox,
                            contentDescription = "more note",
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("detail:colors"),
                        onClick = { onColorClick() },
                    ) {
                        Icon(
                            imageVector = NoteIcon.ColorLens,
                            contentDescription = "colors",
                        )
                    }
                    Row(
                        Modifier
                            .weight(1f)
                            .padding(end = 32.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${stringResource(Rd.string.modules_designsystem_edited)} ${notoMind.editDateString}",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("detail:options"),
                        onClick = { noteOption() },
                    ) {
                        Icon(
                            imageVector = NoteIcon.MoreVert,
                            contentDescription = "note options",
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DetailScreenPreview() {
//    DetailScreen()
}

@Composable
fun NoteCheck(
    noteCheckUiState: NoteCheck,
    onCheckChange: (String, Long) -> Unit = { _, _ -> },
    onCheckDelete: (Long) -> Unit = {},
    onCheck: (Boolean, Long) -> Unit = { _, _ -> },
    strickText: Boolean = false,
    onNextCheck: () -> Unit,
) {
    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }
    LaunchedEffect(
        key1 = Unit,
        block = {
            if (noteCheckUiState.id == 1L) {
                mutableInteractionSource.emit(FocusInteraction.Focus())
            }
        },
    )
    val focused by mutableInteractionSource.collectIsFocusedAsState()
    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(
        key1 = noteCheckUiState,
        block = {
            if (noteCheckUiState.focus) {
                focusRequester.requestFocus()
            } else {
                focusRequester.freeFocus()
            }
        },
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = noteCheckUiState.isCheck,
            onCheckedChange = { onCheck(it, noteCheckUiState.id) },
        )
        NoteTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .weight(1f),
            text = noteCheckUiState.content,
            onTextChange = { onCheckChange(it, noteCheckUiState.id) },
            textStyle = if (strickText) TextStyle.Default.copy(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
            interactionSource = mutableInteractionSource,
            trailingIcon = {
                if (focused) {
                    IconButton(
                        onClick = {
                            onCheckDelete(noteCheckUiState.id)
                        },
                    ) {
                        Icon(imageVector = NoteIcon.Clear, contentDescription = "")
                    }
                }
            },
            imeAction = ImeAction.Next,
            keyboardAction = { onNextCheck() },
        )
    }
}

@Composable
fun NoteUri(
    uriState: NoteUri,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val context = LocalContext.current

    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setData(uriState.uri.toUri())
                }
                context.startActivity(intent)
            },
        colors = ListItemDefaults.colors(containerColor = color),
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(64.dp),
                model = uriState.icon,
                contentDescription = "icon",
            )
        },
        headlineContent = { Text(text = uriState.path) },
        supportingContent = { Text(text = uriState.uri, maxLines = 2) },
        shadowElevation = 8.dp,
        tonalElevation = 8.dp,
    )
}

@Preview
@Composable
fun NoteUriPreview() {
    NoteUri(uriState = NoteUri(1, "", "Path", "akdkdk"))
}
