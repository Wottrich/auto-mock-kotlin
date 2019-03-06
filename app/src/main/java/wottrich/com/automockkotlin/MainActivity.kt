package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel

@MockModel(list = true)
open class MainActivity : AppCompatActivity() {

    @MockField(String::class, "Lucas", "name")
    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for((i, mock) in MockMainActivityList().getMockMainActivityList().withIndex()) {
            Toast.makeText(this, "${mock.name} - $i", Toast.LENGTH_SHORT).show()
        }
    }
}
