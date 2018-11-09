The spec file expects the requred server war, client, update zip and manuals
(pdf and html dir) to be stored in SOURCES, e.g.

	$ ls SOURCES
	genfilelist
	manual
	verinice-linux-x86_64.zip
	verinice-linux-x86.zip
	verinice-macos-x86_64.zip
	veriniceserver.war
	verinice-update-site.zip
	verinice-windows-x86_64.zip
	verinice-windows-x86.zip
	$ ls SOURCES/manual
	verinice-Benutzerdokumentation
	verinice-Benutzerdokumentation.pdf
	verinice-user_manual_EN
	verinice-user_manual_EN.pdf

To build RPMs run:

	rpmbuild --define "_topdir $PWD" --define "version x.y.z" -ba SPECS/verinicepro.spec

Using Docker

	docker build -t verinice-rpm-rhel-7 . \
		&& docker run -e VERSION="x.y.z" -it verinice-rpm-rhel-7 \
		&& docker cp `docker ps -alq`:/rpm/RPMS .

You can then publish the build RPMs, e.g.

	cp -r RPMS/noarch /java/rpmbuild/rpmrepo-centos7 \
		&& createrepo --update -v /java/rpmbuild/rpmrepo-centos7

# Troubleshooting
## Proxy
See [docker proxy](https://docs.docker.com/network/proxy/).

