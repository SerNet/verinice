#!/usr/bin/perl
# extract keys for properties from snca.xml, property tooltips only


while (<>) {
    chomp;
    if (m/param id="(.+?)".*?>(.*?)<\/param>/) {
        print $1."=".$2."\n";
    }

}
