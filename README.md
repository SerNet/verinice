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

To build the Verinice client, client update site, server and
report designer all at once execute the following commands:

1. `cd sernet.verinice.releng.parent`
1. `mvn -Dtycho.disableP2Mirrors=true clean verify`

To see where the Verinice client, client update site, server
and report designer build artifacts can then be found
read the following sections.

**CI build of the `develop` branch:**

[![Build Status](https://travis-ci.com/zaunerc/verinice-mirror.svg?token=dYtmMcJy99yawPLtZTwD&branch=develop)](https://travis-ci.com/zaunerc/verinice-mirror)

The Travis CI server builds the project using the following JDKs:

- OpenJDK 7
- OracleJDK 8

## Verinice client

The built artifacts will be located in
`sernet.verinice.releng.client.product/target/products/`.
Artifacts for the following platforms will be produced:

* Linux GTK 32 and 64 bit
* Windows 32 and 64 bit
* Mac OS X 64 bit

## Verinice client update site

The P2 update site will be located in
`sernet.verinice.update_site/target/repository`.

## Verinice server

The WAR file (which can be deployed e.g. to Tomcat)
will be located under `sernet.verinice.releng.server.product/target/`.
The WAR file is of course platform independent (in contrast to
the Verinice client and report designer builds).

## Verinice report designer

The built artifacts will be located in
`sernet.verinice.report.designer.tycho/target/products/`.
Artifacts for the following platforms will be produced:

* Linux GTK 32 and 64 bit
* Windows 32 and 64 bit
* Mac OS X 64 bit
