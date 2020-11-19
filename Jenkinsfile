pipeline {
    agent {
        dockerfile {
            dir 'verinice-distribution/docker-verinice-builder'
            args '-v $MAVEN_REPOSITORY_BASE/repository$EXECUTOR_NUMBER:/m2/repository -v $HOME/.cache/verinicebuild:/cache/verinicebuild'
        }
    }
    environment {
        // In case the build server exports a custom JAVA_HOME, we fix the JAVA_HOME
        // to the one used by the docker image.
        JAVA_HOME='/usr/lib/jvm/java-1.8.0-openjdk'
    }
    parameters {
        string(name: 'jreversion', defaultValue: 'jdk8u272-b10', description: 'Download and pack a JRE with this version. See https://adoptopenjdk.net/archive.html for a list of possible versions.', trim: true)
        booleanParam(name: 'runIntegrationTests', defaultValue: true, description: 'Run integration tests')
        booleanParam(name: 'runRCPTTTests', defaultValue: true, description: 'Run RCPTT tests')
        booleanParam(name: 'dists', defaultValue: false, description: 'Run distribution steps, i.e. build RPMs files etc.')
        // We need an extra flag. Unfortunately it is not possible to find out, if a password is left empty.
        booleanParam(name: 'distSign', defaultValue: false, description: 'Sign RPM packages')
        password(name: 'GNUPGPASSPHRASE', description: 'The passphrase of the key stored in KEYDIR/verinice.public.')
        booleanParam(name: 'archiveUpdateSite', defaultValue: false, description: 'archive the update site')
        booleanParam(name: 'archiveIntegrationTestResults', defaultValue: false, description: 'archive the integration test results')
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    stages {
        stage('Setup') {
            steps {
                script {
                    if (params.distSign && !params.dists) {
                        def msg = 'You have to enable dists, if you want to sign packages.'
                        buildDescription msg
                        error msg
                    }
                    if (env.TAG_NAME){
                        currentBuild.keepLog = true
                        def targetPlatform = 'target-platform/target-platform.target'
                        def content = readFile(file: targetPlatform, encoding: 'UTF-8')
                        def repositoryLocations = content.findAll('location\\s*=\\s*"([^"]+)"'){it[1]}
                        def repositoriesOnBob = repositoryLocations.findAll{it =~ /\bbob\b/}
                        if (!repositoriesOnBob.isEmpty()){
                            error("Target platform uses repositories on bob: $repositoriesOnBob")
                        }
                    }
                }
                buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
                sh './verinice-distribution/build.sh clean'
            }
        }
        stage('Fetch JREs') {
            steps {
                sh "./verinice-distribution/build.sh JREVERSION=${params.jreversion} -j4 jres"
            }
        }
        stage('Build') {
            steps {
                script {
                    def buildTask = params.runIntegrationTests ? 'verify' : 'products'
	                sh "./verinice-distribution/build.sh ${buildTask}"
	                archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/products/*.zip,sernet.verinice.releng.server.product/target/*.war', fingerprint: true
	                if (params.archiveUpdateSite || params.dists){
	                    archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/repository/**', fingerprint: true
	                }
	                if (params.runIntegrationTests){
		                junit allowEmptyResults: true, testResults: '**/build/reports/**/*.xml,**/target/surefire-reports/*.xml'
		                perfReport filterRegex: '', sourceDataFiles: '**/build/reports/TEST*.xml,**/target/surefire-reports/*.xml'
		                if (params.archiveIntegrationTestResults){
		                    archiveArtifacts artifacts: '**/build/reports/**/*.xml,**/target/surefire-reports/*.xml'
		                }

	                }
                }
            }
        }
        stage('Trigger RCPTT') {
            when { expression { params.runRCPTTTests } }
            steps {
                // normal client rcptt
                // the last parameter can be adjusted to control which tests are to be run
                // e.g. 'bp*.test'
                //runStandaloneUITests tests: '*.test'
                
                // 3-tier test
                // the last parameter can be adjusted to control which tests are to be run
                // e.g. 'bp*.test'
                run3TiertUITests tests: 'bp_editing.test'

                // report verinice en and de
                // triggerRCPTTBuild 'rcptt-all-report-tests'
                
                // product tests
                // triggerRCPTTBuild 'rcptt-product-report-test'
                
                // performance tests
                // the last parameter can be adjusted to control which tests are to be run
                // e.g. 'bp_delete-import-performance.test'
                // triggerRCPTTBuild 'rcptt-client-performance-test', '*-performance.test'
                // triggerRCPTTBuild 'rcptt-server-performance-test', '*-performance.test'
                
                // test specific operating systems
                // runStandaloneUITests os: 'Linux', tests: '*.test'
                // runStandaloneUITests os: 'Mac OSX', tests: '*.test'
                // runStandaloneUITests os: 'Windows 10', tests: '*.test'
            }
        }
        stage('Documentation') {
            steps {
                sh "./verinice-distribution/build.sh -j4 docs"
                archiveArtifacts artifacts: 'doc/manual/*/*.pdf,doc/manual/*/*.zip', fingerprint: true
            }
        }
        stage('Distributions') {
            when {
                expression { params.dists && currentBuild.result in [null, 'SUCCESS'] }
            }
            steps {
                sh "./verinice-distribution/build.sh -j2 dists"
            }
        }
        // Signing is a separate step because we want to be able to build RPMs any time, to test them.
        // However RPMs are only archived if they are signed.
        stage('Distributions Signing') {
            when {
                expression { params.distSign && currentBuild.result in [null, 'SUCCESS'] }
            }
            steps {
                sh "./verinice-distribution/sign-rpms verinice-distribution/rhel-?/RPMS/noarch/*"
                archiveArtifacts artifacts: 'verinice-distribution/rhel-?/RPMS/noarch/*', fingerprint: true
            }
        }
    }
    post {
        always {
            recordIssues(tools: [mavenConsole()])
            recordIssues(tools: [java()])
            recordIssues(tools: [taskScanner(highTags: 'FIXME', ignoreCase: true, normalTags: 'TODO', includePattern: '**/*.java, **/*.xml')])
        }
        failure {
            emailext body: '${JELLY_SCRIPT,template="text"}', subject: '$DEFAULT_SUBJECT', to: 'dm@sernet.de, uz@sernet.de, an@sernet.de, fw@sernet.de, ak@sernet.de'
        }
        success {
            sh './verinice-distribution/build.sh clean'
        }
    }
}

def run3TiertUITests(Map opts = [:]){
    triggerRCPTTBuild('rcptt-server-test', opts)
}

def runStandaloneUITests(Map opts = [:]){
    triggerRCPTTBuild('rcptt-client-test', opts)
}

def triggerRCPTTBuild(String jobName, String testList){
    triggerRCPTTBuild(jobName, [tests: testList])
}

def triggerRCPTTBuild(String jobName, Map opts = [:]){
	def parameters = [
        gitParameter(name: 'BRANCH_OR_TAG', value: "${env.GIT_BRANCH}"),
        string(name: 'job_to_copy_from', value: "${currentBuild.fullProjectName}"),
        string(name: 'build_to_copy_from', value: "<SpecificBuildSelector plugin=\"copyartifact@1.42.1\"><buildNumber>${env.BUILD_NUMBER}</buildNumber></SpecificBuildSelector>"),
        string(name: 'OPERATING_SYSTEM', value: opts.os ?: 'any')
    ]
    String testList = opts.tests
    if (testList != null){
        parameters << string(name: 'test-list', value: testList)// support the old jobs
        parameters << string(name: 'TEST_LIST', value: testList)
    }
    build job: jobName, wait: false, parameters: parameters
}
