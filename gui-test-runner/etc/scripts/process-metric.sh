## process the csv data

cd gui-test-runner/result 


awk '
BEGIN {
	FS=","
}

{
	val = $2
	sums[$1] += val
	records[$1] += 1
	if (maxs[$1] < val) {
		maxs[$1] = val
	}
	if (mins[$1] > val || mins[$1] == 0) {
		mins[$1] = val
	}
}

END {
	for (group in sums) {        
		printf("%1$s-label,%1$s-sum,%1$s-avg,%1$s-min,%1$s-max,", group)
	}
	print ""
	for (group in sums) {        
		printf("%s,%d,%.1f,%d,%d, ", group, sums[group], sums[group]/records[group], mins[group], maxs[group])
	}
}
' performance-metric.csv > metric.csv
