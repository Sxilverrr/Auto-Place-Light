plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5.168" apply false
    id("com.gradleup.shadow") version "9.0.0" apply false
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

for (it in stonecutter.tree.branches) {
    if (it.id.isEmpty()) continue
    stonecutter registerChiseled tasks.register("chiseledBuild${it.id.upperCaseFirst()}", stonecutter.chiseled) {
        group = "project"
        versions { branch, _ -> branch == it.id }
        ofTask("buildAndCollect")
    }
}

for (it in stonecutter.tree.nodes) {
    if (it.metadata != stonecutter.current || it.branch.id.isEmpty()) continue
    for (type in listOf("Client", "Server")) tasks.register("runActive$type${it.branch.id.upperCaseFirst()}") {
        group = "project"
        dependsOn("${it.hierarchy}:run$type")
    }
}
