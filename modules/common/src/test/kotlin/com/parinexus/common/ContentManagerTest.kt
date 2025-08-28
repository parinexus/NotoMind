package com.parinexus.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.every
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ContentManagerTest {

    private lateinit var contentManager: ContentManager
    private lateinit var photoDir: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        contentManager = ContentManager(context)
        photoDir = File(context.filesDir, "photo")
        if (photoDir.exists()) photoDir.deleteRecursively()
    }

    @After
    fun tearDown() {
        unmockkStatic(FileProvider::class)
        if (photoDir.exists()) photoDir.deleteRecursively()
    }

    @Test
    fun `saveImage copies bytes to photo dir and returns timestamp`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val src = File(context.filesDir, "src.txt").apply {
            writeText("hello-world")
        }
        val uri = Uri.fromFile(src).toString()

        val ret = contentManager.saveImage(uri)

        assertTrue(ret > 0, "Expected a positive timestamp from saveImage")

        val destPath = contentManager.getImagePath(ret)
        val dest = File(destPath)
        assertTrue(dest.exists(), "Destination image must exist")
        assertEquals(src.readText(), dest.readText(), "Copied content must match source")
    }

    @Test
    fun `pictureUri uses FileProvider with correct authority and file path`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val authoritySlot = slot<String>()
        val fileSlot = slot<File>()

        mockkStatic(FileProvider::class)
        every {
            FileProvider.getUriForFile(
                context,
                capture(authoritySlot),
                capture(fileSlot)
            )
        } answers {
            Uri.parse("content://${authoritySlot.captured}/${fileSlot.captured.name}")
        }

        val uriStr = contentManager.pictureUri()

        assertEquals(context.packageName + ".provider", authoritySlot.captured)
        assertEquals("Image_\$2.jpg", fileSlot.captured.name)
        assertTrue(uriStr.startsWith("content://"), "Returned URI should be content scheme")
    }

    @Test
    fun `getImagePath returns expected path`() {
        val anyId = 123456789L
        val expected = File(photoDir, "Image_${anyId}.jpg").absolutePath
        assertEquals(expected, contentManager.getImagePath(anyId))
    }

    @Test
    fun `saveBitmap writes a non-empty JPEG file`() {
        val path = contentManager.getImagePath(9999L)
        val bmp = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        contentManager.saveBitmap(path, bmp)

        val out = File(path)
        assertTrue(out.exists(), "Bitmap file must exist")
        assertTrue(out.length() > 0, "Bitmap file must not be empty")
    }

    @Test
    fun `dataFile returns file under drawingfile folder and creates directory`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val id = 321L

        val f = contentManager.dataFile(id)

        val expectedDir = File(context.filesDir, "drawingfile")
        val expectedFile = File(expectedDir, "data_${id}.json")

        assertEquals(expectedFile.absolutePath, f.absolutePath)
        assertTrue(expectedDir.exists(), "drawingfile directory must be created")
    }
}