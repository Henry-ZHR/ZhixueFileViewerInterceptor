package moe.henry_zhr.zhixue_fileviewer_interceptor

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import java.io.File

@Keep
class Interceptor constructor(context: Context?) : ContextWrapper(context) {
  @Keep
  fun intercept(intent: Intent): Intent {
    if (intent.component == null)
      return intent
    Log.i(TAG, "Intercept ${applicationContext.packageName} intent: $intent")
    intent.setExtrasClassLoader(Interceptor::class.java.classLoader)
    return interceptFileViewer(intent) ?: intent
  }

  private fun interceptFileViewer(intent: Intent): Intent? {
    if (intent.component?.className != FILE_VIEWER_CLASS_NAME)
      return null

    val path = intent.getCharSequenceExtra("EXTRA_PATH")
    val fileName = intent.getCharSequenceExtra("EXTRA_FILE_NAME")
    if (path == null || fileName == null)
      return null

    val file = File(path.toString(), fileName.toString())
    val authority = applicationContext.packageName + ".provider"
    val uri = FileProvider.getUriForFile(applicationContext, authority, file)
    val type = contentResolver.getType(uri)

    return Intent(Intent.ACTION_VIEW).apply {
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      setDataAndType(uri, type)
    }
  }

  companion object {
    const val TAG = "ZhixueFileViewerInterceptor"
    const val FILE_VIEWER_CLASS_NAME =
      "com.iflytek.newclass.app_student.modules.punchHomework.ViewDocDetailActivity"
  }

  init {
    Log.i(TAG, "Inject application ${applicationContext.packageName}")
  }
}