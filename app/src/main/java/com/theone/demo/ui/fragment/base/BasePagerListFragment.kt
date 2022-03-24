package com.theone.demo.ui.fragment.base

import android.util.Log
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.theone.common.ext.delay
import com.theone.demo.app.ext.setAdapterAnimation
import com.theone.demo.viewmodel.AppViewModel
import com.theone.demo.viewmodel.BasePagerViewModel
import com.theone.mvvm.ext.getAppViewModel
import com.theone.mvvm.core.base.fragment.BasePagerSwipeRefreshFragment


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
 * @date 2021/3/3 0003
 * @describe TODO
 * @email 625805189@qq.com
 * @remark
 */
abstract class BasePagerListFragment<T, VM : BasePagerViewModel<T>, DB : ViewDataBinding> :
    BasePagerSwipeRefreshFragment<T, VM, DB>() {

    protected val mAppVm: AppViewModel by lazy { getAppViewModel<AppViewModel>() }

    /**
     * 第一次请求成功后是否自动刷新（第一次的数据从Cache里获取的)
     */
    protected open fun isFirstLoadSuccessAutoRefresh() = true

    override fun getItemSpace(): Int = 12

    override fun initAdapter() {
        super.initAdapter()
        mAdapter.setAdapterAnimation(mAppVm.appAnimation.value)
    }

    override fun createObserver() {
        super.createObserver()
        mAppVm.appAnimation.observe(this, Observer {
            mAdapter.setAdapterAnimation(it)
        })
    }

    override fun onFirstLoadSuccess(data: List<T>) {
        super.onFirstLoadSuccess(data)
        if (isFirstLoadSuccessAutoRefresh() && getViewModel().isCache) {
            getViewModel().isCache = false
            delay(500) {
                onAutoRefresh()
            }
        }
    }

    override fun onRefreshSuccess(data: List<T>) {
        mAdapter.setDiffNewData(data.toMutableList()) {
            setRefreshLayoutEnabled(true)
            getRecyclerView().scrollToPosition(0)
        }
    }

    override fun onFirstLoadError(errorMsg: String?) {
        if (isFirstLoadSuccessAutoRefresh() && getViewModel().isCache) {
            getViewModel().isCache = false
            onFirstLoading()
        } else {
            super.onFirstLoadError(errorMsg)
        }
    }

}