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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.soundmind.kphone.R
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoActivity
import com.soundmind.kphone.main.LingGoFragment
import com.soundmind.kphone.main.ViewGoFragment
import com.soundmind.kphone.ui.theme.KPhoneTheme

interface ClickListener {
    fun onClick()
}

data class Item(val imageResId: Int, val text: String, val listener: ClickListener)

@Composable
fun MyGrid(items: List<Item>, onItemClick: (Item) -> Unit) {
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
}

@Composable
fun GridItem(item: Item, onItemClick: (Item) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
        }
    }
}

//class MainActivity : ComponentActivity() {
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val items = listOf(
        Item(R.drawable.logo_mlkit, "LingGo", object : ClickListener {
            override fun onClick() {
                //println("LingGo clicked")
                val intent = Intent(context, LingGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
                //context.startActivity(LingGoActivity.newIntent(context))
            }
        }),
        Item(R.drawable.logo_mlkit, "ViewGo", object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, ViewGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.logo_mlkit, "FxGo", object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, FxGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        Item(R.drawable.logo_mlkit, "Item 4", object : ClickListener {
            override fun onClick() {

            }
        }),
        Item(R.drawable.logo_mlkit, "Item 5", object : ClickListener {
            override fun onClick() {

            }
        }),
        Item(R.drawable.logo_mlkit, "Item 6", object : ClickListener {
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