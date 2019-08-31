package com.odnovolov.forgetmenot.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

open class BaseFragment : Fragment() {

    val fragmentScope = MainScope()
    var viewScope: CoroutineScope? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewScope = MainScope()
    }

    fun <T> Flow<T>.observe(coroutineScope: CoroutineScope = viewScope!!,
                            onChange: (value: T) -> Unit) {
        coroutineScope.launch {
            collect {
                onChange(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewScope!!.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.cancel()
    }

}