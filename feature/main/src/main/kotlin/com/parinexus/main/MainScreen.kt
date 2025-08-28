package com.parinexus.main

import ArchiveTopAppBar
import LabelTopAppBar
import MainTopAppBar
import NoteCard
import SearchTopBar
import SelectTopBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parinexus.common.result.Result
import com.parinexus.designsystem.component.NoteLoadingWheel
import com.parinexus.designsystem.icon.NoteIcon
import com.parinexus.model.Note
import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import com.parinexus.ui.ColorDialog
import com.parinexus.ui.DateDialog
import com.parinexus.ui.NotificationDialogNew
import com.parinexus.ui.TimeDialog
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import com.parinexus.designsystem.R as Rd

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun MainRoute(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    navigateToDetail: (Long) -> Unit,
    onOpenDrawer: () -> Unit,
    ) {
    val mainViewModel: MainViewModel = hiltViewModel()

    val mainState = mainViewModel.mainState.collectAsStateWithLifecycle()

    LaunchedEffect(
        key1 = Unit,
        block = {
            delay(100)
            mainViewModel.deleteEmptyNote()
        },
    )

    var showDialog by remember {
        mutableStateOf(false)
    }
    var showColor by remember {
        mutableStateOf(false)
    }
    var showRenameLabel by remember {
        mutableStateOf(false)
    }
    var showDeleteLabel by remember {
        mutableStateOf(false)
    }

    MainScreen(
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        modifier = modifier,
        mainState = mainState.value,
        navigateToEdit = navigateToDetail,
        searchState = mainViewModel.searchState,
        onSelectedCard = mainViewModel::onSelectCard,
        onClearSelected = mainViewModel::clearSelected,
        setAllPin = mainViewModel::setPin,
        setAllAlarm = { showDialog = true },
        setAllColor = { showColor = true },
        onCopy = mainViewModel::copyNote,
        onArchive = mainViewModel::setAllArchive,
        onSend = {
            mainViewModel.clearSelected()
        },
        onRenameLabel = { showRenameLabel = true },
        onDeleteLabel = { showDeleteLabel = true },
        onOpenDrawer = onOpenDrawer,
        onSetSearch = mainViewModel::onSetSearch,
    )
    val colorIndex by remember { mutableStateOf(0) }
    val dateDialogUiData = mainViewModel.dateTimeState.collectAsStateWithLifecycle()

    NotificationDialogNew(
        showDialog = showDialog,
        dateDialogUiData = dateDialogUiData.value,
        onDismissRequest = { showDialog = false },
        onSetAlarm = mainViewModel::setAlarm,
        onTimeChange = mainViewModel::onSetTime,
        onDateChange = mainViewModel::onSetDate,
        onIntervalChange = mainViewModel::onSetInterval,
        onDeleteAlarm = mainViewModel::deleteAlarm,
    )

    TimeDialog(
        state = mainViewModel.timePicker,
        showDialog = dateDialogUiData.value.showTimeDialog,
        onDismissRequest = mainViewModel::hideTime,
        onSetTime = mainViewModel::onSetTime,
    )
    DateDialog(
        state = mainViewModel.datePicker,
        showDialog = dateDialogUiData.value.showDateDialog,
        onDismissRequest = mainViewModel::hideDate,
        onSetDate = mainViewModel::onSetDate,
    )

    ColorDialog(
        show = showColor,
        onDismissRequest = { showColor = false },
        onColorClick = mainViewModel::setAllColor,
        currentColor = colorIndex ?: -1,
    )

    RenameLabelAlertDialog(
        show = showRenameLabel,
        label = "Name",
        onDismissRequest = { showRenameLabel = false },
        onChangeName = mainViewModel::renameLabel,
    )

    DeleteLabelAlertDialog(
        show = showDeleteLabel,
        onDismissRequest = { showDeleteLabel = false },
        onDelete = mainViewModel::deleteLabel,
    )
}

@OptIn(
    ExperimentalSharedTransitionApi::class,
)
@Composable
internal fun MainScreen(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    mainState: MainState,
    searchState: TextFieldState,
    navigateToEdit: (Long) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onSelectedCard: (Long) -> Unit = {},
    onClearSelected: () -> Unit = {},
    setAllPin: () -> Unit = {},
    setAllAlarm: () -> Unit = {},
    setAllColor: () -> Unit = {},
    onArchive: () -> Unit = {},
    onSend: () -> Unit = {},
    onCopy: () -> Unit = {},
    onRenameLabel: () -> Unit = {},
    onDeleteLabel: () -> Unit = {},
    onSetSearch: (SearchSort?) -> Unit = {},
    ) {

    when (mainState) {
        is MainState.Success -> {
            MainContent(
                modifier = modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                success = mainState,
                searchState = searchState,
                navigateToEdit = navigateToEdit,
                onSelectedCard = onSelectedCard,
                onClearSelected = onClearSelected,
                setAllPin = setAllPin,
                setAllAlarm = setAllAlarm,
                setAllColor = setAllColor,
                onArchive = onArchive,
                onSend = onSend,
                onCopy = onCopy,
                onRenameLabel = onRenameLabel,
                onDeleteLabel = onDeleteLabel,
                onOpenDrawer = onOpenDrawer,
                onSetSearch = onSetSearch,
            )
        }

        is MainState.Loading -> {
            LoadingState()
        }

        is MainState.Empty -> {
            EmptyState()
        }

        is MainState.Finish -> {}
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("main:loading"),
        contentAlignment = Alignment.Center,
    ) {
        NoteLoadingWheel(
            contentDesc = "Loading",
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier, noteType: NoteType = NoteType.NOTE) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .testTag("main:empty"),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Empty notes",
            textAlign = TextAlign.Center,
        )
    }
}

private fun noteUiStateItemsSize(
    topicUiState: Result<List<Note>>,
) = when (topicUiState) {
    is Result.Error -> 0 // Nothing
    is Result.Loading -> 1 // Loading bar
    is Result.Success -> topicUiState.data.size + 2
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    success: MainState.Success,
    searchState: TextFieldState,
    navigateToEdit: (Long) -> Unit = {},
    onSelectedCard: (Long) -> Unit = {},
    onClearSelected: () -> Unit = {},
    setAllPin: () -> Unit = {},
    setAllAlarm: () -> Unit = {},
    setAllColor: () -> Unit = {},
    onArchive: () -> Unit = {},
    onSend: () -> Unit = {},
    onCopy: () -> Unit = {},
    onRenameLabel: () -> Unit = {},
    onDeleteLabel: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onSetSearch: (SearchSort?) -> Unit, // ={}

    ) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val pinNotePad by remember(success.notoMinds) {
        derivedStateOf {
            success.notoMinds.partition { it.isPin }
        }
    }

    val noOfSelected = remember(success.notoMinds) {
        success.notoMinds.count { it.selected }
    }
    val isAllPin = remember(success.notoMinds) {
        success.notoMinds.filter { it.selected }
            .all { it.isPin }
    }
    var isGrid by rememberSaveable { mutableStateOf(true) }
    val onNoteClick: (Long) -> Unit = {
        if (noOfSelected > 0) {
            onSelectedCard(it)
        } else {
            navigateToEdit(it)
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(if (noOfSelected > 0) pinScrollBehavior.nestedScrollConnection else scrollBehavior.nestedScrollConnection),
        topBar = {
            when {
                noOfSelected > 0 -> {
                    SelectTopBar(
                        selectNumber = noOfSelected,
                        isAllPin = isAllPin,
                        scrollBehavior = pinScrollBehavior,
                        onClear = onClearSelected,
                        onPin = setAllPin,
                        onNoti = setAllAlarm,
                        onColor = setAllColor,
                        onArchive = onArchive,
                        onSend = onSend,
                        onCopy = onCopy,
                    )
                }

                success.noteType == NoteType.LABEL -> {
                    LabelTopAppBar(
                        label = "Label Name",
                        onNavigate = { },
                        scrollBehavior = scrollBehavior,
                        onDeleteLabel = onDeleteLabel,
                        onRenameLabel = onRenameLabel,
                    )
                }

                success.noteType == NoteType.NOTE -> {
                    if (success.isSearch) {
                        SearchTopBar(
                            state = searchState,
                        )
                    } else {
                        MainTopAppBar(
                            onNavigate = onOpenDrawer,
                            scrollBehavior = scrollBehavior,
                            isGrid = isGrid,
                            onToggleGrid = { isGrid = !isGrid },
                        )
                    }
                }

                success.noteType == NoteType.REMAINDER -> {
                    ArchiveTopAppBar(
                        name = "Remainder",
                        onNavigate = { },
                        scrollBehavior = scrollBehavior,

                    )
                }

                success.noteType == NoteType.ARCHIVE -> {
                    ArchiveTopAppBar(
                        onNavigate = { },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },

    ) { paddingValues ->

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .testTag("main:list"),
            columns = StaggeredGridCells.Fixed(if (isGrid) 2 else 1),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,

        ) {
            if (success.isSearch && success.notoMinds.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    LabelBox(
                        title = stringResource(Rd.string.modules_designsystem_types),
                        success.types,
                        onItemClick = onSetSearch,
                    )
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    LabelBox(
                        title = stringResource(Rd.string.modules_designsystem_labels),
                        success.label,
                        onItemClick = onSetSearch,
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(text = "Colors")
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),

                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        success.color.forEach {
                            Surface(
                                onClick = {
                                    onSetSearch(it)
                                },
                                shape = CircleShape,
                                color = if (it.colorIndex == -1) Color.White else NoteIcon.noteColors[it.colorIndex],
                                modifier = Modifier
                                    .width(40.dp)
                                    .aspectRatio(1f),

                            ) {
                                if (it.colorIndex == -1) {
                                    Icon(
                                        imageVector = NoteIcon.FormatColorReset,
                                        contentDescription = "done",
                                        tint = Color.Gray,
                                        modifier = Modifier.padding(4.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (!success.isSearch && success.notoMinds.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyState(noteType = success.noteType)
                }
            }
            if (pinNotePad.first.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Rd.string.modules_designsystem_pin),
                    )
                }
            }
            noteItems(
                modifier = Modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                items = pinNotePad.first.toImmutableList(),
                onNoteClick = onNoteClick,
                onSelectedCard = onSelectedCard,
            )

            if (pinNotePad.first.isNotEmpty() && pinNotePad.second.isNotEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(Rd.string.modules_designsystem_other),
                    )
                }
            }
            noteItems(
                modifier = Modifier,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                items = pinNotePad.second.toImmutableList(),
                onNoteClick = onNoteClick,
                onSelectedCard = onSelectedCard,
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
fun LazyStaggeredGridScope.noteItems(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    items: List<NotoMind>,
    onNoteClick: (Long) -> Unit,
    onSelectedCard: (Long) -> Unit,
) = items(
    items = items,
    key = { it.id },
    itemContent = { note ->
        with(sharedTransitionScope) {
            NoteCard(
                modifier = modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("note${note.id}"),
                    animatedVisibilityScope = animatedContentScope,

                ),
                notoMind = note,
                onCardClick = onNoteClick,
                onLongClick = onSelectedCard,
            )
        }
    },
)

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabelBox(
    title: String = "Label",
    list: List<SearchSort> = emptyList(),
    onItemClick: (SearchSort?) -> Unit, // = {},
) {
    var showMore by remember { mutableStateOf(false) }
    FlowRow(
        Modifier.animateContentSize(),
        maxItemsInEachRow = 3,
        maxLines = if (showMore) Int.MAX_VALUE else 2,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(modifier = Modifier.weight(1f), text = title)
            if (list.size > 3) {
                TextButton(onClick = { showMore = !showMore }) {
                    Text(
                        text = if (!showMore) {
                            stringResource(id = Rd.string.modules_designsystem_more)
                        } else {
                            stringResource(
                                id = Rd.string.modules_designsystem_less,
                            )
                        },
                    )
                }
            }
        }
        list
            // .take()
            .forEach { searchSort ->
                val item = when (searchSort) {
                    is SearchSort.Label -> Pair(
                        stringArrayResource(Rd.array.modules_designsystem_search_sort)[searchSort.iconIndex],
                        NoteIcon.searchIcons[searchSort.iconIndex],

                    )

                    is SearchSort.Type -> Pair(
                        stringArrayResource(Rd.array.modules_designsystem_search_sort)[searchSort.index],
                        NoteIcon.searchIcons[searchSort.index],
                    )

                    is SearchSort.Color -> Pair(
                        "",
                        NoteIcon.searchIcons[0],
                    )
                }
                SearchLabel(
                    modifier = Modifier.clickable { onItemClick(searchSort) },
                    iconId = item.second,
                    name = item.first,
                )
            }
    }
}

@Composable
fun SearchLabel(
    modifier: Modifier = Modifier,
    iconId: ImageVector = NoteIcon.Label,
    name: String = "Label",
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .width(72.dp)
                .aspectRatio(1f),
        ) {
            Icon(
                imageVector = iconId,
                contentDescription = "label icon",
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = name)
    }
}
