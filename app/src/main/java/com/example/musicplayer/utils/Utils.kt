package com.example.musicplayer.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object Utils {
    fun Context.hideKeyBoard(editText: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun Context.showKeyboard(editText: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun Context.getColorFromAttr(attr: Int): Int {
        val typedValue = TypedValue()
        val theme: Resources.Theme = theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getAlbumArt(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            // Set the data source to the MP3 file
            retriever.setDataSource(filePath)

            // Retrieve the album art (if any)
            val art = retriever.embeddedPicture
            println("art is null $art")

            // Return album art if it exists, otherwise null
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null  // No album art found
    }

    @SuppressLint("DefaultLocale")
    fun formatDuration(duration: Long): String {
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        val result = String.format("%02d:%02d", minutes, seconds)
        return result
    }

    fun View.setOnSWipeListener(activity: Activity) {
        val gestureDetector = GestureDetector(activity, SwipeGestureListener(activity))
        this.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    fun getMP3Metadata(uri: Uri, context: Context): Pair<String, String> {
        val retriever = MediaMetadataRetriever()
        var title = context.resources.getString(R.string.unknown_song)
        var artist = context.resources.getString(R.string.unknown_artist)

        try {
            retriever.setDataSource(context, uri)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: title
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: artist
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        return Pair(title, artist)
    }

    fun blurBitmap(context: Context, bitmap: Bitmap, radius: Float = 40f): Bitmap {
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width, bitmap.height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)

        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(renderScript, inputBitmap)
        val output = Allocation.createTyped(renderScript, input.type)
        val script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

        script.setRadius(radius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(outputBitmap)
        renderScript.destroy()

        return outputBitmap
    }

    fun setBackgroundColor(
        selected: List<TextView>,
        unselected: List<TextView>,
        selectedColor: Int,
        unselectedColor: Int
    ) {
        selected.onEach {
            it.backgroundTintList = ColorStateList.valueOf(selectedColor)
        }
        unselected.onEach {
            it.backgroundTintList = ColorStateList.valueOf(unselectedColor)
        }
    }

}

class SwipeGestureListener(val activity: Activity) : GestureDetector.SimpleOnGestureListener() {
    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        println("SwipeGestureListener - onFLing: $velocityY")
        try {
            val diffY = e2.y.minus(e1!!.y)
            val diffX = e2.x.minus(e1.x)

            if (abs(diffX) <= abs(diffY)) {
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        activity.finish()
                        activity.overridePendingTransition(
                            R.anim.fade_in_quick, R.anim.exit_to_top
                        )
                    }
                    return true
                }
            }
        } catch (ex: Exception) {
            println("SwipeGestureListener: ${ex.printStackTrace()}")
        }

        return false
    }

}