package com.tekmentors.jenkins;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before;
import org.junit.Test

public class TestExampleDeclarativeJob extends DeclarativePipelineTest {

    @Before
    void setUp() throws Exception {
        super.setUp()
        helper.addShMock('./build.sh --release', '', 0)
        helper.addShMock('./processTestResults.sh --platform debian', 'Executed with SUCCESS', 0)
        binding.setVariable("BRANCH","Dev")
    }
    @Test
    void should_execute_static_analysis_successfully() throws Exception {
        def script = runScript("Jenkinsfile")
        assertJobStatusSuccess()
        printCallStack()
    }
    @Test
    void should_faile_execute_static_analysis_due_to_buildsh_failure() throws Exception {
        helper.addShMock('./build.sh --release', '', 1)
        def script = runScript("Jenkinsfile")
        assertJobStatusUnstable()
        printCallStack()
    }

    @Test
    void should_execute_packaging_without_errors() throws Exception {
        helper.registerAllowedMethod("sh", [String.class], {cmd->
            if (cmd.contains("mvn clean package")) {
                binding.getVariable('currentBuild').result = 'SUCCESS'
            }
        })

        def script = runScript("Jenkinsfile")

        assertJobStatusSuccess()
        printCallStack()
    }

    @Test
    void should_fail_packaging_with_error() throws Exception {
        helper.registerAllowedMethod("sh", [String.class], {cmd->
            if (cmd.contains("mvn clean package")) {
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })

        def script = runScript("Jenkinsfile")

        assertJobStatusFailure()
        printCallStack()
    }
}

