package com.tekmentors.jenkins;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before;
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.*

public class TestExampleDeclarativeJob extends DeclarativePipelineTest {

    @Before
    void setUp() throws Exception {
        super.setUp()
        helper.addShMock('./build.sh --release', '', 0)
        helper.addShMock('./processTestResults.sh --platform debian', 'Executed with SUCCESS', 0)
        binding.setVariable("BRANCH","pev1")
    }

    @Test
    void should_execute_checkout_successfully() throws Exception {
        helper.registerAllowedMethod("sh", [String.class], {cmd->
            if (cmd.contains("if([ ${BRANCH} = \"Dev\" -o ${BRANCH} = \"Fast-Track\"])")) {
                binding.getVariable('currentBuild').result = 'SUCCESS'
            }
        })
        def script = runScript("Jenkinsfile")
        assertEquals("Branch Name is Dev","pev1",binding.getVariable("BRANCH"))

        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            callArgsToString(call).contains("echo")
        })

        assertJobStatusSuccess()
        printCallStack()
    }

    @Test
    void should_execute_static_analysis_successfully() throws Exception {
        def script = runScript("Jenkinsfile")
        assertJobStatusSuccess()
        printCallStack()
    }

    @Test
    void should_fail_execute_static_analysis_due_to_buildsh_failure() throws Exception {
        helper.addShMock('./build.sh --release', '', 1)
        def script = runScript("Jenkinsfile")
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            callArgsToString(call).contains("mvn clean package")
        })
        assertJobStatusUnstable()
        printCallStack()
    }

    @Test
    void should_fail_execute_static_analysis_due_to_processtestresult_failure() throws Exception {
        helper.addShMock('./processTestResults.sh --platform debian', 'Executed with FAILURE', 0)
        def script = runScript("Jenkinsfile")
        assertFalse(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            callArgsToString(call).contains("mvn clean package")
        })
        assertJobStatusFailure()
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
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            callArgsToString(call).contains("mvn clean package")
        })
        assertJobStatusFailure()
        printCallStack()
    }

    @Test
    void should_shell_execution_pass() throws Exception {
        helper.registerAllowedMethod("sh", [String.class], {cmd->
            if (cmd.contains("cp abc.txt cde.txt")) {
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })

        def script = runScript("Jenkinsfile")
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            callArgsToString(call).contains("mv abed.csv xyz.csv")
        })
        assertJobStatusFailure()
        printCallStack()
    }
}

