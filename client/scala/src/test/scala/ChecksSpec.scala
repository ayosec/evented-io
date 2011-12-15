
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.ayosec.eioclient.tasks.MapReduceResult
import net.liftweb.json
import java.util.HashMap

class ChecksSpec extends FlatSpec with ShouldMatchers {

  lazy val jsonObject = json.parse("""[
      {"key": {"a": "b"}, "count": 1}, 
      {"key": {"a": "c"}, "count": 2}, 
      {"key": {"a": "d"}, "count": 3}, 
      {"key": {"0": "1"}, "count": 4}
    ]""")

  it should "detect when the JSON is equal to the expected object" in {
    val expected = new MapReduceResult
    expected.put(Seq("a" -> "b"), 1)
    expected.put(Seq("0" -> "1"), 4)
    expected.put(Seq("a" -> "c"), 2)
    expected.put(Seq("a" -> "d"), 3)
    expected.equals(jsonObject) should be (true)
  }

  it should "detect when the JSON is differete to the expected object" in {
    val expected = new MapReduceResult
    expected.put(Seq("a" -> "b"), 10)
    expected.put(Seq("a" -> "c"), 2)
    expected.put(Seq("0" -> "1"), 4)
    expected.equals(jsonObject) should be (false)
  }

  it should "just return false if the JSON object has an invalid format" in {
    val expected = new MapReduceResult
    expected.put(Seq("0" -> "1"), 4)

    expected.equals(json.parse(""" [{"x": 1}] """)) should be (false)
    expected.equals(json.parse(""" [] """)) should be (false)
    expected.equals(json.parse(""" [1,2,3] """)) should be (false)
    expected.equals(json.parse(""" {} """)) should be (false)
  }

}
