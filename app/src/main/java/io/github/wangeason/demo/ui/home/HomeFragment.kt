package io.github.wangeason.demo.ui.home

import android.content.Intent
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
import io.github.wangeason.collages.model.DragSwapHelperImageView
import io.github.wangeason.demo.R
import kotlin.random.Random


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var collagesView: CollagesView
    private lateinit var dragSwapHelperImageView: DragSwapHelperImageView
    val bitmaps: ArrayList<Bitmap> = ArrayList()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        collagesView = root.findViewById(R.id.collage)
        dragSwapHelperImageView = root.findViewById(R.id.drag_helper)

        (root.findViewById(R.id.clear_pic) as Button).setOnClickListener {
            val arrayList = ArrayList<Bitmap>()
            val cnt = Random.nextInt(2,9)
            System.out.println(cnt)
            arrayList.addAll(bitmaps.subList(0, cnt))
            collagesView.setImageBitmaps(arrayList)
        }


        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image1))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image2))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image3))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image4))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image5))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image6))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image7))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image8))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image9))

        collagesView.setImageBitmaps(bitmaps)

        return root
    }
}