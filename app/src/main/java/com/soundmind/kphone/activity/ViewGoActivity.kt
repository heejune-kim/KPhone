package com.soundmind.kphone.activity

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.fragment.app.Fragment
import com.soundmind.kphone.R
import com.soundmind.kphone.main.LingGoFragment
import com.soundmind.kphone.main.ViewGoFragment
import com.soundmind.kphone.main.ViewGoShotFragment
import com.soundmind.kphone.ui.theme.KPhoneTheme
import java.util.Locale

class ViewGoActivity : AppCompatActivity() {
    lateinit var systemLanguage: String
    //@RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_translateshowcase_activity)
        //systemLanguage = intent.getStringExtra("lang").toString()
        systemLanguage = Locale.getDefault().toString().subSequence(0, 2).toString()
        val bundle = Bundle()
        bundle.putString("lang", systemLanguage)
        val type = intent.getStringExtra("type").toString()
        var fragment: Fragment
        if (type == "shot") {
            fragment = ViewGoShotFragment.newInstance()
            //val image = intent.getStringExtra("image")
            //bundle.putString("image", image)
            bundle.putInt("width", intent.getIntExtra("width", 0))
            bundle.putInt("height", intent.getIntExtra("height", 0))
        } else {
            fragment = ViewGoFragment.newInstance()
        }
        fragment.arguments = bundle
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                //.replace(R.id.container, ViewGoFragment.newInstance())
                .replace(R.id.container, fragment)
                .commitNow()
        }
    }
}
