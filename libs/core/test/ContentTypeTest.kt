import com.sebbia.cloudevents.core.ContentType
import com.sebbia.cloudevents.core.toContentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals

class ContentTypeTest {

    @Test
    fun `Content type to string`() {
        assertEquals(
            "image/svg",
            ContentType("image", "svg").toString()
        )
        assertEquals(
            "image/svg+xml",
            ContentType("image", "svg", "xml").toString()
        )
        assertEquals(
            "image/svg+xml;charset=UTF-8",
            ContentType("image", "svg", "xml", "charset=UTF-8").toString()
        )
    }

    @Test
    fun `Compare content type`() {
        assertEquals(
            ContentType("image", "svg"),
            ContentType("image", "svg")
        )
        assertNotEquals(
            ContentType("image", "svg", "xml"),
            ContentType("image", "svg")
        )
    }

    @Test
    fun `Parse content type`() {
        assertFails { "image".toContentType() }
        assertEquals(ContentType("image", "svg"), "image/svg".toContentType())
        assertEquals(ContentType("image", "svg", "xml"), "image/svg+xml".toContentType())
        assertEquals(
            ContentType("image", "svg", "xml", "charset=UTF-8"),
            "image/svg+xml; charset=UTF-8".toContentType()
        )
    }

}