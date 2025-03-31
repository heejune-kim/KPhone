package com.soundmind.kphone.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.LazyVerticalGridScope
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.Dimension
import com.soundmind.kphone.R
import com.soundmind.kphone.ClickListener
import com.soundmind.kphone.activity.FxGoActivity
import com.soundmind.kphone.activity.LingGoActivity
import com.soundmind.kphone.activity.ViewGoPreviewActivity
import com.soundmind.kphone.util.LanguageFlag
import java.util.Locale
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class MyAppWidgetReceiver : GlanceAppWidgetReceiver() {

    // Let MyAppWidgetReceiver know which GlanceAppWidget to use
    override val glanceAppWidget: GlanceAppWidget = KPhoneGlaceWidget()
}

class KPhoneGlaceWidget : GlanceAppWidget() {

    val systemLanguage = Locale.getDefault().toString().subSequence(0, 2).toString()
    val flag = LanguageFlag.getFlagForLanguage(systemLanguage)
    var _context: Context? = null

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.
        _context = context

        provideContent {
            // create your AppWidget here
            MyContent()
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun LingViewGo(topLeft: Int, subImage: Int, text: String, action: Action) {
        //val bgInner = Color(ContextCompat.getColor(get, R.color.topInnerBox))
        Box(
            modifier = GlanceModifier
                .width(180.dp)
                .height(100.dp)
                .clickable(action)
                .background(Color.LightGray), // Example background
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = GlanceModifier,
            )
            {
                Image(
                    provider = ImageProvider(topLeft),
                    contentDescription = "Flag",
                    modifier = GlanceModifier
                        .padding(top=10.dp, start=10.dp)
                        //.padding(10.dp, 10.dp)
                        .width(80.dp)
                        .height(26.dp)
                )
                Box(
                    modifier = GlanceModifier
                    ,contentAlignment = Alignment.BottomStart,
                ) {
                    Image(
                        provider = ImageProvider(flag),
                        contentDescription = "Flag",
                        modifier = GlanceModifier
                            .padding(start=15.dp, top=15.dp)
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Image(
                        provider = ImageProvider(subImage),
                        contentDescription = "Flag",
                        modifier = GlanceModifier
                            .padding(start=44.dp, top=15.dp, end=5.dp)
                            .width(205.dp)
                            .height(60.dp)
                    )
                    Text(
                        text = text,
                        style = TextStyle(color = ColorProvider(Color.White),
                            fontSize = 10.sp),
                        modifier = GlanceModifier
                            .padding(start=95.dp, top=15.dp, bottom=10.dp)
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun Support(topLeft: Int, subImage: Int, text: String, action: Action) {
        //val bgInner = Color(ContextCompat.getColor(get, R.color.topInnerBox))
        Box(
            modifier = GlanceModifier
                .width(180.dp)
                .height(100.dp)
                .clickable(action)
                .background(Color.LightGray), // Example background
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = GlanceModifier,
            )
            {
                Image(
                    provider = ImageProvider(topLeft),
                    contentDescription = "Flag",
                    modifier = GlanceModifier
                        .padding(top=10.dp, start=10.dp)
                        //.padding(10.dp, 10.dp)
                        .width(120.dp)
                        .height(30.dp)
                )
                //Box(
                Row(
                    modifier = GlanceModifier
                    //,contentAlignment = Alignment.BottomStart,
                ) {
                    Image(
                        provider = ImageProvider(subImage),
                        contentDescription = "Flag",
                        modifier = GlanceModifier
                            .padding(start = 10.dp, top = 15.dp)
                            .width(40.dp)
                            .height(40.dp)
                            //.padding(start=44.dp, top=15.dp, end=5.dp)
                            //.width(205.dp)
                            //.height(60.dp)
                    )
                    Text(
                        text = text,
                        style = TextStyle(color = ColorProvider(Color.Black),
                            fontSize = 17.sp),
                        modifier = GlanceModifier
                            .padding(start = 10.dp, top = 20.dp),
                            //.padding(start=90.dp, top=15.dp, bottom=10.dp)
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun ImageLink(topLeft: Int, action: Action) {
        Box(
            modifier = GlanceModifier
                .width(180.dp)
                .height(100.dp)
                .clickable(action)
                .background(Color.LightGray), // Example background
            contentAlignment = Alignment.TopStart
        ) {
            /*
            Column(
                modifier = GlanceModifier,
            )
            {
                Image(
                    provider = ImageProvider(topLeft),
                    contentDescription = "Flag",
                    modifier = GlanceModifier
                        .padding(top=10.dp, start=10.dp)
                        //.padding(10.dp, 10.dp)
                        .width(120.dp)
                        .height(30.dp)
                )
                //Box(
                Row(
                    modifier = GlanceModifier
                    //,contentAlignment = Alignment.BottomStart,
                ) {
                    Image(
                        provider = ImageProvider(subImage),
                        contentDescription = "Flag",
                        modifier = GlanceModifier
                            .padding(start = 10.dp, top = 5.dp)
                            .width(35.dp)
                            .height(35.dp)
                        //.padding(start=44.dp, top=15.dp, end=5.dp)
                        //.width(205.dp)
                        //.height(60.dp)
                    )
                    Text(
                        text = text,
                        style = TextStyle(color = ColorProvider(Color.Black),
                            fontSize = 17.sp),
                        modifier = GlanceModifier
                            .padding(start = 10.dp, top = 10.dp),
                        //.padding(start=90.dp, top=15.dp, bottom=10.dp)
                    )
                }
            }
            */
            Image(
                provider = ImageProvider(topLeft),
                contentDescription = "Flag",
                modifier = GlanceModifier
                    .padding(start = 10.dp, top = 15.dp)
                    .fillMaxSize()
                    //.width(131.dp)
                    //.height(53.dp)
            )
        }
    }

    @Composable
    private fun Spacer() {
        Box(modifier = GlanceModifier.height(8.dp)) {
            // Empty box to add some space between elements
        }
    }
    @SuppressLint("RestrictedApi")
    @Composable
    private fun MyContent() {
        //val context = LocalContext.current
        val context = _context
            LazyVerticalGrid(
                gridCells = GridCells.Fixed(2),
                modifier = GlanceModifier
                    .fillMaxSize(),
                //contentPadding = GlanceModifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Column(modifier = GlanceModifier) {
                        LingViewGo(topLeft = R.drawable.top_linggo, subImage = R.drawable.top_typing, text = "Type to translate", action = actionStartActivity<LingGoActivity>())
                        Spacer()
                    }
                }
                item {
                    Column(modifier = GlanceModifier) {
                        LingViewGo(
                            topLeft = R.drawable.top_viewgo,
                            subImage = R.drawable.top_camera,
                            text = "Take a picture",
                            action = actionStartActivity<ViewGoPreviewActivity>()
                        )
                        Spacer()
                    }
                }
                item {
                    Column(modifier = GlanceModifier) {
                        LingViewGo(
                            topLeft = R.drawable.top_fxgo,
                            subImage = R.drawable.top_camera,
                            text = "Exchange",
                            action = actionStartActivity<FxGoActivity>()
                        )
                        Spacer()
                    }
                }
                item {
                    Column(modifier = GlanceModifier) {
                        Support(
                            topLeft = R.drawable.top_support,
                            subImage = R.drawable.top_support_phone,
                            text = _context?.getString(R.string.support_center_number).toString(),
                            action = actionRunCallback<DialogActionCallback>()
                        )
                        Spacer()
                    }
                }
                item {
                    Column(modifier = GlanceModifier) {
                        ImageLink(
                            topLeft = R.drawable.top_hanpass,
                            action = actionRunCallback<HanpassActionCallback>()
                        )
                        Spacer()
                    }
                }
                item {
                    Column(modifier = GlanceModifier) {
                        ImageLink(
                            topLeft = R.drawable.top_kvisa,
                            action = actionRunCallback<KVisaActionCallback>()
                        )
                        Spacer()
                    }
                }
            }
        /*
        Row (
            modifier = GlanceModifier
                .fillMaxWidth()
                .fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        }
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
            Row(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    text = "Home",
                    onClick = actionStartActivity<LingGoActivity>()
                )
                Button(
                    text = "Work",
                    onClick = actionStartActivity<ViewGoPreviewActivity>()
                )
            }
        }

         */
    }
}


class DialogActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: androidx.glance.GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("MyDialogActionCallback", "onAction")
        val phonenumber = context.getString(R.string.support_center_number).toString()
        val intent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = "tel:$phonenumber".toUri()
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MyDialogActionCallback", "Error starting dialog", e)
        }
    }
}

class HanpassActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: androidx.glance.GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("MyDialogActionCallback", "onAction")
        val url = "https://www.hanpass.com"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = url.toUri()
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MyDialogActionCallback", "Error starting dialog", e)
        }
    }
}

class KVisaActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: androidx.glance.GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("MyDialogActionCallback", "onAction")
        val url = "https://www.hanpass.com"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = url.toUri()
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MyDialogActionCallback", "Error starting dialog", e)
        }
    }
}
