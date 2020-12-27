package io.github.wangeason.demo.ui.home

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
        return root
    }
}