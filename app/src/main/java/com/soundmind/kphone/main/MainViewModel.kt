/*
 * Copyright 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.soundmind.kphone.main

import android.app.Application
import android.os.Handler
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
import com.soundmind.kphone.main.ViewGoFragment.Companion.DESIRED_HEIGHT_CROP_PERCENT
import com.soundmind.kphone.main.ViewGoFragment.Companion.DESIRED_WIDTH_CROP_PERCENT
import com.soundmind.kphone.util.Language
import com.soundmind.kphone.util.ResultOrError
import com.soundmind.kphone.util.SmoothedMutableLiveData
import java.util.concurrent.Executor

class MainViewModel(application: Application) : AndroidViewModel(application) {
    override fun onCleared() {
        languageIdentification.close()
        translators.evictAll()
    }

    /*lateinit*/ var executor: Executor = Executor { /* Dummy executor */ }
    private val languageIdentification by lazy {
        LanguageIdentification.getClient(
            LanguageIdentificationOptions.Builder().setExecutor(executor).build()
        )
    }

    val targetLang = MutableLiveData<Language>()
    val sourceText = SmoothedMutableLiveData<String>(SMOOTHING_DURATION)

    // We set desired crop percentages to avoid having the analyze the whole image from the live
    // camera feed. However, we are not guaranteed what aspect ratio we will get from the camera, so
    // we use the first frame we get back from the camera to update these crop percentages based on
    // the actual aspect ratio of images.
    val imageCropPercentages = MutableLiveData<Pair<Int, Int>>()
        .apply { value = Pair(DESIRED_HEIGHT_CROP_PERCENT, DESIRED_WIDTH_CROP_PERCENT) }
    val translatedText = MediatorLiveData<ResultOrError>()
    private val translating = MutableLiveData<Boolean>()
    val modelDownloading = SmoothedMutableLiveData<Boolean>(SMOOTHING_DURATION)

    private var modelDownloadTask: Task<Void> = Tasks.forCanceled()

    private val translators =
        object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
            override fun create(options: TranslatorOptions): Translator {
                return Translation.getClient(options)
            }

            override fun entryRemoved(
                evicted: Boolean,
                key: TranslatorOptions,
                oldValue: Translator,
                newValue: Translator?
            ) {
                oldValue.close()
            }
        }
    /***********************************************/
    val availableModels = MutableLiveData<List<String>>()
    private val modelManager: RemoteModelManager = RemoteModelManager.getInstance()
    private val pendingDownloads: HashMap<String, Task<Void>> = hashMapOf()

    // Updates the list of downloaded models available for local translation.
    private fun fetchDownloadedModels() {
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java).addOnSuccessListener {
                remoteModels ->
            availableModels.value = remoteModels.sortedBy { it.language }.map { it.language }
        }
    }

    // Starts downloading a remote model for local translation.
    internal fun downloadLanguage(language: Language) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language.code)!!)
        var downloadTask: Task<Void>?
        if (pendingDownloads.containsKey(language.code)) {
            downloadTask = pendingDownloads[language.code]
            // found existing task. exiting
            if (downloadTask != null && !downloadTask.isCanceled) {
                return
            }
        }
        downloadTask =
            modelManager.download(model, DownloadConditions.Builder().build()).addOnCompleteListener {
                pendingDownloads.remove(language.code)
                fetchDownloadedModels()
            }
        pendingDownloads[language.code] = downloadTask
    }
    // Returns if a new model download task should be started.
    fun requiresModelDownload(
        lang: Language,
        downloadedModels: List<String?>?,
    ): Boolean {
        return if (downloadedModels == null) {
            true
        } else !downloadedModels.contains(lang.code) && !pendingDownloads.containsKey(lang.code)
    }

    private fun getModel(languageCode: String): TranslateRemoteModel {
        return TranslateRemoteModel.Builder(languageCode).build()
    }
    /***********************************************/
    /***********************************************/

    // Deletes a locally stored translation model.
    internal fun deleteLanguage(language: Language) {
        val model = getModel(TranslateLanguage.fromLanguageTag(language.code)!!)
        modelManager.deleteDownloadedModel(model).addOnCompleteListener { fetchDownloadedModels() }
        pendingDownloads.remove(language.code)
    }

    val sourceLang = MediatorLiveData<Language>()

    private fun translate(): Task<String> {
        if (modelDownloading.value != false || translating.value != false) {
            return Tasks.forCanceled()
        }
        val text = sourceText.value ?: return Tasks.forResult("")
        val source = sourceLang.value ?: return Tasks.forResult("")
        val target = targetLang.value ?: return Tasks.forResult("")
        val sourceLangCode =
            TranslateLanguage.fromLanguageTag(source.code) ?: return Tasks.forCanceled()
        val targetLangCode =
            TranslateLanguage.fromLanguageTag(target.code) ?: return Tasks.forCanceled()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
            .setExecutor(executor)
            .build()
        val translator = translators[options]
        modelDownloading.setValue(true)

        // Register watchdog to unblock long running downloads
        Handler().postDelayed({ modelDownloading.setValue(false) }, 15000)
        modelDownloadTask = translator.downloadModelIfNeeded().addOnCompleteListener {
            modelDownloading.setValue(false)
        }
        translating.value = true
        return modelDownloadTask.onSuccessTask {
            translator.translate(text)
        }.addOnCompleteListener {
            translating.value = false
        }
    }

    // Gets a list of all available translation languages.
    val availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
        .map { Language(it) }

    init {
        modelDownloading.setValue(false)
        translating.value = false

        sourceLang.addSource(sourceText) { text ->
            languageIdentification.identifyLanguage(text)
                .addOnSuccessListener {
                    if (it != "und")
                        sourceLang.value = Language(it)
                }
        }

        // Create a translation result or error object.
        val processTranslation =
            OnCompleteListener<String> { task ->
                if (task.isSuccessful) {
                    translatedText.value = ResultOrError(task.result, null)
                } else {
                    if (task.isCanceled) {
                        // Tasks are cancelled for reasons such as gating; ignore.
                        return@OnCompleteListener
                    }
                    translatedText.value = ResultOrError(null, task.exception)
                }
            }
        // Start translation if any of the following change: detected text, source lang, target lang.
        translatedText.addSource(sourceText) { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(sourceLang) { translate().addOnCompleteListener(processTranslation) }
        translatedText.addSource(targetLang) { translate().addOnCompleteListener(processTranslation) }
    }

    companion object {
        // Amount of time (in milliseconds) to wait for detected text to settle
        private const val SMOOTHING_DURATION = 50L

        private const val NUM_TRANSLATORS = 1
    }
}
