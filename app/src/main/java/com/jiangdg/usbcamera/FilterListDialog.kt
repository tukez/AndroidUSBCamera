/*
 * Copyright 2017-2022 Jiangdg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jiangdg.usbcamera

import android.annotation.SuppressLint
import android.app.Activity
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jiangdg.media.base.BaseDialog
import com.jiangdg.media.render.filter.bean.CameraFilter
import com.jiangdg.media.utils.Utils
import com.jiangdg.media.utils.imageloader.ImageLoaders

/** Filter list dialog
 *
 * @author Created by jiangdg on 2022/2/8
 */
@SuppressLint("NotifyDataSetChanged")
class FilterListDialog(activity: Activity): BaseDialog(activity, portraitWidthRatio = 1f) {
    private var mRecyclerView: RecyclerView? = null
    private var mFilterTabBtn: TextView? = null
    private var mAnimTabBtn: TextView? = null
    private var mFilterList: ArrayList<CameraFilter> = ArrayList()
    private val mFilterMap = HashMap<Int, List<CameraFilter>>()

    init {
        mRecyclerView = mDialog.findViewById(R.id.filterRv)
        mFilterTabBtn = mDialog.findViewById(R.id.tabFilterBtn)
        mAnimTabBtn = mDialog.findViewById(R.id.tabAnimBtn)
        mDialog.window?.let {
            it.setGravity(Gravity.BOTTOM)
            it.setWindowAnimations(R.style.camera2_anim_down_to_top)

            it.attributes?.run {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = (240f / 360f * Utils.getScreenWidth(activity)).toInt()
                mDialog.window?.attributes = this
            }
        }

        mDialog.window?.setDimAmount(0f)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        // recycler view
        FilterListAdapter().apply {
            setOnItemChildClickListener { _, _, position ->
                data.getOrNull(position)?.let {
                    if (getCurrFilter()?.id == it.id) {
                        return@setOnItemChildClickListener
                    }
                    setCurrFilter(it)
                    notifyDataSetChanged()
                }
            }
        }.also { adapter ->
            mRecyclerView?.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
            mRecyclerView?.adapter = adapter
        }
    }

    fun setData(list: List<CameraFilter>) {
        mFilterList.clear()
        mFilterList.addAll(list)
        initFilterData()
        initEffectTabs()
    }

    private fun initEffectTabs() {

    }

    private fun initFilterData() {
        // filter list
        mFilterList.filter {
            it.classifyId == CameraFilter.CLASSIFY_ID_FILTER
        }.let {
            val list = ArrayList<CameraFilter>().apply {
                add(CameraFilter.NONE_FILTER)
                addAll(it)
            }
            mFilterMap[CameraFilter.CLASSIFY_ID_FILTER] = list
        }
        // animation list
        mFilterList.filter {
            it.classifyId == CameraFilter.CLASSIFY_ID_ANIMATION
        }.let {
            val list = ArrayList<CameraFilter>().apply {
                add(CameraFilter.NONE_ANIMATION)
                addAll(it)
            }
            mFilterMap[CameraFilter.CLASSIFY_ID_ANIMATION] = list
        }
    }

    override fun getContentLayoutId(): Int {
        return R.layout.dialog_filters
    }
}

private class FilterListAdapter : BaseQuickAdapter<CameraFilter, BaseViewHolder>(R.layout.dialog_filter_item) {

    private var mCurrFilter: CameraFilter? = null

    fun setCurrFilter(effect: CameraFilter?) {
        val oldPosition = getPosition(mCurrFilter)
        mCurrFilter = effect
        val newPosition = getPosition(effect)
        if (oldPosition != newPosition) {
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }

    }

    fun getCurrFilter(): CameraFilter? = mCurrFilter

    private fun getPosition(cameraFilter: CameraFilter?): Int {
        var position = -1
        cameraFilter ?: return position
        data.forEachIndexed { index, filter ->
            if (filter.id == cameraFilter.id) {
                position = index
                return@forEachIndexed
            }
        }
        return position
    }

    override fun convert(helper: BaseViewHolder, item: CameraFilter) {
        helper.setText(R.id.filter_name, item.name)
        helper.getView<ImageView>(R.id.filter_effect).also {
            item.coverResId?.apply {
                ImageLoaders.of(mContext).loadCircle(it, this, R.drawable.filter_placeholder)
                return@also
            }
            ImageLoaders.of(mContext).loadCircle(it, item.coverUrl, R.drawable.filter_placeholder)
        }
        helper.addOnClickListener(R.id.filter_effect)
        // update check status
        (mCurrFilter?.id == item.id).let { isCheck ->
            val textColor = if (isCheck) 0xFF2E5BFF else 0xFF232325
            helper.setTextColor(R.id.filter_name, textColor.toInt())
            helper.setVisible(R.id.filter_check, isCheck)
        }

    }
}