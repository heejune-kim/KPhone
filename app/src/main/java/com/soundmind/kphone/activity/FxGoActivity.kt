package com.soundmind.kphone.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.core.view.WindowCompat
import com.soundmind.kphone.databinding.FxgoActivityBinding
import com.soundmind.kphone.util.compareLast
import com.soundmind.kphone.util.lastAnSameSymbol
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.soundmind.kphone.ClickListener
import com.soundmind.kphone.Item
import com.soundmind.kphone.MainActivity
import com.soundmind.kphone.MainScreen
import com.soundmind.kphone.MyGrid
import com.soundmind.kphone.R
import com.soundmind.kphone.ui.theme.KPhoneTheme
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder

/*
interface ClickListener {
    fun onClick()
}
*/

data class FxItem(val imageResId: Int, val text: String, val type: Char, val listener: ClickListener)


@Composable
fun FxGrid(items: List<FxItem>, onItemClick: (FxItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { item ->
            FxGridItem(item = item, onItemClick = onItemClick)
        }
    }
}

@Composable
fun FxGridItem(item: FxItem, onItemClick: (FxItem) -> Unit) {
    Image(
        painter = painterResource(id = item.imageResId),
        contentDescription = null,
        modifier = Modifier
            .background(Color.Transparent)
            .size(72.dp)
            .clickable { onItemClick(item) },
        //.clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Fit
    )
    /*
    Card(
        modifier = Modifier
            //.fillMaxWidth()
            //.clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .width(72.dp)
            .height(72.dp)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            //modifier = Modifier.padding(16.dp),
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (item.type == 'I') {
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        //.clip(RoundedCornerShape(8.dp)),
                            ,
                    contentScale = ContentScale.Crop
                )
            }
            else {
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
    */
}

class FxGoActivity : AppCompatActivity() {

    private lateinit var binding: FxgoActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val act: MainActivity = activity as MainActivity
        //act.supportActionBar?.hide()

        // Remove the title bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        //binding = FxgoActivityBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        //bindClicks()
        //enableEdgeToEdge()
        setContent {
            KPhoneTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FxScreen(
                        //name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun bindClicks() {
        binding.apply {
            btClear.setOnClickListener {
                clear()
            }

            btOne.setOnClickListener {
                type(it)
            }

            btTwo.setOnClickListener {
                type(it)
            }

            btThree.setOnClickListener {
                type(it)
            }

            btFour.setOnClickListener {
                type(it)
            }

            btFive.setOnClickListener {
                type(it)
            }

            btSix.setOnClickListener {
                type(it)
            }

            btSeven.setOnClickListener {
                type(it)
            }

            btEight.setOnClickListener {
                type(it)
            }

            btNine.setOnClickListener {
                type(it)
            }

            btZero.setOnClickListener {
                type(it)
            }

            btMinus.setOnClickListener {
                arithmeticClick(it)
            }

            btPlus.setOnClickListener {
                arithmeticClick(it)
            }

            btDivide.setOnClickListener {
                arithmeticClick(it)
            }

            btMultiply.setOnClickListener {
                arithmeticClick(it)
            }

            btPercent.setOnClickListener {
                notImplemented(it)
            }

            btParentheses.setOnClickListener {
                notImplemented(it)
            }

            btBack.setOnClickListener {
                backspace()
            }

            btEqual.setOnClickListener {
                calculate()
            }

            btDot.setOnClickListener {
                dotClick(it)
            }
        }
    }

    private fun dotClick(view: View) {
        val button = view as MaterialButton
        binding.apply {
            if (etDisplay.text.isNotEmpty() && !etDisplay.text.toString().lastAnSameSymbol()) {
                if (!button.text.toString().compareLast(etDisplay.text.toString())) {
                    type(button)
                }
            }
        }
    }

    private fun calculate() {
        binding.apply {
            val txt: String = etDisplay.text.toString()
            val expression: Expression = ExpressionBuilder(txt).build()
            try {
                val result: Double = expression.evaluate()
                etDisplay.text = result.toString()
            } catch (arithmeticException: ArithmeticException) {
                etDisplay.text = arithmeticException.message
            } catch (illegalArgumentException: IllegalArgumentException) {
                etDisplay.text = illegalArgumentException.message
            }
        }
    }

    private fun clear() {
        binding.etDisplay.text = ""
    }

    private fun notImplemented(it: View) =
        Snackbar.make(it, "Not implemented yet!", Snackbar.LENGTH_SHORT).show()

    private fun backspace() {
        binding.etDisplay.text = binding.etDisplay.text.dropLast(1)
    }

    private fun arithmeticClick(it: View?) {
        val button = it as MaterialButton
        binding.apply {
            if (etDisplay.text.isNotEmpty() && etDisplay.text.toString().lastAnSameSymbol()) {
                if (!button.text.toString().compareLast(binding.etDisplay.text.toString())) {
                    etDisplay.text = etDisplay.text.toString().dropLast(1)
                    type(button)
                }
            } else {
                type(button)
            }
        }
    }

    private fun type(view: View) {
        val button = view as MaterialButton
        binding.etDisplay.text = "${binding.etDisplay.text}${button.text}"
    }
}

@Composable
fun FxScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val items = listOf(
        FxItem(R.drawable.fx_clear, "C", 'I', object : ClickListener {
            override fun onClick() {
                //println("LingGo clicked")
                val intent = Intent(context, LingGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
                //context.startActivity(LingGoActivity.newIntent(context))
            }
        }),
        FxItem(R.drawable.fx_plus_minus, "+/-", 'I', object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, ViewGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        FxItem(R.drawable.fx_percent, "%", 'C', object : ClickListener {
            override fun onClick() {
                //println("ViewGo clicked")
                val intent = Intent(context, FxGoActivity::class.java)
                //intent.putExtra("key", "value")
                context.startActivity(intent)
            }
        }),
        FxItem(R.drawable.fx_divide, "÷", 'C', object : ClickListener {
            override fun onClick() {

            }
        }),

        // Second row
        FxItem(R.drawable.fx_7, "7", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_8, "8", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_9, "9", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_muliply, "×", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),

        // Third row
        FxItem(R.drawable.fx_4, "7", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_5, "8", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_6, "9", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_minus, "×", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),

        // Fourth row
        FxItem(R.drawable.fx_1, "7", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_2, "8", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_3, "9", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_plus, "×", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),

        // Fifth row
        FxItem(R.drawable.fx_dot, ".", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_0, "0", 'C', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_backspace, "9", 'I', object : ClickListener {
            override fun onClick() {
            }
        }),
        FxItem(R.drawable.fx_calculate, "=", 'I', object : ClickListener {
            override fun onClick() {
            }
        }),
    )
    FxGrid(items = items) { item ->
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
    FxScreen()
}
