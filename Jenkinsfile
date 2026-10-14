pipeline {
     agent {
            kubernetes {
                yaml '''
    apiVersion: v1
    kind: Pod
    metadata:
      labels:
        some-label: otus-test-runner
    '''
            }
        }

         options {
                timeout(time: 1, unit: 'HOURS')
                ansiColor('xterm')
            }

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Ветка репозитория')
        string(name: 'WIREMOCK_HOST', defaultValue: 'wiremock.wiremock-ns.svc.cluster.local', description: 'Хост для WireMock')
        string(name: 'WIREMOCK_PORT', defaultValue: '9021', description: 'Порт для WireMock')
    }

    tools {
        maven 'Maven 3.9.x' // Имя инсталляции Maven в Jenkins
        jdk 'Java 17'       // Имя инсталляции Java в Jenkins
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

//     post {
//         always {
//             // Архивируем отчеты о тестировании для визуализации в Jenkins
//             junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
//         }
//     }
}