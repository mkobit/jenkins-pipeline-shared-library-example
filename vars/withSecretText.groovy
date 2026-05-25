def call(String credentialsId, String variable, Closure body) {
    withCredentials([
        string(credentialsId: credentialsId, variable: variable)
    ]) {
        body()
    }
}
