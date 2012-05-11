#!/usr/bin/perl
# extract keys for properties from snca.xml

my $id="";

while (<>) {
    chomp;
    if (m/huirelation.*id="(.*?)"/) {
        $id=$1;
        next;
    }
    if (m/name="(.*?)".*reversename/) {
        print $id."_name=".$1."\n";
    }
    if (m/name.*reversename="(.*?)"/) {
        print $id."_reversename=".$1."\n";
    }
    if (m/reversename.*tooltip="(.*?)"/) {
        print $id."_tooltip=".$1."\n";
    }

}
