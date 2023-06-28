/**
 * Copyright (C) 2020 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.obelab.repace.core.platform

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
abstract class BaseFragment : Fragment() {
    abstract fun layoutId(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layoutId(), container, false)
    }

    internal fun showLoading() = BaseActivity.instance?.showLoading()

    internal fun hideLoading() = BaseActivity.instance?.hideLoading()

    internal fun showToast(message: String) =
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    internal fun showPopupDisconnectedDevice() = BaseActivity.instance?.showPopupDisconnectedDevice()
    internal fun showPopupDisconnectedDevice(context: Context) = BaseActivity.instance?.showPopupDisconnectedDevice(context)

    internal fun Date.toString(format: String, locale: Locale = Locale.ENGLISH): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    internal fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }
}
