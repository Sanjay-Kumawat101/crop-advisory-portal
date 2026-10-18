pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
    }

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'test', 'prod'],
            description: 'Tomcat deployment environment'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Unit Tests') {
            steps {
                bat 'mvn -B clean package'
                archiveArtifacts artifacts: 'target/*.war'
            }
        }

        stage('UI Tests') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'ui-test-farmer',
                        usernameVariable: 'UI_FARMER_USERNAME',
                        passwordVariable: 'UI_FARMER_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: 'ui-test-expert',
                        usernameVariable: 'UI_EXPERT_USERNAME',
                        passwordVariable: 'UI_EXPERT_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: 'ui-test-admin',
                        usernameVariable: 'UI_ADMIN_USERNAME',
                        passwordVariable: 'UI_ADMIN_PASSWORD'
                    )
                ]) {
                    withEnv([
                        'APP_PORT=8085',
                        'APP_ENV=jenkins',
                        "DB_PATH=${env.WORKSPACE}\\target\\ui-test-data\\cropdb"
                    ]) {
                        bat 'mvn -B verify -Pui-tests -Dbase.url=http://localhost:8085'
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'tomcat-manager',
                    usernameVariable: 'TOMCAT_USERNAME',
                    passwordVariable: 'TOMCAT_PASSWORD'
                )]) {
                    bat 'call scripts\\deploy-tomcat.bat "%ENVIRONMENT%"'
                }
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml, target/failsafe-reports/*.xml'
            archiveArtifacts artifacts: 'target/screenshots/**', allowEmptyArchive: true
        }
    }
}
