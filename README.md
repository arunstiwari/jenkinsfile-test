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
//        helper.scriptRoots += 'vars'
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
