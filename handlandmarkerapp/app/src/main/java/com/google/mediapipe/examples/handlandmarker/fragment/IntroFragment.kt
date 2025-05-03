package com.google.mediapipe.examples.handlandmarker.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.handlandmarker.R
import com.google.mediapipe.examples.handlandmarker.databinding.FragmentIntroBinding

class IntroFragment : Fragment() {

    private var _fragmentIntroBinding: FragmentIntroBinding? = null
    private val fragmentIntroBinding
        get() = _fragmentIntroBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentIntroBinding = FragmentIntroBinding.inflate(inflater, container, false)
        return fragmentIntroBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentIntroBinding.startButton.setOnClickListener {
            findNavController().navigate(R.id.action_intro_to_camera)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentIntroBinding = null
    }
} 