package com.anwesh.uiprojects.linkedlinefoldingupview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linefoldingupview.LineFoldingUpView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineFoldingUpView.create(this)
    }
}
