#
# Simple SPEC file to build verinice RPMs from prepared web-app folder.
# Does not %build anything, expects the directory structure already in 
# build-root.
#
# RPM HOWTO (en): http://tldp.org/HOWTO/RPM-HOWTO/
# RPM HOWTO (de): http://www.linuxhaven.de/dlhp/HOWTO/DE-RPM-HOWTO.html
# 2018-01-08
#	Unify file with SNAPSHOT version
#	Set $post scriptlets
# 2016-01-21
#       Since Jan. 2016 since file is commited to a GIT repository
#       See commit messages for a changelog
# 2015-08-05
#	Initial version for vn 1.11
# 	File attributes set for elasticsearch folder:../WEB-INF/elasticsearch/
# 2015-05-21
#       Package verinicepro-catalogs activated to build a new RPM with English catalog
#       SNCA.xml, snca-messages.properties und snca-messages_de.properties set to (noreplace) 
# 2015-05-13
#       Initial version for vn 1.10
# 2014-12-10
#       Initial version for vn 1.9.1
# 2014-11-10
#       File attributes set for report repository folder:../WEB-INF/reportDeposit/
# 2014-10-22
#       Initial version for vn 1.9
# 2014-06-18
#       Initial version for vn 1.8
# 2014-02-20
#       Initial version for vn 1.7.0
#       sernet/verinice/bpm/GsmExecute[_de].vm added to config files
# 2013-10-22
#       Initial version for vn 1.6.4
#       veriniceserver-jbpm.xml added to config files
# 2013-06-18
#       Initial version for vn 1.6.3
# 2013-05-13
#       New config files for GSM workflow added
# 2013-03-04
#       Initial version for vn 1.6.2
# 2012-12-14
#       Package portal disabled
# 2012-12-12
#       Initial version for vn 1.6.1
# 2012-10-19
#       New property files added.
# 2012-08-30
#       Initial version for vn 1.6.0
#       Additinal files from verinicepro-workflow added
# 2012-05-29
#       Initial version for vn 1.5.3
#       Conf. files added: sernet/gs/server/spring/veriniceserver-common.xml
#       ../veriniceserver-plain.xml
# 2012-04-24
#       Initial version for vn 1.5.2
# 2012-02-08 
#       Initial version for vn 1.5.1
#       New conf. files: ../WEB-INF/verinice-auth-messages[_de].properties
# 2012-01-16
#       Initial version for vn 1.5.0
# 2011-11-21
#       Initial version for vn 1.4.2
#       New conf files: ../WEB-INF/verinice-auth[-default].xml[.bak]
# 2011-09-24
#       Initial version for vn 1.4.1
# 2011-09-19
#       Initial version for vn 1.4.0
# 2011-08-22
#       Task notification templates added to config files
# 2011-06-09
#       initial version for vn 1.3.2 
# 2011-05-16
#       initial version for vn 1.3.1
# 2011-04-19
#       /etc/init:.d/tomcat6 start/stop removed from pre and post section
# 2011-04-05
#       Provides virtual package verinice-server
# 2011-03-28
#       Now conflicts with veriniceweb
# 2011-03-11
#       initial version for vn 1.3.0
# 2011-02-17
#       release vn 1.2.3
# 2011-02-15 dm@sernet.de
#       New configuration file WebContent/WEB-INF/verinice-ldap.properties
# 2011-01-25 dm@sernet.de
#       initial verion for vn 1.2.3
# 2011-01-17 dm@sernet.de
#       release vn 1.2.2
# 2010-12-20 dm@sernet.de
#       initial version for vn 1.2.2
# 2010-11-12 dm@sernet.de
#       release vn 1.2.0
# 2010-07-19 dm@sernet.de
#       XML import config files added
#       tomcat start and stop in pre and post section
#       delete old verincie jars from WEB-INF/lib in install section
# 2010-06-17 dm@sernet.de
#       documentation added
# 2010-04-30 akoderman@sernet.de
# 	release vn 1.1.1
# 2010-03-18 akoderman@sernet.de
# 	release vn 1.1
# 2009-11-05 akoderman@sernet.de
# 	initial version for verinice 1.0.16

%define _binaries_in_noarch_packages_terminate_build 0
# repacking jar costs a lot of build time
%define __jar_repack 0

# servlet files (command service, scheduler, sync API et al)
Summary: 	Verinice.PRO Service Components
Name: 		verinicepro
Version: 	%{version}
# increment on every build with same Version number
Release:	2
License: 	EPL/LGPL/GPL/Apache
Group: 		Applications/System
BuildRoot:	%{_builddir}/%{name}-root
URL:		http://verinice.org
Vendor:		SerNet Service Network GmbH
Packager:	SerNet Service Network GmbH
Prefix:		/usr/share
BuildArch:	noarch
Provides:       verinice-server
Requires:	java-1.8.0-openjdk,jpackage-utils-compat-el5,tomcat6

%description
The main package for the verinice.PRO server. This package contains the command
service, scheduler, synchronisation API, sattelite server for updates of
clients in the LAN and other remotely available services.

The verinice.PRO server accesses the backend database and synchronizes and
speeds up multiple client connections. It offers additional features such as
mail notifications for certain events. 

An optional portal web frontend is available that allows access to a client
download repository and a web frontend for the delegation of individual tasks.

%pre
# nothing to do

%build
# nothing to do

%install
mkdir -p %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-linux-x86_64.zip         %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-linux-x86.zip            %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-windows-x86_64.zip       %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-windows-x86.zip          %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-macos-x86_64.zip         %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/clients

unzip -qo %{_sourcedir}/veriniceserver.war       -d %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/
mkdir %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/elasticsearch
mkdir %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/vnl

unzip -qo %{_sourcedir}/verinice-update-site.zip -d %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/Verinice-Update-Site-2010/

cp -r %{_sourcedir}/manual %{buildroot}%{_prefix}/share/tomcat6/webapps/veriniceserver/manual

sh %{_sourcedir}/genfilelist %{buildroot} > files.txt

# IMPORTANT:
# files.txt is created by running ./filelist.sh in the BUILD directory
%files -f files.txt
%defattr(-,root,tomcat)

# servlet config files
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties.default
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties.local
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-ldap.properties
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/veriniceserver-osgi.properties
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/SNCA.xml
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/snca-messages.properties
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/snca-messages_de.properties
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/web.xml

%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/log4j.xml
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/server_hibernate.cfg.xml
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/server_hibernate_oracle.cfg.xml

%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-common.xml
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-plain.xml
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-jbpm.xml

%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/analysis_de.json
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/analysis_en.json
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/mapping.json

%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskNotification.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskNotification_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskReminder.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/NotResponsibleReminder.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/NotResponsibleReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueFixed.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueFixed_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueNotFixed.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueNotFixed_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/DeadlineReminder.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/DeadlineReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/AuditReminder.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/AuditReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/GsmExecute.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/GsmExecute_de.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/gsm/IsmExecuteDescription.vm
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/gsm/IsmExecuteDescription_de.vm

%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-auth-default.xml
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-auth.xml
%config(noreplace) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-auth.xml.bak
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-auth-messages.properties
%config %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/verinice-auth-messages_de.properties
%attr(775, root, tomcat) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/reportDeposit
%attr(775, root, tomcat) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/elasticsearch
%attr(775, root, tomcat) %{_prefix}/share/tomcat6/webapps/veriniceserver/WEB-INF/vnl


%post
alternatives --set java /usr/lib/jvm/jre-1.8.0-openjdk.x86_64/bin/java
service tomcat6 restart

%clean
# nothing to do (do not clean the build directory)

%changelog
* Thu Feb 01 2018 Alexander Ben Nasrallah <an@sernet.de> 1.15.0
--Initial spec file for CentOS 7.

######################################################################
# download area with ZIPped clients for different platforms
%package clients
Summary:    	verinice.PRO client downloads
Version:	%{version}
Release:        2
Group:	    	Applications/System
Requires:	verinice-server

%description clients
Download area for verinice.PRO server where users can download archived clients to connect to the server. 

This package contains clients for the platforms Linux (x86), Linux (x86_64), Windows (x86), Windows (x86_64), MacOS.

%files clients
%{_prefix}/share/tomcat6/webapps/veriniceserver/clients

######################################################################
# repository where installed clients look for updates
%package update-repo
Summary:    	verinice.PRO client update site
Version: 	%{version}
Release:	2
Group:	    	Applications/System
Requires:	verinice-server

%description update-repo
The verinice.PRO client update site is used by all clients that rely on this verinice.PRO
server to look for updates automatically.

%files update-repo
%{_prefix}/share/tomcat6/webapps/veriniceserver/Verinice-Update-Site-2010

%package manual
Summary:	Verinice.PRO manual
Version:	%{version}
Release:	1
Group:		Applications/System
Requires:	verinice-server

%description manual
The manual for the verinice client as supplement for verinicepro.
After installing this package user can download the manual in the webfrontend of verinicepro.

%files manual
%{_prefix}/share/tomcat6/webapps/veriniceserver/manual

