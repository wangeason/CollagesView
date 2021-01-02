package io.github.wangeason.demo.ui.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.github.wangeason.CollagesView
import io.github.wangeason.EditMode
import io.github.wangeason.demo.R

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel

    private lateinit var addButton: Button
    private lateinit var collagesView: CollagesView
    val bitmaps: ArrayList<Bitmap> = ArrayList()
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
                ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image1))

        collagesView = root.findViewById(R.id.collage)
        collagesView.editMode = EditMode.SINGLE
        collagesView.setImageBitmaps(bitmaps)
        addButton = root.findViewById(R.id.add_text)

        addButton.setOnClickListener {
            collagesView.addOnBitmap(BitmapFactory.decodeResource(resources, R.drawable.sticker)) }


        return root
    }
}