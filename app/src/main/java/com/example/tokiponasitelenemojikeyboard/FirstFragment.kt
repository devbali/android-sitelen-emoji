package com.example.tokiponasitelenemojikeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ourdhi.sitelenemoji.SettingsActivity
import kotlinx.android.synthetic.main.fragment_first.view.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)

        val view = inflater.inflate(R.layout.fragment_first, container, false)
        view.buttonS.setOnClickListener{
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.textview_first).setOnClickListener {
            startActivityForResult(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS),0)
        }

        //view.findViewById<Button>(R.id.button_first).setOnClickListener {
        //    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        //}
    }
}
