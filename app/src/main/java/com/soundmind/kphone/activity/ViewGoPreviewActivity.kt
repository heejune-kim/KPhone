package com.soundmind.kphone.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.lifecycle.MutableLiveData
import com.example.android.camera.utils.YuvToRgbConverter
import com.soundmind.kphone.KPhoneApplication
import com.soundmind.kphone.R
import com.soundmind.kphone.main.ViewGoFragment.Companion.DESIRED_HEIGHT_CROP_PERCENT
import com.soundmind.kphone.main.ViewGoFragment.Companion.DESIRED_WIDTH_CROP_PERCENT
import com.soundmind.kphone.util.ImageUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.text.insert

class ViewGoPreviewActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var liveButton: ImageButton
    private lateinit var imageView: ImageView
    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    val imageCropPercentages = MutableLiveData<Pair<Int, Int>>()
        .apply { value = Pair(DESIRED_HEIGHT_CROP_PERCENT, DESIRED_WIDTH_CROP_PERCENT) }

    val systemLanguage: String = Locale.getDefault().toString().subSequence(0, 2).toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        val context = applicationContext as KPhoneApplication
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_preview_activity)

        previewView = findViewById(R.id.previewView)

        // Set up the listeners for take photo and video capture buttons
        captureButton = findViewById(R.id.captureButton)
        captureButton.setOnClickListener { takePhoto() }

        liveButton = findViewById(R.id.liveButton)
        liveButton.setOnClickListener {
            val intent = Intent(context, ViewGoActivity::class.java)
            intent.putExtra("lang", systemLanguage)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent)
        }
        //imageView = findViewById(R.id.imageView)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }


        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto_toFile() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("TAG", msg)
                }
            }
        )
    }

    fun saveBitmapToMediaStore(
        context: Context,
        bitmap: Bitmap,
        displayName: String,
        mimeType: String = "image/jpeg" // Example: "image/png"
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        }

        val resolver = context.contentResolver
        var imageUri: Uri? = null
        try {
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IOException("Failed to create new MediaStore record.")
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Adjust format and quality as needed
            }
        } catch (e: IOException) {
            Log.e("BitmapUtils", "Error saving Bitmap to MediaStore", e)
            imageUri?.let { resolver.delete(it, null, null) } // Clean up if we inserted a row
            return null
        }
        return imageUri
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exc.message}", exc)
                }

                @androidx.camera.core.ExperimentalGetImage
                //@OptIn(ExperimentalGetImage::class)
                fun doIt(imageProxy: ImageProxy, imageBytes: ByteArray) {
                    val mediaImage = imageProxy.image ?: return

                    //val builder = ImageAnalysis.Builder()
                    //builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888); // or YUV_420_888
                    //val imageAnalysis = builder.build()
                    val convertImageToBitmap = ImageUtils.convertJpegByteArrayToBitmap(imageBytes)

                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                    // We requested a setTargetAspectRatio, but it's not guaranteed that's what the camera
                    // stack is able to support, so we calculate the actual ratio from the first frame to
                    // know how to appropriately crop the image we want to analyze.
                    val imageHeight = mediaImage.height
                    val imageWidth = mediaImage.width

                    val actualAspectRatio = imageWidth / imageHeight

                    val cropRect = Rect(0, 0, imageWidth, imageHeight)

                    // If the image has a way wider aspect ratio than expected, crop less of the height so we
                    // don't end up cropping too much of the image. If the image has a way taller aspect ratio
                    // than expected, we don't have to make any changes to our cropping so we don't handle it
                    // here.
                    val currentCropPercentages = imageCropPercentages.value ?: return
                    if (actualAspectRatio > 3) {
                        val originalHeightCropPercentage = currentCropPercentages.first
                        val originalWidthCropPercentage = currentCropPercentages.second
                        imageCropPercentages.value =
                            Pair(originalHeightCropPercentage / 2, originalWidthCropPercentage)
                    }

                    // If the image is rotated by 90 (or 270) degrees, swap height and width when calculating
                    // the crop.
                    val cropPercentages = imageCropPercentages.value ?: return
                    val heightCropPercent = cropPercentages.first
                    val widthCropPercent = cropPercentages.second
                    val (widthCrop, heightCrop) = when (rotationDegrees) {
                        90, 270 -> Pair(heightCropPercent / 100f, widthCropPercent / 100f)
                        else -> Pair(widthCropPercent / 100f, heightCropPercent / 100f)
                    }

                    cropRect.inset(
                        (imageWidth * widthCrop / 2).toInt(),
                        (imageHeight * heightCrop / 2).toInt()
                    )
                    val croppedBitmap = ImageUtils.rotateAndCrop(convertImageToBitmap!!, rotationDegrees, cropRect)

                    val context = this@ViewGoPreviewActivity
                    val intent = Intent(context, ViewGoPreviewActivity::class.java)
                    intent.putExtra("type", "shot")
                    intent.putExtra("lang", Locale.getDefault().language)
                    intent.putExtra("image", croppedBitmap)
                    intent.putExtra("height", mediaImage.height)
                    intent.putExtra("width", mediaImage.width)
                    context.startActivity(intent)

                    runOnUiThread {
                        imageView.setImageBitmap(croppedBitmap)
                    }

                }

                fun callActivity() {
                }

                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {
                    Log.d("TAG", "Photo capture succeeded")
                    //val uri = saveBitmapToMediaStore(this@ViewGoPreviewActivity, image.toBitmap(), "image.jpg")
                    // Access the image data in memory

                    val app = applicationContext as KPhoneApplication
                    app.sharedImage = image

                    val buffer: ByteBuffer = image.planes[0].buffer
                    buffer.rewind()
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)
                    //doIt(image, bytes)

                    val context = this@ViewGoPreviewActivity
                    val intent = Intent(context, ViewGoActivity::class.java)
                    intent.putExtra("type", "shot")
                    intent.putExtra("lang", Locale.getDefault().language)
                    //intent.putExtra("image", uri.toString())
                    intent.putExtra("height", image.height)
                    intent.putExtra("width", image.width)
                    //image.close()
                    context.startActivity(intent)

                    // At this point, 'bytes' contains the image data as a ByteArray
                    // You can now pass the 'bytes' to your image processing method.
                    //processImageBytes(bytes)

                    // Don't forget to close the image.
                }
            }
        )
    }


    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val builder = ImageAnalysis.Builder()
            //builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888); // or YUV_420_888
            //builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888); // or YUV_420_888
            builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888); // or YUV_420_888
            val imageAnalysis = builder.build()

            // Preview
            val preview = Preview.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            imageCapture = ImageCapture.Builder()
                //.setBufferFormat(ImageFormat.YUV_420_888)
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun processImageBytes(imageBytes: ByteArray) {
        // Here you will manipulate the imageBytes, for example:
        // 1. Send it to a server.
        // 2. Display it in an ImageView
        // 3. Perform any needed transformation.

        Log.d("TAG", "imageBytes Size: ${imageBytes.size}")
        displayImage(imageBytes)
    }

    private fun displayImage(imageBytes: ByteArray) {
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        var matrix: Matrix = Matrix();
        var stream: ByteArrayOutputStream = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        var imageBytes = stream.toByteArray();
        var exifInterface: ExifInterface = ExifInterface(ByteArrayInputStream(imageBytes));
        var orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        orientation = ExifInterface.ORIENTATION_ROTATE_90
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90F)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180F);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true)
            }
        }
        // Run on the main thread to update UI
        runOnUiThread {
            imageView.setImageBitmap(bitmap)
        }
    }

    /*
    fun getCameraPhotoOrientation(context: Context, imageUri: Uri, imagePath: String): Int {
        var rotate: Int = 0
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
    */

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}