/** Loads and echoes a greeting bundled in the library's {@code resources/} directory. */
def call() {
    def content = libraryResource('com/mkobit/libraryexample/greeting.txt')
    echo content.trim()
}
