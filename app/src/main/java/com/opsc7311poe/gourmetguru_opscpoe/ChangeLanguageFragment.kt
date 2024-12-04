package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import java.util.Locale

class ChangeLanguageFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var btnSave: Button
    private lateinit var spinLang: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_change_language, container, false)

        //populate spinner
        spinLang = view.findViewById(R.id.spinLang)

        val langs = arrayOf(getString(R.string.spinEnglishOption), getString(R.string.spinAfrikaansOption), getString(R.string.spinZuluOption), getString(R.string.spinPortugueseOption))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, langs)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinLang.adapter = adapter


        //back btn functionality
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener(){
            replaceFragment(Settings())
        }

        //save button functionality
        btnSave = view.findViewById(R.id.btnSave)
        btnSave.setOnClickListener(){
            val selectedLang = spinLang.selectedItem

            //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 26 October 2024].
            //changing app language into selected language
            when (selectedLang) {
                getString(R.string.spinEnglishOption) -> {
                    setAppLang("en")
                }
                getString(R.string.spinAfrikaansOption) -> {
                    setAppLang("af")
                }
                getString(R.string.spinZuluOption) -> {
                    setAppLang("zu")
                }
                getString(R.string.spinPortugueseOption) -> {
                    setAppLang("pt")
                }
            }

        }

        return view
    }

    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 26 October 2024].
    fun setAppLang(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        //allowing user to see change in language on current page
        replaceFragment(ChangeLanguageFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Settings Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}