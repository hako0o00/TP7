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
          // Use      the configured SonarQube installation
          // IMPORTANT: The name must EXACTLY match the installation name in Jenkins
          // To find it: Manage Jenkins -> System -> SonarQube servers -> check the "Name" field
          // You can set SONAR_SERVER_NAME in Jenkins job configuration to override
          def sonarServer = env.SONAR_SERVER_NAME ?: 'Sonar'
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
        // 2.6 La phase Notification - Succès
        // Notification de succès par email et Slack informant l'équipe du déploiement réussi
        def recipients = 'inima.a04@gmail.com'
        def branchName = env.BRANCH_NAME ?: 'main'
        def commitMessage = ''
        try {
          commitMessage = bat(
            script: 'git log -1 --pretty=%%B',
            returnStdout: true
          ).trim()
        } catch (Exception e) {
          commitMessage = 'Non disponible'
        }
        
        def emailBody = """
Le pipeline s'est terminé avec succès et le déploiement a été effectué avec succès.

Détails du build:
- Projet: ${env.JOB_NAME}
- Build: #${env.BUILD_NUMBER}
- Branche: ${branchName}
- Commit: ${commitMessage}
- URL du build: ${env.BUILD_URL}

Phases exécutées avec succès:
✓ Tests unitaires
✓ Analyse de code (SonarQube)
✓ Quality Gate
✓ Build (JAR + Documentation)
✓ Déploiement vers MyMavenRepo

L'équipe de développement peut maintenant utiliser l'artifact déployé.
"""
        
        // Notification par email
        try {
          emailext (
            subject: "✅ SUCCESS: Déploiement réussi - ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
            body: emailBody,
            to: recipients,
            mimeType: 'text/html'
          )
          echo "Notification email envoyée avec succès à ${recipients}"
        } catch (Exception e) {
          echo "Erreur lors de l'envoi de l'email: ${e.getMessage()}"
          echo "Vérifiez la configuration SMTP dans Jenkins: Manage Jenkins -> System -> Extended E-mail Notification"
        }
      }
    }
    failure {
      script {
        // 2.6 La phase Notification - Échec
        // Notification d'échec par email et Slack informant l'équipe de l'échec
        def recipients = 'inima.a04@gmail.com'
        def branchName =  env.BRANCH_NAME ?: 'main'
        
        def emailBody = """
Le pipeline a échoué lors de l'exécution.

Détails du build:
- Projet: ${env.JOB_NAME}
- Build: #${env.BUILD_NUMBER}
- Branche: ${branchName}
- URL du build: ${env.BUILD_URL}
- URL des logs: ${env.BUILD_URL}console

Le pipeline s'est arrêté en raison d'une erreur dans l'une des phases suivantes:
- Test
- Code Analysis
- Code Quality
- Build
- Deploy

Veuillez consulter les logs complets pour identifier la phase exacte en échec et les détails de l'erreur.
L'équipe de développement doit corriger les problèmes avant de relancer le pipeline.
"""
        
        // Notification par email
        try {
          emailext (
            subject: "❌ FAILED: Pipeline échoué - ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
            body: emailBody,
            to: recipients,
            mimeType: 'text/html'
          )
          echo "Notification email envoyée avec succès à ${recipients}"
        } catch (Exception e) {
          echo "Erreur lors de l'envoi de l'email: ${e.getMessage()}"
          echo "Vérifiez la configuration SMTP dans Jenkins: Manage Jenkins -> System -> Extended E-mail Notification"
        }
      }
    }
    always {
      script {
        // Log final pour traçabilité
        echo "Pipeline terminé. Statut: ${currentBuild.currentResult}"
      }
    }
  }
}
