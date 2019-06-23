package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel

@MockModel(customName = "CustomerModel", list = true)
open class MainActivity : AppCompatActivity() {

    // body = "{\"name\":\"Lucas\", \"age\":34}"

    @MockField(String::class, "Lucas", "name")
    private var name: String? = null

    @MockField(Int::class, "10", "age")
    private var age: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
