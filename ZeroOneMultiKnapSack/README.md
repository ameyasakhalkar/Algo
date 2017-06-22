The program reads following inputs from STDIN:<br>
n: #candidate items<br>
k: #buckets<br>
weights of each of the n candidate iterms<br>
size (weight) of each of the k buckets<br>
values of each of the n candidate items<br>
<br>
e.g. <br>
4<br>
2<br>
45 25 25 20<br>
50 50 <br>
45 50 25 30<br>
<br>
It optionally accepts a boolean argument on the command-line. If the value is true, it<br>
prints debug logs to STDOUT. By default, debug logging is turned off.<br>
<br>
e.g. <b>java ZeroOneMultiKnapSack</b> would not print any debug logs, whereas<br>
<b>java ZeroOneMultiKnapSack true</b> would print them.<br>
