package wottrich.com.automockkotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import org.intellij.lang.annotations.Language
import wottrich.com.mock_annotations.MockField
import wottrich.com.mock_annotations.MockModel
//"[{\"name\": \"Lucas\", \"age\":19, \"address\":\"Rua B\", \"canWork\":true, \"child\":[{\"name\":Luiz}, {\"name\":\"Leandro\"}]}]"
//"{\"simulation\": {\"id\": \"FUSeneoSTDoN_A7jSHpNUg\", \"car\": {\"id\": \"aYdDChhWtH2UGMAhvzzLsQ\", \"description\": \"Clio\", \"url\": null, \"brand\": \"Clio\", \"selected\": true, \"gender\": \"PARTICULAR\", \"vehicleType\": \"USADO\", \"version\": {\"id\": \"gdakHXqtMpXzHY5fi1VM1A\", \"description\": \"Clio Campus Hi-Flex 1.0 16V 3p\", \"fipe\": \"025150-0\", \"yearManufacture\": 2010, \"yearModel\": 2010, \"price\": 15000, \"fuel\": {\"id\": \"4tceabM1b30zdE6xH8mTtQ\", \"description\": \"Gasolina\", \"importCode\": \"G\", \"active\": true}, \"options\": [], \"acessories\": []}}}}"
//"{\"simulation\":{\"nameSimulation\":\"Simulation2\", \"car\": {\"nameCar\":\"Um carro\", \"version\": {\"id\":\"123\", \"fuel\": {\"description\": \"Gasolina\"}}}, \"options\":[]}}"
@MockModel(customName = "MockSimulation", archive = "dasd")
open class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



    }
}
