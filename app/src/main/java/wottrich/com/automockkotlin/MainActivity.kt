package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockList
import wottrich.com.mock_annotations.MockModel
import kotlin.reflect.KClass

@MockModel
open class MainActivity : AppCompatActivity() {

    @MockField(String::class, "Lucas", "name")
    private var name: String? = null

    @MockList([""])
    private var list = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val mock = MockMainActivity()
        //mock.name
    }
}
