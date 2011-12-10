
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.ayosec.eioclient.tasks.HasKey
import net.liftweb.json

class Conditions extends FlatSpec with ShouldMatchers {

  it should "Detect when the JSON has some keys" in {
    val jsonObject = json.parse("""{"foo": "bar", "one": "two"}""")
    new HasKey("foo").validates(jsonObject) should be (true)
    new HasKey("one").validates(jsonObject) should be (true)
    new HasKey("bar").validates(jsonObject) should be (false)
    new HasKey("non").validates(jsonObject) should be (false)
  }


}
