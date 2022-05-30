package com.theone.mvvm.core.base.adapter

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectMimeType.ofAudio
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.DateUtils
import com.qmuiteam.qmui.widget.QMUIRadiusImageView2
import com.theone.common.ext.*
import com.theone.mvvm.core.R
import com.theone.mvvm.core.app.ext.load

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
 * @date 2021-04-30 09:33
 * @describe 图片选择适配器
 * @email 625805189@qq.com
 * @remark
 */
class PictureSelectorAdapter :
    BaseQuickAdapter<LocalMedia, BaseViewHolder>(R.layout.item_picture_selector) {

    companion object {
        const val TYPE_ADD = 1
        const val TYPE_PICTURE = 2
    }

    var mSelectMax: Int = 9

    override fun getItemCount(): Int {
        return if (data.size < mSelectMax) {
            data.size + 1
        } else {
            data.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.size) {
            TYPE_ADD
        } else {
            TYPE_PICTURE
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (position == data.size) {
            convertAdd(holder, position)
        } else
            convert(holder, data[position])
    }

    private fun convertAdd(holder: BaseViewHolder, position: Int) {
        with(holder) {
            getView<QMUIRadiusImageView2>(R.id.iv_filter).run {
                setImageResource(R.drawable.ic_add_image)
            }
            getView<View>(R.id.delete_layout).invisible()
        }
    }

    override fun convert(holder: BaseViewHolder, item: LocalMedia) {
        with(holder) {
            val cover = getView<QMUIRadiusImageView2>(R.id.iv_filter)
            getView<View>(R.id.delete_layout).run {
                visible()
                setOnClickListener {
                    val index = holder.absoluteAdapterPosition
                    if (index != RecyclerView.NO_POSITION && data.size > index) {
                        data.removeAt(index)
                        notifyItemRemoved(index)
                        notifyItemRangeChanged(index, data.size)
                    }
                }
            }
            val path = item.availablePath
            val isVideo = PictureMimeType.isHasVideo(item.mimeType)
            val isAudio = PictureMimeType.isHasAudio(item.mimeType)
            getView<TextView>(R.id.tv_duration).run {
                if (isVideo || isAudio) {
                    visible()
                    setCompoundDrawablesRelativeWithIntrinsicBounds(
                       if (isVideo) R.drawable.ps_ic_video else R.drawable.ps_ic_audio,
                        0,
                        0,
                        0
                    )
                    text = DateUtils.formatDurationTime(item.duration)
                } else {
                    invisible()
                }
            }
            if (isAudio) {
                cover.setImageResource(R.drawable.ps_ic_audio_placeholder)
            } else {
                cover.load(
                    if (PictureMimeType.isContent(path) && !item.isCut && !item.isCompressed)
                        Uri.parse(path)
                    else path
                )
            }
        }
    }

}
