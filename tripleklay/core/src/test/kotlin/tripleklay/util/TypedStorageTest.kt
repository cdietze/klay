package tripleklay.util

class TypedStorageTest {

    // TODO(cdi) re-add when react.RSet is implemented
//    @Test fun testSetFor() {
//        val pf = StubPlatform()
//        val ts = TypedStorage(pf.log(), pf.storage())
//        val id = Functions.identity()
//
//        val strings = ts.setFor("strings", id, id)
//        assertTrue(strings.isEmpty())
//        strings.add("one")
//
//        // (each call to setFor creates a new set from the curren state of storage)
//        assertTrue(ts.setFor("strings", id, id).contains("one"))
//
//        strings.remove("one")
//        assertTrue(ts.setFor("strings", id, id).isEmpty())
//
//        strings.add("")
//        assertTrue(ts.setFor("strings", id, id).contains(""))
//        strings.add("two")
//        assertTrue(ts.setFor("strings", id, id).contains(""))
//        strings.remove("two")
//        assertTrue(ts.setFor("strings", id, id).contains(""))
//        strings.remove("")
//        assertFalse(ts.setFor("strings", id, id).contains(""))
//    }
}
