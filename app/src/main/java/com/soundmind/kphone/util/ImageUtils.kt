/*
 * Copyright 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.soundmind.kphone.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.annotation.ColorInt

/**
 * Utility class for manipulating images.
 */
object ImageUtils {
    private val CHANNEL_RANGE = 0 until (1 shl 18)

    fun convertYuv420888ImageToBitmap(image: Image): Bitmap {
        //require(image.format == ImageFormat.YUV_420_888) {
        //    "Unsupported image format $(image.format)"
        //}

        val planes = image.planes

        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        val width = image.width
        val height = image.height
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        val yuvBytes = planes.map { plane ->
            val buffer = plane.buffer
            val yuvBytes = ByteArray(buffer.capacity())
            buffer[yuvBytes]
            buffer.rewind()  // Be kind…
            yuvBytes
        }

        //val yRowStride = planes[0].rowStride
        //val uvRowStride = planes[1].rowStride
        //val uvPixelStride = planes[1].pixelStride
        //val width = image.width
        //val height = image.height
        @ColorInt val argb8888 = IntArray(width * height)
        var i = 0
        for (y in 0 until height) {
            val pY = yRowStride * y
            val uvRowStart = uvRowStride * (y shr 1)
            for (x in 0 until width) {
                val uvOffset = (x shr 1) * uvPixelStride
                argb8888[i++] =
                    yuvToRgb(
                        yuvBytes[0][pY + x].toIntUnsigned(),
                        yuvBytes[1][uvRowStart + uvOffset].toIntUnsigned(),
                        yuvBytes[2][uvRowStart + uvOffset].toIntUnsigned()
                    )
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(argb8888, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun convertYuv420DataToBitmap(image: YUV420Data): Bitmap {
        //require(image.format == ImageFormat.YUV_420_888) {
        //    "Unsupported image format $(image.format)"
        //}

        //val planes = image.planes

        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        /*
        val yuvBytes = planes.map { plane ->
            val buffer = plane.buffer
            val yuvBytes = ByteArray(buffer.capacity())
            buffer[yuvBytes]
            buffer.rewind()  // Be kind…
            yuvBytes
        }
        */

        val yRowStride = image.width
        val uvRowStride = image.width / 2
        val uvPixelStride = uvRowStride
        val width = image.width
        val height = image.height
        @ColorInt val argb8888 = IntArray(width * height)
        var i = 0
        for (y in 0 until height) {
            val pY = yRowStride * y
            val uvRowStart = uvRowStride * (y shr 1)
            for (x in 0 until width) {
                val uvOffset = (x shr 1) * uvPixelStride
                argb8888[i++] =
                    yuvToRgb(
                        image.yPlane[pY + x].toIntUnsigned(),
                        image.uPlane[uvRowStart + uvOffset].toIntUnsigned(),
                        image.vPlane[uvRowStart + uvOffset].toIntUnsigned()
                    )
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(argb8888, 0, width, 0, 0, width, height)
        return bitmap
    }


    fun rotateAndCrop(
        bitmap: Bitmap,
        imageRotationDegrees: Int,
        cropRect: Rect
    ): Bitmap {
        val matrix = Matrix()
        matrix.preRotate(imageRotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height(),
            matrix,
            true
        )
    }

    @ColorInt
    private fun yuvToRgb(nY: Int, nU: Int, nV: Int): Int {
        var nY = nY
        var nU = nU
        var nV = nV
        nY -= 16
        nU -= 128
        nV -= 128
        nY = nY.coerceAtLeast(0)

        // This is the floating point equivalent. We do the conversion in integer
        // because some Android devices do not have floating point in hardware.
        // nR = (int)(1.164 * nY + 2.018 * nU);
        // nG = (int)(1.164 * nY - 0.813 * nV - 0.391 * nU);
        // nB = (int)(1.164 * nY + 1.596 * nV);
        var nR = 1192 * nY + 1634 * nV
        var nG = 1192 * nY - 833 * nV - 400 * nU
        var nB = 1192 * nY + 2066 * nU

        // Clamp the values before normalizing them to 8 bits.
        nR = nR.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nG = nG.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        nB = nB.coerceIn(CHANNEL_RANGE) shr 10 and 0xff
        return -0x1000000 or (nR shl 16) or (nG shl 8) or nB
    }

    fun convertJpegByteArrayToBitmap(jpegByteArray: ByteArray): Bitmap? {
        // 1. Decode JPEG to Bitmap (Intermediate Step)
        val bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)
            ?: run {
                Log.e("ImageUtils", "Failed to decode JPEG ByteArray to Bitmap")
                return null
            }
        return bitmap
    }

    fun convertJpegByteArrayToYUV420(jpegByteArray: ByteArray): YUV420Data? {
        // 1. Decode JPEG to Bitmap (Intermediate Step)
        val bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)
            ?: run {
                Log.e("ImageUtils", "Failed to decode JPEG ByteArray to Bitmap")
                return null
            }
        val width = bitmap.width
        val height = bitmap.height

        // 2. Bitmap to RGB Pixels
        val rgbPixels = IntArray(width * height)
        bitmap.getPixels(rgbPixels, 0, width, 0, 0, width, height)
        bitmap.recycle()

        // 3. RGB to YUV Conversion and Subsampling
        val yPlane = ByteArray(width * height)
        val uPlane = ByteArray((width / 2) * (height / 2))
        val vPlane = ByteArray((width / 2) * (height / 2))

        var yIndex = 0
        var uvIndex = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rgb = rgbPixels[y * width + x]
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF

                // RGB to YUV Conversion
                val yValue = ((0.299 * r) + (0.587 * g) + (0.114 * b)).toInt()
                yPlane[yIndex++] = yValue.toByte()

                // Subsampling (4:2:0)
                if (y % 2 == 0 && x % 2 == 0) {
                    var uSum = 0
                    var vSum = 0
                    var count = 0
                    for (i in 0 until 2) {
                        for (j in 0 until 2) {
                            val currentY = y + i
                            val currentX = x + j

                            if (currentY < height && currentX < width) {
                                val currentRGB = rgbPixels[currentY * width + currentX]
                                val currentR = (currentRGB shr 16) and 0xFF
                                val currentG = (currentRGB shr 8) and 0xFF
                                val currentB = currentRGB and 0xFF

                                val uValue = ((-0.169 * currentR) - (0.331 * currentG) + (0.500 * currentB) + 128).toInt()
                                val vValue = ((0.500 * currentR) - (0.419 * currentG) - (0.081 * currentB) + 128).toInt()

                                uSum += uValue
                                vSum += vValue
                                count++
                            }
                        }
                    }
                    val uValue = if(count>0) (uSum / count) else 0
                    val vValue = if(count>0) (vSum / count) else 0

                    uPlane[uvIndex] = uValue.toByte()
                    vPlane[uvIndex] = vValue.toByte()
                    uvIndex++
                }
            }
        }

        return YUV420Data(
            width,
            height,
            yPlane,
            uPlane,
            vPlane
        )
    }

    /**
     * Converts YUV 4:2:0 data (as separate Y, U, and V ByteArrays) to a Bitmap.
     *
     * @param yPlane The Y (luma) plane data.
     * @param uPlane The U (Cb) plane data.
     * @param vPlane The V (Cr) plane data.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A Bitmap object, or null if the conversion fails.
     */
    fun convertYUV420ToBitmap(
        yPlane: ByteArray,
        uPlane: ByteArray,
        vPlane: ByteArray,
        width: Int,
        height: Int
    ): Bitmap? {
        if (yPlane.size != width * height) {
            Log.e(
                "ImageUtils",
                "Y plane size mismatch: expected ${width * height}, got ${yPlane.size}"
            )
            return null
        }
        if (uPlane.size != (width / 2) * (height / 2)) {
            Log.e(
                "ImageUtils",
                "U plane size mismatch: expected ${(width / 2) * (height / 2)}, got ${uPlane.size}"
            )
            return null
        }
        if (vPlane.size != (width / 2) * (height / 2)) {
            Log.e(
                "ImageUtils",
                "V plane size mismatch: expected ${(width / 2) * (height / 2)}, got ${vPlane.size}"
            )
            return null
        }

        val argb = IntArray(width * height)
        val ySize = width * height
        var yIndex = 0
        var uvIndex = 0
        var index = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val yValue = yPlane[yIndex].toInt() and 0xFF
                val uValue = uPlane[uvIndex / 4].toInt() and 0xFF
                val vValue = vPlane[uvIndex / 4].toInt() and 0xFF
                //val uValue = uPlane[uvIndex / 4].toInt() and 0xFF
                //val vValue = vPlane[uvIndex / 4].toInt() and 0xFF

                val u = uValue - 128
                val v = vValue - 128

                //Integer approximation of YUV to RGB
                //val y1192 = 1192 * yValue
                //var r = (y1192 + 1634 * v)
                //var g = (y1192 - 833 * v - 400 * u)
                //var b = (y1192 + 2066 * u)

                var _r = yValue + 1.4075 * v
                var _g = yValue - 0.3455 * u - (0.7169 * v)
                var _b = yValue + 1.7790 * u

                //Clamp to 0-262143
                //r = r.coerceIn(0, 262143)
                //g = g.coerceIn(0, 262143)
                //b = b.coerceIn(0, 262143)
                val r = _r.toInt().coerceIn(0, 255)
                val g = _g.toInt().coerceIn(0, 255)
                val b = _b.toInt().coerceIn(0, 255)

                //argb[yIndex] = -0x1000000 or
                //        ((r shl 6) and 0xff0000) or
                //        ((g shr 2) and 0xff00) or
                //        ((b shr 10) and 0xff)
               argb[yIndex] = (0xff000000).toInt() or //-0x1000000 or
                        ((r shl 16) and 0xff0000) or
                        ((g shl 8) and 0xff00) or
                        (b and 0xff)
                //(((r shr 10) shl 16) and 0xff0000) or
                //        (((g shr 10) shl 8) and 0xff00) or
                //        ((b shr 10) and 0xff)

                yIndex++
                if (x % 2 == 0) {
                    uvIndex+=1
                }
                index++
            }
            if(y % 2 == 0) {
                uvIndex = (y/2) * width
            }
        }

        return Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888)
    }
}


data class YUV420Data(
    val width: Int,
    val height: Int,
    val yPlane: ByteArray,
    val uPlane: ByteArray,
    val vPlane: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as YUV420Data

        if (width != other.width) return false
        if (height != other.height) return false
        if (!yPlane.contentEquals(other.yPlane)) return false
        if (!uPlane.contentEquals(other.uPlane)) return false
        if (!vPlane.contentEquals(other.vPlane)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + yPlane.contentHashCode()
        result = 31 * result + uPlane.contentHashCode()
        result = 31 * result + vPlane.contentHashCode()
        return result
    }
}

private fun Byte.toIntUnsigned(): Int {
    return toInt() and 0xFF
}
