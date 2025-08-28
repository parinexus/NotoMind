import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.parinexus.designsystem.component.NoteTextField
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.model.NoteCheck
import com.parinexus.model.NotoMind
import com.parinexus.ui.FlowLayout2
import com.parinexus.ui.LabelCard
import com.parinexus.ui.ReminderCard
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Clock
import com.parinexus.designsystem.R as Rd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(),
) {
    val focusRequester = remember {
        FocusRequester()
    }
    LaunchedEffect(key1 = Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = {
                state.clearText()
            }) {
                Icon(imageVector = NoteIcon.ArrowBack, contentDescription = "back")
            }
        },
        title = {
            NoteTextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                state = state,
                placeholder = "Search",
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = {
                    if (state.text.isNotBlank()) {
                        IconButton(onClick = { state.clearText() }) {
                            Icon(
                                imageVector = NoteIcon.Clear,
                                contentDescription = stringResource(Rd.string.modules_designsystem_delete),
                            )
                        }
                    }
                },
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTopBar(
    selectNumber: Int = 0,
    isAllPin: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    onClear: () -> Unit = {},
    onPin: () -> Unit = {},
    onNoti: () -> Unit = {},
    onColor: () -> Unit = {},
    onArchive: () -> Unit = {},
    onSend: () -> Unit = {},
    onCopy: () -> Unit = {},

) {
    var showDropDown by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(imageVector = NoteIcon.Clear, contentDescription = "clear note")
            }
        },
        title = {
            Text(text = "$selectNumber")
        },
        actions = {
            IconButton(
                modifier = Modifier.testTag("main:pin"),
                onClick = onPin,
            ) {
                Icon(
                    imageVector = if (isAllPin) NoteIcon.PushPin else NoteIcon.PushPinD, // painterResource(id = if (isAllPin) NoteIcon.Pin else NoteIcon.PinFill),
                    contentDescription = "pin note",
                )
            }
            IconButton(
                modifier = Modifier.testTag("main:notification"),
                onClick = onNoti,
            ) {
                Icon(
                    imageVector = NoteIcon.Notification,
                    contentDescription = "notification",
                )
            }
            IconButton(
                modifier = Modifier.testTag("main:color"),
                onClick = onColor,
            ) {
                Icon(
                    imageVector = NoteIcon.ColorLens,
                    contentDescription = "color",
                )
            }

            Box {
                IconButton(
                    modifier = Modifier.testTag("main:more"),
                    onClick = { showDropDown = true },
                ) {
                    Icon(NoteIcon.MoreVert, contentDescription = "more")
                }
                DropdownMenu(expanded = showDropDown, onDismissRequest = { showDropDown = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(Rd.string.modules_designsystem_archive)) },
                        onClick = {
                            showDropDown = false
                            onArchive()
                        },
                    )
                    if (selectNumber == 1) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(Rd.string.modules_designsystem_make_a_copy)) },
                            onClick = {
                                showDropDown = false
                                onCopy()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(Rd.string.modules_designsystem_send)) },
                            onClick = {
                                showDropDown = false
                                onSend()
                            },
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SelectTopAppBarPreview() {
    SelectTopBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelTopAppBar(
    label: String = "label",
    onNavigate: () -> Unit = {},
    onSearch: () -> Unit = {},
    onRenameLabel: () -> Unit = {},
    onDeleteLabel: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
    var showDropDown by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigate) {
                Icon(imageVector = NoteIcon.Menu, contentDescription = "menu")
            }
        },
        title = { Text(text = label) },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = NoteIcon.Search,
                    contentDescription = "search",
                )
            }

            Box {
                IconButton(onClick = { showDropDown = true }) {
                    Icon(NoteIcon.MoreVert, contentDescription = "more")
                }
                DropdownMenu(expanded = showDropDown, onDismissRequest = { showDropDown = false }) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(Rd.string.modules_designsystem_rename_label)) },
                        onClick = {
                            showDropDown = false
                            onRenameLabel()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(Rd.string.modules_designsystem_delete_label)) },
                        onClick = {
                            showDropDown = false
                            onDeleteLabel()
                        },
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LabelTopAppBarPreview() {
    LabelTopAppBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveTopAppBar(
    name: String = "Archive",
    onNavigate: () -> Unit = {},
    onSearch: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
//    var showDropDown by remember {
//        mutableStateOf(false)
//    }

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigate) {
                Icon(imageVector = NoteIcon.Menu, contentDescription = "menu")
            }
        },
        title = { Text(text = name) },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = NoteIcon.Search,
                    contentDescription = "search",
                )
            }
        },
        scrollBehavior = scrollBehavior,

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ArchiveTopAppBarPreview() {
    ArchiveTopAppBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashTopAppBar(
    onNavigate: () -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
//    var showDropDown by remember {
//        mutableStateOf(false)
//    }
//
//    TopAppBar(
//        navigationIcon = {
//            IconButton(onClick = onNavigate) {
//                Icon(imageVector = NoteIcon.Menu, contentDescription = "menu")
//            }
//        },
//        title = { Text(text = "Trash") },
//        actions = {
//            Box {
//                IconButton(onClick = { showDropDown = true }) {
//                    Icon(NoteIcon.MoreVert, contentDescription = "more")
//                }
//                DropdownMenu(expanded = showDropDown, onDismissRequest = { showDropDown = false }) {
//                    DropdownMenuItem(
//                        text = { Text(text = stringResource(Rd.string.modules_designsystem_empty_trash)) },
//                        onClick = {
//                            showDropDown = false
//                            onEmptyTrash()
//                        },
//                    )
//                }
//            }
//        },
//        scrollBehavior = scrollBehavior,
//
//    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TrashTopAppBarPreview() {
    TrashTopAppBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    isGrid: Boolean = false,
    onNavigate: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    onToggleGrid: () -> Unit = {},
) {
    TopAppBar(
        modifier = Modifier,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .padding(end = 16.dp)
                        .clip(
                            RoundedCornerShape(
                                topEnd = 50f,
                                topStart = 50f,
                                bottomEnd = 50f,
                                bottomStart = 50f,
                            ),
                        ),
                ) {
                    IconButton(onClick = onNavigate) {
                        Icon(
                            imageVector = NoteIcon.Menu,
                            contentDescription = "menu",
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Rd.string.modules_designsystem_search_note),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                    IconButton(onClick = { onToggleGrid() }) {
                        if (!isGrid) {
                            Icon(imageVector = NoteIcon.GridView, contentDescription = "grid")
                        } else {
                            Icon(imageVector = NoteIcon.ViewAgenda, contentDescription = "column")
                        }
                    }
                }

                HorizontalDivider()
            }

        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MainTopAppBarPreview() {
    MainTopAppBar()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameLabelAlertDialog(
    show: Boolean = false,
    label: String = "Label",
    onDismissRequest: () -> Unit = {},
    onChangeName: (String) -> Unit = {},
) {
    var name by remember(label) {
        mutableStateOf(label)
    }

    AnimatedVisibility(visible = show) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = stringResource(id = Rd.string.modules_designsystem_rename_label)) },
            text = {
                TextField(value = name, onValueChange = { name = it })
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissRequest()
                        onChangeName(name)
                    },
                ) {
                    Text(text = stringResource(Rd.string.modules_designsystem_rename))
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = stringResource(Rd.string.modules_designsystem_cancel))
                }
            },
        )
    }
}

@Preview
@Composable
fun RenameLabelPreview() {
    RenameLabelAlertDialog(show = true)
}

@Composable
fun DeleteLabelAlertDialog(
    show: Boolean = false,
    onDismissRequest: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    AnimatedVisibility(visible = show) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = "Rename Label") },
            text = {
                Text(text = " We'll delete the label and remove it from all of from all of your keep notes. Your notes won't be deleted")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        onDelete()
                    },
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismissRequest() }) {
                    Text(text = "Cancel")
                }
            },
        )
    }
}

@Preview
@Composable
fun DeleteLabelPreview() {
    DeleteLabelAlertDialog(show = true)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    notoMind: NotoMind,
    onCardClick: (Long) -> Unit = {},
    onLongClick: (Long) -> Unit = {},
) {
    val unCheckNote by remember(notoMind.checks) {
        derivedStateOf { notoMind.checks.filter { !it.isCheck } }
    }
    val numberOfChecked by remember(key1 = notoMind.checks) {
        derivedStateOf { notoMind.checks.count { it.isCheck } }
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

    val sColor = if (notoMind.background != -1) {
        NoteIcon.background[notoMind.background].fgColor
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    val images = remember(notoMind.images) {
        notoMind.images.reversed().chunked(3)
    }

    val de = LocalDensity.current

    OutlinedCard(
        modifier = modifier.combinedClickable(
            onClick = { notoMind.id.let { onCardClick(it) } },
            onLongClick = { notoMind.id.let { onLongClick(it) } },
        ),
        border = if (notoMind.selected) {
            BorderStroke(3.dp, Color.Blue)
        } else {
            BorderStroke(
                1.dp,
                sColor,
            )
        },
        colors = CardDefaults.outlinedCardColors(containerColor = bg),
    ) {
        Box (Modifier.background(Color.White)){
            if (notoMind.background != -1) {
                Image(
                    painter = painterResource(id = NoteIcon.background[notoMind.background].bg),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(
                        with(de) { size.width.toDp() },
                        with(de) { size.height.toDp() },
                    ),
                )
            }

            Column(
                Modifier
                    .onSizeChanged {
                        size = it
                    },
            ) {
                if (notoMind.images.isNotEmpty()) {
                    images.forEach { imageList ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                        ) {
                            imageList.forEach {
                                AsyncImage(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(100.dp),
                                    model = it.path,
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }

                if (!notoMind.isImageOnly()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = notoMind.title.ifEmpty { notoMind.detail },
                            style = if (notoMind.title.isNotEmpty()) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            maxLines = 10,
                            textAlign = TextAlign.Center
                        )
                        if (!notoMind.isCheck) {
                            if (notoMind.title.isNotEmpty()) {
                                if (notoMind.detail.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = notoMind.detail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 10,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            unCheckNote.take(10).forEach {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = NoteIcon.CheckBoxOutlineBlank,
                                        contentDescription = "",
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        it.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                    )
                                }
                            }
                            if (unCheckNote.size > 10) {
                                Text(text = "....")
                            }
                            if (numberOfChecked > 0) {
                                Text(text = "+ $numberOfChecked checked items")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        FlowLayout2(
                            verticalSpacing = 4.dp,
                        ) {

                            if (notoMind.reminder > 0) {
                                ReminderCard(
                                    date = notoMind.reminderString,
                                    interval = notoMind.interval,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            notoMind.labels.forEach {
                                LabelCard(name = it.label, color = MaterialTheme.colorScheme.inverseSurface)
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteCardPreview() {
    NoteCard(
        notoMind = NotoMind(
            id = 1,
            title = "Parisa",
            detail = "Delfani Note",
            editDate = 314L,
            isCheck = true,
            reminder = Clock.System.now().toEpochMilliseconds(),
            color = 2,
            isPin = false,
            background = 3,
            selected = true,
            checks = listOf(
                NoteCheck(
                    id = 2418L,
                    noteId = 6429L,
                    content = "Maegan",
                    isCheck = false,
                    focus = false,
                ),
                NoteCheck(
                    id = 2418L,
                    noteId = 6429L,
                    content = "Book",
                    isCheck = false,
                    focus = false,
                ),
            ).toImmutableList(),
        ),
    )
}
