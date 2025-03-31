package com.soundmind.kphone

import android.app.AppOpsManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.google.mlkit.nl.translate.TranslateLanguage
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoActivity
import com.soundmind.kphone.ui.theme.KPhoneTheme
import java.util.Locale
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.soundmind.kphone.activity.ViewGoPreviewActivity
import com.soundmind.kphone.activity.getActivity
import com.soundmind.kphone.main.FxGoViewModel
import com.soundmind.kphone.main.KPhoneModule
import com.soundmind.kphone.util.ConnectivityObserver
import com.soundmind.kphone.util.LanguageFlag
import com.soundmind.kphone.util.NetworkConnectivityObserver
import kotlinx.coroutines.launch
import kotlin.getValue

interface ClickListener {
    fun onClick()
}

data class Item(
    val imageResId: Int,
    val type: Char, // G - Go series, S - Support Center, H - Hanpass and K-VISA
    val subImage: Int,
    val text: String,
    val listener: ClickListener
)

@Composable
fun MyGrid(items: List<Item>, onItemClick: (Item) -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(ContextCompat.getColor(context, R.color.topOuterBox))
    Card (
        modifier = Modifier
            .padding(top = 100.dp)
            .padding(start = 20.dp, end = 20.dp)
            .fillMaxWidth()
            .background(Color.DarkGray)
            //.offset(y = 100.dp)
            .clip(RoundedCornerShape(16.dp))
            .width(328.dp)
            .height(324.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .wrapContentHeight(align = Alignment.CenterVertically)
                //.padding(start = 20.dp, end = 20.dp)
                .background(bgColor)
                    ,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                GridItem(item = item, onItemClick = onItemClick)
            }
        }
    }
    /*
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            GridItem(item = item, onItemClick = onItemClick)
        }
    }
    */
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Composable
fun GridItem(item: Item, onItemClick: (Item) -> Unit) {
    val context = LocalContext.current
    val activity = context.getActivity() as MainActivity
    val systemLanguage = activity.systemLanguage
    val viewModel = activity.fxViewModel
    var exchangeRate by remember { mutableStateOf("No value") }

    viewModel.exchangeRate.observe(activity) {
        exchangeRate = "1 = ${viewModel.exchangeRate.value!!}"
    }
    val bgColor = Color(ContextCompat.getColor(context, R.color.topFxGoBox))
    val bgInner = Color(ContextCompat.getColor(context, R.color.topInnerBox))
    val flag = LanguageFlag.getFlagForLanguage(activity!!.systemLanguage)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.DarkGray)
            .width(140.dp)
            .height(84.dp)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier,//.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgInner)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                if (item.type == 'G') {
                    Column(
                        modifier = Modifier,
                        //verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = item.imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                //.size(100.dp)
                                //.align(Alignment.TopStart)
                                .padding(10.dp, 10.dp)
                                .width(80.dp)
                                .height(16.dp)
                                //.clip(RoundedCornerShape(8.dp))
                            ,
                            contentScale = ContentScale.FillHeight
                        )
                        Box(modifier = Modifier) {
                            Image(
                                //painter = painterResource(id = R.drawable.flag_vietnam),
                                painter = painterResource(id = flag),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(15.dp, 10.dp)
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clip(CircleShape),
                                //.clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (item.subImage != -1) {
                                Image(
                                    painter = painterResource(id = item.subImage),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(155.dp)
                                        .height(40.dp)
                                        .padding(start = 54.dp, top = 10.dp)
                                )
                                Text(
                                    text = item.text,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 90.dp, top = 17.dp),
                                    maxLines = 2
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .width(145.dp)
                                        .height(40.dp)
                                        .padding(start = 64.dp, top = 10.dp)
                                        .background(bgColor)
                                        .clip(RoundedCornerShape(20.dp)),
                                ) {
                                    Text(
                                        text = exchangeRate.toString(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .fillMaxSize(),
                                        //maxLines = 2,
                                        textAlign = TextAlign.Center
                                    )
                                    //Text("PPP", color = Color.White)
                                }
                            }
                            if (item.text.isNotEmpty()) {
                                Text(
                                    //item.text,
                                    "AAA",
                                    color = Color.Red,
                                    modifier = Modifier
                                        .padding(start = 74.dp, top = 50.dp)
                                        .width(145.dp)
                                        .height(45.dp)
                                )
                            }
                        }
                    }
                } else if (item.type == 'S') {
                    // Support Center

                    Column(
                        modifier = Modifier,
                        //verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = item.imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                //.size(100.dp)
                                //.align(Alignment.TopStart)
                                .padding(start=10.dp, top=10.dp)
                                .width(120.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Row(modifier = Modifier) {
                            Image(
                                painter = painterResource(id = item.subImage),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 10.dp, top = 5.dp)
                                    .size(35.dp)
                                    //.width(35.dp)
                                    //.height(20.dp)
                            )
                            Text(
                                text = item.text,
                                color = Color.Black,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(start = 10.dp, top = 10.dp),
                                maxLines = 2
                            )
                        }
                    }

                } else {
                    // Hanpass and K-VISA
                    Image(
                        painter = painterResource(id = item.imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            //.size(100.dp)
                            //.align(Alignment.TopStart)
                            .padding(10.dp, 15.dp)
                            .width(131.dp)
                            .height(53.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            /*
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = item.text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            */
        }
    }
}

//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {
    // Get system's language
    val systemLanguage: String = Locale.getDefault().toString().subSequence(0, 2).toString()

    val fxViewModel: FxGoViewModel by viewModels()
    val kphone: KPhoneModule by viewModels()

    var isNetworkAvailable = false
    private lateinit var connectivityObserver: ConnectivityObserver
    private fun collectConnectivityStatus() {
        lifecycleScope.launch {
            connectivityObserver.observe().collect {
                when (it) {
                    ConnectivityObserver.Status.Available -> {
                        isNetworkAvailable = true
                        fxViewModel.getExchangeRate(systemLanguage)
                        kphone.checkAndDownload(systemLanguage)
                        //kpnone.downloadAllRequiredLanguages(systemLanguage)
                    }

                    ConnectivityObserver.Status.Unavailable -> {
                        isNetworkAvailable = false
                        Toast.makeText(applicationContext, "Network is unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityObserver = NetworkConnectivityObserver(applicationContext)
        collectConnectivityStatus()
        if (isNetworkAvailable) {
            //fxViewModel.getExchangeRate(systemLanguage)
        }
        // MLKit supported languages
        val availableLanguages: List<String> = TranslateLanguage.getAllLanguages().map { it }
        val mlkit_langages = availableLanguages.toString()

        // Remove the title bar
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        /*
        setContentView(R.layout.main_translateshowcase_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                //.replace(R.id.container, ViewGoFragment.newInstance())
                .replace(R.id.container, LingGoFragment.newInstance())
                .commitNow()
        }
        */
        enableEdgeToEdge()
        setContent {
            KPhoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        //name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context.getActivity() as MainActivity
    val systemLanguage = activity.systemLanguage
    val viewModel = activity.fxViewModel
    var exchangeRate by remember { mutableStateOf(-1f) }
    val kphone = activity.kphone

    var linggoMessage by remember { mutableStateOf(context.getString(R.string.top_linggo_desc)) }
    var viewgoMessage by remember { mutableStateOf(context.getString(R.string.top_viewgo_desc)) }

    kphone.linggoTranslated.observe(activity) {
            linggoMessage = kphone.linggoTranslated.value!!
    }
    kphone.viewgoTranslated.observe(activity) {
        viewgoMessage = kphone.viewgoTranslated.value!!
    }
    viewModel.exchangeRate.observe(activity) {
        exchangeRate = viewModel.exchangeRate.value!!
    }
    kphone.translateTop(systemLanguage, linggoMessage, viewgoMessage)

    val phoneNumber = context.getString(R.string.support_center_number).toString()

    val items = listOf(
        Item(R.drawable.top_linggo, 'G', R.drawable.top_typing, linggoMessage, object : ClickListener {
            override fun onClick() {
                ///*
                val intent = Intent(context, LingGoActivity::class.java)
                intent.putExtra("lang", systemLanguage)
                context.startActivity(intent)
                //*/
                //kphone.testPrintAllModel()
            }
        }),
        Item(R.drawable.top_viewgo, 'G', R.drawable.top_camera, viewgoMessage, object : ClickListener {
            override fun onClick() {
                ///*
                //val intent = Intent(context, ViewGoActivity::class.java)
                val intent = Intent(context, ViewGoPreviewActivity::class.java)
                intent.putExtra("lang", systemLanguage)
                context.startActivity(intent)
                //*/
                //kphone.testTranslate(language = systemLanguage, "Don't worry be happy~ happy master")
                //kphone.testDownload("vi")
            }
        }),
        Item(R.drawable.top_fxgo, 'G', -1, "", object : ClickListener {
            override fun onClick() {
                ///*
                val intent = Intent(context, FxGoActivity::class.java)
                intent.putExtra("lang", systemLanguage)
                context.startActivity(intent)
                //*/
                //kphone.testDeleteAllModel()
            }
        }),
        Item(R.drawable.top_support, 'S', R.drawable.top_support_phone, phoneNumber, object : ClickListener {
            override fun onClick() {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:$phoneNumber".toUri()
                }
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.top_hanpass, 'H', -1, "https://www.hanpass.com/", object : ClickListener {
            override fun onClick() {
                val url = "https://www.hanpass.com"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(url.toUri())
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.top_kvisa, 'H', -1, "https://www.k-visa.co.kr/", object : ClickListener {
            override fun onClick() {
                val url = "https://www.k-visa.co.kr"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setData(url.toUri())
                context.startActivity(intent)
            }
        })
    )
    MyGrid(items = items) { item ->
        //Toast.makeText(context, "Clicked: ${item.text}", Toast.LENGTH_SHORT).show()
        println("Clicked: ${item.text}")
        item.listener.onClick()
        // Handle item click here
    }
    //Text(
    //    text = "Hello $name!",
    //    modifier = modifier
    //)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainScreen()
}