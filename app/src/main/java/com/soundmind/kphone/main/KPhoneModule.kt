package com.soundmind.kphone.main

import android.app.Application
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.soundmind.kphone.util.Language

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

    var linggoTranslated = MutableLiveData<String>()
    var viewgoTranslated = MutableLiveData<String>()
    fun translateTop(language: String, lingo: String, viewgo: String) {
        var registerTranslated = { translated: String -> linggoTranslated.postValue(translated) }
        testTranslate(language, lingo, callback = registerTranslated)
        registerTranslated = { translated: String -> viewgoTranslated.postValue(translated) }
        testTranslate(language, viewgo, callback = registerTranslated)
    }
    fun testTranslate(language: String, message_in_english: String, callback: (String) -> Unit) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage("en")
            .setTargetLanguage(language)
            .build()
        val model = Translation.getClient(options)
        Log.d("KPhoneModule", "Translation model: - [${model}]")
        model
            .downloadModelIfNeeded( )
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    Log.d("KPhoneModule", "Translation model is downloaded")
                    model.translate(message_in_english)
                } else {
                    Tasks.forException(
                        task.exception
                            ?: Exception("Unknown error occurred")
                    )
                }
            }
            .addOnCompleteListener(
                OnCompleteListener<String> { task ->
                    run {
                        if (task.isSuccessful) {
                            Log.d("KPhoneModule", "Translation result: - [${task.result}]")
                            callback(task.result)
                        } else {
                            Log.d("KPhoneModule", "Translation error: - [${task.exception}]")
                        }
                    }
                }
            )
    }

    fun downloadLanguageModel(language: String) {
        val languageCode = TranslateLanguage.fromLanguageTag(language)
        val model = getModel(languageCode!!)
        val manager = RemoteModelManager.getInstance()
        Log.d("KPhoneModule", "KPhoneModule - [${model.language}] downloading is requested")
        manager.download(model, DownloadConditions.Builder().build()).addOnCompleteListener {
            // fetch
            Log.d("KPhoneModule", "KPhoneModule - [${model.language}] is downloaded")
            checkAndDownload(systemLanguage = language)
        }.addOnFailureListener {
            Log.d("KPhoneModule", "KPhoneModule - [${model.language}] downloading is failed")
        }
    }

    var warnings = MutableLiveData<String>()
    var warning_: String = ""
    init {
        warnings.value = ""
    }

    fun checkAndDownload(systemLanguage: String) {
        val manager = RemoteModelManager.getInstance()
        val required: HashMap<String, String> = hashMapOf()
        required["ko"] = "ko"
        required["en"] = "en"
        required[systemLanguage] = systemLanguage

        manager.getDownloadedModels(TranslateRemoteModel::class.java).addOnSuccessListener {
            for (model in it) {
                required.remove(model.language)
            }
            if (required.size > 0) {
                warning_ = "Some languages are missing. please check network to download and run application again."
                warnings.value = warning_
            } else {
                warning_ = ""
                warnings.value = warning_
            }
            Log.d("KPhoneModule", "KPhoneModule - COUNT - [${required.size}]")
            for (key in required.keys) {
                downloadLanguageModel(key)
            }
        }
    }

    fun testPrintAllModel() {
        val manager = RemoteModelManager.getInstance()
        manager.getDownloadedModels(TranslateRemoteModel::class.java).addOnSuccessListener {
            for (model in it) {
                val info = model.language
                Log.d("KPhoneModule", "KPhoneModule - [${info}]")
            }
        }
    }

    fun testDeleteAllModel() {
        val manager = RemoteModelManager.getInstance()
        manager.getDownloadedModels(TranslateRemoteModel::class.java).addOnSuccessListener {
            for (model in it) {
                val info = model.language
                manager.deleteDownloadedModel(model)
                    .addOnSuccessListener {
                        Log.d("KPhoneModule", "KPhoneModule - [${info}] is deleted")
                    }
                    .addOnFailureListener {
                        Log.d("KPhoneModule", "KPhoneModule - [${info}] is not deleted")
                    }
            }
        }
    }

    fun downloadAllRequiredLanguages(systemLanguage: String) {
        downloadLanguageModel(systemLanguage)
        downloadLanguageModel("ko")
        //downloadLanguageModel("en") // -> english language model is already installed and cannot be deleted.
    }

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
            Log.d("KPhoneModule", "KPhoneModule - [$language - ${model}] is downloaded")
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
        languageIdentification.identifyLanguage(source)
            .addOnSuccessListener {
                if (it != "und")
                    sourceLang = it //Language(it)
                //log("${source} is set.")
                if (source.length > 0) {
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
            }
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

    private val languageIdentification by lazy {
        LanguageIdentification.getClient(
            //LanguageIdentificationOptions.Builder().setExecutor(executor).build()
            LanguageIdentificationOptions.Builder().build()
        )
    }
    inner class ResultOrError(var result: String?, var error: Exception?)
}
