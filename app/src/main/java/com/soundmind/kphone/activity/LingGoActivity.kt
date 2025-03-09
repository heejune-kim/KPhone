package com.soundmind.kphone.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.soundmind.kphone.MainActivity
import com.soundmind.kphone.MainScreen
import com.soundmind.kphone.R
import com.soundmind.kphone.main.KPhoneModule
import com.soundmind.kphone.main.LingGoFragment
import com.soundmind.kphone.main.MainViewModel
import com.soundmind.kphone.main.ViewGoFragment
import com.soundmind.kphone.ui.theme.KPhoneTheme
import com.soundmind.kphone.util.Language
import com.soundmind.kphone.util.LanguageFlag
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import java.util.Locale

@Composable
fun Modifier.verticalColumnScrollbar(
    scrollState: ScrollState,
    width: Dp = 4.dp,
    showScrollBarTrack: Boolean = true,
    scrollBarTrackColor: Color = Color.Gray,
    scrollBarColor: Color = Color.Black,
    scrollBarCornerRadius: Float = 4f,
    endPadding: Float = 12f
): Modifier {
    return drawWithContent {
        // Draw the column's content
        drawContent()
        // Dimensions and calculations
        val viewportHeight = this.size.height
        val totalContentHeight = scrollState.maxValue.toFloat() + viewportHeight
        val scrollValue = scrollState.value.toFloat()
        // Compute scrollbar height and position
        val scrollBarHeight =
            (viewportHeight / totalContentHeight) * viewportHeight
        val scrollBarStartOffset =
            (scrollValue / totalContentHeight) * viewportHeight
        // Draw the track (optional)
        if (showScrollBarTrack) {
            drawRoundRect(
                cornerRadius = CornerRadius(scrollBarCornerRadius),
                color = scrollBarTrackColor,
                topLeft = Offset(this.size.width - endPadding, 0f),
                size = Size(width.toPx(), viewportHeight),
            )
        }
        // Draw the scrollbar
        drawRoundRect(
            cornerRadius = CornerRadius(scrollBarCornerRadius),
            color = scrollBarColor,
            topLeft = Offset(this.size.width - endPadding, scrollBarStartOffset),
            size = Size(width.toPx(), scrollBarHeight)
        )
    }
}

@SuppressLint("ModifierFactoryUnreferencedReceiver", "UnnecessaryComposedModifier")
fun Modifier.semiBorder(strokeWidth: Dp, color: Color, cornerRadiusDp: Dp) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadius = density.run { cornerRadiusDp.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = 0f, y = cornerRadius),
                strokeWidth = strokeWidthPx
            )

            // Top left arc
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidthPx)
            )


            drawLine(
                color = color,
                start = Offset(x = cornerRadius, y = 0f),
                end = Offset(x = width - cornerRadius, y = 0f),
                strokeWidth = strokeWidthPx
            )

            // Top right arc
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(x = width - cornerRadius * 2, y = 0f),
                size = Size(cornerRadius * 2, cornerRadius * 2),
                style = Stroke(width = strokeWidthPx)
            )

            drawLine(
                color = color,
                start = Offset(x = width, y = height),
                end = Offset(x = width, y = cornerRadius),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

class LingGoActivity : AppCompatActivity() {
    //val viewModel: MainViewModel by viewModels()
    val viewModel: KPhoneModule by viewModels()

    lateinit var systemLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //systemLanguage = intent.getStringExtra("lang").toString()
        systemLanguage = Locale.getDefault().toString().subSequence(0, 2).toString()

        viewModel.checkAndDownload(systemLanguage)
        /*
        setContentView(R.layout.main_translateshowcase_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                //.replace(R.id.container, ViewGoFragment.newInstance())
                .replace(R.id.container, LingGoFragment.newInstance())
                .commitNow()
        }
        */
        //viewModel.sourceLang.value = Language("en")
        //viewModel.sourceLang.value = Language("ko")
        viewModel.setLanguages(source = systemLanguage, target = "ko")
        /*
        viewModel.translatedText.observe(
            this,
            { resultOrError ->
                if (resultOrError.error != null) {
                    Toast.makeText(this, resultOrError.error!!.localizedMessage, Toast.LENGTH_LONG).show()
                    //srcTextView.setError(resultOrError.error!!.localizedMessage)
                } else {
                    //targetTextView.text = resultOrError.result
                    Toast.makeText(this, resultOrError.result, Toast.LENGTH_LONG).show()
                }
            }
        )
        */

        enableEdgeToEdge()
        setContent {
            KPhoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LingGoScreen(
                        //name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("ResourceAsColor")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LingGoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.getActivity() as? LingGoActivity
    val systemLanguage: String = activity!!.systemLanguage
    //val (flag, language) = LanguageFlag.getInfoForLanguage(systemLanguage)

    //var sourceFlag by remember { mutableStateOf(R.drawable.flag_vietnam) }
    //var destinationFlag by remember { mutableStateOf(R.drawable.flag_korea) }
    var sourceLang by remember { mutableStateOf(systemLanguage) }
    var destinationLang by remember { mutableStateOf("ko") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(top = 0.dp)
        ) {
            // Title Bar
            Image(
                painter = painterResource(id = R.drawable.linggo_topleft),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 15.dp, top = 10.dp)
                    .width(98.dp)
                    .height(30.dp),
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
        Card(
            shape = RoundedCornerShape(
                topEnd = 16.dp,
                topStart = 16.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            ),
            modifier = Modifier
                //.background(Color.Black)
                .padding(start = 15.dp, end = 15.dp, top = 15.dp, bottom = 15.dp)
                //.shadow(
                //    elevation = 8.dp,
                //    ambientColor = Color.White,
                //    )
                .semiBorder(5.dp, Color.Black, 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(colorResource(R.color.linggoLanguages))
                    //.padding(start = 15.dp, end = 15.dp)
                    .height(46.dp)
                    //.shadow(2.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    //painter = painterResource(id = R.drawable.flag_vietnam),
                    painter = painterResource(id = LanguageFlag.getFlagForLanguage(sourceLang)),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 10.dp)
                        .width(30.dp)
                        .height(30.dp)
                        .clip(CircleShape),
                    //.clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = LanguageFlag.getFullNameForLanguage(sourceLang),
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(start = 5.dp, top = 15.dp)
                        .width(100.dp)
                    //.border(2.dp, Color.White)
                    //.fillMaxWidth(),
                )

                Image(
                    painter = painterResource(id = R.drawable.linggo_exchange),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 12.dp)
                        .size(24.dp)
                        //.background(Color.LightGray)
                        .clickable {
                            val temp = sourceLang
                            sourceLang = destinationLang
                            destinationLang = temp
                            activity.viewModel.setLanguages(
                                source = sourceLang,
                                target = destinationLang
                            )
                        },
                    contentScale = ContentScale.Crop,
                )

                Image(
                    //painter = painterResource(id = R.drawable.flag_korea),
                    painter = painterResource(id = LanguageFlag.getFlagForLanguage(destinationLang)),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp)
                        .width(30.dp)
                        .height(30.dp)
                        .clip(CircleShape),
                    //.clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = LanguageFlag.getFullNameForLanguage(destinationLang),
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(start = 5.dp, top = 12.dp)
                        //.border(2.dp, Color.White)
                        .width(100.dp)
                    //.fillMaxWidth(),
                )

                Image(
                    painter = painterResource(id = R.drawable.linggo_switch),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 0.dp, top = 10.dp)
                        .width(24.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            var temp: String =
                                LanguageFlag.switchDestinationLanguage(destinationLang)
                            if (!temp.equals("")) {
                                if (temp.equals(sourceLang)) {
                                    Toast.makeText(
                                        context,
                                        "The Source and target languages are the same.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    destinationLang = temp
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Only Korean and English are supported",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            activity.viewModel.setLanguages(
                                source = sourceLang,
                                target = destinationLang
                            )
                        },
                    //.clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                modifier = Modifier
                    .background(colorResource(R.color.linggoInput))
                    //.padding(start = 15.dp, end = 15.dp)
                    //.height(460.dp)
                    .fillMaxHeight()
            ) {
                SourceText()
            }
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@OptIn(FlowPreview::class)
@Composable
fun SourceText() {
    val context = LocalContext.current
    val activity = context.getActivity() as? LingGoActivity
    val viewModel = activity!!.viewModel

    var value by remember { mutableStateOf("") }
    var translated by remember { mutableStateOf("") }
    //var anotherValue by remember { mutableStateOf("") }
    //val translated = activity?.viewModel?.translatedText.
    //val lifecycle = activity?.lifecycle as LifecycleOwner
    val lifecycle = LocalLifecycleOwner.current
    viewModel.warnings.observe(activity) {
        translated = viewModel.warning_
    }
    activity?.viewModel?.translatedText?.observe(
        lifecycle,
        { resultOrError ->
            if (resultOrError.error != null) {
                Toast.makeText(activity, resultOrError.error!!.localizedMessage, Toast.LENGTH_LONG).show()
                //srcTextView.setError(resultOrError.error!!.localizedMessage)
            } else {
                //targetTextView.text = resultOrError.result
                //Toast.makeText(activity, resultOrError.result, Toast.LENGTH_LONG).show()
                translated = resultOrError.result!!
            }
        }
    )
    LaunchedEffect(value) {
        snapshotFlow { value }
            .debounce(1000)
            .collectLatest {
                //anotherValue = it
                //activity?.viewModel?.sourceText?.postValue(it)
                activity?.viewModel?.translateText(it)
            }
    }
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        //val scrollState = rememberScrollState()
        TextField(
            value = value,
            onValueChange = { value = it },
            //onValueChangeFinished = { value = it },
            modifier = Modifier
                .fillMaxWidth()
                //.verticalColumnScrollbar(scrollState) // Apply the scrollbar first
                //.verticalScroll(scrollState) // Then apply the scrolling behavior
                .height(275.dp),
            placeholder = {
                Text(text = "Type here...")
            },
            colors = OutlinedTextFieldDefaults.colors( unfocusedContainerColor = colorResource(R.color.linggoInput) ),
            textStyle = TextStyle(fontSize = 22.sp),
        )

        Box(modifier = Modifier) {
            TextField(
                value = translated,
                onValueChange = { },
                modifier = Modifier
                    //.padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors( unfocusedContainerColor = colorResource(R.color.linggoInput) ),
                textStyle = TextStyle(fontSize = 22.sp),
                readOnly = true
            )

            //if (viewModel.warnings.value!!.length > 0) {
            if (viewModel.warning_.isEmpty()) {
                return
            }
            //translated = viewModel.warnings.value!!
            CircularProgressIndicator(
                modifier = Modifier
                    //.padding(start = 130.dp, top = 250.dp)
                    .padding(start = 130.dp, top = 150.dp)
                    .width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LingGoPreview() {
    LingGoScreen()
}

