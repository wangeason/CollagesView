package io.github.wangeason.collages.listener

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import io.github.wangeason.CollagesView
import io.github.wangeason.collages.model.addon.AddOnItem

class OnBtnClickListener : CollagesView.OnBtnClickListener {
    override fun onDelBtnClicked(
            context: Context?,
            collagesView: CollagesView?,
            addOnItem: AddOnItem?
    ) {
        Log.i(TAG, "onDelBtnClicked")
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("确认").setCancelable(true).setMessage("删除贴纸")
            .setPositiveButton("YES",
                DialogInterface.OnClickListener { dialog, which ->
                    collagesView!!.removeAddOn(
                            addOnItem!!
                    )
                })
            .setNegativeButton("NO",
                DialogInterface.OnClickListener { dialog, which -> })
            .create().show()
    }

    override fun onRotateButtonClicked(
        context: Context?,
        collageImageView: CollagesView?,
        addOnItem: AddOnItem?
    ) {
        Log.i(TAG, "onRotateButtonClicked")
    }

    companion object {
        private const val TAG = "CollagesView"
    }
}
