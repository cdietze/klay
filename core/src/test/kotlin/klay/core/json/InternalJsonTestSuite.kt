package klay.core.json

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        InternalJsonNumberTest::class,
        InternalJsonParserTest::class,
        InternalJsonTypesTest::class,
        InternalJsonWriterTest::class
)
class InternalJsonTestSuite