package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel

@MockModel(serializable = true, serializedName = true)
open class MainActivity : AppCompatActivity() {

    @MockField(String::class, "Lucas", "Name")
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
