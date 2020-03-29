package openhoangnc.browser.unit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

object ViewUnit {
    fun bound(context: Context?, view: View) {
        val windowWidth = getWindowWidth(context)
        val windowHeight = getWindowHeight(context)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(windowWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(windowHeight, View.MeasureSpec.EXACTLY)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    fun capture(view: View?, width: Float, height: Float, config: Bitmap.Config?): Bitmap {
        view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), config!!)
        bitmap.eraseColor(Color.WHITE)
        val canvas = Canvas(bitmap)
        val left = view?.left
        val top = view?.top
        val status = canvas.save()
        canvas.translate(-left?.toFloat()!!, -top?.toFloat()!!)
        val scale = width / view.width
        canvas.scale(scale, scale, left.toFloat(), top.toFloat())
        view.draw(canvas)
        canvas.restoreToCount(status)
        val alphaPaint = Paint()
        alphaPaint.color = Color.TRANSPARENT
        canvas.drawRect(0f, 0f, 1f, height, alphaPaint)
        canvas.drawRect(width - 1f, 0f, width, height, alphaPaint)
        canvas.drawRect(0f, 0f, width, 1f, alphaPaint)
        canvas.drawRect(0f, height - 1f, width, height, alphaPaint)
        canvas.setBitmap(null)
        return bitmap
    }

    fun getDensity(context: Context?): Float {
        return context?.resources?.displayMetrics?.density!!
    }

    private fun getWindowHeight(context: Context?): Int {
        return context?.resources?.displayMetrics?.heightPixels!!
    }

    fun getWindowWidth(context: Context?): Int {
        return context?.resources?.displayMetrics?.widthPixels!!
    }
}