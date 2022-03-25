package com.theone.mvvm.core.base.fragment

import android.view.KeyEvent
import android.view.View
import androidx.databinding.ViewDataBinding
import com.hjq.toast.ToastUtils
import com.theone.mvvm.base.fragment.BaseVmDbFragment
import com.theone.mvvm.base.viewmodel.BaseViewModel
import com.theone.mvvm.core.R
import com.theone.mvvm.core.base.callback.ICore
import com.theone.mvvm.core.app.ext.hideProgressDialog
import com.theone.mvvm.core.app.ext.registerLoader
import com.theone.mvvm.core.app.ext.showProgressDialog
import com.theone.mvvm.core.base.loader.LoaderView
import com.theone.mvvm.entity.ProgressBean

/**
 * @author The one
 * @date 2021/3/23 0022
 * @describe CoreBaseFragment
 * @email 625805189@qq.com
 * @remark 添加界面状态管理
 */
abstract class BaseCoreFragment<VM : BaseViewModel, DB : ViewDataBinding> :
    BaseVmDbFragment<VM, DB>(), ICore {

    private val mLoader: LoaderView by lazy {
        LoaderView(getViewConstructor())
    }

    override fun getLoader(): LoaderView? = if (loaderRegisterView() != null) mLoader else null

    override fun onCreateView(): View {
        return super.onCreateView().apply {
            registerLoader()
        }
    }

    override fun showProgress(progress: ProgressBean) {
        requireActivity().showProgressDialog(progress)
    }

    override fun hideProgress() {
        hideProgressDialog()
    }

    override fun showExitTips() {
        ToastUtils.show(R.string.core_exit_tips)
    }

    private var exitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isExitPage()) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                //弹出提示，可以有多种方式
                exitTime = System.currentTimeMillis()
                showExitTips()
            } else {
                requireActivity().finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}