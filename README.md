iCasa
=====

This project aims at providing a simulator to do functional testing of digital home application based on iCasa runtime.
The home page of the project is <http://adeleresearchgroup.github.com/iCasa/>.
The project websites are:
 * Development version <http://adeleresearchgroup.github.com/iCasa/snapshot/>
 * Last stable release version <http://adeleresearchgroup.github.com/iCasa/1.2.6/>

License
=====

This project relies on Apache v2 license (<http://www.apache.org/licenses/LICENSE-2.0.html>) for the gateway part and
a specific end user license for the simulator and dashboard web applications
(http://adeleresearchgroup.github.io/iCasa/1.2.1/license.html).

Contributors
=====

This project has been created by the LIG laboratory of Grenoble University.
The main contributors are 
- _ADELE Reseach Group_


Source Organization (to be changed)
====

- _distribution_: Contains ready to use distributions of client and server simulator parts.
- _ihm_: Contains android applications, the simulator web application and the dashboard web application.
- _packages_: Contains deployment packages of client and server simulator parts.
- _parent_: Contains the global build configuration including licensing information.
- _simulator_: Contains server part of the simulator.
- _src_: Contains the project web site source code.
- _tests_: Contains automatic tests (integration and acceptance tests) of the simulator.
- _tools_: Contains optional tools and components that you can use with the simulator.

How to use it
=====

Use a distribution
----

1. Install jdk 6 or greater
2. Unzip one of the distribution
3. Execute startGateway.sh file on Unix (or startGateway.bat file on Windows)

If your distribution contains simulator Web application
4. Execute startSimulatorGUI.sh on Unix (or startSimulatorGUI.bat file on Windows)

If your distribution contains dashboard Web application
5. Execute startDashboardGUI.sh on Unix (or startSimulatorGUI.bat file on Windows)

Build
=====

Prerequisites
-----

- install Maven 3.x
- install jdk 6 or greater

Instructions
----

Use the following command to compile the project
> mvn clean install

Continuous Integration
----

The project is built every week on the following continuous integration server :
<https://icasa.ci.cloudbees.com/>

Maven Repositories
----

```xml
<!-- Project repositories -->
<repositories>

    <!-- ADELE repositories -->
    <repository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>adele-central-release</id>
        <name>adele-repos</name>
        <url>http://repository-icasa.forge.cloudbees.com/release/</url>
    </repository>
    <repository>
        <snapshots />
        <id>snapshots</id>
        <name>adele-central-snapshot</name>
        <url>http://repository-icasa.forge.cloudbees.com/snapshot/</url>
    </repository>
</repositories>
<pluginRepositories>
    <pluginRepository>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
        <id>central</id>
        <name>adele-repos</name>
        <url>http://repository-icasa.forge.cloudbees.com/release/</url>
    </pluginRepository>
    <pluginRepository>
        <snapshots />
        <id>snapshots</id>
        <name>adele-central-snapshot</name>
        <url>http://repository-icasa.forge.cloudbees.com/snapshot/</url>
    </pluginRepository>
</pluginRepositories>
```

Contribute to this project
====

Released Version semantic
----

 major.minor.revision 

 * _major_ changed when there are modification or addition in the functionalities. 
 * _minor_ changed when minor features or critical fixes have been added.
 * _revision_ changed when minor bugs are fixed.

Developer Guidelines
----
 
If you want to contribute to this project, you MUST follow the developper guidelines:
- Use Sun naming convention in your code.
- You should prefix private class member by an underscore (e.g. : _bundleContext).
- All project directory names must be lower case without dots (you can use - instead of underscores).
- All packages must start with fr.liglab.adele.icasa
- All Maven artifact group id must be fr.liglab.adele.icasa
- All maven artifact id must not contain fr.liglab.adele.icasa and must be lower case (cannot use underscore, prefer dots)
- All maven project pom.xml file must inherent from parent pom (group id = fr.liglab.adele.icasa and artifact id = parent)
