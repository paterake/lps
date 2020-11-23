package com.paterake.lps.address.translate

import java.io.FileInputStream

import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import com.google.cloud.translate.Translate.TranslateOption
import com.google.cloud.translate.TranslateOptions

class TranslationService {
  private val credentialPath = "/home/paterak/__cfg/google-key-paterake-lsp.json"
  private val credentials: GoogleCredentials = {
    val fis: FileInputStream = new FileInputStream(credentialPath)
    ServiceAccountCredentials.fromStream(fis)
  }
  private val translator = TranslateOptions.newBuilder().setCredentials(credentials).build().getService

  def getTranslation(srcText: String, tgtLanguageCode: String): String = {
    val translation = translator.translate(srcText, TranslateOption.targetLanguage(tgtLanguageCode))
    val tgtText = translation.getTranslatedText
    tgtText
  }

}
