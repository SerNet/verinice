#!/usr/bin/perl
# extract keys for properties from snca.xml, all ids and names where id comes
# first, some rows output multiple times, clean up by hand necessary.

my $id="";

while (<>) {
    chomp;
    if (m/id="(.*?)"/) {
        $id=$1;
    }
    if (m/name="(.*?)"/) {
        $name=$1;
    }
    {print "$id=$name\n"};
}
