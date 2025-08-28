package com.parinexus.gallery

import com.parinexus.model.NoteImage

data class GalleryUiState(
    val images: List<NoteImage> = emptyList(),
    val currentIndex: Int = 0,
)
