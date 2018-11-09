# Simple SPEC file to build verinice RPMs from prepared web-app folder.
# Does not %build anything, expects the server war, client and update zip to
# be in SOURCES dir.
#
# This spec file expecst to variables to bet set
# - version : the release version of verinice
#
# So you can build RPMs running 
#
# 	rpmbuild --define "version 1.15.0" -ba verinicepro.spec
#
# RPM HOWTO (en): http://tldp.org/HOWTO/RPM-HOWTO/
# RPM HOWTO (de): http://www.linuxhaven.de/dlhp/HOWTO/DE-RPM-HOWTO.html

%define _binaries_in_noarch_packages_terminate_build 0
# repacking jar costs a lot of build time
%define __jar_repack 0

# servlet files (command service, scheduler, sync API et al)
Summary: 	Verinice.PRO Service Components
Name: 		verinicepro
Version: 	%{version}
# increment on every build with same Version number
Release:	1
License: 	EPL/LGPL/GPL/Apache
Group: 		Applications/System
URL:		http://verinice.org
Vendor:		SerNet Service Network GmbH
Packager:	SerNet Service Network GmbH
BuildArch:	noarch
Provides:       verinice-server
Requires:	java-1.8.0-openjdk,tomcat

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
mkdir -p %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-linux-x86_64.zip         %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-linux-x86.zip            %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-windows-x86_64.zip       %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-windows-x86.zip          %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients
cp -f %{_sourcedir}/verinice-macos-x86_64.zip         %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/clients

unzip -qo %{_sourcedir}/veriniceserver.war       -d %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/
mkdir %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/elasticsearch
mkdir %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/vnl

unzip -qo %{_sourcedir}/verinice-update-site.zip -d %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/Verinice-Update-Site-2010/

cp -r %{_sourcedir}/manual %{buildroot}%{_prefix}/share/tomcat/webapps/veriniceserver/manual

sh %{_sourcedir}/genfilelist %{buildroot} > files.txt

# IMPORTANT:
# files.txt is created in the install step
%files -f files.txt
%defattr(-,root,tomcat)

# servlet config files
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties.default
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/veriniceserver-plain.properties.local
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-ldap.properties
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/veriniceserver-osgi.properties
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/SNCA.xml
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/snca-messages.properties
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/snca-messages_de.properties
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/web.xml

%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/log4j.xml
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/server_hibernate.cfg.xml
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/server_hibernate_oracle.cfg.xml

%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-common.xml
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-plain.xml
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/gs/server/spring/veriniceserver-jbpm.xml

%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/analysis_de.json
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/analysis_en.json
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/search/mapping.json

%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskNotification.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskNotification_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskReminder.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/TaskReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/NotResponsibleReminder.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/NotResponsibleReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueFixed.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueFixed_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueNotFixed.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/IssueNotFixed_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/DeadlineReminder.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/DeadlineReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/AuditReminder.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/AuditReminder_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/GsmExecute.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/GsmExecute_de.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/gsm/IsmExecuteDescription.vm
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/classes/sernet/verinice/bpm/gsm/IsmExecuteDescription_de.vm

%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-auth-default.xml
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-auth.xml
%config(noreplace) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-auth.xml.bak
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-auth-messages.properties
%config %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/verinice-auth-messages_de.properties
%attr(775, root, tomcat) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/reportDeposit
%attr(775, root, tomcat) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/elasticsearch
%attr(775, root, tomcat) %{_prefix}/share/tomcat/webapps/veriniceserver/WEB-INF/vnl


%post
systemctl restart tomcat

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
Release:        1
Group:	    	Applications/System
Requires:	verinice-server

%description clients
Download area for verinice.PRO server where users can download archived clients to connect to the server. 

This package contains clients for the platforms Linux (x86), Linux (x86_64), Windows (x86), Windows (x86_64), MacOS.

%files clients
%{_prefix}/share/tomcat/webapps/veriniceserver/clients

######################################################################
# repository where installed clients look for updates
%package update-repo
Summary:	verinice.PRO client update site
Version: 	%{version}
Release:	1
Group:	 	Applications/System
Requires:	verinice-server

%description update-repo
The verinice.PRO client update site is used by all clients that rely on this verinice.PRO
server to look for updates automatically.

%files update-repo
%{_prefix}/share/tomcat/webapps/veriniceserver/Verinice-Update-Site-2010

%package manual
Summary:	Verinice.PRO manual
Version:	%{version}
Release:	1
Group: 		Applications/System
Requires:	verinice-server

%description manual
The manual for the verinice client as supplement for verinicepro.
After installing this package user can download the manual in the webfrontend of verinicepro.

%files manual
%{_prefix}/share/tomcat/webapps/veriniceserver/manual

%changelog
* Wed Jul 05 2017 bweissen
- v 1.14.0
* Wed Oct 05 2016 bweissen
- v 1.13.1
* Tue Sep 06 2016 bweissen
- v 1.13.0
* Wed Feb 10 2016 bweissen
- v 1.12.0
* Thu Aug 27 2015 dmurygin
- v 1.11
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Fri May 29 2015 bweissen
- v 1.10
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Tue May 19 2015 dmurygin
- v 1.10
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Thu Nov 20 2014 dmurygin
- v 1.9
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Tue Jul 29 2014 dmurygin
- v 1.8
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Fri May 09 2014 dmurygin
- v 1.7.0
- Check release notes at http://verinice.org/verinice-support/release-notes/
* Wed Sep 18 2013 dmurygin
- v 1.6.3
- Check release notes at http://verinicepro.org/release-notes/
* Wed Sep 18 2013 dmurygin
- v 1.6.3
- Check release notes at http://verinicepro.org/release-notes/
* Mon May 27 2013 dmurygin
- v 1.6.2
- Check release notes at http://verinicepro.org/release-notes/
