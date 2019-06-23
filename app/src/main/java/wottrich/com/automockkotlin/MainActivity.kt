package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel

@MockModel(customName = "", body = "[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true}, {\"name\": \"Marcelo\", \"age\":20, \"address\":\"Rua C\", \"canWork\":false}]")
open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }
}
