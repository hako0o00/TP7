pipeline {
  agent any

  environment {
    SONAR_SERVER = 'SonarQubeLocal'
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
        withSonarQubeEnv("${SONAR_SERVER}") {
          bat '.\\gradlew.bat --no-daemon sonar'
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
