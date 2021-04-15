import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before
import org.junit.Test
import static org.hamcrest.MatcherAssert.*;

import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

public class Jenkinsfile2Test extends DeclarativePipelineTest {

    def map
    @Before
    void setUp() throws Exception {
        super.setUp()
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'N']
        binding.setVariable('params',map)
        binding.setVariable("BRANCH", "Dev32")
        binding.setVariable("APPLICATION_POM_LOCATION", ".")
        binding.setVariable("GIT_CODEBASE_URL", "https://github.com/xyz/abc.git")
        binding.setVariable("BRANCH", "v1234")

//        binding.setVariable("ARTIFACT_CHECKING", "ABC")
    }

    @Test
    void checkout_is_successfull() throws Exception {
        binding.setVariable("GIT_CODEBASE_URL", "https://github.com/xyz/abc.git")
        binding.setVariable("BRANCH", "v1234")
        def script = runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            def BRANCH  = "v1234"
            def GIT_CODEBASE_URL = "https://github.com/abc.git"
            callArgsToString(call).contains("git clone -b ")
        })
        // Validate that sh is only called once
        assertEquals(1, helper.methodCallCount('sh'))
        assertCallStack().contains("Skipping stage Run unit test")
        assertCallStack().contains("Skipping stage Run Sonar, Push Stats and Validation")
        assertCallStack().contains("Skipping stage Run OWASP dependency Check analysis")
        assertCallStack().contains("Skipping stage Push to nexus")
        assertJobStatusSuccess()
    }

    @Test
    void skip_other_stages_if_checkout_is_unsuccessfull() throws Exception {
        binding.setVariable("GIT_CODEBASE_URL", "https://github.com/xyz/abc.git")
        binding.setVariable("BRANCH", "v1234")
        helper.registerAllowedMethod("sh", [String.class], {cmd->
            if (cmd.contains("git clone -b")) {
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("git clone -b")
        })
        assertCallStack().contains("Run unit test\" skipped due to earlier failure(s)")
        assertCallStack().contains("Run Sonar, Push Stats and Validation\" skipped due to earlier failure(s)")
        assertCallStack().contains("Run OWASP dependency Check analysis\" skipped due to earlier failure(s)")
        assertCallStack().contains("Publish artifacts\" skipped due to earlier failure(s)")
        assertCallStack().contains("Push to nexus\" skipped due to earlier failure(s)")
        assertJobStatusFailure()
    }

    @Test
    void run_unit_test_successfully() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y']
        binding.setVariable('params',map)
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -s settings.xml -f codebase/. clean test")
        })
        assertCallStack().contains("Skipping stage Run Sonar, Push Stats and Validation")
        assertCallStack().contains("Skipping stage Run OWASP dependency Check analysis")
        assertCallStack().contains("Skipping stage Push to nexus")
        assertJobStatusSuccess()
    }

    @Test
    void skip_other_stages_if_test_is_unsuccessfull() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y']
        binding.setVariable('params',map)
        helper.registerAllowedMethod("sh" , [String.class] , {cmd ->
            if (cmd.contains("mvn -s settings.xml -f codebase/. clean test")){
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -s settings.xml -f codebase/. clean test")
        })
        assertCallStack().contains("Run Sonar, Push Stats and Validation\" skipped due to earlier failure(s)")
        assertCallStack().contains("Run OWASP dependency Check analysis\" skipped due to earlier failure(s)")
        assertCallStack().contains("Publish artifacts\" skipped due to earlier failure(s)")
        assertCallStack().contains("Push to nexus\" skipped due to earlier failure(s)")
        assertJobStatusFailure()
    }

    @Test
    void run_sonar_stage_successfully() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y', Run_Sonar_and_push_stats: 'Y']
        binding.setVariable('params',map)
        helper.registerAllowedMethod("sh" , [String.class] , {cmd ->
            if (cmd.contains("mvn -s settings.xml -f codebase/. clean test cobertura:cobertura -Dcobertura.report.format=xml -Dsonar.scm.disabled=true sonar:sonar")){
                binding.getVariable('currentBuild').result = 'SUCCESS'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -s settings.xml -f codebase/. clean test cobertura:cobertura -Dcobertura.report.format=xml -Dsonar.scm.disabled=true sonar:sonar")
        })
        assertCallStack().contains("Executing multiple steps")
        assertCallStack().contains("Skipping stage Run OWASP dependency Check analysis")
        assertCallStack().contains("Skipping stage Push to nexus")
        assertJobStatusSuccess()
        printCallStack()
    }

    @Test
    void skip_other_stages_if_sonar_stage_is_unsuccessfull() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y', Run_Sonar_and_push_stats: 'Y']
        binding.setVariable('params',map)
        helper.registerAllowedMethod("sh" , [String.class] , {cmd ->
            if (cmd.contains("mvn -s settings.xml -f codebase/. clean test cobertura:cobertura -Dcobertura.report.format=xml -Dsonar.scm.disabled=true sonar:sonar")){
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -s settings.xml -f codebase/. clean test cobertura:cobertura -Dcobertura.report.format=xml -Dsonar.scm.disabled=true sonar:sonar")
        })
        assertCallStack().contains("Sonar check failed, please look for the reason above")
        assertCallStack().contains("Run OWASP dependency Check analysis\" skipped due to earlier failure(s)")
        assertCallStack().contains("Publish artifacts\" skipped due to earlier failure(s)")
        assertCallStack().contains("Push to nexus\" skipped due to earlier failure(s)")
       assertJobStatusFailure()
//        printCallStack()
    }

    @Test
    void owasp_dependency_check_stage_is_successfull() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y', Run_Sonar_and_push_stats: 'Y', RUN_OWASP_DEPENDENCY_CHECK: 'Y']
        binding.setVariable('params',map)
        helper.registerAllowedMethod("sh" , [String.class] , {cmd ->
            if (cmd.contains("mvn -B -s settings.xml -f codebase/. clean install")){
                binding.getVariable('currentBuild').result = 'SUCCESS'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -B -s settings.xml -f codebase/. clean install")
        })
        assertCallStack().contains("Skipping stage Push to nexus")
        assertJobStatusSuccess()
        long count = helper.callStack.stream()
                .filter { c -> c.methodName ==~ /archiveArtifacts/  }
                .count();
        assertEquals(count, 1)
        assertCallStack().contains("Skipping stage Push to nexus")
    }

    @Test
    void skip_other_stages_if_owasp_dependency_check_stage_is_unsuccessfull() throws Exception {
        map = [BRANCH: "v23456", ARTIFACT_CHECKING: "ABC", Run_Unit_Test: 'Y', Run_Sonar_and_push_stats: 'Y',RUN_OWASP_DEPENDENCY_CHECK: 'Y']
        binding.setVariable('params',map)
        helper.registerAllowedMethod("sh" , [String.class] , {cmd ->
            if (cmd.contains("mvn -B -s settings.xml -f codebase/. clean install")){
                binding.getVariable('currentBuild').result = 'FAILURE'
            }
        })
        runScript("Jenkinsfile2")
        assertEquals("Branch Name is Dev","v1234",binding.getVariable("BRANCH"))
        helper.callStack.stream().forEach({ c ->
            println(c)
        })
        assertTrue(helper.callStack.findAll { call ->
            call.methodName == "sh"
        }.any { call ->
            println("-----begin-----${call}")
            callArgsToString(call).contains("mvn -B -s settings.xml -f codebase/. clean install")
        })
        assertCallStack().contains("Publish artifacts\" skipped due to earlier failure(s)")
        assertCallStack().contains("Push to nexus\" skipped due to earlier failure(s)")
        assertJobStatusFailure()
//        printCallStack()
    }
}