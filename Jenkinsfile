pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
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

        stage('Docker Build') {
            steps {
                script {
                    def shortCommit = bat(returnStdout: true, script: '@git rev-parse --short=7 HEAD').trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${shortCommit}"
                }
                bat 'docker build --tag localhost:5000/crop-advisory:%IMAGE_TAG% --tag localhost:5000/crop-advisory:latest .'
            }
        }

        stage('Docker Push') {
            steps {
                bat 'docker push localhost:5000/crop-advisory:%IMAGE_TAG%'
                bat 'docker push localhost:5000/crop-advisory:latest'
            }
        }

        stage('Docker Deploy') {
            steps {
                bat 'docker rm -f crop-advisory || exit /b 0'
                bat 'docker run -d --name crop-advisory -p 8082:8080 -v crop-data:/data --restart unless-stopped localhost:5000/crop-advisory:%IMAGE_TAG%'
                bat '''powershell -NoProfile -Command "$deadline=(Get-Date).AddSeconds(60); while ((Get-Date) -lt $deadline) { try { $health=Invoke-RestMethod -Uri 'http://127.0.0.1:8082/actuator/health' -TimeoutSec 3; if ($health.status -eq 'UP') { Write-Output 'Docker deployment health check passed.'; exit 0 } } catch { }; Start-Sleep -Seconds 2 }; Write-Error 'Docker deployment health check failed.'; exit 1"'''
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
