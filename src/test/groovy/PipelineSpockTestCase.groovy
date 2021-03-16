import com.lesfurets.jenkins.unit.PipelineTestHelper
import com.lesfurets.jenkins.unit.RegressionTest
import spock.lang.Specification

class PipelineSpockTestCase  extends Specification implements  RegressionTest{
    @Delegate PipelineTestHelper pipelineTestHelper

    def setup(){
        callStackPath = 'pipelineTests/groovy/tests/callstacks/'

        // create and config the helper
        pipelineTestHelper = new PipelineTestHelper();
        pipelineTestHelper.setUp()
    }
}
