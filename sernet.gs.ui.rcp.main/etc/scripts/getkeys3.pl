#!/usr/bin/perl
# extract keys for properties from snca.xml, rule parameters only


while (<>) {
    chomp;
    if (m/param id="(.+?)".*?>(.*?)<\/param>/) {
        print $1."=".$2."\n";
    }

}
