pipeline {
  agent any


  stages {
    stage('Test') {
      steps {
        echo "Running unit tests..."
        // --no-daemon utile sur Jenkins
        bat './gradlew --no-daemon clean test'
      }
      post {
        always {
          // 1) Archivage des résultats des tests unitaires
          // (par défaut Gradle met JUnit XML dans build/test-results/test/*.xml)
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

          // 2) Archive HTML du rapport de tests unitaires (optionnel mais pratique)
          archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/tests/test/**'

          // 3) Génération + archive rapports Cucumber (si existants)
          // Beaucoup de setups mettent le report ici: build/reports/cucumber/**
          archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/reports/cucumber/**'

          // (Optionnel) si ton plugin cucumber sort un json ailleurs, archive aussi:
          archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/**/cucumber*.json, **/build/**/cucumber*.html'
        }
      }
    }
  }
}
