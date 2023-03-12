plugins {
	id("io.papermc.paperweight.userdev") version "1.3.8"
}

dependencies {
	paperDevBundle("1.19.3-R0.1-SNAPSHOT")

	compileOnly("net.civmc.civmodcore:CivModCore:2.4.0:dev-all")

	compileOnly("org.projectlombok:lombok:1.18.24")
	annotationProcessor("org.projectlombok:lombok:1.18.24")
}
