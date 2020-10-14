/*
 * Copyright 2020 Datalogics, Inc.
 */

pipeline {
    agent {
        label 'pdfjt-linux'
    }

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "M3"
        jdk 'AdoptOpenJDK 8'
    }

    stages {
        stage('Build and Deploy') {
            when {
                anyOf {
                    branch 'develop'
                }
            }

            steps {
                withMaven(jdk: 'AdoptOpenJDK 8', maven: 'M3') {
                    // Run Maven on a Unix agent.
                    sh "./mvnw -B -V -U clean dependency:tree deploy -P integration-tests,distributed-samples"
                }

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }

        }

        stage('Build and Deploy Lite') {
            when {
                anyOf {
                    branch 'develop'
                }
            }

            steps {
                withMaven(jdk: 'AdoptOpenJDK 8', maven: 'M3') {
                    // Run Maven on a Unix agent.
                    sh "./mvnw -B -V -U -f lite/pom.xml clean dependency:tree deploy -P integration-tests,generate-distribution"
                }

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }

        }

        stage('Test Pull Request') {
            when {
                changeRequest()
            }

            steps {
                withMaven(jdk: 'AdoptOpenJDK 8', maven: 'M3') {
                    // Run Maven on a Unix agent.
                    sh "./mvnw -V -B -U clean dependency:tree install -P integration-tests,distributed-samples"
                }

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }
        }

        stage('Test Pull Request Lite') {
            when {
                changeRequest()
            }

            steps {
                withMaven(jdk: 'AdoptOpenJDK 8', maven: 'M3') {
                    // Run Maven on a Unix agent.
                    sh "./mvnw -f lite/pom.xml -V -B -U clean dependency:tree install -P integration-tests,generate-distribution"
                }

                // To run Maven on a Windows agent, use
                // bat "mvn -Dmaven.test.failure.ignore=true clean package"
            }
        }

        stage('Analysis') {
            steps {
                recordIssues enabledForFailure: true, healthy: 1, tools: [checkStyle(), findBugs(useRankAsPriority: true), pmdParser(), cpd(), javaDoc(), java()]
            }
        }

        stage('Changed files check') {
            when {
                anyOf {
                    branch 'develop'
                    changeRequest()
                }
            }
            steps {
                sh "git diff --stat --exit-code || (echo '[ERROR] Build changed some Git files' 1>&2; exit 1)"
            }
        }
    }
}
