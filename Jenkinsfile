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

        stage('Build') {
            steps {
                bat 'mvn -B clean package -DskipTests'
            }
        }

        stage('Package') {
            steps {
                archiveArtifacts artifacts: 'target/*.war'
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
}
