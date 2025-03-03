package com.soundmind.kphone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import com.soundmind.kphone.R
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoActivity
import com.soundmind.kphone.main.LingGoFragment
import com.soundmind.kphone.main.ViewGoFragment
import com.soundmind.kphone.ui.theme.KPhoneTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.util.Locale

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
            .padding(top=100.dp)
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

@Composable
fun GridItem(item: Item, onItemClick: (Item) -> Unit) {
    val context = LocalContext.current
    val bgColor = Color(ContextCompat.getColor(context, R.color.topFxGoBox))
    val bgInner = Color(ContextCompat.getColor(context, R.color.topInnerBox))
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
                                .width(70.dp)
                                .height(16.dp)
                                //.clip(RoundedCornerShape(8.dp))
                            ,
                            contentScale = ContentScale.FillHeight
                        )
                        Box(modifier = Modifier) {
                            Image(
                                painter = painterResource(id = R.drawable.flag_vietnam),
                                contentDescription = null,
                                modifier = Modifier
                                    //.size(100.dp)
                                    //.align(Alignment.TopStart)
                                    .padding(15.dp, 10.dp)
                                    //.width (61.dp)
                                    .width(30.dp)
                                    .height(30.dp)
                                    //.size(30.dp)
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
                                    "111",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 84.dp, top = 10.dp),
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
                                        "111",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        //modifier = Modifier.padding(start = 84.dp, top = 10.dp),
                                        maxLines = 2
                                    )
                                    //Text("PPP", color = Color.White)
                                }
                            }
                            if (item.text.isNotEmpty()) {
                                Text(
                                    //item.text,
                                    "AAA",
                                    color = Color.Red,
                                    modifier = Modifier.padding(start = 74.dp, top = 50.dp)
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
                                .padding(10.dp, 10.dp)
                                .width(120.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Box(modifier = Modifier) {
                            Image(
                                painter = painterResource(id = item.subImage),
                                contentDescription = null,
                                modifier = Modifier
                                    //.padding(start = 5.dp, top = 5.dp)
                                    .width(40.dp)
                                    .height(20.dp)
                            )
                            Text(
                                "02-123-4567",
                                color = Color.Black,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(start = 50.dp, top = 0.dp),
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get system's language
        val bcp47Code = Locale.getDefault().toString()

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
    //modifier
    //    .background(Color.Black)
    //    .fillMaxSize()
    val context = LocalContext.current
    val items = listOf(
        Item(R.drawable.top_linggo, 'G', R.drawable.top_typing, "LingGo", object : ClickListener {
            override fun onClick() {
                //println("LingGo clicked")
                val intent = Intent(context, LingGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
                //context.startActivity(LingGoActivity.newIntent(context))
            }
        }),
        Item(R.drawable.top_viewgo, 'G', R.drawable.top_camera, "ViewGo", object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, ViewGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.top_fxgo, 'G', -1, "", object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, FxGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.top_support, 'S', R.drawable.top_support_phone, "Support Center", object : ClickListener {
            override fun onClick() {
            }
        }),
        Item(R.drawable.top_hanpass, 'H', -1, "https://www.hanpass.com/", object : ClickListener {
            override fun onClick() {
            }
        }),
        Item(R.drawable.top_kvisa, 'H', -1, "https://www.k-visa.co.kr/", object : ClickListener {
            override fun onClick() {
            }
        })
    )
    MyGrid(items = items) { item ->
        Toast.makeText(context, "Clicked: ${item.text}", Toast.LENGTH_SHORT).show()
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