package com.theone.mvvm.core.app

import android.app.Activity
import android.app.Application
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.hjq.toast.ToastUtils
import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager
import com.theone.common.ext.LogInit
import com.theone.mvvm.base.BaseApplication
import com.theone.mvvm.core.data.entity.RxHttpBuilder
import com.theone.mvvm.core.app.ext.initLoadSir
import com.theone.mvvm.core.ui.activity.TheErrorActivity
import com.theone.mvvm.core.app.util.MMKVUtil
import com.theone.mvvm.core.app.util.NotificationManager
import com.theone.mvvm.core.app.util.RxHttpManager
import rxhttp.RxHttpPlugins

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
 * @date 2021/3/23 0022
 * @describe 对Core里面的组件进行初始化
 * @email 625805189@qq.com
 * @remark
 */
abstract class CoreApplication : BaseApplication() {

    override fun init(application: Application) {
        MMKVUtil.init(application)
        LogInit(DEBUG)
        QMUISwipeBackActivityManager.init(application)
        NotificationManager.getInstance().register()
        initLoadSir()
        ToastUtils.init(this)
    }

}