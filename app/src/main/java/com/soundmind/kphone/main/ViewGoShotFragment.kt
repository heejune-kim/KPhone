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

package com.soundmind.kphone.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.soundmind.kphone.R
//import com.google.mlkit.showcase.translate.R
import com.soundmind.kphone.databinding.ViewgoshotFragmentBinding // .MainFragmentBinding
//import kotlinx.android.synthetic.main.main_fragment.*
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.soundmind.kphone.KPhoneApplication
import com.soundmind.kphone.analyzer.TextAnalyzer
import com.soundmind.kphone.util.ImageUtils
import com.soundmind.kphone.util.Language
import java.nio.ByteBuffer
import java.util.Locale

class ViewGoShotFragment : Fragment() {

    val imageCropPercentages = MutableLiveData<Pair<Int, Int>>()
        .apply { value = Pair(
            ViewGoFragment.Companion.DESIRED_HEIGHT_CROP_PERCENT, ViewGoFragment.Companion.DESIRED_WIDTH_CROP_PERCENT
        ) }

    private val detector =
        TextRecognition.getClient( KoreanTextRecognizerOptions.Builder().build())

    companion object {
        fun newInstance() = ViewGoShotFragment()

        private const val TAG = "ViewGoShotFragment"
        // We only need to analyze the part of the image that has text, so we set crop percentages
        // to avoid analyze the entire image from the live camera feed.
        //const val DESIRED_WIDTH_CROP_PERCENT = 8
        //const val DESIRED_HEIGHT_CROP_PERCENT = 74
        const val DESIRED_WIDTH_CROP_PERCENT = 2
        const val DESIRED_HEIGHT_CROP_PERCENT = 2

        // This is an arbitrary number we are using to keep tab of the permission
        // request. Where an app has multiple context for requesting permission,
        // this can help differentiate the different contexts
        private const val REQUEST_CODE_PERMISSIONS = 10

        // This is an array of all the permission specified in the manifest
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    //private var displayId: Int = -1
    //private val viewModel: MainViewModel by viewModels()
    private val viewModel: KPhoneModule by viewModels()
    //private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var container: ConstraintLayout
    private lateinit var shotView: ImageView

    val systemLanguage: String = Locale.getDefault().toString().subSequence(0, 2).toString()
    /** Blocking camera and inference operations are performed using this executor. */
    //private lateinit var cameraExecutor: ExecutorService

    /** UI callbacks are run on this executor. */
    //private lateinit var scopedExecutor: ScopedExecutor

    private var _binding: ViewgoshotFragmentBinding? = null
    private val binding get() = _binding!!

    @OptIn(ExperimentalGetImage::class)
    fun drawImage(imageProxy: ImageProxy) {
        //val bitmap = imageProxy.toBitmap()
        val buffer: ByteBuffer = imageProxy.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)

        val mediaImage = imageProxy.image ?: return

        //val builder = ImageAnalysis.Builder()
        //builder.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888); // or YUV_420_888
        //val imageAnalysis = builder.build()
        val convertImageToBitmap = ImageUtils.convertJpegByteArrayToBitmap(bytes)

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

        binding.shotView.setImageBitmap(croppedBitmap)

        detector.process(InputImage.fromBitmap(croppedBitmap, 0))
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                //result.value = visionText.text
                //binding.translatedText.text = visionText.text
                binding.progressText.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.INVISIBLE
                viewModel.translateText(visionText.text)
                //Toast.makeText(context, visionText.text, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                // Task failed with an exception
                Log.e(TAG, "Text recognition error", exception)
                val message = getErrorMessage(exception)
                message?.let {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }

        //imageProxy.close()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return inflater.inflate(R.layout.main_fragment, container, false)
        _binding = ViewgoshotFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        //systemLanguage = arguments?.getString("lang").toString()
        //val image = arguments?.getString("image").toString().toUri()
        //_binding?.imageView?.setImageURI(image)

        //_binding?.imageView?.setImageBitmap()
        viewModel.checkAndDownload(systemLanguage)
        viewModel.targetLang = systemLanguage

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        // Shut down the scoped executor. The camera executor will automatically shut down its
        // background threads after 60s of idling.
        //scopedExecutor.shutdown()
    }

    //@RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val image = savedInstanceState?.getParcelable<Bitmap>("image", Bitmap::class.java)
        container = view as ConstraintLayout
        shotView = container.findViewById(R.id.shotView)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        // Enable the back button
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            // Handle the back button click, navigate up in the hierarchy
            activity?.finish()
            //findNavController().navigateUp()
        }

        // Initialize our background executor
        //cameraExecutor = Executors.newCachedThreadPool()
        //scopedExecutor = ScopedExecutor(cameraExecutor)

        //viewModel.executor = cameraExecutor

        // Request camera permissions
        if (allPermissionsGranted()) {
            // Wait for the views to be properly laid out
            //viewFinder.post {
                // Keep track of the display in which this view is attached
                //displayId = viewFinder.display.displayId

                // Set up the camera and its use cases
                //setUpCamera()
            //}
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Get available language list and set up the target language spinner
        // with default selections.
        /*
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item, viewModel.availableLanguages
        )
        */

        /*
        binding.targetLangSelector.adapter = adapter
        binding.targetLangSelector.setSelection(adapter.getPosition(Language(systemLanguage)))
        binding.targetLangSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.targetLang.value = adapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        */

        /*
        viewModel.targetLang.value = Language(systemLanguage)

        viewModel.sourceLang.observe(viewLifecycleOwner, Observer {
            //binding.srcLang.text = it.displayName
            //val str = it.displayName
        })
        viewModel.translatedText.observe(viewLifecycleOwner, Observer { resultOrError ->
            resultOrError?.let {
                if (it.error != null) {
                    binding.translatedText.error = resultOrError.error?.localizedMessage
                } else {
                    binding.translatedText.text = resultOrError.result
                }
            }
        })
        viewModel.modelDownloading.observe(viewLifecycleOwner, Observer { isDownloading ->
            binding.progressBar.visibility = if (isDownloading) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            binding.progressText.visibility = binding.progressBar.visibility
        })
        */

        /*
        binding.overlay.apply {
            setZOrderOnTop(true)
            holder.setFormat(PixelFormat.TRANSPARENT)
            holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {}

                override fun surfaceCreated(holder: SurfaceHolder) {
                    holder?.let {
                        drawOverlay(
                            it,
                            DESIRED_HEIGHT_CROP_PERCENT,
                            DESIRED_WIDTH_CROP_PERCENT
                        )
                    }
                }
            })
        }
        */
        val app = activity?.applicationContext as KPhoneApplication
        drawImage(app.sharedImage!!)
        viewModel.translatedText.observe(viewLifecycleOwner) {
            binding.translatedText.text = it.result
        }
    }


    /** Initialize CameraX, and prepare to bind the camera use cases  */
    /*
    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: ExecutionException) {
                throw IllegalStateException("Camera initialization failed.", e.cause!!)
            }
            // Build and bind the camera use cases
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = viewFinder.display.rotation

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        // Build the image analysis use case and instantiate our analyzer
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    scopedExecutor, TextAnalyzer(
                        requireContext(),
                        lifecycle,
                        cameraExecutor,
                        viewModel.sourceText,
                        viewModel.imageCropPercentages
                    )
                )
            }
        viewModel.sourceText.observe(viewLifecycleOwner, Observer {
            //binding.srcText.text = it
            val str = it
        })
        viewModel.imageCropPercentages.observe(viewLifecycleOwner,
            Observer { drawOverlay(binding.overlay.holder, it.first, it.second) })

        // Select back camera since text detection does not work with front camera
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
            preview.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (exc: IllegalStateException) {
            Log.e(TAG, "Use case binding failed. This must be running on main thread.", exc)
        }
    }
    */

    private fun drawOverlay(
        holder: SurfaceHolder,
        heightCropPercent: Int,
        widthCropPercent: Int
    ) {
        val canvas = holder.lockCanvas()
        val bgPaint = Paint().apply {
            alpha = 140
        }
        canvas.drawPaint(bgPaint)
        val rectPaint = Paint()
        rectPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = Color.WHITE
        val outlinePaint = Paint()
        outlinePaint.style = Paint.Style.STROKE
        outlinePaint.color = Color.WHITE
        outlinePaint.strokeWidth = 4f
        val surfaceWidth = holder.surfaceFrame.width()
        val surfaceHeight = holder.surfaceFrame.height()

        val cornerRadius = 25f
        // Set rect centered in frame
        val rectTop = surfaceHeight * heightCropPercent / 2 / 100f
        val rectLeft = surfaceWidth * widthCropPercent / 2 / 100f
        val rectRight = surfaceWidth * (1 - widthCropPercent / 2 / 100f)
        val rectBottom = surfaceHeight * (1 - heightCropPercent / 2 / 100f)
        val rect = RectF(rectLeft, rectTop, rectRight, rectBottom)
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, rectPaint
        )
        canvas.drawRoundRect(
            rect, cornerRadius, cornerRadius, outlinePaint
        )
        val textPaint = Paint()
        textPaint.color = Color.WHITE
        textPaint.textSize = 50F

        val overlayText = getString(R.string.overlay_help)
        val textBounds = Rect()
        textPaint.getTextBounds(overlayText, 0, overlayText.length, textBounds)
        val textX = (surfaceWidth - textBounds.width()) / 2f
        val textY = rectBottom + textBounds.height() + 15f // put text below rect and 15f padding
        canvas.drawText(getString(R.string.overlay_help), textX, textY, textPaint)
        holder.unlockCanvasAndPost(canvas)
    }

    /**
     *  [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by comparing absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = ln(max(width, height).toDouble() / min(width, height))
        if (abs(previewRatio - ln(RATIO_4_3_VALUE))
            <= abs(previewRatio - ln(RATIO_16_9_VALUE))
        ) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //viewFinder.post { setUpCamera() }
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getErrorMessage(exception: Exception): String? {
        val mlKitException = exception as? MlKitException ?: return exception.message
        return if (mlKitException.errorCode == MlKitException.UNAVAILABLE) {
            "Waiting for text recognition model to be downloaded"
        } else exception.message
    }

}
