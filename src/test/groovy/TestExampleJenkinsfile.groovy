import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test;

class TestExampleJenkinsfile extends BasePipelineTest {
    @Before
    void setUp() throws Exception {
//        helper.scriptRoots += 'vars'
//        helper.registerAllowedMethod("sh", [String.class], {cmd -> "mvn clean package"})
        super.setUp()
    }

    @Test
    void debianBuildSuccess() {
        helper.addShMock('uname', 'Debian', 0)
        helper.addShMock('./build.sh --release', '', 0)
        helper.addShMock('./test.sh', '', 0)
        helper.addShMock('./processTestResults.sh --platform debian', 'Executing with SUCCESS', 0)
        // Have the sh mock execute the closure when the corresponding script is run
//        helper.addShMock('./processTestResults.sh --platform debian', { script ->
//            return "Executing ${script}: SUCCESS"
//        })

        runScript("Jenkinsfile1")

        assertJobStatusSuccess()
    }
}
