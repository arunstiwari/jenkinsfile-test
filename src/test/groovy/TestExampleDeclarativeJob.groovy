package com.tekmentors.jenkins;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before;
import org.junit.Test

public class TestExampleDeclarativeJob extends DeclarativePipelineTest {

    @Before
    void setUp() throws Exception {
//        helper.scriptRoots += 'vars'
        super.setUp()
    }
    @Test
    void should_execute_without_errors() throws Exception {
//        helper.addShMock('uname', 'Debian', 0)
        helper.addShMock('./build.sh --release', '', 0)
//        helper.addShMock('./test.sh', '', 0)
        def script = runScript("Jenkinsfile")
        assertJobStatusSuccess()
        printCallStack()
    }
}

