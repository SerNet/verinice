VERSION=

BUILDCMD=mvn -Dtycho.disableP2Mirrors=true -Dmaven.antrun.skip=true verify

PRODUCTDIR=sernet.verinice.releng.client.product/target/products

SERVERWAR=sernet.verinice.releng.server.product/target/sernet.verinice.releng.server.product-1.0.0.war

MANUALS=\
	doc/manual/de/verinice-Benutzerdokumentation.pdf \
	doc/manual/de/bin/verinice-Benutzerdokumentation \
	doc/manual/en/verinice-user_manual_EN.pdf \
	doc/manual/en/bin/verinice-user_manual_EN

all: version products docs dists

version:
	@test -n "$(VERSION)" || (echo "please specify a version: $(MAKE) VERSION=x.y.z TARGET"; exit 1)

# Build the application, i.e. client and server, if the product dir is not
# present. This is a workaround, since the maven target always builds
# everything. Also we use the fact, that serverwar and clients are always build
# together.
products:
	@if ! [ -e $(PRODUCTDIR) ];\
		then echo "$(BUILDCMD)";\
		$(BUILDCMD);\
	else\
		echo products already built;\
	fi

# Build the application and run the tests
tests:
	mvn -Dtycho.disableP2Mirrors=true test

# Build the documentation
docs:
	$(MAKE) -C doc/manual all

# Build distribution packages
dists: version dist/rhel-6/RPMS dist/rhel-7/RPMS

dist/rhel-6/RPMS: dist/rhel-6/SPECS/verinicepro.spec dist/rhel-6/Dockerfile products docs
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-linux.gtk.x86.zip dist/rhel-6/SOURCES/verinice-linux-x86_64.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-linux.gtk.x86_64.zip dist/rhel-6/SOURCES/verinice-linux-x86.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-macosx.cocoa.x86_64.zip dist/rhel-6/SOURCES/verinice-macos-x86_64.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-win32.win32.x86.zip dist/rhel-6/SOURCES/verinice-windows-x86.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-win32.win32.x86_64.zip dist/rhel-6/SOURCES/verinice-windows-x86_64.zip
	cp $(SERVERWAR) dist/rhel-6/SOURCES/veriniceserver.war
	zip -qr dist/rhel-6/SOURCES/verinice-update-site.zip sernet.verinice.releng.client.product/target/repository
	mkdir -p dist/rhel-6/SOURCES/manual
	cp -rf $(MANUALS) dist/rhel-6/SOURCES/manual

	docker build -t verinice-rpm-rhel-6 dist/rhel-6
	docker run -e VERSION="$(VERSION)" verinice-rpm-rhel-6
	docker cp `docker ps -alq`:/rpm/RPMS dist/rhel-6
	touch $@

dist/rhel-7/RPMS: dist/rhel-7/SPECS/verinicepro.spec dist/rhel-7/Dockerfile products docs
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-linux.gtk.x86.zip dist/rhel-7/SOURCES/verinice-linux-x86_64.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-linux.gtk.x86_64.zip dist/rhel-7/SOURCES/verinice-linux-x86.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-macosx.cocoa.x86_64.zip dist/rhel-7/SOURCES/verinice-macos-x86_64.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-win32.win32.x86.zip dist/rhel-7/SOURCES/verinice-windows-x86.zip
	cp $(PRODUCTDIR)/sernet.verinice.releng.client-win32.win32.x86_64.zip dist/rhel-7/SOURCES/verinice-windows-x86_64.zip
	cp $(SERVERWAR) dist/rhel-7/SOURCES/veriniceserver.war
	zip -qr dist/rhel-7/SOURCES/verinice-update-site.zip sernet.verinice.releng.client.product/target/repository
	mkdir -p dist/rhel-7/SOURCES/manual
	cp -rf $(MANUALS) dist/rhel-7/SOURCES/manual

	docker build -t verinice-rpm-rhel-7 dist/rhel-7
	docker run -e VERSION="$(VERSION)" verinice-rpm-rhel-7
	docker cp `docker ps -alq`:/rpm/RPMS dist/rhel-7
	touch $@

clean:
	mvn -Dtycho.mode=maven clean
	$(MAKE) -C doc/manual clean
	rm -rf dist/rhel-6/RPMS
	rm -rf dist/rhel-6/SOURCES/manual
	rm -f dist/rhel-6/SOURCES/*.zip
	rm -f dist/rhel-6/SOURCES/veriniceserver.war
	rm -rf dist/rhel-7/RPMS
	rm -rf dist/rhel-7/SOURCES/manual
	rm -f dist/rhel-7/SOURCES/*.zip
	rm -f dist/rhel-7/SOURCES/veriniceserver.war

