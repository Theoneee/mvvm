package com.theone.mvvm.core.service

import android.app.Activity
import android.app.IntentService
import android.app.Notification
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.core.app.NotificationCompat
import com.hjq.toast.ToastUtils
import com.theone.common.constant.BundleConstant
import com.theone.common.ext.delay
import com.theone.common.ext.installApk
import com.theone.mvvm.core.app.appContext
import com.theone.mvvm.core.R
import com.theone.mvvm.core.app.util.FileDownloadUtil
import com.theone.mvvm.core.data.entity.DownloadBean
import com.theone.mvvm.core.app.util.NotificationManager
import java.io.File
import java.util.*

//  ┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//    ┃　　　┃                  神兽保佑
//    ┃　　　┃                  永无BUG！
//    ┃　　　┗━━━┓
//    ┃　　　　　　　┣┓
//    ┃　　　　　　　┏┛
//    ┗┓┓┏━┳┓┏┛
//      ┃┫┫　┃┫┫
//      ┗┻┛　┗┻┛
/**
 * @author The one
 * @date 2021-05-08 13:15
 * @describe TODO
 * @email 625805189@qq.com
 * @remark
 */

fun Activity.startDownloadService(download: DownloadBean) {
    val intent = Intent(this, DownloadService::class.java).apply {
        putExtra(BundleConstant.DATA, download)
    }
    startService(intent)
}

class DownloadService : IntentService(this::class.java.canonicalName) {

    companion object {
        const val TAG = "DownloadService"
        const val DOWNLOAD_OK = "download_ok"
        const val DOWNLOAD_PATH = "download_path"
        const val DOWNLOAD_ERROR = "download_error"
        const val DOWNLOAD_ERROR_MSG = "download_error_msg"
        const val DOWNLOAD_PROGRESS = "download_progress"
        const val DOWNLOAD_PROGRESS_PERCENT = "download_progress_percent"
    }

    private var mDownload: DownloadBean? = null
    private var NOTIFICATION_ID: Int = UUID.randomUUID().hashCode()
    private var mOldPercent: Int = 0

    private lateinit var mNotificationBuilder: NotificationCompat.Builder

    override fun onHandleIntent(intent: Intent?) {
        if (null != intent && null == mDownload) {
            NOTIFICATION_ID = UUID.randomUUID().hashCode()
            mDownload = intent.getParcelableExtra(BundleConstant.DATA)
            initNotification()
            startDown()
        } else {
            ToastUtils.show("等待当前任务下载完成")
        }
    }

    private fun initNotification() {
        mNotificationBuilder = NotificationManager.getInstance().createNotification(
            NOTIFICATION_ID,
            "开始下载",
            "开始下载",
            mDownload?.url
        ).apply {
            setOngoing(true)
        }
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build())
    }

    private fun startDown() {
        mDownload?.run {
            FileDownloadUtil.get()
                .download(url, downloadPath, name, object : FileDownloadUtil.OnDownloadListener {
                    override fun onDownloadSuccess(file: File) {
                        mHandler.sendMessage(Message.obtain().apply {
                            what = DOWNLOAD_SUCCESS
                            obj = DownloadInfo("下载成功", file)
                        })
                    }

                    override fun onDownloading(progress: Int) {
                        if (progress != mOldPercent) {
                            mOldPercent = progress
                            updateProgress(progress)
                            sendBroadCast(DOWNLOAD_PROGRESS, DOWNLOAD_PROGRESS_PERCENT, progress)
                        }
                    }

                    override fun onDownloadFailed(e: java.lang.Exception?) {
                        val error = e?.localizedMessage
                        val file = File(downloadPath, name)

                        mHandler.sendMessage(Message.obtain().apply {
                            what = DOWNLOAD_FAIL
                            obj = DownloadInfo(error?:"", file)
                        })
                    }
                })
        }
    }

    private val DOWNLOAD_SUCCESS = 1
    private val DOWNLOAD_FAIL = 0

    private data class DownloadInfo(val msg: String, val file: File)

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val obj = msg.obj as DownloadInfo
            when (msg.what) {
                DOWNLOAD_SUCCESS -> {
                    if (obj.file.name.endsWith(".apk") && mDownload?.apkInstall == true) {
                        installApk(obj.file)
                    } else {
                        updateLocationFile(obj.file)
                        updateNotification("下载完成", obj.file.absolutePath, true)
                        sendBroadCast(DOWNLOAD_OK, DOWNLOAD_PATH, obj.file.absolutePath)
                    }
                }

                DOWNLOAD_FAIL -> {
                    updateNotification("下载失败", obj.file.absolutePath, false)
                    sendBroadCast(DOWNLOAD_ERROR, DOWNLOAD_ERROR_MSG, obj.msg)
                    ToastUtils.show(obj.msg)
                    if (obj.file.exists()) {
                        obj.file.delete()
                    }
                }
            }
            stopForeground(true)
            stopSelf()
        }

    }

    private fun updateNotification(title: String, content: String, isSuccess: Boolean) {
        val builder = NotificationManager.getInstance().createNotification(
            NOTIFICATION_ID, title, title, content,
            smallIcon = if (isSuccess) R.drawable.service_down_finish else R.drawable.service_down
        ).apply {
            setDefaults(Notification.DEFAULT_VIBRATE)
            setAutoCancel(true)
        }
        builder.build().run {
            flags = Notification.FLAG_AUTO_CANCEL
        }
        NotificationManager.getInstance().notify(NOTIFICATION_ID, builder)
    }

    private fun updateProgress(percent: Int) {
        mNotificationBuilder.run {
            setContentTitle("下载中")
            setContentText("$percent%")
            setProgress(100, percent, false)
        }
        NotificationManager.getInstance().notify(NOTIFICATION_ID, mNotificationBuilder)
    }


    /**
     * 通知系统刷新文件
     */
    private fun updateLocationFile(file: File?) {
        try {
            MediaScannerConnection.scanFile(
                appContext,
                arrayOf(file?.absolutePath),
                null
            ) { path: String?, _: Uri? ->
                ToastUtils.show("已保存到：$path")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun sendBroadCast(action: String, msgName: String, msgValue: Any) {
        sendBroadcast(Intent().apply {
            setAction(action)
            if (msgValue is String)
                putExtra(msgName, msgValue)
            else if (msgValue is Int)
                putExtra(msgName, msgValue)
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null

}