import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before;
import org.junit.Test
import static com.lesfurets.jenkins.unit.MethodCall.callArgsToString
import static org.junit.Assert.*

class TestJenkinsfileCP extends DeclarativePipelineTest{

    @Before
    void setUp() throws Exception {
        super.setUp()
        def map = [CONTRACT_MODELER_BRANCH: "v23456"]
        binding.setVariable('params',map)
        helper.registerAllowedMethod("fileOperations", [ArrayList.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("folderCreateOperation", [String.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("folderDeleteOperation", [String.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("folderCopyOperation", [Map.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("withAnt", [Closure.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("junit", [Object.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("fileCopyOperation", [Map.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("recordIssues", [Object.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("spotBugs", [Map.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("tool", [String.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("withSonarQubeEnv", [String.class, Closure.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("copyArtifacts", [Object.class], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        helper.registerAllowedMethod("lastCompleted", [], {cmd->
            binding.getVariable('currentBuild').result = 'SUCCESS'
        })
        binding.setVariable("BRANCH","master")
    }

    @Test
    void should_execute_successfully() throws  Exception {
        runScript('Jenkinsfile-CP')
        assertJobStatusSuccess()
        printCallStack()
    }
}
