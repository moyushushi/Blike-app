package com.example.blike

import android.app.Application
import com.example.blike.di.ServiceLocator

class BlikeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}