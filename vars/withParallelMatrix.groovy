import com.cloudbees.groovy.cps.NonCPS
import com.mkobit.libraryexample.BasicScriptStepsLogger

def call(Map axes, Closure body) {
    def log = new BasicScriptStepsLogger(this, 'withParallelMatrix')
    def combinations = cartesianProduct(axes)
    def stages = [:]
    for (combo in combinations) {
        def captured = combo
        stages[stageLabel(captured)] = { body(captured) }
    }
    log.info("Running ${stages.size()} parallel combinations")
    parallel stages
}

@NonCPS
private List<Map> cartesianProduct(Map axes) {
    List<String> keys = new ArrayList<>(axes.keySet())
    List<List> result = [[]]
    for (def key in keys) {
        result = result.collectMany { existing ->
            (axes[key] as List).collect { v ->
                existing + [v]
            }
        }
    }
    result.collect { vals ->
        Map m = [:]
        keys.eachWithIndex { k, i -> m[k] = vals[i] }
        m
    }
}

@NonCPS
private String stageLabel(Map combo) {
    combo.collect { k, v -> "${k}=${v}" }.join(', ')
}
