package com.soundmind.kphone.main

import android.app.Application
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class KPhoneModule(application: Application): AndroidViewModel(application)  {
    companion object {
        private const val NUM_TRANSLATORS = 5
    }

    private val modelManager: RemoteModelManager = RemoteModelManager.getInstance()
    val translatedText = MediatorLiveData<ResultOrError>()

    private val translators =
        object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
            override fun create(options: TranslatorOptions): Translator {
                return Translation.getClient(options)
            }
            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions,
                oldValue: Translator,
                newValue: Translator?,
            ) {
                oldValue.close()
            }
        }

    var sourceLang: String = "ko"
    var targetLang: String = "en"
    //var sourceText: String = ""
    var sourceText: String = ""

    private val pendingDownloads: HashMap<String, Task<Void>> = hashMapOf()

    private fun getModel(languageCode: String): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(languageCode).build()
    }

    internal fun downloadLanguage(language: String) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language)!!)
        var downloadTask: Task<Void>?
        if (pendingDownloads.containsKey(language)) {
            downloadTask = pendingDownloads[language]
            if (downloadTask != null && !downloadTask.isCanceled) {
                // currently download or pended
                return
            }
        }
        downloadTask = modelManager.download(model, DownloadConditions.Builder().build()).addOnCompleteListener {
            pendingDownloads.remove(language)
            // fetch
        }
        pendingDownloads[language] = downloadTask
    }

    fun translate(): Task<String> {
        if (sourceText == "") {
            //log("Empty...")
            return Tasks.forResult("EMPTY")
        }
        val sourceLangCode = TranslateLanguage.fromLanguageTag(sourceLang)!!
        val targetLangCode = TranslateLanguage.fromLanguageTag(targetLang)!!
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .build()
        return translators[options].downloadModelIfNeeded().continueWithTask {
                task ->
            if (task.isSuccessful) {
                translators[options].translate(sourceText)
            } else {
                Tasks.forException<String> (
                    task.exception
                        ?: Exception("Unknown error occurred")
                )
            }
        }
    }

    fun setLanguages(source: String, target: String) {
        sourceLang = source
        targetLang = target
        downloadLanguage(source)
        downloadLanguage(target)
    }

    fun translateText(source: String) {
        //log("${source} is set.")
        if (source.length < 2) {
            return
        }
        sourceText = source
        val processTranslation = OnCompleteListener<String> { task ->
            run {
                //val map = Arguments.createMap()
                if (task.isSuccessful) {
                    translatedText.value = ResultOrError(task.result, null)
                    //map.putString("value1", "true")
                    //map.putString("value2", task.result)
                } else {
                    translatedText.value = ResultOrError(null, task.exception)
                    //map.putString("value1", "false")
                    //map.putString("value1", "ERROR")
                }
                //this.reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                //    .emit("translationComplete", map)
            }
        }
        translate().addOnCompleteListener(processTranslation)
    }

    fun getTranslatedText(): String {
        return translatedText.value?.result!!
    }

    fun getLocale(): String {
        //Log.d("KPhoneModule", "KPhoneModule - getLocale() is called.")
        //String bcp47Code = Locale.getDefault().toString()
        val availableLanguages: List<String> = TranslateLanguage.getAllLanguages().map { it }
        return availableLanguages.toString()
        // return Locale.getDefault().toString()
    }

    inner class ResultOrError(var result: String?, var error: Exception?)
}
