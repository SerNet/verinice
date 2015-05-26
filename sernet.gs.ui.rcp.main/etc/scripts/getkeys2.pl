#!/usr/bin/perl
# extract keys for properties from snca.xml, property tooltips only

my $id="";

while (<>) {
    chomp;
    if (m/id="(.*?)"/) {
        $id=$1;
        next;
    }
    if (m/^\s*tooltip="(.+?)"/) {
        print $id."_tooltip=".$1."\n";
    }

}
