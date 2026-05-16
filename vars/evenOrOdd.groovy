/**
 * Runs an "Even Stage" or "Odd Stage" declarative pipeline depending on {@code buildNumber} parity.
 * See: https://www.jenkins.io/doc/book/pipeline/shared-libraries/#pretesting-library-changes
 */
def call(int buildNumber) {
    if (buildNumber % 2 == 0) {
        pipeline {
            agent any
            stages {
                stage('Even Stage') {
                    steps {
                        echo 'The build number is even'
                    }
                }
            }
        }
    } else {
        pipeline {
            agent any
            stages {
                stage('Odd Stage') {
                    steps {
                        echo 'The build number is odd'
                    }
                }
            }
        }
    }
}
