pipeline {
  agent any

  environment {
    SONAR_SERVER  = 'SonarQubeLocal'     // Manage Jenkins -> System -> SonarQube servers
    TEAM_EMAIL    = 'dev-team@example.com'
    SLACK_CHANNEL = '#ci-cd'
  }

  stages {
    stage('Test') {
      steps {
        bat '.\\gradlew.bat --no-daemon clean test jacocoTestReport'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
          archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/**'
        }
      }
    }

    stage('Code Analysis') {
      steps {
        withSonarQubeEnv("${SONAR_SERVER}") {
          bat '.\\gradlew.bat --no-daemon sonar'
        }
      }
    }

    stage('Code Quality') {
      steps {
        timeout(time: 10, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Build') {
      steps {
        bat '.\\gradlew.bat --no-daemon jar javadoc'
      }
      post {
        always {
          archiveArtifacts artifacts: '**/build/libs/*.jar, **/build/docs/javadoc/**', allowEmptyArchive: false
        }
      }
    }

    stage('Deploy') {
      steps {
        bat """
          .\\gradlew.bat --no-daemon publish ^
            -PrepoUser=%MYMAVENREPO_USER% ^
            -PrepoPass=%MYMAVENREPO_PASS%
        """
      }
    }
  }

  post {
    success {
      emailext(
        to: "${TEAM_EMAIL}",
        subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: "Deployed OK\\n${env.BUILD_URL}"
      )
      slackSend channel: "${SLACK_CHANNEL}",
        message: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} — ${env.BUILD_URL}"
    }

    failure {
      emailext(
        to: "${TEAM_EMAIL}",
        subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        body: "Pipeline failed\\n${env.BUILD_URL}"
      )
      slackSend channel: "${SLACK_CHANNEL}",
        message: "❌ FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER} — ${env.BUILD_URL}"
    }
  }
}
