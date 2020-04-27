import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.buildReportTab

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {
    description = "Contains all other projects"

    template(MavenBuild)
    template(DeployBuild)

    features {
        buildReportTab {
            id = "PROJECT_EXT_1"
            title = "Code Coverage"
            startPage = "coverage.zip!index.html"
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }
}

object MavenBuild : Template({
    id("MavenBuild")
    name = "MavenBuild"

    artifactRules = "target/*.jar"
    buildNumberPattern = "1.%build.counter%.0"

    steps {
        maven {
            goals = "versions:set versions:commit -DnewVersion=%system.build.number%"
        }
    }

})

object DeployBuild : Template({
    name = "DeployBuild"

    type = Type.DEPLOYMENT

    params {
        text("deploy.environment.name", "", display = ParameterDisplay.HIDDEN, allowEmpty = false)
    }

    steps {
        script {
            scriptContent = """
                echo "Deploying %system.build.number% to env %deploy.environment.name%"
                dir
                """.trimMargin()
        }
    }

})