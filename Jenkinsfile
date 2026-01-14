pipeline {
  agent any

  environment {
    SONAR_SERVER = "${env.SONAR_SERVER_NAME ?: 'SonarQube'}"
    GRADLE_OPTS = '-Xmx1024m -XX:MaxMetaspaceSize=512m'
  }

  stages {
    stage('Test') {
      steps {
        bat '.\\gradlew.bat --no-daemon clean test'
      }
      post {
        always {
          junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: false
          cucumber fileIncludePattern: '**/build/**/cucumber*.json', buildStatus: 'UNSTABLE'
          archiveArtifacts artifacts: '**/build/**/cucumber*.json, **/build/reports/**', allowEmptyArchive: true
        }
      }
    }

    stage('Code Analysis') {
      steps {
        script {
          def sonarServer = env.SONAR_SERVER_NAME ?: 'Sonar'
          withSonarQubeEnv("${sonarServer}") {
            bat '.\\gradlew.bat --no-daemon sonar'
          }
        }
      }
    }

    stage('Code Quality') {
      steps {
        script {
          timeout(time: 5, unit: 'MINUTES') {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
              error "Quality Gate failed: ${qg.status}"
            }
          }
        }
      }
    }

    stage('Build') {
      steps {
        script {
          bat '.\\gradlew.bat --no-daemon jar'
          bat '.\\gradlew.bat --no-daemon javadoc'
        }
      }
      post {
        always {
          archiveArtifacts artifacts: 'build/libs/*.jar, build/docs/javadoc/**', allowEmptyArchive: true
        }
      }
    }

    stage('Deploy') {
      steps {
        script {
          bat '.\\gradlew.bat --no-daemon publish'
        }
      }
    }
  }

  post {
    success {
      script {
        def recipients = 'inima.a04@gmail.com'
        def branchName = env.BRANCH_NAME ?: 'main'

        def emailBody = "✅ Déploiement réussi\n${env.JOB_NAME} #${env.BUILD_NUMBER} - ${branchName}\n${env.BUILD_URL}"

        try {
          emailext(
            subject: "✅ Déploiement Réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: emailBody,
            to: recipients
          )
        } catch (Exception e) {
          echo "Erreur email: ${e.getMessage()}"
        }

        def slackText = "✅ Déploiement réussi\n${env.JOB_NAME} #${env.BUILD_NUMBER} - ${branchName}\n${env.BUILD_URL}"
        def slackJson = groovy.json.JsonOutput.toJson([text: slackText])

        try {
          withCredentials([string(credentialsId: 'slack_hook', variable: 'SLACK_WEBHOOK_URL')]) {
            bat """
              curl -X POST -H "Content-type: application/json" --data "${slackJson}" "%SLACK_WEBHOOK_URL%"
            """
          }
        } catch (Exception e) {
          echo "Erreur Slack: ${e.getMessage()}"
        }
      }
    }

    failure {
      script {
        def recipients = 'inima.a04@gmail.com'
        def branchName = env.BRANCH_NAME ?: 'main'

        def emailBody = "❌ Pipeline échoué\n${env.JOB_NAME} #${env.BUILD_NUMBER} - ${branchName}\n${env.BUILD_URL}console"

        try {
          emailext(
            subject: "❌ Pipeline Échoué - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: emailBody,
            to: recipients
          )
        } catch (Exception e) {
          echo "Erreur email: ${e.getMessage()}"
        }

        def slackText = "❌ Pipeline échoué\n${env.JOB_NAME} #${env.BUILD_NUMBER} - ${branchName}\n${env.BUILD_URL}console"
        def slackJson = groovy.json.JsonOutput.toJson([text: slackText])

        try {
          withCredentials([string(credentialsId: 'slack_hook', variable: 'SLACK_WEBHOOK_URL')]) {
            bat """
              curl -X POST -H "Content-type: application/json" --data "${slackJson}" "%SLACK_WEBHOOK_URL%"
            """
          }
        } catch (Exception e) {
          echo "Erreur Slack: ${e.getMessage()}"
        }
      }
    }

    always {
      script {
        echo "Pipeline terminé. Statut: ${currentBuild.currentResult}"
      }
    }
  }
}
