
rootProject.name = "mctennis"

// Reference common as sub project
include(":common")
project(":common").projectDir = file("../MCUtils/MCUtils.common/common")
include(":arena")
project(":arena").projectDir = file("../MCUtils/MCUtils.arena/arena")
include(":packet")
project(":packet").projectDir = file("../MCUtils/MCUtils.packet/packet")
include(":ball")
project(":ball").projectDir = file("../MCUtils/MCUtils.ball/ball")

pluginManagement {
    plugins {
        kotlin("jvm") version "1.6.21"
    }
}
