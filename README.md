### 1. Add the following dependency
```xml
<dependency>
    <groupId>com.lesfurets</groupId>
    <artifactId>jenkins-pipeline-unit</artifactId>
    <version>1.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13</version>
    <scope>test</scope>
</dependency>

```
### 2. To test the Jenkins 'pipeline' we have to create test class that extends DeclarativePipelineTest as shown in example
```groovy
package com.tekmentors.jenkins;

import com.lesfurets.jenkins.unit.declarative.DeclarativePipelineTest
import org.junit.Before;
import org.junit.Test

public class TestExampleDeclarativeJob extends DeclarativePipelineTest {

    @Before
    void setUp() throws Exception {
        super.setUp()
    }
    @Test
    void should_execute_without_errors() throws Exception {
        def script = runScript("Jenkinsfile")
        assertJobStatusSuccess()
        printCallStack()
    }
}
```
+ It looks for pipeline scripts in your project in root (./.) and src/main/jenkins paths.
+ Jenkins pipelines let you load other scritps in parent script with `load` command
+ `load` command takes full path relative to the project root.
+ To make the relative path work, you need to configure the path of the project where your pipeline scripts are which defaults to `..`
+ Overriding these defaults are shown below
```groovy
class TestExampleJob extends BasePipelineTest {

    @Override
    @Before
    void setUp() throws Exception {
        baseScriptRoot = 'jenkinsJobs'
        scriptRoots += 'src/main/groovy'
        scriptExtension = 'pipeline'
        super.setUp()
    }

}
```

```text
jenkinsJobs
 └── src
     ├── main
     │   └── groovy
     │       └── ExampleJob.pipeline
     └── test
         └── groovy
             └── TestExampleJob.groovy
```

### 3. Let us write a test to test the following stage in Jenkinsfile
```text
stage('Static Code Analysis'){
  steps {
     echo 'Executing Static Code Analysis'
     int status = sh(returnStatus: true, script: './build.sh --release')
     if(status > 0) {
        currentBuild.result = 'UNSTABLE'
     }else {
        def result = sh(returnStdout: true, script: './processTestResults.sh --platform debian')
         echo "result: ${result}"
         if (!result.endsWith('SUCCESS')) {
             currentBuild.result = 'FAILURE'
             error 'Build failed!'
         }
     }

 }
 post {
     success {
         echo 'Static Code analysis completed successfully'
     }
     failure {
        echo 'Static Code analysis failed'
     }
     unstable {
        echo 'Static Code build is unstable'
     }
  }
}
```
+ Test will be written something like this
```groovy
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

```

### 4. Let us write a test for stage which is defined as follows
```text
stage('Packaging'){
    steps {
       echo 'Packaging it as a jar'
       sh '''
        mvn clean package
       '''
   }
   post {
       success {
           echo 'Packaging as jar was done successfully'
       }
       failure {
           echo 'Packaging failed'
       }
     }
  }
```

+ Test will be written like something below
```groovy
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
```