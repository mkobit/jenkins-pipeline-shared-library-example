def call() {
    def content = libraryResource('com/mkobit/libraryexample/greeting.txt')
    echo content.trim()
}
