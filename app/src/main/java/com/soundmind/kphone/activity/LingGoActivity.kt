package com.soundmind.kphone.activity

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

class LingGoActivity : AppCompatActivity() {
    //val viewModel: MainViewModel by viewModels()
    val viewModel: KPhoneModule by viewModels()

    lateinit var systemLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        systemLanguage = intent.getStringExtra("lang").toString()

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
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(62.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.linggo_topleft),
                contentDescription = null,
                modifier = Modifier
                    .padding(15.dp, 10.dp)
                    .width(98.dp)
                    .height(30.dp),
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
        Box(
            modifier = Modifier
                .background(Color.LightGray)
                .padding(start = 15.dp, end = 15.dp)
                .height(46.dp)
                .shadow(2.dp)
                .fillMaxWidth()
        ) {
            Image(
                //painter = painterResource(id = R.drawable.flag_vietnam),
                painter = painterResource(id = LanguageFlag.getFlagForLanguage(sourceLang)),
                contentDescription = null,
                modifier = Modifier
                    .padding(15.dp, 10.dp)
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
                    .padding(60.dp, 12.dp)
                    //.fillMaxWidth(),
            )

            /*
            val contentColor: Color = MaterialTheme.colors.primary
            Button(
                onClick = { Toast.makeText(context, "Exchange", Toast.LENGTH_SHORT).show() },
                content = {
                    Image(
                        painter = painterResource(id = R.drawable.linggo_exchange),
                        contentDescription = null)
                          },
                modifier = Modifier
                    //.fillMaxSize()
                    .background(Color.Red)
                    .padding(160.dp, 10.dp)
                    .width(24.dp)
                    .height(24.dp),
                //.clip(RoundedCornerShape(8.dp)),
                //contentScale = ContentScale.Crop,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = contentColor,
                    disabledBackgroundColor = Color.Transparent,
                    disabledContentColor = contentColor.copy(alpha = ContentAlpha.disabled),
                )
            )
            */

            Image(
                painter = painterResource(id = R.drawable.linggo_exchange),
                contentDescription = null,
                modifier = Modifier
                    .padding(160.dp, 10.dp)
                    .width(24.dp)
                    .height(24.dp)
                    .clickable {
                        //Toast.makeText(context, "Exchange", Toast.LENGTH_SHORT).show()
                        val temp = sourceLang
                        sourceLang = destinationLang
                        destinationLang = temp
                        activity.viewModel.setLanguages(source = sourceLang, target = destinationLang)
                    },
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )

            Image(
                //painter = painterResource(id = R.drawable.flag_korea),
                painter = painterResource(id = LanguageFlag.getFlagForLanguage(destinationLang)),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 200.dp, top = 10.dp)
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
                    .padding(start = 250.dp, top = 12.dp)
                    //.fillMaxWidth(),
            )

            Image(
                painter = painterResource(id = R.drawable.linggo_switch),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 300.dp, top = 10.dp)
                    .width(24.dp)
                    .height(24.dp)
                    .clip(CircleShape)
                    .clickable {
                        var temp: String = LanguageFlag.switchDestinationLanguage(destinationLang)
                        if (!temp.equals("")) {
                            if (temp.equals(sourceLang)) {
                                Toast.makeText(context, "The Source and target languages are the same.", Toast.LENGTH_SHORT).show()
                            } else {
                                destinationLang = temp
                            }
                        } else {
                            Toast.makeText(context, "Only Korean and English are supported", Toast.LENGTH_SHORT).show()
                        }
                        activity.viewModel.setLanguages(source = sourceLang, target = destinationLang)
                   },
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .background(Color.DarkGray)
                .padding(start = 15.dp, end = 15.dp)
                .height(460.dp)
                .fillMaxHeight()
        ) {
            SourceText()
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Composable
fun SourceText() {
    val context = LocalContext.current
    val activity = context.getActivity() as? LingGoActivity

    var value by remember { mutableStateOf("") }
    var translated by remember { mutableStateOf("") }
    //var anotherValue by remember { mutableStateOf("") }
    //val translated = activity?.viewModel?.translatedText.
    //val lifecycle = activity?.lifecycle as LifecycleOwner
    val lifecycle = LocalLifecycleOwner.current
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
    TextField(
        value = value,
        onValueChange = { value = it },
        //onValueChangeFinished = { value = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        placeholder = { Text(text = "Type here...") },
    )

    Text(
        //text = anotherValue,
        text = translated,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 200.dp)
            .height(200.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun LingGoPreview() {
    LingGoScreen()
}

