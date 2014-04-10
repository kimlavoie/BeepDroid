set autoscale
unset log
unset label
set title "Message processing time in BeepDroid"
set ylabel "Time (ms)"
set xlabel "Number of messages"
plot "beepdroid.dat" using 1:2 title "message/time" with lines