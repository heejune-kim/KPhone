package com.soundmind.kphone.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.soundmind.kphone.databinding.FxgoActivityBinding
import com.soundmind.kphone.util.compareLast
import com.soundmind.kphone.util.lastAnSameSymbol
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.soundmind.kphone.Item
import com.soundmind.kphone.MainActivity
import com.soundmind.kphone.MainScreen
import com.soundmind.kphone.MyGrid
import com.soundmind.kphone.R
import com.soundmind.kphone.main.ExchangeRateApi
import com.soundmind.kphone.main.FxGoViewModel
import com.soundmind.kphone.ui.theme.KPhoneTheme
import com.soundmind.kphone.util.LanguageFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.Locale

interface ClickListener {
    fun onClick(item: FxItem)
}

data class FxItem(val imageResId: Int, val text: String, val type: Char, val listener: ClickListener)


@Composable
fun FxGrid(items: List<FxItem>, onItemClick: (FxItem) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier
            .padding(bottom = 10.dp),
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            .size(80.dp)
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

    val viewModel: FxGoViewModel by viewModels()

    lateinit var systemLanguage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //systemLanguage = intent.getStringExtra("lang").toString()
        systemLanguage = Locale.getDefault().toString().subSequence(0, 2).toString()
        //val act: MainActivity = activity as MainActivity
        //act.supportActionBar?.hide()

        viewModel.systemLanguage.value = systemLanguage
        viewModel.getExchangeRate(systemLanguage)

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
    val activity = context.getActivity() as? FxGoActivity
    val systemLanguage: String = activity!!.systemLanguage
    val viewModel = activity.viewModel

    var sourceLang by remember { mutableStateOf(systemLanguage) }
    var destinationLang by remember { mutableStateOf("ko") }

    var exchangeRate: Float? by remember { mutableStateOf(-1f) }
    var currencyCode by remember { mutableStateOf("USD") }
    var updated_at by remember { mutableStateOf("-") }

    var sourceCurrency = viewModel.getSourceCurrency()
    var destinationCurrency = viewModel.getDestinationCurrency()
    var sourceUnit = viewModel.getSourceUnit()
    var destinationUnit = viewModel.getDestinationUnit()

    var calculatedSource by remember { mutableStateOf(0f) }
    var calculatedDestination by remember { mutableStateOf(0f) }

    viewModel.currencyCode.observe(activity) {
        currencyCode = viewModel.currencyCode.value.toString()
    }
    viewModel.exchangeRate.observe(activity) {
        exchangeRate = viewModel.exchangeRate.value
    }
    viewModel.updated_at.observe(activity) {
        updated_at = viewModel.updated_at.value.toString()
    }

    viewModel.calculatedSource.observe(activity) {
        calculatedSource = viewModel.calculatedSource.value!!
        viewModel.doCalculate()
    }
    viewModel.calculatedDestination.observe(activity) {
        calculatedDestination = viewModel.calculatedDestination.value!!
    }

    val items = listOf(
        FxItem(R.drawable.fx_clear, "C", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_plus_minus, "+/-", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_percent, "%", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_divide, "/", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),

        // Second row
        FxItem(R.drawable.fx_7, "7", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_8, "8", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_9, "9", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_muliply, "*", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),

        // Third row
        FxItem(R.drawable.fx_4, "4", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_5, "5", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_6, "6", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_minus, "-", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),

        // Fourth row
        FxItem(R.drawable.fx_1, "1", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_2, "2", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_3, "3", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_plus, "+", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),

        // Fifth row
        FxItem(R.drawable.fx_dot, ".", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_0, "0", 'C', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_backspace, "<", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
        FxItem(R.drawable.fx_calculate, "=", 'I', object : ClickListener {
            override fun onClick(item: FxItem) {
                viewModel.processKey(item.text)
            }
        }),
    )
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Select nation's currency
        Row(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                .height(50.dp)
        ) {
            Image(
                painter = painterResource(id = LanguageFlag.getFlagForLanguage(sourceLang)),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 30.dp, top = 10.dp)
                    .width(30.dp)
                    .height(30.dp)
                    .clip(CircleShape),
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = LanguageFlag.getCurrencyCodeForLanguage(sourceLang),
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(start = 10.dp, top = 12.dp)
                    .width(80.dp)
                //.fillMaxWidth(),
            )
            Image(
                painter = painterResource(id = R.drawable.linggo_exchange),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 20.dp, top = 10.dp)
                    .width(24.dp)
                    .height(24.dp)
                    .background(Color.Black)
                    .clickable {
                        val temp = sourceLang
                        sourceLang = destinationLang
                        destinationLang = temp
                        viewModel.changeDirection()
                    },
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Image(
                painter = painterResource(id = LanguageFlag.getFlagForLanguage(destinationLang)),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 30.dp, top = 10.dp)
                    .width(30.dp)
                    .height(30.dp)
                    .clip(CircleShape),
                //.clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = LanguageFlag.getCurrencyCodeForLanguage(destinationLang),
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(start = 10.dp, top = 12.dp)
                    .width(80.dp)
                //.fillMaxWidth(),
            )
        }

        // Exchange rate
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "KRW ${exchangeRate.toString()} = 1 ${currencyCode}",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
                    .padding(start = 20.dp, top = 10.dp),
                textAlign = TextAlign.Start // Left align
                //.fillMaxWidth(),
            )
            Text(
                text = updated_at,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
                    .padding(end = 20.dp, top = 10.dp),
                textAlign = TextAlign.End // Left align
                //.fillMaxWidth(),
            )
        }

        ////////// Source currency
        Row(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = sourceCurrency,
                color = Color.White,
                fontSize = 35.sp,
                modifier = Modifier.weight(1f)
                    .padding(start = 20.dp, top = 90.dp),
                textAlign = TextAlign.Start
            )
            Text(
                text = calculatedSource.toString() + sourceUnit,
                color = Color.White,
                fontSize = 35.sp,
                modifier = Modifier.weight(1f)
                    .padding(end = 20.dp, top = 90.dp),
                textAlign = TextAlign.End
            )
        }

        // Target currency
        Row(
            modifier = Modifier
                .height(70.dp)
                .padding(top = 10.dp)
                .background(Color.Transparent)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = destinationCurrency,
                color = colorResource(R.color.fxGo_SourceValue),
                fontSize = 35.sp,
                modifier = Modifier.weight(1f)
                    .padding(start = 20.dp, top = 0.dp),
                textAlign = TextAlign.Start
            )
            Text(
                text = calculatedDestination.toString() + destinationUnit,
                color = colorResource(R.color.fxGo_SourceValue),
                fontSize = 35.sp,
                modifier = Modifier.weight(1f)
                    .padding(end = 20.dp, top = 0.dp),
                textAlign = TextAlign.End
            )

        }
        FxGrid(items = items) { item ->
            //Toast.makeText(context, "Clicked: ${item.text}", Toast.LENGTH_SHORT).show()
            //println("Clicked: ${item.text}")
            item.listener.onClick(item)
            // Handle item click here
        }
        //Text(
        //    text = "Hello $name!",
        //    modifier = modifier
        //)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FxScreen()
}
