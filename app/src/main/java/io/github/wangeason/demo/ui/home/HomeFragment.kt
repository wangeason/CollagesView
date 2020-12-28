package io.github.wangeason.demo.ui.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.github.wangeason.CollagesView
import io.github.wangeason.collages.model.DragSwapHelperImageView
import io.github.wangeason.demo.R


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var collagesView: CollagesView
    private lateinit var dragSwapHelperImageView: DragSwapHelperImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        collagesView = root.findViewById(R.id.collage)
        dragSwapHelperImageView = root.findViewById(R.id.drag_helper)

        val bitmaps: ArrayList<Bitmap> = ArrayList()
        val flip = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_flip)
        val ic_launcher = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
        val rotate = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_rotate)
        val flip2 = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_flip)
        val ic_launcher2 = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
        val rotate2 = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_rotate)
        val flip3 = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_flip)
        val ic_launcher3 = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher)
        val rotate3 = BitmapFactory.decodeResource(resources, R.drawable.collage_icon_rotate)
        bitmaps.add(flip)
        bitmaps.add(ic_launcher)
        bitmaps.add(rotate)
        bitmaps.add(flip2)
        bitmaps.add(ic_launcher2)
        bitmaps.add(rotate2)
        bitmaps.add(flip3)
        bitmaps.add(ic_launcher3)
        bitmaps.add(rotate3)

        collagesView.setImageBitmaps(bitmaps)
        return root
    }
}