pipeline {
    agent { label 'java' }

    options {
        timeout(time: 1, unit: 'HOURS')
    }

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Ветка репозитория')
        string(name: 'WIREMOCK_HOST', defaultValue: 'wiremock.wiremock-ns.svc.cluster.local', description: 'Хост для WireMock')
        string(name: 'WIREMOCK_PORT', defaultValue: '9021', description: 'Порт для WireMock')
    }

    stages {
        stage('WireMock Tests') {
            steps {
                container('maven') {
                    withEnv([
                        "WIREMOCK_HOST=${params.WIREMOCK_HOST}",
                        "WIREMOCK_PORT=${params.WIREMOCK_PORT}"
                    ]) {
                        sh 'mvn clean test'
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            allure ([results: [[path:'target/allure-results']]])
        }
    }
}