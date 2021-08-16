# verinice

verinice helps you to build and operate your management system for
information security (ISMS). Whether you base it on ISO 27001, BSI IT
Baseline Protection, IDW PS 330 or another standard: verinice supports
you in your daily work as a CISO or IT Security Officer.

All relevant standards are either already integrated in the tool or can
be easily imported. All data is stored in an object model that is
tailored to the requirements of information security and is dynamically
expandable. This makes your data the basis for a sustainable IS process.

## Eclipse Rich Client Platform

verinice is a Java application. The graphical surface is implemented
with the Rich Client Platform (RCP). This makes verinice platform
independent while using the native GUI elements of the operating system.

Also part of the Eclipse platform is the BIRT Report Designer. All
verinice reports can be customized – and you’re able to design
completely new reports which can be exported as PDF, HTML or Excel (CSV)
file.

### Target Platform

The spec file for the verinice target platform is stored in
sernet.verinice.releng.tp. Add this folder as a project.

To build and run your project go to Window > Preferences > Plug-in
Development > Target Platform and select 'verinice-platform'

## Dynamic Object Model (HitroUI)

The HitroUI Framework is a part of verinice. A simple XML-file defines
all fields and field types which appear in the application. So the
database data and all displayed forms are generated dynamically.

This dynamic object model allows you to define additional data fields
for specific objects as needed or to remove unneeded fields from the
standard forms. That is how you can adapt verinice to your working
methods and the requirements of your organization.

## Databases


By using the object-relational mapper Hibernate, verinice is able to
connect with different database systems. The supported database systems
are:

- PostgreSQL
- Apache Derby
- Oracle DB

## Three-Tier Architecture


verinice uses a three-tier architecture where independent software
modules are implemented. A centralized database and an application
server provide data to the client.

The verinice.PRO application server complements the pure client with a
centralized IS repository hosted in your company. It enables multiple
people to work on one ISMS - even across different locations.

# Contributing to verinice via GitHub

- fork our repository on GitHub

- become familiar with our coding standards and read [verinice coding
  style](CODINGSTYLE.md)

- send a pull request for your branch through GitHub

- this will trigger an email to the verinice developer mailing list

- discussion happens on your pull request on github

- after your pull request is approved, we pull the branch in our
  internal repository, do the merge there and push it back to the
  GitHub mirror

# How to build
To build the Verinice client, client update site and server all 
at once execute the following command:

	./mvnw -Dtycho.disableP2Mirrors=true clean verify

To see where the Verinice client, client update site and server
build artifacts can then be found
read the following sections.

If you want to skip the junit tests you need to add the `-Dmaven.antrun.skip=true` parameter.

	./mvnw -Dtycho.disableP2Mirrors=true -Dmaven.antrun.skip=true  clean verify

## Verinice client

The built artifacts will be located in
`sernet.verinice.releng.client.product/target/products/`.
Artifacts for the following platforms will be produced:

* Linux GTK 64 bit
* Windows 64 bit
* Mac OS X 64 bit

If you want to pack a JRE into the build, you can copy the JRE to

sernet.verinice.extraresources.feature/linux/jre
sernet.verinice.extraresources.feature/windows/jre
sernet.verinice.extraresources.feature/macos/jre


Packing the JRE is required for macOS builds. It is
[best practice](https://docs.oracle.com/javase/7/docs/technotes/guides/jweb/packagingAppsForMac.html),
to bundle a JRE into an Application.app folder. Hence in order to
obtain a valid verinice.app a proper macOS JRE has to be present in
sernet.thirdparty.feature/macos.

## Verinice client update site

The P2 update site will be located in
`sernet.verinice.update_site/target/repository`.

## Verinice server

The WAR file (which can be deployed e.g. to Tomcat)
will be located under `sernet.verinice.releng.server.product/target/`.
The WAR file is of course platform independent (in contrast to
the Verinice client and report designer builds).

## Versioning
To update the version of the project

1.	run

		./mvnw -Dtycho.mode=maven -DnewVersion=x.y.z.qualifier tycho-versions:set-version

	Note that *qualifier* is meant literately and is treated as a *magic string*
	by tycho/osgi, i.e.

		./mvnw -DnewVersion=1.19.0.qualifier tycho-versions:set-version

	will write *1.19.0-SNAPSHOT* into update pom.xml files and *1.19.0.qualifier*
	into into updated feature.xml files. The final product version gets a
	timestamp, e.g.*1.19.0.201908011226*. See
	[Plugin Documentation](https://www.eclipse.org/tycho/sitedocs/tycho-packaging-plugin/plugin-info.html)
	for details.

2.	update version and codename in the about text in **Branding > About Dialog** of
	*sernet.verinice.releng.client.product/sernet.verinice.releng.client.product* and

3.	synchronize the about text with the plugin (**Overview > Testing >
	Synchronize**) again in *sernet.verinice.releng.client.product/sernet.verinice.releng.client.product*.

4.	Update the version macro in the manuals.

5.	Update the version in the splash screens of

	-	verinice

	*sernet.gs.ui.rcp.main/etc/splashscreen/splash.xcf*

