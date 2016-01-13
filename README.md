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
