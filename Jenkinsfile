pipeline {
  agent any

  environment {
    SONAR_SERVER = 'SonarQubeLocal'
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
  }
}
