pipeline {
    agent any
    parameters {
        booleanParam(name: 'dists', defaultValue: false, description: 'Run distribution steps, i.e. build RPMs files etc.')
    }
    environment {
        MAVEN_OPTS = "-Dmaven.repo.local=${env.MAVEN_REPOSITORY_BASE}/repository${env.EXECUTOR_NUMBER}"
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }
    stages {
        stage('Setup') {
            steps {
                script {
                    if (env.TAG_NAME){
                        currentBuild.keepLog = true
                    }
                }
                buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
                sh 'env'
                sh './verinice-distribution/build.sh clean'
            }
        }
        stage('Fetch JREs') {
            steps {
                sh './verinice-distribution/build.sh -j4 jres'
            }
        }
        stage('Build') {
            steps {
                sh "./verinice-distribution/build.sh products"
                archiveArtifacts artifacts: 'sernet.verinice.releng.client.product/target/products/*.zip,sernet.verinice.report.designer.product/target/products/*.zip,sernet.verinice.releng.server.product/target/*.war,sernet.verinice.releng.client.product/target/repository/**', fingerprint: true
            }
        }
        stage('Trigger RCPTT') {
            steps {
                build job: 'verinice-client-rcptt', wait: false, parameters: [gitParameter(name: 'BRANCH_OR_TAG', value: "${env.GIT_BRANCH}"), string(name: 'artifact_selector', value: 'sernet.verinice.releng.client.product/target/products/*linux.gtk.x86_64*.zip'), string(name: 'job_to_copy_from', value: "${currentBuild.fullProjectName}"), string(name: 'build_to_copy_from', value: '<TriggeredBuildSelector plugin="copyartifact@1.42.1">  <upstreamFilterStrategy>UseGlobalSetting</upstreamFilterStrategy>  <allowUpstreamDependencies>false</allowUpstreamDependencies></TriggeredBuildSelector>')]
            }
        }
        stage('Test') {
            steps {
                sh "./verinice-distribution/build.sh tests"
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
                archiveArtifacts artifacts: 'verinice-distribution/rhel-?/RPMS/noarch/*', fingerprint: true
            }
        }
    }
    post {
        always {
            recordIssues(tools: [mavenConsole()])
            recordIssues(tools: [java()])
            recordIssues(tools: [taskScanner(highTags: 'FIXME', ignoreCase: true, normalTags: 'TODO', includePattern: '**/*.java, **/*.xml')])
            junit allowEmptyResults: true, testResults: '**/build/reports/**/*.xml'
            perfReport filterRegex: '', sourceDataFiles: '**/build/reports/TEST*.xml'
        }
        failure {
            emailext body: '${JELLY_SCRIPT,template="text"}', subject: '$DEFAULT_SUBJECT', to: 'dm@sernet.de, uz@sernet.de, an@sernet.de, fw@sernet.de, ak@sernet.de'
        }
    }
}
