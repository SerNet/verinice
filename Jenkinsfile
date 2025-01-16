def qualifier

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
        JAVA_HOME='/usr/lib/jvm/java-17-openjdk'
    }
    parameters {
        booleanParam(name: 'runIntegrationTests', defaultValue: true, description: 'Run integration tests')
        booleanParam(name: 'runRCPTTTests', defaultValue: true, description: 'Run RCPTT tests')
        booleanParam(name: 'clientSign', defaultValue: false, description: 'Sign verinice clients')
        booleanParam(name: 'dists', defaultValue: false, description: 'Run distribution steps, i.e. build RPMs files etc.')
        // We need an extra flag. Unfortunately it is not possible to find out, if a password is left empty.
        booleanParam(name: 'distSign', defaultValue: false, description: 'Sign RPM packages')
        password(name: 'GNUPGPASSPHRASE', description: 'The passphrase of the key stored in KEYDIR/verinice.public.')
        booleanParam(name: 'archiveUpdateSite', defaultValue: false, description: 'archive the update site')
        booleanParam(name: 'archiveIntegrationTestResults', defaultValue: false, description: 'archive the integration test results')
        booleanParam(name: 'generateSNCAReport', defaultValue: false, description: 'generate the snca report/validation with snca editor')
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
        gitLabConnection('sernet-gitlab')
    }
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, branchFilterType: 'All')
    }
    stages {
        stage('Setup') {
            steps {
                gitlabCommitStatus(name: 'Setup') {
                    script {
                        qualifier = "v${new Date().format('yyyyMMddHHmmss')}"
                        echo "Using build qualifier $qualifier"
                        if (params.distSign && !params.dists) {
                            def msg = 'You have to enable dists, if you want to sign packages.'
                            buildDescription msg
                            error msg
                        }
                        def targetPlatform = 'target-platform/target-platform.target'
                        def content = readFile(file: targetPlatform, encoding: 'UTF-8')
                        def repositoryLocations = content.findAll('location\\s*=\\s*"([^"]+)"'){it[1]}
                        if (env.TAG_NAME){
                            currentBuild.keepLog = true
                            def repositoriesOnBob = repositoryLocations.findAll{it =~ /\bbob\b/}
                            if (!repositoriesOnBob.isEmpty()){
                                error("Target platform uses repositories on bob: $repositoriesOnBob")
                            }
                        }
                        def httpRepositories = repositoryLocations.findAll{it =~ /http:/}.findAll{!(it =~ '^http://bob(-2023)?\\.')}
                        if (!httpRepositories.isEmpty()){
                            error("Target platform uses non-HTTPS repositories: $httpRepositories")
                        }
                    }
                    buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
                    sh "./verinice-distribution/build.sh QUALIFIER=${qualifier} clean"
                }
            }
        }
        stage('Build') {
            steps {
                gitlabCommitStatus(name: 'Build') {
                    script {
                        def buildTask = params.runIntegrationTests ? 'verify' : 'products'
                        sh "./verinice-distribution/build.sh QUALIFIER=${qualifier} ${buildTask}"
                        archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/products/*.zip,sernet.verinice.releng.server.product/target/*.war', fingerprint: true
                        if (params.archiveUpdateSite || params.dists){
                            archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/repository/**', fingerprint: true
                        }
                        if (params.runIntegrationTests){
                            junit allowEmptyResults: true, testResults: '**/build/reports/**/*.xml,**/target/surefire-reports/*.xml'
                            if (params.archiveIntegrationTestResults){
                                archiveArtifacts artifacts: '**/build/reports/**/*.xml,**/target/surefire-reports/*.xml'
                            }

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
                runStandaloneUITests tests: '*.test'
                
                // 3-tier test
                // the last parameter can be adjusted to control which tests are to be run
                // e.g. 'bp*.test'
                // run3TierUITests tests: '*.test'

                // report verinice en and de
                // triggerRCPTTBuild 'rcptt-all-report-tests'
                
                // product tests
                //product tests partially is broken see VN-2882
                // triggerRCPTTBuild 'rcptt-product-report-test'
                
                // performance tests
                // the last parameter can be adjusted to control which tests are to be run
                // e.g. 'bp_delete-import-performance.test'
                // triggerRCPTTBuild 'rcptt-client-performance-test', '*-performance.test'
                // triggerRCPTTBuild 'rcptt-server-performance-test', '*-performance.test'
                
                // test specific operating systems
                // runStandaloneUITests os: 'Linux', tests: '*.test'
                // runStandaloneUITests os: 'macOS current', tests: '*.test'
                // runStandaloneUITests os: 'macOS predecessor', tests: '*.test'
                // runStandaloneUITests os: 'Windows 10', tests: '*.test'
            }
        }
        stage('generate SNCA Report') {
            when { expression { params.generateSNCAReport } }
            steps {
                gitlabCommitStatus(name: 'generate SNCA Report') {
                    archiveArtifacts artifacts: 'sernet.gs.server/WebContent/WEB-INF/SNCA.xml,sernet.gs.server/WebContent/WEB-INF/snca-messages*.properties', fingerprint: true
                    build job: 'snca-report', wait: false, parameters: [
                                    string(name: 'job_to_copy_from', value: "${currentBuild.fullProjectName}"),
                                    string(name: 'build_to_copy_from', value: "<SpecificBuildSelector plugin=\"copyartifact@1.42.1\"><buildNumber>${env.BUILD_NUMBER}</buildNumber></SpecificBuildSelector>"),
                                    ]
                }
            }
        }
        stage('Documentation') {
            steps {
                gitlabCommitStatus(name: 'Documentation') {
                    sh "./verinice-distribution/build.sh QUALIFIER=${qualifier} -j4 docs"
                    archiveArtifacts artifacts: 'doc/manual/*/*.pdf,doc/manual/*/*.zip', fingerprint: true
                }
            }
        }
        stage('Sign clients') {
            when {
                expression { params.clientSign && currentBuild.result in [null, 'SUCCESS'] }
            }
            steps {
                gitlabCommitStatus(name: 'Sign clients') {
                    script {
                        def fileNameWindows = sh returnStdout: true, script: 'ls sernet.verinice.releng.client.product/target/products/*win32*zip'
                        def windowsClientUrl = input message: 'Supply URLS to client ZIPs with signed executables', parameters: [string(name: 'windows', description: 'URL to Windows Client ZIP')], submitter: 'dm'
                        sh "rm $fileNameWindows"
                        sh "curl $windowsClientUrl --output $fileNameWindows"
                        archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/products/*.zip', fingerprint: true
                    }
                }
            }
        }
        stage('Distributions') {
            when {
                expression { params.dists && currentBuild.result in [null, 'SUCCESS'] }
            }
            steps {
                gitlabCommitStatus(name: 'Distributions') {
                    sh "./verinice-distribution/build.sh QUALIFIER=${qualifier} -j2 dists"
                }
            }
        }
        // Signing is a separate step because we want to be able to build RPMs any time, to test them.
        // However RPMs are only archived if they are signed.
        stage('Distributions Signing') {
            when {
                expression { params.distSign && currentBuild.result in [null, 'SUCCESS'] }
            }
            steps {
                gitlabCommitStatus(name: 'Distributions Signing') {
                    sh "./verinice-distribution/sign-rpms verinice-distribution/rhel-?/RPMS/noarch/*"
                    archiveArtifacts artifacts: 'verinice-distribution/rhel-?/RPMS/noarch/*', fingerprint: true
                }
            }
        }
    }
    post {
        always {
            recordIssues(tools: [mavenConsole()])
            recordIssues(tools: [java()])
            recordIssues(tools: [taskScanner(highTags: 'FIXME', ignoreCase: true, normalTags: 'TODO', includePattern: '**/*.java, **/*.xml')])
        }
        success {
            sh './verinice-distribution/build.sh QUALIFIER=${qualifier} clean'
        }
    }
}

def run3TierUITests(Map opts = [:]){
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
    String tomcat = opts.tomcat
    if (tomcat != null){
        parameters << string(name: 'TOMCAT_VERSION', value: tomcat)// allow to choose the tomcat version
    }
    build job: jobName, wait: false, parameters: parameters
}
