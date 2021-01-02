package io.github.wangeason.demo.ui.notifications

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import io.github.wangeason.CollagesView
import io.github.wangeason.EditMode
import io.github.wangeason.demo.R

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel

    private lateinit var addButton: Button
    private lateinit var input: EditText
    private lateinit var collagesView: CollagesView
    val bitmaps: ArrayList<Bitmap> = ArrayList()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)


        input = root.findViewById(R.id.text_input)
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.image1))

        collagesView = root.findViewById(R.id.collage)
        collagesView.editMode = EditMode.SINGLE
        collagesView.setImageBitmaps(bitmaps)
        addButton = root.findViewById(R.id.add_text)

        addButton.setOnClickListener {
            val text = input.text.toString()
            if (!text.isEmpty()) collagesView.addOnText(text)}


        return root
    }
}