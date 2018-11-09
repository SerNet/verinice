FROM centos:7

RUN yum install -y gcc gcc-c++ \
                   libtool libtool-ltdl \
                   make cmake \
                   git \
                   pkgconfig \
                   sudo \
                   automake autoconf \
                   yum-utils rpm-build && \
    yum clean all

ENV FLAVOR=rpmbuild OS=centos DIST=el7

RUN useradd builder -u 1000 -m -G users,wheel && \
    mkdir /rpm && \
    chown -R builder /rpm

USER builder
WORKDIR /rpm
COPY --chown=builder SOURCES /rpm/SOURCES
COPY --chown=builder SPECS /rpm/SPECS

ENTRYPOINT rpmbuild --define "_topdir $PWD" --define "version ${VERSION:-develop}" -bb SPECS/verinicepro.spec
