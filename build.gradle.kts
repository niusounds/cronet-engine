// Top-level build file where you can add configuration options common to all sub-projects/modules.
tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}