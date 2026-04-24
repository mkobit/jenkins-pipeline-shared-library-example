def call(int maxAttempts = 3, Closure body) {
  for (int attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      body()
      return
    } catch (Exception e) {
      if (attempt == maxAttempts) {
        throw e
      }
      echo "Attempt ${attempt} failed, retrying (${maxAttempts - attempt} remaining)..."
    }
  }
}
