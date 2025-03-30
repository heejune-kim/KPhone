package com.soundmind.kphone

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

class KPhoneApplication: Application() {
    var sharedImage: ImageProxy? = null
}