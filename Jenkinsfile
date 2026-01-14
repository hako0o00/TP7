pipeline {
  agent any

  environment {
    // SonarQube server name - should match the installation name configured in Jenkins
    // To find the correct name: Manage Jenkins -> System -> SonarQube servers
    // If not set, will default to 'SonarQube' (common default name)
    // You can override by setting SONAR_SERVER_NAME in Jenkins job configuration
    SONAR_SERVER = "${env.SONAR_SERVER_NAME ?: 'SonarQube'}"
    GRADLE_OPTS = '-Xmx1024m -XX:MaxMetaspaceSize=512m'
  }

  stages {
    stage('Test') {
      steps {
        // 1) Lancement tests unitaires (+ report Cucumber si ta config le produit pendant test)
        bat '.\\gradlew.bat --no-daemon clean test'
      }
      post {
        always {
          // 2) Archivage résultats unitaires (JUnit XML)
          junit testResults: '**/build/test-results/test/*.xml', allowEmptyResults: false

          // 3) Rapport Cucumber (publie dans Jenkins) + archive les fichiers
          cucumber fileIncludePattern: '**/build/**/cucumber*.json', buildStatus: 'UNSTABLE'
          archiveArtifacts artifacts: '**/build/**/cucumber*.json, **/build/reports/**', allowEmptyArchive: true
        }
      }
    }

    stage('Code Analysis') {
      steps {
        script {
          // Use the configured SonarQube installation
          // IMPORTANT: The name must EXACTLY match the installation name in Jenkins
          // To find it: Manage Jenkins -> System -> SonarQube servers -> check the "Name" field
          // You can set SONAR_SERVER_NAME in Jenkins job configuration to override
          def sonarServer = env.SONAR_SERVER_NAME ?: 'SonarQube'
          echo "Attempting to use SonarQube server: ${sonarServer}"
          echo "If this fails, check the exact name in: Manage Jenkins -> System -> SonarQube servers"
          try {
            withSonarQubeEnv("${sonarServer}") {
              bat '.\\gradlew.bat --no-daemon sonar'
            }
          } catch (Exception e) {
            error("SonarQube connection failed. Please verify the installation name matches exactly. " +
                  "Current name: '${sonarServer}'. " +
                  "Check: Manage Jenkins -> System -> SonarQube servers")
          }
        }
      }
    }

    stage('Code Quality') {
      steps {
        script {
          // Wait for SonarQube Quality Gate
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
          // 1) Génération du fichier Jar
          bat '.\\gradlew.bat --no-daemon jar'
          
          // 2) Génération de la documentation
          bat '.\\gradlew.bat --no-daemon javadoc'
        }
      }
      post {
        always {
          // 3) Archivage du fichier Jar et de la documentation
          archiveArtifacts artifacts: 'build/libs/*.jar, build/docs/javadoc/**', allowEmptyArchive: true
        }
      }
    }

    stage('Deploy') {
      steps {
        script {
          // Déploiement vers MyMavenRepo
          bat '.\\gradlew.bat --no-daemon publish'
        }
      }
    }
  }

  post {
    success {
      script {
        // Notification de succès par email et Slack
        def recipients = env.DEFAULT_RECIPIENTS ?: ''
        if (recipients) {
          emailext (
            subject: "SUCCESS: Pipeline '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
            body: "Le pipeline s'est terminé avec succès.\n\nConsultez: ${env.BUILD_URL}",
            to: recipients
          )
        }
        // Slack notification (requires Slack plugin)
        // slackSend(
        //   color: 'good',
        //   message: "Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} réussi avec succès!"
        // )
      }
    }
    failure {
      script {
        // Notification d'échec par email et Slack
        def recipients = env.DEFAULT_RECIPIENTS ?: ''
        if (recipients) {
          emailext (
            subject: "FAILED: Pipeline '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
            body: "Le pipeline a échoué.\n\nConsultez: ${env.BUILD_URL}",
            to: recipients
          )
        }
        // Slack notification (requires Slack plugin)
        // slackSend(
        //   color: 'danger',
        //   message: "Pipeline ${env.JOB_NAME} #${env.BUILD_NUMBER} a échoué!"
        // )
      }
    }
  }
}
