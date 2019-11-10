/**
 *
 * The MIT License
 *
 * Copyright 2006-2011 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import java.io.*
import java.util.*

t = new IntegrationBase();

/**
 * This will filter out the version of the
 * appassembler-maven-plugin whcih is configured
 * within the integration test.
 * @return Version information.
 */
def getProjectVersion() {
	def pom = new XmlSlurper().parse(new File(basedir, 'pom.xml'))

	def allPlugins = pom.build.plugins

	def dependencies = allPlugins.plugin

	def appassemblerModule = dependencies.find { item ->
		item.groupId.equals("org.codehaus.mojo") && item.artifactId.equals("appassembler-maven-plugin");
	}

	return appassemblerModule.version;
}

def projectVersion = getProjectVersion();

println "ProjectVersion:" + projectVersion

def buildLogFile = new File( basedir, "build.log");

//All Maven 3.X versions
if (mavenVersion.startsWith("3.")) {
	t.checkExistenceAndContentOfAFile(buildLogFile, [
    '[ERROR] Failed to execute goal org.codehaus.mojo:appassembler-maven-plugin:' + projectVersion + ':assemble (default) on project programNameDuplicate-test: The program id: program-01-test exists more than once! -> [Help 1]',
	]);
} else {
  //Maven 2.X output looks different...
	t.checkExistenceAndContentOfAFile(buildLogFile, [
		'org.apache.maven.BuildFailureException: The program id: program-01-test exists more than once!',
	]);
}

return true;
